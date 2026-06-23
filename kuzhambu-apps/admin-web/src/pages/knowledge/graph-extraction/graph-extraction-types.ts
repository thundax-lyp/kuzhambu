/* eslint-disable local/service-input-type-location */

export type GraphExtractionTaskType = "RELATION" | "GRAPH" | "LINEAGE" | string;
export type GraphExtractionTaskStatus =
    | "PENDING"
    | "RUNNING"
    | "SUCCEEDED"
    | "FAILED"
    | "APPLIED"
    | string;

export interface GraphExtractionTaskRecord {
    taskId: string;
    taskType?: GraphExtractionTaskType | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    aiCallId?: number | null;
    aiCandidateId?: number | null;
    status?: GraphExtractionTaskStatus | null;
    errorType?: string | null;
    errorMessage?: string | null;
    requestedBy?: number | null;
    requestedAt?: number | null;
    completedAt?: number | null;
    appliedAt?: number | null;
}

export interface GraphExtractionCreateCommand {
    taskType: GraphExtractionTaskType;
    scopeType?: string | null;
    scopeJson?: string | null;
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
    taskType?: GraphExtractionTaskType | null;
    status?: GraphExtractionTaskStatus | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
}

export interface GraphExtractionTaskIdCommand {
    taskId: number;
}
