export type AiCandidateCapability =
    "translate" | "summary" | "tags" | "qa" | "image_analysis" | "visual" | "fusion" | "image_gen";

export interface AiCandidateRecord {
    candidateId: number;
    callId?: number | null;
    capability: AiCandidateCapability | string;
    contentType: string;
    contentId: number;
    objectId?: number | null;
    resultFormat: string;
    resultPayload?: string | null;
    status: "PENDING" | "APPLIED" | "REJECTED" | string;
    promptVersionId?: number | null;
    modelName?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    requestedAt?: string | null;
    appliedAt?: string | null;
}

export interface AiCandidateApplyRecord {
    contentType: string;
    contentId: number;
    versionId: number;
    versionNo: number;
}

export interface AiCandidateApplyPayload {
    candidateId: number;
    contentType: string;
    contentId: number;
    capability: AiCandidateCapability | string;
    objectId?: number | null;
    resultFormat: string;
    resultPayload: string;
    changeSummary?: string | null;
}

export interface AiCandidateRejectPayload {
    candidateId: number;
    errorType: string;
    errorMessage?: string | null;
}
