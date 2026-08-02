import { postJson } from "@/api/http";
import type { AiCandidateApplyPayload } from "./ai-candidate-types";
import type {
    ClassicsBatchOperationRecord,
    ClassicsContentListPayload,
    ClassicsContentQaPairPayload,
    ClassicsContentQaPairDeletePayload,
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
export type ClassicsContentQaPairDeleteCommand = ClassicsContentQaPairDeletePayload;
export type ClassicsAiCandidateBatchApplyCommand = {
    items: AiCandidateApplyPayload[];
};
export type ClassicsAiCandidateBatchRejectCommand = {
    errorType?: string | null;
    errorMessage?: string | null;
    items: ClassicsAiCandidateBatchRejectItemPayload[];
};

export const listTags = (query: ClassicsContentListQuery) => {
    return postJson<ClassicsContentTagRecord[], ClassicsContentListQuery>(
        `${CLASSICS_CONTENT_PATH}/tags/list`,
        {
            body: query
        }
    );
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
    return postJson<ClassicsContentQaPairRecord[], ClassicsContentListQuery>(
        `${CLASSICS_CONTENT_PATH}/qa-pairs/list`,
        {
            body: query
        }
    );
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

export const deleteQaPair = (request: ClassicsContentQaPairDeleteCommand) => {
    return postJson<boolean, ClassicsContentQaPairDeleteCommand>(
        `${CLASSICS_CONTENT_PATH}/qa-pairs/delete`,
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
