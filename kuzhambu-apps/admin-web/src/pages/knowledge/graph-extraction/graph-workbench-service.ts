import { postJson } from "@/api/http";
import type {
    GraphWorkbenchCandidateApplyRecord,
    GraphWorkbenchCandidateRecord,
    GraphWorkbenchManuscriptNode,
    GraphWorkbenchManuscriptRecord,
    GraphWorkbenchSourceContentType,
    GraphExtractionTaskRecord,
    GraphExtractionTaskType,
    GraphWorkbenchStatus
} from "./graph-extraction-types";

const API_PREFIX = "/knowledge/graph-workbench";

export interface GraphWorkbenchManuscriptTreeQuery {
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    parentKey?: string | null;
    keyword?: string | null;
    graphStatus?: GraphWorkbenchStatus | null;
}

export interface GraphWorkbenchManuscriptQuery {
    sourceContentType: GraphWorkbenchSourceContentType;
    sourceContentId: string;
}

export interface GraphWorkbenchExtractCommand {
    sourceContentType: GraphWorkbenchSourceContentType;
    sourceContentId: string;
    taskType: GraphExtractionTaskType;
}

export interface GraphWorkbenchCandidateQuery {
    sourceContentType: GraphWorkbenchSourceContentType;
    sourceContentId: string;
    taskType?: GraphExtractionTaskType | null;
}

export interface GraphWorkbenchCandidateApplyCommand {
    applyMode?: "APPEND" | "MERGE" | "OVERWRITE" | string;
    taskId: string;
}

export const listManuscriptTree = (request: GraphWorkbenchManuscriptTreeQuery = {}) => {
    return postJson<GraphWorkbenchManuscriptNode[], GraphWorkbenchManuscriptTreeQuery>(
        `${API_PREFIX}/manuscript-tree`,
        {
            body: request
        }
    );
};

export const getManuscript = (request: GraphWorkbenchManuscriptQuery) => {
    return postJson<GraphWorkbenchManuscriptRecord, GraphWorkbenchManuscriptQuery>(
        `${API_PREFIX}/manuscript/get`,
        {
            body: request
        }
    );
};

export const extractManuscript = (request: GraphWorkbenchExtractCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphWorkbenchExtractCommand>(
        `${API_PREFIX}/manuscript/extract`,
        {
            body: request
        }
    );
};

export const getLatestCandidate = (request: GraphWorkbenchCandidateQuery) => {
    return postJson<GraphWorkbenchCandidateRecord, GraphWorkbenchCandidateQuery>(
        `${API_PREFIX}/candidate/get`,
        {
            body: request
        }
    );
};

export const applyCandidate = (request: GraphWorkbenchCandidateApplyCommand) => {
    return postJson<GraphWorkbenchCandidateApplyRecord, GraphWorkbenchCandidateApplyCommand>(
        `${API_PREFIX}/candidate/apply`,
        {
            body: request
        }
    );
};
