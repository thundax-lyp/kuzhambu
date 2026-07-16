import { forwardRef } from "react";
import { Button } from "antd";
import type { ButtonProps } from "antd";

export interface KuzhambuButtonProps extends Omit<
    ButtonProps,
    "aria-label" | "data-testid" | "name"
> {
    ariaLabel?: string;
    testId: string;
}

export const KuzhambuButton = forwardRef<
    HTMLAnchorElement | HTMLButtonElement,
    KuzhambuButtonProps
>(({ ariaLabel, testId, ...props }, ref) => {
    return <Button {...props} ref={ref} aria-label={ariaLabel} data-testid={testId} />;
});

KuzhambuButton.displayName = "KuzhambuButton";
