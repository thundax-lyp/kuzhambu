import { KuzhambuModal } from "@/components";
import type { RefinementEntityRecord } from "../refinement-types";

interface RefinementEntityDeleteModalProps {
    open: boolean;
    deleting?: boolean;
    entity?: RefinementEntityRecord | null;
    onCancel: () => void;
    onConfirm: () => void;
}

export const RefinementEntityDeleteModal = ({
    open,
    deleting = false,
    entity,
    onCancel,
    onConfirm
}: RefinementEntityDeleteModalProps) => {
    return (
        <KuzhambuModal
            testId="knowledge-refinement-entity-delete-modal"
            title="删除实体草稿"
            open={open}
            confirmLoading={deleting}
            onCancel={onCancel}
            onOk={onConfirm}
        >
            确认删除实体草稿“{entity?.name || entity?.entityKey || "-"}”？
        </KuzhambuModal>
    );
};
