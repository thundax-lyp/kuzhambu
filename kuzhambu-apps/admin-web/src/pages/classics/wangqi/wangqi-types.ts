export interface WangqiDocumentEventRecord {
    id?: string | null;
    documentId?: string | null;
    title?: string | null;
    occurredAt?: string | null;
    occurredLabel?: string | null;
    summary?: string | null;
    priority?: number | null;
}

export interface WangqiDocumentRecord {
    id: string;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: string | null;
    lifecycleStatus?: "DRAFT" | "PUBLISHED" | "OFFLINE" | "ERROR" | string | null;
    transitionStatus?: "NONE" | "PUBLISHING" | "OFFLINING" | string | null;
    currentPublicationJobId?: string | null;
    currentVersionId?: string | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    events?: WangqiDocumentEventRecord[];
    versionDirty?: boolean;
}

export interface WangqiPublicationActionRecord {
    jobId: string;
    contentType: "WANGQI_DOCUMENT";
    contentId: string;
    lifecycleStatus: string;
    transitionStatus: string;
}

export interface WangqiPublicationBatchRecord {
    acceptedCount: number;
    rejectedCount: number;
    items: Array<{
        contentId: string;
        accepted: boolean;
        jobId?: string | null;
        reason?: string | null;
    }>;
}

export interface WangqiSourceFileRecord {
    documentId: string;
    storageObjectId?: string | null;
    originalFilename?: string | null;
    contentType?: string | null;
    size?: number | null;
    contentUrl?: string | null;
}

export type WangqiSourceFileContentMode = "preview" | "download";

export interface WangqiContentVersionRecord {
    id: string;
    contentType?: string | null;
    contentId?: string | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface WangqiTagSnapshotRecord {
    id?: string | null;
    tagId?: string | null;
    tagNameSnapshot?: string | null;
}

export interface WangqiQaPairSnapshotRecord {
    id?: string | null;
    question?: string | null;
    answer?: string | null;
}

export interface WangqiVersionSnapshot {
    contentType?: string | null;
    contentId?: string | null;
    contentUpdatedAt?: string | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: string | null;
    tags?: WangqiTagSnapshotRecord[];
    qaPairs?: WangqiQaPairSnapshotRecord[];
}
