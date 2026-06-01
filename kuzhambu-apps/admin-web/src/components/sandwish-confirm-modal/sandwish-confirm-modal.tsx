import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Modal, Typography } from "antd";
import type { ModalProps } from "antd";
import type { ReactNode } from "react";
import "./sandwish-confirm-modal.css";

const { Text } = Typography;

export type SandwishConfirmModalTone = "danger";

export interface SandwishConfirmModalProps extends Omit<ModalProps, "children"> {
    description?: ReactNode;
    message: ReactNode;
    tone?: SandwishConfirmModalTone;
}

export const SandwishConfirmModal = ({
    className,
    description,
    message,
    okButtonProps,
    okText = "确认",
    rootClassName,
    tone = "danger",
    ...modalProps
}: SandwishConfirmModalProps) => {
    return (
        <Modal
            {...modalProps}
            className={["sandwish-confirm-modal", `sandwish-confirm-modal-${tone}`, className]
                .filter(Boolean)
                .join(" ")}
            rootClassName={["sandwish-confirm-modal-root", rootClassName].filter(Boolean).join(" ")}
            okButtonProps={{
                danger: tone === "danger",
                ...okButtonProps
            }}
            okText={okText}
        >
            <div className="sandwish-confirm-modal-content">
                <ExclamationCircleOutlined className="sandwish-confirm-modal-icon" />
                <div>
                    <Text strong>{message}</Text>
                    {description ? <Text type="secondary">{description}</Text> : null}
                </div>
            </div>
        </Modal>
    );
};
