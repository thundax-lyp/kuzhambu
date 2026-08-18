import { App, Empty, Progress, Typography } from "antd";
import { RobotOutlined } from "@ant-design/icons";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type {
    GraphMaterialDetailRecord,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import "./material-task-summary-section.css";

const { Text } = Typography;

const TASK_STATUS_LABELS: Readonly<Record<GraphTaskExecutionStatus, string>> = {
    CANCELLED: "已取消",
    FAILED: "已失败",
    PENDING: "待执行",
    RUNNING: "运行中",
    SUCCEEDED: "已成功"
};

const TASK_STATUS_TYPES: Readonly<
    Record<GraphTaskExecutionStatus, "neutral" | "info" | "success" | "warning" | "danger">
> = {
    CANCELLED: "neutral",
    FAILED: "danger",
    PENDING: "warning",
    RUNNING: "info",
    SUCCEEDED: "success"
};

const DISPOSITION_LABELS: Readonly<Record<GraphTaskDisposition, string>> = {
    ADOPTED_MERGE: "合并采纳",
    ADOPTED_REPLACE: "替换采纳",
    DISCARDED: "已丢弃",
    PENDING: "待处置",
    SUPERSEDED: "已替代"
};

interface MaterialTaskSummarySectionProps {
    detail: GraphMaterialDetailRecord | null;
}

const formatTimestamp = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(Number(value));
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleString("zh-CN", { hour12: false });
};

const renderTaskStatus = (status: GraphTaskExecutionStatus) => (
    <KuzhambuTag type={TASK_STATUS_TYPES[status]}>{TASK_STATUS_LABELS[status]}</KuzhambuTag>
);

const hasActiveExtractionTask = (status?: GraphTaskExecutionStatus) =>
    status === "PENDING" || status === "RUNNING";

const renderDisposition = (disposition?: GraphTaskDisposition | null) => {
    if (!disposition) {
        return <Text type="secondary">-</Text>;
    }
    return (
        <KuzhambuTag type={disposition === "PENDING" ? "warning" : "success"}>
            {DISPOSITION_LABELS[disposition]}
        </KuzhambuTag>
    );
};

export const MaterialTaskSummarySection = ({ detail }: MaterialTaskSummarySectionProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canExtractMaterial = hasPermission("knowledge:graph:edit");
    const extractionMutation = useMutation({
        mutationFn: service.createExtraction,
        onSuccess: async (task) => {
            await queryClient.refetchQueries({
                queryKey: ["knowledge", "graph-material"],
                type: "active"
            });
            messageApi.success(`抽取任务已创建 #${task?.id ?? "-"}`);
        }
    });

    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-material-detail-tasks-section"
                description="请选择素材查看任务摘要。"
            />
        );
    }

    const taskSummary = detail.taskSummary;
    const latestTask = detail.extractionTasks?.[0] ?? taskSummary?.latestTask;

    return (
        <KuzhambuSpace
            className="knowledge-graph-material-task-summary-section"
            data-testid="knowledge-graph-material-detail-tasks-section"
            orientation="vertical"
            size={12}
        >
            <KuzhambuCard
                title="任务摘要"
                size="small"
                extra={
                    <KuzhambuButton
                        ariaLabel={`抽取素材 ${detail.source.title}`}
                        disabled={
                            !canExtractMaterial ||
                            extractionMutation.isPending ||
                            hasActiveExtractionTask(latestTask?.executionStatus)
                        }
                        icon={<RobotOutlined />}
                        loading={extractionMutation.isPending}
                        testId="knowledge-graph-material-detail-extract-button"
                        type="primary"
                        onClick={() =>
                            extractionMutation.mutate({ contentRef: detail.source.contentRef })
                        }
                    >
                        抽取
                    </KuzhambuButton>
                }
            >
                <KuzhambuDescriptions
                    ariaLabel="素材任务摘要"
                    column={3}
                    items={[
                        {
                            label: "运行中任务",
                            children: taskSummary?.activeTaskCount ?? "0"
                        },
                        {
                            label: "待处置候选",
                            children: taskSummary?.pendingReviewTaskCount ?? "0"
                        },
                        {
                            label: "失败任务",
                            children: taskSummary?.failedTaskCount ?? "0"
                        }
                    ]}
                    size="small"
                    bordered
                />
                {extractionMutation.error ? (
                    <KuzhambuAlert
                        title={
                            extractionMutation.error instanceof Error
                                ? extractionMutation.error.message
                                : "抽取任务创建失败"
                        }
                        type="error"
                        showIcon
                    />
                ) : null}
            </KuzhambuCard>

            <KuzhambuCard title="最近任务" size="small">
                {latestTask ? (
                    <KuzhambuSpace
                        className="knowledge-graph-material-task-summary-section-latest"
                        orientation="vertical"
                        size={12}
                    >
                        <KuzhambuDescriptions
                            ariaLabel="最近任务"
                            column={2}
                            items={[
                                { label: "任务编号", children: latestTask.id },
                                {
                                    label: "执行状态",
                                    children: renderTaskStatus(latestTask.executionStatus)
                                },
                                {
                                    label: "处置状态",
                                    children: renderDisposition(latestTask.disposition)
                                },
                                { label: "当前阶段", children: latestTask.currentStage },
                                { label: "尝试次数", children: latestTask.attemptNo },
                                { label: "批次号", children: latestTask.batchId ?? "-" },
                                {
                                    label: "请求时间",
                                    children: formatTimestamp(latestTask.requestedAt)
                                },
                                {
                                    label: "完成时间",
                                    children: formatTimestamp(latestTask.completedAt)
                                }
                            ]}
                            size="small"
                            bordered
                        />
                        <Progress
                            percent={latestTask.progress}
                            status={
                                latestTask.executionStatus === "FAILED" ? "exception" : undefined
                            }
                        />
                        {latestTask.failureReason ? (
                            <Text type="danger">{latestTask.failureReason}</Text>
                        ) : null}
                    </KuzhambuSpace>
                ) : (
                    <Text type="secondary">暂无任务记录</Text>
                )}
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
