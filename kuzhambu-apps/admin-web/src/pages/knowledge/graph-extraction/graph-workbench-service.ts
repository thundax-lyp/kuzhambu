import { postJson } from "@/api/http";
import type { GraphExtractionTaskRecord, GraphExtractionTaskType } from "./graph-extraction-types";
import type {
    GraphWorkbenchCandidateApplyRecord,
    GraphWorkbenchCandidateSummary,
    GraphWorkbenchManuscriptDetail,
    GraphWorkbenchManuscriptTreeNode,
    GraphWorkbenchSourceContentType,
    GraphWorkbenchStatus
} from "./graph-workbench-types";

const API_PREFIX = "/knowledge/graph-workbench";

export interface GraphWorkbenchManuscriptTreeQuery {
    sourceContentType?: GraphWorkbenchSourceContentType | null;
    parentKey?: string | null;
    keyword?: string | null;
    graphStatus?: GraphWorkbenchStatus | null;
}

export interface GraphWorkbenchManuscriptQuery {
    sourceContentType: GraphWorkbenchSourceContentType;
    sourceContentId: number;
}

export interface GraphWorkbenchExtractCommand {
    sourceContentType: GraphWorkbenchSourceContentType;
    sourceContentId: number;
    taskType: GraphExtractionTaskType;
    requestedBy?: number | null;
}

export interface GraphWorkbenchCandidateQuery {
    sourceContentType: GraphWorkbenchSourceContentType;
    sourceContentId: number;
    taskType?: GraphExtractionTaskType | null;
}

export interface GraphWorkbenchCandidateApplyCommand {
    taskId: number;
}

export const listManuscriptTree = (request: GraphWorkbenchManuscriptTreeQuery = {}) => {
    return postJson<GraphWorkbenchManuscriptTreeNode[], GraphWorkbenchManuscriptTreeQuery>(
        `${API_PREFIX}/manuscript-tree`,
        {
            body: request
        }
    );
};

export const getManuscript = (request: GraphWorkbenchManuscriptQuery) => {
    return postJson<GraphWorkbenchManuscriptDetail, GraphWorkbenchManuscriptQuery>(
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
    return postJson<GraphWorkbenchCandidateSummary, GraphWorkbenchCandidateQuery>(
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
