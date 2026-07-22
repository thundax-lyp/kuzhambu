import { Alert } from "antd";
import type { AlertProps } from "antd";

export interface KuzhambuAlertProps extends Omit<AlertProps, "message"> {
    title?: AlertProps["title"];
}

// AI NOTE: This is the alert wrapper that standardizes title/message naming for pages.
// Keep domain status interpretation in the caller or a workflow component; this wrapper only maps props to Ant Design Alert.
export const KuzhambuAlert = ({ title, ...props }: KuzhambuAlertProps) => {
    return <Alert {...props} title={title} />;
};
