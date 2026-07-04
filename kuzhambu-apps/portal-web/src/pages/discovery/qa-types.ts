export interface DiscoveryQaOpenSessionRequest {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    ownerUserId?: number | null;
    requestId?: string | null;
    scope?: string | null;
    title?: string | null;
    traceId?: string | null;
}

export interface DiscoveryQaOpenSessionResponse {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    lastMessageAt?: number | null;
    openedAt?: number | null;
    scope?: string | null;
    sessionId?: number | null;
    status?: string | null;
    title?: string | null;
}

export interface DiscoveryQaSessionPageQuery {
    ownerUserId?: number | null;
    pageNo?: number | null;
    pageSize?: number | null;
    scope?: string | null;
}

export interface DiscoveryQaSessionPageResponse {
    items?: DiscoveryQaOpenSessionResponse[] | null;
    pageNo?: number | null;
    pageSize?: number | null;
    total?: number | null;
}

export interface DiscoveryQaGetSessionRequest {
    ownerUserId?: number | null;
    sessionId: number;
}

export interface DiscoveryQaDeleteSessionRequest {
    ownerUserId?: number | null;
    sessionId: number;
}

export interface DiscoveryQaExportSessionRequest {
    format?: string | null;
    ownerUserId?: number | null;
    sessionId: number;
}

export interface DiscoveryQaExportSessionResponse {
    completedAt?: number | null;
    contentType?: string | null;
    exportId?: number | null;
    exportStatus?: string | null;
    failureReason?: string | null;
    filename?: string | null;
    format?: string | null;
    requestedAt?: number | null;
    sessionId?: number | null;
    storageObjectId?: number | null;
}

export interface QaChatCompletionMessage {
    content: string;
    role: string;
}

export interface QaChatCompletionMetadata {
    contextContentId?: number | null;
    contextContentType?: string | null;
    sessionId: number;
}

export interface DiscoveryQaChatCompletionRequest {
    messages: QaChatCompletionMessage[];
    metadata?: QaChatCompletionMetadata | null;
    model: string;
    options?: Record<string, unknown> | null;
    requestId?: string | null;
    stream: boolean;
    traceId?: string | null;
}

export interface QaChatCompletionChoiceMessage {
    content?: string | null;
    role?: string | null;
}

export interface QaChatCompletionChoice {
    finishReason?: string | null;
    index?: number | null;
    message?: QaChatCompletionChoiceMessage | null;
}

export interface DiscoveryQaChatCompletionSource {
    contentId?: number | string | null;
    contentType?: string | null;
    knowledgeBase?: string | null;
    locationLabel?: string | null;
    score?: number | null;
    sourceId?: string | null;
    sourcePath?: string | null;
    sourceRank?: number | null;
    sourceStatus?: string | null;
    snippet?: string | null;
    titleSnapshot?: string | null;
}

export interface DiscoveryQaChatCompletionResponse {
    answerStatus?: string | null;
    failureReason?: string | null;
    id?: string | null;
    model?: string | null;
    questionMessageId?: number | null;
    answerMessageId?: number | null;
    sessionId?: number | null;
    choices?: QaChatCompletionChoice[] | null;
    sources?: DiscoveryQaChatCompletionSource[] | null;
}
