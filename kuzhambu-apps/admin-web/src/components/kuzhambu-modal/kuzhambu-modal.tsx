import { Modal } from "antd";
import type { ModalProps } from "antd";

export interface KuzhambuModalProps extends Omit<ModalProps, "data-testid"> {
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: This is the thin modal policy wrapper.
// Use it instead of importing Ant Design Modal in pages so testId exposure and class naming stay consistent.
// Do not add workflow-specific state here; use a specialized component such as KuzhambuSyncTaskModal.
export const KuzhambuModal = ({
    className,
    rootClassName,
    testId,
    ...modalProps
}: KuzhambuModalProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return (
        <Modal
            {...modalProps}
            {...testIdProps}
            className={["kuzhambu-modal", className].filter(Boolean).join(" ")}
            rootClassName={["kuzhambu-modal-root", rootClassName].filter(Boolean).join(" ")}
        />
    );
};
