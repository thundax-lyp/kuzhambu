export interface GraphWorkbenchMetricRecord {
    key: "nodes" | "relations" | "materials" | "orphans" | "missingCoreRelations";
    label: string;
    value: number;
}

export interface GraphWorkbenchCategoryRecord {
    code: string;
    name: string;
}
