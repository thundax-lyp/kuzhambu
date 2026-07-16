import { KuzhambuModal } from "@/components/kuzhambu-modal";
import type { RefinementRelationRecord } from "../refinement-types";

interface RefinementRelationDeleteModalProps {
    open: boolean;
    deleting?: boolean;
    relation?: RefinementRelationRecord | null;
    onCancel: () => void;
    onConfirm: () => void;
}

export const RefinementRelationDeleteModal = ({
    open,
    deleting = false,
    relation,
    onCancel,
    onConfirm
}: RefinementRelationDeleteModalProps) => {
    return (
        <KuzhambuModal
            title="删除关系草稿"
            open={open}
            confirmLoading={deleting}
            onCancel={onCancel}
            onOk={onConfirm}
        >
            确认删除关系草稿“{relation?.relationType || relation?.relationKey || "-"}”？
        </KuzhambuModal>
    );
};
