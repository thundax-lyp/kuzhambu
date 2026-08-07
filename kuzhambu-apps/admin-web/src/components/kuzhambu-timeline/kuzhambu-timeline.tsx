import { Timeline } from "antd";
import type { TimelineProps } from "antd";

import "./kuzhambu-timeline.css";

export interface KuzhambuTimelineProps extends Omit<TimelineProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

export const KuzhambuTimeline = ({
    testId,
    className,
    ...timelineProps
}: KuzhambuTimelineProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};
    const timelineClassName = ["kuzhambu-timeline", className].filter(Boolean).join(" ");

    return <Timeline {...timelineProps} {...testIdProps} className={timelineClassName} />;
};
