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
    lifecycleStatus?: "DRAFT" | "PUBLISHED" | "OFFLINE" | "ERROR" | string | null;
    transitionStatus?: "NONE" | "PUBLISHING" | "OFFLINING" | string | null;
    currentPublicationJobId?: string | null;
}

export interface MingCustomsPublicationActionRecord {
    jobId: string;
    contentType: "MING_CUSTOMS";
    contentId: string;
    lifecycleStatus: string;
    transitionStatus: string;
}

export interface MingCustomsPublicationBatchRecord {
    acceptedCount: number;
    rejectedCount: number;
    items: Array<{
        contentId: string;
        accepted: boolean;
        jobId?: string | null;
        reason?: string | null;
    }>;
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
    tags?: MingCustomsTagSnapshotRecord[];
    qaPairs?: MingCustomsQaPairSnapshotRecord[];
}
