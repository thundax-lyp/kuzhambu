import { Tag, Typography } from "antd";
import { KuzhambuSpace, KuzhambuButton, KuzhambuAlert, KuzhambuCard } from "@/components";
import type {
    AiRefinementStreamEventRecord,
    AiRefinementTaskRecord
} from "./ai-refinement-task-types";
import * as aiRefinementTaskService from "./ai-refinement-task-service";

interface AiRefinementStreamPanelProps {
    events: AiRefinementStreamEventRecord[];
    isStreaming?: boolean;
    onClose: () => void;
    onRetry?: () => void;
    onViewCandidate?: () => void;
    streamErrorText?: string | null;
    task: AiRefinementTaskRecord;
}

const findLatestStage = (events: AiRefinementStreamEventRecord[], task: AiRefinementTaskRecord) => {
    const latestEvent = [...events].reverse().find((event) => event.stage);
    return latestEvent?.stage || task.failureStage || task.status;
};

const collectDeltaText = (events: AiRefinementStreamEventRecord[]) => {
    return events
        .map((event) => event.deltaText)
        .filter((value): value is string => Boolean(value?.trim()))
        .join("");
};

const findWarningText = (events: AiRefinementStreamEventRecord[]) => {
    const warning = events.find((event) => event.eventType === "warning");
    return warning?.deltaText || warning?.errorMessage || warning?.stage || null;
};

const findErrorText = (
    events: AiRefinementStreamEventRecord[],
    task: AiRefinementTaskRecord,
    streamErrorText?: string | null
) => {
    if (streamErrorText) {
        return streamErrorText;
    }
    const event = [...events].reverse().find((item) => item.eventType === "error");
    return (
        aiRefinementTaskService.getTaskFailureText(
            event?.failureStage,
            event?.errorType,
            event?.errorMessage
        ) ||
        aiRefinementTaskService.getTaskFailureText(
            task.failureStage,
            task.errorType,
            task.errorMessage
        )
    );
};

export const AiRefinementStreamPanel = ({
    events,
    isStreaming = false,
    onClose,
    onRetry,
    onViewCandidate,
    streamErrorText = null,
    task
}: AiRefinementStreamPanelProps) => {
    const stage = findLatestStage(events, task);
    const deltaText = collectDeltaText(events);
    const warningText = findWarningText(events);
    const errorText = findErrorText(events, task, streamErrorText);
    const canViewCandidate = task.status === "SUCCEEDED" && Boolean(task.candidateId);
    const canRetryStreamFailure =
        Boolean(errorText) && aiRefinementTaskService.getTaskRetryable("FAILED", task.capability);
    const canRetry =
        (aiRefinementTaskService.getTaskRetryable(task.status, task.capability) ||
            canRetryStreamFailure) &&
        Boolean(onRetry);

    return (
        <KuzhambuCard
            aria-label="三才图会 AI 流式过程"
            size="small"
            title="AI 流式过程"
            extra={
                <KuzhambuSpace size={8}>
                    <Tag color={isStreaming ? "processing" : "default"}>{task.status}</Tag>
                    <span>{aiRefinementTaskService.getTaskCapabilityLabel(task.capability)}</span>
                </KuzhambuSpace>
            }
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <Typography.Text type="secondary">阶段：{stage}</Typography.Text>
                {warningText ? <KuzhambuAlert showIcon type="warning" title={warningText} /> : null}
                {errorText ? (
                    <KuzhambuAlert showIcon type="error" title="失败原因" description={errorText} />
                ) : null}
                <Typography.Paragraph
                    style={{
                        background: "#f6f8fa",
                        border: "1px solid #e5e7eb",
                        borderRadius: 6,
                        marginBottom: 0,
                        minHeight: 96,
                        padding: 12,
                        whiteSpace: "pre-wrap"
                    }}
                >
                    {deltaText || task.resultPreview || "等待 AI 流式内容..."}
                </Typography.Paragraph>
                <KuzhambuSpace wrap>
                    {canViewCandidate ? (
                        <KuzhambuButton
                            testId="classics-common-ai-refinement-stream-view-candidate-button"
                            type="primary"
                            onClick={onViewCandidate}
                        >
                            查看候选
                        </KuzhambuButton>
                    ) : null}
                    {canRetry ? (
                        <KuzhambuButton
                            testId="classics-common-ai-refinement-stream-retry-button"
                            onClick={onRetry}
                        >
                            重试
                        </KuzhambuButton>
                    ) : null}
                    <KuzhambuButton
                        testId="classics-common-ai-refinement-stream-close-process-button"
                        onClick={onClose}
                    >
                        关闭过程
                    </KuzhambuButton>
                </KuzhambuSpace>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
