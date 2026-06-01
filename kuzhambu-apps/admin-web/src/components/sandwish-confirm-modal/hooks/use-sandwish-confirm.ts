import { ExclamationCircleOutlined } from "@ant-design/icons";
import { App, Modal, Typography } from "antd";
import type { ModalFuncProps } from "antd";
import type { ReactNode } from "react";
import { createElement } from "react";

const { Text } = Typography;

interface SandwishConfirmOptions {
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
        { className: "sandwish-confirm-modal-content" },
        createElement(ExclamationCircleOutlined, {
            className: "sandwish-confirm-modal-icon"
        }),
        createElement(
            "div",
            null,
            createElement(Text, { strong: true }, message),
            description ? createElement(Text, { type: "secondary" }, description) : null
        )
    );
};

export const useSandwishConfirm = () => {
    const { modal } = App.useApp();

    const danger = ({
        cancelText = "取消",
        description,
        message,
        okText = "确认",
        onConfirm,
        title
    }: SandwishConfirmOptions) => {
        const options: ModalFuncProps = {
            title,
            icon: null,
            content: renderContent(message, description),
            okText,
            cancelText,
            className: "sandwish-confirm-modal sandwish-confirm-modal-danger",
            rootClassName: "sandwish-confirm-modal-root",
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
