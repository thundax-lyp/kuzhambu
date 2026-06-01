import type { ReactNode } from "react";
import { Typography } from "antd";
import "./sandwish-batch-action-bar.css";

const { Text } = Typography;

export interface SandwishBatchActionBarProps {
    actions: ReactNode;
    className?: string;
    selectedCount: number;
}

export const SandwishBatchActionBar = ({
    actions,
    className,
    selectedCount
}: SandwishBatchActionBarProps) => {
    const hasSelection = selectedCount > 0;

    return (
        <div
            className={[
                "sandwish-batch-action-bar",
                hasSelection ? "" : "sandwish-batch-action-bar-muted",
                className
            ]
                .filter(Boolean)
                .join(" ")}
        >
            <Text
                className="sandwish-batch-action-bar-count"
                type={hasSelection ? undefined : "secondary"}
            >
                已选择 {selectedCount} 项
            </Text>
            <div className="sandwish-batch-action-bar-actions">{actions}</div>
        </div>
    );
};
