import {
    ArrowDownOutlined,
    ArrowUpOutlined,
    DeleteOutlined,
    EditOutlined,
    MenuOutlined,
    PlusOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Empty, Input, Modal, Select, Skeleton, Typography } from "antd";
import type { DragEvent } from "react";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./sancai-service";
import type { SancaiEntryPageQuery } from "./sancai-service";
import type { SancaiCategoryRecord, SancaiEntryRecord, SancaiVolumeRecord } from "./sancai-types";
import "./sancai-page.css";

const { Text, Title } = Typography;

const entryStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "草稿", value: "DRAFT" },
    { label: "已发布", value: "PUBLISHED" },
    { label: "已归档", value: "ARCHIVED" }
];

const visibilityOptions = [
    { label: "公开", value: "PUBLIC" },
    { label: "私有", value: "PRIVATE" }
];

const categoryTypeOptions = [
    { label: "正式门类", value: "FORMAL" },
    { label: "辅助内容", value: "AUXILIARY" }
];

interface SancaiCategoryFormValues {
    categoryType: string;
    title: string;
}

interface SancaiEntryFormValues {
    originalText: string;
    summary: string;
    title: string;
    translationText: string;
    visibility: string;
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无摘要";
};

const isQueryLoading = (...queries: Array<{ isLoading: boolean }>) => {
    return queries.some((query) => query.isLoading);
};

const isQueryError = (...queries: Array<{ isError: boolean }>) => {
    return queries.some((query) => query.isError);
};

const reloadQueries = (...queries: Array<{ refetch: () => Promise<unknown> }>) => {
    void Promise.all(queries.map((query) => query.refetch()));
};

const normalizeKeyword = (value: string) => {
    const keyword = value.trim();
    return keyword || undefined;
};

const readCategoryTypeLabel = (category: SancaiCategoryRecord) => {
    return category.categoryType === "AUXILIARY" ? "辅助内容" : "正式门类";
};

const readVolumeTypeLabel = (volume: SancaiVolumeRecord) => {
    return volume.volumeType === "AUXILIARY" ? "辅助卷目" : "正式卷目";
};

const renderCategoryList = (
    categories: SancaiCategoryRecord[],
    selectedCategoryId: number | null,
    onSelect: (category: SancaiCategoryRecord) => void,
    onEdit: (category: SancaiCategoryRecord) => void,
    onDelete: (category: SancaiCategoryRecord) => void
) => {
    if (!categories.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无门类" />;
    }

    return (
        <div className="sancai-category-list" aria-label="三才图会门类">
            {categories.map((category) => (
                <div
                    className={
                        category.id === selectedCategoryId
                            ? "sancai-catalog-row sancai-catalog-row-active"
                            : "sancai-catalog-row"
                    }
                    key={category.id}
                >
                    <button
                        className="sancai-catalog-item"
                        type="button"
                        aria-label={`选择门类 ${readTitle(category, "门类")}`}
                        aria-pressed={category.id === selectedCategoryId}
                        onClick={() => onSelect(category)}
                    >
                        <span className="sancai-category-main">
                            <span
                                className={
                                    category.categoryType === "AUXILIARY"
                                        ? "sancai-category-type-dot sancai-category-type-dot-auxiliary"
                                        : "sancai-category-type-dot sancai-category-type-dot-formal"
                                }
                                aria-label={`门类类型 ${readCategoryTypeLabel(category)}`}
                            />
                            <span>{readTitle(category, "门类")}</span>
                        </span>
                    </button>
                    <div
                        className="sancai-catalog-actions"
                        aria-label={`${readTitle(category, "门类")} 操作`}
                    >
                        <Button
                            aria-label={`编辑门类 ${readTitle(category, "门类")}`}
                            icon={<EditOutlined />}
                            size="small"
                            type="text"
                            onClick={() => onEdit(category)}
                        />
                        <Button
                            aria-label={`删除门类 ${readTitle(category, "门类")}`}
                            danger
                            icon={<DeleteOutlined />}
                            size="small"
                            type="text"
                            onClick={() => onDelete(category)}
                        />
                    </div>
                </div>
            ))}
        </div>
    );
};

const renderVolumeList = (
    volumes: SancaiVolumeRecord[],
    selectedVolumeId: number | null,
    onSelect: (volume: SancaiVolumeRecord) => void
) => {
    if (!volumes.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无卷目" />;
    }

    return (
        <div className="sancai-volume-list" aria-label="三才图会卷目">
            {volumes.slice(0, 8).map((volume) => (
                <button
                    className={
                        volume.id === selectedVolumeId
                            ? "sancai-volume-item sancai-volume-item-active"
                            : "sancai-volume-item"
                    }
                    type="button"
                    key={volume.id}
                    aria-label={`选择卷目 ${readTitle(volume, "卷")}`}
                    aria-pressed={volume.id === selectedVolumeId}
                    onClick={() => onSelect(volume)}
                >
                    <span className="sancai-volume-main">
                        <span
                            className={
                                volume.volumeType === "AUXILIARY"
                                    ? "sancai-category-type-dot sancai-category-type-dot-auxiliary"
                                    : "sancai-category-type-dot sancai-category-type-dot-formal"
                            }
                            aria-label={`卷目类型 ${readVolumeTypeLabel(volume)}`}
                        />
                        <span>{readTitle(volume, "卷")}</span>
                    </span>
                </button>
            ))}
        </div>
    );
};

const readVolumeTitle = (entry: SancaiEntryRecord, volumes: SancaiVolumeRecord[]) => {
    const volume = volumes.find((item) => item.id === entry.volumeId);
    return volume ? readTitle(volume, "卷") : `卷 ${entry.volumeId || "-"}`;
};

const renderEntryTable = (
    entries: SancaiEntryRecord[],
    volumes: SancaiVolumeRecord[],
    currentPageNo: number,
    currentPageSize: number,
    totalCount: number,
    onPageChange: (pageNo: number, pageSize: number) => void,
    onView: (entry: SancaiEntryRecord) => void
) => {
    if (!entries.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无符合筛选条件的条目" />;
    }

    const columns: KuzhambuTableProps<SancaiEntryRecord>["columns"] = [
        {
            title: "条目",
            key: "title",
            width: 220,
            render: (_, entry) => (
                <div className="sancai-entry-title-cell">
                    <Text strong>{readTitle(entry, "条目")}</Text>
                    <Text type="secondary">ID {entry.id}</Text>
                </div>
            )
        },
        {
            title: "卷",
            key: "volume",
            width: 180,
            render: (_, entry) => <Text>{readVolumeTitle(entry, volumes)}</Text>
        },
        {
            title: "状态",
            dataIndex: "lifecycleStatus",
            key: "status",
            width: 120,
            render: (status?: string | null) => (
                <span className="sancai-entry-status">{status || "UNKNOWN"}</span>
            )
        },
        {
            title: "摘要",
            key: "summary",
            render: (_, entry) => <Text type="secondary">{readEntrySummary(entry)}</Text>
        },
        {
            key: "actions",
            options: (entry) => [
                {
                    key: "view",
                    text: "查看",
                    ariaLabel: `查看 ${readTitle(entry, "条目")}`,
                    onClick: () => onView(entry)
                }
            ]
        }
    ];

    return (
        <div className="sancai-entry-table-wrap">
            <KuzhambuTable
                className="sancai-entry-table"
                aria-label="三才图会条目表格"
                columns={columns}
                dataSource={entries}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    onChange: onPageChange
                }}
                rowKey="id"
                size="middle"
                scroll={{ x: 760 }}
            />
        </div>
    );
};

const toFormValues = (entry?: SancaiEntryRecord): SancaiEntryFormValues => {
    return {
        originalText: entry?.originalText || "",
        summary: entry?.summary || "",
        title: entry?.title || "",
        translationText: entry?.translationText || "",
        visibility: entry?.visibility || "PUBLIC"
    };
};

const toCategoryFormValues = (category?: SancaiCategoryRecord): SancaiCategoryFormValues => {
    return {
        categoryType: category?.categoryType || "FORMAL",
        title: category?.title || ""
    };
};

const renderCategoryEditor = (
    category: SancaiCategoryRecord | null,
    form: SancaiCategoryFormValues,
    isSaving: boolean,
    onChange: (values: Partial<SancaiCategoryFormValues>) => void,
    onSave: () => void,
    onCancel: () => void
) => {
    const title = category ? "编辑门类" : "新增门类";
    return (
        <div className="sancai-category-editor" aria-label={title}>
            <Text strong>{title}</Text>
            <Input
                aria-label="三才图会门类标题"
                placeholder="门类标题"
                value={form.title}
                onChange={(event) => onChange({ title: event.target.value })}
            />
            <Select
                aria-label="三才图会门类类型"
                value={form.categoryType}
                options={categoryTypeOptions}
                onChange={(categoryType) => onChange({ categoryType })}
            />
            <div className="sancai-category-editor-actions">
                <Button aria-label="取消编辑三才图会门类" onClick={onCancel}>
                    取消
                </Button>
                <Button
                    aria-label={
                        category ? `保存门类 ${readTitle(category, "门类")}` : "保存新增门类"
                    }
                    loading={isSaving}
                    type="primary"
                    onClick={onSave}
                >
                    保存
                </Button>
            </div>
        </div>
    );
};

const renderCategorySortEditor = (
    categories: SancaiCategoryRecord[],
    isSaving: boolean,
    draggedCategoryId: number | null,
    onMove: (categoryId: number, direction: -1 | 1) => void,
    onDragStart: (categoryId: number) => void,
    onDragOver: (event: DragEvent<HTMLDivElement>) => void,
    onDrop: (targetCategoryId: number) => void
) => {
    if (!categories.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无可排序门类" />;
    }

    return (
        <div className="sancai-category-sort-list" aria-label="三才图会门类排序列表">
            {categories.map((category, index) => {
                const title = readTitle(category, "门类");
                return (
                    <div
                        className={
                            draggedCategoryId === category.id
                                ? "sancai-category-sort-item sancai-category-sort-item-dragging"
                                : "sancai-category-sort-item"
                        }
                        draggable
                        key={category.id}
                        role="listitem"
                        aria-label={`门类排序项 ${title}`}
                        onDragStart={() => onDragStart(category.id)}
                        onDragOver={onDragOver}
                        onDrop={() => onDrop(category.id)}
                    >
                        <span
                            className="sancai-category-sort-handle"
                            aria-label={`拖动门类 ${title}`}
                        >
                            <MenuOutlined />
                        </span>
                        <span className="sancai-category-sort-title">
                            <span
                                className={
                                    category.categoryType === "AUXILIARY"
                                        ? "sancai-category-type-dot sancai-category-type-dot-auxiliary"
                                        : "sancai-category-type-dot sancai-category-type-dot-formal"
                                }
                                aria-label={`门类类型 ${readCategoryTypeLabel(category)}`}
                            />
                            <span>{title}</span>
                        </span>
                        <div className="sancai-category-sort-actions">
                            <Button
                                aria-label={`上移门类 ${title}`}
                                disabled={isSaving || index === 0}
                                icon={<ArrowUpOutlined />}
                                size="small"
                                type="text"
                                onClick={() => onMove(category.id, -1)}
                            />
                            <Button
                                aria-label={`下移门类 ${title}`}
                                disabled={isSaving || index === categories.length - 1}
                                icon={<ArrowDownOutlined />}
                                size="small"
                                type="text"
                                onClick={() => onMove(category.id, 1)}
                            />
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

const renderDetail = (
    entry: SancaiEntryRecord | undefined,
    form: SancaiEntryFormValues,
    isSaving: boolean,
    onChange: (values: Partial<SancaiEntryFormValues>) => void,
    onSave: () => void
) => {
    if (!entry) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="选择条目后查看详情" />;
    }

    return (
        <div className="sancai-detail-card">
            <Text type="secondary">当前预览</Text>
            <Input
                aria-label="三才图会条目标题"
                value={form.title}
                onChange={(event) => onChange({ title: event.target.value })}
            />
            <Input.TextArea
                aria-label="三才图会原文"
                value={form.originalText}
                autoSize={{ minRows: 4, maxRows: 8 }}
                onChange={(event) => onChange({ originalText: event.target.value })}
            />
            <Input.TextArea
                aria-label="三才图会译文"
                value={form.translationText}
                autoSize={{ minRows: 4, maxRows: 8 }}
                onChange={(event) => onChange({ translationText: event.target.value })}
            />
            <Input.TextArea
                aria-label="三才图会摘要"
                value={form.summary}
                autoSize={{ minRows: 3, maxRows: 6 }}
                onChange={(event) => onChange({ summary: event.target.value })}
            />
            <Select
                aria-label="三才图会公开状态"
                value={form.visibility}
                options={visibilityOptions}
                onChange={(value) => onChange({ visibility: value })}
            />
            <Button
                aria-label="保存三才图会条目"
                type="primary"
                loading={isSaving}
                onClick={onSave}
            >
                保存
            </Button>
        </div>
    );
};

export const SancaiPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<SancaiEntryPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [queryVersion, setQueryVersion] = useState(0);
    const [keyword, setKeyword] = useState("");
    const [lifecycleStatus, setLifecycleStatus] = useState("ALL");
    const [editingCategory, setEditingCategory] = useState<SancaiCategoryRecord | null>(null);
    const [isCategoryEditorOpen, setIsCategoryEditorOpen] = useState(false);
    const [isCategorySortOpen, setIsCategorySortOpen] = useState(false);
    const [categoryForm, setCategoryForm] = useState<SancaiCategoryFormValues>(() =>
        toCategoryFormValues()
    );
    const [sortedCategories, setSortedCategories] = useState<SancaiCategoryRecord[]>([]);
    const [draggedCategoryId, setDraggedCategoryId] = useState<number | null>(null);
    const [activeEntryId, setActiveEntryId] = useState<number | null>(null);
    const [entryForm, setEntryForm] = useState<SancaiEntryFormValues>(toFormValues());
    const categoriesQuery = useQuery({
        queryKey: ["classics", "sancai", "categories"],
        queryFn: service.listCategories,
        retry: false
    });
    const volumesQuery = useQuery({
        queryKey: ["classics", "sancai", "volumes", query.categoryId ?? null],
        queryFn: () => service.listVolumes({ categoryId: query.categoryId }),
        retry: false
    });
    const entriesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "page", query, queryVersion],
        queryFn: () => service.pageEntries(query),
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["classics", "sancai", "entry", activeEntryId],
        queryFn: () => service.getEntry(activeEntryId || 0),
        enabled: activeEntryId !== null,
        retry: false
    });
    const saveMutation = useMutation({
        mutationFn: service.saveEntry,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            if (activeEntryId !== null) {
                await queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "entry", activeEntryId]
                });
            }
            messageApi.success("三才图会条目已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const categoryMutation = useMutation({
        mutationFn: service.saveCategory,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
            setIsCategoryEditorOpen(false);
            setEditingCategory(null);
            setCategoryForm(toCategoryFormValues());
            messageApi.success("三才图会门类已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类保存失败");
        }
    });
    const deleteCategoryMutation = useMutation({
        mutationFn: service.removeCategory,
        onSuccess: async (_, variables) => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
            if (variables.id === query.categoryId) {
                setQueryVersion((version) => version + 1);
                setQuery({
                    pageNo: DEFAULT_PAGE_NO,
                    pageSize: query.pageSize || DEFAULT_PAGE_SIZE
                });
            }
            messageApi.success("三才图会门类已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类删除失败");
        }
    });
    const sortCategoryMutation = useMutation({
        mutationFn: service.sortCategories,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
            setIsCategorySortOpen(false);
            setDraggedCategoryId(null);
            messageApi.success("三才图会门类顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类排序保存失败");
        }
    });
    const categories = categoriesQuery.data || [];
    const volumes = volumesQuery.data || [];
    const entries = entriesQuery.data?.records || [];
    const totalCount = entriesQuery.data?.totalCount ?? entriesQuery.data?.count ?? 0;
    const currentPageNo = entriesQuery.data?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = entriesQuery.data?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const selectedEntry = detailQuery.data;
    const isLoading = isQueryLoading(categoriesQuery, volumesQuery, entriesQuery);
    const hasError = isQueryError(categoriesQuery, volumesQuery, entriesQuery);
    const selectedCategoryId = query.categoryId ?? null;
    const selectedVolumeId = query.volumeId ?? null;

    const updateQuery = (values: Partial<SancaiEntryPageQuery>) => {
        setQueryVersion((version) => version + 1);
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...values,
            pageNo: DEFAULT_PAGE_NO,
            pageSize: currentQuery.pageSize || DEFAULT_PAGE_SIZE
        }));
    };

    const changePage = (pageNo: number, pageSize: number) => {
        setQueryVersion((version) => version + 1);
        setQuery((currentQuery) => ({
            ...currentQuery,
            pageNo: pageSize === currentPageSize ? pageNo : DEFAULT_PAGE_NO,
            pageSize
        }));
    };

    const selectCategory = (category: SancaiCategoryRecord) => {
        updateQuery({
            categoryId: category.id,
            volumeId: undefined
        });
    };

    const startCreateCategory = () => {
        setEditingCategory(null);
        setCategoryForm(toCategoryFormValues());
        setIsCategoryEditorOpen(true);
    };

    const startEditCategory = (category: SancaiCategoryRecord) => {
        setEditingCategory(category);
        setCategoryForm(toCategoryFormValues(category));
        setIsCategoryEditorOpen(true);
    };

    const cancelCategoryEdit = () => {
        setIsCategoryEditorOpen(false);
        setEditingCategory(null);
        setCategoryForm(toCategoryFormValues());
    };

    const saveCategory = () => {
        categoryMutation.mutate({
            id: editingCategory?.id,
            title: categoryForm.title,
            categoryType: categoryForm.categoryType
        });
    };

    const openCategorySort = () => {
        setSortedCategories(categories);
        setDraggedCategoryId(null);
        setIsCategorySortOpen(true);
    };

    const closeCategorySort = () => {
        setIsCategorySortOpen(false);
        setDraggedCategoryId(null);
    };

    const moveCategoryInSortForm = (categoryId: number, direction: -1 | 1) => {
        setSortedCategories((currentCategories) => {
            const index = currentCategories.findIndex((category) => category.id === categoryId);
            const nextIndex = index + direction;
            if (index < 0 || nextIndex < 0 || nextIndex >= currentCategories.length) {
                return currentCategories;
            }
            const nextCategories = [...currentCategories];
            const [category] = nextCategories.splice(index, 1);
            nextCategories.splice(nextIndex, 0, category);
            return nextCategories;
        });
    };

    const dropCategoryInSortForm = (targetCategoryId: number) => {
        if (draggedCategoryId === null || draggedCategoryId === targetCategoryId) {
            return;
        }
        setSortedCategories((currentCategories) => {
            const draggedCategory = currentCategories.find(
                (category) => category.id === draggedCategoryId
            );
            const targetIndex = currentCategories.findIndex(
                (category) => category.id === targetCategoryId
            );
            if (!draggedCategory || targetIndex < 0) {
                return currentCategories;
            }
            const remainingCategories = currentCategories.filter(
                (category) => category.id !== draggedCategoryId
            );
            remainingCategories.splice(targetIndex, 0, draggedCategory);
            return remainingCategories;
        });
        setDraggedCategoryId(null);
    };

    const saveCategorySort = () => {
        sortCategoryMutation.mutate({
            orderedIds: sortedCategories.map((category) => category.id),
            sortDirection: "ASC"
        });
    };

    const confirmDeleteCategory = (category: SancaiCategoryRecord) => {
        confirm.danger({
            title: "删除三才图会门类",
            message: `确认删除 ${readTitle(category, "门类")}？`,
            description: "仅空门类可删除。若该门类下仍有关联卷，接口会拒绝删除。",
            okText: "删除",
            onConfirm: () => deleteCategoryMutation.mutateAsync({ id: category.id })
        });
    };

    const selectVolume = (volume: SancaiVolumeRecord) => {
        updateQuery({
            categoryId: volume.categoryId ?? query.categoryId,
            volumeId: volume.id
        });
    };

    const applyFilters = () => {
        updateQuery({
            keyword: normalizeKeyword(keyword),
            lifecycleStatus: lifecycleStatus === "ALL" ? undefined : lifecycleStatus
        });
    };

    const resetFilters = () => {
        setKeyword("");
        setLifecycleStatus("ALL");
        setQueryVersion((version) => version + 1);
        setQuery({
            pageNo: DEFAULT_PAGE_NO,
            pageSize: query.pageSize || DEFAULT_PAGE_SIZE
        });
    };

    const openEntry = (entry: SancaiEntryRecord) => {
        setActiveEntryId(entry.id);
        setEntryForm(toFormValues(entry));
    };

    const saveEntry = () => {
        const entry = detailQuery.data || selectedEntry;
        if (!entry) {
            return;
        }
        saveMutation.mutate({
            id: entry.id,
            volumeId: entry.volumeId,
            title: entryForm.title,
            originalText: entryForm.originalText,
            translationText: entryForm.translationText,
            summary: entryForm.summary,
            lifecycleStatus: entry.lifecycleStatus,
            visibility: entryForm.visibility,
            translationStatus: entry.translationStatus,
            imageStatus: entry.imageStatus,
            visualAssetStatus: entry.visualAssetStatus,
            refinementStatus: entry.refinementStatus
        });
    };

    return (
        <KuzhambuPage
            className="sancai-page"
            eyebrow="Classics"
            title="三才图会"
            description="按门类、卷目和条目组织三才图会后台治理入口。"
            actions={
                <Button
                    aria-label="刷新三才图会数据"
                    icon={<ReloadOutlined />}
                    onClick={() => reloadQueries(categoriesQuery, volumesQuery, entriesQuery)}
                >
                    刷新
                </Button>
            }
        >
            {hasError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    message="三才图会数据加载失败"
                    description="请确认后台三才图会接口可用后刷新页面。"
                />
            ) : null}

            <div className="sancai-shell">
                <aside className="sancai-catalog-panel">
                    <div className="sancai-panel-heading">
                        <Title level={3}>目录</Title>
                        <div className="sancai-heading-actions">
                            <Button
                                aria-label="调整三才图会门类顺序"
                                title="调整门类顺序"
                                icon={<MenuOutlined />}
                                size="small"
                                onClick={openCategorySort}
                            />
                            <Button
                                aria-label="新增三才图会门类"
                                title="新增门类"
                                icon={<PlusOutlined />}
                                size="small"
                                onClick={startCreateCategory}
                            />
                        </div>
                    </div>
                    {isCategoryEditorOpen
                        ? renderCategoryEditor(
                              editingCategory,
                              categoryForm,
                              categoryMutation.isPending,
                              (values) =>
                                  setCategoryForm((currentForm) => ({
                                      ...currentForm,
                                      ...values
                                  })),
                              saveCategory,
                              cancelCategoryEdit
                          )
                        : null}
                    <div className="sancai-catalog-columns">
                        <section className="sancai-catalog-column">
                            <div className="sancai-panel-heading">
                                <Title level={3}>门类</Title>
                                <Text type="secondary">{categories.length} 项</Text>
                            </div>
                            {isLoading ? (
                                <Skeleton active paragraph={{ rows: 8 }} />
                            ) : (
                                renderCategoryList(
                                    categories,
                                    selectedCategoryId,
                                    selectCategory,
                                    startEditCategory,
                                    confirmDeleteCategory
                                )
                            )}
                        </section>

                        <section className="sancai-catalog-column">
                            <div className="sancai-panel-heading">
                                <Title level={3}>卷目</Title>
                                <Text type="secondary">{volumes.length} 卷</Text>
                            </div>
                            {isLoading ? (
                                <Skeleton active paragraph={{ rows: 5 }} />
                            ) : (
                                renderVolumeList(volumes, selectedVolumeId, selectVolume)
                            )}
                        </section>
                    </div>
                </aside>

                <section className="sancai-workspace">
                    <div className="sancai-toolbar">
                        <Input.Search
                            aria-label="三才图会关键词"
                            placeholder="搜索标题、原文或摘要"
                            value={keyword}
                            allowClear
                            enterButton="查询"
                            onChange={(event) => setKeyword(event.target.value)}
                            onSearch={applyFilters}
                        />
                        <Select
                            aria-label="三才图会条目状态"
                            value={lifecycleStatus}
                            options={entryStatusOptions}
                            onChange={setLifecycleStatus}
                        />
                        <Button aria-label="重置三才图会筛选" onClick={resetFilters}>
                            重置
                        </Button>
                    </div>

                    <div className="sancai-content-grid">
                        <section className="sancai-list-panel">
                            <div className="sancai-panel-heading">
                                <Title level={3}>条目</Title>
                                <Text type="secondary">{totalCount} 条</Text>
                            </div>
                            {isLoading ? (
                                <Skeleton active paragraph={{ rows: 7 }} />
                            ) : (
                                renderEntryTable(
                                    entries,
                                    volumes,
                                    currentPageNo,
                                    currentPageSize,
                                    totalCount,
                                    changePage,
                                    openEntry
                                )
                            )}
                        </section>

                        <aside className="sancai-detail-panel">
                            <div className="sancai-panel-heading">
                                <Title level={3}>详情</Title>
                            </div>
                            {isLoading || detailQuery.isLoading ? (
                                <Skeleton active paragraph={{ rows: 6 }} />
                            ) : (
                                renderDetail(
                                    selectedEntry,
                                    entryForm,
                                    saveMutation.isPending,
                                    (values) =>
                                        setEntryForm((currentForm) => ({
                                            ...currentForm,
                                            ...values
                                        })),
                                    saveEntry
                                )
                            )}
                        </aside>
                    </div>
                </section>
            </div>
            <Modal
                title="调整三才图会门类顺序"
                open={isCategorySortOpen}
                okText="保存"
                cancelText="取消"
                confirmLoading={sortCategoryMutation.isPending}
                onCancel={closeCategorySort}
                onOk={saveCategorySort}
                okButtonProps={{
                    "aria-label": "保存三才图会门类顺序"
                }}
                cancelButtonProps={{
                    "aria-label": "取消调整三才图会门类顺序"
                }}
            >
                {renderCategorySortEditor(
                    sortedCategories,
                    sortCategoryMutation.isPending,
                    draggedCategoryId,
                    moveCategoryInSortForm,
                    setDraggedCategoryId,
                    (event) => event.preventDefault(),
                    dropCategoryInSortForm
                )}
            </Modal>
        </KuzhambuPage>
    );
};
