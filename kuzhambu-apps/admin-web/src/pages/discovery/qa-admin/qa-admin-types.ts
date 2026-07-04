import type { Page } from "@/types/page";

export interface KnowledgeHealthRecord {
    checkedAt?: number | null;
    failureReason?: string | null;
    knowledgeBaseName?: string | null;
    provider?: string | null;
    raw?: unknown;
    status?: string | null;
}

export interface KnowledgeSyncItemRecord {
    contentId?: number | null;
    contentType?: string | null;
    createdAt?: number | null;
    currentVersionNo?: number | null;
    externalKnowledgeBaseId?: string | null;
    externalKnowledgeItemId?: string | null;
    failureReason?: string | null;
    knowledgeBaseName?: string | null;
    knowledgeRevision?: string | null;
    provider?: string | null;
    sourceId?: string | null;
    syncStatus?: string | null;
    syncedAt?: number | null;
    updatedAt?: number | null;
}

export type KnowledgeSyncItemPageRecord = Page<KnowledgeSyncItemRecord>;

export interface ProviderTraceRecord {
    externalChatId?: string | null;
    externalKnowledgeBaseId?: string | null;
    externalKnowledgeItemIds?: string | null;
    failureReason?: string | null;
    latencyMs?: number | null;
    messageId?: number | null;
    provider?: string | null;
    providerRequestId?: string | null;
    raw?: string | null;
    rawQuestion?: string | null;
    retrievedAt?: string | null;
    traceId?: number | null;
}

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
