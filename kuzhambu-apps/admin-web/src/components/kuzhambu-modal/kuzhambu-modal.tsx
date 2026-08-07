import { Modal } from "antd";
import type { ModalProps } from "antd";

const joinClassNames = (...classNames: Array<string | false | null | undefined>) => {
    return classNames.filter(Boolean).join(" ");
};

export interface KuzhambuModalProps extends Omit<
    ModalProps,
    | "autoFocusButton"
    | "bodyStyle"
    | "data-testid"
    | "destroyOnClose"
    | "focusTriggerAfterClose"
    | "maskClosable"
    | "maskStyle"
> {
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
    classNames,
    rootClassName,
    testId,
    ...modalProps
}: KuzhambuModalProps) => {
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return (
        <Modal
            {...modalProps}
            {...testIdProps}
            className={joinClassNames("kuzhambu-modal", className)}
            classNames={classNames}
            rootClassName={joinClassNames("kuzhambu-modal-root", rootClassName)}
        />
    );
};
