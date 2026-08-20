import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphContentRefRecord,
    GraphContentType,
    GraphExtractionTaskActionResultRecord,
    GraphExtractionTaskDeleteResultRecord,
    GraphExtractionTaskRecord,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "./graph-extraction-types";

const TASK_PAGE_PATH = "/knowledge/graph/task/page";
const TASK_RETRY_PATH = "/knowledge/graph/task/retry";
const TASK_DELETE_PATH = "/knowledge/graph/task/delete";

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
    deleteTask: (
        command: GraphExtractionTaskStateCommand
    ) => Promise<GraphExtractionTaskDeleteResultRecord>;
}

const createIdempotencyKey = () => {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `idem-${Date.now()}-${Math.random().toString(36).slice(2)}`;
};

const toPage = <TRecord>(page: {
    count: number;
    pageNo: number;
    pageSize: number;
    records: TRecord[];
    totalPage: number;
}): Page<TRecord> => ({
    count: page.count,
    pageNo: page.pageNo,
    pageSize: page.pageSize,
    records: page.records,
    totalCount: page.count,
    totalPage: page.totalPage
});

export const httpGraphExtractionService: GraphExtractionService = {
    pageTasks: async (query = {}) => {
        const page = await postJson<
            {
                count: number;
                pageNo: number;
                pageSize: number;
                records: GraphExtractionTaskRecord[];
                totalPage: number;
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
    },
    deleteTask: async (command) =>
        postJson<
            GraphExtractionTaskDeleteResultRecord,
            GraphExtractionTaskStateCommand & {
                idempotencyKey: string;
                taskId: string;
                taskLockVersion: string;
            }
        >(TASK_DELETE_PATH, {
            body: {
                expectedExecutionStatus: command.expectedExecutionStatus,
                idempotencyKey: createIdempotencyKey(),
                taskId: command.taskId ?? command.sourceTaskId ?? "",
                taskLockVersion: command.taskLockVersion ?? ""
            }
        })
};

export const deleteTask = httpGraphExtractionService.deleteTask;
export const pageTasks = httpGraphExtractionService.pageTasks;
export const retryTask = httpGraphExtractionService.retryTask;
