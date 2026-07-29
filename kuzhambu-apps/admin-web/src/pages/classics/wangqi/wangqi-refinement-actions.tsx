import { Tooltip } from "antd";
import { KuzhambuButton, KuzhambuSpaceCompact, KuzhambuCard } from "@/components";

import { AiCandidatePanel } from "@/pages/classics/common/ai-candidate-panel";
import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import { ClassicsContentQaPanel } from "@/pages/classics/common/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/classics-content-tag-panel";
import type { WangqiQaTaskPair } from "./wangqi-qa-ai-modal";
import { WangqiQaAiModal } from "./wangqi-qa-ai-modal";
import { WangqiTagAiModal } from "./wangqi-tag-ai-modal";
import type { WangqiDocumentRecord } from "./wangqi-types";
import "./wangqi-refinement-actions.css";

interface WangqiRefinementActionsProps {
    canOpenDiscoveryQa: boolean;
    creatingRefinementCapability: AiRefinementTaskCapability | null;
    document: WangqiDocumentRecord;
    qaTasks: AiRefinementTaskRecord[];
    qaTrackingTask: AiRefinementTaskRecord | null;
    section: "qa" | "tags";
    singleDocumentQaDisabledReason?: string;
    tagTasks: AiRefinementTaskRecord[];
    tagTrackingTask: AiRefinementTaskRecord | null;
    onChanged: () => void | Promise<void>;
    onCreateQaTask: (existingQaPairs: WangqiQaTaskPair[]) => void;
    onCreateTagTask: (existingTags: string[]) => void;
    onOpenSingleDocumentQa: (document: WangqiDocumentRecord) => void;
    onRejectedCandidate: () => void | Promise<void>;
    onQaTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    onTagTaskChange?: (task: AiRefinementTaskRecord | null) => void;
}

export const WangqiRefinementActions = ({
    canOpenDiscoveryQa,
    creatingRefinementCapability,
    document,
    qaTasks,
    qaTrackingTask,
    section,
    singleDocumentQaDisabledReason,
    tagTasks,
    tagTrackingTask,
    onChanged,
    onCreateQaTask,
    onCreateTagTask,
    onOpenSingleDocumentQa,
    onRejectedCandidate,
    onQaTaskChange,
    onTagTaskChange
}: WangqiRefinementActionsProps) => {
    if (section === "tags") {
        return (
            <div className="wangqi-page-drawer-panels">
                <ClassicsContentTagPanel
                    contentId={document.id}
                    contentType="WANGQI_DOCUMENT"
                    showHeader={false}
                    toolbarExtra={
                        <WangqiTagAiModal
                            creatingTagTask={creatingRefinementCapability === "tags"}
                            document={document}
                            tagTasks={tagTasks}
                            tagTrackingTask={tagTrackingTask}
                            onChanged={onChanged}
                            onCreateTagTask={onCreateTagTask}
                            onTaskChange={onTagTaskChange}
                        />
                    }
                    onChanged={onChanged}
                />
                <AiCandidatePanel
                    capabilities={["tags"]}
                    contentId={document.id}
                    contentType="WANGQI_DOCUMENT"
                    onApplied={onChanged}
                    onRejected={onRejectedCandidate}
                />
            </div>
        );
    }

    return (
        <div className="wangqi-page-drawer-panels">
            <KuzhambuCard
                size="small"
                title="问答生成"
                extra={
                    <KuzhambuSpaceCompact>
                        <Tooltip title={singleDocumentQaDisabledReason}>
                            <KuzhambuButton
                                testId="classics-wangqi-wangqi-action-button-2"
                                disabled={!document.id || !canOpenDiscoveryQa}
                                onClick={() => onOpenSingleDocumentQa(document)}
                            >
                                单文档问答
                            </KuzhambuButton>
                        </Tooltip>
                        <WangqiQaAiModal
                            creatingQaTask={creatingRefinementCapability === "qa"}
                            document={document}
                            qaTasks={qaTasks}
                            qaTrackingTask={qaTrackingTask}
                            onChanged={onChanged}
                            onCreateQaTask={onCreateQaTask}
                            onTaskChange={onQaTaskChange}
                        />
                    </KuzhambuSpaceCompact>
                }
            >
                <div className="wangqi-refinement-task-list">
                    {qaTasks.length ? (
                        qaTasks.slice(0, 3).map((task) => (
                            <div key={task.taskId}>
                                问答：{task.status}
                                {task.resultPreview ? ` · ${task.resultPreview}` : ""}
                            </div>
                        ))
                    ) : (
                        <div>暂无问答任务</div>
                    )}
                </div>
            </KuzhambuCard>
            <AiCandidatePanel
                capabilities={["qa"]}
                contentId={document.id}
                contentType="WANGQI_DOCUMENT"
                onApplied={onChanged}
                onRejected={onRejectedCandidate}
            />
            <ClassicsContentQaPanel
                panelTitle="王圻问答对"
                contentId={document.id}
                contentType="WANGQI_DOCUMENT"
                onChanged={onChanged}
            />
        </div>
    );
};
