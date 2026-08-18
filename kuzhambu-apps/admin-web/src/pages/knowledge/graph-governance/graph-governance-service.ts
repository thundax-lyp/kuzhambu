import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    GraphGovernanceAdjacencyRecord,
    GraphGovernanceNodeDetailRecord,
    GraphGovernanceNodeRecord,
    GraphGovernanceRelationDetailRecord,
    GraphGovernanceRelationRecord
} from "./graph-governance-types";

const API_PREFIX = "/knowledge/graph/published";

export type GraphGovernanceNodePageQuery = PageQuery<{
    keyword?: string | null;
    nodeType?: string | null;
    source?: string | null;
    status?: string | null;
}>;

export type GraphGovernanceRelationPageQuery = PageQuery<{
    keyword?: string | null;
    relationType?: string | null;
    source?: string | null;
    status?: string | null;
}>;

export type GraphGovernanceAdjacencyQuery = PageQuery<{
    includeIsolated?: boolean;
    subjectKeyword?: string | null;
}>;

export const pagePublishedNodes = (query: GraphGovernanceNodePageQuery) =>
    postJson<Page<GraphGovernanceNodeRecord>, GraphGovernanceNodePageQuery>(
        `${API_PREFIX}/node/page`,
        { body: query }
    );

export const pagePublishedRelations = (query: GraphGovernanceRelationPageQuery) =>
    postJson<Page<GraphGovernanceRelationRecord>, GraphGovernanceRelationPageQuery>(
        `${API_PREFIX}/edge/page`,
        { body: query }
    );

export const getPublishedNode = (nodeId: string) =>
    postJson<GraphGovernanceNodeDetailRecord, { nodeId: string }>(`${API_PREFIX}/node/get`, {
        body: { nodeId }
    });

export const getPublishedRelation = (edgeId: string) =>
    postJson<GraphGovernanceRelationDetailRecord, { edgeId: string }>(`${API_PREFIX}/edge/get`, {
        body: { edgeId }
    });

export const pagePublishedAdjacency = (query: GraphGovernanceAdjacencyQuery) =>
    postJson<Page<GraphGovernanceAdjacencyRecord>, GraphGovernanceAdjacencyQuery>(
        `${API_PREFIX}/adjacency/page`,
        { body: query }
    );
