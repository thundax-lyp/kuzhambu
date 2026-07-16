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

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

export const KuzhambuButton = forwardRef<
    HTMLAnchorElement | HTMLButtonElement,
    KuzhambuButtonProps
>(({ ariaLabel, testId, ...props }, ref) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return <Button {...props} {...testIdProps} ref={ref} aria-label={ariaLabel} />;
});

KuzhambuButton.displayName = "KuzhambuButton";
