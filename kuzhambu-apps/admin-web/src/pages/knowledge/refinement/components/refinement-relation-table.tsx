import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import type { ColumnsType } from "antd/es/table";
import type { RefinementRelationRecord } from "../refinement-types";

interface RefinementRelationTableProps {
    canEdit?: boolean;
    onAdd: () => void;
    onAnnotate: (relation: RefinementRelationRecord) => void;
    onConfirm: (relation: RefinementRelationRecord) => void;
    onDelete: (relation: RefinementRelationRecord) => void;
    onEdit: (relation: RefinementRelationRecord) => void;
    relations: RefinementRelationRecord[];
}

const readStatusColor = (status?: string | null) =>
    status === "MANUAL_CONFIRMED" ? "green" : "blue";

export const RefinementRelationTable = ({
    canEdit = false,
    onAdd,
    onAnnotate,
    onConfirm,
    onDelete,
    onEdit,
    relations
}: RefinementRelationTableProps) => {
    const columns: ColumnsType<RefinementRelationRecord> = [
        { title: "源名称", dataIndex: "sourceName", key: "sourceName" },
        { title: "目标名称", dataIndex: "targetName", key: "targetName" },
        { title: "关系类型", dataIndex: "relationType", key: "relationType" },
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
            render: (_, relation) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-relation-edit-button"
                        disabled={!canEdit}
                        onClick={() => onEdit(relation)}
                    >
                        编辑
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-relation-action-button"
                        disabled={!canEdit || relation.confirmationStatus === "MANUAL_CONFIRMED"}
                        onClick={() => onConfirm(relation)}
                    >
                        确认
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-relation-annotate-button"
                        disabled={!canEdit}
                        onClick={() => onAnnotate(relation)}
                    >
                        标注
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-relation-delete-button"
                        danger
                        disabled={!canEdit}
                        onClick={() => onDelete(relation)}
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
                    testId="knowledge-refinement-refinement-relation-create-relation-button"
                    disabled={!canEdit}
                    type="primary"
                    onClick={onAdd}
                >
                    新增关系
                </KuzhambuButton>
            </div>
            <Table<RefinementRelationRecord>
                aria-label="知识图谱精修关系表格"
                columns={columns}
                dataSource={relations}
                pagination={false}
                rowKey={(relation) =>
                    relation.draftId || relation.relationKey || relation.relationType || "relation"
                }
            />
        </>
    );
};
