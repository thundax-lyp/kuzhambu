import { FileTextOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Input, Typography } from "antd";
import { useCallback, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { isSameId, normalizeId, normalizeNullableId } from "@/types/id";
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
import type { ClassicsContentQaPairRecord } from "@/pages/classics/common/classics-content-types";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";
import "./wangqi-qa-ai-modal.css";

const { Text } = Typography;

const QA_CANDIDATE_POLL_INTERVAL_MS = 3000;
const QA_AI_MODAL_TEST_ID = "classics-wangqi-document-qa-ai-modal";

interface WangqiQaAiModalProps {
    creatingQaTask?: boolean;
    document: WangqiDocumentRecord;
    onChanged: () => Promise<void> | void;
    onCreateQaTask?: (existingQaPairs: WangqiQaTaskPair[]) => void;
    onTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    qaTasks?: AiRefinementTaskRecord[];
    qaTrackingTask?: AiRefinementTaskRecord | null;
}

export interface WangqiQaTaskPair {
    answer: string;
    question: string;
}

const QA_TASK_STATUS_LABELS: Record<string, string> = {
    PENDING: "排队中",
    RUNNING: "运行中",
    SUCCEEDED: "已完成",
    PARTIAL: "部分完成",
    FAILED: "失败",
    CANCELLED: "已取消"
};

const QA_TASK_ALERT_TYPES: Record<string, "success" | "info" | "warning" | "error"> = {
    PENDING: "info",
    RUNNING: "info",
    SUCCEEDED: "success",
    PARTIAL: "warning",
    FAILED: "error",
    CANCELLED: "warning"
};

const sortTasksByNewest = (left: AiRefinementTaskRecord, right: AiRefinementTaskRecord) => {
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

const isQaTaskActive = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "PENDING" || task?.status === "RUNNING";
};

const isQaTaskCompleted = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "SUCCEEDED" || task?.status === "PARTIAL";
};

const getQaTaskDescription = (task: AiRefinementTaskRecord) => {
    const failureText = aiRefinementTaskService.getTaskFailureText(
        task.failureStage,
        task.errorType,
        task.errorMessage
    );
    if (failureText) {
        return failureText;
    }
    if (isQaTaskCompleted(task)) {
        if (!task.candidateId) {
            return "任务已完成，正在等待候选问答落库并回填。";
        }
        return "任务完成后会刷新候选问答，确认后可采用。";
    }
    if (isQaTaskActive(task)) {
        return "任务执行期间会持续刷新状态，完成后回填候选问答。";
    }
    return "可重新生成问答任务。";
};

const selectLatestQaCandidate = (
    candidates: AiCandidateRecord[] | undefined,
    trackedCandidateId?: string | null
) => {
    const normalizedTrackedCandidateId = normalizeNullableId(trackedCandidateId);
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                    "qa" &&
                candidate.status === "PENDING" &&
                (!normalizedTrackedCandidateId ||
                    isSameId(candidate.candidateId, normalizedTrackedCandidateId)) &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) => {
            return aiRefinementTaskService.sortNewestByRequestedAtThenId({
                left: {
                    id: left.candidateIdText || left.candidateId,
                    requestedAt: left.requestedAt
                },
                right: {
                    id: right.candidateIdText || right.candidateId,
                    requestedAt: right.requestedAt
                }
            });
        })[0];
};

const defaultResultFormatForQa = (candidate?: AiCandidateRecord) => {
    return candidate?.resultFormat?.trim() || "STRUCTURED";
};

const qaTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => aiRefinementTaskService.getTaskStableId(task.taskId, task.taskIdText),
    getMessage: getQaTaskDescription,
    getPhase: (task) => {
        if (isQaTaskActive(task)) {
            return "tracking";
        }
        if (isQaTaskCompleted(task)) {
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
    getResultKey: (task) => normalizeId(task.candidateId),
    getStatusLabel: (task) => {
        const statusLabel = QA_TASK_STATUS_LABELS[task.status] || task.status;
        return `问答任务${statusLabel}`;
    }
};

const renderQaTaskStatus = ({
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
                title="正在创建问答任务"
                description="任务创建成功后会自动进入状态跟踪。"
            />
        );
    } else if (task) {
        const statusLabel = QA_TASK_STATUS_LABELS[task.status] || task.status;
        taskAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type={QA_TASK_ALERT_TYPES[task.status] || "info"}
                title={`问答任务${statusLabel}`}
                description={getQaTaskDescription(task)}
            />
        );
    } else if (resultLoading) {
        taskAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type="info"
                title="正在加载候选问答"
                description="任务完成后会自动刷新候选问答。"
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
                title="候选问答暂未返回"
                description="生成任务仍在跟踪中，系统会继续刷新候选问答。"
            />
        );
    } else if (resultError) {
        resultAlert = (
            <KuzhambuAlert
                showIcon
                className="kuzhambu-sync-task-modal-status"
                type="warning"
                title="候选问答加载失败"
                description="请稍后重试加载候选问答。"
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

const readVisibleQaPairs = (qaPairs: ClassicsContentQaPairRecord[] | unknown) => {
    return (Array.isArray(qaPairs) ? qaPairs : [])
        .map((pair) => ({
            question: pair.question?.trim() || "",
            answer: pair.answer?.trim() || ""
        }))
        .filter((pair) => pair.question || pair.answer);
};

export const WangqiQaAiModal = ({
    creatingQaTask = false,
    document,
    onChanged,
    onCreateQaTask,
    onTaskChange,
    qaTasks = [],
    qaTrackingTask
}: WangqiQaAiModalProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [isOpen, setIsOpen] = useState(false);
    const [candidatePayloads, setCandidatePayloads] = useState<Record<string, string>>({});
    const [candidateSubmitEnabled, setCandidateSubmitEnabled] = useState<Record<string, boolean>>(
        {}
    );
    const qaTrackingTaskId = qaTrackingTask?.taskId;
    const currentQaPairsQuery = useQuery({
        queryKey: ["classics", "content", "qa-pairs", "WANGQI_DOCUMENT", document.id],
        queryFn: () =>
            contentService.listQaPairs({
                contentId: document.id,
                contentType: "WANGQI_DOCUMENT"
            }),
        enabled: isOpen && Boolean(document.id),
        retry: false
    });
    const currentQaPairs = useMemo(
        () => readVisibleQaPairs(currentQaPairsQuery.data),
        [currentQaPairsQuery.data]
    );

    const latestQaTaskFromList = useMemo(() => {
        return [...qaTasks]
            .filter(
                (task) =>
                    aiRefinementTaskService.getNormalizedTaskCapability(task.capability) === "qa"
            )
            .sort(sortTasksByNewest)[0];
    }, [qaTasks]);
    const trackedQaTaskFromList = useMemo(() => {
        return qaTasks.find((task) => isSameId(task.taskId, qaTrackingTaskId));
    }, [qaTasks, qaTrackingTaskId]);

    const latestQaTask = trackedQaTaskFromList || qaTrackingTask || latestQaTaskFromList;

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
                        QA_AI_MODAL_TEST_ID,
                        "result",
                        "WANGQI_DOCUMENT",
                        document.id,
                        "qa"
                    ]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "content", "qa-pairs", "WANGQI_DOCUMENT", document.id]
                })
            ]);
            await onChanged();
            setIsOpen(false);
            messageApi.success("候选问答已采用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "采用问答失败");
        }
    });

    const openModal = () => {
        setCandidatePayloads({});
        setCandidateSubmitEnabled({});
        setIsOpen(true);
    };

    const createQaTask = () => {
        if (!onCreateQaTask) {
            messageApi.warning("请先保存王圻文档后再生成问答");
            return;
        }
        onCreateQaTask(currentQaPairs);
    };

    const loadQaCandidate = async (task: AiRefinementTaskRecord | null) => {
        const trackedCandidateId = task?.candidateId ?? null;
        if (qaTrackingTaskId && !trackedCandidateId) {
            return null;
        }
        const candidates = await aiCandidateService.list({
            contentId: document.id,
            contentType: "WANGQI_DOCUMENT",
            capability: "qa",
            status: "PENDING"
        });
        return selectLatestQaCandidate(candidates, trackedCandidateId) ?? null;
    };

    const updateCandidatePayload = useCallback((candidateId: string, payload: string) => {
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

    const updateCandidateSubmitEnabled = useCallback((candidateId: string, canSubmit: boolean) => {
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
            messageApi.warning("暂无可采用的候选问答");
            return;
        }
        const candidateId = normalizeId(candidate.candidateId);
        const payload = candidatePayloads[candidateId] || candidate.resultPayload?.trim() || "";
        if (!payload || !candidateSubmitEnabled[candidateId]) {
            messageApi.warning("请先确认候选问答内容");
            return;
        }
        applyMutation.mutate({
            candidateId: candidate.candidateIdText || normalizeId(candidate.candidateId),
            contentId: document.id,
            contentType: "WANGQI_DOCUMENT",
            capability: "qa",
            objectId: candidate.objectId,
            resultFormat: defaultResultFormatForQa(candidate),
            resultPayload: payload,
            changeSummary: "AI 应用：qa"
        });
    };

    return (
        <>
            <KuzhambuButton
                testId="classics-wangqi-document-qa-ai-button"
                type="primary"
                icon={<FileTextOutlined />}
                onClick={openModal}
            >
                生成问答
            </KuzhambuButton>
            <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
                testId={QA_AI_MODAL_TEST_ID}
                className="wangqi-qa-modal"
                title="问答生成"
                open={isOpen}
                width={880}
                applying={applyMutation.isPending}
                applyDisabled={({ creating, result, resultLoading, tracking }) =>
                    creating ||
                    tracking ||
                    resultLoading ||
                    !result ||
                    !candidateSubmitEnabled[normalizeId(result.candidateId)]
                }
                applyTestId="classics-wangqi-document-qa-ai-apply-button"
                createAriaLabel="生成问答"
                createIcon={<FileTextOutlined />}
                createTestId="classics-wangqi-document-qa-ai-generate-button"
                createText="生成问答"
                creating={creatingQaTask}
                onCancel={() => setIsOpen(false)}
                workflow={{
                    ...qaTaskAdapter,
                    task: latestQaTask,
                    createTask: createQaTask,
                    fetchResult: loadQaCandidate,
                    fetchTask: (taskId) => aiRefinementTaskService.getTask({ taskId }),
                    applyResult: applyCandidate,
                    onTaskChange,
                    pollIntervalMs: QA_CANDIDATE_POLL_INTERVAL_MS,
                    resultQueryKey: ["WANGQI_DOCUMENT", document.id, "qa"],
                    trackTask: Boolean(qaTrackingTaskId)
                }}
                renderStatus={renderQaTaskStatus}
                renderBody={({ creating, result, resultLoading, tracking }) => (
                    <>
                        <div className="wangqi-summary-modal-compare-grid">
                            <section className="wangqi-summary-modal-card" aria-label="当前问答">
                                <Text strong>当前问答</Text>
                                {currentQaPairs.length ? (
                                    <KuzhambuSpace orientation="vertical" size="small">
                                        {currentQaPairs.map((pair, index) => (
                                            <Text key={`${pair.question}-${index}`}>
                                                Q: {pair.question || "-"}；A: {pair.answer || "-"}
                                            </Text>
                                        ))}
                                    </KuzhambuSpace>
                                ) : (
                                    <Empty
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        description="暂无当前问答"
                                    />
                                )}
                            </section>
                            <section className="wangqi-summary-modal-card" aria-label="候选问答">
                                <Text strong>候选问答</Text>
                                {result ? (
                                    <AiCandidatePayloadEditor
                                        key={`${result.candidateId}-${result.resultPayload ?? ""}`}
                                        candidateId={normalizeId(result.candidateId)}
                                        capability="qa"
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
                                                ? "问答生成中"
                                                : "暂无候选问答，可先点击生成问答"
                                        }
                                    />
                                )}
                            </section>
                        </div>
                        <section className="wangqi-summary-modal-card" aria-label="问答生成依据">
                            <Text strong>生成依据</Text>
                            <div className="wangqi-tags-modal-source-grid">
                                <label>
                                    <Text type="secondary">标题</Text>
                                    <Input
                                        aria-label="问答依据标题"
                                        readOnly
                                        value={document.title || "未命名文档"}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">摘要</Text>
                                    <Input.TextArea
                                        aria-label="问答依据摘要"
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
                                        aria-label="问答依据正文"
                                        readOnly
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 4,
                                            maxRows: 8
                                        })}
                                        value={document.content || "暂无正文"}
                                    />
                                </label>
                                <div aria-label="问答依据已有问答">
                                    <Text type="secondary">已有问答</Text>
                                    {currentQaPairs.length ? (
                                        <KuzhambuSpace orientation="vertical" size="small">
                                            {currentQaPairs.map((pair, index) => (
                                                <Text key={`${pair.question}-${index}`}>
                                                    Q: {pair.question || "-"}；A:{" "}
                                                    {pair.answer || "-"}
                                                </Text>
                                            ))}
                                        </KuzhambuSpace>
                                    ) : (
                                        <Text type="secondary">暂无已有问答</Text>
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
