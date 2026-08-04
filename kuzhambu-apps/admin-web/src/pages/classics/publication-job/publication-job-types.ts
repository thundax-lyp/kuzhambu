export type ClassicsPublicationJobType = "PUBLISH" | "OFFLINE";

export type ClassicsPublicationJobStatus =
    | "QUEUED"
    | "SNAPSHOT_READY"
    | "ES_PREPARED"
    | "FASTGPT_PREPARED"
    | "ES_READY"
    | "FASTGPT_READY"
    | "ES_DISABLED"
    | "FASTGPT_DISABLED"
    | "CONTENT_COMMITTED";

export type ClassicsPublicationJobResultStatus = "RUNNING" | "FAILED" | "SUCCEEDED";

export type ClassicsPublicationCleanupStatus =
    "NONE" | "PENDING" | "RUNNING" | "FAILED" | "SUCCEEDED";

export type ClassicsPublicationLifecycleStatus = "DRAFT" | "PUBLISHED" | "OFFLINE" | "ERROR";

export type ClassicsPublicationContentType = "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS";

export interface ClassicsPublicationJobRecord {
    id: string;
    jobType: ClassicsPublicationJobType;
    jobStatus: ClassicsPublicationJobStatus;
    jobResultStatus: ClassicsPublicationJobResultStatus;
    failureStep?: ClassicsPublicationJobStatus | null;
    contentType: ClassicsPublicationContentType;
    contentId: string;
    contentTitleSnapshot?: string | null;
    contentDeletedAt?: string | null;
    sourceLifecycleStatus: ClassicsPublicationLifecycleStatus;
    targetLifecycleStatus: ClassicsPublicationLifecycleStatus;
    contentVersionId?: string | null;
    contentVersionNo?: number | null;
    attemptCount: number;
    maxAttempts: number;
    expiresAt?: string | null;
    nextRetryAt?: string | null;
    esDocumentId?: string | null;
    esCleanupStatus: ClassicsPublicationCleanupStatus;
    fastgptCollectionId?: string | null;
    fastgptCleanupStatus: ClassicsPublicationCleanupStatus;
    failureReason?: string | null;
    detailJsonSummary?: string | null;
    requestedAt: string;
    startedAt?: string | null;
    finishedAt?: string | null;
}
