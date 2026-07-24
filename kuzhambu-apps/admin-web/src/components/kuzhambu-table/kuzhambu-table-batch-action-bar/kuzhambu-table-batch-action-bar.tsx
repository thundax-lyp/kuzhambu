import type { ReactNode } from "react";
import { Typography } from "antd";

const { Text } = Typography;

export interface KuzhambuTableBatchActionBarProps {
    actions: ReactNode;
    className?: string;
    selectedCount: number;
}

// AI NOTE: This is a passive selected-count + batch-actions bar owned by KuzhambuTable.
// It must not decide which actions are allowed; pages own permissions, disabled states, and confirmations.
export const KuzhambuTableBatchActionBar = ({
    actions,
    className,
    selectedCount
}: KuzhambuTableBatchActionBarProps) => {
    const hasSelection = selectedCount > 0;

    return (
        <div
            className={[
                "kuzhambu-table-batch-action-bar",
                hasSelection ? "" : "kuzhambu-table-batch-action-bar-muted",
                className
            ]
                .filter(Boolean)
                .join(" ")}
        >
            <Text
                className="kuzhambu-table-batch-action-bar-count"
                type={hasSelection ? undefined : "secondary"}
            >
                已选择 {selectedCount} 项
            </Text>
            <div className="kuzhambu-table-batch-action-bar-actions">{actions}</div>
        </div>
    );
};
