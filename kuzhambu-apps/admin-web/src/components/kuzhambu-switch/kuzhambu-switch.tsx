import { Switch } from "antd";
import type { SwitchProps } from "antd";
import "./kuzhambu-switch.css";

export type KuzhambuSwitchProps = SwitchProps;

// AI NOTE: This switch wrapper exists only to attach shared switch styling.
// Keep permission checks, confirmation prompts, and mutation behavior in the page.
export const KuzhambuSwitch = ({ className, ...props }: KuzhambuSwitchProps) => {
    return (
        <Switch {...props} className={["kuzhambu-switch", className].filter(Boolean).join(" ")} />
    );
};
