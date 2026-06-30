export type AiRefinementTaskStatus =
    | "PENDING"
    | "RUNNING"
    | "SUCCEEDED"
    | "FAILED"
    | "PARTIAL"
    | "CANCELLED"
    | string;

export interface AiRefinementTaskRecord {
    taskId: number;
    status: AiRefinementTaskStatus;
    scope?: string | null;
    capability: string;
    contentType: string;
    contentId: number;
    objectId?: number | null;
    requestedBy?: number | null;
    serviceRole?: string | null;
    modelId?: number | null;
    modelName?: string | null;
    promptVersionId?: number | null;
    requestId?: string | null;
    traceId?: string | null;
    callId?: number | null;
    candidateId?: number | null;
    failureStage?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    resultFormat?: string | null;
    resultPreview?: string | null;
    requestedAt?: string | null;
    startedAt?: string | null;
    completedAt?: string | null;
    cancelledAt?: string | null;
}

export interface AiRefinementTaskCreatePayload {
    capability: string;
    scope: string;
    contentType: string;
    contentId: number;
    objectId?: number | null;
    requestedBy: number;
    serviceId?: number | null;
    serviceRole?: string | null;
    modelId: number;
    modelName: string;
    promptVersionId?: number | null;
    requestId: string;
    traceId: string;
    promptMessagesJson: string;
    promptVariablesJson?: string | null;
    promptHash?: string | null;
    inputPayloadJson: string;
    outputSchemaJson?: string | null;
    forceJson?: boolean | null;
    locale?: string | null;
}

export interface AiRefinementTaskGetPayload {
    taskId: number;
}

export interface AiRefinementTaskCancelPayload {
    taskId: number;
    requestedBy: number;
}

export interface AiRefinementTaskPagePayload {
    capability?: string | null;
    status?: AiRefinementTaskStatus | null;
    contentType?: string | null;
    contentId?: number | null;
    requestedBy?: number | null;
    pageNo?: number;
    pageSize?: number;
}

export interface AiRefinementTaskAcceptedRecord {
    taskId: number;
    status: AiRefinementTaskStatus;
    capability: string;
    contentType: string;
    contentId: number;
    requestedAt?: string | null;
}

export interface AiRefinementTaskPageResult {
    items: AiRefinementTaskRecord[];
    total: number;
    pageNo: number;
    pageSize: number;
}
