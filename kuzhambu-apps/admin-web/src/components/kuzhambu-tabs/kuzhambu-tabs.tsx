import { Tabs } from "antd";
import type { TabsProps } from "antd";

export interface KuzhambuTabsProps extends Omit<TabsProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

export const KuzhambuTabs = ({ testId, ...tabsProps }: KuzhambuTabsProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return <Tabs {...tabsProps} {...testIdProps} />;
};
