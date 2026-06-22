import { postJson } from "@/api/http";
import type {
    AiCandidateApplyCommand,
    AiCandidateApplyResult,
    AiCandidateListRequest,
    AiCandidateRejectRequest,
    AiCandidateRecord
} from "@/api/ai/ai-candidate-types";

const AI_INVOCATION_CANDIDATE_PATH = "/ai/invocation/candidate";
const CLASSICS_CONTENT_CANDIDATE_PATH = "/classics/content/ai-candidates";

export const listCandidates = (request: AiCandidateListRequest) => {
    return postJson<AiCandidateRecord[], AiCandidateListRequest>(`${AI_INVOCATION_CANDIDATE_PATH}/list`, {
        body: request
    });
};

export const applyCandidate = (request: AiCandidateApplyCommand) => {
    return postJson<AiCandidateApplyResult, AiCandidateApplyCommand>(
        `${CLASSICS_CONTENT_CANDIDATE_PATH}/apply`,
        {
            body: request
        }
    );
};

export const rejectCandidate = (request: AiCandidateRejectRequest) => {
    return postJson<AiCandidateRecord, AiCandidateRejectRequest>(
        `${AI_INVOCATION_CANDIDATE_PATH}/reject`,
        {
            body: request
        }
    );
};
