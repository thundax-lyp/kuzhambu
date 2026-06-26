import { BookOutlined, FolderOutlined, ReloadOutlined } from "@ant-design/icons";
import { Button, Empty, Skeleton, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import type { Key, ReactNode } from "react";
import { useMemo } from "react";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { SancaiCatalogNodeType, SancaiCatalogTreeNode } from "../sancai-types";

const { Text } = Typography;

interface SancaiCatalogTreePanelProps {
    expandedKeys: string[];
    isRefreshing: boolean;
    isLoading: boolean;
    nodes: SancaiCatalogTreeNode[];
    onExpandedKeysChange: (keys: string[]) => void;
    onRefresh: () => void;
    onSelectNode: (node: SancaiCatalogTreeNode) => void;
    selectedKey: string | null;
    title: string;
}

const iconByType: Record<SancaiCatalogNodeType, ReactNode> = {
    category: <FolderOutlined />,
    root: <FolderOutlined />,
    volume: <BookOutlined />
};

const flattenNodes = (nodes: SancaiCatalogTreeNode[]): SancaiCatalogTreeNode[] => {
    return nodes.flatMap((node) => [node, ...flattenNodes(node.children || [])]);
};

const toTreeData = (nodes: SancaiCatalogTreeNode[]): DataNode[] => {
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
    isRefreshing,
    isLoading,
    nodes,
    onExpandedKeysChange,
    onRefresh,
    onSelectNode,
    selectedKey,
    title
}: SancaiCatalogTreePanelProps) => {
    const nodeByKey = useMemo(() => {
        return new Map(flattenNodes(nodes).map((node) => [node.key, node]));
    }, [nodes]);
    const treeData = useMemo(() => toTreeData(nodes), [nodes]);
    let treeContent = <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无目录" />;
    if (isLoading) {
        treeContent = <Skeleton active paragraph={{ rows: 10 }} />;
    } else if (nodes.length) {
        treeContent = (
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
        );
    }

    return (
        <div className="sancai-catalog-tree-panel" aria-label="三才图会目录树">
            <div className="sancai-catalog-panel-head">
                <KuzhambuSpace size={8}>
                    <FolderOutlined />
                    <Text strong>{title}</Text>
                </KuzhambuSpace>
                <Button
                    aria-label="刷新三才图会目录树"
                    className="sancai-catalog-refresh"
                    icon={<ReloadOutlined />}
                    loading={isRefreshing}
                    size="small"
                    onClick={onRefresh}
                />
            </div>
            {treeContent}
        </div>
    );
};
