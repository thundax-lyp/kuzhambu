import { Card } from "antd";
import { useRef } from "react";
import { KuzhambuAlert, KuzhambuButton } from "@/components";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementStreamEventRecord,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import { AiRefinementStreamPanel } from "@/pages/classics/common/components/ai-refinement-stream-panel";

interface SancaiEntryVisualRefinementSectionProps {
    entryId: number;
    isStreamingRefinementTask: boolean;
    refinementTasks: AiRefinementTaskRecord[];
    retryingRefinementTaskId: number | null;
    selectedVisualAssetId: number | null;
    streamErrorText?: string | null;
    streamEvents: AiRefinementStreamEventRecord[];
    streamingRefinementTask: AiRefinementTaskRecord | null;
    onCloseStreamingRefinementTask: () => void;
    onRefreshVisualAssetCandidates: (objectId?: number | null) => void;
    onRetryRefinementTask: (task: AiRefinementTaskRecord) => void;
    onVisualAssetCandidateChanged: () => Promise<void> | void;
}

const isVisualRefinementTask = (task: AiRefinementTaskRecord) => {
    const capability = aiRefinementTaskService.getNormalizedTaskCapability(task.capability);
    return (
        capability === "image_analysis" ||
        capability === "visual" ||
        capability === "fusion" ||
        capability === "image_gen"
    );
};

export const SancaiEntryVisualRefinementSection = ({
    entryId,
    isStreamingRefinementTask,
    refinementTasks,
    retryingRefinementTaskId,
    selectedVisualAssetId,
    streamErrorText = null,
    streamEvents,
    streamingRefinementTask,
    onCloseStreamingRefinementTask,
    onRefreshVisualAssetCandidates,
    onRetryRefinementTask,
    onVisualAssetCandidateChanged
}: SancaiEntryVisualRefinementSectionProps) => {
    const candidatePanelRef = useRef<HTMLDivElement | null>(null);

    const viewStreamingCandidate = () => {
        onRefreshVisualAssetCandidates(streamingRefinementTask?.objectId ?? selectedVisualAssetId);
        candidatePanelRef.current?.scrollIntoView({
            block: "start",
            behavior: "smooth"
        });
        candidatePanelRef.current?.focus();
    };

    return (
        <>
            <Card size="small" title="AI 精修任务">
                <div style={{ display: "grid", gap: 8 }}>
                    {refinementTasks
                        .filter(isVisualRefinementTask)
                        .slice(0, 6)
                        .map((task) => {
                            const failureText = aiRefinementTaskService.getTaskFailureText(
                                task.failureStage,
                                task.errorType,
                                task.errorMessage
                            );
                            return (
                                <Card key={task.taskId} size="small" bodyStyle={{ padding: 12 }}>
                                    <div
                                        style={{
                                            display: "flex",
                                            justifyContent: "space-between",
                                            gap: 12,
                                            alignItems: "center",
                                            flexWrap: "wrap"
                                        }}
                                    >
                                        <div>
                                            {aiRefinementTaskService.getTaskCapabilityLabel(
                                                task.capability
                                            )}
                                            ：{task.status}
                                            {task.resultPreview ? ` / ${task.resultPreview}` : ""}
                                        </div>
                                        {aiRefinementTaskService.getTaskRetryable(
                                            task.status,
                                            task.capability
                                        ) ? (
                                            <KuzhambuButton
                                                testId="classics-sancai-sancai-entry-retry-button"
                                                size="small"
                                                loading={retryingRefinementTaskId === task.taskId}
                                                onClick={() => onRetryRefinementTask(task)}
                                            >
                                                重试
                                            </KuzhambuButton>
                                        ) : null}
                                    </div>
                                    {failureText ? (
                                        <KuzhambuAlert
                                            showIcon
                                            type="error"
                                            style={{ marginTop: 8 }}
                                            title="失败原因"
                                            description={failureText}
                                        />
                                    ) : null}
                                </Card>
                            );
                        })}
                </div>
            </Card>
            {streamingRefinementTask ? (
                <AiRefinementStreamPanel
                    events={streamEvents}
                    isStreaming={isStreamingRefinementTask}
                    streamErrorText={streamErrorText}
                    task={streamingRefinementTask}
                    onClose={onCloseStreamingRefinementTask}
                    onRetry={() => onRetryRefinementTask(streamingRefinementTask)}
                    onViewCandidate={viewStreamingCandidate}
                />
            ) : null}
            {selectedVisualAssetId ? (
                <div
                    ref={candidatePanelRef}
                    className="sancai-candidate-panel-anchor"
                    tabIndex={-1}
                >
                    <AiCandidatePanel
                        capabilities={["image_analysis", "visual", "fusion", "image_gen"]}
                        contentId={entryId}
                        contentType="SANCAI_ENTRY"
                        objectId={selectedVisualAssetId}
                        onApplied={onVisualAssetCandidateChanged}
                        onRejected={onVisualAssetCandidateChanged}
                    />
                </div>
            ) : null}
        </>
    );
};
