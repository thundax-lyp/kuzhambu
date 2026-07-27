import type { GraphVersionRecord } from "@/pages/knowledge/graph-results/graph-results-types";
import type { QualitySummaryRecord } from "@/pages/knowledge/refinement/refinement-types";
import type { GraphExtractionTaskRecord, GraphExtractionTaskType } from "./graph-extraction-types";

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

export interface GraphWorkbenchManuscriptTreeNode {
    nodeKey: string;
    parentKey?: string | null;
    nodeType?: GraphWorkbenchNodeType | null;
    title?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourceContentId?: number | null;
    sourcePath?: string | null;
    graphStatus?: GraphWorkbenchStatus | null;
    latestTaskId?: number | null;
    latestGraphVersionId?: number | null;
    children?: GraphWorkbenchManuscriptTreeNode[] | null;
}

export interface GraphWorkbenchManuscriptDetail {
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourceContentId?: number | null;
    title?: string | null;
    summary?: string | null;
    sourcePath?: string | null;
    currentVersionNo?: number | null;
    graphStatus?: GraphWorkbenchStatus | null;
    latestExtractionTask?: GraphExtractionTaskRecord | null;
    latestGraphVersion?: GraphVersionRecord | null;
    qualitySummary?: QualitySummaryRecord | null;
}

export interface GraphWorkbenchCandidateSummary {
    taskId?: number | null;
    aiCandidateId?: number | null;
    taskType?: GraphExtractionTaskType | null;
    status?: string | null;
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    sourceContentId?: number | null;
    candidatePayloadJson?: string | null;
}

export interface GraphWorkbenchCandidateApplyRecord {
    taskId?: number | null;
    graphVersionId?: number | null;
    graphStatus?: GraphWorkbenchStatus | null;
}
