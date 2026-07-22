import { Drawer } from "antd";
import type { DrawerProps } from "antd";
import "./kuzhambu-drawer.css";

export type KuzhambuDrawerSize = "full" | "large" | "middle" | "small";

export interface KuzhambuDrawerProps extends Omit<DrawerProps, "data-testid" | "size" | "width"> {
    size?: KuzhambuDrawerSize;
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: This is the only page-level Drawer wrapper.
// Use it instead of importing Ant Design Drawer in pages so width tokens, footer layout, and testId exposure stay consistent.
// Do not add section-switching or task workflow behavior here; use higher-level wrappers for those patterns.
export const KuzhambuDrawer = ({
    className,
    footer,
    placement = "right",
    rootClassName,
    size = "small",
    testId,
    ...drawerProps
}: KuzhambuDrawerProps) => {
    const drawerSize = `var(--kuzhambu-drawer-${size}-width)`;
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return (
        <Drawer
            {...drawerProps}
            {...testIdProps}
            className={["kuzhambu-drawer", `kuzhambu-drawer-${size}`, className]
                .filter(Boolean)
                .join(" ")}
            footer={
                footer ? <div className="kuzhambu-drawer-footer-actions">{footer}</div> : footer
            }
            placement={placement}
            rootClassName={["kuzhambu-drawer-root", `kuzhambu-drawer-root-${size}`, rootClassName]
                .filter(Boolean)
                .join(" ")}
            size={drawerSize}
        />
    );
};
