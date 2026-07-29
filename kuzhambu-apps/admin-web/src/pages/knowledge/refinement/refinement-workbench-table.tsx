import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { RefinementWorkbenchRecord } from "./refinement-types";

interface RefinementWorkbenchTableProps {
    items: RefinementWorkbenchRecord[];
    loading?: boolean;
    onOpenTask: (item: RefinementWorkbenchRecord) => void;
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
    const columns: ColumnsType<RefinementWorkbenchRecord> = [
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
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-workbench-open-task-button"
                        onClick={() => onOpenTask(item)}
                    >
                        打开任务
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <Table<RefinementWorkbenchRecord>
            aria-label="知识图谱精修任务表格"
            columns={columns}
            dataSource={items}
            loading={loading}
            pagination={false}
            rowKey={(item) => normalizeId(item.refinementTaskId)}
        />
    );
};
