import { Input } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuPage,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";
import { WorkbenchCanvas } from "./workbench-canvas";
import { WorkbenchDetailDrawer } from "./workbench-detail-drawer";

import { graphWorkbenchMockData } from "./__mocks__/graph-mock-data";
import type {
    GraphWorkbenchCategoryRecord,
    GraphWorkbenchMetricRecord
} from "./graph-workbench-types";

const CATEGORY_OPTIONS: GraphWorkbenchCategoryRecord[] = [
    { code: "all", name: "全部门类" },
    { code: "person", name: "人物" },
    { code: "work", name: "作品" }
];

const createMetrics = (): GraphWorkbenchMetricRecord[] => [
    { key: "nodes", label: "节点", value: graphWorkbenchMockData.metrics.nodeCount },
    { key: "relations", label: "关系", value: graphWorkbenchMockData.metrics.relationCount },
    {
        key: "materials",
        label: "覆盖素材",
        value: graphWorkbenchMockData.metrics.coveredMaterialCount
    },
    { key: "orphans", label: "孤立节点", value: graphWorkbenchMockData.metrics.orphanNodeCount },
    {
        key: "missingCoreRelations",
        label: "核心关系缺失",
        value: graphWorkbenchMockData.metrics.missingCoreRelationCount
    }
];

export const GraphWorkbenchPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [searchText, setSearchText] = useState("");
    const [categoryCode, setCategoryCode] = useState("all");
    const [isMockFailure, setIsMockFailure] = useState(false);
    const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
    const metrics = useMemo(createMetrics, []);
    const visibleSeeds = useMemo(
        () =>
            graphWorkbenchMockData.seedNodes.filter((node) =>
                node.label
                    .toLocaleLowerCase("zh-CN")
                    .includes(searchText.toLocaleLowerCase("zh-CN"))
            ),
        [searchText]
    );
    const selectedNode = graphWorkbenchMockData.seedNodes.find(
        (node) => node.id === selectedNodeId
    );

    if (!canViewGraph) {
        return (
            <KuzhambuPage description="需要知识图谱查看权限。" title="图谱工作台">
                <KuzhambuAlert title="无权查看图谱工作台" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage description="从近期发布种子开始，逐步浏览局部知识图谱。" title="图谱工作台">
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuSpace wrap>
                    {metrics.map((metric) => (
                        <KuzhambuCard key={metric.key} size="small" title={metric.label}>
                            <strong>{metric.value}</strong>
                        </KuzhambuCard>
                    ))}
                </KuzhambuSpace>
                <KuzhambuSpace wrap>
                    <Input
                        aria-label="搜索图谱种子"
                        placeholder="搜索节点或关系"
                        value={searchText}
                        onChange={(event) => setSearchText(event.target.value)}
                    />
                    <KuzhambuSelect
                        aria-label="筛选图谱门类"
                        options={CATEGORY_OPTIONS.map((category) => ({
                            label: category.name,
                            value: category.code
                        }))}
                        value={categoryCode}
                        onChange={setCategoryCode}
                    />
                    <KuzhambuButton
                        testId="knowledge-graph-workbench-toggle-mock-failure-button"
                        onClick={() => setIsMockFailure((value) => !value)}
                    >
                        {isMockFailure ? "恢复模拟数据" : "模拟加载失败"}
                    </KuzhambuButton>
                </KuzhambuSpace>
                {isMockFailure ? (
                    <KuzhambuAlert title="工作台 Mock 数据加载失败" type="error" showIcon />
                ) : null}
                {!isMockFailure && visibleSeeds.length === 0 ? (
                    <KuzhambuAlert title="没有匹配的图谱种子" type="info" showIcon />
                ) : null}
                {!isMockFailure && visibleSeeds.length > 0 ? (
                    <WorkbenchCanvas
                        edgeBatches={graphWorkbenchMockData.edgeBatches}
                        seedNodes={visibleSeeds}
                        onSelectNode={(node) => setSelectedNodeId(node.id)}
                    />
                ) : null}
            </KuzhambuSpace>
            <WorkbenchDetailDrawer
                node={selectedNode}
                open={selectedNode !== undefined}
                onClose={() => setSelectedNodeId(null)}
            />
        </KuzhambuPage>
    );
};
