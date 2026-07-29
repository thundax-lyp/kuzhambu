import { KuzhambuButton, KuzhambuSpaceCompact, KuzhambuCard } from "@/components";

import { AiCandidatePanel } from "@/pages/classics/common/ai-candidate-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/classics-content-tag-panel";
import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import type { MingCustomsRecord } from "./ming-customs-types";

interface MingCustomsAiActionsProps {
    creatingRefinementCapability: AiRefinementTaskCapability | null;
    entry: MingCustomsRecord;
    onCandidateApplied: () => Promise<void>;
    onCandidateRejected: () => Promise<void>;
    onContentChanged: () => Promise<void>;
    onCreateRefinementTask: (
        entry: MingCustomsRecord,
        capability: AiRefinementTaskCapability
    ) => void;
    refinementTasks: AiRefinementTaskRecord[];
}

export const MingCustomsAiActions = ({
    creatingRefinementCapability,
    entry,
    onCandidateApplied,
    onCandidateRejected,
    onContentChanged,
    onCreateRefinementTask,
    refinementTasks
}: MingCustomsAiActionsProps) => {
    return (
        <>
            <KuzhambuCard
                size="small"
                title="AI 精修任务"
                extra={
                    <KuzhambuSpaceCompact>
                        <KuzhambuButton
                            testId="classics-ming-customs-ming-customs-action-button-3"
                            type="primary"
                            onClick={() => onCreateRefinementTask(entry, "summary")}
                            loading={creatingRefinementCapability === "summary"}
                        >
                            创建摘要任务
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-ming-customs-ming-customs-action-button-4"
                            onClick={() => onCreateRefinementTask(entry, "tags")}
                            loading={creatingRefinementCapability === "tags"}
                        >
                            创建标签任务
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-ming-customs-ming-customs-action-button-5"
                            onClick={() => onCreateRefinementTask(entry, "qa")}
                            loading={creatingRefinementCapability === "qa"}
                        >
                            创建问答任务
                        </KuzhambuButton>
                    </KuzhambuSpaceCompact>
                }
            >
                {refinementTasks.length ? (
                    refinementTasks.slice(0, 4).map((task) => (
                        <div key={task.taskId}>
                            {task.capability}：{task.status}
                            {task.resultPreview ? ` · ${task.resultPreview}` : ""}
                        </div>
                    ))
                ) : (
                    <div>暂无精修任务</div>
                )}
            </KuzhambuCard>
            <AiCandidatePanel
                capabilities={["summary", "tags", "qa"]}
                contentId={entry.id}
                contentType="MING_CUSTOMS"
                onApplied={onCandidateApplied}
                onRejected={onCandidateRejected}
            />
            <ClassicsContentTagPanel
                contentId={entry.id}
                contentType="MING_CUSTOMS"
                onChanged={onContentChanged}
            />
            <ClassicsContentQaPanel
                panelTitle="明代习俗问答对"
                contentId={entry.id}
                contentType="MING_CUSTOMS"
                onChanged={onContentChanged}
            />
        </>
    );
};
