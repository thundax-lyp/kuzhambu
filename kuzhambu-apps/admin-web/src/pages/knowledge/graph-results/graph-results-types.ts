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
    refinementApplied?: boolean | null;
    lastRefinementTaskId?: number | null;
    lastRefinementAppliedAt?: number | null;
}

export interface GraphEntityRecord {
    entityId: number;
    entityKey?: string | null;
    name?: string | null;
    entityType?: string | null;
    description?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: number | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}

export interface GraphRelationRecord {
    relationId: number;
    relationKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: number | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}

export interface GraphLineageNodeRecord {
    nodeId: number;
    nodeKey?: string | null;
    name?: string | null;
    nodeType?: string | null;
    generation?: number | null;
    gender?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: number | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}

export interface GraphLineageRelationRecord {
    relationId: number;
    relationKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: number | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}
