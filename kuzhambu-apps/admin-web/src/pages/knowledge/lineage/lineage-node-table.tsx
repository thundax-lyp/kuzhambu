import { Table, Tag } from "antd";
import { isSameId, normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { LineageNodeRecord } from "./lineage-types";

interface LineageNodeTableProps {
    loading?: boolean;
    nodes: LineageNodeRecord[];
    selectedNodeId?: string | null;
    onSelectNode: (node: LineageNodeRecord) => void;
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

export const LineageNodeTable = ({
    loading = false,
    nodes,
    selectedNodeId,
    onSelectNode
}: LineageNodeTableProps) => {
    const columns: ColumnsType<LineageNodeRecord> = [
        { dataIndex: "nodeId", key: "nodeId", title: "节点号", width: 96 },
        { dataIndex: "name", key: "name", title: "名称" },
        { dataIndex: "nodeType", key: "nodeType", title: "类型" },
        { dataIndex: "generation", key: "generation", title: "代际", width: 88 },
        {
            dataIndex: "confirmationStatus",
            key: "confirmationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            ),
            title: "确认状态",
            width: 128
        }
    ];

    return (
        <Table<LineageNodeRecord>
            aria-label="世系节点列表"
            columns={columns}
            dataSource={nodes}
            loading={loading}
            pagination={false}
            rowClassName={(node) =>
                isSameId(node.nodeId, selectedNodeId) ? "knowledge-lineage-table-row--selected" : ""
            }
            rowKey={(node) => normalizeId(node.nodeId)}
            size="small"
            onRow={(node) => ({
                onClick: () => onSelectNode(node)
            })}
        />
    );
};
