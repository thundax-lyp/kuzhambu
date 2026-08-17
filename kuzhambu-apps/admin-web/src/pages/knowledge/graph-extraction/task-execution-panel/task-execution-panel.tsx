import { Empty, Progress, Typography } from "antd";
import type { ComponentProps } from "react";
import {
    KuzhambuDescriptions,
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuListMeta,
    KuzhambuSpace,
    KuzhambuSteps,
    KuzhambuTag
} from "@/components";
import type {
    GraphExtractionStageRecord,
    GraphExtractionStageStatus,
    GraphExtractionTaskDetailRecord
} from "@/pages/knowledge/graph-extraction/graph-extraction-types";

const { Text } = Typography;

interface TaskExecutionPanelProps {
    detail: GraphExtractionTaskDetailRecord | null;
}

const STAGE_STATUS_LABELS: Record<GraphExtractionStageStatus, string> = {
    FAILED: "失败",
    PENDING: "待执行",
    RUNNING: "执行中",
    SKIPPED: "已跳过",
    SUCCEEDED: "已完成"
};

const STAGE_STATUS_TAG_TYPES: Record<
    GraphExtractionStageStatus,
    ComponentProps<typeof KuzhambuTag>["type"]
> = {
    FAILED: "danger",
    PENDING: "neutral",
    RUNNING: "info",
    SKIPPED: "warning",
    SUCCEEDED: "success"
};

const STAGE_CODE_LABELS: Record<string, string> = {
    AI_EXECUTION: "AI 执行",
    CANDIDATE_PARSE: "候选解析",
    CANDIDATE_READY: "候选生成",
    CANDIDATE_VALIDATE: "候选校验",
    MATERIAL_RESOLVE: "素材准备",
    PROMPT_RENDER: "请求准备",
    RESULT_PARSE: "结果解析",
    RESULT_VALIDATE: "结果校验"
};

const formatTimestamp = (value?: number | string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

const formatText = (value?: string | null) => {
    return value?.trim() || "-";
};

const formatResultSummary = (detail: GraphExtractionTaskDetailRecord) => {
    const summary = detail.task.resultSummary;
    if (!summary) {
        return "-";
    }
    return `节点 ${summary.nodeCount}，关系 ${summary.edgeCount}，告警 ${summary.warningCount}`;
};

const clampProgress = (value?: number | null) => {
    if (typeof value !== "number" || Number.isNaN(value)) {
        return 0;
    }
    return Math.min(Math.max(Math.round(value), 0), 100);
};

const readStageTitle = (stage: GraphExtractionStageRecord) => {
    const stageLabel = STAGE_CODE_LABELS[stage.stageCode] ?? stage.stageCode;
    return `${stage.stageNo}. ${stageLabel}`;
};

const toStepStatus = (stage: GraphExtractionStageRecord) => {
    if (stage.status === "FAILED") {
        return "error" as const;
    }
    if (stage.status === "RUNNING") {
        return "process" as const;
    }
    if (stage.status === "SUCCEEDED" || stage.status === "SKIPPED") {
        return "finish" as const;
    }
    return "wait" as const;
};

export const TaskExecutionPanel = ({ detail }: TaskExecutionPanelProps) => {
    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-extraction-task-detail-execution-section"
                description="暂无执行过程"
            />
        );
    }

    const { stages, task } = detail;
    const taskProgress = clampProgress(task.progress);
    const activeStageIndex = stages.findIndex((stage) => stage.status === "RUNNING");

    return (
        <KuzhambuSpace
            data-testid="knowledge-graph-extraction-task-detail-execution-section"
            orientation="vertical"
            size={16}
            style={{ width: "100%" }}
        >
            <KuzhambuDescriptions
                column={1}
                size="small"
                variant="detail"
                bordered
                items={[
                    {
                        key: "executionStatus",
                        label: "运行状态",
                        children: task.executionStatus || task.status || "-"
                    },
                    {
                        key: "currentStage",
                        label: "当前阶段",
                        children: task.currentStage || "-"
                    },
                    {
                        key: "progress",
                        label: "整体进度",
                        children: <Progress percent={taskProgress} size="small" />
                    },
                    {
                        key: "resultSummary",
                        label: "结果摘要",
                        children: formatResultSummary(detail)
                    },
                    {
                        key: "failureReason",
                        label: "失败原因",
                        children: formatText(task.failureReason ?? task.errorMessage)
                    },
                    {
                        key: "requestedAt",
                        label: "请求时间",
                        children: formatTimestamp(task.requestedAt)
                    },
                    {
                        key: "completedAt",
                        label: "完成时间",
                        children: formatTimestamp(task.completedAt)
                    }
                ]}
            />

            {stages.length > 0 ? (
                <>
                    <KuzhambuSteps
                        current={activeStageIndex >= 0 ? activeStageIndex : undefined}
                        items={stages.map((stage) => ({
                            content: `${clampProgress(stage.progress)}%`,
                            status: toStepStatus(stage),
                            title: readStageTitle(stage)
                        }))}
                        size="small"
                        testId="knowledge-graph-extraction-task-execution-steps"
                    />
                    <KuzhambuList
                        ariaLabel="图谱抽取任务执行阶段"
                        bordered
                        dataSource={stages}
                        itemKey={(stage) => `${stage.stageNo}-${stage.stageCode}`}
                        renderItem={(stage) => (
                            <KuzhambuListItem
                                extra={
                                    <KuzhambuTag type={STAGE_STATUS_TAG_TYPES[stage.status]}>
                                        {STAGE_STATUS_LABELS[stage.status]}
                                    </KuzhambuTag>
                                }
                            >
                                <KuzhambuListMeta
                                    title={
                                        <KuzhambuSpace size={8} wrap>
                                            <Text strong>{readStageTitle(stage)}</Text>
                                            <Text type="secondary">
                                                进度 {clampProgress(stage.progress)}%
                                            </Text>
                                        </KuzhambuSpace>
                                    }
                                    description={
                                        <KuzhambuDescriptions
                                            column={1}
                                            items={[
                                                {
                                                    key: "inputSummary",
                                                    label: "输入摘要",
                                                    children: formatText(stage.inputSummary)
                                                },
                                                {
                                                    key: "outputSummary",
                                                    label: "输出摘要",
                                                    children: formatText(stage.outputSummary)
                                                },
                                                {
                                                    key: "failureReason",
                                                    label: "失败原因",
                                                    children: formatText(stage.failureReason)
                                                },
                                                {
                                                    key: "startedAt",
                                                    label: "开始时间",
                                                    children: formatTimestamp(stage.startedAt)
                                                },
                                                {
                                                    key: "completedAt",
                                                    label: "完成时间",
                                                    children: formatTimestamp(stage.completedAt)
                                                }
                                            ]}
                                            size="small"
                                            variant="detail"
                                        />
                                    }
                                />
                            </KuzhambuListItem>
                        )}
                        size="small"
                    />
                </>
            ) : (
                <Empty description="暂无阶段记录" />
            )}
        </KuzhambuSpace>
    );
};
