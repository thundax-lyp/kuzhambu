import { Steps } from "antd";
import type { StepsProps } from "antd";

export interface KuzhambuStepsProps extends Omit<StepsProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: Thin wrapper for Ant Design Steps test anchors and naming consistency.
// Business step state, navigation, and workflow ownership stay in the caller.
export const KuzhambuSteps = ({ className, testId, ...stepsProps }: KuzhambuStepsProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return (
        <Steps
            {...stepsProps}
            {...testIdProps}
            className={["kuzhambu-steps", className].filter(Boolean).join(" ")}
        />
    );
};
