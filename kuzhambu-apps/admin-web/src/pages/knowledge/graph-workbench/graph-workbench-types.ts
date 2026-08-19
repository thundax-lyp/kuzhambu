export interface GraphPublishedNodeRecord {
    id: string;
    nodeType?: string | null;
    name?: string | null;
    source?: string | null;
    status?: string | null;
    lockVersion?: string | null;
}

export interface GraphPublishedEdgeRecord {
    id: string;
    sourceNodeId?: string | null;
    targetNodeId?: string | null;
    relationType?: string | null;
    source?: string | null;
    status?: string | null;
    lockVersion?: string | null;
}

export interface GraphWorkbenchOverviewRecord {
    snapshotAt: string;
    publishedNodeCount: string;
    publishedEdgeCount: string;
    coveredMaterialCount: string;
    isolatedNodeCount: string;
    missingCoreRelationNodeCount: string;
    pendingConflictCount: string;
    recentActivities: Array<{
        type: string;
        occurredAt: string | null;
        summary: string;
    }>;
}

export interface GraphWorkbenchGraphRecord {
    nodes: GraphPublishedNodeRecord[];
    edges: GraphPublishedEdgeRecord[];
}

export interface GraphWorkbenchOneHopEdgesRecord extends GraphWorkbenchGraphRecord {
    nextCursor: string | null;
    truncated: boolean;
}
