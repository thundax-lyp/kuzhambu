import { Card as AntdCard } from "antd";
import type { CardProps } from "antd";

export type KuzhambuCardProps = Omit<CardProps, "bodyStyle" | "bordered" | "headStyle"> & {
    bodyStyle?: never;
};

// AI NOTE: Card body styling must use Ant Design v6 styles.body through this wrapper.
// Page code uses KuzhambuCard so deprecated bodyStyle cannot leak back into forms and panels.
export const KuzhambuCard = (props: KuzhambuCardProps) => {
    return <AntdCard {...props} />;
};
