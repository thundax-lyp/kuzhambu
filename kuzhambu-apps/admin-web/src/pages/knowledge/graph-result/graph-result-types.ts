export interface GraphVersionRecord {
    versionId: string;
    taskId?: string | null;
    candidateId?: string | null;
    taskType?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    versionNo?: number | null;
    status?: string | null;
    appliedAt?: number | null;
    refinementApplied?: boolean | null;
    lastRefinementTaskId?: string | null;
    lastRefinementAppliedAt?: number | null;
}

export interface GraphEntityRecord {
    entityId: string;
    entityKey?: string | null;
    name?: string | null;
    entityType?: string | null;
    description?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: string | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}

export interface GraphRelationRecord {
    relationId: string;
    relationKey?: string | null;
    sourceName?: string | null;
    sourceType?: string | null;
    targetName?: string | null;
    targetType?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: string | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}

export interface GraphLineageNodeRecord {
    nodeId: string;
    nodeKey?: string | null;
    name?: string | null;
    nodeType?: string | null;
    generation?: number | null;
    gender?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: string | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}

export interface GraphLineageRelationRecord {
    relationId: string;
    relationKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    confirmationStatus?: string | null;
    latestVersionId?: string | null;
    sourceRefsJson?: string | null;
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    confirmedAt?: number | null;
}
