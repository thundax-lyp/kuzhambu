import { Sender } from "@ant-design/x";
import type { SenderProps } from "@ant-design/x";

import "./kuzhambu-x-chat.css";

export interface KuzhambuXSenderProps extends Omit<SenderProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

export const KuzhambuXSender = ({ testId, className, ...senderProps }: KuzhambuXSenderProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};
    const senderClassName = ["kuzhambu-x-sender", className].filter(Boolean).join(" ");

    return <Sender {...senderProps} {...testIdProps} className={senderClassName} />;
};
