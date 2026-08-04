import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { GraphRelationRecord } from "@/pages/knowledge/graph-result/graph-result-types";

interface GraphRelationTableProps {
    loading?: boolean;
    onOpenDetail: (relation: GraphRelationRecord) => void;
    relations: GraphRelationRecord[];
}

const readStatusColor = (status?: string | null) => {
    switch (status) {
        case "CONFIRMED":
            return "green";
        case "REJECTED":
            return "red";
        default:
            return "blue";
    }
};

export const GraphRelationTable = ({
    loading = false,
    onOpenDetail,
    relations
}: GraphRelationTableProps) => {
    const columns: ColumnsType<GraphRelationRecord> = [
        { dataIndex: "relationId", key: "relationId", title: "关系号" },
        { dataIndex: "sourceName", key: "sourceName", title: "源实体" },
        { dataIndex: "targetName", key: "targetName", title: "目标实体" },
        { dataIndex: "relationType", key: "relationType", title: "关系类型" },
        {
            dataIndex: "confirmationStatus",
            key: "confirmationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            ),
            title: "确认状态"
        },
        {
            key: "actions",
            render: (_, relation) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-graph-results-graph-relation-view-detail-button"
                        onClick={() => onOpenDetail(relation)}
                    >
                        查看详情
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            ),
            title: "操作"
        }
    ];

    return (
        <Table<GraphRelationRecord>
            aria-label="知识正式关系表格"
            columns={columns}
            dataSource={relations}
            loading={loading}
            pagination={false}
            rowKey={(relation) => normalizeId(relation.relationId)}
        />
    );
};
