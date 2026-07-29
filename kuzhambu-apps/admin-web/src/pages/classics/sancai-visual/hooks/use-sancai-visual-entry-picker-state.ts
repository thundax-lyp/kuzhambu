import { BookOutlined, FileTextOutlined, FolderOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import type { DataNode } from "antd/es/tree";
import type { Key } from "react";
import { createElement } from "react";
import { useMemo, useState } from "react";
import { useSancaiCatalogState } from "@/pages/classics/sancai/hooks/use-sancai-catalog-state";
import type { SancaiCatalogTreeNode } from "@/pages/classics/sancai/sancai-types";
import * as entryService from "@/pages/classics/sancai-visual/sancai-visual-service";
import type { SancaiEntryRecord } from "@/pages/classics/sancai-visual/sancai-visual-types";

const readEntryTitle = (entry: SancaiEntryRecord | null | undefined) => {
    if (!entry) {
        return "未选择稿件";
    }
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const toEntryPickerEntryKey = (entryId: string) => `entry:${entryId}`;

const flattenCatalogNodes = (nodes: SancaiCatalogTreeNode[]): SancaiCatalogTreeNode[] => {
    return nodes.flatMap((node) => [node, ...flattenCatalogNodes(node.children || [])]);
};

export const useSancaiVisualEntryPickerState = (open: boolean) => {
    const [pendingEntry, setPendingEntry] = useState<SancaiEntryRecord | null>(null);
    const {
        actualSelectedKey,
        hasError: hasCatalogError,
        isLoading: isCatalogLoading,
        selectedCategory,
        selectedVolume,
        selectCatalogNode,
        setExpandedKeys,
        treeExpandedKeys,
        treeNodes
    } = useSancaiCatalogState({ enabled: open });
    const entriesQuery = useQuery({
        queryKey: [
            "classics",
            "sancai",
            "visual",
            "entries",
            selectedCategory?.id,
            selectedVolume?.id
        ],
        queryFn: () =>
            entryService.list({
                categoryId: selectedCategory?.id ?? null,
                volumeId: selectedVolume?.id ?? null,
                sortDirection: "ASC"
            }),
        enabled: open && Boolean(selectedVolume?.id),
        retry: false
    });
    const entries = useMemo(() => entriesQuery.data || [], [entriesQuery.data]);
    const catalogNodeByKey = useMemo(() => {
        return new Map(flattenCatalogNodes(treeNodes).map((node) => [node.key, node]));
    }, [treeNodes]);
    const entryByPickerKey = useMemo(() => {
        return new Map(entries.map((entry) => [toEntryPickerEntryKey(entry.id), entry]));
    }, [entries]);
    const treeData = useMemo(() => {
        const entryChildren: DataNode[] = entries.map((entry) => ({
            icon: createElement(FileTextOutlined),
            key: toEntryPickerEntryKey(entry.id),
            title: readEntryTitle(entry)
        }));
        const buildTreeData = (nodes: SancaiCatalogTreeNode[]): DataNode[] =>
            nodes.map((node) => {
                const isSelectedVolume =
                    node.key === actualSelectedKey && node.nodeType === "volume";
                let children = node.children ? buildTreeData(node.children) : undefined;
                if (isSelectedVolume) {
                    if (entriesQuery.isLoading) {
                        children = [
                            {
                                disabled: true,
                                key: `${node.key}:loading`,
                                title: "加载稿件中"
                            }
                        ];
                    } else if (entryChildren.length) {
                        children = entryChildren;
                    } else {
                        children = [
                            {
                                disabled: true,
                                key: `${node.key}:empty`,
                                title: "暂无稿件"
                            }
                        ];
                    }
                }
                return {
                    children,
                    icon: createElement(node.nodeType === "volume" ? BookOutlined : FolderOutlined),
                    key: node.key,
                    title: node.title
                };
            });
        return buildTreeData(treeNodes);
    }, [actualSelectedKey, entries, entriesQuery.isLoading, treeNodes]);
    const selectedKeys = [
        pendingEntry ? toEntryPickerEntryKey(pendingEntry.id) : actualSelectedKey
    ].filter(Boolean);

    const selectNode = (keys: Key[]) => {
        const key = String(keys[0] ?? "");
        const entry = entryByPickerKey.get(key);
        if (entry) {
            setPendingEntry(entry);
            return;
        }
        const catalogNode = catalogNodeByKey.get(key);
        if (catalogNode) {
            setPendingEntry(null);
            selectCatalogNode(catalogNode);
            if (catalogNode.nodeType === "volume") {
                setExpandedKeys((keys) => Array.from(new Set([...keys, catalogNode.key])));
            }
        }
    };

    const resetPendingEntry = () => setPendingEntry(null);

    return {
        expandedKeys: treeExpandedKeys,
        hasError: entriesQuery.isError || hasCatalogError,
        isCatalogLoading,
        pendingEntry,
        selectedKeys,
        selectedVolume,
        treeData,
        readEntryTitle,
        resetPendingEntry,
        selectNode,
        setExpandedKeys
    };
};
