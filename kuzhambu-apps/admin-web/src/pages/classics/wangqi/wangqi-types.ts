export interface WangqiDocumentEventRecord {
    id?: number | null;
    documentId?: number | null;
    title?: string | null;
    occurredAt?: string | null;
    occurredLabel?: string | null;
    summary?: string | null;
    priority?: number | null;
}

export interface WangqiDocumentRecord {
    id: number;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
    currentVersionId?: number | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    events?: WangqiDocumentEventRecord[];
    versionDirty?: boolean;
}

export interface WangqiSourceFileRecord {
    documentId: number;
    storageObjectId?: number | null;
    originalFilename?: string | null;
    contentType?: string | null;
    size?: number | null;
    contentUrl?: string | null;
}

export type WangqiSourceFileContentMode = "preview" | "download";

export interface WangqiContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface WangqiTagSnapshotRecord {
    id?: number | null;
    tagId?: number | null;
    tagNameSnapshot?: string | null;
}

export interface WangqiQaPairSnapshotRecord {
    id?: number | null;
    question?: string | null;
    answer?: string | null;
}

export interface WangqiVersionSnapshot {
    contentType?: string | null;
    contentId?: number | null;
    contentUpdatedAt?: string | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
    tags?: WangqiTagSnapshotRecord[];
    qaPairs?: WangqiQaPairSnapshotRecord[];
}
