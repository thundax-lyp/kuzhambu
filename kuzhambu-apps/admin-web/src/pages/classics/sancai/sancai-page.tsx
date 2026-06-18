import { ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Input, Select, Skeleton, Typography } from "antd";
import { useState } from "react";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { SancaiCategoryPanel } from "./components/sancai-category-panel";
import { SancaiEntryList } from "./components/sancai-entry-list";
import { SancaiEntryModel } from "./components/sancai-entry-model";
import type { SancaiEntryFormValues } from "./components/sancai-form-values";
import { SancaiVolumePanel } from "./components/sancai-volume-panel";
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
    const categoriesQuery = useQuery({
        queryKey: ["classics", "sancai", "categories"],
        queryFn: service.listCategories,
        retry: false
    });
    const categories = categoriesQuery.data || [];
    const selectedCategory =
        categories.find((category) => category.id === query.categoryId) ?? categories[0] ?? null;
    const selectedCategoryId = selectedCategory?.id ?? null;
    const volumesQuery = useQuery({
        queryKey: ["classics", "sancai", "volumes", selectedCategoryId],
        queryFn: () => service.listVolumes({ categoryId: selectedCategoryId }),
        enabled: selectedCategoryId !== null,
        retry: false
    });
    const volumes = volumesQuery.data || [];
    const selectedVolume =
        volumes.find((volume) => volume.id === query.volumeId) ?? volumes[0] ?? null;
    const selectedVolumeId = selectedVolume?.id ?? null;
    const effectiveEntryQuery: SancaiEntryPageQuery = {
        ...query,
        categoryId: selectedCategoryId ?? undefined,
        volumeId: selectedVolumeId ?? undefined
    };
    const entriesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "page", effectiveEntryQuery, queryVersion],
        queryFn: () => service.pageEntries(effectiveEntryQuery),
        enabled: selectedCategoryId !== null,
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["classics", "sancai", "entry", activeEntryId],
        queryFn: () => service.getEntry(activeEntryId || 0),
        enabled: activeEntryId !== null,
        retry: false
    });
    const updateEntryMutation = useMutation({
        mutationFn: service.updateEntry,
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
    const entries = entriesQuery.data?.records || [];
    const totalCount = entriesQuery.data?.totalCount ?? entriesQuery.data?.count ?? 0;
    const currentPageNo = entriesQuery.data?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = entriesQuery.data?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const selectedEntry = detailQuery.data;
    const isLoading = isQueryLoading(categoriesQuery, volumesQuery, entriesQuery);
    const hasError = isQueryError(categoriesQuery, volumesQuery, entriesQuery);
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
            categoryId: volume.categoryId ?? selectedCategory?.id,
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
    };

    const updateEntry = (form: SancaiEntryFormValues) => {
        const entry = detailQuery.data || selectedEntry;
        if (!entry) {
            return;
        }
        updateEntryMutation.mutate({
            id: entry.id,
            volumeId: entry.volumeId,
            title: form.title,
            originalText: form.originalText,
            translationText: form.translationText,
            summary: form.summary,
            lifecycleStatus: entry.lifecycleStatus,
            visibility: form.visibility,
            translationStatus: entry.translationStatus,
            imageStatus: entry.imageStatus,
            visualAssetStatus: entry.visualAssetStatus,
            refinementStatus: entry.refinementStatus
        });
    };

    return (
        <KuzhambuPage
            className="sancai-page"
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
                    <div className="sancai-catalog-columns">
                        <SancaiCategoryPanel
                            categories={categories}
                            isLoading={isLoading}
                            selectedCategory={selectedCategory}
                            onSelect={selectCategory}
                        />
                        <SancaiVolumePanel
                            categories={categories}
                            volumes={volumes}
                            isLoading={isLoading}
                            selectedCategory={selectedCategory}
                            selectedVolume={selectedVolume}
                            onSelect={selectVolume}
                        />
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
                            <SancaiEntryList
                                currentPageNo={currentPageNo}
                                currentPageSize={currentPageSize}
                                entries={entries}
                                isLoading={isLoading}
                                totalCount={totalCount}
                                volumes={volumes}
                                onPageChange={changePage}
                                onView={openEntry}
                            />
                        </section>

                        <aside className="sancai-detail-panel">
                            <div className="sancai-panel-heading">
                                <Title level={3}>详情</Title>
                            </div>
                            {isLoading || detailQuery.isLoading ? (
                                <Skeleton active paragraph={{ rows: 6 }} />
                            ) : (
                                <SancaiEntryModel
                                    key={selectedEntry?.id ?? "empty"}
                                    entry={selectedEntry}
                                    isSubmitting={updateEntryMutation.isPending}
                                    onSubmit={updateEntry}
                                />
                            )}
                        </aside>
                    </div>
                </section>
            </div>
        </KuzhambuPage>
    );
};
