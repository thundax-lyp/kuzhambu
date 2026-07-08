export interface MingCustomsRecord {
    id: number;
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

export interface MingCustomsContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface MingCustomsVersionSnapshot {
    contentType?: string | null;
    contentId?: number | null;
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
}
