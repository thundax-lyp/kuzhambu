import { useMemo } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDescriptions, KuzhambuExpandableText } from "@/components";
import { ClassicsContentQaAiPanel } from "@/pages/classics/common/classics-content-qa-ai-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/classics-content-qa-panel";
import { ClassicsContentTagAiPanel } from "@/pages/classics/common/classics-content-tag-ai-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/classics-content-tag-panel";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementTaskCapability,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import type { ClassicsContentQaTaskPair } from "@/pages/classics/common/classics-content-qa-ai-panel";
import type { MingCustomsRecord } from "@/pages/classics/ming-custom/ming-custom-types";
import "./ming-customs-refinement-section.css";

interface MingCustomsRefinementSectionProps {
    creatingRefinementCapability: AiRefinementTaskCapability | null;
    entry: MingCustomsRecord;
    refinementTasks: AiRefinementTaskRecord[];
    section: "tags" | "qa";
    onChanged: () => void | Promise<void>;
    onCreateTask: (
        capability: "tags" | "qa",
        context: { existingQaPairs?: ClassicsContentQaTaskPair[]; existingTags?: string[] }
    ) => void;
}

export const MingCustomsRefinementSection = ({
    creatingRefinementCapability,
    entry,
    refinementTasks,
    section,
    onChanged,
    onCreateTask
}: MingCustomsRefinementSectionProps) => {
    const tasks = useMemo(
        () =>
            refinementTasks.filter(
                (task) =>
                    aiRefinementTaskService.getNormalizedTaskCapability(task.capability) === section
            ),
        [refinementTasks, section]
    );
    const canApplyAiCandidates = hasPermission("classics:content:edit");
    const canCreateAiRefinementTask = hasPermission("ai:refinement:edit");
    const canRejectAiCandidates = hasPermission("ai:invocation:edit");
    const canViewAiCandidates = hasPermission("ai:invocation:view");

    const overview = (
        <KuzhambuDescriptions
            ariaLabel="明代习俗稿件基础信息"
            column={3}
            colon={false}
            size="small"
            items={[
                { key: "category", label: "分类", children: entry.category || "—" },
                { key: "chapter", label: "章节", children: entry.chapter || "—" },
                { key: "section", label: "小节", children: entry.section || "—" },
                { key: "title", label: "稿件", span: 3, children: entry.title || "未命名稿件" },
                {
                    key: "summary",
                    label: "概述",
                    span: 3,
                    children: (
                        <KuzhambuExpandableText
                            content={entry.summary || entry.content || "暂无概述"}
                            collapsedRows={2}
                        />
                    )
                }
            ]}
        />
    );

    return (
        <div className="ming-customs-refinement-section">
            {overview}
            {section === "tags" ? (
                <ClassicsContentTagPanel
                    contentId={entry.id}
                    contentType="MING_CUSTOMS"
                    panelTitle="标签"
                    toolbarExtra={
                        <ClassicsContentTagAiPanel
                            canApplyCandidate={canApplyAiCandidates}
                            canCreateTask={canCreateAiRefinementTask}
                            canRejectCandidate={canRejectAiCandidates}
                            canViewCandidate={canViewAiCandidates}
                            contentId={entry.id}
                            contentType="MING_CUSTOMS"
                            creatingTask={creatingRefinementCapability === "tags"}
                            tagTasks={tasks}
                            onChanged={onChanged}
                            onCreateTask={(existingTags) => onCreateTask("tags", { existingTags })}
                        />
                    }
                    onChanged={onChanged}
                />
            ) : (
                <ClassicsContentQaPanel
                    contentId={entry.id}
                    contentType="MING_CUSTOMS"
                    panelTitle="明代习俗问答对"
                    toolbarExtra={
                        <ClassicsContentQaAiPanel
                            canApplyCandidate={canApplyAiCandidates}
                            canCreateTask={canCreateAiRefinementTask}
                            canViewCandidate={canViewAiCandidates}
                            contentId={entry.id}
                            contentType="MING_CUSTOMS"
                            creatingTask={creatingRefinementCapability === "qa"}
                            qaTasks={tasks}
                            onChanged={onChanged}
                            onCreateTask={(existingQaPairs) =>
                                onCreateTask("qa", { existingQaPairs })
                            }
                        />
                    }
                    onChanged={onChanged}
                />
            )}
        </div>
    );
};
