export interface AiCandidateRecord {
    candidateId: number;
    callId?: number | null;
    capability: string;
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

export interface AiCandidateApplyCommand {
    candidateId: number;
    contentType: string;
    contentId: number;
    capability: string;
    resultFormat: string;
    resultPayload: string;
    changeSummary?: string | null;
}

export interface AiCandidateApplyResult {
    contentType: string;
    contentId: number;
    versionId: number;
    versionNo: number;
}

export interface AiCandidateListRequest {
    contentType?: string | null;
    contentId?: number | null;
    capability?: string | null;
    status?: "PENDING" | "APPLIED" | "REJECTED" | string | null;
}

export interface AiCandidateRejectRequest {
    candidateId: number;
    errorType: string;
    errorMessage?: string | null;
}
