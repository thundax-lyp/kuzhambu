import {
    DeleteOutlined,
    MergeCellsOutlined,
    ReloadOutlined,
    StopOutlined,
    SwapOutlined,
    SyncOutlined
} from "@ant-design/icons";
import { Empty, Typography } from "antd";
import type { ReactNode } from "react";
import { KuzhambuButton, KuzhambuDescriptions, KuzhambuSpace, KuzhambuTag } from "@/components";
import type {
    GraphExtractionTaskDetailRecord,
    GraphExtractionTaskRecord
} from "@/pages/knowledge/graph-extraction/graph-extraction-types";

const { Text } = Typography;

export interface TaskDispositionPanelProps {
    detail: GraphExtractionTaskDetailRecord | null;
    onCancel?: (task: GraphExtractionTaskRecord) => void;
    onDiscard?: (task: GraphExtractionTaskRecord) => void;
    onMerge?: (task: GraphExtractionTaskRecord) => void;
    onRegenerate?: (task: GraphExtractionTaskRecord) => void;
    onReplace?: (task: GraphExtractionTaskRecord) => void;
    onRetry?: (task: GraphExtractionTaskRecord) => void;
}

const readExecutionStatus = (task: GraphExtractionTaskRecord) =>
    task.executionStatus || task.status || "UNKNOWN";

const readTaskId = (task: GraphExtractionTaskRecord) => task.taskId || task.id || "-";

const readCandidateId = (detail: GraphExtractionTaskDetailRecord) =>
    detail.candidate?.candidateId || detail.task.aiCandidateId || detail.task.candidateId || "-";

const isRunningTask = (task: GraphExtractionTaskRecord) => {
    const status = readExecutionStatus(task);
    return status === "PENDING" || status === "RUNNING";
};

const isSucceededTask = (task: GraphExtractionTaskRecord) =>
    readExecutionStatus(task) === "SUCCEEDED";

const renderActionButton = ({
    danger = false,
    icon,
    label,
    onClick,
    testId,
    type
}: {
    danger?: boolean;
    icon: ReactNode;
    label: string;
    onClick?: () => void;
    testId: string;
    type?: "default" | "primary";
}) => (
    <KuzhambuButton
        ariaLabel={label}
        danger={danger}
        disabled={!onClick}
        icon={icon}
        testId={testId}
        type={type}
        onClick={onClick}
    >
        {label}
    </KuzhambuButton>
);

export const TaskDispositionPanel = ({
    detail,
    onCancel,
    onDiscard,
    onMerge,
    onRegenerate,
    onReplace,
    onRetry
}: TaskDispositionPanelProps) => {
    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-extraction-task-detail-disposition-section"
                description="暂无候选处置信息"
            />
        );
    }

    const { task } = detail;
    const executionStatus = readExecutionStatus(task);
    const hasPendingCandidate = executionStatus === "SUCCEEDED" && task.disposition === "PENDING";
    const canRegenerate = isSucceededTask(task);
    const actionButtons: ReactNode[] = [];

    if (executionStatus === "FAILED") {
        actionButtons.push(
            renderActionButton({
                icon: <ReloadOutlined />,
                label: "重试",
                onClick: onRetry ? () => onRetry(task) : undefined,
                testId: "knowledge-graph-extraction-task-disposition-retry-button",
                type: "primary"
            })
        );
    }

    if (isRunningTask(task)) {
        actionButtons.push(
            renderActionButton({
                danger: true,
                icon: <StopOutlined />,
                label: "取消",
                onClick: onCancel ? () => onCancel(task) : undefined,
                testId: "knowledge-graph-extraction-task-disposition-cancel-button"
            })
        );
    }

    if (hasPendingCandidate) {
        actionButtons.push(
            renderActionButton({
                icon: <MergeCellsOutlined />,
                label: "合并",
                onClick: onMerge ? () => onMerge(task) : undefined,
                testId: "knowledge-graph-extraction-task-disposition-merge-button",
                type: "primary"
            }),
            renderActionButton({
                icon: <SwapOutlined />,
                label: "覆盖",
                onClick: onReplace ? () => onReplace(task) : undefined,
                testId: "knowledge-graph-extraction-task-disposition-replace-button"
            }),
            renderActionButton({
                danger: true,
                icon: <DeleteOutlined />,
                label: "丢弃",
                onClick: onDiscard ? () => onDiscard(task) : undefined,
                testId: "knowledge-graph-extraction-task-disposition-discard-button"
            })
        );
    }

    if (canRegenerate) {
        actionButtons.push(
            renderActionButton({
                icon: <SyncOutlined />,
                label: "重新抽取",
                onClick: onRegenerate ? () => onRegenerate(task) : undefined,
                testId: "knowledge-graph-extraction-task-disposition-regenerate-button"
            })
        );
    }

    return (
        <KuzhambuSpace
            data-testid="knowledge-graph-extraction-task-detail-disposition-section"
            orientation="vertical"
            size={16}
            style={{ width: "100%" }}
        >
            <KuzhambuDescriptions
                bordered
                column={1}
                items={[
                    {
                        key: "taskId",
                        label: "任务号",
                        children: readTaskId(task)
                    },
                    {
                        key: "executionStatus",
                        label: "运行状态",
                        children: <KuzhambuTag type="neutral">{executionStatus}</KuzhambuTag>
                    },
                    {
                        key: "disposition",
                        label: "候选处置",
                        children: (
                            <KuzhambuTag type="neutral">{task.disposition || "-"}</KuzhambuTag>
                        )
                    },
                    {
                        key: "candidateId",
                        label: "候选 ID",
                        children: readCandidateId(detail)
                    }
                ]}
                size="small"
                variant="detail"
            />
            {actionButtons.length > 0 ? (
                <KuzhambuSpace size={8} wrap>
                    {actionButtons.map((button, index) => (
                        <span key={index}>{button}</span>
                    ))}
                </KuzhambuSpace>
            ) : (
                <Text type="secondary">当前状态没有可执行的候选处置动作。</Text>
            )}
        </KuzhambuSpace>
    );
};
