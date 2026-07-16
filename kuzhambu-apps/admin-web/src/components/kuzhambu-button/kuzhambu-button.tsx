import { Button } from "antd";
import type { ButtonProps } from "antd";

export interface KuzhambuButtonProps extends Omit<ButtonProps, "aria-label" | "name"> {
    name: string;
}

export const KuzhambuButton = ({ name, ...props }: KuzhambuButtonProps) => {
    return <Button {...props} aria-label={name} />;
};
