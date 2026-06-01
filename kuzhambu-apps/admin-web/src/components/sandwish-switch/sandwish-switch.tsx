import { Switch } from "antd";
import type { SwitchProps } from "antd";
import "./sandwish-switch.css";

export type SandwishSwitchProps = SwitchProps;

export const SandwishSwitch = ({ className, ...props }: SandwishSwitchProps) => {
    return (
        <Switch {...props} className={["sandwish-switch", className].filter(Boolean).join(" ")} />
    );
};
