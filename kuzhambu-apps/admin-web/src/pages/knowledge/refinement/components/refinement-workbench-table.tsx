import { Button, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import type { RefinementWorkbenchItem } from "../refinement-types";

interface RefinementWorkbenchTableProps {
    items: RefinementWorkbenchItem[];
    loading?: boolean;
    onOpenTask: (item: RefinementWorkbenchItem) => void;
}

const readStatusColor = (status?: string | null) => {
    switch (status) {
        case "APPLIED":
            return "green";
        case "DRAFT":
            return "blue";
        default:
            return "default";
    }
};

export const RefinementWorkbenchTable = ({
    items,
    loading = false,
    onOpenTask
}: RefinementWorkbenchTableProps) => {
    const columns: ColumnsType<RefinementWorkbenchItem> = [
        { title: "任务号", dataIndex: "refinementTaskId", key: "refinementTaskId" },
        { title: "版本号", dataIndex: "graphVersionId", key: "graphVersionId" },
        { title: "任务类型", dataIndex: "taskType", key: "taskType" },
        { title: "来源类型", dataIndex: "sourceContentType", key: "sourceContentType" },
        { title: "来源 ID", dataIndex: "sourceContentId", key: "sourceContentId" },
        { title: "门类", dataIndex: "sourceCategoryName", key: "sourceCategoryName" },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        {
            title: "进度",
            key: "progress",
            render: (_, item) =>
                `实体 ${item.progressSummary?.entityConfirmedCount ?? 0}/${
                    (item.progressSummary?.entityConfirmedCount ?? 0) +
                    (item.progressSummary?.entityPendingCount ?? 0)
                } · 关系 ${item.progressSummary?.relationConfirmedCount ?? 0}/${
                    (item.progressSummary?.relationConfirmedCount ?? 0) +
                    (item.progressSummary?.relationPendingCount ?? 0)
                }`
        },
        {
            title: "操作",
            key: "actions",
            render: (_, item) => (
                <Space.Compact>
                    <Button onClick={() => onOpenTask(item)}>打开任务</Button>
                </Space.Compact>
            )
        }
    ];

    return (
        <Table<RefinementWorkbenchItem>
            aria-label="知识图谱精修任务表格"
            columns={columns}
            dataSource={items}
            loading={loading}
            pagination={false}
            rowKey={(item) => item.refinementTaskId}
        />
    );
};
