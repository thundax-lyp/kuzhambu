import { ExclamationCircleOutlined } from "@ant-design/icons";
import { App, Modal, Typography } from "antd";
import type { ModalFuncProps } from "antd";
import type { ReactNode } from "react";
import { createElement } from "react";

const { Text } = Typography;

interface KuzhambuConfirmOptions {
    cancelText?: string;
    description?: ReactNode;
    message: ReactNode;
    okText?: string;
    onConfirm: () => Promise<unknown> | unknown;
    title: ReactNode;
}

const renderContent = (message: ReactNode, description?: ReactNode) => {
    return createElement(
        "div",
        { className: "kuzhambu-confirm-modal-content" },
        createElement(ExclamationCircleOutlined, {
            className: "kuzhambu-confirm-modal-icon"
        }),
        createElement(
            "div",
            null,
            createElement(Text, { strong: true }, message),
            description ? createElement(Text, { type: "secondary" }, description) : null
        )
    );
};

export const useKuzhambuConfirm = () => {
    const { modal } = App.useApp();

    const danger = ({
        cancelText = "取消",
        description,
        message,
        okText = "确认",
        onConfirm,
        title
    }: KuzhambuConfirmOptions) => {
        const options: ModalFuncProps = {
            title,
            icon: null,
            content: renderContent(message, description),
            okText,
            cancelText,
            className: "kuzhambu-confirm-modal kuzhambu-confirm-modal-danger",
            rootClassName: "kuzhambu-confirm-modal-root",
            okButtonProps: {
                danger: true
            },
            onOk: onConfirm
        };

        const openConfirm =
            "confirm" in modal && typeof modal.confirm === "function"
                ? modal.confirm
                : Modal.confirm;

        return openConfirm(options);
    };

    return { danger };
};
