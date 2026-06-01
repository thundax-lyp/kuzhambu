import { Drawer } from "antd";
import type { DrawerProps } from "antd";
import "./sandwish-drawer.css";

export type SandwishDrawerSize = "full" | "large" | "middle" | "small";

export interface SandwishDrawerProps extends Omit<DrawerProps, "size" | "width"> {
    size?: SandwishDrawerSize;
}

export const SandwishDrawer = ({
    className,
    placement = "right",
    rootClassName,
    size = "small",
    ...drawerProps
}: SandwishDrawerProps) => {
    const drawerSize = `var(--sandwish-drawer-${size}-width)`;

    return (
        <Drawer
            {...drawerProps}
            className={["sandwish-drawer", `sandwish-drawer-${size}`, className]
                .filter(Boolean)
                .join(" ")}
            placement={placement}
            rootClassName={["sandwish-drawer-root", `sandwish-drawer-root-${size}`, rootClassName]
                .filter(Boolean)
                .join(" ")}
            size={drawerSize}
        />
    );
};
