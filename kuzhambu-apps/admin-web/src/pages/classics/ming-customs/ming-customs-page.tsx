import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Select } from "antd";
import { useMemo, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import * as shareService from "@/pages/classics/common/classics-share-service";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { MingCustomsKeywordCloud } from "./components/ming-customs-keyword-cloud";
import { MingCustomsList } from "./components/ming-customs-list";
import { MingCustomsModel } from "./components/ming-customs-model";
import * as service from "./ming-customs-service";
import type { MingCustomsCommand, MingCustomsQuery } from "./ming-customs-service";
import type { MingCustomsRecord } from "./ming-customs-types";
import "./ming-customs-page.css";

type MingCustomsVisibilityFilter = "ALL" | "PUBLIC" | "PRIVATE";
type MingCustomsSortDirectionFilter = "ASC" | "DESC";

interface MingCustomsFilters {
    category: string;
    sortDirection: MingCustomsSortDirectionFilter;
    visibility: MingCustomsVisibilityFilter;
}

const DEFAULT_MING_CUSTOMS_FILTERS: MingCustomsFilters = {
    category: "",
    sortDirection: "DESC",
    visibility: "ALL"
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readVisibilityValue = (visibility: MingCustomsVisibilityFilter) => {
    return visibility === "ALL" ? undefined : visibility;
};

export const MingCustomsPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<MingCustomsQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<MingCustomsFilters>(DEFAULT_MING_CUSTOMS_FILTERS);
    const [editorMode, setEditorMode] = useState<"create" | "edit">("create");
    const [editorOpen, setEditorOpen] = useState(false);
    const [editingEntry, setEditingEntry] = useState<MingCustomsRecord | null>(null);
    const hasActiveFilters = Boolean(
        filters.category ||
        filters.visibility !== "ALL" ||
        filters.sortDirection !== DEFAULT_MING_CUSTOMS_FILTERS.sortDirection
    );

    const mingCustomsQuery = useQuery({
        queryKey: ["ming-customs", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const categoryOptionsQuery = useQuery({
        queryKey: ["ming-customs", "category-options"],
        queryFn: service.listCategoryOptions,
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["ming-customs", "detail", editingEntry?.id],
        queryFn: () => service.get(editingEntry?.id ?? 0),
        enabled: editorOpen && editorMode === "edit" && Boolean(editingEntry?.id),
        retry: false
    });
    const pageResult = mingCustomsQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const categoryOptions = useMemo(
        () => categoryOptionsQuery.data || [],
        [categoryOptionsQuery.data]
    );
    const categoryLabels = useMemo(() => {
        return Object.fromEntries(categoryOptions.map((option) => [option.value, option.label]));
    }, [categoryOptions]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const editorEntry = detailQuery.data || editingEntry;

    const invalidateMingCustoms = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "keyword-cloud"] }),
            queryClient.invalidateQueries({ queryKey: ["ming-customs", "detail"] })
        ]);
    };

    const saveMutation = useMutation({
        mutationFn: (command: MingCustomsCommand) =>
            editorMode === "create" ? service.add(command) : service.update(command),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingEntry(null);
            await invalidateMingCustoms();
            messageApi.success(editorMode === "create" ? "明代习俗已新增" : "明代习俗已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: service.deleteById,
        onSuccess: async () => {
            await invalidateMingCustoms();
            messageApi.success("明代习俗已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const shareMutation = useMutation({
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

    const searchMingCustoms = (value: string) => {
        setSearchText(value);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword: normalizeSearch(value),
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const applyFilters = () => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: filters.category || undefined,
            visibility: readVisibilityValue(filters.visibility),
            sortDirection: filters.sortDirection,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const resetFilters = () => {
        setFilters(DEFAULT_MING_CUSTOMS_FILTERS);
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: undefined,
            visibility: undefined,
            sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const selectKeyword = (keyword: string) => {
        setSearchText(keyword);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const openCreateEditor = () => {
        setEditorMode("create");
        setEditingEntry(null);
        setEditorOpen(true);
    };

    const openEditEditor = (entry: MingCustomsRecord) => {
        setEditorMode("edit");
        setEditingEntry(entry);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingEntry(null);
    };

    const deleteEntry = (entry: MingCustomsRecord) => {
        const title = entry.title?.trim() || `条目 ${entry.id}`;
        confirm.danger({
            title: "删除明代习俗",
            message: `确认删除 ${title}？`,
            description: "删除后该条目将不再出现在明代习俗列表。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(entry.id)
        });
    };

    const shareEntry = (entry: MingCustomsRecord) => {
        const title = entry.title?.trim() || `条目 ${entry.id}`;
        shareMutation.mutate({
            targets: [
                {
                    contentId: entry.id,
                    contentType: "MING_CUSTOMS"
                }
            ],
            title: `${title} 分享`,
            visibility: "PUBLIC"
        });
    };

    return (
        <>
            <KuzhambuListPage<MingCustomsRecord>
                pageClassName="ming-customs-page"
                title="明代习俗"
                description="明代习俗专题条目治理入口。"
                subjectName="明代习俗"
                enableSearch
                enableFilter
                enableAdd
                addText="新增明代习俗"
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "category",
                        label: "分类",
                        render: () => (
                            <Select
                                allowClear
                                aria-label="明代习俗分类"
                                placeholder="全部分类"
                                value={filters.category || undefined}
                                options={categoryOptions.map((option) => ({
                                    value: option.value,
                                    label: option.label
                                }))}
                                onChange={(value) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        category: value || ""
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "visibility",
                        label: "可见性",
                        render: () => (
                            <Select
                                aria-label="明代习俗可见性"
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
                                aria-label="明代习俗排序方向"
                                value={filters.sortDirection}
                                options={[
                                    { value: "DESC", label: "最新优先" },
                                    { value: "ASC", label: "最早优先" }
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
                    <MingCustomsKeywordCloud
                        visibility={query.visibility}
                        onSelect={selectKeyword}
                    />
                }
                searchValue={searchText}
                onSearchChange={searchMingCustoms}
                content={
                    <MingCustomsList
                        categoryLabels={categoryLabels}
                        loading={mingCustomsQuery.isLoading}
                        dataSource={records}
                        onDelete={deleteEntry}
                        onOpenEdit={openEditEditor}
                        onShare={shareEntry}
                        pagination={{
                            current: currentPageNo,
                            pageSize: currentPageSize,
                            total: totalCount,
                            onChange: (pageNo, pageSize) =>
                                setQuery((currentQuery) => ({
                                    ...currentQuery,
                                    pageNo,
                                    pageSize
                                }))
                        }}
                    />
                }
            />
            <MingCustomsModel
                categoryOptions={categoryOptions}
                entry={editorEntry}
                loading={detailQuery.isLoading}
                mode={editorMode}
                open={editorOpen}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={(command) => saveMutation.mutate(command)}
                afterForm={
                    editorMode === "edit" && editorEntry ? (
                        <AiCandidatePanel
                            capabilities={["summary", "tags", "qa"]}
                            contentId={editorEntry.id}
                            contentType="MING_CUSTOMS"
                            onApplied={async () => {
                                await invalidateMingCustoms();
                            }}
                        />
                    ) : null
                }
            />
        </>
    );
};
