import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import type { ColumnsType } from "antd/es/table";
import type { GraphLineageNodeRecord } from "../graph-results-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface GraphLineageNodeTableProps {
    loading?: boolean;
    nodes: GraphLineageNodeRecord[];
    onOpenDetail: (node: GraphLineageNodeRecord) => void;
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

export const GraphLineageNodeTable = ({
    loading = false,
    nodes,
    onOpenDetail
}: GraphLineageNodeTableProps) => {
    const columns: ColumnsType<GraphLineageNodeRecord> = [
        { dataIndex: "nodeId", key: "nodeId", title: "节点号" },
        { dataIndex: "name", key: "name", title: "名称" },
        { dataIndex: "nodeType", key: "nodeType", title: "类型" },
        { dataIndex: "generation", key: "generation", title: "世代" },
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
            render: (_, node) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-graph-results-graph-lineage-node-view-detail-button"
                        onClick={() => onOpenDetail(node)}
                    >
                        查看详情
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            ),
            title: "操作"
        }
    ];

    return (
        <Table<GraphLineageNodeRecord>
            aria-label="知识正式世系节点表格"
            columns={columns}
            dataSource={nodes}
            loading={loading}
            pagination={false}
            rowKey={(node) => node.nodeId}
        />
    );
};
