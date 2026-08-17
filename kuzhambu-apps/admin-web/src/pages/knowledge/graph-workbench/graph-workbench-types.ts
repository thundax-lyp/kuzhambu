export interface GraphWorkbenchMetricRecord {
    key: "nodes" | "relations" | "materials" | "orphans" | "missingCoreRelations";
    label: string;
    value: number;
}

export interface GraphWorkbenchCategoryRecord {
    code: string;
    name: string;
}

export interface GraphWorkbenchNodeRecord {
    categoryCode: string;
    id: string;
    label: string;
    isFaded: boolean;
    isOrphan?: boolean;
    sourceName?: string;
    qualityTodo?: string;
}

export interface GraphWorkbenchEdgeRecord {
    id: string;
    source: string;
    target: string;
    predicate: string;
}

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

export interface GraphPublishedAdjacencyRecord {
    subject: GraphPublishedNodeRecord;
    relation?: GraphPublishedEdgeRecord | null;
    object?: GraphPublishedNodeRecord | null;
    isolated: boolean;
}
