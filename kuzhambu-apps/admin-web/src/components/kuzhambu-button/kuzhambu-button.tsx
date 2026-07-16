import { Button } from "antd";
import type { ButtonProps } from "antd";

export interface KuzhambuButtonProps extends Omit<
    ButtonProps,
    "aria-label" | "data-testid" | "name"
> {
    ariaLabel?: string;
    testId: string;
}

export const KuzhambuButton = ({ ariaLabel, testId, ...props }: KuzhambuButtonProps) => {
    return <Button {...props} aria-label={ariaLabel} data-testid={testId} />;
};
