import { forwardRef } from "react";
import { Button } from "antd";
import type { ButtonProps } from "antd";

export interface KuzhambuButtonProps extends Omit<
    ButtonProps,
    "aria-label" | "data-testid" | "iconPosition" | "name"
> {
    ariaLabel?: string;
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: This is the button wrapper that centralizes testId exposure and ariaLabel conversion.
// Use it in page code instead of Ant Design Button; keep button text and domain action semantics in the caller.
export const KuzhambuButton = forwardRef<
    HTMLAnchorElement | HTMLButtonElement,
    KuzhambuButtonProps
>(({ ariaLabel, testId, ...props }, ref) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return <Button {...props} {...testIdProps} ref={ref} aria-label={ariaLabel} />;
});

KuzhambuButton.displayName = "KuzhambuButton";
