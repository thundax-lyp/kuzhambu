import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { GraphPublishedAdjacencyRecord } from "./graph-workbench-types";

const API_PREFIX = "/knowledge/graph";

export interface GraphPublishedAdjacencyQuery {
    pageNo?: number;
    pageSize?: number;
    subjectKeyword?: string | null;
    subjectType?: string | null;
    subjectStatus?: string | null;
    subjectSource?: string | null;
    relationType?: string | null;
    relationStatus?: string | null;
    relationSource?: string | null;
    objectKeyword?: string | null;
    objectType?: string | null;
    objectStatus?: string | null;
    objectSource?: string | null;
    includeIsolated?: boolean;
}

export const pagePublishedAdjacency = (request: GraphPublishedAdjacencyQuery = {}) => {
    return postJson<Page<GraphPublishedAdjacencyRecord>, GraphPublishedAdjacencyQuery>(
        `${API_PREFIX}/published/adjacency/page`,
        {
            body: request
        }
    );
};
