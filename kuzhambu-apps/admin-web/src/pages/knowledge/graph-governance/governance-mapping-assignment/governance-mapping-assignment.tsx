import { KuzhambuButton, KuzhambuSpace } from "@/components";

interface GovernanceMappingAssignmentProps {
    onAssign: () => void;
}

export const GovernanceMappingAssignment = ({ onAssign }: GovernanceMappingAssignmentProps) => (
    <KuzhambuSpace orientation="vertical">
        <strong>映射分配</strong>
        <span>将受影响来源映射到目标节点：</span>
        <KuzhambuButton
            testId="knowledge-graph-governance-assign-mapping-button"
            onClick={onAssign}
        >
            分配到李白
        </KuzhambuButton>
    </KuzhambuSpace>
);
