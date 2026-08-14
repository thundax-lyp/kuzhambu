import { KuzhambuAlert, KuzhambuButton, KuzhambuCard, KuzhambuSpace } from "@/components";
import type {
    GraphDeletionChangeRecord,
    GraphDeletionDecision
} from "../graph-deletion-change-types";

interface DeletionDecisionPanelProps {
    change: GraphDeletionChangeRecord;
    onDecision: (decision: GraphDeletionDecision) => void;
}

export const DeletionDecisionPanel = ({ change, onDecision }: DeletionDecisionPanelProps) => (
    <KuzhambuCard title={`${change.materialTitle} 删除影响`}>
        <KuzhambuSpace orientation="vertical" size={10}>
            <span>将影响节点：{change.affectedNodeCount}</span>
            <span>将影响关系：{change.affectedRelationCount}</span>
            <KuzhambuAlert title="以下操作不可逆，请确认影响后选择。" type="warning" showIcon />
            <KuzhambuSpace>
                <KuzhambuButton
                    testId="knowledge-graph-deletion-preserve-contribution-button"
                    onClick={() => onDecision("PRESERVE_CONTRIBUTION")}
                >
                    保留贡献
                </KuzhambuButton>
                <KuzhambuButton
                    danger
                    testId="knowledge-graph-deletion-withdraw-associations-button"
                    onClick={() => onDecision("WITHDRAW_ASSOCIATIONS")}
                >
                    撤回关联
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuSpace>
    </KuzhambuCard>
);
