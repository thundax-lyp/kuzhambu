export interface DiscoveryQaSessionMessageRecord {
    answeredAt?: string | null;
    content?: string | null;
    contextTurnCount?: number | null;
    failureReason?: string | null;
    messageId?: number | null;
    messageStatus?: string | null;
    role?: string | null;
    sentAt?: string | null;
    sessionId?: number | null;
}

export interface DiscoveryQaSessionDetailRecord {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    lastMessageAt?: number | null;
    messages?: DiscoveryQaSessionMessageRecord[] | null;
    openedAt?: number | null;
    ownerUserId?: number | null;
    scope?: string | null;
    sessionId?: number | null;
    status?: string | null;
    title?: string | null;
}

export interface DiscoveryQaSourceRecord {
    contentId?: number | null;
    contentType?: string | null;
    knowledgeBase?: string | null;
    locationLabel?: string | null;
    score?: number | null;
    snippet?: string | null;
    sourceId?: number | null;
    sourceRank?: number | null;
    sourceStatus?: string | null;
    titleSnapshot?: string | null;
}

export interface DiscoveryQaTraceRecord {
    candidateCount?: number | null;
    contextSnapshot?: string | null;
    expandedTermsJson?: string | null;
    filtersJson?: string | null;
    linkedEntitiesJson?: string | null;
    messageId?: number | null;
    rawQuestion?: string | null;
    retrievedAt?: string | null;
    rewrittenQuestion?: string | null;
    scope?: string | null;
    traceId?: number | null;
}
