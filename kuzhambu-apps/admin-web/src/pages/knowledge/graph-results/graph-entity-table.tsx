import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { GraphEntityRecord } from "./graph-results-types";

interface GraphEntityTableProps {
    entities: GraphEntityRecord[];
    loading?: boolean;
    onOpenDetail: (entity: GraphEntityRecord) => void;
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

export const GraphEntityTable = ({
    entities,
    loading = false,
    onOpenDetail
}: GraphEntityTableProps) => {
    const columns: ColumnsType<GraphEntityRecord> = [
        { dataIndex: "entityId", key: "entityId", title: "实体号" },
        { dataIndex: "name", key: "name", title: "名称" },
        { dataIndex: "entityType", key: "entityType", title: "类型" },
        {
            dataIndex: "confirmationStatus",
            key: "confirmationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            ),
            title: "确认状态"
        },
        { dataIndex: "latestVersionId", key: "latestVersionId", title: "版本号" },
        {
            key: "actions",
            render: (_, entity) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-graph-results-graph-entity-view-detail-button"
                        onClick={() => onOpenDetail(entity)}
                    >
                        查看详情
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            ),
            title: "操作"
        }
    ];

    return (
        <Table<GraphEntityRecord>
            aria-label="知识正式实体表格"
            columns={columns}
            dataSource={entities}
            loading={loading}
            pagination={false}
            rowKey={(entity) => normalizeId(entity.entityId)}
        />
    );
};
