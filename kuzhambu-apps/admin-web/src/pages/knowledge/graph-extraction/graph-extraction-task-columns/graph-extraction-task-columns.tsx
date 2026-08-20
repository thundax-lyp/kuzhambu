import { Popover, Typography } from "antd";
import { KuzhambuTag } from "@/components";
import { normalizeId } from "@/types/id";
import type {
    KuzhambuTableColumn,
    KuzhambuTableRowActionOption,
    KuzhambuTagType
} from "@/components";
import type { GraphExtractionTaskRecord } from "@/pages/knowledge/graph-extraction/graph-extraction-types";

const { Text } = Typography;

const EXECUTION_STATUS_LABELS: Record<string, string> = {
    CANCELLED: "已取消",
    FAILED: "已失败",
    PENDING: "待执行",
    RUNNING: "运行中",
    SUCCEEDED: "已成功"
};

const DISPOSITION_LABELS: Record<string, string> = {
    ADOPTED_MERGE: "合并采纳",
    ADOPTED_REPLACE: "替换采纳",
    DISCARDED: "已丢弃",
    PENDING: "待采纳",
    SUPERSEDED: "已替代"
};

const readExecutionStatusType = (status?: string | null): KuzhambuTagType => {
    switch (status) {
        case "SUCCEEDED":
            return "success";
        case "FAILED":
            return "danger";
        case "RUNNING":
            return "info";
        case "PENDING":
            return "warning";
        case "CANCELLED":
            return "neutral";
        default:
            return "neutral";
    }
};

const readDispositionType = (disposition?: string | null): KuzhambuTagType => {
    switch (disposition) {
        case "ADOPTED_MERGE":
        case "ADOPTED_REPLACE":
            return "success";
        case "DISCARDED":
        case "SUPERSEDED":
            return "warning";
        case "PENDING":
            return "info";
        default:
            return "neutral";
    }
};

const readTaskExecutionStatus = (task: GraphExtractionTaskRecord) =>
    task.executionStatus || task.status || "UNKNOWN";

const readMaterialPath = (task: GraphExtractionTaskRecord) =>
    [task.categoryName, task.volumeName, task.materialTitle]
        .map((value) => value?.trim())
        .filter(Boolean)
        .join(" / ") || "-";

const readFailureReason = (task: GraphExtractionTaskRecord) =>
    task.failureReason || task.errorMessage || task.errorType || "暂无失败原因";

const readExecutionStatusLabel = (status: string) => EXECUTION_STATUS_LABELS[status] ?? status;
const readDispositionLabel = (disposition?: string | null) =>
    disposition ? (DISPOSITION_LABELS[disposition] ?? disposition) : "-";

const formatTimestamp = (value?: number | string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = typeof value === "string" && /^\d+$/.test(value) ? Number(value) : value;
    const date = new Date(timestamp);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

const readTaskId = (task: GraphExtractionTaskRecord) => normalizeId(task.taskId || task.id);
const isTaskFailed = (task: GraphExtractionTaskRecord) =>
    readTaskExecutionStatus(task) === "FAILED";
const isTaskDeletable = (task: GraphExtractionTaskRecord) => {
    const status = readTaskExecutionStatus(task);
    return (
        status === "FAILED" ||
        status === "CANCELLED" ||
        (status === "SUCCEEDED" && Boolean(task.disposition) && task.disposition !== "PENDING")
    );
};

interface GraphExtractionTaskColumnOptions {
    canRetry?: boolean;
    deletingTaskId?: string | null;
    retryingTaskId?: string | null;
    onDelete: (task: GraphExtractionTaskRecord) => void;
    onRetry: (task: GraphExtractionTaskRecord) => void;
}

export const createGraphExtractionTaskColumns = ({
    canRetry = false,
    deletingTaskId = null,
    onDelete,
    onRetry,
    retryingTaskId = null
}: GraphExtractionTaskColumnOptions): KuzhambuTableColumn<GraphExtractionTaskRecord>[] => {
    const columns: KuzhambuTableColumn<GraphExtractionTaskRecord>[] = [
        {
            key: "material",
            render: (_, task) => <Text strong>{readMaterialPath(task)}</Text>,
            title: "素材路径"
        },
        {
            key: "executionStatus",
            render: (_, task) => {
                const status = readTaskExecutionStatus(task);
                const statusTag = (
                    <KuzhambuTag type={readExecutionStatusType(status)}>
                        {readExecutionStatusLabel(status)}
                    </KuzhambuTag>
                );
                return status === "FAILED" ? (
                    <Popover content={readFailureReason(task)} title="失败原因">
                        {statusTag}
                    </Popover>
                ) : (
                    statusTag
                );
            },
            title: "运行状态",
            width: 120
        },
        {
            dataIndex: "disposition",
            key: "disposition",
            render: (disposition?: string | null) => (
                <KuzhambuTag type={readDispositionType(disposition)}>
                    {readDispositionLabel(disposition)}
                </KuzhambuTag>
            ),
            title: "采纳状态",
            width: 140
        },
        {
            dataIndex: "attemptNo",
            key: "attemptNo",
            title: "尝试",
            width: 60
        },
        {
            key: "completedAt",
            render: (_, task) => formatTimestamp(task.completedAt || task.requestedAt),
            title: "最后执行时间",
            width: 180
        }
    ];

    if (!canRetry) {
        return columns;
    }

    return [
        ...columns,
        {
            key: "actions",
            options: (task) => {
                const taskId = readTaskId(task);
                const options: KuzhambuTableRowActionOption<GraphExtractionTaskRecord>[] = [];
                if (isTaskFailed(task)) {
                    options.push({
                        ariaLabel: `重试任务 ${taskId}`,
                        disabled: retryingTaskId === taskId,
                        key: "retry",
                        onClick: () => onRetry(task),
                        testId: "knowledge-graph-extraction-graph-extraction-task-retry-button",
                        text: "重试"
                    });
                }
                if (isTaskDeletable(task)) {
                    options.push({
                        ariaLabel: `删除任务 ${taskId}`,
                        disabled: deletingTaskId === taskId,
                        key: "delete",
                        onClick: () => onDelete(task),
                        testId: "knowledge-graph-extraction-graph-extraction-task-delete-button",
                        text: "删除",
                        type: "danger"
                    });
                }
                return options;
            }
        }
    ];
};

export const graphExtractionTaskRowKey = readTaskId;
