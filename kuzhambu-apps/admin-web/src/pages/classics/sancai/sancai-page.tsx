import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Alert, Button, Input, Select } from "antd";
import { useState } from "react";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { SancaiCategoryPanel } from "./components/sancai-category-panel";
import { SancaiEntryPanel } from "./components/sancai-entry-panel";
import { SancaiVolumePanel } from "./components/sancai-volume-panel";
import * as service from "./sancai-service";
import type { SancaiEntryPageQuery } from "./sancai-service";
import type { SancaiCategoryRecord, SancaiVolumeRecord } from "./sancai-types";
import "./sancai-page.css";

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
    const [query, setQuery] = useState<SancaiEntryPageQuery>({});
    const [refreshVersion, setRefreshVersion] = useState(0);
    const [keyword, setKeyword] = useState("");
    const [lifecycleStatus, setLifecycleStatus] = useState("ALL");
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
    const isLoading = isQueryLoading(categoriesQuery, volumesQuery);
    const hasError = isQueryError(categoriesQuery, volumesQuery);

    const updateQuery = (values: Partial<SancaiEntryPageQuery>) => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...values
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
        setQuery((currentQuery) => ({
            categoryId: currentQuery.categoryId,
            volumeId: currentQuery.volumeId
        }));
    };

    const refreshPage = () => {
        reloadQueries(categoriesQuery, volumesQuery);
        setRefreshVersion((version) => version + 1);
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
                    onClick={refreshPage}
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

                    <SancaiEntryPanel
                        categoryId={selectedCategoryId}
                        isCatalogLoading={isLoading}
                        keyword={query.keyword ?? null}
                        lifecycleStatus={query.lifecycleStatus ?? null}
                        refreshVersion={refreshVersion}
                        volumeId={selectedVolumeId}
                        volumes={volumes}
                    />
                </section>
            </div>
        </KuzhambuPage>
    );
};
