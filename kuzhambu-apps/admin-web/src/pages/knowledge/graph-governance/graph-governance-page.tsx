import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuGraph,
    KuzhambuPage,
    KuzhambuSpace
} from "@/components";
import { graphGovernanceMockData } from "./__mocks__/graph-mock-data";
import { GovernanceDetailDrawer } from "./governance-detail-drawer";
import { GovernanceTable } from "./governance-table";
import type {
    GraphGovernanceNodeRecord,
    GraphGovernanceRelationRecord
} from "./graph-governance-types";

export const GraphGovernancePage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [isMockFailure, setIsMockFailure] = useState(false);
    const [isMockEmpty, setIsMockEmpty] = useState(false);
    const [selectedNode, setSelectedNode] = useState<GraphGovernanceNodeRecord | null>(null);
    const [selectedRelation, setSelectedRelation] = useState<GraphGovernanceRelationRecord | null>(
        null
    );
    const nodes = graphGovernanceMockData.nodes as readonly GraphGovernanceNodeRecord[];
    const relations = graphGovernanceMockData.relations as readonly GraphGovernanceRelationRecord[];
    const graphItems = useMemo(() => {
        if (selectedRelation) {
            const source = nodes.find((node) => node.id === selectedRelation.sourceId);
            const target = nodes.find((node) => node.id === selectedRelation.targetId);
            return source && target
                ? [{ subject: source.name, predicate: selectedRelation.type, object: target.name }]
                : [];
        }
        return selectedNode
            ? [{ subject: selectedNode.name, predicate: "来源", object: "治理详情" }]
            : [];
    }, [nodes, selectedNode, selectedRelation]);

    if (!canViewGraph) {
        return (
            <KuzhambuPage description="需要知识图谱查看权限。" title="图谱治理">
                <KuzhambuAlert title="无权查看图谱治理" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage description="浏览正式节点、关系、来源与审计信息。" title="图谱治理">
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuSpace>
                    <KuzhambuButton
                        testId="knowledge-graph-governance-toggle-empty-button"
                        onClick={() => setIsMockEmpty((value) => !value)}
                    >
                        模拟空态
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-governance-toggle-failure-button"
                        onClick={() => setIsMockFailure((value) => !value)}
                    >
                        模拟加载失败
                    </KuzhambuButton>
                </KuzhambuSpace>
                {isMockFailure ? (
                    <KuzhambuAlert
                        title={graphGovernanceMockData.failureMessage}
                        type="error"
                        showIcon
                    />
                ) : null}
                {!isMockFailure && isMockEmpty ? (
                    <KuzhambuAlert title="暂无治理节点或关系" type="info" showIcon />
                ) : null}
                {!isMockFailure && !isMockEmpty ? (
                    <>
                        <KuzhambuCard title="节点与关系">
                            <GovernanceTable
                                nodes={nodes}
                                relations={relations}
                                onSelectNode={(node) => {
                                    setSelectedNode(node);
                                    setSelectedRelation(null);
                                }}
                                onSelectRelation={(relation) => {
                                    setSelectedRelation(relation);
                                    setSelectedNode(null);
                                }}
                            />
                        </KuzhambuCard>
                        <KuzhambuCard title="局部画布">
                            <KuzhambuGraph height={320} spoList={graphItems} />
                            <span>当前仅浏览，不提供高风险写操作。</span>
                        </KuzhambuCard>
                    </>
                ) : null}
            </KuzhambuSpace>
            <GovernanceDetailDrawer
                node={selectedNode}
                relation={selectedRelation}
                open={selectedNode !== null || selectedRelation !== null}
                onClose={() => {
                    setSelectedNode(null);
                    setSelectedRelation(null);
                }}
            />
        </KuzhambuPage>
    );
};
