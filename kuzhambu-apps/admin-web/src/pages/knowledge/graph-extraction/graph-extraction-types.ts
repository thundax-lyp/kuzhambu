import type { GraphVersionRecord } from "@/pages/knowledge/graph-results/graph-results-types";
import type { QualitySummaryRecord } from "@/pages/knowledge/refinement/refinement-types";

export type GraphExtractionTaskType = "RELATION" | "GRAPH" | "LINEAGE" | string;
export type GraphExtractionTriggerSource =
    "MANUAL" | "QUALITY_REPORT" | "REGENERATE" | "REFINEMENT_APPLIED" | string;
export type GraphExtractionTaskStatus =
    "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED" | "APPLIED" | string;
export type GraphWorkbenchSourceContentType =
    "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS" | string;
export type GraphWorkbenchNodeType = "SOURCE_ROOT" | "CATEGORY" | "VOLUME" | "MANUSCRIPT" | string;
export type GraphWorkbenchStatus =
    | "NOT_EXTRACTED"
    | "EXTRACTING"
    | "EXTRACTION_FAILED"
    | "CANDIDATE_READY"
    | "APPLIED"
    | "REFINING"
    | "REFINED"
    | "QUALITY_ISSUE"
    | string;

export interface GraphExtractionTaskRecord {
    taskId: string;
    batchJobId?: string | null;
    triggerSource?: GraphExtractionTriggerSource | null;
    taskType?: GraphExtractionTaskType | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    selectionScopeJson?: string | null;
    replaceUnconfirmedOnly?: boolean | null;
    parentTaskId?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
    aiCallId?: string | null;
    aiCandidateId?: string | null;
    status?: GraphExtractionTaskStatus | null;
    errorType?: string | null;
    errorMessage?: string | null;
    requestedBy?: string | null;
    requestedAt?: number | null;
    completedAt?: number | null;
    appliedAt?: number | null;
}

export interface GraphExtractionBatchCancelRecord {
    batchJobId: string;
    status?: GraphExtractionTaskStatus | null;
    cancelledCount?: number | null;
    completedCount?: number | null;
    failedCount?: number | null;
}

export interface GraphWorkbenchManuscriptNode {
    nodeKey: string;
    parentKey?: string | null;
    nodeType?: GraphWorkbenchNodeType | null;
    title?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourceContentId?: string | null;
    sourcePath?: string | null;
    graphStatus?: GraphWorkbenchStatus | null;
    latestTaskId?: string | null;
    latestGraphVersionId?: string | null;
    children?: GraphWorkbenchManuscriptNode[] | null;
}

export interface GraphWorkbenchManuscriptRecord {
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourceContentId?: string | null;
    title?: string | null;
    summary?: string | null;
    sourcePath?: string | null;
    currentVersionNo?: number | null;
    graphStatus?: GraphWorkbenchStatus | null;
    latestExtractionTask?: GraphExtractionTaskRecord | null;
    latestGraphVersion?: GraphVersionRecord | null;
    qualitySummary?: QualitySummaryRecord | null;
}

export interface GraphWorkbenchCandidateRecord {
    taskId?: string | null;
    aiCandidateId?: string | null;
    taskType?: GraphExtractionTaskType | null;
    status?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourceContentId?: string | null;
    candidatePayloadJson?: string | null;
}

export interface GraphWorkbenchCandidateApplyRecord {
    taskId?: string | null;
    graphVersionId?: string | null;
    graphStatus?: GraphWorkbenchStatus | null;
}
