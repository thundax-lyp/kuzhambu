export type AiRefinementTaskStatus =
    "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "PARTIAL" | "CANCELLED" | string;

export type AiRefinementTaskCapability = "summary" | "tags" | "qa";

export interface AiRefinementTaskRecord {
    taskId: string;
    taskIdText?: string | null;
    status: AiRefinementTaskStatus;
    scope?: string | null;
    capability: string;
    contentType: string;
    contentId: string;
    objectId?: string | null;
    serviceRole?: string | null;
    modelId?: string | null;
    modelName?: string | null;
    promptVersionId?: string | null;
    requestId?: string | null;
    traceId?: string | null;
    callId?: string | null;
    candidateId?: string | null;
    candidateIdText?: string | null;
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
    contentId: string;
    objectId?: string | null;
    serviceId?: string | null;
    serviceRole?: string | null;
    modelId?: string | null;
    modelName?: string | null;
    promptVersionId?: string | null;
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
    taskId: string;
}

export interface AiRefinementTaskCancelPayload {
    taskId: string;
}

export interface AiRefinementTaskPagePayload {
    capability?: string | null;
    status?: AiRefinementTaskStatus | null;
    contentType?: string | null;
    contentId?: string | null;
    pageNo?: number;
    pageSize?: number;
}

export interface AiRefinementTaskAcceptedRecord {
    taskId: string;
    taskIdText?: string | null;
    status: AiRefinementTaskStatus;
    capability: string;
    contentType: string;
    contentId: string;
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
