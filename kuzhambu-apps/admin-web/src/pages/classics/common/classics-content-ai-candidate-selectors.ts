import { isSameId, normalizeId } from "@/types/id";

import type { AiCandidateRecord } from "./ai-candidate-types";
import * as aiRefinementTaskService from "./ai-refinement-task-service";

const getCandidateStableId = (candidate: AiCandidateRecord) => {
    return candidate.candidateIdText || normalizeId(candidate.candidateId);
};

const selectLatestContentCandidate = (
    candidates: AiCandidateRecord[] | undefined,
    capability: "qa" | "tags",
    trackedCandidateId?: string | null
) => {
    const normalizedTrackedCandidateId = normalizeId(trackedCandidateId);
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                candidate.status === "PENDING" &&
                aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                    capability &&
                (!normalizedTrackedCandidateId ||
                    isSameId(getCandidateStableId(candidate), normalizedTrackedCandidateId)) &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) =>
            aiRefinementTaskService.sortNewestByRequestedAtThenId({
                left: {
                    id: left.candidateIdText || left.candidateId,
                    requestedAt: left.requestedAt
                },
                right: {
                    id: right.candidateIdText || right.candidateId,
                    requestedAt: right.requestedAt
                }
            })
        )[0];
};

export const selectLatestQaCandidate = (
    candidates: AiCandidateRecord[] | undefined,
    trackedCandidateId?: string | null
) => selectLatestContentCandidate(candidates, "qa", trackedCandidateId);

export const selectLatestTagCandidate = (
    candidates: AiCandidateRecord[] | undefined,
    trackedCandidateId?: string | null
) => selectLatestContentCandidate(candidates, "tags", trackedCandidateId);
