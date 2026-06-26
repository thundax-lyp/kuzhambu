import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphExtractionBatchCancelCommand,
    GraphExtractionBatchCancelResult,
    GraphExtractionCreateCommand,
    GraphExtractionRegenerateCommand,
    GraphExtractionTaskIdCommand,
    GraphExtractionTaskPageQuery,
    GraphExtractionTaskRecord
} from "./graph-extraction-types";

const API_PREFIX = "/knowledge/graph-extraction";

export const addTask = (request: GraphExtractionCreateCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionCreateCommand>(
        `${API_PREFIX}/task/add`,
        {
            body: request
        }
    );
};

export const pageTasks = (request: GraphExtractionTaskPageQuery = {}) => {
    return postJson<Page<GraphExtractionTaskRecord>, GraphExtractionTaskPageQuery>(
        `${API_PREFIX}/task/page`,
        {
            body: request
        }
    );
};

export const getTaskDetail = (request: GraphExtractionTaskIdCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionTaskIdCommand>(
        `${API_PREFIX}/task/get`,
        {
            body: request
        }
    );
};

export const applyTaskCandidate = (request: GraphExtractionTaskIdCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionTaskIdCommand>(
        `${API_PREFIX}/task/apply`,
        {
            body: request
        }
    );
};

export const regenerateTask = (request: GraphExtractionRegenerateCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionRegenerateCommand>(
        `${API_PREFIX}/task/regenerate`,
        {
            body: request
        }
    );
};

export const cancelBatchTask = (request: GraphExtractionBatchCancelCommand) => {
    return postJson<GraphExtractionBatchCancelResult, GraphExtractionBatchCancelCommand>(
        `${API_PREFIX}/task/cancel-batch`,
        {
            body: request
        }
    );
};
