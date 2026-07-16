import { Modal } from "antd";
import type { ModalProps } from "antd";

export type KuzhambuModalProps = ModalProps;

export const KuzhambuModal = ({ className, rootClassName, ...modalProps }: KuzhambuModalProps) => {
    return (
        <Modal
            {...modalProps}
            className={["kuzhambu-modal", className].filter(Boolean).join(" ")}
            rootClassName={["kuzhambu-modal-root", rootClassName].filter(Boolean).join(" ")}
        />
    );
};
