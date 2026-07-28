import { Table, Tag } from "antd";
import { isSameId, normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { LineageRelationRecord } from "../lineage-types";

interface LineageRelationTableProps {
    loading?: boolean;
    relations: LineageRelationRecord[];
    selectedRelationId?: string | null;
    onSelectRelation: (relation: LineageRelationRecord) => void;
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

export const LineageRelationTable = ({
    loading = false,
    relations,
    selectedRelationId,
    onSelectRelation
}: LineageRelationTableProps) => {
    const columns: ColumnsType<LineageRelationRecord> = [
        { dataIndex: "relationId", key: "relationId", title: "关系号", width: 96 },
        { dataIndex: "sourceNodeName", key: "sourceNodeName", title: "起点" },
        { dataIndex: "targetNodeName", key: "targetNodeName", title: "终点" },
        { dataIndex: "relationLabel", key: "relationLabel", title: "关系" },
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
        <Table<LineageRelationRecord>
            aria-label="世系关系列表"
            columns={columns}
            dataSource={relations}
            loading={loading}
            pagination={false}
            rowClassName={(relation) =>
                isSameId(relation.relationId, selectedRelationId)
                    ? "knowledge-lineage-table-row--selected"
                    : ""
            }
            rowKey={(relation) => normalizeId(relation.relationId)}
            size="small"
            onRow={(relation) => ({
                onClick: () => onSelectRelation(relation)
            })}
        />
    );
};
