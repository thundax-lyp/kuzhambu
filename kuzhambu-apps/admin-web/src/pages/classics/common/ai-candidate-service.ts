import { postJson } from "@/api/http";
import { normalizeId, normalizeNullableId } from "@/types/id";
import type {
    AiCandidateApplyRecord,
    AiCandidateApplyPayload,
    AiCandidateRecord
} from "./ai-candidate-types";

export interface AiCandidateListQuery {
    contentType?: string | null;
    contentId?: string | null;
    objectId?: string | null;
    capability?: string | null;
    status?: "PENDING" | "APPLIED" | "REJECTED" | string | null;
}

export interface AiCandidateGetQuery {
    candidateId: string;
}

export type AiCandidateApplyCommand = AiCandidateApplyPayload;

export interface AiCandidateRejectCommand {
    candidateId: string;
    errorType: string;
    errorMessage?: string | null;
}

const AI_INVOCATION_CANDIDATE_PATH = "/ai/invocation/candidate";
const CLASSICS_CONTENT_CANDIDATE_PATH = "/classics/content/ai-candidates";

const normalizeCandidateRecord = (record: AiCandidateRecord): AiCandidateRecord => ({
    ...record,
    candidateId: normalizeId(record.candidateId),
    callId: normalizeNullableId(record.callId),
    contentId: normalizeId(record.contentId),
    objectId: normalizeNullableId(record.objectId),
    promptVersionId: normalizeNullableId(record.promptVersionId)
});

const normalizeCandidateApplyRecord = (record: AiCandidateApplyRecord): AiCandidateApplyRecord => ({
    ...record,
    contentId: normalizeId(record.contentId),
    versionId: normalizeId(record.versionId)
});

export const list = (query: AiCandidateListQuery) => {
    return postJson<AiCandidateRecord[], AiCandidateListQuery>(
        `${AI_INVOCATION_CANDIDATE_PATH}/list`,
        {
            body: query
        }
    ).then((records) => records.map(normalizeCandidateRecord));
};

export const get = (query: AiCandidateGetQuery) => {
    return postJson<AiCandidateRecord, AiCandidateGetQuery>(`${AI_INVOCATION_CANDIDATE_PATH}/get`, {
        body: query
    }).then(normalizeCandidateRecord);
};

export const apply = (command: AiCandidateApplyCommand) => {
    return postJson<AiCandidateApplyRecord, AiCandidateApplyCommand>(
        `${CLASSICS_CONTENT_CANDIDATE_PATH}/change`,
        {
            body: command
        }
    ).then(normalizeCandidateApplyRecord);
};

export const reject = (command: AiCandidateRejectCommand) => {
    return postJson<AiCandidateRecord, AiCandidateRejectCommand>(
        `${AI_INVOCATION_CANDIDATE_PATH}/reject`,
        {
            body: command
        }
    ).then(normalizeCandidateRecord);
};
