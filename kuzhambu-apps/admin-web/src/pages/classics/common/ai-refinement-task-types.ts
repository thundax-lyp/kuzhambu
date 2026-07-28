export type AiRefinementTaskStatus =
    "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "PARTIAL" | "CANCELLED" | string;

export type AiRefinementTaskCapability = "summary" | "tags" | "qa";

export interface AiRefinementTaskRecord {
    taskId: number;
    taskIdText?: string | null;
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
    streamEnabled?: boolean | null;
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
    modelId?: number | null;
    modelName?: string | null;
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
    taskId: number | string;
}

export interface AiRefinementTaskCancelPayload {
    taskId: number | string;
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
    taskIdText?: string | null;
    status: AiRefinementTaskStatus;
    capability: string;
    contentType: string;
    contentId: number;
    requestedAt?: string | null;
}

export interface AiRefinementTaskPageRecord {
    items: AiRefinementTaskRecord[];
    total: number;
    pageNo: number;
    pageSize: number;
}

export type AiRefinementStreamEventType =
    "started" | "delta" | "progress" | "warning" | "error" | "completed" | string;

export interface AiRefinementStreamEventRecord {
    eventType: AiRefinementStreamEventType;
    eventId?: string | null;
    requestId?: string | null;
    traceId?: string | null;
    stage?: string | null;
    timestamp?: string | null;
    deltaText?: string | null;
    status?: string | null;
    resultFormat?: string | null;
    resultPayload?: string | null;
    artifactReferenceJson?: string | null;
    usage?: Record<string, unknown> | null;
    errorType?: string | null;
    errorMessage?: string | null;
    failureStage?: string | null;
    fallbackUsed?: boolean | null;
}
