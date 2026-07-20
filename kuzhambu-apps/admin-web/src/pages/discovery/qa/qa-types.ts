export interface DiscoveryQaSessionRecord {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    lastMessageAt?: number | null;
    openedAt?: number | null;
    scope?: string | null;
    sessionId?: string | null;
    status?: string | null;
    title?: string | null;
}

export interface DiscoveryQaSessionPageRecord {
    items?: DiscoveryQaSessionRecord[] | null;
    records?: DiscoveryQaSessionRecord[] | null;
    pageNo?: number | null;
    pageSize?: number | null;
    total?: number | null;
}

export interface DiscoveryQaExportSessionRecord {
    completedAt?: number | null;
    contentType?: string | null;
    exportId?: number | null;
    exportStatus?: string | null;
    failureReason?: string | null;
    filename?: string | null;
    format?: string | null;
    requestedAt?: number | null;
    sessionId?: string | null;
    storageObjectId?: number | null;
}

export interface DiscoveryQaChatMessage {
    content: string;
    role: string;
}

export interface DiscoveryQaChatMetadata {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    sessionId: string;
}

export interface DiscoveryQaChatChoiceMessage {
    content?: string | null;
    role?: string | null;
}

export interface DiscoveryQaChatChoice {
    finishReason?: string | null;
    index?: number | null;
    message?: DiscoveryQaChatChoiceMessage | null;
}

export interface DiscoveryQaSourceRecord {
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

export interface DiscoveryQaChatCompletionRecord {
    answerStatus?: string | null;
    failureReason?: string | null;
    id?: string | null;
    model?: string | null;
    questionMessageId?: string | null;
    answerMessageId?: string | null;
    sessionId?: string | null;
    choices?: DiscoveryQaChatChoice[] | null;
    sources?: DiscoveryQaSourceRecord[] | null;
}
