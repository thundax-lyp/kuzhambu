import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Alert, Button, Select } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { SancaiCatalogTreePanel } from "./components/sancai-catalog-tree-panel";
import { SancaiContentPanel } from "./components/sancai-content-panel";
import { SancaiEntryPanel } from "./components/sancai-entry-panel";
import { SancaiVolumePanel } from "./components/sancai-volume-panel";
import * as categoryService from "./services/sancai-category-service";
import * as entryService from "./services/sancai-entry-service";
import * as volumeService from "./services/sancai-volume-service";
import type {
    SancaiCatalogTreeNode,
    SancaiCategoryRecord,
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "./sancai-types";
import "./sancai-page.css";

const entryStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "草稿", value: "DRAFT" },
    { label: "已发布", value: "PUBLISHED" },
    { label: "已归档", value: "ARCHIVED" }
];

const EMPTY_CATEGORIES: SancaiCategoryRecord[] = [];
const EMPTY_ENTRIES: SancaiEntryRecord[] = [];
const EMPTY_VOLUMES: SancaiVolumeRecord[] = [];

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

const toEntryKey = (id: number) => `entry:${id}`;

const readNodeId = (key: string | null, nodeType: string) => {
    if (!key?.startsWith(`${nodeType}:`)) {
        return null;
    }
    const id = Number(key.slice(nodeType.length + 1));
    return Number.isFinite(id) ? id : null;
};

const buildTreeNodes = (
    categories: SancaiCategoryRecord[],
    volumes: SancaiVolumeRecord[],
    entries: SancaiEntryRecord[]
): SancaiCatalogTreeNode[] => {
    return categories.map((category) => {
        const categoryVolumes = volumes.filter((volume) => volume.categoryId === category.id);
        return {
            children: categoryVolumes.map((volume) => {
                const volumeEntries = entries.filter((entry) => entry.volumeId === volume.id);
                return {
                    children: volumeEntries.map((entry) => ({
                        key: toEntryKey(entry.id),
                        nodeType: "entry",
                        title: readTitle(entry, "条目")
                    })),
                    key: toVolumeKey(volume.id),
                    nodeType: "volume",
                    title: readTitle(volume, "卷")
                };
            }),
            key: toCategoryKey(category.id),
            nodeType: "category",
            title: readTitle(category, "门类")
        };
    });
};

export const SancaiPage = () => {
    const [selectedKey, setSelectedKey] = useState<string | null>(null);
    const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
    const [createIntent, setCreateIntent] = useState<{
        target: "content" | "entry" | "volume";
        version: number;
    }>({ target: "volume", version: 0 });
    const [refreshVersion, setRefreshVersion] = useState(0);
    const [keyword, setKeyword] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState<string | null>(null);
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
    const entriesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "tree", refreshVersion],
        queryFn: () => entryService.list({ sortDirection: "ASC" }),
        retry: false
    });
    const categories = categoriesQuery.data ?? EMPTY_CATEGORIES;
    const volumes = volumesQuery.data ?? EMPTY_VOLUMES;
    const entries = entriesQuery.data ?? EMPTY_ENTRIES;
    const treeNodes = useMemo(
        () => buildTreeNodes(categories, volumes, entries),
        [categories, entries, volumes]
    );
    const defaultSelectedKey = categories[0] ? toCategoryKey(categories[0].id) : null;
    const actualSelectedKey = selectedKey || defaultSelectedKey;
    const selectedCategoryIdFromKey = readNodeId(actualSelectedKey, "category");
    const selectedVolumeIdFromKey = readNodeId(actualSelectedKey, "volume");
    const selectedEntryIdFromKey = readNodeId(actualSelectedKey, "entry");
    const selectedEntry = entries.find((entry) => entry.id === selectedEntryIdFromKey) ?? null;
    const selectedVolumeId = selectedEntry?.volumeId ?? selectedVolumeIdFromKey;
    const selectedVolume = volumes.find((volume) => volume.id === selectedVolumeId) ?? null;
    const selectedCategoryId = selectedVolume?.categoryId ?? selectedCategoryIdFromKey;
    const selectedCategory =
        categories.find((category) => category.id === selectedCategoryId) ?? null;
    const visibleVolumes = selectedCategory
        ? volumes.filter((volume) => volume.categoryId === selectedCategory.id)
        : volumes;
    let selectedPanel: "content" | "entry" | "volume" = "volume";
    if (selectedVolume) {
        selectedPanel = "entry";
    }
    if (selectedEntry) {
        selectedPanel = "content";
    }
    const treeExpandedKeys = expandedKeys.length
        ? expandedKeys
        : categories.map((category) => toCategoryKey(category.id));
    const isLoading = isQueryLoading(categoriesQuery, volumesQuery, entriesQuery);
    const hasError = isQueryError(categoriesQuery, volumesQuery, entriesQuery);
    let addText = "新增卷目";
    if (selectedPanel === "entry") {
        addText = "新增条目";
    }
    if (selectedPanel === "content") {
        addText = "新增内容";
    }
    let enableAdd = Boolean(selectedCategory);
    if (selectedPanel === "entry") {
        enableAdd = Boolean(selectedVolume);
    }
    if (selectedPanel === "content") {
        enableAdd = Boolean(selectedEntry);
    }

    const selectVolume = (volume: SancaiVolumeRecord) => {
        setSelectedKey(toVolumeKey(volume.id));
        if (volume.categoryId) {
            setExpandedKeys((keys) => Array.from(new Set([...keys, toCategoryKey(volume.categoryId)])));
        }
    };

    const selectEntry = (entry: SancaiEntryRecord) => {
        setSelectedKey(toEntryKey(entry.id));
        const volume = volumes.find((item) => item.id === entry.volumeId);
        if (volume) {
            setExpandedKeys((keys) =>
                Array.from(
                    new Set([
                        ...keys,
                        ...(volume.categoryId ? [toCategoryKey(volume.categoryId)] : []),
                        toVolumeKey(volume.id)
                    ])
                )
            );
        }
    };

    const clearEntry = () => {
        if (selectedVolume) {
            setSelectedKey(toVolumeKey(selectedVolume.id));
            return;
        }
        if (selectedCategory) {
            setSelectedKey(toCategoryKey(selectedCategory.id));
        }
    };

    const applyFilters = () => {
        setAppliedKeyword(normalizeKeyword(keyword) ?? null);
        setAppliedLifecycleStatus(lifecycleStatus === "ALL" ? null : lifecycleStatus);
    };

    const resetFilters = () => {
        setKeyword("");
        setLifecycleStatus("ALL");
        setAppliedKeyword(null);
        setAppliedLifecycleStatus(null);
    };

    const refreshPage = () => {
        reloadQueries(categoriesQuery, volumesQuery, entriesQuery);
        setRefreshVersion((version) => version + 1);
    };

    const startCreate = () => {
        setCreateIntent((intent) => ({
            target: selectedPanel,
            version: intent.version + 1
        }));
    };

    let panelContent = (
        <SancaiVolumePanel
            key={`volume-${selectedCategory?.id ?? "none"}-${createIntent.version}`}
            categories={categories}
            defaultCreateOpen={createIntent.target === "volume" && createIntent.version > 0}
            volumes={visibleVolumes}
            isLoading={isLoading}
            selectedCategory={selectedCategory}
            selectedVolume={selectedVolume}
            onSelect={selectVolume}
        />
    );
    if (selectedVolume) {
        panelContent = (
            <SancaiEntryPanel
                key={`entry-${selectedVolume.id}-${createIntent.version}`}
                categoryId={selectedCategory?.id ?? null}
                defaultCreateOpen={createIntent.target === "entry" && createIntent.version > 0}
                isCatalogLoading={isLoading}
                keyword={appliedKeyword}
                lifecycleStatus={appliedLifecycleStatus}
                refreshVersion={refreshVersion}
                selectedEntryId={selectedEntryIdFromKey}
                volumeId={selectedVolume.id}
                volumes={visibleVolumes}
                onClearEntry={clearEntry}
                onSelectEntry={selectEntry}
            />
        );
    }
    if (selectedEntry) {
        panelContent = (
            <SancaiContentPanel
                key={`content-${selectedEntry.id}-${createIntent.version}`}
                defaultCreateOpen={createIntent.target === "content" && createIntent.version > 0}
                entry={selectedEntry}
                refreshVersion={refreshVersion}
            />
        );
    }

    const pageContent = (
        <>
            {hasError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    message="三才图会数据加载失败"
                    description="请确认后台三才图会接口可用后刷新页面。"
                />
            ) : null}
            {panelContent}
        </>
    );

    return (
        <KuzhambuListPage
            pageClassName="sancai-page"
            title="三才图会"
            description="按门类、卷目和条目组织三才图会后台治理入口。"
            addText={addText}
            content={pageContent}
            enableAdd={enableAdd}
            enableFilter={selectedPanel === "entry"}
            enableSearch={selectedPanel === "entry"}
            filterActive={Boolean(appliedLifecycleStatus)}
            filterFields={[
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
            pageActions={
                <Button
                    aria-label="刷新三才图会数据"
                    icon={<ReloadOutlined />}
                    onClick={refreshPage}
                >
                    刷新
                </Button>
            }
            searchPlaceholder="搜索标题、原文或摘要"
            searchValue={keyword}
            tableAside={
                <SancaiCatalogTreePanel
                    expandedKeys={treeExpandedKeys}
                    isLoading={isLoading}
                    nodes={treeNodes}
                    selectedKey={actualSelectedKey}
                    onExpandedKeysChange={setExpandedKeys}
                    onSelectNode={(node) => setSelectedKey(node.key)}
                />
            }
            tableAsideClassName="sancai-catalog-panel"
            tableAsidePlacement="left"
            tableAreaClassName="sancai-shell"
            onAdd={startCreate}
            onFilterApply={applyFilters}
            onFilterReset={resetFilters}
            onSearchChange={(value) => {
                setKeyword(value);
                setAppliedKeyword(normalizeKeyword(value) ?? null);
            }}
        />
    );
};
