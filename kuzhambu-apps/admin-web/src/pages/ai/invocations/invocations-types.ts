export interface AiTopCapabilityRecord {
    capability: string;
    invocationCount: number;
}

export interface AiCallSummaryRecord {
    periodStart?: string | null;
    periodEnd?: string | null;
    invocationCount: number;
    succeededInvocationCount: number;
    failedInvocationCount: number;
    avgLatencyMs?: number | null;
    totalCostAmount?: number | string | null;
    topCapabilities: AiTopCapabilityRecord[];
}

export interface AiCallRecord {
    callId: number;
    callIdText?: string | null;
    batchId?: number | null;
    scope?: string | null;
    capability?: string | null;
    contentType?: string | null;
    contentId?: number | null;
    objectId?: number | null;
    serviceRole?: string | null;
    modelId?: number | null;
    modelName?: string | null;
    promptVersionId?: number | null;
    requestId?: string | null;
    traceId?: string | null;
    status?: string | null;
    streamUsed?: boolean | null;
    streamCompleted?: boolean | null;
    fallbackUsed?: boolean | null;
    latencyMs?: number | null;
    inputTokens?: number | null;
    outputTokens?: number | null;
    costAmount?: number | string | null;
    failureStage?: string | null;
    resultFormat?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    warningsJson?: string | null;
    requestedAt?: string | null;
    completedAt?: string | null;
}

export interface AiInvocationCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}
