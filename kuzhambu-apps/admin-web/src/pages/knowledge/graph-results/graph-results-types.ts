/* eslint-disable local/service-input-type-location */

export interface GraphVersionRecord {
    versionId: number;
    taskId?: string | null;
    candidateId?: number | null;
    taskType?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    versionNo?: number | null;
    status?: string | null;
    appliedAt?: number | null;
}

export interface GraphVersionPageQuery {
    pageNo?: number;
    pageSize?: number;
    taskType?: string | null;
    status?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
}
