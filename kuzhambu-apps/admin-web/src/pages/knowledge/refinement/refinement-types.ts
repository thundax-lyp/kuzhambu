export type RefinementTaskType = "GRAPH" | "RELATION" | "LINEAGE" | string;
export type RefinementTaskStatus = "DRAFT" | "SUBMITTED" | "APPLIED" | "CANCELLED" | string;
export type RefinementConfirmationStatus = "PENDING" | "MANUAL_CONFIRMED" | string;
export type RefinementOperationType =
    "UNCHANGED" | "UPDATED" | "DELETED" | "CONFIRMED" | "ADDED" | string;

export interface RefinementProgressSummary {
    entityPendingCount?: number | null;
    entityConfirmedCount?: number | null;
    relationPendingCount?: number | null;
    relationConfirmedCount?: number | null;
}

export interface RefinementWorkbenchRecord {
    refinementTaskId: string;
    graphVersionId?: string | null;
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    status?: RefinementTaskStatus | null;
    openedBy?: string | null;
    openedAt?: number | null;
    progressSummary?: RefinementProgressSummary | null;
}

export interface RefinementEntityOption {
    entityKey: string;
    name?: string | null;
}

export interface RefinementEntityRecord {
    draftId?: string | null;
    entityId?: string | null;
    entityKey?: string | null;
    originType?: string | null;
    operationType?: RefinementOperationType | null;
    name?: string | null;
    entityType?: string | null;
    description?: string | null;
    confirmationStatus?: RefinementConfirmationStatus | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
}

export interface RefinementRelationRecord {
    draftId?: string | null;
    relationId?: string | null;
    relationKey?: string | null;
    originType?: string | null;
    operationType?: RefinementOperationType | null;
    sourceEntityKey?: string | null;
    targetEntityKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    confirmationStatus?: RefinementConfirmationStatus | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
}

export interface RefinementLineageNodeRecord {
    draftId?: string | null;
    nodeId?: string | null;
    nodeKey?: string | null;
    originType?: string | null;
    operationType?: RefinementOperationType | null;
    name?: string | null;
    nodeType?: string | null;
    generation?: number | null;
    gender?: string | null;
    confirmationStatus?: RefinementConfirmationStatus | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
}

export interface RefinementLineageRelationRecord {
    draftId?: string | null;
    relationId?: string | null;
    relationKey?: string | null;
    originType?: string | null;
    operationType?: RefinementOperationType | null;
    sourceNodeKey?: string | null;
    targetNodeKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    confirmationStatus?: RefinementConfirmationStatus | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
}

export interface QualityAnnotationRecord {
    annotationId: string;
    objectType?: QualityAnnotationObjectType | null;
    objectKey?: string | null;
    graphVersionId?: string | null;
    annotationStatus?: QualityAnnotationStatus | null;
    annotationLabel?: QualityAnnotationLabel | null;
    comment?: string | null;
}

export type QualityAnnotationObjectType =
    "ENTITY" | "RELATION" | "LINEAGE_NODE" | "LINEAGE_RELATION" | string;
export type QualityAnnotationStatus = "PASSED" | "ISSUE" | "IGNORED" | string;
export type QualityAnnotationLabel =
    | "MISSING_SOURCE"
    | "WRONG_ENTITY"
    | "WRONG_RELATION"
    | "INCOMPLETE_LINEAGE"
    | "DUPLICATED"
    | "OTHER"
    | string;

export interface QualityAnnotationTarget {
    objectType: QualityAnnotationObjectType;
    objectKey: string;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    graphVersionId?: string | null;
}

export interface QualitySummaryRecord {
    entityCoverageRate?: number | null;
    relationAccuracyRate?: number | null;
    completenessRate?: number | null;
}

export interface RefinementDetailRecord {
    refinementTaskId: string;
    graphVersionId?: string | null;
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    status?: RefinementTaskStatus | null;
    progressSummary?: RefinementProgressSummary | null;
    entities?: RefinementEntityRecord[] | null;
    relations?: RefinementRelationRecord[] | null;
    lineageNodes?: RefinementLineageNodeRecord[] | null;
    lineageRelations?: RefinementLineageRelationRecord[] | null;
    entityOptions?: RefinementEntityOption[] | null;
}

export interface RefinementApplyRecord {
    refinementTaskId: string;
    graphVersionId?: string | null;
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    status?: RefinementTaskStatus | null;
    appliedAt?: number | null;
    graphRefreshRequired?: boolean | null;
    regenerateSupported?: boolean | null;
    sourceTaskId?: string | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    triggerSource?: string | null;
    nextAction?: string | null;
    qualityReportRefreshRequired?: boolean | null;
}
