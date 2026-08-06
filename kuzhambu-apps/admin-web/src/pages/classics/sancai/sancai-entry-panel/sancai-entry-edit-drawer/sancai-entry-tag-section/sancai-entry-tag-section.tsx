import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { forwardRef, useCallback, useMemo } from "react";
import { KuzhambuDescriptions, KuzhambuExpandableText } from "@/components";

import { hasPermission } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import {
    AI_BUSINESS_CAPABILITY,
    type AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import { ClassicsContentTagAiPanel } from "@/pages/classics/common/classics-content-tag-ai-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/classics-content-tag-panel";
import type { SancaiEntryRecord } from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-tag-section.css";

const TAG_TASK_POLL_INTERVAL_MS = 3000;

type TagTaskPage = {
    items?: AiRefinementTaskRecord[];
    pageNo?: number;
    pageSize?: number;
    total?: number;
};

interface SancaiEntryTagSectionProps {
    categoryTitle: string;
    entry: SancaiEntryRecord;
    readOnly: boolean;
    volumeTitle: string;
    onChanged: () => void | Promise<void>;
}

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无简介/摘要";
};

const isSameTaskRecord = (
    left: AiRefinementTaskRecord | undefined,
    right: AiRefinementTaskRecord
) => {
    if (!left) {
        return false;
    }
    const leftRecord = left as unknown as Record<string, unknown>;
    const rightRecord = right as unknown as Record<string, unknown>;
    const keys = new Set([...Object.keys(leftRecord), ...Object.keys(rightRecord)]);
    return [...keys].every((key) => leftRecord[key] === rightRecord[key]);
};

const buildTagInputPayloadJson = (entry: SancaiEntryRecord) => {
    const document = [entry.originalText, entry.translationText, entry.summary]
        .filter((value): value is string => Boolean(value?.trim()))
        .join("\n\n");
    return JSON.stringify({
        capability: AI_BUSINESS_CAPABILITY.CLASSICS_TAG_EXTRACT,
        contentId: entry.id,
        contentType: "SANCAI_ENTRY",
        document,
        bodyText: entry.originalText,
        existingSummary: entry.summary,
        objectId: null,
        originalText: entry.originalText,
        sourceText: entry.originalText,
        summary: entry.summary,
        title: entry.title,
        translationText: entry.translationText
    });
};

export const SancaiEntryTagSection = forwardRef<HTMLDivElement, SancaiEntryTagSectionProps>(
    ({ categoryTitle, entry, readOnly, volumeTitle, onChanged }, ref) => {
        const { message: messageApi } = App.useApp();
        const queryClient = useQueryClient();
        const canApplyTagAiCandidates = hasPermission("classics:content:edit");
        const canCreateAiRefinementTask = hasPermission("ai:refinement:edit");
        const canRejectAiCandidates = hasPermission("ai:invocation:edit");
        const canViewAiCandidates = hasPermission("ai:invocation:view");
        const tagTasksQuery = useQuery({
            queryKey: ["classics", "sancai", "refinement", "tasks", entry.id],
            queryFn: () =>
                aiRefinementTaskService.pageTasks({
                    contentType: "SANCAI_ENTRY",
                    contentId: entry.id,
                    pageNo: 1,
                    pageSize: 20
                }),
            retry: false,
            refetchInterval: (query) => {
                const tasks = query.state.data?.items || [];
                return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                    ? TAG_TASK_POLL_INTERVAL_MS
                    : false;
            }
        });
        const tagTasks = useMemo(
            () =>
                (tagTasksQuery.data?.items || []).filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "tags"
                ),
            [tagTasksQuery.data?.items]
        );
        const syncTagTask = useCallback(
            (task: AiRefinementTaskRecord | null) => {
                if (!task) {
                    return;
                }
                const normalizedTask = { ...task, capability: "tags" };
                queryClient.setQueryData<TagTaskPage>(
                    ["classics", "sancai", "refinement", "tasks", entry.id],
                    (currentPage) => {
                        const items = currentPage?.items || [];
                        const existingTask = items.find(
                            (item) => item.taskId === normalizedTask.taskId
                        );
                        if (isSameTaskRecord(existingTask, normalizedTask)) {
                            return currentPage;
                        }
                        return {
                            pageNo: currentPage?.pageNo ?? 1,
                            pageSize: currentPage?.pageSize ?? Math.max(items.length + 1, 20),
                            total: existingTask
                                ? currentPage?.total
                                : (currentPage?.total ?? items.length) + 1,
                            items: existingTask
                                ? items.map((item) =>
                                      item.taskId === normalizedTask.taskId
                                          ? { ...item, ...normalizedTask }
                                          : item
                                  )
                                : [normalizedTask, ...items]
                        };
                    }
                );
            },
            [entry.id, queryClient]
        );
        const createTagTaskMutation = useMutation({
            mutationFn: aiRefinementTaskService.createTask,
            onSuccess: async (acceptedTask) => {
                syncTagTask(acceptedTask);
                await tagTasksQuery.refetch();
                messageApi.success("标签任务已创建");
            },
            onError: (error) => {
                messageApi.error(error instanceof Error ? error.message : "AI 精修任务创建失败");
            }
        });
        const createTagTask = () => {
            if (!entry.originalText?.trim()) {
                messageApi.warning("当前条目缺少原文，无法创建 AI 精修任务");
                return;
            }
            createTagTaskMutation.mutate({
                capability: AI_BUSINESS_CAPABILITY.CLASSICS_TAG_EXTRACT,
                scope: "classics",
                contentType: "SANCAI_ENTRY",
                contentId: entry.id,
                objectId: null,
                requestId: createEventId("sancai-tag-task"),
                traceId: createEventId("sancai-tag-trace"),
                inputPayloadJson: buildTagInputPayloadJson(entry),
                locale: "zh-CN"
            });
        };
        const handleTagTaskChange = useCallback(
            (task: AiRefinementTaskRecord | null) => {
                syncTagTask(task);
            },
            [syncTagTask]
        );

        return (
            <div ref={ref} className="sancai-candidate-panel-anchor" tabIndex={-1}>
                <div className="sancai-tag-section">
                    <KuzhambuDescriptions
                        ariaLabel="三才图会条目基础信息"
                        className="sancai-detail-card sancai-tag-section-basic"
                        column={3}
                        colon={false}
                        size="small"
                        items={[
                            {
                                key: "category",
                                label: "门类",
                                children: categoryTitle
                            },
                            {
                                key: "volume",
                                label: "卷目",
                                children: volumeTitle
                            },
                            {
                                key: "title",
                                label: "标题",
                                children: readEntryTitle(entry)
                            },
                            {
                                key: "summary",
                                label: "摘要",
                                span: 3,
                                children: (
                                    <KuzhambuExpandableText
                                        className="sancai-tag-section-summary-text"
                                        content={readEntrySummary(entry)}
                                        collapsedRows={2}
                                    />
                                )
                            }
                        ]}
                    />
                    <ClassicsContentTagPanel
                        contentId={entry.id}
                        contentType="SANCAI_ENTRY"
                        panelTitle="标签"
                        readOnly={readOnly}
                        toolbarExtra={
                            readOnly ? undefined : (
                                <ClassicsContentTagAiPanel
                                    canApplyCandidate={canApplyTagAiCandidates}
                                    canCreateTask={canCreateAiRefinementTask}
                                    canRejectCandidate={canRejectAiCandidates}
                                    canViewCandidate={canViewAiCandidates}
                                    contentId={entry.id}
                                    contentType="SANCAI_ENTRY"
                                    creatingTask={createTagTaskMutation.isPending}
                                    tagTasks={tagTasks}
                                    onChanged={onChanged}
                                    onCreateTask={createTagTask}
                                    onTaskChange={handleTagTaskChange}
                                />
                            )
                        }
                        onChanged={onChanged}
                    />
                </div>
            </div>
        );
    }
);

SancaiEntryTagSection.displayName = "SancaiEntryTagSection";
