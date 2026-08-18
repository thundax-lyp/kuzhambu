import { Pagination, Popover, Typography } from "antd";
import { KuzhambuSpace, KuzhambuTable, KuzhambuTag } from "@/components";
import { normalizeId } from "@/types/id";
import { PAGE_SIZE_OPTIONS } from "@/types/page";
import type { KuzhambuTableColumn, KuzhambuTagType } from "@/components";
import type { GraphExtractionTaskRecord } from "../graph-extraction-types";

const { Text } = Typography;

interface GraphExtractionTaskTableProps {
    canRetry?: boolean;
    loading?: boolean;
    pageNo?: number;
    pageSize?: number;
    retryingTaskId?: string | null;
    tasks: GraphExtractionTaskRecord[];
    total?: number;
    onPageChange?: (pageNo: number, pageSize: number) => void;
    onRetry: (task: GraphExtractionTaskRecord) => void;
}

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

const readMaterialLabel = (task: GraphExtractionTaskRecord) => {
    const materialRef = task.materialRef;
    const contentType = materialRef?.contentType || task.sourceContentType || "-";
    const contentRefId = materialRef?.contentRefId || task.sourceContentId || "-";
    return `${contentType} / ${contentRefId}`;
};

const readFailureReason = (task: GraphExtractionTaskRecord) =>
    task.failureReason || task.errorMessage || task.errorType || "暂无失败原因";

const formatTimestamp = (value?: number | string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

const readRelatedTasks = (task: GraphExtractionTaskRecord) => {
    const relatedItems = [
        task.batchId ? `批次 ${task.batchId}` : null,
        task.batchJobId ? `批任务 ${task.batchJobId}` : null,
        task.parentTaskId ? `父任务 ${task.parentTaskId}` : null,
        task.triggeredByTaskId ? `触发 ${task.triggeredByTaskId}` : null,
        task.regeneratedFromTaskId ? `重生成自 ${task.regeneratedFromTaskId}` : null,
        task.supersededByTaskId ? `替代为 ${task.supersededByTaskId}` : null
    ].filter((item): item is string => Boolean(item));

    return relatedItems.length > 0 ? relatedItems.join("；") : "-";
};

const readTaskId = (task: GraphExtractionTaskRecord) => normalizeId(task.taskId || task.id);
const isTaskFailed = (task: GraphExtractionTaskRecord) =>
    readTaskExecutionStatus(task) === "FAILED";

export const GraphExtractionTaskTable = ({
    canRetry = false,
    loading = false,
    pageNo = 1,
    pageSize = 20,
    retryingTaskId = null,
    tasks,
    total = 0,
    onPageChange = () => undefined,
    onRetry
}: GraphExtractionTaskTableProps) => {
    const columns = createGraphExtractionTaskColumns({ canRetry, onRetry, retryingTaskId });

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <KuzhambuTable<GraphExtractionTaskRecord>
                ariaLabel="知识抽取表格"
                columns={columns}
                dataSource={tasks}
                loading={loading}
                pagination={false}
                rowKey={readTaskId}
            />
            <Pagination
                current={pageNo}
                pageSize={pageSize}
                pageSizeOptions={PAGE_SIZE_OPTIONS}
                showSizeChanger
                showTotal={(count) => `共 ${count} 个任务`}
                total={total}
                onChange={onPageChange}
            />
        </KuzhambuSpace>
    );
};

export const createGraphExtractionTaskColumns = ({
    canRetry = false,
    onRetry,
    retryingTaskId = null
}: Pick<
    GraphExtractionTaskTableProps,
    "canRetry" | "onRetry" | "retryingTaskId"
>): KuzhambuTableColumn<GraphExtractionTaskRecord>[] => [
    {
        key: "material",
        render: (_, task) => (
            <KuzhambuSpace orientation="vertical" size={2}>
                <Text strong>{task.materialTitle || readMaterialLabel(task)}</Text>
                <Text type="secondary">任务 {readTaskId(task) || "-"}</Text>
            </KuzhambuSpace>
        ),
        title: "任务素材"
    },
    {
        key: "categoryName",
        render: (_, task) => <KuzhambuTag type="neutral">{task.categoryName || "-"}</KuzhambuTag>,
        title: "素材分类",
        width: 140
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
        width: 112
    },
    {
        dataIndex: "disposition",
        key: "disposition",
        render: (disposition?: string | null) => (
            <KuzhambuTag type={readDispositionType(disposition)}>{disposition || "-"}</KuzhambuTag>
        ),
        title: "采纳状态",
        width: 128
    },
    {
        key: "stage",
        render: (_, task) => `${task.currentStage || "-"} / ${task.progress ?? 0}%`,
        title: "阶段",
        width: 168
    },
    {
        dataIndex: "attemptNo",
        key: "attemptNo",
        title: "尝试",
        width: 80
    },
    {
        key: "completedAt",
        render: (_, task) => formatTimestamp(task.completedAt || task.requestedAt),
        title: "最后执行时间",
        width: 180
    },
    {
        key: "relatedTasks",
        ellipsis: true,
        render: (_, task) => readRelatedTasks(task),
        title: "关联任务",
        width: 220
    },
    {
        dataIndex: "purgeAfter",
        key: "purgeAfter",
        render: (purgeAfter?: string | null) => purgeAfter || "-",
        title: "清理时间",
        width: 160
    },
    {
        key: "actions",
        options: (task) =>
            isTaskFailed(task)
                ? [
                      {
                          key: "retry",
                          text: "重试",
                          ariaLabel: `重试任务 ${readTaskId(task)}`,
                          testId: "knowledge-graph-extraction-graph-extraction-task-retry-button",
                          disabled: !canRetry || retryingTaskId === readTaskId(task),
                          onClick: () => onRetry(task)
                      }
                  ]
                : []
    }
];

export const graphExtractionTaskRowKey = readTaskId;
