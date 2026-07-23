import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import * as categoryService from "../sancai-category-service";
import * as volumeService from "../sancai-volume-service";
import type {
    SancaiCatalogTreeNode,
    SancaiCategoryRecord,
    SancaiVolumeRecord
} from "../sancai-types";

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

export const useSancaiCatalogState = () => {
    const [selectedKey, setSelectedKey] = useState<string | null>(null);
    const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
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
    const actualSelectedKey = selectedKey || ROOT_KEY;
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
    const isRefreshing = categoriesQuery.isFetching || volumesQuery.isFetching;

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

    const refreshCatalogTree = () => {
        reloadQueries(categoriesQuery, volumesQuery);
    };

    return {
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
    };
};
