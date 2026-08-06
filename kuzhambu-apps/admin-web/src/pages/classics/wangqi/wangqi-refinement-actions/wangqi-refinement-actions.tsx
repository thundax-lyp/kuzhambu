import { Tooltip } from "antd";
import { KuzhambuButton, KuzhambuSpaceCompact } from "@/components";
import { hasPermission } from "@/auth/permission-storage";

import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import {
    ClassicsContentQaAiPanel,
    type ClassicsContentQaTaskPair
} from "@/pages/classics/common/classics-content-qa-ai-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/classics-content-qa-panel";
import { ClassicsContentTagAiPanel } from "@/pages/classics/common/classics-content-tag-ai-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/classics-content-tag-panel";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";
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
    onCreateQaTask: (existingQaPairs: ClassicsContentQaTaskPair[]) => void;
    onCreateTagTask: (existingTags: string[]) => void;
    onOpenSingleDocumentQa: (document: WangqiDocumentRecord) => void;
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
    onQaTaskChange,
    onTagTaskChange
}: WangqiRefinementActionsProps) => {
    const canApplyAiCandidates = hasPermission("classics:content:edit");
    const canCreateAiRefinementTask = hasPermission("ai:refinement:edit");
    const canRejectAiCandidates = hasPermission("ai:invocation:edit");
    const canViewAiCandidates = hasPermission("ai:invocation:view");

    if (section === "tags") {
        return (
            <ClassicsContentTagPanel
                contentId={document.id}
                contentType="WANGQI_DOCUMENT"
                panelTitle="标签"
                toolbarExtra={
                    <ClassicsContentTagAiPanel
                        canApplyCandidate={canApplyAiCandidates}
                        canCreateTask={canCreateAiRefinementTask}
                        canRejectCandidate={canRejectAiCandidates}
                        canViewCandidate={canViewAiCandidates}
                        contentId={document.id}
                        contentType="WANGQI_DOCUMENT"
                        creatingTask={creatingRefinementCapability === "tags"}
                        tagTasks={tagTasks}
                        trackingTask={tagTrackingTask}
                        onChanged={onChanged}
                        onCreateTask={onCreateTagTask}
                        onTaskChange={onTagTaskChange}
                    />
                }
                onChanged={onChanged}
            />
        );
    }

    return (
        <ClassicsContentQaPanel
            contentId={document.id}
            contentType="WANGQI_DOCUMENT"
            panelTitle="王圻问答对"
            toolbarExtra={
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
                    <ClassicsContentQaAiPanel
                        canApplyCandidate={canApplyAiCandidates}
                        canCreateTask={canCreateAiRefinementTask}
                        canRejectCandidate={canRejectAiCandidates}
                        canViewCandidate={canViewAiCandidates}
                        contentId={document.id}
                        contentType="WANGQI_DOCUMENT"
                        creatingTask={creatingRefinementCapability === "qa"}
                        qaTasks={qaTasks}
                        trackingTask={qaTrackingTask}
                        onChanged={onChanged}
                        onCreateTask={onCreateQaTask}
                        onTaskChange={onQaTaskChange}
                    />
                </KuzhambuSpaceCompact>
            }
            onChanged={onChanged}
        />
    );
};
