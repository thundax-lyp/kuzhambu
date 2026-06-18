import { BookOutlined, FileTextOutlined, FolderOutlined } from "@ant-design/icons";
import { Empty, Skeleton, Tree, Typography } from "antd";
import type { Key, ReactNode } from "react";
import { useMemo } from "react";
import type { SancaiCatalogNodeType, SancaiCatalogTreeNode } from "../sancai-types";

const { Text } = Typography;

interface SancaiCatalogTreePanelProps {
    expandedKeys: string[];
    isLoading: boolean;
    nodes: SancaiCatalogTreeNode[];
    onExpandedKeysChange: (keys: string[]) => void;
    onSelectNode: (node: SancaiCatalogTreeNode) => void;
    selectedKey: string | null;
}

const iconByType: Record<SancaiCatalogNodeType, ReactNode> = {
    category: <FolderOutlined />,
    entry: <FileTextOutlined />,
    volume: <BookOutlined />
};

const flattenNodes = (nodes: SancaiCatalogTreeNode[]): SancaiCatalogTreeNode[] => {
    return nodes.flatMap((node) => [node, ...flattenNodes(node.children || [])]);
};

const toTreeData = (nodes: SancaiCatalogTreeNode[]) => {
    return nodes.map((node) => ({
        children: node.children ? toTreeData(node.children) : undefined,
        key: node.key,
        title: (
            <span className="sancai-catalog-tree-title">
                <span className="sancai-catalog-tree-icon" aria-hidden>
                    {iconByType[node.nodeType]}
                </span>
                <Text>{node.title}</Text>
            </span>
        )
    }));
};

export const SancaiCatalogTreePanel = ({
    expandedKeys,
    isLoading,
    nodes,
    onExpandedKeysChange,
    onSelectNode,
    selectedKey
}: SancaiCatalogTreePanelProps) => {
    const nodeByKey = useMemo(() => {
        return new Map(flattenNodes(nodes).map((node) => [node.key, node]));
    }, [nodes]);
    const treeData = useMemo(() => toTreeData(nodes), [nodes]);

    if (isLoading) {
        return <Skeleton active paragraph={{ rows: 10 }} />;
    }

    if (!nodes.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无目录" />;
    }

    return (
        <div className="sancai-catalog-tree-panel" aria-label="三才图会目录树">
            <Tree
                blockNode
                expandedKeys={expandedKeys}
                selectedKeys={selectedKey ? [selectedKey] : []}
                treeData={treeData}
                onExpand={(keys: Key[]) => onExpandedKeysChange(keys.map(String))}
                onSelect={(keys) => {
                    const selectedNode = nodeByKey.get(String(keys[0] ?? ""));
                    if (selectedNode) {
                        onSelectNode(selectedNode);
                    }
                }}
            />
        </div>
    );
};
