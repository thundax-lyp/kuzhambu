import { FileTextOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Input, Tag, Typography } from "antd";
import { useCallback, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuSyncTaskModal,
    type KuzhambuSyncTaskAdapter,
    type KuzhambuSyncTaskModalState
} from "@/components";

import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import { AiCandidatePayloadEditor } from "@/pages/classics/common/components/ai-candidate-payload-editor";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import * as contentService from "@/pages/classics/common/classics-content-service";
import type { ClassicsContentTagRecord } from "@/pages/classics/common/classics-content-types";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";
import "./wangqi-tag-ai-modal.css";

const { Text } = Typography;

const TAG_CANDIDATE_POLL_INTERVAL_MS = 3000;
const TAG_AI_MODAL_TEST_ID = "classics-wangqi-document-tags-ai-modal";

interface WangqiTagAiModalProps {
    creatingTagTask?: boolean;
    document: WangqiDocumentRecord;
    onChanged: () => Promise<void> | void;
    onCreateTagTask?: (existingTags: string[]) => void;
    onTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    tagTasks?: AiRefinementTaskRecord[];
    tagTrackingTask?: AiRefinementTaskRecord | null;
}

const TAG_TASK_STATUS_LABELS: Record<string, string> = {
    PENDING: "排队中",
    RUNNING: "运行中",
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

const sortTasksByNewest = (left: AiRefinementTaskRecord, right: AiRefinementTaskRecord) => {
    if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
        return right.requestedAt.localeCompare(left.requestedAt);
    }
    return right.taskId - left.taskId;
};

const isTagTaskActive = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "PENDING" || task?.status === "RUNNING";
};

const isTagTaskCompleted = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "SUCCEEDED" || task?.status === "PARTIAL";
};

const getTagTaskDescription = (task: AiRefinementTaskRecord) => {
    const failureText = aiRefinementTaskService.getTaskFailureText(
        task.failureStage,
        task.errorType,
        task.errorMessage
    );
    if (failureText) {
        return failureText;
    }
    if (isTagTaskCompleted(task)) {
        if (!task.candidateId) {
            return "任务已完成，正在等待候选标签落库并回填。";
        }
        return "任务完成后会刷新候选标签，确认后可采用。";
    }
    if (isTagTaskActive(task)) {
        return "任务执行期间会持续刷新状态，完成后回填候选标签。";
    }
    return "可重新生成标签任务。";
};

const selectLatestTagCandidate = (
    candidates: AiCandidateRecord[] | undefined,
    trackedCandidateId?: number | null
) => {
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                candidate.capability === "tags" &&
                candidate.status === "PENDING" &&
                (!trackedCandidateId || candidate.candidateId === trackedCandidateId) &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) => {
            if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
                return right.requestedAt.localeCompare(left.requestedAt);
            }
            return right.candidateId - left.candidateId;
        })[0];
};

const defaultResultFormatForTags = (candidate?: AiCandidateRecord) => {
    return candidate?.resultFormat?.trim() || "STRUCTURED";
};

const tagTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => aiRefinementTaskService.getTaskStableId(task.taskId, task.taskIdText),
    getMessage: getTagTaskDescription,
    getPhase: (task) => {
        if (isTagTaskActive(task)) {
            return "tracking";
        }
        if (isTagTaskCompleted(task)) {
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
        return `标签任务${statusLabel}`;
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
                title={`标签任务${statusLabel}`}
                description={getTagTaskDescription(task)}
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

const readTagName = (tag: ClassicsContentTagRecord) => {
    return tag.tagNameSnapshot?.trim() || "-";
};

const readVisibleTagNames = (tags: ClassicsContentTagRecord[]) => {
    return tags.map(readTagName).filter((tagName) => tagName && tagName !== "-");
};

const getActiveTags = (tags: ClassicsContentTagRecord[] | unknown) => {
    return (Array.isArray(tags) ? tags : []).filter(
        (tag) => (tag.status || "ACTIVE") !== "REMOVED"
    );
};

export const WangqiTagAiModal = ({
    creatingTagTask = false,
    document,
    onChanged,
    onCreateTagTask,
    onTaskChange,
    tagTasks = [],
    tagTrackingTask
}: WangqiTagAiModalProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [isOpen, setIsOpen] = useState(false);
    const [candidatePayloads, setCandidatePayloads] = useState<Record<number, string>>({});
    const [candidateSubmitEnabled, setCandidateSubmitEnabled] = useState<Record<number, boolean>>(
        {}
    );
    const tagTrackingTaskId = tagTrackingTask?.taskId;
    const currentTagsQuery = useQuery({
        queryKey: ["classics", "content", "tags", "WANGQI_DOCUMENT", document.id],
        queryFn: () =>
            contentService.listTags({
                contentId: document.id,
                contentType: "WANGQI_DOCUMENT"
            }),
        enabled: isOpen && Boolean(document.id),
        retry: false
    });
    const currentTags = useMemo(
        () => getActiveTags(currentTagsQuery.data),
        [currentTagsQuery.data]
    );
    const currentTagNames = useMemo(() => readVisibleTagNames(currentTags), [currentTags]);

    const latestTagTaskFromList = useMemo(() => {
        return [...tagTasks]
            .filter((task) => task.capability === "tags")
            .sort(sortTasksByNewest)[0];
    }, [tagTasks]);
    const trackedTagTaskFromList = useMemo(() => {
        return tagTasks.find((task) => task.taskId === tagTrackingTaskId);
    }, [tagTasks, tagTrackingTaskId]);
    const latestTagTask = trackedTagTaskFromList || tagTrackingTask || latestTagTaskFromList;

    const applyMutation = useMutation({
        mutationFn: aiCandidateService.apply,
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", document.id]
                }),
                queryClient.removeQueries({
                    queryKey: [
                        "sync-task-modal",
                        TAG_AI_MODAL_TEST_ID,
                        "result",
                        "WANGQI_DOCUMENT",
                        document.id,
                        "tags"
                    ]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "content", "tags", "WANGQI_DOCUMENT", document.id]
                })
            ]);
            await onChanged();
            setIsOpen(false);
            messageApi.success("候选标签已采用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "采用标签失败");
        }
    });

    const openModal = () => {
        setCandidatePayloads({});
        setCandidateSubmitEnabled({});
        setIsOpen(true);
    };

    const createTagTask = () => {
        if (!onCreateTagTask) {
            messageApi.warning("请先保存王圻文档后再使用 AI 标签");
            return;
        }
        onCreateTagTask(currentTagNames);
    };

    const loadTagCandidate = async (task: AiRefinementTaskRecord | null) => {
        const trackedCandidateId = task?.candidateId ?? null;
        if (tagTrackingTaskId && !trackedCandidateId) {
            return null;
        }
        const candidates = await aiCandidateService.list({
            contentId: document.id,
            contentType: "WANGQI_DOCUMENT",
            capability: "tags",
            status: "PENDING"
        });
        return selectLatestTagCandidate(candidates, trackedCandidateId) ?? null;
    };

    const updateCandidatePayload = useCallback((candidateId: number, payload: string) => {
        setCandidatePayloads((currentPayloads) => {
            if (currentPayloads[candidateId] === payload) {
                return currentPayloads;
            }
            return {
                ...currentPayloads,
                [candidateId]: payload
            };
        });
    }, []);

    const updateCandidateSubmitEnabled = useCallback((candidateId: number, canSubmit: boolean) => {
        setCandidateSubmitEnabled((currentSubmitEnabled) => {
            if ((currentSubmitEnabled[candidateId] ?? false) === canSubmit) {
                return currentSubmitEnabled;
            }
            return {
                ...currentSubmitEnabled,
                [candidateId]: canSubmit
            };
        });
    }, []);

    const applyCandidate = (candidate: AiCandidateRecord | null) => {
        if (!candidate) {
            messageApi.warning("暂无可采用的候选标签");
            return;
        }
        const payload =
            candidatePayloads[candidate.candidateId] || candidate.resultPayload?.trim() || "";
        if (!payload || !candidateSubmitEnabled[candidate.candidateId]) {
            messageApi.warning("请先确认候选标签内容");
            return;
        }
        applyMutation.mutate({
            candidateId: candidate.candidateIdText || String(candidate.candidateId),
            contentId: document.id,
            contentType: "WANGQI_DOCUMENT",
            capability: "tags",
            objectId: candidate.objectId,
            resultFormat: defaultResultFormatForTags(candidate),
            resultPayload: payload,
            changeSummary: "AI 应用：tags"
        });
    };

    return (
        <>
            <KuzhambuButton
                testId="classics-wangqi-document-tags-ai-button"
                type="primary"
                icon={<FileTextOutlined />}
                onClick={openModal}
            >
                生成标签
            </KuzhambuButton>
            <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
                testId={TAG_AI_MODAL_TEST_ID}
                className="wangqi-tags-modal"
                title="AI 标签"
                open={isOpen}
                width={880}
                applying={applyMutation.isPending}
                applyDisabled={({ creating, result, resultLoading, tracking }) =>
                    creating ||
                    tracking ||
                    resultLoading ||
                    !result ||
                    !candidateSubmitEnabled[result.candidateId]
                }
                applyTestId="classics-wangqi-document-tags-ai-apply-button"
                createAriaLabel="生成 AI 标签"
                createIcon={<FileTextOutlined />}
                createTestId="classics-wangqi-document-tags-ai-generate-button"
                createText="生成标签"
                creating={creatingTagTask}
                onCancel={() => setIsOpen(false)}
                workflow={{
                    ...tagTaskAdapter,
                    task: latestTagTask,
                    createTask: createTagTask,
                    fetchResult: loadTagCandidate,
                    fetchTask: (taskId) => aiRefinementTaskService.getTask({ taskId }),
                    applyResult: applyCandidate,
                    onTaskChange,
                    pollIntervalMs: TAG_CANDIDATE_POLL_INTERVAL_MS,
                    resultQueryKey: ["WANGQI_DOCUMENT", document.id, "tags"],
                    trackTask: Boolean(tagTrackingTaskId)
                }}
                renderStatus={renderTagTaskStatus}
                renderBody={({ creating, result, resultLoading, tracking }) => (
                    <>
                        <div className="wangqi-summary-modal-compare-grid">
                            <section className="wangqi-summary-modal-card" aria-label="当前标签">
                                <Text strong>当前标签</Text>
                                {currentTagNames.length ? (
                                    <KuzhambuSpace wrap size="small">
                                        {currentTagNames.map((tagName) => (
                                            <Tag key={tagName}>{tagName}</Tag>
                                        ))}
                                    </KuzhambuSpace>
                                ) : (
                                    <Empty
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        description="暂无当前标签"
                                    />
                                )}
                            </section>
                            <section className="wangqi-summary-modal-card" aria-label="AI候选标签">
                                <Text strong>AI 标签</Text>
                                {result ? (
                                    <AiCandidatePayloadEditor
                                        key={`${result.candidateId}-${result.resultPayload ?? ""}`}
                                        candidateId={result.candidateId}
                                        capability="tags"
                                        initialPayload={result.resultPayload}
                                        disabled={creating || tracking || resultLoading}
                                        onPayloadChange={updateCandidatePayload}
                                        onSubmitEnabledChange={updateCandidateSubmitEnabled}
                                    />
                                ) : (
                                    <Empty
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        description={
                                            creating || resultLoading
                                                ? "AI 标签生成中"
                                                : "暂无候选标签，可先点击生成标签"
                                        }
                                    />
                                )}
                            </section>
                        </div>
                        <section className="wangqi-summary-modal-card" aria-label="AI标签生成依据">
                            <Text strong>生成依据</Text>
                            <div className="wangqi-tags-modal-source-grid">
                                <label>
                                    <Text type="secondary">标题</Text>
                                    <Input
                                        aria-label="AI标签依据标题"
                                        readOnly
                                        value={document.title || "未命名文档"}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">摘要</Text>
                                    <Input.TextArea
                                        aria-label="AI标签依据摘要"
                                        readOnly
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 2,
                                            maxRows: 4
                                        })}
                                        value={document.summary || "暂无摘要"}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">正文</Text>
                                    <Input.TextArea
                                        aria-label="AI标签依据正文"
                                        readOnly
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 4,
                                            maxRows: 8
                                        })}
                                        value={document.content || "暂无正文"}
                                    />
                                </label>
                                <div aria-label="AI标签依据已有标签">
                                    <Text type="secondary">已有标签</Text>
                                    {currentTagNames.length ? (
                                        <KuzhambuSpace wrap size="small">
                                            {currentTagNames.map((tagName) => (
                                                <Tag key={tagName}>{tagName}</Tag>
                                            ))}
                                        </KuzhambuSpace>
                                    ) : (
                                        <Text type="secondary">暂无已有标签</Text>
                                    )}
                                </div>
                            </div>
                        </section>
                    </>
                )}
            />
        </>
    );
};
