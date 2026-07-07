/* eslint-disable local/service-input-type-location */

import type { PageQuery } from "@/types/page";

export type QualityReportStatus = "PUBLISHED" | "DRAFT" | string;

export interface QualityReportRecord {
    reportId: number;
    reportNo?: string | null;
    graphVersionId?: number | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    reportStatus?: QualityReportStatus | null;
    entityTotalCount?: number | null;
    entityConfirmedCount?: number | null;
    relationTotalCount?: number | null;
    relationConfirmedCount?: number | null;
    lineageTotalCount?: number | null;
    lineageConfirmedCount?: number | null;
    entityCoverageRate?: number | null;
    relationAccuracyRate?: number | null;
    lineageCoverageRate?: number | null;
    completenessRate?: number | null;
    annotationCount?: number | null;
    issueCount?: number | null;
    generatedBy?: number | null;
    generatedAt?: number | null;
    publishedAt?: number | null;
}

export interface QualityReportIssueRecord {
    issueId: number;
    issueType?: string | null;
    severity?: string | null;
    objectType?: string | null;
    objectKey?: string | null;
    title?: string | null;
    description?: string | null;
    suggestion?: string | null;
    href?: string | null;
    priority?: number | null;
}

export interface QualityReportSourceDetailRecord {
    detailId: number;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    graphVersionId?: number | null;
    appliedAt?: number | null;
    annotationCount?: number | null;
    issueCount?: number | null;
    status?: string | null;
    href?: string | null;
}

export interface QualityReportAnnotationRecord {
    annotationId: number;
    objectType?: string | null;
    objectKey?: string | null;
    graphVersionId?: number | null;
    annotationStatus?: string | null;
    annotationLabel?: string | null;
    comment?: string | null;
}

export interface QualityReportDetailRecord {
    report?: QualityReportRecord | null;
    issues?: QualityReportIssueRecord[] | null;
    sourceDetails?: QualityReportSourceDetailRecord[] | null;
    annotations?: QualityReportAnnotationRecord[] | null;
}

export interface GenerateQualityReportCommand {
    graphVersionId: number;
    generatedBy?: number | null;
}

export interface ReextractLowQualityCategoryCommand {
    reportId: number;
    sourceCategoryCode: string;
    taskType?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    modelId?: number | null;
    modelName?: string | null;
    promptMessagesJson?: string | null;
    inputPayloadJson?: string | null;
    requestedBy?: number | null;
}

export interface ReextractLowQualityCategoryRecord {
    reportId?: number | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    taskId?: number | null;
    batchJobId?: number | null;
    taskType?: string | null;
    triggerSource?: string | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
}

export type QualityReportPageQuery = PageQuery<{
    graphVersionId?: number | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    reportStatus?: QualityReportStatus | null;
}>;

export interface QualityReportDetailQuery {
    reportId: number;
}

export interface QualityReportLatestQuery {
    graphVersionId?: number | null;
}
