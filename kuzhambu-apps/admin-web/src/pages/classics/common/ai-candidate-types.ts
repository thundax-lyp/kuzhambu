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

export interface AiCandidateApplyRecord {
    contentType: string;
    contentId: number;
    versionId: number;
    versionNo: number;
}

export interface AiCandidateApplyCommand {
    candidateId: number;
    contentType: string;
    contentId: number;
    capability: string;
    objectId?: number | null;
    resultFormat: string;
    resultPayload: string;
    changeSummary?: string | null;
}
