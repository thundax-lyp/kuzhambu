import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Card, Select } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { ClassicsContentQaPanel } from "@/pages/classics/common/components/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/components/classics-content-tag-panel";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
import type { ClassicsBatchOperationRecord } from "@/pages/classics/common/classics-content-types";
import type { ClassicsExportScopePayload } from "@/pages/classics/common/classics-export-types";
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
const TASK_POLL_INTERVAL_MS = 3000;
const EXPORT_PAGE_SIZE = 8;
const DEFAULT_REFINEMENT_MODEL_ID = 1;
const DEFAULT_REFINEMENT_MODEL_NAME = "gpt-5.5";
const DEFAULT_REFINEMENT_SERVICE_ROLE = "PRIMARY";

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readVisibilityValue = (visibility: WangqiVisibilityFilter) => {
    return visibility === "ALL" ? undefined : visibility;
};

const createEventId = (prefix: string) => {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const buildPromptMessagesJson = (document: WangqiDocumentRecord) => {
    return JSON.stringify([
        {
            role: "system",
            content: "你是古籍整理助理。请基于输入文稿生成简洁、准确、可直接回填到后台的中文摘要。"
        },
        {
            role: "user",
            content: [
                `标题：${document.title || "未命名文档"}`,
                `现有摘要：${document.summary || "暂无"}`,
                `正文：${document.content || "暂无正文"}`
            ].join("\n")
        }
    ]);
};

const buildInputPayloadJson = (document: WangqiDocumentRecord) => {
    return JSON.stringify({
        title: document.title || null,
        summary: document.summary || null,
        content: document.content || null,
        contentFormat: document.contentFormat || null
    });
};

const readDocumentTitle = (document: WangqiDocumentRecord) => {
    return document.title?.trim() || `王圻文档 ${document.id}`;
};

const buildExportScopeJson = (document: WangqiDocumentRecord) => {
    const title = readDocumentTitle(document);
    const scopePayload: ClassicsExportScopePayload = {
        title: `${title} 导出`,
        contentType: "WANGQI_DOCUMENT",
        scopeType: "SELECTED_ITEMS",
        items: [
            {
                id: document.id,
                title,
                text: document.content || "",
                summary: document.summary || null,
                visibility: document.visibility || null,
                documentTime: document.documentTime || null,
                sourceFileStorageObjectId: document.storageObjectId || null
            }
        ]
    };

    return JSON.stringify(scopePayload);
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
    const [selectedDocumentIds, setSelectedDocumentIds] = useState<number[]>([]);
    const [batchShareResult, setBatchShareResult] = useState<ClassicsBatchOperationRecord | null>(
        null
    );
    const [batchVisibilityResult, setBatchVisibilityResult] =
        useState<ClassicsBatchOperationRecord | null>(null);
    const [creatingRefinementCapability, setCreatingRefinementCapability] = useState<
        "summary" | null
    >(null);
    const handledSucceededTaskIdsRef = useRef<Set<number>>(new Set());

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
    const currentUserQuery = useQuery({
        queryKey: ["sys", "current-user", "info"],
        queryFn: currentUserService.getCurrentUserInfo,
        retry: false
    });
    const exportJobsQuery = useQuery({
        queryKey: ["classics", "wangqi", "exports", "jobs"],
        queryFn: () =>
            exportService.page({
                pageNo: 1,
                pageSize: EXPORT_PAGE_SIZE,
                contentType: "WANGQI_DOCUMENT",
                exportKind: "CONTENT_DATASET"
            }),
        retry: false
    });
    const currentUserId = Number(currentUserQuery.data?.id ?? 0);
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
    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "wangqi", "refinement", "tasks", activeDocument?.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                pageNo: 1,
                pageSize: 10,
                contentType: "WANGQI_DOCUMENT",
                contentId: activeDocument?.id,
                capability: "summary"
            }),
        enabled: editorOpen && editorMode === "edit" && Boolean(activeDocument?.id),
        retry: false,
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                ? TASK_POLL_INTERVAL_MS
                : false;
        }
    });

    const pageResult = pageQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const versions = useMemo(() => {
        return Array.isArray(versionsQuery.data) ? versionsQuery.data : [];
    }, [versionsQuery.data]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => version.id === selectedVersionId) ||
        null;
    const refinementTasks = useMemo(
        () => refinementTasksQuery.data?.items || [],
        [refinementTasksQuery.data?.items]
    );
    const exportJobs = exportJobsQuery.data?.records || [];
    const selectedDocuments = useMemo(
        () => records.filter((record) => selectedDocumentIds.includes(record.id)),
        [records, selectedDocumentIds]
    );

    const invalidateWangqi = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["wangqi", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "detail"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "source-file"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "versions"] }),
            queryClient.invalidateQueries({ queryKey: ["wangqi", "version"] }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "WANGQI_DOCUMENT"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "WANGQI_DOCUMENT"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", activeDocument?.id]
            })
        ]);
    }, [activeDocument?.id, queryClient]);

    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "wangqi", "refinement", "tasks", activeDocument?.id]
        });
    };
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "wangqi", "exports", "jobs"]
        });
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
    const shareDocumentMutation = useMutation({
        mutationFn: shareService.create,
        onSuccess: (share) => {
            if (typeof navigator.clipboard?.writeText === "function") {
                void navigator.clipboard.writeText(share.shareUrl);
                messageApi.success("分享链接已复制");
                return;
            }
            messageApi.success(`分享链接：${share.shareUrl}`);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "分享创建失败");
        }
    });
    const batchShareMutation = useMutation({
        mutationFn: shareService.createBatch,
        onSuccess: (result) => {
            setBatchShareResult(result);
            messageApi.success(
                `批量分享完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量分享创建失败");
        }
    });
    const batchVisibilityMutation = useMutation({
        mutationFn: contentService.changeVisibilityBatch,
        onSuccess: async (result) => {
            setBatchVisibilityResult(result);
            await invalidateWangqi();
            messageApi.success(
                `批量可见性完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量可见性修改失败");
        }
    });
    const exportMutation = useMutation({
        mutationFn: (document: WangqiDocumentRecord) =>
            exportService.create({
                contentType: "WANGQI_DOCUMENT",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: buildExportScopeJson(document)
            }),
        onSuccess: async () => {
            await invalidateExportJobs();
            messageApi.success("导出任务已提交，请到下方任务列表查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出提交失败");
        }
    });
    const createRefinementTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onSuccess: async () => {
            await invalidateRefinementTasks();
            messageApi.success("王圻精修任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "精修任务创建失败");
        },
        onSettled: () => {
            setCreatingRefinementCapability(null);
        }
    });

    useEffect(() => {
        const completedTaskIds = refinementTasks
            .filter(
                (task) =>
                    (task.status === "SUCCEEDED" || task.status === "PARTIAL") &&
                    typeof task.taskId === "number" &&
                    !handledSucceededTaskIdsRef.current.has(task.taskId)
            )
            .map((task) => task.taskId);
        if (!completedTaskIds.length) {
            return;
        }
        completedTaskIds.forEach((taskId) => handledSucceededTaskIdsRef.current.add(taskId));
        void invalidateWangqi();
    }, [invalidateWangqi, refinementTasks]);

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
        handledSucceededTaskIdsRef.current.clear();
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

    const shareDocument = (document: WangqiDocumentRecord) => {
        const title = readDocumentTitle(document);
        shareDocumentMutation.mutate({
            targets: [
                {
                    contentId: document.id,
                    contentType: "WANGQI_DOCUMENT"
                }
            ],
            title: `${title} 分享`,
            visibility: "PUBLIC"
        });
    };

    const shareSelectedDocuments = () => {
        if (!selectedDocuments.length) {
            messageApi.warning("请先选择要批量分享的王圻文档");
            return;
        }
        batchShareMutation.mutate({
            privateContentConfirmed: false,
            status: "ACTIVE",
            targets: selectedDocuments.map((document) => ({
                contentId: document.id,
                contentType: "WANGQI_DOCUMENT"
            })),
            titlePrefix: "王圻批量分享 - ",
            visibility: "PUBLIC"
        });
    };

    const changeSelectedVisibility = (visibility: "PRIVATE" | "PUBLIC") => {
        if (!selectedDocuments.length) {
            messageApi.warning("请先选择要批量修改可见性的王圻文档");
            return;
        }
        batchVisibilityMutation.mutate({
            contentIds: selectedDocuments.map((document) => document.id),
            contentType: "WANGQI_DOCUMENT",
            visibility
        });
    };

    const exportDocument = (document: WangqiDocumentRecord) => {
        exportMutation.mutate(document);
    };

    const createRefinementTask = (document: WangqiDocumentRecord) => {
        const requestedBy = currentUserQuery.data?.id;
        if (!requestedBy) {
            messageApi.warning("当前用户信息未加载完成，请稍后重试");
            return;
        }
        if (!document.content?.trim()) {
            messageApi.warning("正文为空，无法创建摘要精修任务");
            return;
        }
        setCreatingRefinementCapability("summary");
        createRefinementTaskMutation.mutate({
            capability: "summary",
            scope: "classics",
            contentType: "WANGQI_DOCUMENT",
            contentId: document.id,
            requestedBy: currentUserId,
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId("wangqi-summary-request"),
            traceId: createEventId("wangqi-summary-trace"),
            promptMessagesJson: buildPromptMessagesJson(document),
            promptVariablesJson: JSON.stringify({ title: document.title || null }),
            inputPayloadJson: buildInputPayloadJson(document),
            locale: "zh-CN"
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
                    <>
                        <ClassicsExportJobSection
                            items={exportJobs}
                            loading={exportJobsQuery.isLoading || exportMutation.isPending}
                            onDownload={(job) => {
                                if (job.downloadUrl) {
                                    window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                                }
                            }}
                            onRefresh={() => {
                                void invalidateExportJobs();
                            }}
                        />
                        <div style={{ marginBottom: 12 }}>
                            <Button
                                disabled={!selectedDocuments.length}
                                loading={batchShareMutation.isPending}
                                onClick={shareSelectedDocuments}
                            >
                                批量分享
                            </Button>
                            <Button
                                disabled={!selectedDocuments.length}
                                loading={batchVisibilityMutation.isPending}
                                style={{ marginLeft: 8 }}
                                onClick={() => changeSelectedVisibility("PUBLIC")}
                            >
                                批量公开
                            </Button>
                            <Button
                                disabled={!selectedDocuments.length}
                                loading={batchVisibilityMutation.isPending}
                                style={{ marginLeft: 8 }}
                                onClick={() => changeSelectedVisibility("PRIVATE")}
                            >
                                批量私有
                            </Button>
                        </div>
                        {batchShareResult ? (
                            <Alert
                                showIcon
                                type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                                style={{ marginBottom: 12 }}
                                message={`批量分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
                                description={
                                    batchShareResult.failures.length
                                        ? batchShareResult.failures
                                              .map(
                                                  (item) =>
                                                      `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
                                              )
                                              .join("；")
                                        : "全部选中王圻文档已创建分享记录。"
                                }
                            />
                        ) : null}
                        {batchVisibilityResult ? (
                            <Alert
                                showIcon
                                type={
                                    batchVisibilityResult.failureCount > 0 ? "warning" : "success"
                                }
                                style={{ marginBottom: 12 }}
                                message={`批量可见性结果：成功 ${batchVisibilityResult.successCount}，失败 ${batchVisibilityResult.failureCount}`}
                                description={
                                    batchVisibilityResult.failures.length
                                        ? batchVisibilityResult.failures
                                              .map(
                                                  (item) =>
                                                      `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
                                              )
                                              .join("；")
                                        : "全部选中王圻文档已更新可见性。"
                                }
                            />
                        ) : null}
                        <WangqiDocumentList
                            loading={pageQuery.isLoading}
                            dataSource={records}
                            onDelete={deleteDocument}
                            onExport={exportDocument}
                            onOpenEdit={openEditEditor}
                            onShare={shareDocument}
                            onSelectedDocumentIdsChange={setSelectedDocumentIds}
                            onSortDirectionChange={sortWangqiDocuments}
                            pagination={{
                                current: currentPageNo,
                                pageSize: currentPageSize,
                                total: totalCount,
                                onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                            }}
                            sortDirection={
                                query.sortDirection || DEFAULT_WANGQI_FILTERS.sortDirection
                            }
                            selectedDocumentIds={selectedDocumentIds}
                        />
                    </>
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
                            <Card
                                size="small"
                                title="AI 精修任务"
                                extra={
                                    <Button
                                        type="primary"
                                        onClick={() => createRefinementTask(activeDocument)}
                                        loading={creatingRefinementCapability === "summary"}
                                    >
                                        创建摘要任务
                                    </Button>
                                }
                            >
                                {refinementTasks.length ? (
                                    refinementTasks.slice(0, 4).map((task) => (
                                        <div key={task.taskId}>
                                            {task.capability}：{task.status}
                                            {task.resultPreview ? ` · ${task.resultPreview}` : ""}
                                        </div>
                                    ))
                                ) : (
                                    <div>暂无精修任务</div>
                                )}
                            </Card>
                            <AiCandidatePanel
                                capabilities={["summary", "tags", "qa"]}
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onApplied={async () => {
                                    await invalidateWangqi();
                                }}
                            />
                            <ClassicsContentTagPanel
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onChanged={invalidateWangqi}
                            />
                            <ClassicsContentQaPanel
                                contentId={activeDocument.id}
                                contentType="WANGQI_DOCUMENT"
                                onChanged={invalidateWangqi}
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
