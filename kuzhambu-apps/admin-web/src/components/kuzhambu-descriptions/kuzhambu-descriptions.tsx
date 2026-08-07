import { Descriptions } from "antd";
import type { DescriptionsProps } from "antd";
import "./kuzhambu-descriptions.css";

export interface KuzhambuDescriptionsProps extends Omit<
    DescriptionsProps,
    "children" | "contentStyle" | "labelStyle"
> {
    ariaLabel?: string;
    testId?: string;
    variant?: "default" | "detail" | "compare";
}

// AI NOTE: This is a thin Ant Design Descriptions wrapper for page-level
// label/value display. Keep domain-specific item construction in callers.
export const KuzhambuDescriptions = ({
    ariaLabel,
    className,
    testId,
    variant = "default",
    ...props
}: KuzhambuDescriptionsProps) => {
    return (
        <Descriptions
            {...props}
            aria-label={ariaLabel}
            className={["kuzhambu-descriptions", `kuzhambu-descriptions-${variant}`, className]
                .filter(Boolean)
                .join(" ")}
            data-testid={testId}
        />
    );
};
