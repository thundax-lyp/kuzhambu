export type AiCandidateCapability =
    "translate" | "summary" | "tags" | "qa" | "image_analysis" | "visual" | "fusion" | "image_gen";

export interface AiCandidateRecord {
    candidateId: string;
    candidateIdText?: string | null;
    callId?: string | null;
    callIdText?: string | null;
    capability: AiCandidateCapability | string;
    contentType: string;
    contentId: string;
    objectId?: string | null;
    resultFormat: string;
    resultPayload?: string | null;
    status: "PENDING" | "APPLIED" | "REJECTED" | string;
    promptVersionId?: string | null;
    modelName?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    requestedAt?: string | null;
    appliedAt?: string | null;
}

export interface AiCandidateApplyRecord {
    contentType: string;
    contentId: string;
    versionId: string;
    versionNo: number;
}

export interface AiCandidateApplyPayload {
    candidateId: string;
    contentType: string;
    contentId: string;
    capability: AiCandidateCapability | string;
    objectId?: string | null;
    resultFormat: string;
    resultPayload: string;
    changeSummary?: string | null;
}

export interface AiCandidateRejectPayload {
    candidateId: string;
    errorType: string;
    errorMessage?: string | null;
}
