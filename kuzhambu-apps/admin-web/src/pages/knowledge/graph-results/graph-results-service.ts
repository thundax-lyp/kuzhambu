import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphEntityRecord,
    GraphLineageNodeRecord,
    GraphLineageRelationRecord,
    GraphRelationRecord,
    GraphVersionRecord
} from "./graph-results-types";

const API_PREFIX = "/knowledge/graph-extraction";

export interface GraphVersionPageQuery {
    pageNo?: number;
    pageSize?: number;
    taskType?: string | null;
    status?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: string | null;
}

export interface GraphEntityPageQuery {
    pageNo?: number;
    pageSize?: number;
    versionId?: string | null;
    keyword?: string | null;
    entityType?: string | null;
    confirmationStatus?: string | null;
}

export interface GraphRelationPageQuery {
    pageNo?: number;
    pageSize?: number;
    versionId?: string | null;
    keyword?: string | null;
    relationType?: string | null;
    confirmationStatus?: string | null;
}

export interface GraphLineageNodePageQuery {
    pageNo?: number;
    pageSize?: number;
    versionId?: string | null;
    keyword?: string | null;
    nodeType?: string | null;
    confirmationStatus?: string | null;
}

export interface GraphLineageRelationPageQuery {
    pageNo?: number;
    pageSize?: number;
    versionId?: string | null;
    keyword?: string | null;
    relationType?: string | null;
    confirmationStatus?: string | null;
}

interface GraphVersionDetailCommand {
    versionId: string;
}

interface GraphEntityDetailCommand {
    entityId: string;
}

interface GraphRelationDetailCommand {
    relationId: string;
}

interface GraphLineageNodeDetailCommand {
    nodeId: string;
}

interface GraphLineageRelationDetailCommand {
    relationId: string;
}

export const pageVersions = (request: GraphVersionPageQuery = {}) => {
    return postJson<Page<GraphVersionRecord>, GraphVersionPageQuery>(`${API_PREFIX}/version/page`, {
        body: request
    });
};

export const getVersionDetail = (request: GraphVersionDetailCommand) => {
    return postJson<GraphVersionRecord, GraphVersionDetailCommand>(`${API_PREFIX}/version/get`, {
        body: request
    });
};

export const pageEntities = (request: GraphEntityPageQuery = {}) => {
    return postJson<Page<GraphEntityRecord>, GraphEntityPageQuery>(`${API_PREFIX}/entity/page`, {
        body: request
    });
};

export const getEntityDetail = (request: GraphEntityDetailCommand) => {
    return postJson<GraphEntityRecord, GraphEntityDetailCommand>(`${API_PREFIX}/entity/get`, {
        body: request
    });
};

export const pageRelations = (request: GraphRelationPageQuery = {}) => {
    return postJson<Page<GraphRelationRecord>, GraphRelationPageQuery>(
        `${API_PREFIX}/relation/page`,
        {
            body: request
        }
    );
};

export const getRelationDetail = (request: GraphRelationDetailCommand) => {
    return postJson<GraphRelationRecord, GraphRelationDetailCommand>(`${API_PREFIX}/relation/get`, {
        body: request
    });
};

export const pageLineageNodes = (request: GraphLineageNodePageQuery = {}) => {
    return postJson<Page<GraphLineageNodeRecord>, GraphLineageNodePageQuery>(
        `${API_PREFIX}/lineage/node/page`,
        {
            body: request
        }
    );
};

export const getLineageNodeDetail = (request: GraphLineageNodeDetailCommand) => {
    return postJson<GraphLineageNodeRecord, GraphLineageNodeDetailCommand>(
        `${API_PREFIX}/lineage/node/get`,
        {
            body: request
        }
    );
};

export const pageLineageRelations = (request: GraphLineageRelationPageQuery = {}) => {
    return postJson<Page<GraphLineageRelationRecord>, GraphLineageRelationPageQuery>(
        `${API_PREFIX}/lineage/relation/page`,
        {
            body: request
        }
    );
};

export const getLineageRelationDetail = (request: GraphLineageRelationDetailCommand) => {
    return postJson<GraphLineageRelationRecord, GraphLineageRelationDetailCommand>(
        `${API_PREFIX}/lineage/relation/get`,
        {
            body: request
        }
    );
};
