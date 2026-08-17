import { BookOutlined, FolderOutlined, ReloadOutlined } from "@ant-design/icons";
import { Empty, Skeleton, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import type { Key, ReactNode } from "react";
import { useMemo } from "react";
import { KuzhambuButton, KuzhambuSpace } from "@/components";
import type {
    MaterialCatalogNode,
    MaterialCatalogNodeType
} from "@/pages/knowledge/graph-material/graph-material-types";

import "./material-catalog-panel.css";

const { Text } = Typography;

interface MaterialCatalogPanelProps {
    expandedKeys: string[];
    isLoading?: boolean;
    isRefreshing?: boolean;
    nodes: MaterialCatalogNode[];
    selectedKey: string;
    onExpandedKeysChange: (keys: string[]) => void;
    onRefresh: () => void;
    onSelectNode: (node: MaterialCatalogNode) => void;
}

const iconByType: Record<MaterialCatalogNodeType, ReactNode> = {
    all: <FolderOutlined />,
    category: <FolderOutlined />,
    contentType: <FolderOutlined />,
    volume: <BookOutlined />
};

const flattenNodes = (nodes: MaterialCatalogNode[]): MaterialCatalogNode[] => {
    return nodes.flatMap((node) => [node, ...flattenNodes(node.children || [])]);
};

const toTreeData = (nodes: MaterialCatalogNode[]): DataNode[] =>
    nodes.map((node) => ({
        children: node.children ? toTreeData(node.children) : undefined,
        key: node.key,
        title: (
            <span className="graph-material-catalog-tree-title">
                <span className="graph-material-catalog-tree-icon" aria-hidden>
                    {iconByType[node.nodeType]}
                </span>
                <Text className="graph-material-catalog-tree-text">{node.title}</Text>
            </span>
        )
    }));

export const MaterialCatalogPanel = ({
    expandedKeys,
    isLoading = false,
    isRefreshing = false,
    nodes,
    selectedKey,
    onExpandedKeysChange,
    onRefresh,
    onSelectNode
}: MaterialCatalogPanelProps) => {
    const nodeByKey = useMemo(() => {
        return new Map(flattenNodes(nodes).map((node) => [node.key, node]));
    }, [nodes]);
    const treeData = useMemo(() => toTreeData(nodes), [nodes]);
    let treeContent = <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无素材目录" />;
    if (isLoading) {
        treeContent = <Skeleton active paragraph={{ rows: 10 }} />;
    } else if (nodes.length) {
        treeContent = (
            <Tree
                blockNode
                className="graph-material-catalog-tree"
                expandedKeys={expandedKeys}
                selectedKeys={[selectedKey]}
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
        <div className="graph-material-catalog-tree-panel" aria-label="图谱素材目录树">
            <div className="graph-material-catalog-panel-head">
                <KuzhambuSpace size={8}>
                    <FolderOutlined className="graph-material-catalog-panel-title-icon" />
                    <Text strong>素材目录</Text>
                </KuzhambuSpace>
                <KuzhambuButton
                    testId="knowledge-graph-material-catalog-refresh-button"
                    ariaLabel="刷新图谱素材目录"
                    className="graph-material-catalog-refresh"
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
