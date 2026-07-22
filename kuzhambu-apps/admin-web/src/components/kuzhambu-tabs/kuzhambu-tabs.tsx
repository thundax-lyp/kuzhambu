import { Tabs } from "antd";
import type { TabsProps } from "antd";

export interface KuzhambuTabsProps extends Omit<TabsProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: This is the tabs wrapper for consistent testId exposure.
// It does not own routing, permissions, or tab state; pages provide those decisions.
export const KuzhambuTabs = ({ testId, ...tabsProps }: KuzhambuTabsProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return <Tabs {...tabsProps} {...testIdProps} />;
};
