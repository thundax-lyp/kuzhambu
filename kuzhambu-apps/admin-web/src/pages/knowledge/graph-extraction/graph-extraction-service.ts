import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphContentRefRecord,
    GraphContentType,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "./graph-extraction-types";
import type { GraphExtractionTaskRecord } from "./graph-extraction-types";

const TASK_PAGE_PATH = "/knowledge/graph/task/page";
const TASK_RETRY_PATH = "/knowledge/graph/task/retry";

export type GraphExtractionTaskListMode = "NONE" | "MATERIAL";

export interface GraphExtractionTaskPageQuery {
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
    volumeCode?: string;
}

export interface GraphExtractionTaskStateCommand {
    expectedDisposition?: GraphTaskDisposition;
    expectedExecutionStatus?: GraphTaskExecutionStatus;
    sourceTaskId?: string | null;
    taskId?: string;
    taskLockVersion?: string;
}

export interface GraphExtractionService {
    pageTasks: (query?: GraphExtractionTaskPageQuery) => Promise<Page<GraphExtractionTaskRecord>>;
    retryTask: (
        command: GraphExtractionTaskStateCommand
    ) => Promise<GraphExtractionTaskActionResultRecord>;
}

export interface GraphExtractionTaskActionResultRecord {
    conflict?: GraphExtractionTaskConflictRecord;
    task?: GraphExtractionTaskRecord;
}

export interface GraphExtractionTaskConflictRecord {
    code: "GRAPH_TASK_LOCK_CONFLICT" | "GRAPH_TASK_STATE_CONFLICT" | "GRAPH_TASK_ACTIVE_EXISTS";
    message: string;
}

const createIdempotencyKey = () => {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `idem-${Date.now()}-${Math.random().toString(36).slice(2)}`;
};

const toPage = <TRecord>(page: {
    pageNo: string;
    pageSize: string;
    records: TRecord[];
    totalCount: string;
    totalPage: string;
}): Page<TRecord> => ({
    count: Number(page.totalCount),
    pageNo: Number(page.pageNo),
    pageSize: Number(page.pageSize),
    records: page.records,
    totalCount: Number(page.totalCount),
    totalPage: Number(page.totalPage)
});

export const httpGraphExtractionService: GraphExtractionService = {
    pageTasks: async (query = {}) => {
        const page = await postJson<
            {
                pageNo: string;
                pageSize: string;
                records: GraphExtractionTaskRecord[];
                totalCount: string;
                totalPage: string;
            },
            GraphExtractionTaskPageQuery
        >(TASK_PAGE_PATH, {
            body: {
                ...query,
                groupBy: query.groupBy ?? "NONE"
            }
        });
        return toPage(page);
    },
    retryTask: async (command) => {
        const task = await postJson<
            GraphExtractionTaskRecord,
            GraphExtractionTaskStateCommand & {
                idempotencyKey: string;
                taskId: string;
                taskLockVersion: string;
            }
        >(TASK_RETRY_PATH, {
            body: {
                expectedDisposition: command.expectedDisposition,
                expectedExecutionStatus: command.expectedExecutionStatus ?? "FAILED",
                idempotencyKey: createIdempotencyKey(),
                taskId: command.taskId ?? command.sourceTaskId ?? "",
                taskLockVersion: command.taskLockVersion ?? ""
            }
        });
        return { task };
    }
};

export const pageTasks = httpGraphExtractionService.pageTasks;
export const retryTask = httpGraphExtractionService.retryTask;
