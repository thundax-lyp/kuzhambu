import { Typography } from "antd";
import { KuzhambuSpace, KuzhambuTable, KuzhambuTag } from "@/components";
import { normalizeId } from "@/types/id";
import type {
    KuzhambuTableColumn,
    KuzhambuTableRowActionOption,
    KuzhambuTagType
} from "@/components";
import type { GraphExtractionTaskRecord } from "../graph-extraction-types";

const { Text } = Typography;

interface GraphExtractionTaskTableProps {
    applyingTaskId?: string | null;
    cancellingBatchId?: string | null;
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

const readResultSummary = (task: GraphExtractionTaskRecord) => {
    const summary = task.resultSummary;
    if (!summary) {
        return "-";
    }
    return `节点 ${summary.nodeCount}，关系 ${summary.edgeCount}，告警 ${summary.warningCount}`;
};

const readFailureReason = (task: GraphExtractionTaskRecord) =>
    task.failureReason || task.errorMessage || task.errorType || "-";

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

const isTaskRunning = (task: GraphExtractionTaskRecord) => {
    const status = readTaskExecutionStatus(task);
    return status === "PENDING" || status === "RUNNING";
};

const isTaskFailed = (task: GraphExtractionTaskRecord) =>
    readTaskExecutionStatus(task) === "FAILED";

const canApplyTask = (task: GraphExtractionTaskRecord) =>
    Boolean(task.aiCandidateId || task.candidateId) &&
    readTaskExecutionStatus(task) === "SUCCEEDED" &&
    task.disposition === "PENDING";

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
    const columns: KuzhambuTableColumn<GraphExtractionTaskRecord>[] = [
        {
            key: "material",
            render: (_, task) => (
                <KuzhambuSpace orientation="vertical" size={2}>
                    <Text strong>{readMaterialLabel(task)}</Text>
                    <Text type="secondary">任务 {normalizeId(task.taskId || task.id) || "-"}</Text>
                </KuzhambuSpace>
            ),
            title: "任务素材"
        },
        {
            key: "executionStatus",
            render: (_, task) => (
                <KuzhambuTag type={readExecutionStatusType(readTaskExecutionStatus(task))}>
                    {readTaskExecutionStatus(task)}
                </KuzhambuTag>
            ),
            title: "运行状态",
            width: 112
        },
        {
            dataIndex: "disposition",
            key: "disposition",
            render: (disposition?: string | null) => (
                <KuzhambuTag type={readDispositionType(disposition)}>
                    {disposition || "-"}
                </KuzhambuTag>
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
            dataIndex: "selectionScopeJson",
            ellipsis: true,
            key: "inputSummary",
            render: (selectionScopeJson?: string | null) => selectionScopeJson || "-",
            title: "输入摘要",
            width: 220
        },
        {
            key: "resultSummary",
            render: (_, task) => readResultSummary(task),
            title: "结果摘要",
            width: 190
        },
        {
            key: "failureReason",
            ellipsis: true,
            render: (_, task) => readFailureReason(task),
            title: "失败原因",
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
            options: (task) => {
                const taskId = normalizeId(task.taskId || task.id);
                const actions: KuzhambuTableRowActionOption<GraphExtractionTaskRecord>[] = [
                    {
                        key: "view",
                        text: "查看",
                        ariaLabel: `查看任务 ${taskId}`,
                        testId: "knowledge-graph-extraction-graph-extraction-task-view-button",
                        onClick: () => onOpenDetail(task)
                    }
                ];

                if (isTaskFailed(task)) {
                    actions.push({
                        key: "retry",
                        text: "重试",
                        ariaLabel: `重试任务 ${taskId}`,
                        testId: "knowledge-graph-extraction-graph-extraction-task-retry-button",
                        disabled: !canEdit || regeneratingTaskId === task.taskId,
                        onClick: () => onRegenerate(task)
                    });
                }

                if (isTaskRunning(task)) {
                    actions.push({
                        key: "cancel",
                        text: "取消",
                        ariaLabel: `取消任务 ${taskId}`,
                        testId: "knowledge-graph-extraction-graph-extraction-task-cancel-button",
                        disabled:
                            !canEdit || !task.batchJobId || cancellingBatchId === task.batchJobId,
                        onClick: () => onCancelBatch(task)
                    });
                }

                if (canApplyTask(task)) {
                    actions.push({
                        key: "apply",
                        text: "应用",
                        ariaLabel: `应用任务 ${taskId}`,
                        testId: "knowledge-graph-extraction-graph-extraction-task-apply-button",
                        disabled: !canApply || applyingTaskId === task.taskId,
                        onClick: () => onApply(task)
                    });
                }

                return actions;
            }
        }
    ];

    return (
        <KuzhambuTable<GraphExtractionTaskRecord>
            ariaLabel="知识抽取表格"
            columns={columns}
            dataSource={tasks}
            loading={loading}
            pagination={false}
            rowKey={(task) => normalizeId(task.taskId)}
        />
    );
};
