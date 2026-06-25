export interface DiscoveryQaOpenSessionRequest {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    ownerUserId: number;
    requestId?: string | null;
    scope?: string | null;
    title?: string | null;
    traceId?: string | null;
}

export interface DiscoveryQaAskQuestionRequest {
    contextTurnCount?: number | null;
    operatorId?: string | null;
    operatorType?: string | null;
    requestId?: string | null;
    sessionId: number;
    question: string;
    traceId?: string | null;
}

export interface DiscoveryQaOpenSessionResponse {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    lastMessageAt?: number | null;
    openedAt?: number | null;
    ownerUserId?: number | null;
    scope?: string | null;
    sessionId?: number | null;
    status?: string | null;
    title?: string | null;
}

export interface DiscoveryQaSourceResponse {
    contentId?: number | null;
    contentType?: string | null;
    knowledgeBase?: string | null;
    locationLabel?: string | null;
    score?: number | string | null;
    sourceId?: number | null;
    sourceRank?: number | null;
    sourceStatus?: string | null;
    snippet?: string | null;
    titleSnapshot?: string | null;
}

export interface DiscoveryQaTraceSummaryResponse {
    candidateCount?: number | null;
    expandedTermsJson?: string | null;
    linkedEntitiesJson?: string | null;
    rewrittenQuestion?: string | null;
    traceId?: number | null;
}

export interface DiscoveryQaAskQuestionResponse {
    answer?: string | null;
    answerMessageId?: number | null;
    answerStatus?: string | null;
    failureReason?: string | null;
    question?: string | null;
    questionMessageId?: number | null;
    searchLogId?: string | null;
    sessionId?: number | null;
    sources?: DiscoveryQaSourceResponse[] | null;
    traceSummary?: DiscoveryQaTraceSummaryResponse | null;
}
