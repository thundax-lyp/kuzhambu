import { getJson, postJson } from "@/api/http";
import type { AiCandidateApplyPayload } from "./ai-candidate-types";
import type {
    ClassicsBatchOperationRecord,
    ClassicsBatchVisibilityPayload,
    ClassicsContentListPayload,
    ClassicsContentQaPairPayload,
    ClassicsContentQaPairRecord,
    ClassicsContentQaPairSortPayload,
    ClassicsAiCandidateBatchRejectItemPayload,
    ClassicsContentTagDeletePayload,
    ClassicsContentTagPayload,
    ClassicsContentTagRecord,
    ClassicsContentTagSortPayload
} from "./classics-content-types";

const CLASSICS_CONTENT_PATH = "/classics/content";

export type ClassicsContentListQuery = ClassicsContentListPayload;
export type ClassicsContentTagCommand = ClassicsContentTagPayload;
export type ClassicsContentTagDeleteCommand = ClassicsContentTagDeletePayload;
export type ClassicsContentTagSortCommand = ClassicsContentTagSortPayload;
export type ClassicsContentQaPairCommand = ClassicsContentQaPairPayload;
export type ClassicsContentQaPairSortCommand = ClassicsContentQaPairSortPayload;
export type ClassicsBatchVisibilityCommand = ClassicsBatchVisibilityPayload;
export type ClassicsAiCandidateBatchApplyCommand = {
    items: AiCandidateApplyPayload[];
};
export type ClassicsAiCandidateBatchRejectCommand = {
    errorType?: string | null;
    errorMessage?: string | null;
    items: ClassicsAiCandidateBatchRejectItemPayload[];
};

const buildTagsListPath = ({ contentType, contentId }: ClassicsContentListQuery) => {
    const search = new URLSearchParams({ contentType, contentId: String(contentId) }).toString();
    return `${CLASSICS_CONTENT_PATH}/tags?${search}`;
};

const buildQaPairsListPath = ({ contentType, contentId }: ClassicsContentListQuery) => {
    const search = new URLSearchParams({ contentType, contentId: String(contentId) }).toString();
    return `${CLASSICS_CONTENT_PATH}/qa-pairs?${search}`;
};

export const listTags = (query: ClassicsContentListQuery) => {
    return getJson<ClassicsContentTagRecord[]>(buildTagsListPath(query));
};

export const addTag = (request: ClassicsContentTagCommand) => {
    return postJson<ClassicsContentTagRecord, ClassicsContentTagCommand>(
        `${CLASSICS_CONTENT_PATH}/tags/add`,
        {
            body: request
        }
    );
};

export const updateTag = (request: ClassicsContentTagCommand) => {
    return postJson<ClassicsContentTagRecord, ClassicsContentTagCommand>(
        `${CLASSICS_CONTENT_PATH}/tags/update`,
        {
            body: request
        }
    );
};

export const deleteTag = (request: ClassicsContentTagDeleteCommand) => {
    return postJson<boolean, ClassicsContentTagDeleteCommand>(
        `${CLASSICS_CONTENT_PATH}/tags/delete`,
        {
            body: request
        }
    );
};

export const sortTags = (request: ClassicsContentTagSortCommand) => {
    return postJson<boolean, ClassicsContentTagSortCommand>(`${CLASSICS_CONTENT_PATH}/tags/sort`, {
        body: request
    });
};

export const listQaPairs = (query: ClassicsContentListQuery) => {
    return getJson<ClassicsContentQaPairRecord[]>(buildQaPairsListPath(query));
};

export const addQaPair = (request: ClassicsContentQaPairCommand) => {
    return postJson<ClassicsContentQaPairRecord, ClassicsContentQaPairCommand>(
        `${CLASSICS_CONTENT_PATH}/qa-pairs/add`,
        {
            body: request
        }
    );
};

export const updateQaPair = (request: ClassicsContentQaPairCommand) => {
    return postJson<ClassicsContentQaPairRecord, ClassicsContentQaPairCommand>(
        `${CLASSICS_CONTENT_PATH}/qa-pairs/update`,
        {
            body: request
        }
    );
};

export const sortQaPairs = (request: ClassicsContentQaPairSortCommand) => {
    return postJson<boolean, ClassicsContentQaPairSortCommand>(
        `${CLASSICS_CONTENT_PATH}/qa-pairs/sort`,
        {
            body: request
        }
    );
};

export const applyAiCandidatesBatch = (request: ClassicsAiCandidateBatchApplyCommand) => {
    return postJson<ClassicsBatchOperationRecord, ClassicsAiCandidateBatchApplyCommand>(
        `${CLASSICS_CONTENT_PATH}/ai-candidates/batch/apply`,
        {
            body: request
        }
    );
};

export const rejectAiCandidatesBatch = (request: ClassicsAiCandidateBatchRejectCommand) => {
    return postJson<ClassicsBatchOperationRecord, ClassicsAiCandidateBatchRejectCommand>(
        `${CLASSICS_CONTENT_PATH}/ai-candidates/batch/reject`,
        {
            body: request
        }
    );
};

export const changeVisibilityBatch = (request: ClassicsBatchVisibilityCommand) => {
    return postJson<ClassicsBatchOperationRecord, ClassicsBatchVisibilityCommand>(
        `${CLASSICS_CONTENT_PATH}/visibility/change`,
        {
            body: request
        }
    );
};
