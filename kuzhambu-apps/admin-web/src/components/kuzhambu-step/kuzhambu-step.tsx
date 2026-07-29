import { Steps } from "antd";
import type { StepsProps } from "antd";

export interface KuzhambuStepProps extends Omit<StepsProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: Thin wrapper for Ant Design Steps test anchors and naming consistency.
// Business step state, navigation, and workflow ownership stay in the caller.
export const KuzhambuStep = ({ className, testId, ...stepsProps }: KuzhambuStepProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return (
        <Steps
            {...stepsProps}
            {...testIdProps}
            className={["kuzhambu-step", className].filter(Boolean).join(" ")}
        />
    );
};
