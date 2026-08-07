import { Segmented } from "antd";
import type { SegmentedProps } from "antd";

import "./kuzhambu-segmented.css";

export interface KuzhambuSegmentedProps<
    TValue extends string | number = string | number
> extends Omit<SegmentedProps<TValue>, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

export const KuzhambuSegmented = <TValue extends string | number = string | number>({
    testId,
    className,
    ...segmentedProps
}: KuzhambuSegmentedProps<TValue>) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};
    const segmentedClassName = ["kuzhambu-segmented", className].filter(Boolean).join(" ");

    return (
        <Segmented<TValue> {...segmentedProps} {...testIdProps} className={segmentedClassName} />
    );
};
