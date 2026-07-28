export interface MingCustomsRecord {
    id: string;
    title?: string | null;
    category?: string | null;
    chapter?: string | null;
    section?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    originalExcerpts?: string | null;
    visibility?: string | null;
}

export interface MingCustomsKeywordCloudItem {
    keyword: string;
    count: number;
}

export type MingCustomsKeywordCloudRecord = MingCustomsKeywordCloudItem;

export interface MingCustomsTagCloudItem {
    tagId?: string | null;
    tagNameSnapshot: string;
    count: number;
}

export type MingCustomsTagCloudRecord = MingCustomsTagCloudItem;

export interface MingCustomsContentVersionRecord {
    id: string;
    contentType?: string | null;
    contentId?: string | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface MingCustomsTagSnapshotRecord {
    id?: string | null;
    tagId?: string | null;
    tagNameSnapshot?: string | null;
}

export interface MingCustomsQaPairSnapshotRecord {
    id?: string | null;
    question?: string | null;
    answer?: string | null;
}

export interface MingCustomsVersionSnapshot {
    contentType?: string | null;
    contentId?: string | null;
    contentUpdatedAt?: string | null;
    title?: string | null;
    category?: string | null;
    chapter?: string | null;
    section?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    originalExcerpts?: string | null;
    visibility?: string | null;
    tags?: MingCustomsTagSnapshotRecord[];
    qaPairs?: MingCustomsQaPairSnapshotRecord[];
}
