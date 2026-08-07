import {
    CloseOutlined,
    ImportOutlined,
    MergeCellsOutlined,
    PlusOutlined,
    RobotOutlined
} from "@ant-design/icons";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuSyncTaskModal,
    type KuzhambuSyncTaskAdapter
} from "@/components";
import type {
    GraphExtractionTaskRecord,
    GraphExtractionTaskType,
    GraphWorkbenchCandidateRecord,
    GraphWorkbenchManuscriptRecord
} from "../graph-extraction-types";
import { GraphExtractionCandidatePreview } from "./graph-extraction-candidate-preview";

interface GraphExtractionCandidateModalProps {
    applying?: boolean;
    canApply?: boolean;
    canEdit?: boolean;
    candidate?: GraphWorkbenchCandidateRecord | null;
    candidateLoading?: boolean;
    detail?: GraphWorkbenchManuscriptRecord | null;
    extracting?: boolean;
    open: boolean;
    task?: GraphExtractionTaskRecord | null;
    onApplyCandidate: (taskId: string, applyMode: "APPEND" | "MERGE" | "OVERWRITE") => void;
    onCancel: () => void;
    onFetchCandidate: (
        task: GraphExtractionTaskRecord | null
    ) => Promise<GraphWorkbenchCandidateRecord | null>;
    onFetchTask: (taskId: string) => Promise<GraphExtractionTaskRecord>;
    onExtract: (taskType: GraphExtractionTaskType) => void;
    onTaskChange?: (task: GraphExtractionTaskRecord | null) => void;
}

const TASK_LABEL = "图谱";

const hasCandidate = (candidate?: GraphWorkbenchCandidateRecord | null) =>
    Boolean(candidate?.taskId && candidate.aiCandidateId);

const hasUnappliedCandidate = (
    candidate?: GraphWorkbenchCandidateRecord | null,
    task?: GraphExtractionTaskRecord | null,
    detail?: GraphWorkbenchManuscriptRecord | null
) => {
    const status = candidate?.status || task?.status || detail?.latestExtractionTask?.status;
    return (
        status !== "APPLIED" &&
        status !== "REJECTED" &&
        (hasCandidate(candidate) ||
            Boolean(task?.aiCandidateId || detail?.latestExtractionTask?.aiCandidateId))
    );
};

const readTaskId = (task?: GraphExtractionTaskRecord | null) =>
    task?.taskId === null || task?.taskId === undefined ? "" : String(task.taskId).trim();

const readTaskStatusLabel = (status?: string | null) => {
    switch (status) {
        case "REQUESTED":
        case "PENDING":
            return "等待中";
        case "RUNNING":
            return "处理中";
        case "SUCCEEDED":
            return "已完成";
        case "FAILED":
            return "失败";
        case "APPLIED":
            return "已应用";
        default:
            return status || "-";
    }
};

const readTaskAlertType = (status?: string | null) => {
    if (status === "SUCCEEDED" || status === "APPLIED") {
        return "success";
    }
    if (status === "FAILED") {
        return "warning";
    }
    return "info";
};

const graphTaskAdapter: KuzhambuSyncTaskAdapter<GraphExtractionTaskRecord> = {
    getId: readTaskId,
    getMessage: (task) => task.errorMessage || undefined,
    getPhase: (task) => {
        if (task.status === "REQUESTED" || task.status === "PENDING" || task.status === "RUNNING") {
            return "tracking";
        }
        if (task.status === "SUCCEEDED") {
            return task.aiCandidateId ? "result_ready" : "waiting_result";
        }
        if (task.status === "FAILED") {
            return "failed";
        }
        if (task.status === "APPLIED") {
            return "result_ready";
        }
        return "tracking";
    },
    getResultKey: (task) => task.aiCandidateId,
    getStatusLabel: (task) => `${TASK_LABEL}任务：${readTaskStatusLabel(task.status)}`
};

export const GraphExtractionCandidateModal = ({
    applying = false,
    canApply = false,
    canEdit = false,
    candidate,
    candidateLoading = false,
    detail,
    extracting = false,
    open,
    task,
    onApplyCandidate,
    onCancel,
    onFetchCandidate,
    onFetchTask,
    onExtract,
    onTaskChange
}: GraphExtractionCandidateModalProps) => {
    const unappliedCandidate = hasUnappliedCandidate(candidate, task, detail);
    const readApplyTaskId = (
        result?: GraphWorkbenchCandidateRecord | null,
        stateTask?: GraphExtractionTaskRecord | null
    ) => result?.taskId || candidate?.taskId || stateTask?.taskId || task?.taskId;
    const isApplyDisabled = ({
        result,
        resultLoading,
        task,
        taskLoading,
        tracking
    }: {
        result: GraphWorkbenchCandidateRecord | null;
        resultLoading: boolean;
        task: GraphExtractionTaskRecord | null;
        taskLoading: boolean;
        tracking: boolean;
    }) =>
        !canApply ||
        !hasUnappliedCandidate(result || candidate, task || undefined, detail) ||
        tracking ||
        taskLoading ||
        resultLoading ||
        candidateLoading;

    return (
        <KuzhambuSyncTaskModal<GraphExtractionTaskRecord, GraphWorkbenchCandidateRecord>
            testId="knowledge-graph-extraction-candidate-modal"
            title="图谱抽取"
            open={open}
            width={760}
            applying={applying}
            applyText={
                <KuzhambuSpace size={6}>
                    <MergeCellsOutlined />
                    合并
                </KuzhambuSpace>
            }
            applyDisabled={isApplyDisabled}
            applyTestId="knowledge-graph-extraction-candidate-merge-apply-button"
            cancelTestId="knowledge-graph-extraction-candidate-cancel-button"
            cancelText={
                <KuzhambuSpace size={6}>
                    <CloseOutlined />
                    关闭
                </KuzhambuSpace>
            }
            createIcon={<RobotOutlined />}
            createTestId="knowledge-graph-extraction-candidate-extract-button"
            createText={unappliedCandidate ? "重新抽取" : "抽取"}
            creating={extracting}
            createDisabled={!canEdit}
            onCancel={onCancel}
            workflow={{
                ...graphTaskAdapter,
                task: task || null,
                createTask: () => onExtract("GRAPH"),
                fetchTask: onFetchTask,
                fetchResult: onFetchCandidate,
                applyResult: (result) => {
                    const taskId = readApplyTaskId(result);
                    if (taskId) {
                        onApplyCandidate(String(taskId), "MERGE");
                    }
                },
                onTaskChange,
                pollIntervalMs: 3000,
                resultQueryKey: [
                    "graph-extraction",
                    task?.sourceContentType,
                    task?.sourceContentId
                ],
                trackTask: Boolean(task?.taskId)
            }}
            renderStatus={({ creating, task, tracking }) =>
                creating || task ? (
                    <KuzhambuAlert
                        showIcon
                        type={creating ? "info" : readTaskAlertType(task?.status)}
                        title={
                            creating
                                ? `正在创建${TASK_LABEL}任务`
                                : `${TASK_LABEL}任务：${readTaskStatusLabel(task?.status)}`
                        }
                        description={
                            tracking
                                ? "任务完成后会自动刷新候选。"
                                : task?.errorMessage || undefined
                        }
                    />
                ) : null
            }
            renderFooterActions={(state) => {
                const appendDisabled = isApplyDisabled(state);
                const taskId = readApplyTaskId(state.result, state.task);
                return (
                    <>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-candidate-overwrite-apply-button"
                            disabled={appendDisabled}
                            icon={<ImportOutlined />}
                            loading={applying}
                            onClick={() => {
                                if (taskId) {
                                    onApplyCandidate(String(taskId), "OVERWRITE");
                                }
                            }}
                        >
                            覆盖
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-candidate-append-apply-button"
                            disabled={appendDisabled}
                            icon={<PlusOutlined />}
                            loading={applying}
                            onClick={() => {
                                if (taskId) {
                                    onApplyCandidate(String(taskId), "APPEND");
                                }
                            }}
                        >
                            追加
                        </KuzhambuButton>
                    </>
                );
            }}
            renderBody={({ result, resultLoading, tracking, taskLoading }) => (
                <GraphExtractionCandidatePreview
                    candidate={result || candidate || null}
                    loading={candidateLoading || resultLoading || tracking || taskLoading}
                />
            )}
        />
    );
};
