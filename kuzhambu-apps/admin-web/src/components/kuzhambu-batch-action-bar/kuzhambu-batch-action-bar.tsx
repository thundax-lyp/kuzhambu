import type { ReactNode } from "react";
import { Typography } from "antd";
import "./kuzhambu-batch-action-bar.css";

const { Text } = Typography;

export interface KuzhambuBatchActionBarProps {
    actions: ReactNode;
    className?: string;
    selectedCount: number;
}

// AI NOTE: This is a passive selected-count + batch-actions bar.
// It must not decide which actions are allowed; pages own permissions, disabled states, and confirmations.
export const KuzhambuBatchActionBar = ({
    actions,
    className,
    selectedCount
}: KuzhambuBatchActionBarProps) => {
    const hasSelection = selectedCount > 0;

    return (
        <div
            className={[
                "kuzhambu-batch-action-bar",
                hasSelection ? "" : "kuzhambu-batch-action-bar-muted",
                className
            ]
                .filter(Boolean)
                .join(" ")}
        >
            <Text
                className="kuzhambu-batch-action-bar-count"
                type={hasSelection ? undefined : "secondary"}
            >
                已选择 {selectedCount} 项
            </Text>
            <div className="kuzhambu-batch-action-bar-actions">{actions}</div>
        </div>
    );
};
