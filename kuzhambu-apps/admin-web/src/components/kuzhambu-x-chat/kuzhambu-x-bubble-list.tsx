import { Bubble } from "@ant-design/x";
import type { BubbleListProps } from "@ant-design/x";

import "./kuzhambu-x-chat.css";

export interface KuzhambuXBubbleListProps extends Omit<BubbleListProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

export const KuzhambuXBubbleList = ({
    testId,
    className,
    ...bubbleListProps
}: KuzhambuXBubbleListProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};
    const bubbleListClassName = ["kuzhambu-x-bubble-list", className].filter(Boolean).join(" ");

    return <Bubble.List {...bubbleListProps} {...testIdProps} className={bubbleListClassName} />;
};
