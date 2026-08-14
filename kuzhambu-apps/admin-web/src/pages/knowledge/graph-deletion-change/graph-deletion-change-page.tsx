import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuPage,
    KuzhambuSpace
} from "@/components";
import { graphDeletionChangeMockData } from "./__mocks__/graph-mock-data";
import { DeletionDecisionPanel } from "./deletion-decision-panel";
import type {
    GraphDeletionChangeRecord,
    GraphDeletionDecision
} from "./graph-deletion-change-types";
import "./graph-deletion-change-page.css";

export const GraphDeletionChangePage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [isMockFailure, setIsMockFailure] = useState(false);
    const [isMockEmpty, setIsMockEmpty] = useState(false);
    const [selectedChange, setSelectedChange] = useState<GraphDeletionChangeRecord | null>(null);
    const [decision, setDecision] = useState<GraphDeletionDecision | null>(null);
    const changes = graphDeletionChangeMockData.changes as readonly GraphDeletionChangeRecord[];

    if (!canViewGraph)
        return (
            <KuzhambuPage
                className="graph-deletion-change-page"
                description="需要知识图谱查看权限。"
                title="图谱删除变更"
            >
                <KuzhambuAlert title="无权查看图谱删除变更" type="warning" showIcon />
            </KuzhambuPage>
        );
    return (
        <KuzhambuPage
            className="graph-deletion-change-page"
            description="查看删除来源对正式图谱的影响并选择关联处理方式。"
            title="图谱删除变更"
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuSpace>
                    <KuzhambuButton
                        testId="knowledge-graph-deletion-change-toggle-empty-button"
                        onClick={() => setIsMockEmpty((value) => !value)}
                    >
                        模拟空态
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-deletion-change-toggle-failure-button"
                        onClick={() => setIsMockFailure((value) => !value)}
                    >
                        模拟加载失败
                    </KuzhambuButton>
                </KuzhambuSpace>
                {isMockFailure ? (
                    <KuzhambuAlert
                        title={graphDeletionChangeMockData.failureMessage}
                        type="error"
                        showIcon
                    />
                ) : null}
                {!isMockFailure && isMockEmpty ? (
                    <KuzhambuAlert title="暂无删除变更" type="info" showIcon />
                ) : null}
                {!isMockFailure && !isMockEmpty
                    ? changes.map((change) => (
                          <KuzhambuCard key={change.id} title={change.materialTitle}>
                              <KuzhambuSpace>
                                  <span>节点 {change.affectedNodeCount}</span>
                                  <span>关系 {change.affectedRelationCount}</span>
                                  <KuzhambuButton
                                      testId={`knowledge-graph-deletion-change-view-${change.id}-button`}
                                      onClick={() => setSelectedChange(change)}
                                  >
                                      查看影响
                                  </KuzhambuButton>
                              </KuzhambuSpace>
                          </KuzhambuCard>
                      ))
                    : null}
                {selectedChange ? (
                    <DeletionDecisionPanel change={selectedChange} onDecision={setDecision} />
                ) : null}
                {decision ? (
                    <KuzhambuAlert title={`已选择 ${decision}`} type="success" showIcon />
                ) : null}
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
