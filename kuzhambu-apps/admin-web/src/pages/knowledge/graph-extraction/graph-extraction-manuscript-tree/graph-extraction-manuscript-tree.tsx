import { Tag, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { KuzhambuSpace, KuzhambuCard } from "@/components";

import type { GraphWorkbenchManuscriptNode, GraphWorkbenchStatus } from "../graph-extraction-types";

interface GraphExtractionManuscriptTreeProps {
    loading?: boolean;
    nodes: GraphWorkbenchManuscriptNode[];
    selectedNodeKey?: string | null;
    onLoadChildren: (nodeKey: string) => Promise<void>;
    onSelectManuscript: (node: GraphWorkbenchManuscriptNode) => void;
}

const { Text } = Typography;

const STATUS_LABELS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "未抽取"],
    ["EXTRACTING", "抽取中"],
    ["EXTRACTION_FAILED", "抽取失败"],
    ["CANDIDATE_READY", "待应用"],
    ["APPLIED", "待精修"],
    ["REFINING", "精修中"],
    ["REFINED", "已精修"],
    ["QUALITY_ISSUE", "质量异常"]
]);

const STATUS_COLORS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "default"],
    ["EXTRACTING", "processing"],
    ["EXTRACTION_FAILED", "error"],
    ["CANDIDATE_READY", "warning"],
    ["APPLIED", "blue"],
    ["REFINING", "purple"],
    ["REFINED", "success"],
    ["QUALITY_ISSUE", "error"]
]);

const isManuscriptNode = (node: GraphWorkbenchManuscriptNode) =>
    node.nodeType === "MANUSCRIPT" && node.sourceContentType && node.sourceContentId;

const statusLabel = (status?: GraphWorkbenchStatus | null) =>
    STATUS_LABELS.get(status || "") || status || "未知";

const statusColor = (status?: GraphWorkbenchStatus | null) =>
    STATUS_COLORS.get(status || "") || "default";

const renderTitle = (node: GraphWorkbenchManuscriptNode) => (
    <span className="graph-extraction-manuscript-tree-node">
        <Text ellipsis className="graph-extraction-manuscript-tree-title">
            {node.title || node.nodeKey}
        </Text>
        {isManuscriptNode(node) ? (
            <Tag color={statusColor(node.graphStatus)}>{statusLabel(node.graphStatus)}</Tag>
        ) : null}
    </span>
);

const toTreeData = (nodes: GraphWorkbenchManuscriptNode[]): DataNode[] =>
    nodes.map((node) => ({
        key: node.nodeKey,
        title: renderTitle(node),
        isLeaf: node.nodeType === "MANUSCRIPT",
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
                        const node = findNode(nodes, String(keys[0] || ""));
                        if (node && isManuscriptNode(node)) {
                            onSelectManuscript(node);
                        }
                    }}
                />
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
