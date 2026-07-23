import {
    FilterOutlined,
    PlusOutlined,
    ReloadOutlined,
    ScheduleOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Input, Select, Splitter } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuFilterPanel } from "@/components/kuzhambu-filter-panel";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { SancaiCatalogTreePanel } from "./components/sancai-catalog-tree-panel";
import { SancaiCategoryPanel } from "./components/sancai-category-panel";
import { SancaiEntryPanel } from "./components/sancai-entry-panel";
import { SancaiVolumePanel } from "./components/sancai-volume-panel";
import * as categoryService from "./sancai-category-service";
import * as volumeService from "./sancai-volume-service";
import type {
    SancaiCatalogTreeNode,
    SancaiCategoryRecord,
    SancaiVolumeRecord
} from "./sancai-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./sancai-page.css";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";

const entryStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "草稿", value: "DRAFT" },
    { label: "已发布", value: "PUBLISHED" },
    { label: "已下线", value: "ARCHIVED" }
];

const EMPTY_CATEGORIES: SancaiCategoryRecord[] = [];
const EMPTY_VOLUMES: SancaiVolumeRecord[] = [];
const ROOT_KEY = "sancai-root";

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

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const toCategoryKey = (id: number) => `category:${id}`;

const toVolumeKey = (id: number) => `volume:${id}`;

const readNodeId = (key: string | null, nodeType: string) => {
    if (!key?.startsWith(`${nodeType}:`)) {
        return null;
    }
    const id = Number(key.slice(nodeType.length + 1));
    return Number.isFinite(id) ? id : null;
};

const buildTreeNodes = (
    categories: SancaiCategoryRecord[],
    volumes: SancaiVolumeRecord[]
): SancaiCatalogTreeNode[] => {
    const categoryNodes: SancaiCatalogTreeNode[] = categories.map((category) => {
        const categoryVolumes = volumes.filter((volume) => volume.categoryId === category.id);
        return {
            children: categoryVolumes.map((volume) => ({
                key: toVolumeKey(volume.id),
                nodeType: "volume",
                title: readTitle(volume, "卷")
            })),
            key: toCategoryKey(category.id),
            nodeType: "category",
            title: readTitle(category, "门类")
        };
    });
    return [
        {
            children: categoryNodes,
            key: ROOT_KEY,
            nodeType: "root",
            title: "三才图会"
        }
    ];
};

export const SancaiPage = () => {
    const [selectedKey, setSelectedKey] = useState<string | null>(null);
    const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
    const [createIntent, setCreateIntent] = useState<{
        target: "category" | "entry" | "volume";
        version: number;
    }>({ target: "category", version: 0 });
    const [refreshVersion, setRefreshVersion] = useState(0);
    const [searchText, setSearchText] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState<string | null>(null);
    const [exportJobsDrawerOpen, setExportJobsDrawerOpen] = useState(false);
    const [isFilterOpen, setIsFilterOpen] = useState(false);
    const [lifecycleStatus, setLifecycleStatus] = useState("ALL");
    const [appliedLifecycleStatus, setAppliedLifecycleStatus] = useState<string | null>(null);
    const categoriesQuery = useQuery({
        queryKey: ["classics", "sancai", "categories"],
        queryFn: categoryService.list,
        retry: false
    });
    const volumesQuery = useQuery({
        queryKey: ["classics", "sancai", "volumes"],
        queryFn: () => volumeService.list(),
        retry: false
    });
    const categories = categoriesQuery.data ?? EMPTY_CATEGORIES;
    const volumes = volumesQuery.data ?? EMPTY_VOLUMES;
    const treeNodes = useMemo(() => buildTreeNodes(categories, volumes), [categories, volumes]);
    const defaultSelectedKey = ROOT_KEY;
    const actualSelectedKey = selectedKey || defaultSelectedKey;
    const isRootSelected = actualSelectedKey === ROOT_KEY;
    const selectedCategoryIdFromKey = readNodeId(actualSelectedKey, "category");
    const selectedVolumeIdFromKey = readNodeId(actualSelectedKey, "volume");
    const selectedVolume = volumes.find((volume) => volume.id === selectedVolumeIdFromKey) ?? null;
    const selectedCategoryId = selectedVolume?.categoryId ?? selectedCategoryIdFromKey;
    const selectedCategory =
        categories.find((category) => category.id === selectedCategoryId) ?? null;
    const visibleVolumes = selectedCategory
        ? volumes.filter((volume) => volume.categoryId === selectedCategory.id)
        : volumes;
    let selectedPanel: "category" | "entry" | "volume" = "category";
    if (selectedCategory && !isRootSelected) {
        selectedPanel = "volume";
    }
    if (selectedVolume) {
        selectedPanel = "entry";
    }
    const treeExpandedKeys = expandedKeys.length ? expandedKeys : [ROOT_KEY];
    const isLoading = isQueryLoading(categoriesQuery, volumesQuery);
    const hasError = isQueryError(categoriesQuery, volumesQuery);
    const enableEntryFilter = selectedPanel === "entry";
    const filterActive = Boolean(appliedLifecycleStatus);
    let addText = "新增门类";
    if (selectedPanel === "volume") {
        addText = "新增卷目";
    }
    if (selectedPanel === "entry") {
        addText = "新增条目";
    }
    let enableAdd = selectedPanel === "category";
    if (selectedPanel === "volume") {
        enableAdd = Boolean(selectedCategory);
    }
    if (selectedPanel === "entry") {
        enableAdd = Boolean(selectedVolume);
    }
    const selectVolume = (volume: SancaiVolumeRecord) => {
        setSelectedKey(toVolumeKey(volume.id));
        const categoryId = volume.categoryId;
        if (categoryId !== null && categoryId !== undefined) {
            setExpandedKeys((keys) => Array.from(new Set([...keys, toCategoryKey(categoryId)])));
        }
    };

    const selectCategory = (category: SancaiCategoryRecord) => {
        setSelectedKey(toCategoryKey(category.id));
        setExpandedKeys((keys) =>
            Array.from(new Set([...keys, ROOT_KEY, toCategoryKey(category.id)]))
        );
    };

    const selectCatalogNode = (node: SancaiCatalogTreeNode) => {
        setSelectedKey(node.key);
        if (node.nodeType === "category") {
            setExpandedKeys((keys) => Array.from(new Set([...keys, ROOT_KEY, node.key])));
        }
    };

    const applyFilters = () => {
        setAppliedKeyword(normalizeKeyword(searchText) ?? null);
        setAppliedLifecycleStatus(lifecycleStatus === "ALL" ? null : lifecycleStatus);
    };

    const resetFilters = () => {
        setSearchText("");
        setLifecycleStatus("ALL");
        setAppliedKeyword(null);
        setAppliedLifecycleStatus(null);
    };

    const refreshPage = () => {
        reloadQueries(categoriesQuery, volumesQuery);
        setRefreshVersion((version) => version + 1);
    };

    const refreshCatalogTree = () => {
        reloadQueries(categoriesQuery, volumesQuery);
    };

    const startCreate = () => {
        setCreateIntent((intent) => ({
            target: selectedPanel,
            version: intent.version + 1
        }));
    };

    return (
        <KuzhambuPage
            className="sancai-page"
            title="三才图会"
            description="按门类、卷目和条目组织三才图会后台治理入口。"
            actions={
                <KuzhambuSpace className="sancai-page-actions">
                    {enableEntryFilter ? (
                        <Input
                            allowClear
                            aria-label="搜索三才图会条目"
                            className="sancai-page-search"
                            placeholder="搜索标题、原文或摘要"
                            prefix={<SearchOutlined />}
                            value={searchText}
                            onChange={(event) => {
                                const { value } = event.target;
                                setSearchText(value);
                                setAppliedKeyword(normalizeKeyword(value) ?? null);
                            }}
                        />
                    ) : null}
                    {enableEntryFilter ? (
                        <KuzhambuButton
                            testId="classics-sancai-sancai-filter-button"
                            className={
                                isFilterOpen || filterActive ? "sancai-page-filter-active" : ""
                            }
                            icon={<FilterOutlined />}
                            aria-expanded={isFilterOpen}
                            onClick={() => setIsFilterOpen((open) => !open)}
                        >
                            筛选
                        </KuzhambuButton>
                    ) : null}
                    <KuzhambuButton
                        testId="classics-sancai-sancai-action-button"
                        icon={<ReloadOutlined />}
                        onClick={refreshPage}
                    >
                        刷新
                    </KuzhambuButton>
                    {selectedVolume ? (
                        <KuzhambuButton
                            testId="classics-sancai-sancai-action-button-2"
                            icon={<ScheduleOutlined />}
                            onClick={() => setExportJobsDrawerOpen(true)}
                        >
                            任务
                        </KuzhambuButton>
                    ) : null}
                    {enableAdd ? (
                        <KuzhambuButton
                            testId="classics-sancai-sancai-action-button-3"
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={startCreate}
                        >
                            {addText}
                        </KuzhambuButton>
                    ) : null}
                </KuzhambuSpace>
            }
        >
            {enableEntryFilter ? (
                <KuzhambuFilterPanel
                    open={isFilterOpen}
                    resetDisabled={!filterActive}
                    fields={[
                        {
                            name: "lifecycleStatus",
                            label: "条目状态",
                            render: () => (
                                <Select
                                    aria-label="三才图会条目状态"
                                    value={lifecycleStatus}
                                    options={entryStatusOptions}
                                    onChange={setLifecycleStatus}
                                />
                            )
                        }
                    ]}
                    onApply={() => {
                        applyFilters();
                        setIsFilterOpen(false);
                    }}
                    onReset={resetFilters}
                />
            ) : null}
            {hasError ? (
                <KuzhambuAlert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="三才图会数据加载失败"
                    description="请确认后台三才图会接口可用后刷新页面。"
                />
            ) : null}
            <Splitter className="sancai-work-area">
                <Splitter.Panel defaultSize={320} min={260} max={520}>
                    <aside className="sancai-catalog-panel">
                        <SancaiCatalogTreePanel
                            expandedKeys={treeExpandedKeys}
                            isRefreshing={categoriesQuery.isFetching || volumesQuery.isFetching}
                            isLoading={isLoading}
                            nodes={treeNodes}
                            selectedKey={actualSelectedKey}
                            title="目录"
                            onExpandedKeysChange={setExpandedKeys}
                            onRefresh={refreshCatalogTree}
                            onSelectNode={selectCatalogNode}
                        />
                    </aside>
                </Splitter.Panel>
                <Splitter.Panel className="sancai-work-panel">
                    {selectedVolume ? (
                        <SancaiEntryPanel
                            key={`entry-${selectedVolume.id}-${createIntent.version}`}
                            categories={categories}
                            categoryId={selectedCategory?.id ?? null}
                            defaultCreateOpen={
                                createIntent.target === "entry" && createIntent.version > 0
                            }
                            exportJobsDrawerOpen={exportJobsDrawerOpen}
                            isCatalogLoading={isLoading}
                            keyword={appliedKeyword}
                            lifecycleStatus={appliedLifecycleStatus}
                            refreshVersion={refreshVersion}
                            volumeId={selectedVolume.id}
                            volumes={volumes}
                            onExportJobsDrawerOpenChange={setExportJobsDrawerOpen}
                        />
                    ) : selectedPanel === "volume" ? (
                        <SancaiVolumePanel
                            key={`volume-${selectedCategory?.id ?? "none"}-${createIntent.version}`}
                            categories={categories}
                            defaultCreateOpen={
                                createIntent.target === "volume" && createIntent.version > 0
                            }
                            volumes={visibleVolumes}
                            isLoading={isLoading}
                            selectedCategory={selectedCategory}
                            selectedVolume={selectedVolume}
                            onSelect={selectVolume}
                        />
                    ) : (
                        <SancaiCategoryPanel
                            key={`category-${createIntent.version}`}
                            categories={categories}
                            defaultCreateOpen={
                                createIntent.target === "category" && createIntent.version > 0
                            }
                            isLoading={isLoading}
                            selectedCategory={selectedCategory}
                            onSelect={selectCategory}
                        />
                    )}
                </Splitter.Panel>
            </Splitter>
        </KuzhambuPage>
    );
};
