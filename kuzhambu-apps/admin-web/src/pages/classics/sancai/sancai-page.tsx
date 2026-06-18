import { ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Empty, Input, Select, Skeleton, Typography } from "antd";
import { useState } from "react";
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
    { label: "内部", value: "INTERNAL" },
    { label: "隐藏", value: "HIDDEN" }
];

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

const renderCategoryList = (
    categories: SancaiCategoryRecord[],
    selectedCategoryId: number | null,
    onSelect: (category: SancaiCategoryRecord) => void
) => {
    if (!categories.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无门类" />;
    }

    return (
        <div className="sancai-category-list" aria-label="三才图会门类">
            {categories.map((category) => (
                <button
                    className={
                        category.id === selectedCategoryId
                            ? "sancai-catalog-item sancai-catalog-item-active"
                            : "sancai-catalog-item"
                    }
                    type="button"
                    key={category.id}
                    aria-label={`选择门类 ${readTitle(category, "门类")}`}
                    aria-pressed={category.id === selectedCategoryId}
                    onClick={() => onSelect(category)}
                >
                    <span>{readTitle(category, "门类")}</span>
                    <Text type="secondary">{category.categoryType || "未分类"}</Text>
                </button>
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
                    <span>{readTitle(volume, "卷")}</span>
                    <Text type="secondary">{volume.volumeType || "未分类"}</Text>
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
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<SancaiEntryPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [queryVersion, setQueryVersion] = useState(0);
    const [keyword, setKeyword] = useState("");
    const [lifecycleStatus, setLifecycleStatus] = useState("ALL");
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
                        <Text type="secondary">{categories.length} 门类</Text>
                    </div>
                    {isLoading ? (
                        <Skeleton active paragraph={{ rows: 8 }} />
                    ) : (
                        renderCategoryList(categories, selectedCategoryId, selectCategory)
                    )}

                    <div className="sancai-panel-heading sancai-panel-heading-secondary">
                        <Title level={3}>卷目</Title>
                        <Text type="secondary">{volumes.length} 卷</Text>
                    </div>
                    {isLoading ? (
                        <Skeleton active paragraph={{ rows: 5 }} />
                    ) : (
                        renderVolumeList(volumes, selectedVolumeId, selectVolume)
                    )}
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
        </KuzhambuPage>
    );
};
