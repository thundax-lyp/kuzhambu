import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import type { ColumnsType } from "antd/es/table";
import type { GraphExtractionTaskRecord } from "../graph-extraction-types";

interface GraphExtractionTaskTableProps {
    applyingTaskId?: string | null;
    cancellingBatchId?: number | null;
    canApply?: boolean;
    canEdit?: boolean;
    loading?: boolean;
    regeneratingTaskId?: string | null;
    tasks: GraphExtractionTaskRecord[];
    onApply: (task: GraphExtractionTaskRecord) => void;
    onCancelBatch: (task: GraphExtractionTaskRecord) => void;
    onOpenDetail: (task: GraphExtractionTaskRecord) => void;
    onRegenerate: (task: GraphExtractionTaskRecord) => void;
}

const readStatusColor = (status?: string | null) => {
    switch (status) {
        case "APPLIED":
            return "green";
        case "FAILED":
            return "red";
        case "SUCCEEDED":
            return "blue";
        default:
            return "default";
    }
};

export const GraphExtractionTaskTable = ({
    applyingTaskId,
    cancellingBatchId = null,
    canApply = false,
    canEdit = false,
    loading = false,
    regeneratingTaskId = null,
    tasks,
    onApply,
    onCancelBatch,
    onOpenDetail,
    onRegenerate
}: GraphExtractionTaskTableProps) => {
    const columns: ColumnsType<GraphExtractionTaskRecord> = [
        {
            dataIndex: "taskId",
            key: "taskId",
            title: "任务号"
        },
        {
            dataIndex: "taskType",
            key: "taskType",
            title: "类型"
        },
        {
            dataIndex: "batchJobId",
            key: "batchJobId",
            title: "批次号"
        },
        {
            dataIndex: "triggerSource",
            key: "triggerSource",
            title: "触发来源"
        },
        {
            dataIndex: "status",
            key: "status",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            ),
            title: "状态"
        },
        {
            dataIndex: "sourceContentType",
            key: "sourceContentType",
            title: "来源类型"
        },
        {
            dataIndex: "sourceContentId",
            key: "sourceContentId",
            title: "来源ID"
        },
        {
            dataIndex: "aiCandidateId",
            key: "aiCandidateId",
            title: "候选ID"
        },
        {
            key: "actions",
            render: (_, task) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-graph-extraction-graph-extraction-task-view-button"
                        onClick={() => onOpenDetail(task)}
                    >
                        查看
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-extraction-graph-extraction-task-action-button"
                        disabled={!canEdit}
                        loading={regeneratingTaskId === task.taskId}
                        onClick={() => onRegenerate(task)}
                    >
                        重生成
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-extraction-graph-extraction-task-action-button-2"
                        disabled={!canEdit || !task.batchJobId}
                        loading={cancellingBatchId === task.batchJobId}
                        onClick={() => onCancelBatch(task)}
                    >
                        取消批任务
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-extraction-graph-extraction-task-action-button-3"
                        type="primary"
                        disabled={!canApply || !task.aiCandidateId || task.status === "APPLIED"}
                        loading={applyingTaskId === task.taskId}
                        onClick={() => onApply(task)}
                    >
                        应用
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            ),
            title: "操作"
        }
    ];

    return (
        <Table<GraphExtractionTaskRecord>
            aria-label="知识抽取任务表格"
            columns={columns}
            dataSource={tasks}
            loading={loading}
            pagination={false}
            rowKey={(task) => task.taskId}
        />
    );
};
