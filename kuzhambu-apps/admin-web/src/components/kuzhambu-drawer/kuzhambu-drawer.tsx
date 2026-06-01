import { Drawer } from "antd";
import type { DrawerProps } from "antd";
import "./kuzhambu-drawer.css";

export type KuzhambuDrawerSize = "full" | "large" | "middle" | "small";

export interface KuzhambuDrawerProps extends Omit<DrawerProps, "size" | "width"> {
    size?: KuzhambuDrawerSize;
}

export const KuzhambuDrawer = ({
    className,
    placement = "right",
    rootClassName,
    size = "small",
    ...drawerProps
}: KuzhambuDrawerProps) => {
    const drawerSize = `var(--kuzhambu-drawer-${size}-width)`;

    return (
        <Drawer
            {...drawerProps}
            className={["kuzhambu-drawer", `kuzhambu-drawer-${size}`, className]
                .filter(Boolean)
                .join(" ")}
            placement={placement}
            rootClassName={["kuzhambu-drawer-root", `kuzhambu-drawer-root-${size}`, rootClassName]
                .filter(Boolean)
                .join(" ")}
            size={drawerSize}
        />
    );
};
