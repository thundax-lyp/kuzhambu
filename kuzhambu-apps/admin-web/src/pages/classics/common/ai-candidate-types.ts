export type AiCandidateCapability =
    "translate" | "summary" | "tags" | "qa" | "image_analysis" | "visual" | "fusion" | "image_gen";

export interface AiCandidateRecord {
    candidateId: number;
    candidateIdText?: string | null;
    callId?: number | null;
    callIdText?: string | null;
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
    candidateId: string;
    contentType: string;
    contentId: number;
    capability: AiCandidateCapability | string;
    objectId?: number | null;
    resultFormat: string;
    resultPayload: string;
    changeSummary?: string | null;
}

export interface AiCandidateRejectPayload {
    candidateId: string;
    errorType: string;
    errorMessage?: string | null;
}
