import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphExtractionCreateCommand,
    GraphExtractionTaskIdCommand,
    GraphExtractionTaskPageQuery,
    GraphExtractionTaskRecord
} from "./graph-extraction-types";

const API_PREFIX = "/knowledge/graph-extraction";

export const requestRelationExtraction = (request: GraphExtractionCreateCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionCreateCommand>(
        `${API_PREFIX}/relation/request`,
        {
            body: request
        }
    );
};

export const requestGraphExtraction = (request: GraphExtractionCreateCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionCreateCommand>(
        `${API_PREFIX}/graph/request`,
        {
            body: request
        }
    );
};

export const requestLineageExtraction = (request: GraphExtractionCreateCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionCreateCommand>(
        `${API_PREFIX}/lineage/request`,
        {
            body: request
        }
    );
};

export const pageTasks = (request: GraphExtractionTaskPageQuery = {}) => {
    return postJson<Page<GraphExtractionTaskRecord>, GraphExtractionTaskPageQuery>(
        `${API_PREFIX}/task/page`,
        {
            body: request
        }
    );
};

export const getTaskDetail = (request: GraphExtractionTaskIdCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionTaskIdCommand>(
        `${API_PREFIX}/task/detail`,
        {
            body: request
        }
    );
};

export const applyTaskCandidate = (request: GraphExtractionTaskIdCommand) => {
    return postJson<GraphExtractionTaskRecord, GraphExtractionTaskIdCommand>(
        `${API_PREFIX}/task/apply`,
        {
            body: request
        }
    );
};
