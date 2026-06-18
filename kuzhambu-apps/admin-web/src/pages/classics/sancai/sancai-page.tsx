import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Alert, Button, Empty, Input, Select, Skeleton, Typography } from "antd";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./sancai-service";
import type { SancaiCategoryRecord, SancaiEntryRecord, SancaiVolumeRecord } from "./sancai-types";
import "./sancai-page.css";

const { Text, Title } = Typography;

const entryStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "草稿", value: "DRAFT" },
    { label: "已发布", value: "PUBLISHED" },
    { label: "已归档", value: "ARCHIVED" }
];

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

const renderCategoryList = (categories: SancaiCategoryRecord[]) => {
    if (!categories.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无门类" />;
    }

    return (
        <div className="sancai-category-list" aria-label="三才图会门类">
            {categories.map((category, index) => (
                <button
                    className={
                        index === 0
                            ? "sancai-catalog-item sancai-catalog-item-active"
                            : "sancai-catalog-item"
                    }
                    type="button"
                    key={category.id}
                    disabled
                >
                    <span>{readTitle(category, "门类")}</span>
                    <Text type="secondary">{category.categoryType || "未分类"}</Text>
                </button>
            ))}
        </div>
    );
};

const renderVolumeList = (volumes: SancaiVolumeRecord[]) => {
    if (!volumes.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无卷目" />;
    }

    return (
        <div className="sancai-volume-list" aria-label="三才图会卷目">
            {volumes.slice(0, 8).map((volume) => (
                <button className="sancai-volume-item" type="button" key={volume.id} disabled>
                    <span>{readTitle(volume, "卷")}</span>
                    <Text type="secondary">{volume.volumeType || "未分类"}</Text>
                </button>
            ))}
        </div>
    );
};

const renderEntryList = (entries: SancaiEntryRecord[]) => {
    if (!entries.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无符合筛选条件的条目" />;
    }

    return (
        <div className="sancai-entry-list" aria-label="三才图会条目">
            {entries.slice(0, 6).map((entry) => (
                <article className="sancai-entry-row" key={entry.id}>
                    <div>
                        <Title level={4}>{readTitle(entry, "条目")}</Title>
                        <Text type="secondary">{readEntrySummary(entry)}</Text>
                    </div>
                    <span>{entry.lifecycleStatus || "UNKNOWN"}</span>
                </article>
            ))}
        </div>
    );
};

const renderDetail = (entry?: SancaiEntryRecord) => {
    if (!entry) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="选择条目后查看详情" />;
    }

    return (
        <div className="sancai-detail-card">
            <Text type="secondary">当前预览</Text>
            <Title level={3}>{readTitle(entry, "条目")}</Title>
            <p>{readEntrySummary(entry)}</p>
        </div>
    );
};

export const SancaiPage = () => {
    const categoriesQuery = useQuery({
        queryKey: ["classics", "sancai", "categories"],
        queryFn: service.listCategories,
        retry: false
    });
    const volumesQuery = useQuery({
        queryKey: ["classics", "sancai", "volumes"],
        queryFn: () => service.listVolumes({}),
        retry: false
    });
    const entriesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "page", "skeleton"],
        queryFn: () =>
            service.pageEntries({
                pageNo: DEFAULT_PAGE_NO,
                pageSize: DEFAULT_PAGE_SIZE
            }),
        retry: false
    });
    const categories = categoriesQuery.data || [];
    const volumes = volumesQuery.data || [];
    const entries = entriesQuery.data?.records || [];
    const selectedEntry = entries[0];
    const isLoading = isQueryLoading(categoriesQuery, volumesQuery, entriesQuery);
    const hasError = isQueryError(categoriesQuery, volumesQuery, entriesQuery);

    return (
        <KuzhambuPage
            className="sancai-page"
            eyebrow="Classics"
            title="三才图会"
            description="按门类、卷目和条目组织三才图会后台治理入口。"
            actions={
                <Button
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
                        renderCategoryList(categories)
                    )}

                    <div className="sancai-panel-heading sancai-panel-heading-secondary">
                        <Title level={3}>卷目</Title>
                        <Text type="secondary">{volumes.length} 卷</Text>
                    </div>
                    {isLoading ? (
                        <Skeleton active paragraph={{ rows: 5 }} />
                    ) : (
                        renderVolumeList(volumes)
                    )}
                </aside>

                <section className="sancai-workspace">
                    <div className="sancai-toolbar">
                        <Input.Search
                            disabled
                            placeholder="搜索标题、原文或摘要"
                            allowClear
                            enterButton="查询"
                        />
                        <Select disabled value="ALL" options={entryStatusOptions} />
                        <Button disabled>重置</Button>
                    </div>

                    <div className="sancai-content-grid">
                        <section className="sancai-list-panel">
                            <div className="sancai-panel-heading">
                                <Title level={3}>条目</Title>
                                <Text type="secondary">
                                    {entriesQuery.data?.totalCount ?? entriesQuery.data?.count ?? 0}{" "}
                                    条
                                </Text>
                            </div>
                            {isLoading ? (
                                <Skeleton active paragraph={{ rows: 7 }} />
                            ) : (
                                renderEntryList(entries)
                            )}
                        </section>

                        <aside className="sancai-detail-panel">
                            <div className="sancai-panel-heading">
                                <Title level={3}>详情</Title>
                            </div>
                            {isLoading ? (
                                <Skeleton active paragraph={{ rows: 6 }} />
                            ) : (
                                renderDetail(selectedEntry)
                            )}
                        </aside>
                    </div>
                </section>
            </div>
        </KuzhambuPage>
    );
};
