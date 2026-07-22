import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Modal, Typography } from "antd";
import type { ModalProps } from "antd";
import type { ReactNode } from "react";
import "./kuzhambu-confirm-modal.css";

const { Text } = Typography;

export type KuzhambuConfirmModalTone = "danger";

export interface KuzhambuConfirmModalProps extends Omit<ModalProps, "children"> {
    description?: ReactNode;
    message: ReactNode;
    tone?: KuzhambuConfirmModalTone;
}

// AI NOTE: This is the visual body for explicit confirmation dialogs.
// Pages should usually call useKuzhambuConfirm instead of composing this directly.
// Keep destructive-action semantics in the caller; this component only renders the confirmation shell.
export const KuzhambuConfirmModal = ({
    className,
    description,
    message,
    okButtonProps,
    okText = "确认",
    rootClassName,
    tone = "danger",
    ...modalProps
}: KuzhambuConfirmModalProps) => {
    return (
        <Modal
            {...modalProps}
            className={["kuzhambu-confirm-modal", `kuzhambu-confirm-modal-${tone}`, className]
                .filter(Boolean)
                .join(" ")}
            rootClassName={["kuzhambu-confirm-modal-root", rootClassName].filter(Boolean).join(" ")}
            okButtonProps={{
                danger: tone === "danger",
                ...okButtonProps
            }}
            okText={okText}
        >
            <div className="kuzhambu-confirm-modal-content">
                <ExclamationCircleOutlined className="kuzhambu-confirm-modal-icon" />
                <div>
                    <Text strong>{message}</Text>
                    {description ? <Text type="secondary">{description}</Text> : null}
                </div>
            </div>
        </Modal>
    );
};
