export interface AtlasNodeRecord {
    id: string;
    name?: string | null;
    nodeType?: string | null;
}

export interface AtlasEdgeRecord {
    id: string;
    relationType?: string | null;
    sourceNodeId?: string | null;
    targetNodeId?: string | null;
}

export interface AtlasGraphRecord {
    nodes: AtlasNodeRecord[];
    edges: AtlasEdgeRecord[];
}

export interface AtlasOverviewRecord {
    publishedNodeCount: string;
    publishedEdgeCount: string;
    coveredMaterialCount: string;
    isolatedNodeCount: string;
}

export interface AtlasOneHopEdgesRecord extends AtlasGraphRecord {
    nextCursor: string | null;
    truncated: boolean;
}
