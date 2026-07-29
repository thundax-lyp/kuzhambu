import { useRef } from "react";
import type {
    AiRefinementStreamEventRecord,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import { AiCandidatePanel } from "@/pages/classics/common/ai-candidate-panel";
import { AiRefinementStreamPanel } from "@/pages/classics/common/ai-refinement-stream-panel";

interface SancaiEntryVisualRefinementSectionProps {
    entryId: string;
    isStreamingRefinementTask: boolean;
    selectedVisualAssetId: string | null;
    streamErrorText?: string | null;
    streamEvents: AiRefinementStreamEventRecord[];
    streamingRefinementTask: AiRefinementTaskRecord | null;
    onCloseStreamingRefinementTask: () => void;
    onRefreshVisualAssetCandidates: (objectId?: string | null) => void;
    onRetryRefinementTask: (task: AiRefinementTaskRecord) => void;
    onVisualAssetCandidateChanged: () => Promise<void> | void;
}

export const SancaiEntryVisualRefinementSection = ({
    entryId,
    isStreamingRefinementTask,
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
