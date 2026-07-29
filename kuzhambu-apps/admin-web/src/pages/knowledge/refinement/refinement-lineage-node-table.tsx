import { Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { QualityAnnotationTarget, RefinementLineageNodeRecord } from "./refinement-types";

interface RefinementLineageNodeTableProps {
    canEdit: boolean;
    nodes: RefinementLineageNodeRecord[];
    sourceContentId?: string | null;
    sourceContentType?: string | null;
    onAnnotate: (target: Omit<QualityAnnotationTarget, "graphVersionId">) => void;
}

const readStatusColor = (status?: string | null) =>
    status === "MANUAL_CONFIRMED" ? "green" : "blue";

export const RefinementLineageNodeTable = ({
    canEdit,
    nodes,
    sourceContentId,
    sourceContentType,
    onAnnotate
}: RefinementLineageNodeTableProps) => {
    const columns: ColumnsType<RefinementLineageNodeRecord> = [
        { title: "名称", dataIndex: "name", key: "name" },
        { title: "类型", dataIndex: "nodeType", key: "nodeType" },
        { title: "代际", dataIndex: "generation", key: "generation" },
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
            render: (_, node) => (
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-annotate-button"
                    disabled={!canEdit}
                    onClick={() =>
                        onAnnotate({
                            objectType: "LINEAGE_NODE",
                            objectKey: node.nodeKey || "",
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
        <Table<RefinementLineageNodeRecord>
            aria-label="知识图谱精修世系节点表格"
            columns={columns}
            dataSource={nodes}
            pagination={false}
            rowKey={(node) =>
                normalizeId(node.draftId || node.nodeKey || node.name || "lineage-node")
            }
        />
    );
};
