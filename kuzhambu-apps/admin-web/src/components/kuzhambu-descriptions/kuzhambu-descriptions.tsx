import { Descriptions } from "antd";
import type { DescriptionsProps } from "antd";
import "./kuzhambu-descriptions.css";

export interface KuzhambuDescriptionsProps extends DescriptionsProps {
    ariaLabel?: string;
    testId?: string;
}

// AI NOTE: This is a thin Ant Design Descriptions wrapper for page-level
// label/value display. Keep domain-specific item construction in callers.
export const KuzhambuDescriptions = ({
    ariaLabel,
    className,
    testId,
    ...props
}: KuzhambuDescriptionsProps) => {
    return (
        <Descriptions
            {...props}
            aria-label={ariaLabel}
            className={["kuzhambu-descriptions", className].filter(Boolean).join(" ")}
            data-testid={testId}
        />
    );
};
