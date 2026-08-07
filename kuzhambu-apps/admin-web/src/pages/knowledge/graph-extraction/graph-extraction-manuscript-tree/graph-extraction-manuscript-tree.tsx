import { Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { KuzhambuSpace, KuzhambuCard } from "@/components";

import type { GraphWorkbenchManuscriptNode } from "../graph-extraction-types";

interface GraphExtractionManuscriptTreeProps {
    loading?: boolean;
    nodes: GraphWorkbenchManuscriptNode[];
    selectedNodeKey?: string | null;
    onLoadChildren: (nodeKey: string) => Promise<void>;
    onSelectManuscript: (node: GraphWorkbenchManuscriptNode) => void;
}

const { Text } = Typography;

const isSelectableDirectoryNode = (node: GraphWorkbenchManuscriptNode) =>
    node.nodeType === "VOLUME";

const isExpandableDirectoryNode = (node: GraphWorkbenchManuscriptNode) =>
    node.nodeType === "SOURCE_ROOT" || node.nodeType === "CATEGORY";

const renderTitle = (node: GraphWorkbenchManuscriptNode) => (
    <span className="graph-extraction-manuscript-tree-node">
        <Text ellipsis className="graph-extraction-manuscript-tree-title">
            {node.title || node.nodeKey}
        </Text>
    </span>
);

const toTreeData = (nodes: GraphWorkbenchManuscriptNode[]): DataNode[] =>
    nodes.map((node) => ({
        key: node.nodeKey,
        title: renderTitle(node),
        isLeaf: node.nodeType === "MANUSCRIPT" || node.nodeType === "VOLUME",
        children: node.children?.length ? toTreeData(node.children) : undefined
    }));

const findNode = (
    nodes: GraphWorkbenchManuscriptNode[],
    nodeKey?: string
): GraphWorkbenchManuscriptNode | null => {
    if (!nodeKey) {
        return null;
    }
    for (const node of nodes) {
        if (node.nodeKey === nodeKey) {
            return node;
        }
        const child = findNode(node.children || [], nodeKey);
        if (child) {
            return child;
        }
    }
    return null;
};

export const GraphExtractionManuscriptTree = ({
    loading = false,
    nodes,
    selectedNodeKey,
    onLoadChildren,
    onSelectManuscript
}: GraphExtractionManuscriptTreeProps) => {
    const rootNodeKeys = useMemo(() => nodes.map((node) => node.nodeKey), [nodes]);
    const [expandedKeys, setExpandedKeys] = useState<Key[] | null>(null);
    const mergedExpandedKeys = expandedKeys ?? rootNodeKeys;

    return (
        <KuzhambuCard
            className="graph-extraction-create-card graph-extraction-manuscript-tree-card"
            loading={loading && nodes.length === 0}
            variant="borderless"
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <Tree
                    blockNode
                    expandedKeys={mergedExpandedKeys}
                    loadData={(treeNode) => onLoadChildren(String(treeNode.key))}
                    selectedKeys={selectedNodeKey ? [selectedNodeKey] : []}
                    treeData={toTreeData(nodes)}
                    onExpand={(keys) => setExpandedKeys(keys)}
                    onSelect={(keys) => {
                        const nodeKey = String(keys[0] || "");
                        const node = findNode(nodes, nodeKey);
                        if (node && isExpandableDirectoryNode(node)) {
                            setExpandedKeys(Array.from(new Set([...mergedExpandedKeys, nodeKey])));
                            if (!node.children?.length) {
                                void onLoadChildren(nodeKey);
                            }
                            return;
                        }
                        if (node && isSelectableDirectoryNode(node)) {
                            onSelectManuscript(node);
                        }
                    }}
                />
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
