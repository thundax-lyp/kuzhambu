import { postJson } from "@/api/http";
import type {
    AiCandidateApplyRecord,
    AiCandidateApplyPayload,
    AiCandidateRecord
} from "./ai-candidate-types";

export interface AiCandidateListQuery {
    contentType?: string | null;
    contentId?: number | null;
    objectId?: number | null;
    capability?: string | null;
    status?: "PENDING" | "APPLIED" | "REJECTED" | string | null;
}

export type AiCandidateApplyCommand = AiCandidateApplyPayload;

export interface AiCandidateRejectCommand {
    candidateId: number;
    errorType: string;
    errorMessage?: string | null;
}

const AI_INVOCATION_CANDIDATE_PATH = "/ai/invocation/candidate";
const CLASSICS_CONTENT_CANDIDATE_PATH = "/classics/content/ai-candidates";

export const list = (query: AiCandidateListQuery) => {
    return postJson<AiCandidateRecord[], AiCandidateListQuery>(
        `${AI_INVOCATION_CANDIDATE_PATH}/list`,
        {
            body: query
        }
    );
};

export const apply = (command: AiCandidateApplyCommand) => {
    return postJson<AiCandidateApplyRecord, AiCandidateApplyCommand>(
        `${CLASSICS_CONTENT_CANDIDATE_PATH}/change`,
        {
            body: command
        }
    );
};

export const reject = (command: AiCandidateRejectCommand) => {
    return postJson<AiCandidateRecord, AiCandidateRejectCommand>(
        `${AI_INVOCATION_CANDIDATE_PATH}/reject`,
        {
            body: command
        }
    );
};
