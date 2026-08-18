import { Popover, Typography } from "antd";
import { KuzhambuTag } from "@/components";
import { normalizeId } from "@/types/id";
import type { KuzhambuTableColumn, KuzhambuTagType } from "@/components";
import type { GraphExtractionTaskRecord } from "./graph-extraction-types";

const { Text } = Typography;

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

const readMaterialTitle = (task: GraphExtractionTaskRecord) => task.materialTitle?.trim() || "-";

const readFailureReason = (task: GraphExtractionTaskRecord) =>
    task.failureReason || task.errorMessage || task.errorType || "暂无失败原因";

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

interface GraphExtractionTaskColumnOptions {
    canRetry?: boolean;
    retryingTaskId?: string | null;
    onRetry: (task: GraphExtractionTaskRecord) => void;
}

export const createGraphExtractionTaskColumns = ({
    canRetry = false,
    onRetry,
    retryingTaskId = null
}: GraphExtractionTaskColumnOptions): KuzhambuTableColumn<GraphExtractionTaskRecord>[] => [
    {
        key: "material",
        render: (_, task) => <Text strong>{readMaterialTitle(task)}</Text>,
        title: "任务素材"
    },
    {
        key: "categoryName",
        render: (_, task) => <KuzhambuTag type="neutral">{task.categoryName || "-"}</KuzhambuTag>,
        title: "素材分类",
        width: 120
    },
    {
        key: "executionStatus",
        render: (_, task) => {
            const status = readTaskExecutionStatus(task);
            const statusTag = (
                <KuzhambuTag type={readExecutionStatusType(status)}>{status}</KuzhambuTag>
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
            <KuzhambuTag type={readDispositionType(disposition)}>{disposition || "-"}</KuzhambuTag>
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
    },
    {
        key: "actions",
        options: (task) =>
            canRetry
                ? [
                      {
                          ariaLabel: `重试任务 ${readTaskId(task)}`,
                          disabled: !isTaskFailed(task) || retryingTaskId === readTaskId(task),
                          key: "retry",
                          onClick: () => onRetry(task),
                          testId: "knowledge-graph-extraction-graph-extraction-task-retry-button",
                          text: "重试"
                      }
                  ]
                : []
    }
];

export const graphExtractionTaskRowKey = readTaskId;
