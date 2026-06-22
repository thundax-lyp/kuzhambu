import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Select } from "antd";
import { useMemo, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { WangqiDocumentList } from "./components/wangqi-document-list";
import { WangqiDocumentModel } from "./components/wangqi-document-model";
import { WangqiStorageFilePanel } from "./components/wangqi-storage-file-panel";
import { WangqiTimeline } from "./components/wangqi-timeline";
import { WangqiVersionHistoryPanel } from "./components/wangqi-version-history-panel";
import * as wangqiService from "./wangqi-service";
import type { WangqiDocumentCommand, WangqiDocumentQuery } from "./wangqi-service";
import type { WangqiContentVersionRecord, WangqiDocumentRecord } from "./wangqi-types";
import "./wangqi-page.css";

type WangqiVisibilityFilter = "ALL" | "PUBLIC" | "PRIVATE";
type WangqiSortDirectionFilter = "ASC" | "DESC";

interface WangqiFilters {
    sortDirection: WangqiSortDirectionFilter;
    visibility: WangqiVisibilityFilter;
}

const DEFAULT_WANGQI_FILTERS: WangqiFilters = {
    sortDirection: "DESC",
    visibility: "ALL"
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readVisibilityValue = (visibility: WangqiVisibilityFilter) => {
    return visibility === "ALL" ? undefined : visibility;
};

export const WangqiPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<WangqiDocumentQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        sortDirection: DEFAULT_WANGQI_FILTERS.sortDirection
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<WangqiFilters>(DEFAULT_WANGQI_FILTERS);
    const [editorMode, setEditorMode] = useState<"create" | "edit">("create");
    const [editorOpen, setEditorOpen] = useState(false);
    const [editingDocument, setEditingDocument] = useState<WangqiDocumentRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);

    const hasActiveFilters = Boolean(
        filters.visibility !== "ALL" ||
        filters.sortDirection !== DEFAULT_WANGQI_FILTERS.sortDirection
    );

    const pageQuery = useQuery({
        queryKey: ["wangqi", "page", query],
        queryFn: () => wangqiService.page(query),
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["wangqi", "detail", editingDocument?.id],
        queryFn: () => wangqiService.get(editingDocument?.id ?? 0),
        enabled: editorOpen && editorMode === "edit" && Boolean(editingDocument?.id),
        retry: false
    });
    const activeDocument = detailQuery.data || editingDocument;
    const sourceFileQuery = useQuery({
        queryKey: ["wangqi", "source-file", activeDocument?.id, activeDocument?.storageObjectId],
        queryFn: () => wangqiService.getSourceFile(activeDocument?.id ?? 0),
        enabled: editorOpen && Boolean(activeDocument?.id && activeDocument?.storageObjectId),
        retry: false
    });
    const versionsQuery = useQuery({
        queryKey: ["wangqi", "versions", activeDocument?.id],
        queryFn: () => wangqiService.listVersions(activeDocument?.id ?? 0),
        enabled: editorOpen && editorMode === "edit" && Boolean(activeDocument?.id),
        retry: false
    });
    const versionDetailQuery = useQuery({
        queryKey: ["wangqi", "version", activeDocument?.id, selectedVersionId],
        queryFn: () => wangqiService.getVersion(activeDocument?.id ?? 0, selectedVersionId ?? 0),
        enabled: editorOpen && Boolean(activeDocument?.id && selectedVersionId),
        retry: false
    });

    const pageResult = pageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const versions = useMemo(() => versionsQuery.data || [], [versionsQuery.data]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => version.id === selectedVersionId) ||
        null;

    const invalidateWangqi = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["wangqi", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "detail"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "source-file"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "versions"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "version"] })
        ]);
    };

    const updateQuery = (values: Partial<WangqiDocumentQuery>) => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...values,
            pageNo: values.pageNo || DEFAULT_PAGE_NO,
            pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
        }));
    };

    const saveMutation = useMutation({
        mutationFn: (command: WangqiDocumentCommand) =>
            editorMode === "create" ? wangqiService.add(command) : wangqiService.update(command),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingDocument(null);
            setSelectedVersionId(null);
            await invalidateWangqi();
            messageApi.success(editorMode === "create" ? "王圻文档已新增" : "王圻文档已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: wangqiService.deleteById,
        onSuccess: async () => {
            await invalidateWangqi();
            messageApi.success("王圻文档已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const uploadSourceFileMutation = useMutation({
        mutationFn: ({ documentId, file }: { documentId: number; file: File }) =>
            wangqiService.uploadSourceFile(documentId, file),
        onSuccess: async () => {
            await invalidateWangqi();
            messageApi.success("王圻原始文件已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "原始文件上传失败");
        }
    });
    const resetVersionMutation = useMutation({
        mutationFn: ({ documentId, versionId }: { documentId: number; versionId: number }) =>
            wangqiService.resetVersion(documentId, versionId),
        onSuccess: async () => {
            setSelectedVersionId(null);
            await invalidateWangqi();
            messageApi.success("王圻版本已恢复");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "版本恢复失败");
        }
    });

    const searchWangqi = (value: string) => {
        setSearchText(value);
        updateQuery({ keyword: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            visibility: readVisibilityValue(filters.visibility),
            sortDirection: filters.sortDirection
        });
    };

    const sortWangqiDocuments = (sortDirection: WangqiSortDirectionFilter) => {
        setFilters((currentFilters) => ({
            ...currentFilters,
            sortDirection
        }));
        updateQuery({ sortDirection });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_WANGQI_FILTERS);
        updateQuery({
            visibility: undefined,
            sortDirection: DEFAULT_WANGQI_FILTERS.sortDirection
        });
    };

    const openCreateEditor = () => {
        setEditorMode("create");
        setEditingDocument(null);
        setSelectedVersionId(null);
        setEditorOpen(true);
    };

    const openEditEditor = (document: WangqiDocumentRecord) => {
        setEditorMode("edit");
        setEditingDocument(document);
        setSelectedVersionId(null);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingDocument(null);
        setSelectedVersionId(null);
    };

    const deleteDocument = (document: WangqiDocumentRecord) => {
        const title = document.title?.trim() || `文档 ${document.id}`;
        confirm.danger({
            title: "删除王圻文档",
            message: `确认删除 ${title}？`,
            description: "删除后该文档、版本历史和关联引用会按后端流程清理。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(document.id)
        });
    };

    const uploadSourceFile = (file: File) => {
        if (!activeDocument?.id) {
            messageApi.warning("请先保存王圻文档后再上传原始文件");
            return;
        }
        uploadSourceFileMutation.mutate({ documentId: activeDocument.id, file });
    };

    const resetVersion = (version: WangqiContentVersionRecord) => {
        if (!activeDocument?.id) {
            return;
        }
        confirm.danger({
            title: "恢复王圻版本",
            message: `确认恢复版本 ${version.versionNo ?? version.id}？`,
            description: "恢复后会产生新的历史恢复版本，并刷新详情、列表、时间线和原始文件元数据。",
            okText: "恢复",
            onConfirm: () =>
                resetVersionMutation.mutateAsync({
                    documentId: activeDocument.id,
                    versionId: version.id
                })
        });
    };

    return (
        <>
            <KuzhambuListPage<WangqiDocumentRecord>
                pageClassName="wangqi-page"
                title="王圻文档"
                description="王圻古籍文档管理入口。"
                subjectName="王圻文档"
                enableSearch
                enableFilter
                enableAdd
                addText="新增王圻文档"
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "visibility",
                        label: "可见性",
                        render: () => (
                            <Select
                                aria-label="王圻文档可见性"
                                value={filters.visibility}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "PUBLIC", label: "公开" },
                                    { value: "PRIVATE", label: "私有" }
                                ]}
                                onChange={(value) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        visibility: value
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "sortDirection",
                        label: "排序",
                        render: () => (
                            <Select
                                aria-label="王圻文档排序方向"
                                value={filters.sortDirection}
                                options={[
                                    { value: "DESC", label: "新到旧" },
                                    { value: "ASC", label: "旧到新" }
                                ]}
                                onChange={(value) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        sortDirection: value
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onAdd={openCreateEditor}
                pageActions={
                    <WangqiTimeline
                        loading={pageQuery.isLoading}
                        dataSource={records}
                        onOpenDocument={openEditEditor}
                    />
                }
                searchPlaceholder="搜索王圻文档标题、摘要或正文"
                searchValue={searchText}
                onSearchChange={searchWangqi}
                content={
                    <WangqiDocumentList
                        loading={pageQuery.isLoading}
                        dataSource={records}
                        onDelete={deleteDocument}
                        onOpenEdit={openEditEditor}
                        onSortDirectionChange={sortWangqiDocuments}
                        pagination={{
                            current: currentPageNo,
                            pageSize: currentPageSize,
                            total: totalCount,
                            onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                        }}
                        sortDirection={query.sortDirection || DEFAULT_WANGQI_FILTERS.sortDirection}
                    />
                }
            />
            <WangqiDocumentModel
                document={activeDocument}
                loading={detailQuery.isLoading}
                mode={editorMode}
                open={editorOpen}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={(command) => saveMutation.mutate(command)}
                afterForm={
                    editorMode === "edit" && activeDocument ? (
                        <div className="wangqi-page-drawer-panels">
                            <AiCandidatePanel
                                capabilities={["summary", "tags", "qa"]}
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onApplied={async () => {
                                    await invalidateWangqi();
                                }}
                            />
                            <WangqiStorageFilePanel
                                document={activeDocument}
                                loading={sourceFileQuery.isLoading || sourceFileQuery.isFetching}
                                sourceFile={sourceFileQuery.data}
                                uploading={uploadSourceFileMutation.isPending}
                                onRefresh={() => void sourceFileQuery.refetch()}
                                onUpload={uploadSourceFile}
                            />
                            <WangqiVersionHistoryPanel
                                currentDocument={activeDocument}
                                detailLoading={versionDetailQuery.isLoading}
                                listLoading={versionsQuery.isLoading}
                                resetting={resetVersionMutation.isPending}
                                selectedVersion={selectedVersion}
                                versions={versions}
                                onSelectVersion={(version) => setSelectedVersionId(version.id)}
                                onResetVersion={resetVersion}
                            />
                        </div>
                    ) : null
                }
            />
        </>
    );
};
