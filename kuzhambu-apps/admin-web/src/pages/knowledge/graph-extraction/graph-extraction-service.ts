import { ApiError, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphBatchExtractionResultRecord,
    GraphCandidateApplyMode,
    GraphContentRefRecord,
    GraphContentType,
    GraphExtractionTaskStatus,
    GraphExtractionTaskActionResultRecord,
    GraphExtractionTaskDetailRecord,
    GraphExtractionTaskListMode,
    GraphExtractionTaskRecord,
    GraphExtractionTaskType,
    GraphExtractionTriggerSource,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "./graph-extraction-types";

const TASK_PAGE_PATH = "/knowledge/graph/task/page";
const TASK_GET_PATH = "/knowledge/graph/task/get";
const TASK_RETRY_PATH = "/knowledge/graph/task/retry";
const TASK_CANCEL_PATH = "/knowledge/graph/task/cancel";
const TASK_CANDIDATE_APPLY_PATH = "/knowledge/graph/task/candidate/apply";
const TASK_CANDIDATE_DISCARD_PATH = "/knowledge/graph/task/candidate/discard";
const TASK_CANDIDATE_REGENERATE_PATH = "/knowledge/graph/task/candidate/regenerate";
const TASK_BATCH_CREATE_PATH = "/knowledge/graph/task/batch/create";

interface GraphApiPage<TRecord> {
    pageNo: string;
    pageSize: string;
    totalCount: string;
    totalPage: string;
    records: TRecord[];
}

interface GraphTaskPageRequest {
    batchId?: string;
    categoryCode?: string;
    contentRefs?: GraphContentRefRecord[];
    contentType?: GraphContentType;
    disposition?: GraphTaskDisposition;
    executionStatus?: GraphTaskExecutionStatus;
    groupBy: GraphExtractionTaskListMode;
    keyword?: string;
    pageNo?: number;
    pageSize?: number;
    volumeCode?: string;
}

interface GraphTaskIdRequest {
    taskId: string;
}

interface GraphTaskStateRequest extends GraphTaskIdRequest {
    expectedDisposition?: GraphTaskDisposition;
    expectedExecutionStatus: GraphTaskExecutionStatus;
    idempotencyKey: string;
    taskLockVersion: string;
}

interface GraphCandidateApplyRequest extends GraphTaskStateRequest {
    applyMode: GraphCandidateApplyMode;
    materialLockVersion: string;
}

interface GraphCandidateDiscardRequest extends GraphTaskStateRequest {
    reason?: string;
}

interface GraphBatchCreateRequest {
    idempotencyKey: string;
    selection: {
        contentRefs?: GraphContentRefRecord[];
        volumeCode?: string;
    };
}

export interface GraphExtractionTaskPageQuery {
    batchJobId?: string;
    batchId?: string;
    categoryCode?: string;
    contentRefs?: GraphContentRefRecord[];
    contentType?: GraphContentType;
    disposition?: GraphTaskDisposition;
    executionStatus?: GraphTaskExecutionStatus;
    groupBy?: GraphExtractionTaskListMode;
    keyword?: string;
    pageNo?: number;
    pageSize?: number;
    sourceContentId?: string | null;
    sourceContentType?: string | null;
    status?: GraphExtractionTaskStatus | null;
    taskType?: GraphExtractionTaskType | null;
    triggerSource?: GraphExtractionTriggerSource | null;
    volumeCode?: string;
}

export interface GraphExtractionTaskIdCommand {
    taskId: string;
}

export interface GraphExtractionTaskStateCommand {
    expectedDisposition?: GraphTaskDisposition;
    expectedExecutionStatus?: GraphTaskExecutionStatus;
    replaceUnconfirmedOnly?: boolean | null;
    requestedBy?: string | null;
    selectionScopeJson?: string | null;
    sourceTaskId?: string | null;
    taskId?: string;
    taskLockVersion?: string;
    taskType?: GraphExtractionTaskType;
    triggerSource?: GraphExtractionTriggerSource | null;
}

export interface GraphExtractionApplyCandidateCommand extends GraphExtractionTaskStateCommand {
    applyMode: GraphCandidateApplyMode;
    materialLockVersion: string;
}

export interface GraphExtractionDiscardCandidateCommand extends GraphExtractionTaskStateCommand {
    reason?: string;
}

export interface GraphExtractionBatchCreateCommand {
    contentRefs?: GraphContentRefRecord[];
    volumeCode?: string;
}

export interface GraphExtractionRegenerateCommand extends GraphExtractionTaskStateCommand {}

export interface GraphExtractionService {
    applyCandidate: (
        command: GraphExtractionApplyCandidateCommand
    ) => Promise<GraphExtractionTaskActionResultRecord>;
    cancelTask: (
        command: GraphExtractionTaskStateCommand
    ) => Promise<GraphExtractionTaskActionResultRecord>;
    createBatchExtraction: (
        command: GraphExtractionBatchCreateCommand
    ) => Promise<GraphBatchExtractionResultRecord>;
    discardCandidate: (
        command: GraphExtractionDiscardCandidateCommand
    ) => Promise<GraphExtractionTaskActionResultRecord>;
    getTask: (command: GraphExtractionTaskIdCommand) => Promise<GraphExtractionTaskDetailRecord>;
    pageTasks: (query?: GraphExtractionTaskPageQuery) => Promise<Page<GraphExtractionTaskRecord>>;
    regenerateTask: (
        command: GraphExtractionTaskStateCommand
    ) => Promise<GraphExtractionTaskActionResultRecord>;
    retryTask: (
        command: GraphExtractionTaskStateCommand
    ) => Promise<GraphExtractionTaskActionResultRecord>;
}

const createIdempotencyKey = () => {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `idem-${Date.now()}-${Math.random().toString(36).slice(2)}`;
};

const toPage = <TRecord>(page: GraphApiPage<TRecord>): Page<TRecord> => ({
    count: Number(page.totalCount),
    pageNo: Number(page.pageNo),
    pageSize: Number(page.pageSize),
    records: page.records,
    totalCount: Number(page.totalCount),
    totalPage: Number(page.totalPage)
});

const toActionResult = (
    task: GraphExtractionTaskRecord
): GraphExtractionTaskActionResultRecord => ({
    task
});

const toStateRequest = (command: GraphExtractionTaskStateCommand): GraphTaskStateRequest => ({
    expectedDisposition: command.expectedDisposition,
    expectedExecutionStatus: command.expectedExecutionStatus ?? "SUCCEEDED",
    idempotencyKey: createIdempotencyKey(),
    taskId: command.taskId ?? command.sourceTaskId ?? "",
    taskLockVersion: command.taskLockVersion ?? ""
});

const assertBatchSelection = (command: GraphExtractionBatchCreateCommand) => {
    const hasContentRefs = Boolean(command.contentRefs?.length);
    const hasVolumeCode = Boolean(command.volumeCode);
    if (hasContentRefs === hasVolumeCode) {
        throw new ApiError("GRAPH_TASK_SELECTION_INVALID", "批量提取必须且只能选择素材或整卷");
    }
};

export const httpGraphExtractionService: GraphExtractionService = {
    pageTasks: async (query = {}) => {
        const page = await postJson<GraphApiPage<GraphExtractionTaskRecord>, GraphTaskPageRequest>(
            TASK_PAGE_PATH,
            {
                body: {
                    ...query,
                    groupBy: query.groupBy ?? "NONE"
                }
            }
        );
        return toPage(page);
    },
    getTask: (command) =>
        postJson<GraphExtractionTaskDetailRecord, GraphTaskIdRequest>(TASK_GET_PATH, {
            body: command
        }),
    retryTask: async (command) => {
        const task = await postJson<GraphExtractionTaskRecord, GraphTaskStateRequest>(
            TASK_RETRY_PATH,
            {
                body: toStateRequest(command)
            }
        );
        return toActionResult(task);
    },
    cancelTask: async (command) => {
        const task = await postJson<GraphExtractionTaskRecord, GraphTaskStateRequest>(
            TASK_CANCEL_PATH,
            {
                body: toStateRequest(command)
            }
        );
        return toActionResult(task);
    },
    applyCandidate: async (command) => {
        const task = await postJson<GraphExtractionTaskRecord, GraphCandidateApplyRequest>(
            TASK_CANDIDATE_APPLY_PATH,
            {
                body: {
                    ...toStateRequest(command),
                    applyMode: command.applyMode,
                    materialLockVersion: command.materialLockVersion
                }
            }
        );
        return toActionResult(task);
    },
    discardCandidate: async (command) => {
        const task = await postJson<GraphExtractionTaskRecord, GraphCandidateDiscardRequest>(
            TASK_CANDIDATE_DISCARD_PATH,
            {
                body: {
                    ...toStateRequest(command),
                    reason: command.reason
                }
            }
        );
        return toActionResult(task);
    },
    regenerateTask: async (command) => {
        const task = await postJson<GraphExtractionTaskRecord, GraphTaskStateRequest>(
            TASK_CANDIDATE_REGENERATE_PATH,
            {
                body: toStateRequest(command)
            }
        );
        return toActionResult(task);
    },
    createBatchExtraction: (command) => {
        assertBatchSelection(command);
        return postJson<GraphBatchExtractionResultRecord, GraphBatchCreateRequest>(
            TASK_BATCH_CREATE_PATH,
            {
                body: {
                    idempotencyKey: createIdempotencyKey(),
                    selection: {
                        contentRefs: command.contentRefs,
                        volumeCode: command.volumeCode
                    }
                }
            }
        );
    }
};

export const pageTasks = httpGraphExtractionService.pageTasks;
export const getTask = httpGraphExtractionService.getTask;
export const retryTask = httpGraphExtractionService.retryTask;
export const cancelTask = httpGraphExtractionService.cancelTask;
export const applyCandidate = httpGraphExtractionService.applyCandidate;
export const discardCandidate = httpGraphExtractionService.discardCandidate;
export const regenerateTask = httpGraphExtractionService.regenerateTask;
export const createBatchExtraction = httpGraphExtractionService.createBatchExtraction;
