import { CloseCircleOutlined, PlusOutlined, RobotOutlined } from "@ant-design/icons";
import { App, Empty, Input } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuSyncTaskModal,
    KuzhambuTable,
    type KuzhambuSyncTaskAdapter,
    type KuzhambuSyncTaskModalState
} from "@/components";
import { normalizeId } from "@/types/id";

import * as aiCandidateService from "./ai-candidate-service";
import type { AiCandidateRecord } from "./ai-candidate-types";
import { selectLatestQaCandidate } from "./classics-content-ai-candidate-selectors";
import * as aiRefinementTaskService from "./ai-refinement-task-service";
import * as contentService from "./classics-content-service";
import { AI_BUSINESS_CAPABILITY, type AiRefinementTaskRecord } from "./ai-refinement-task-types";
import type { ClassicsContentType } from "./classics-content-types";

interface ClassicsContentQaAiPanelProps {
    canApplyCandidate?: boolean;
    canCreateTask?: boolean;
    canRejectCandidate?: boolean;
    canViewCandidate?: boolean;
    contentId: string;
    contentType: ClassicsContentType;
    creatingTask?: boolean;
    onChanged?: () => void | Promise<void>;
    onCreateTask?: (existingQaPairs: ClassicsContentQaTaskPair[]) => void;
    onTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    qaTasks?: AiRefinementTaskRecord[];
    trackingTask?: AiRefinementTaskRecord | null;
}

export interface ClassicsContentQaTaskPair {
    answer: string;
    question: string;
}

const QA_TASK_POLL_INTERVAL_MS = 3000;
const QA_AI_MODAL_TEST_ID = "classics-content-qa-ai-modal";
const REJECT_ERROR_TYPE = "USER_REJECTED";
const REJECT_ERROR_MESSAGE = "用户已拒绝该 AI 候选";

const QA_TASK_STATUS_LABELS: Record<string, string> = {
    PENDING: "等待中",
    RUNNING: "处理中",
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

interface CandidateQaPairRow {
    answer: string;
    id: string;
    question: string;
}

const parseQaPayload = (payload?: string | null): CandidateQaPairRow[] => {
    if (!payload?.trim()) {
        return [];
    }

    try {
        const parsed = JSON.parse(payload);
        const qaPairs = Array.isArray(parsed) ? parsed : (parsed?.qaPairs ?? parsed?.qa_pairs);
        if (Array.isArray(qaPairs)) {
            return qaPairs
                .map((pair: { question?: unknown; answer?: unknown }, index) => ({
                    id: `${index}-${String(pair?.question ?? "")}-${String(pair?.answer ?? "")}`,
                    question: String(pair?.question ?? "").trim(),
                    answer: String(pair?.answer ?? "").trim()
                }))
                .filter((pair) => pair.question || pair.answer);
        }
    } catch {
        return [];
    }

    return [];
};

const stringifyQaPayload = (qaPairs: CandidateQaPairRow[]): string => {
    return JSON.stringify({
        qaPairs: qaPairs
            .map((pair) => ({
                question: pair.question.trim(),
                answer: pair.answer.trim()
            }))
            .filter((pair) => pair.question && pair.answer)
    });
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

const isQaTaskActive = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "PENDING" || task?.status === "RUNNING";
};

const isQaTaskCompleted = (task?: AiRefinementTaskRecord | null) => {
    return task?.status === "SUCCEEDED" || task?.status === "PARTIAL";
};

const getTaskCandidateId = (task?: AiRefinementTaskRecord | null) =>
    task?.candidateIdText ?? task?.candidateId;

const getCandidateStableId = (candidate: AiCandidateRecord) => {
    return candidate.candidateIdText || normalizeId(candidate.candidateId);
};

const readTaskMessage = (task?: AiRefinementTaskRecord | null) => {
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

const qaTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => aiRefinementTaskService.getTaskStableId(task.taskId, task.taskIdText),
    getMessage: readTaskMessage,
    getPhase: (task) => {
        if (isQaTaskActive(task)) {
            return "tracking";
        }
        if (isQaTaskCompleted(task)) {
            return getTaskCandidateId(task) ? "result_ready" : "waiting_result";
        }
        if (task.status === "CANCELLED") {
            return "cancelled";
        }
        if (task.status === "FAILED") {
            return "failed";
        }
        return "tracking";
    },
    getResultKey: getTaskCandidateId,
    getStatusLabel: (task) => {
        const statusLabel = QA_TASK_STATUS_LABELS[task.status] || task.status;
        return `问答任务：${statusLabel}`;
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
                title={`问答任务：${statusLabel}`}
                description={tracking ? "任务完成后会自动刷新候选问答。" : readTaskMessage(task)}
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
                description="AI 任务仍在跟踪中，系统会继续刷新候选问答。"
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

const defaultResultFormatForQa = (candidate?: AiCandidateRecord) => {
    return candidate?.resultFormat?.trim() || "STRUCTURED";
};

let candidateQaRowSequence = 0;

const createCandidateQaRowId = () => {
    candidateQaRowSequence += 1;
    return `candidate-qa-${candidateQaRowSequence}`;
};

interface CandidateQaTableProps {
    candidate: AiCandidateRecord;
    disabled?: boolean;
    onPayloadChange: (candidateId: string, payload: string) => void;
    onSubmitEnabledChange: (candidateId: string, canSubmit: boolean) => void;
}

const CandidateQaTable = ({
    candidate,
    disabled = false,
    onPayloadChange,
    onSubmitEnabledChange
}: CandidateQaTableProps) => {
    const candidateId = getCandidateStableId(candidate);
    const [rows, setRows] = useState<CandidateQaPairRow[]>(() =>
        parseQaPayload(candidate.resultPayload)
    );

    useEffect(() => {
        const payload = stringifyQaPayload(rows);
        const canSubmit = rows.some((row) => row.question.trim() && row.answer.trim());
        onPayloadChange(candidateId, payload);
        onSubmitEnabledChange(candidateId, canSubmit);
    }, [candidateId, onPayloadChange, onSubmitEnabledChange, rows]);

    const updateRow = (rowId: string, field: "question" | "answer", value: string) => {
        setRows((currentRows) =>
            currentRows.map((row) => (row.id === rowId ? { ...row, [field]: value } : row))
        );
    };

    return (
        <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
            <KuzhambuButton
                testId="classics-content-qa-ai-generated-add-button"
                disabled={disabled}
                icon={<PlusOutlined />}
                onClick={() =>
                    setRows((currentRows) => [
                        ...currentRows,
                        { id: createCandidateQaRowId(), question: "", answer: "" }
                    ])
                }
            >
                新增问答
            </KuzhambuButton>
            {rows.length ? (
                <KuzhambuTable<CandidateQaPairRow>
                    ariaLabel="候选问答列表"
                    dataSource={rows}
                    pagination={false}
                    rowKey="id"
                    columns={[
                        {
                            key: "qa",
                            title: "生成问答",
                            render: (_value, row, index) => (
                                <KuzhambuSpace
                                    orientation="vertical"
                                    size={6}
                                    style={{ width: "100%" }}
                                >
                                    <Input.TextArea
                                        aria-label={`候选问题 ${index + 1}`}
                                        value={row.question}
                                        disabled={disabled}
                                        autoSize={{ minRows: 1, maxRows: 4 }}
                                        onChange={(event) =>
                                            updateRow(row.id, "question", event.target.value)
                                        }
                                    />
                                    <Input.TextArea
                                        aria-label={`候选答案 ${index + 1}`}
                                        value={row.answer}
                                        disabled={disabled}
                                        autoSize={{ minRows: 2, maxRows: 8 }}
                                        onChange={(event) =>
                                            updateRow(row.id, "answer", event.target.value)
                                        }
                                    />
                                </KuzhambuSpace>
                            )
                        },
                        {
                            key: "actions",
                            title: "操作",
                            options: (row) => [
                                {
                                    key: "delete-divider",
                                    type: "divider"
                                },
                                {
                                    key: "delete",
                                    text: "删除",
                                    type: "danger",
                                    disabled,
                                    testId: `classics-content-qa-ai-generated-delete-${row.id}-button`,
                                    onClick: () =>
                                        setRows((currentRows) =>
                                            currentRows.filter(
                                                (currentRow) => currentRow.id !== row.id
                                            )
                                        )
                                }
                            ]
                        }
                    ]}
                />
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="候选问答为空" />
            )}
        </KuzhambuSpace>
    );
};

export const ClassicsContentQaAiPanel = ({
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
    qaTasks = [],
    trackingTask
}: ClassicsContentQaAiPanelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [open, setOpen] = useState(false);
    const [candidatePayloads, setCandidatePayloads] = useState<Record<string, string>>({});
    const [candidateSubmitEnabled, setCandidateSubmitEnabled] = useState<Record<string, boolean>>(
        {}
    );
    const qaPairsQuery = useQuery({
        queryKey: ["classics", "content", "qa-pairs", contentType, contentId],
        queryFn: () => contentService.listQaPairs({ contentId, contentType }),
        enabled: open && Boolean(contentId),
        retry: false
    });
    const currentQaPairs = useMemo(
        () =>
            (Array.isArray(qaPairsQuery.data) ? qaPairsQuery.data : [])
                .map((pair) => ({
                    answer: pair.answer?.trim() || "",
                    question: pair.question?.trim() || ""
                }))
                .filter((pair) => pair.question || pair.answer),
        [qaPairsQuery.data]
    );

    const latestQaTaskFromList = useMemo(
        () =>
            [...qaTasks]
                .filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "qa"
                )
                .sort(sortRefinementTasksByNewest)[0] ?? null,
        [qaTasks]
    );
    const latestQaTask = trackingTask || latestQaTaskFromList;

    const refresh = async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", contentType, contentId]
            }),
            queryClient.removeQueries({
                queryKey: ["sync-task-modal", QA_AI_MODAL_TEST_ID, "result", contentType, contentId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", contentType, contentId]
            })
        ]);
        if (onChanged) {
            await onChanged();
        }
    };

    const applyMutation = useMutation({
        mutationFn: aiCandidateService.apply,
        onSuccess: async () => {
            await refresh();
            setOpen(false);
            messageApi.success("候选问答已追加");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "追加候选问答失败");
        }
    });

    const rejectMutation = useMutation({
        mutationFn: aiCandidateService.reject,
        onSuccess: async () => {
            await refresh();
            setOpen(false);
            messageApi.success("候选问答已拒绝");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "拒绝候选问答失败");
        }
    });

    const openModal = () => {
        setCandidatePayloads({});
        setCandidateSubmitEnabled({});
        setOpen(true);
    };

    const loadQaCandidate = async (task: AiRefinementTaskRecord | null) => {
        if (!canViewCandidate) {
            return null;
        }
        const candidates = await aiCandidateService.list({
            contentId,
            contentType,
            capability: AI_BUSINESS_CAPABILITY.CLASSICS_QA,
            status: "PENDING"
        });
        return selectLatestQaCandidate(candidates, getTaskCandidateId(task)) ?? null;
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
            messageApi.warning("暂无可追加的候选问答");
            return;
        }
        const candidateId = getCandidateStableId(candidate);
        const payload = candidatePayloads[candidateId] || candidate.resultPayload?.trim() || "";
        if (!payload || !candidateSubmitEnabled[candidateId]) {
            messageApi.warning("请先确认候选问答内容");
            return;
        }
        applyMutation.mutate({
            candidateId,
            contentId,
            contentType,
            capability: AI_BUSINESS_CAPABILITY.CLASSICS_QA,
            objectId: candidate.objectId,
            resultFormat: defaultResultFormatForQa(candidate),
            resultPayload: payload,
            changeSummary: "AI 应用：问答"
        });
    };

    const rejectCandidate = (candidate: AiCandidateRecord | null) => {
        if (!candidate) {
            messageApi.warning("暂无可拒绝的候选问答");
            return;
        }
        rejectMutation.mutate({
            candidateId: getCandidateStableId(candidate),
            errorType: REJECT_ERROR_TYPE,
            errorMessage: REJECT_ERROR_MESSAGE
        });
    };

    return (
        <>
            <KuzhambuButton
                testId="classics-common-content-qa-ai-button"
                icon={<RobotOutlined />}
                type="primary"
                onClick={openModal}
            >
                AI生成
            </KuzhambuButton>
            <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
                testId={QA_AI_MODAL_TEST_ID}
                open={open}
                title="AI 问答"
                width={880}
                createIcon={<RobotOutlined />}
                createTestId="classics-content-qa-ai-create-task-button"
                createText="生成候选问答"
                createDisabled={!canCreateTask || !canViewCandidate}
                creating={creatingTask}
                hideCancel={({ result }) => Boolean(result)}
                onCancel={() => setOpen(false)}
                workflow={{
                    ...qaTaskAdapter,
                    task: latestQaTask,
                    createTask: () => onCreateTask?.(currentQaPairs),
                    fetchResult: loadQaCandidate,
                    fetchTask: (taskId) => aiRefinementTaskService.getTask({ taskId }),
                    onTaskChange,
                    pollIntervalMs: QA_TASK_POLL_INTERVAL_MS,
                    resultQueryKey: [contentType, contentId, "qa"],
                    trackTask: Boolean(latestQaTask?.taskId)
                }}
                renderStatus={renderQaTaskStatus}
                renderFooterActions={({ creating, result, resultLoading, tracking }) => {
                    const candidateId = result ? getCandidateStableId(result) : "";
                    const isBusy =
                        creating ||
                        tracking ||
                        resultLoading ||
                        applyMutation.isPending ||
                        rejectMutation.isPending;
                    const canAppend =
                        canApplyCandidate &&
                        canViewCandidate &&
                        Boolean(result) &&
                        Boolean(candidateSubmitEnabled[candidateId]) &&
                        !isBusy;
                    const canReject =
                        canRejectCandidate && canViewCandidate && Boolean(result) && !isBusy;

                    return (
                        <>
                            <KuzhambuButton
                                testId="classics-content-qa-ai-close-button"
                                disabled={isBusy}
                                icon={<CloseCircleOutlined />}
                                onClick={() => setOpen(false)}
                            >
                                关闭
                            </KuzhambuButton>
                            <KuzhambuButton
                                testId="classics-content-qa-ai-reject-button"
                                disabled={!canReject}
                                loading={rejectMutation.isPending}
                                onClick={() => rejectCandidate(result)}
                            >
                                拒绝
                            </KuzhambuButton>
                            <KuzhambuButton
                                testId="classics-content-qa-ai-append-button"
                                disabled={!canAppend}
                                icon={<PlusOutlined />}
                                loading={applyMutation.isPending}
                                type="primary"
                                onClick={() => applyCandidate(result)}
                            >
                                追加
                            </KuzhambuButton>
                        </>
                    );
                }}
                renderBody={({ creating, result, resultLoading, tracking }) => (
                    <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                        {result ? (
                            <CandidateQaTable
                                key={`${getCandidateStableId(result)}-${result.resultPayload ?? ""}`}
                                candidate={result}
                                disabled={creating || tracking || resultLoading}
                                onPayloadChange={updateCandidatePayload}
                                onSubmitEnabledChange={updateCandidateSubmitEnabled}
                            />
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description={
                                    creating || tracking || resultLoading
                                        ? "正在生成候选问答"
                                        : "暂无候选问答，可点击生成"
                                }
                            />
                        )}
                    </KuzhambuSpace>
                )}
            />
        </>
    );
};
