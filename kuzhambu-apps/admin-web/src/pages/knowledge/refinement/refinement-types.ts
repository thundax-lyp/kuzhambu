/* eslint-disable local/service-input-type-location */

import type { PageQuery } from "@/types/page";

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
    refinementTaskId: number;
    graphVersionId?: number | null;
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    status?: RefinementTaskStatus | null;
    openedBy?: number | null;
    openedAt?: number | null;
    progressSummary?: RefinementProgressSummary | null;
}

export interface RefinementEntityOption {
    entityKey: string;
    name?: string | null;
}

export interface RefinementEntityRecord {
    draftId?: number | null;
    entityId?: number | null;
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
    draftId?: number | null;
    relationId?: number | null;
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
    draftId?: number | null;
    nodeId?: number | null;
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
    draftId?: number | null;
    relationId?: number | null;
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
    annotationId: number;
    objectType?: "ENTITY" | "RELATION" | string | null;
    objectKey?: string | null;
    graphVersionId?: number | null;
    annotationStatus?: string | null;
    annotationLabel?: string | null;
    comment?: string | null;
}

export interface QualitySummaryRecord {
    entityCoverageRate?: number | null;
    relationAccuracyRate?: number | null;
    completenessRate?: number | null;
}

export interface RefinementDetailRecord {
    refinementTaskId: number;
    graphVersionId?: number | null;
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
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

export type RefinementTaskPageQuery = PageQuery<{
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    status?: RefinementTaskStatus | null;
}>;

export interface RefinementTaskOpenCommand {
    graphVersionId: number;
    openedBy?: number | null;
}

export interface RefinementTaskIdCommand {
    refinementTaskId: number;
}

export interface RefinementTaskApplyCommand extends RefinementTaskIdCommand {
    appliedBy?: number | null;
}

export interface UpsertRefinementEntityCommand {
    refinementTaskId: number;
    entityId?: number | null;
    entityKey?: string | null;
    name?: string | null;
    entityType?: string | null;
    description?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface ConfirmRefinementEntityCommand {
    refinementTaskId: number;
    entityKey: string;
    operatorId?: number | null;
}

export interface DeleteRefinementEntityCommand {
    refinementTaskId: number;
    entityKey: string;
    operatorId?: number | null;
}

export interface UpsertRefinementRelationCommand {
    refinementTaskId: number;
    relationId?: number | null;
    relationKey?: string | null;
    sourceEntityKey?: string | null;
    targetEntityKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface ConfirmRefinementRelationCommand {
    refinementTaskId: number;
    relationKey: string;
    operatorId?: number | null;
}

export interface DeleteRefinementRelationCommand {
    refinementTaskId: number;
    relationKey: string;
    operatorId?: number | null;
}

export interface UpsertRefinementLineageNodeCommand {
    refinementTaskId: number;
    nodeId?: number | null;
    nodeKey?: string | null;
    name?: string | null;
    nodeType?: string | null;
    generation?: number | null;
    gender?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface UpsertRefinementLineageRelationCommand {
    refinementTaskId: number;
    relationId?: number | null;
    relationKey?: string | null;
    sourceNodeKey?: string | null;
    targetNodeKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface UpsertQualityAnnotationCommand {
    annotationId?: number | null;
    objectType?: "ENTITY" | "RELATION" | string | null;
    objectKey?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    graphVersionId?: number | null;
    annotationStatus?: string | null;
    annotationLabel?: string | null;
    comment?: string | null;
    operatorId?: number | null;
}

export type QualityAnnotationPageQuery = PageQuery<{
    refinementTaskId: number;
    objectType?: string | null;
}>;
