import { Space } from "antd";
import type { SpaceProps } from "antd";
import type { ComponentProps } from "react";

export type KuzhambuSpaceProps = Omit<SpaceProps, "direction" | "split">;
export type KuzhambuSpaceCompactProps = Omit<ComponentProps<typeof Space.Compact>, "direction">;

// AI NOTE: These spacing wrappers are the page-approved replacement for direct Ant Design Space imports.
// They own no business behavior; use them only to keep page spacing conventions consistent.
export const KuzhambuSpace = ({ ...props }: KuzhambuSpaceProps) => {
    return <Space {...props} />;
};

export const KuzhambuSpaceCompact = ({ ...props }: KuzhambuSpaceCompactProps) => {
    return <Space.Compact {...props} />;
};
