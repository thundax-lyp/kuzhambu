import { Space } from "antd";
import type { SpaceProps } from "antd";
import type { ComponentProps } from "react";

export type KuzhambuSpaceProps = Omit<SpaceProps, "direction">;
export type KuzhambuSpaceCompactProps = Omit<ComponentProps<typeof Space.Compact>, "direction">;

export const KuzhambuSpace = ({ ...props }: KuzhambuSpaceProps) => {
    return <Space {...props} />;
};

export const KuzhambuSpaceCompact = ({ ...props }: KuzhambuSpaceCompactProps) => {
    return <Space.Compact {...props} />;
};
