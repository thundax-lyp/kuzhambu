import { Empty, Progress, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import {
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
    PENDING: "neutral",
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

interface MaterialTaskSummaryPanelProps {
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

const buildGraphMaterialTaskUrl = (detail: GraphMaterialDetailRecord) => {
    const params = new URLSearchParams();
    params.set("contentRefs", JSON.stringify([detail.source.contentRef]));
    return `/knowledge/graph-extraction?${params.toString()}`;
};

const renderTaskStatus = (status: GraphTaskExecutionStatus) => (
    <KuzhambuTag type={TASK_STATUS_TYPES[status]}>{TASK_STATUS_LABELS[status]}</KuzhambuTag>
);

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

export const MaterialTaskSummaryPanel = ({ detail }: MaterialTaskSummaryPanelProps) => {
    const navigate = useNavigate();

    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-material-detail-tasks-section"
                description="请选择素材查看任务摘要。"
            />
        );
    }

    const latestTask = detail.taskSummary.latestTask;

    return (
        <KuzhambuSpace
            data-testid="knowledge-graph-material-detail-tasks-section"
            orientation="vertical"
            size={12}
            style={{ width: "100%" }}
        >
            <KuzhambuCard
                title="任务摘要"
                size="small"
                extra={
                    <KuzhambuButton
                        ariaLabel={`查看任务 ${detail.source.title}`}
                        testId="knowledge-graph-material-detail-view-tasks-button"
                        onClick={() => navigate(buildGraphMaterialTaskUrl(detail))}
                    >
                        查看任务
                    </KuzhambuButton>
                }
            >
                <KuzhambuDescriptions
                    ariaLabel="素材任务摘要"
                    column={3}
                    items={[
                        {
                            label: "运行中任务",
                            children: detail.taskSummary.activeTaskCount
                        },
                        {
                            label: "待处置候选",
                            children: detail.taskSummary.pendingReviewTaskCount
                        },
                        {
                            label: "失败任务",
                            children: detail.taskSummary.failedTaskCount
                        }
                    ]}
                    size="small"
                    bordered
                />
            </KuzhambuCard>

            <KuzhambuCard title="最近任务" size="small">
                {latestTask ? (
                    <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
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
