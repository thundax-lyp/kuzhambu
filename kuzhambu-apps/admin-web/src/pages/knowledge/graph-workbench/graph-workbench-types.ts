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
