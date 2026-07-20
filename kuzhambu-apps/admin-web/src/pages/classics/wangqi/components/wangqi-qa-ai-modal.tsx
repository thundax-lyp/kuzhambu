import { FileTextOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Input, Typography } from "antd";
import { useCallback, useMemo, useState } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import { AiCandidatePayloadEditor } from "@/pages/classics/common/components/ai-candidate-payload-editor";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import * as contentService from "@/pages/classics/common/classics-content-service";
import type { ClassicsContentQaPairRecord } from "@/pages/classics/common/classics-content-types";
import type { WangqiDocumentRecord } from "../wangqi-types";

const { Text } = Typography;

const QA_CANDIDATE_POLL_INTERVAL_MS = 3000;

type QaTaskAlertType = "success" | "info" | "warning" | "error";

interface WangqiQaAiModalProps {
    creatingQaTask?: boolean;
    document: WangqiDocumentRecord;
    onChanged: () => Promise<void> | void;
    onCreateQaTask?: (existingQaPairs: WangqiQaTaskPair[]) => void;
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

const QA_TASK_ALERT_TYPES: Record<string, QaTaskAlertType> = {
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

const isQaTaskActive = (task?: AiRefinementTaskRecord) => {
    return task?.status === "PENDING" || task?.status === "RUNNING";
};

const isQaTaskCompleted = (task?: AiRefinementTaskRecord) => {
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
    trackedCandidateId?: number | null
) => {
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                candidate.capability === "qa" &&
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

const defaultResultFormatForQa = (candidate?: AiCandidateRecord) => {
    return candidate?.resultFormat?.trim() || "STRUCTURED";
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
    qaTasks = [],
    qaTrackingTask
}: WangqiQaAiModalProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [isOpen, setIsOpen] = useState(false);
    const [candidatePayloads, setCandidatePayloads] = useState<Record<number, string>>({});
    const [candidateSubmitEnabled, setCandidateSubmitEnabled] = useState<Record<number, boolean>>(
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
        return [...qaTasks].filter((task) => task.capability === "qa").sort(sortTasksByNewest)[0];
    }, [qaTasks]);
    const trackedQaTaskFromList = useMemo(() => {
        return qaTasks.find((task) => task.taskId === qaTrackingTaskId);
    }, [qaTasks, qaTrackingTaskId]);

    const trackedQaTaskQuery = useQuery({
        queryKey: ["classics", "wangqi", "refinement", "task", qaTrackingTaskId],
        queryFn: () => aiRefinementTaskService.getTask({ taskId: qaTrackingTaskId ?? 0 }),
        enabled: isOpen && Boolean(qaTrackingTaskId),
        retry: false,
        refetchInterval: (query) => {
            const task = query.state.data;
            if (!qaTrackingTaskId) {
                return false;
            }
            if (!task || isQaTaskActive(task)) {
                return QA_CANDIDATE_POLL_INTERVAL_MS;
            }
            if (isQaTaskCompleted(task) && !task.candidateId) {
                return QA_CANDIDATE_POLL_INTERVAL_MS;
            }
            return false;
        }
    });
    const latestQaTask =
        trackedQaTaskQuery.data || trackedQaTaskFromList || qaTrackingTask || latestQaTaskFromList;
    const trackedQaCandidateId =
        qaTrackingTaskId && latestQaTask?.taskId === qaTrackingTaskId
            ? latestQaTask.candidateId
            : null;
    const shouldPollQaCandidates =
        creatingQaTask ||
        isQaTaskActive(latestQaTask) ||
        Boolean(qaTrackingTaskId && isQaTaskCompleted(latestQaTask) && trackedQaCandidateId);

    const qaCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", document.id, "qa", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: document.id,
                contentType: "WANGQI_DOCUMENT",
                capability: "qa",
                status: "PENDING"
            }),
        enabled: isOpen && Boolean(document.id),
        retry: false,
        refetchInterval: () => (shouldPollQaCandidates ? QA_CANDIDATE_POLL_INTERVAL_MS : false)
    });

    const latestQaCandidate = useMemo(() => {
        if (creatingQaTask || (qaTrackingTaskId && !trackedQaCandidateId)) {
            return undefined;
        }
        return selectLatestQaCandidate(qaCandidatesQuery.data, trackedQaCandidateId);
    }, [creatingQaTask, qaCandidatesQuery.data, qaTrackingTaskId, trackedQaCandidateId]);

    const qaTaskAlert = useMemo(() => {
        if (creatingQaTask) {
            return {
                description: "任务创建成功后会自动进入状态跟踪。",
                title: "正在创建问答任务",
                type: "info" as const
            };
        }
        if (latestQaTask) {
            const statusLabel = QA_TASK_STATUS_LABELS[latestQaTask.status] || latestQaTask.status;
            return {
                description: getQaTaskDescription(latestQaTask),
                title: `问答任务${statusLabel}`,
                type: QA_TASK_ALERT_TYPES[latestQaTask.status] || ("info" as const)
            };
        }
        if (qaCandidatesQuery.isFetching) {
            return {
                description: "任务完成后会自动刷新候选问答。",
                title: "正在加载候选问答",
                type: "info" as const
            };
        }
        return null;
    }, [creatingQaTask, latestQaTask, qaCandidatesQuery.isFetching]);

    const applyMutation = useMutation({
        mutationFn: aiCandidateService.apply,
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["ai", "candidates", "WANGQI_DOCUMENT", document.id]
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
            messageApi.warning("请先保存王圻文档后再使用 AI 问答");
            return;
        }
        onCreateQaTask(currentQaPairs);
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

    const applyCandidate = () => {
        if (!latestQaCandidate) {
            messageApi.warning("暂无可采用的候选问答");
            return;
        }
        const payload =
            candidatePayloads[latestQaCandidate.candidateId] ||
            latestQaCandidate.resultPayload?.trim() ||
            "";
        if (!payload || !candidateSubmitEnabled[latestQaCandidate.candidateId]) {
            messageApi.warning("请先确认候选问答内容");
            return;
        }
        applyMutation.mutate({
            candidateId: latestQaCandidate.candidateId,
            contentId: document.id,
            contentType: "WANGQI_DOCUMENT",
            capability: "qa",
            objectId: latestQaCandidate.objectId,
            resultFormat: defaultResultFormatForQa(latestQaCandidate),
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
            <KuzhambuModal
                testId="classics-wangqi-document-qa-ai-modal"
                className="wangqi-qa-modal"
                title={
                    <div className="wangqi-summary-modal-title">
                        <span>AI 问答</span>
                        <KuzhambuButton
                            testId="classics-wangqi-document-qa-ai-generate-button"
                            type="primary"
                            ariaLabel="生成 AI 问答"
                            icon={<FileTextOutlined />}
                            loading={creatingQaTask}
                            onClick={createQaTask}
                        >
                            生成问答
                        </KuzhambuButton>
                    </div>
                }
                destroyOnHidden
                footer={
                    <div className="wangqi-summary-modal-footer">
                        <KuzhambuButton
                            testId="classics-wangqi-document-qa-ai-cancel-button"
                            onClick={() => setIsOpen(false)}
                        >
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-wangqi-document-qa-ai-apply-button"
                            type="primary"
                            disabled={
                                !latestQaCandidate ||
                                !candidateSubmitEnabled[latestQaCandidate.candidateId]
                            }
                            loading={applyMutation.isPending}
                            onClick={applyCandidate}
                        >
                            采用
                        </KuzhambuButton>
                    </div>
                }
                open={isOpen}
                width={880}
                onCancel={() => setIsOpen(false)}
            >
                {qaTaskAlert ? (
                    <KuzhambuAlert
                        showIcon
                        type={qaTaskAlert.type}
                        title={qaTaskAlert.title}
                        description={qaTaskAlert.description}
                    />
                ) : null}
                {qaCandidatesQuery.isError && !shouldPollQaCandidates ? (
                    <KuzhambuAlert
                        showIcon
                        type="warning"
                        title="候选问答加载失败"
                        description="请稍后重试加载候选问答。"
                    />
                ) : null}
                {qaCandidatesQuery.isError && shouldPollQaCandidates ? (
                    <KuzhambuAlert
                        showIcon
                        type="info"
                        title="候选问答暂未返回"
                        description="AI 任务仍在跟踪中，系统会继续刷新候选问答。"
                    />
                ) : null}
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
                    <section className="wangqi-summary-modal-card" aria-label="AI候选问答">
                        <Text strong>AI 问答</Text>
                        {latestQaCandidate ? (
                            <AiCandidatePayloadEditor
                                key={`${latestQaCandidate.candidateId}-${
                                    latestQaCandidate.resultPayload ?? ""
                                }`}
                                candidateId={latestQaCandidate.candidateId}
                                capability="qa"
                                initialPayload={latestQaCandidate.resultPayload}
                                onPayloadChange={updateCandidatePayload}
                                onSubmitEnabledChange={updateCandidateSubmitEnabled}
                            />
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description={
                                    creatingQaTask || qaCandidatesQuery.isFetching
                                        ? "AI 问答生成中"
                                        : "暂无候选问答，可先点击生成问答"
                                }
                            />
                        )}
                    </section>
                </div>
                <section className="wangqi-summary-modal-card" aria-label="AI问答生成依据">
                    <Text strong>生成依据</Text>
                    <div className="wangqi-tags-modal-source-grid">
                        <label>
                            <Text type="secondary">标题</Text>
                            <Input
                                aria-label="AI问答依据标题"
                                readOnly
                                value={document.title || "未命名文档"}
                            />
                        </label>
                        <label>
                            <Text type="secondary">摘要</Text>
                            <Input.TextArea
                                aria-label="AI问答依据摘要"
                                readOnly
                                autoSize={resolveTextAreaAutoSize({ minRows: 2, maxRows: 4 })}
                                value={document.summary || "暂无摘要"}
                            />
                        </label>
                        <label>
                            <Text type="secondary">正文</Text>
                            <Input.TextArea
                                aria-label="AI问答依据正文"
                                readOnly
                                autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                                value={document.content || "暂无正文"}
                            />
                        </label>
                        <div aria-label="AI问答依据已有问答">
                            <Text type="secondary">已有问答</Text>
                            {currentQaPairs.length ? (
                                <KuzhambuSpace orientation="vertical" size="small">
                                    {currentQaPairs.map((pair, index) => (
                                        <Text key={`${pair.question}-${index}`}>
                                            Q: {pair.question || "-"}；A: {pair.answer || "-"}
                                        </Text>
                                    ))}
                                </KuzhambuSpace>
                            ) : (
                                <Text type="secondary">暂无已有问答</Text>
                            )}
                        </div>
                    </div>
                </section>
            </KuzhambuModal>
        </>
    );
};
