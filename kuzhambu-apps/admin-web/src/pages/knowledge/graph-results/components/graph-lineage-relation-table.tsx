import { Button, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import type { GraphLineageRelationRecord } from "../graph-results-types";

interface GraphLineageRelationTableProps {
    loading?: boolean;
    onOpenDetail: (relation: GraphLineageRelationRecord) => void;
    relations: GraphLineageRelationRecord[];
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

export const GraphLineageRelationTable = ({
    loading = false,
    onOpenDetail,
    relations
}: GraphLineageRelationTableProps) => {
    const columns: ColumnsType<GraphLineageRelationRecord> = [
        { dataIndex: "relationId", key: "relationId", title: "关系号" },
        { dataIndex: "sourceName", key: "sourceName", title: "源节点" },
        { dataIndex: "targetName", key: "targetName", title: "目标节点" },
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
                <Space.Compact>
                    <Button onClick={() => onOpenDetail(relation)}>查看详情</Button>
                </Space.Compact>
            ),
            title: "操作"
        }
    ];

    return (
        <Table<GraphLineageRelationRecord>
            aria-label="知识正式世系关系表格"
            columns={columns}
            dataSource={relations}
            loading={loading}
            pagination={false}
            rowKey={(relation) => relation.relationId}
        />
    );
};
