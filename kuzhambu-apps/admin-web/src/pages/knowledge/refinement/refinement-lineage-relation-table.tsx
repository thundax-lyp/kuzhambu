import { Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { QualityAnnotationTarget, RefinementLineageRelationRecord } from "./refinement-types";

interface RefinementLineageRelationTableProps {
    canEdit: boolean;
    relations: RefinementLineageRelationRecord[];
    sourceContentId?: string | null;
    sourceContentType?: string | null;
    onAnnotate: (target: Omit<QualityAnnotationTarget, "graphVersionId">) => void;
}

const readStatusColor = (status?: string | null) =>
    status === "MANUAL_CONFIRMED" ? "green" : "blue";

export const RefinementLineageRelationTable = ({
    canEdit,
    relations,
    sourceContentId,
    sourceContentType,
    onAnnotate
}: RefinementLineageRelationTableProps) => {
    const columns: ColumnsType<RefinementLineageRelationRecord> = [
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
            key: "actions",
            render: (_, relation) => (
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-annotate-button-2"
                    disabled={!canEdit}
                    onClick={() =>
                        onAnnotate({
                            objectType: "LINEAGE_RELATION",
                            objectKey: relation.relationKey || "",
                            sourceContentType,
                            sourceContentId
                        })
                    }
                >
                    标注
                </KuzhambuButton>
            )
        }
    ];

    return (
        <Table<RefinementLineageRelationRecord>
            aria-label="知识图谱精修世系关系表格"
            columns={columns}
            dataSource={relations}
            pagination={false}
            rowKey={(relation) =>
                normalizeId(
                    relation.draftId ||
                        relation.relationKey ||
                        relation.relationType ||
                        "lineage-relation"
                )
            }
        />
    );
};
