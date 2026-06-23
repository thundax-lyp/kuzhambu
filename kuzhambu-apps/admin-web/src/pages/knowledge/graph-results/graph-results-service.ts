import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphEntityPageQuery,
    GraphEntityRecord,
    GraphLineageNodePageQuery,
    GraphLineageNodeRecord,
    GraphLineageRelationPageQuery,
    GraphLineageRelationRecord,
    GraphRelationPageQuery,
    GraphRelationRecord,
    GraphVersionPageQuery,
    GraphVersionRecord
} from "./graph-results-types";

const API_PREFIX = "/knowledge/graph-extraction";

interface GraphVersionDetailCommand {
    versionId: number;
}

interface GraphEntityDetailCommand {
    entityId: number;
}

interface GraphRelationDetailCommand {
    relationId: number;
}

interface GraphLineageNodeDetailCommand {
    nodeId: number;
}

interface GraphLineageRelationDetailCommand {
    relationId: number;
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
