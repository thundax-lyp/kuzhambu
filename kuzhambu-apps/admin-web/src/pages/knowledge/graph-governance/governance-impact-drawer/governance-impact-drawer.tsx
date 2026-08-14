import { useState } from "react";
import { KuzhambuAlert, KuzhambuButton, KuzhambuDrawer, KuzhambuSpace } from "@/components";
import { GovernanceMappingAssignment } from "../governance-mapping-assignment";

export type GovernanceAction = "CREATE" | "EDIT" | "DELETE" | "MERGE" | "SPLIT";

interface GovernanceImpactDrawerProps {
    action: GovernanceAction | null;
    onApply: (action: GovernanceAction) => void;
    onClose: () => void;
}

const ACTION_LABELS: Record<GovernanceAction, string> = {
    CREATE: "创建",
    EDIT: "编辑",
    DELETE: "删除",
    MERGE: "合并",
    SPLIT: "拆分"
};

export const GovernanceImpactDrawer = ({
    action,
    onApply,
    onClose
}: GovernanceImpactDrawerProps) => {
    const [isMapped, setIsMapped] = useState(false);
    const isOpen = action !== null;
    const requiresMapping = action === "MERGE" || action === "SPLIT";
    const actionLabel = action ? ACTION_LABELS[action] : "";
    return (
        <KuzhambuDrawer
            open={isOpen}
            onClose={onClose}
            title={`${actionLabel}影响预览`}
            size="middle"
            testId="knowledge-graph-governance-impact-drawer"
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <KuzhambuAlert title="确认前请检查以下影响" type="warning" showIcon />
                <span>受影响节点：2</span>
                <span>受影响边：3</span>
                <span>受影响映射：1</span>
                <span>关联 issue：1</span>
                {requiresMapping ? (
                    <GovernanceMappingAssignment onAssign={() => setIsMapped(true)} />
                ) : null}
                {isMapped ? <KuzhambuAlert title="映射已分配" type="success" showIcon /> : null}
                <KuzhambuButton
                    danger
                    disabled={requiresMapping && !isMapped}
                    testId="knowledge-graph-governance-confirm-impact-button"
                    onClick={() => action && onApply(action)}
                >
                    确认并应用{actionLabel}
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuDrawer>
    );
};
