import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    GraphGovernanceAdjacencyRecord,
    GraphGovernanceImpactRecord,
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
    subjectNodeId?: string | null;
}>;

export interface GraphGovernancePublishedNodeUpdateCommand {
    lockVersion: string;
    node: {
        id: string;
        name: string;
        nodeType: string;
        properties: Record<string, unknown>;
        source: string;
        status: string;
    };
    properties: Array<{
        id?: string;
        preferred: boolean;
        propertyName: string;
        value: string;
    }>;
    reason: string;
}

export interface GraphGovernancePublishedEdgeUpdateCommand {
    edge: {
        id: string;
        qualifiers: Record<string, unknown>;
        relationType: string;
        source: string;
        sourceNodeId: string;
        status: string;
        targetNodeId: string;
    };
    lockVersion: string;
    properties: Array<{
        id?: string;
        preferred: boolean;
        propertyName: string;
        value: string;
    }>;
    reason: string;
}

export interface GraphGovernancePublishedNodeDeletionPreviewQuery {
    nodeId: string;
}

export interface GraphGovernancePublishedRelationDeletionPreviewQuery {
    edgeId: string;
}

export interface GraphGovernancePublishedNodeDeletionCommand {
    impactToken: string;
    lockVersion: string;
    nodeId: string;
    reason: string;
}

export interface GraphGovernancePublishedRelationDeletionCommand {
    edgeId: string;
    impactToken: string;
    lockVersion: string;
    reason: string;
}

export interface GraphGovernancePublishedNodeMergePreviewQuery {
    mergedNodeIds: string[];
    retainedNodeId: string;
}

export interface GraphGovernancePublishedNodeMergeCommand extends GraphGovernancePublishedNodeMergePreviewQuery {
    impactToken: string;
    reason: string;
    retainedNodeLockVersion: string;
}

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

export const updatePublishedNode = (command: GraphGovernancePublishedNodeUpdateCommand) =>
    postJson<GraphGovernanceNodeDetailRecord, GraphGovernancePublishedNodeUpdateCommand>(
        `${API_PREFIX}/node/update`,
        { body: command }
    );

export const updatePublishedRelation = (command: GraphGovernancePublishedEdgeUpdateCommand) =>
    postJson<GraphGovernanceRelationDetailRecord, GraphGovernancePublishedEdgeUpdateCommand>(
        `${API_PREFIX}/edge/update`,
        { body: command }
    );

export const previewPublishedNodeDeletion = (
    query: GraphGovernancePublishedNodeDeletionPreviewQuery
) =>
    postJson<GraphGovernanceImpactRecord, { cascadeEdges: boolean; nodeId: string }>(
        `${API_PREFIX}/node/delete/preview`,
        { body: { cascadeEdges: true, nodeId: query.nodeId } }
    );

export const deletePublishedNode = (command: GraphGovernancePublishedNodeDeletionCommand) =>
    postJson<GraphGovernanceNodeRecord, GraphGovernancePublishedNodeDeletionCommand>(
        `${API_PREFIX}/node/delete`,
        { body: command }
    );

export const previewPublishedRelationDeletion = (
    query: GraphGovernancePublishedRelationDeletionPreviewQuery
) =>
    postJson<GraphGovernanceImpactRecord, { edgeId: string }>(`${API_PREFIX}/edge/delete/preview`, {
        body: query
    });

export const deletePublishedRelation = (command: GraphGovernancePublishedRelationDeletionCommand) =>
    postJson<GraphGovernanceRelationRecord, GraphGovernancePublishedRelationDeletionCommand>(
        `${API_PREFIX}/edge/delete`,
        { body: command }
    );

export const previewPublishedNodeMerge = (query: GraphGovernancePublishedNodeMergePreviewQuery) =>
    postJson<GraphGovernanceImpactRecord, GraphGovernancePublishedNodeMergePreviewQuery>(
        `${API_PREFIX}/node/merge/preview`,
        { body: query }
    );

export const mergePublishedNodes = (command: GraphGovernancePublishedNodeMergeCommand) =>
    postJson<GraphGovernanceNodeDetailRecord, GraphGovernancePublishedNodeMergeCommand>(
        `${API_PREFIX}/node/merge`,
        { body: command }
    );

export const pagePublishedAdjacency = (query: GraphGovernanceAdjacencyQuery) =>
    postJson<Page<GraphGovernanceAdjacencyRecord>, GraphGovernanceAdjacencyQuery>(
        `${API_PREFIX}/adjacency/page`,
        { body: query }
    );
