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
    contentId?: string | null;
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
    title?: string | null;
    updatedAt?: number | null;
}

export type KnowledgeSyncItemPageRecord = Page<KnowledgeSyncItemRecord>;

export interface DiscoveryQaSessionMessageRecord {
    answeredAt?: string | null;
    content?: string | null;
    contextTurnCount?: number | null;
    failureReason?: string | null;
    messageId?: string | null;
    messageStatus?: string | null;
    role?: string | null;
    sentAt?: string | null;
    sessionId?: string | null;
}

export interface DiscoveryQaSessionDetailRecord {
    contextContentId?: string | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    lastMessageAt?: number | null;
    messages?: DiscoveryQaSessionMessageRecord[] | null;
    openedAt?: number | null;
    ownerUserId?: string | null;
    scope?: string | null;
    sessionId?: string | null;
    status?: string | null;
    title?: string | null;
}

export type DiscoveryQaSessionPageRecord = Page<DiscoveryQaSessionDetailRecord>;

export interface DiscoveryQaSessionExportRecord {
    completedAt?: number | null;
    contentType?: string | null;
    exportId?: string | null;
    exportStatus?: string | null;
    failureReason?: string | null;
    filename?: string | null;
    format?: string | null;
    requestedAt?: number | null;
    sessionId?: string | null;
    storageObjectId?: string | null;
}
