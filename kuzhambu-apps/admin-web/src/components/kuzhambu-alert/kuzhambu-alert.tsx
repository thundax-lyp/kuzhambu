import { Alert } from "antd";
import type { AlertProps } from "antd";

export interface KuzhambuAlertProps extends Omit<AlertProps, "message"> {
    title?: AlertProps["title"];
}

export const KuzhambuAlert = ({ title, ...props }: KuzhambuAlertProps) => {
    return <Alert {...props} title={title} />;
};
