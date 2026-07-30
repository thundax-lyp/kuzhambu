import { RobotOutlined } from "@ant-design/icons";
import { App, Empty } from "antd";
import { useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuSpace,
    KuzhambuSyncTaskModal,
    type KuzhambuSyncTaskAdapter,
    type KuzhambuSyncTaskModalState,
    KuzhambuTag
} from "@/components";

import * as aiCandidateService from "./ai-candidate-service";
import * as contentService from "./classics-content-service";
import * as aiRefinementTaskService from "./ai-refinement-task-service";
import type { AiCandidateRecord } from "./ai-candidate-types";
import type { AiRefinementTaskRecord } from "./ai-refinement-task-types";
import type { ClassicsContentType } from "./classics-content-types";

interface ClassicsContentTagAiPanelProps {
    canApplyCandidate?: boolean;
    canCreateTask?: boolean;
    canRejectCandidate?: boolean;
    canViewCandidate?: boolean;
    contentId: string;
    contentType: ClassicsContentType;
    creatingTask?: boolean;
    onChanged?: () => void | Promise<void>;
    onCreateTask?: () => void;
    onTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    tagTasks?: AiRefinementTaskRecord[];
}

const REJECT_ERROR_TYPE = "USER_REJECTED";
const REJECT_ERROR_MESSAGE = "用户已放弃该 AI 标签候选";
const TAG_TASK_POLL_INTERVAL_MS = 3000;
const TAG_TASK_LABEL = "标签";
const TAG_AI_MODAL_TEST_ID = "classics-content-tag-ai-modal";

const TAG_TASK_STATUS_LABELS: Record<string, string> = {
    PENDING: "等待中",
    RUNNING: "处理中",
    SUCCEEDED: "已完成",
    PARTIAL: "部分完成",
    FAILED: "失败",
    CANCELLED: "已取消"
};

const TAG_TASK_ALERT_TYPES: Record<string, "success" | "info" | "warning" | "error"> = {
    PENDING: "info",
    RUNNING: "info",
    SUCCEEDED: "success",
    PARTIAL: "warning",
    FAILED: "error",
    CANCELLED: "warning"
};

const normalizeTagName = (value?: string | null) => value?.trim() || "";

const uniqueTagNames = (values: Array<string | null | undefined>) => {
    const seen = new Set<string>();
    return values.map(normalizeTagName).filter((value) => {
        if (!value) {
            return false;
        }
        const key = value.toLocaleLowerCase();
        if (seen.has(key)) {
            return false;
        }
        seen.add(key);
        return true;
    });
};

const parseCandidateTags = (payload?: string | null) => {
    if (!payload?.trim()) {
        return [];
    }
    try {
        const parsed = JSON.parse(payload);
        if (Array.isArray(parsed)) {
            return uniqueTagNames(parsed.map((tag) => String(tag ?? "")));
        }
        if (Array.isArray(parsed?.tags)) {
            return uniqueTagNames(parsed.tags.map((tag: unknown) => String(tag ?? "")));
        }
    } catch {
        return uniqueTagNames(payload.split(/\r?\n|,|，/));
    }
    return [];
};

const tagKey = (tagName: string) => tagName.toLocaleLowerCase();

const getCandidateStableId = (candidate: AiCandidateRecord) => {
    return candidate.candidateIdText || String(candidate.candidateId);
};

const sortRefinementTasksByNewest = (
    left: AiRefinementTaskRecord,
    right: AiRefinementTaskRecord
) => {
    return aiRefinementTaskService.sortNewestByRequestedAtThenId({
        left: {
            id: aiRefinementTaskService.getTaskStableId(left.taskId, left.taskIdText),
            requestedAt: left.requestedAt
        },
        right: {
            id: aiRefinementTaskService.getTaskStableId(right.taskId, right.taskIdText),
            requestedAt: right.requestedAt
        }
    });
};

const readRefinementTaskFailureText = (task?: AiRefinementTaskRecord | null) => {
    if (!task) {
        return undefined;
    }
    return (
        aiRefinementTaskService.getTaskFailureText(
            task.failureStage,
            task.errorType,
            task.errorMessage
        ) || undefined
    );
};

const tagTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => aiRefinementTaskService.getTaskStableId(task.taskId, task.taskIdText),
    getMessage: (task) => readRefinementTaskFailureText(task),
    getPhase: (task) => {
        if (task.status === "PENDING" || task.status === "RUNNING") {
            return "tracking";
        }
        if (task.status === "SUCCEEDED" || task.status === "PARTIAL") {
            return task.candidateId ? "result_ready" : "waiting_result";
        }
        if (task.status === "CANCELLED") {
            return "cancelled";
        }
        if (task.status === "FAILED") {
            return "failed";
        }
        return "tracking";
    },
    getResultKey: (task) => task.candidateId,
    getStatusLabel: (task) => {
        const statusLabel = TAG_TASK_STATUS_LABELS[task.status] || task.status;
        return `${TAG_TASK_LABEL}任务：${statusLabel}`;
    }
};

const renderTagTaskStatus = ({
    creating,
    resultError,
    resultLoading,
    task,
    tracking
}: KuzhambuSyncTaskModalState<AiRefinementTaskRecord, AiCandidateRecord>) => {
    let taskAlert: ReactNode = null;
    if (creating) {
        taskAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type="info"
                title="正在创建标签任务"
                description="任务创建成功后会自动进入状态跟踪。"
            />
        );
    } else if (task) {
        const statusLabel = TAG_TASK_STATUS_LABELS[task.status] || task.status;
        taskAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type={TAG_TASK_ALERT_TYPES[task.status] || "info"}
                title={`${TAG_TASK_LABEL}任务：${statusLabel}`}
                description={
                    tracking
                        ? "任务完成后会自动刷新 AI 标签。"
                        : readRefinementTaskFailureText(task)
                }
            />
        );
    } else if (resultLoading) {
        taskAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type="info"
                title="正在加载候选标签"
                description="任务完成后会自动刷新候选标签。"
            />
        );
    }

    let resultAlert: ReactNode = null;
    if (resultError && tracking) {
        resultAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type="info"
                title="候选标签暂未返回"
                description="AI 任务仍在跟踪中，系统会继续刷新候选标签。"
            />
        );
    } else if (resultError) {
        resultAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type="warning"
                title="候选标签加载失败"
                description="请稍后重试加载候选标签。"
            />
        );
    }

    return taskAlert || resultAlert ? (
        <>
            {taskAlert}
            {resultAlert}
        </>
    ) : null;
};

const selectLatestTagCandidate = (
    candidates: AiCandidateRecord[] | undefined,
    trackedCandidateId?: string | null
) => {
    const normalizedTrackedCandidateId = String(trackedCandidateId ?? "").trim();
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                candidate.status === "PENDING" &&
                aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                    "tags" &&
                (!normalizedTrackedCandidateId ||
                    String(candidate.candidateId) === normalizedTrackedCandidateId ||
                    candidate.candidateIdText === normalizedTrackedCandidateId) &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) =>
            aiRefinementTaskService.sortNewestByRequestedAtThenId({
                left: {
                    id: left.candidateIdText || left.candidateId,
                    requestedAt: left.requestedAt
                },
                right: {
                    id: right.candidateIdText || right.candidateId,
                    requestedAt: right.requestedAt
                }
            })
        )[0];
};

export const ClassicsContentTagAiPanel = ({
    canApplyCandidate = false,
    canCreateTask = true,
    canRejectCandidate = false,
    canViewCandidate = false,
    contentId,
    contentType,
    creatingTask = false,
    onChanged,
    onCreateTask,
    onTaskChange,
    tagTasks = []
}: ClassicsContentTagAiPanelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [open, setOpen] = useState(false);
    const [handlingCandidateId, setHandlingCandidateId] = useState<string | null>(null);

    const tagsQuery = useQuery({
        queryKey: ["classics", "content", "tags", contentType, contentId],
        queryFn: () => contentService.listTags({ contentId, contentType }),
        enabled: open && Boolean(contentId),
        retry: false
    });

    const tags = useMemo(
        () => (tagsQuery.data || []).filter((tag) => (tag.status || "ACTIVE") !== "REMOVED"),
        [tagsQuery.data]
    );
    const currentTagNames = useMemo(
        () => uniqueTagNames(tags.map((tag) => tag.tagNameSnapshot)),
        [tags]
    );
    const latestTagTask = useMemo(
        () =>
            [...tagTasks]
                .filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "tags"
                )
                .sort(sortRefinementTasksByNewest)[0] ?? null,
        [tagTasks]
    );

    const refresh = async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", contentType, contentId]
            }),
            queryClient.removeQueries({
                queryKey: [
                    "sync-task-modal",
                    TAG_AI_MODAL_TEST_ID,
                    "result",
                    contentType,
                    contentId
                ]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", contentType, contentId]
            })
        ]);
        if (onChanged) {
            await onChanged();
        }
    };

    const markCandidateApplied = async (
        candidate: AiCandidateRecord,
        appliedTags: string[],
        tagApplyMode?: "APPEND" | "COVER"
    ) => {
        if (!appliedTags.length) {
            await aiCandidateService.reject({
                candidateId: getCandidateStableId(candidate),
                errorType: "NO_TAG_CHANGE",
                errorMessage: "候选标签已存在，追加操作未产生新标签"
            });
            return;
        }
        await aiCandidateService.apply({
            candidateId: getCandidateStableId(candidate),
            contentId,
            contentType,
            capability: "tags",
            objectId: candidate.objectId,
            resultFormat: candidate.resultFormat || "STRUCTURED",
            resultPayload: JSON.stringify({ tags: appliedTags }),
            changeSummary: "AI 应用：标签",
            tagApplyMode
        });
    };

    const readCandidateTagsOrThrow = (candidate: AiCandidateRecord) => {
        const tags = parseCandidateTags(candidate.resultPayload);
        if (!tags.length) {
            throw new Error("AI 候选标签为空");
        }
        return tags;
    };

    const loadTagCandidate = async (task: AiRefinementTaskRecord | null) => {
        if (!canViewCandidate) {
            return null;
        }
        const candidates = await aiCandidateService.list({
            contentId,
            contentType,
            capability: "tags",
            status: "PENDING"
        });
        return selectLatestTagCandidate(candidates, task?.candidateId) ?? null;
    };

    const coverMutation = useMutation({
        mutationFn: async (candidate: AiCandidateRecord) => {
            setHandlingCandidateId(getCandidateStableId(candidate));
            const candidateTags = readCandidateTagsOrThrow(candidate);
            await markCandidateApplied(candidate, candidateTags, "COVER");
        },
        onSuccess: async () => {
            await refresh();
            messageApi.success("AI 标签已覆盖当前条目标签");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "覆盖 AI 标签失败");
        },
        onSettled: () => setHandlingCandidateId(null)
    });

    const appendMutation = useMutation({
        mutationFn: async (candidate: AiCandidateRecord) => {
            setHandlingCandidateId(getCandidateStableId(candidate));
            const existingTags = (tagsQuery.data || []).filter(
                (tag) => (tag.status || "ACTIVE") !== "REMOVED"
            );
            const existingByName = new Map(
                existingTags.map((tag) => [tagKey(normalizeTagName(tag.tagNameSnapshot)), tag])
            );
            const candidateTags = readCandidateTagsOrThrow(candidate);
            const candidateTagsToApply = candidateTags.filter(
                (tagName) => !existingByName.has(tagKey(tagName))
            );
            await markCandidateApplied(candidate, candidateTagsToApply, "APPEND");
        },
        onSuccess: async () => {
            await refresh();
            messageApi.success("AI 标签已追加到当前条目");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "追加 AI 标签失败");
        },
        onSettled: () => setHandlingCandidateId(null)
    });

    const discardMutation = useMutation({
        mutationFn: async (candidate: AiCandidateRecord) => {
            setHandlingCandidateId(getCandidateStableId(candidate));
            await aiCandidateService.reject({
                candidateId: getCandidateStableId(candidate),
                errorType: REJECT_ERROR_TYPE,
                errorMessage: REJECT_ERROR_MESSAGE
            });
        },
        onSuccess: async () => {
            await refresh();
            messageApi.success("AI 标签候选已放弃");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "放弃 AI 标签失败");
        },
        onSettled: () => setHandlingCandidateId(null)
    });
    const isCandidateMutationPending =
        appendMutation.isPending || coverMutation.isPending || discardMutation.isPending;

    return (
        <>
            <KuzhambuButton
                testId="classics-common-content-tag-ai-button"
                icon={<RobotOutlined />}
                onClick={() => setOpen(true)}
            >
                AI 生成
            </KuzhambuButton>
            <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
                testId={TAG_AI_MODAL_TEST_ID}
                className="classics-content-tag-ai-modal"
                open={open}
                title="AI标签"
                width={880}
                createIcon={<RobotOutlined />}
                createTestId="classics-content-tag-ai-create-task-button"
                createText="生成标签"
                createDisabled={!canCreateTask || !canViewCandidate}
                creating={creatingTask}
                onCancel={() => setOpen(false)}
                workflow={{
                    ...tagTaskAdapter,
                    task: latestTagTask,
                    createTask: () => onCreateTask?.(),
                    fetchResult: loadTagCandidate,
                    fetchTask: (taskId) => aiRefinementTaskService.getTask({ taskId }),
                    onTaskChange,
                    pollIntervalMs: TAG_TASK_POLL_INTERVAL_MS,
                    resultQueryKey: [contentType, contentId, "tags"],
                    trackTask: Boolean(latestTagTask?.taskId)
                }}
                renderStatus={renderTagTaskStatus}
                renderBody={({ creating, result, resultLoading, tracking }) => {
                    const candidateTags = result ? parseCandidateTags(result.resultPayload) : [];
                    const candidateKeys = new Set(candidateTags.map(tagKey));
                    const currentKeys = new Set(currentTagNames.map(tagKey));
                    const addedTagNames = candidateTags.filter(
                        (tagName) => !currentKeys.has(tagKey(tagName))
                    );
                    const removedTagNames = currentTagNames.filter(
                        (tagName) => !candidateKeys.has(tagKey(tagName))
                    );
                    const unchangedTagNames = candidateTags.filter((tagName) =>
                        currentKeys.has(tagKey(tagName))
                    );
                    const loading = result
                        ? handlingCandidateId === getCandidateStableId(result)
                        : false;
                    const isResultBusy = creating || tracking || resultLoading;
                    const areCandidateActionsDisabled =
                        !canApplyCandidate ||
                        !canViewCandidate ||
                        !candidateTags.length ||
                        isResultBusy ||
                        isCandidateMutationPending;
                    const isDiscardDisabled =
                        !canRejectCandidate ||
                        !canViewCandidate ||
                        isResultBusy ||
                        isCandidateMutationPending;

                    return (
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <div
                                style={{
                                    display: "grid",
                                    gap: 12,
                                    gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
                                    minWidth: 0,
                                    width: "100%"
                                }}
                            >
                                <KuzhambuCard size="small" title="当前标签" style={{ minWidth: 0 }}>
                                    {currentTagNames.length ? (
                                        <KuzhambuSpace wrap>
                                            {currentTagNames.map((tagName) => (
                                                <KuzhambuTag key={tagName} type="neutral">
                                                    {tagName}
                                                </KuzhambuTag>
                                            ))}
                                        </KuzhambuSpace>
                                    ) : (
                                        <Empty
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                            description="暂无当前标签"
                                        />
                                    )}
                                </KuzhambuCard>
                                <KuzhambuCard size="small" title="AI标签" style={{ minWidth: 0 }}>
                                    {result ? (
                                        <KuzhambuSpace wrap>
                                            {candidateTags.map((tagName) => (
                                                <KuzhambuTag key={tagName} type="accent">
                                                    {tagName}
                                                </KuzhambuTag>
                                            ))}
                                        </KuzhambuSpace>
                                    ) : (
                                        <Empty
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                            description={
                                                isResultBusy
                                                    ? "AI 标签生成中"
                                                    : "暂无候选标签，可先点击生成标签"
                                            }
                                        />
                                    )}
                                </KuzhambuCard>
                            </div>
                            <KuzhambuCard size="small" title="标签差异" style={{ width: "100%" }}>
                                {result ? (
                                    addedTagNames.length ||
                                    removedTagNames.length ||
                                    unchangedTagNames.length ? (
                                        <KuzhambuSpace orientation="vertical" size={8}>
                                            <KuzhambuSpace wrap>
                                                {addedTagNames.map((tagName) => (
                                                    <KuzhambuTag key={tagName} type="accent">
                                                        新增：{tagName}
                                                    </KuzhambuTag>
                                                ))}
                                                {removedTagNames.map((tagName) => (
                                                    <KuzhambuTag key={tagName} type="warning">
                                                        覆盖会移除：{tagName}
                                                    </KuzhambuTag>
                                                ))}
                                                {unchangedTagNames.map((tagName) => (
                                                    <KuzhambuTag key={tagName} type="neutral">
                                                        已存在：{tagName}
                                                    </KuzhambuTag>
                                                ))}
                                            </KuzhambuSpace>
                                            <KuzhambuSpace wrap>
                                                <KuzhambuButton
                                                    testId="classics-content-tag-ai-discard-button"
                                                    disabled={isDiscardDisabled}
                                                    loading={loading && discardMutation.isPending}
                                                    onClick={() => discardMutation.mutate(result)}
                                                >
                                                    放弃
                                                </KuzhambuButton>
                                                <KuzhambuButton
                                                    testId="classics-content-tag-ai-append-button"
                                                    disabled={areCandidateActionsDisabled}
                                                    loading={loading && appendMutation.isPending}
                                                    onClick={() => appendMutation.mutate(result)}
                                                >
                                                    追加
                                                </KuzhambuButton>
                                                <KuzhambuButton
                                                    testId="classics-content-tag-ai-cover-button"
                                                    disabled={areCandidateActionsDisabled}
                                                    loading={loading && coverMutation.isPending}
                                                    type="primary"
                                                    onClick={() => coverMutation.mutate(result)}
                                                >
                                                    覆盖
                                                </KuzhambuButton>
                                            </KuzhambuSpace>
                                        </KuzhambuSpace>
                                    ) : (
                                        <Empty
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                            description="当前标签与 AI 标签暂无差异"
                                        />
                                    )
                                ) : (
                                    <Empty
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        description="暂无可比较的 AI 标签"
                                    />
                                )}
                            </KuzhambuCard>
                        </KuzhambuSpace>
                    );
                }}
            />
        </>
    );
};
