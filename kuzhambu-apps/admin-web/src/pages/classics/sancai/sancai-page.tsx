import {
    FilterOutlined,
    PlusOutlined,
    ReloadOutlined,
    ScheduleOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { Input, Splitter } from "antd";
import { useState } from "react";
import {
    KuzhambuFilterPanel,
    KuzhambuPage,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuAlert,
    KuzhambuSelect
} from "@/components";

import { SancaiCatalogTreePanel } from "./sancai-catalog-tree-panel";
import { SancaiCategoryPanel } from "./components/sancai-category-panel";
import { SancaiEntryPanel } from "./sancai-entry-panel";
import { SancaiVolumePanel } from "./components/sancai-volume-panel";
import { useSancaiCatalogState } from "./hooks/use-sancai-catalog-state";

import "./sancai-page.css";

const entryStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "草稿", value: "DRAFT" },
    { label: "已发布", value: "PUBLISHED" },
    { label: "已下线", value: "ARCHIVED" }
];

const normalizeKeyword = (value: string) => {
    const keyword = value.trim();
    return keyword || undefined;
};

export const SancaiPage = () => {
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
    const {
        actualSelectedKey,
        categories,
        hasError,
        isLoading,
        isRefreshing,
        refreshCatalogTree,
        selectCatalogNode,
        selectCategory,
        selectedCategory,
        selectedPanel,
        selectedVolume,
        selectVolume,
        setExpandedKeys,
        treeExpandedKeys,
        treeNodes,
        visibleVolumes,
        volumes
    } = useSancaiCatalogState();
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
        refreshCatalogTree();
        setRefreshVersion((version) => version + 1);
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
                                <KuzhambuSelect
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
                            isRefreshing={isRefreshing}
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
