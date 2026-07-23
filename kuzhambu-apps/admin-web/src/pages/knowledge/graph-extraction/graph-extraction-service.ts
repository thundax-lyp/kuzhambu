import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphExtractionBatchCancelRecord,
    GraphExtractionTaskRecord,
    GraphExtractionTaskStatus,
    GraphExtractionTaskType,
    GraphExtractionTriggerSource
} from "./graph-extraction-types";

const API_PREFIX = "/knowledge/graph-extraction";

export interface GraphExtractionCreateCommand {
    taskType: GraphExtractionTaskType;
    triggerSource?: GraphExtractionTriggerSource | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    requestedBy?: number | null;
    serviceId?: number | null;
    serviceRole?: string | null;
    modelId?: number | null;
    modelName?: string | null;
    promptVersionId?: number | null;
    requestId?: string | null;
    traceId?: string | null;
    promptMessagesJson?: string | null;
    promptVariablesJson?: string | null;
    promptHash?: string | null;
    inputPayloadJson?: string | null;
    outputSchemaJson?: string | null;
    forceJson?: boolean | null;
    locale?: string | null;
}

export interface GraphExtractionTaskPageQuery {
    pageNo?: number;
    pageSize?: number;
    batchJobId?: number | null;
    triggerSource?: GraphExtractionTriggerSource | null;
    taskType?: GraphExtractionTaskType | null;
    status?: GraphExtractionTaskStatus | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
}

export interface GraphExtractionTaskIdCommand {
    taskId: number;
}

export interface GraphExtractionRegenerateCommand {
    taskType: GraphExtractionTaskType;
    sourceTaskId?: number | null;
    triggerSource?: GraphExtractionTriggerSource | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    requestedBy?: number | null;
}

export interface GraphExtractionBatchCancelCommand {
    batchJobId: number;
    requestedBy?: number | null;
}

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
    return postJson<GraphExtractionBatchCancelRecord, GraphExtractionBatchCancelCommand>(
        `${API_PREFIX}/task/cancel-batch`,
        {
            body: request
        }
    );
};
