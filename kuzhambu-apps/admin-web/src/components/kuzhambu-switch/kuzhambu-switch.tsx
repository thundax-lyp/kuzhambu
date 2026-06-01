import { Switch } from "antd";
import type { SwitchProps } from "antd";
import "./kuzhambu-switch.css";

export type KuzhambuSwitchProps = SwitchProps;

export const KuzhambuSwitch = ({ className, ...props }: KuzhambuSwitchProps) => {
    return (
        <Switch {...props} className={["kuzhambu-switch", className].filter(Boolean).join(" ")} />
    );
};
