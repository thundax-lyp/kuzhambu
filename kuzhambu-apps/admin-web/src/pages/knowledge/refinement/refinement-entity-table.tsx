import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { RefinementEntityRecord } from "./refinement-types";

interface RefinementEntityTableProps {
    canEdit?: boolean;
    entities: RefinementEntityRecord[];
    onAdd: () => void;
    onAnnotate: (entity: RefinementEntityRecord) => void;
    onConfirm: (entity: RefinementEntityRecord) => void;
    onDelete: (entity: RefinementEntityRecord) => void;
    onEdit: (entity: RefinementEntityRecord) => void;
}

const readStatusColor = (status?: string | null) =>
    status === "MANUAL_CONFIRMED" ? "green" : "blue";

export const RefinementEntityTable = ({
    canEdit = false,
    entities,
    onAdd,
    onAnnotate,
    onConfirm,
    onDelete,
    onEdit
}: RefinementEntityTableProps) => {
    const columns: ColumnsType<RefinementEntityRecord> = [
        { title: "名称", dataIndex: "name", key: "name" },
        { title: "类型", dataIndex: "entityType", key: "entityType" },
        {
            title: "确认状态",
            dataIndex: "confirmationStatus",
            key: "confirmationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        { title: "操作类型", dataIndex: "operationType", key: "operationType" },
        {
            title: "操作",
            key: "actions",
            render: (_, entity) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-entity-edit-button"
                        disabled={!canEdit}
                        onClick={() => onEdit(entity)}
                    >
                        编辑
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-entity-action-button"
                        disabled={!canEdit || entity.confirmationStatus === "MANUAL_CONFIRMED"}
                        onClick={() => onConfirm(entity)}
                    >
                        确认
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-entity-annotate-button"
                        disabled={!canEdit}
                        onClick={() => onAnnotate(entity)}
                    >
                        标注
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-entity-delete-button"
                        danger
                        disabled={!canEdit}
                        onClick={() => onDelete(entity)}
                    >
                        删除
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <>
            <div className="knowledge-refinement-section-actions">
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-entity-create-entity-button"
                    disabled={!canEdit}
                    type="primary"
                    onClick={onAdd}
                >
                    新增实体
                </KuzhambuButton>
            </div>
            <Table<RefinementEntityRecord>
                aria-label="知识图谱精修实体表格"
                columns={columns}
                dataSource={entities}
                pagination={false}
                rowKey={(entity) =>
                    normalizeId(entity.draftId || entity.entityKey || entity.name || "entity")
                }
            />
        </>
    );
};
