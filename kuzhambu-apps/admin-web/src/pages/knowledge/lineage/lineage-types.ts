export interface LineageCanvasRecord {
    version?: LineageVersionRecord | null;
    summary: LineageSummaryRecord;
    nodes: LineageNodeRecord[];
    relations: LineageRelationRecord[];
    selectedNode?: LineageNodeRecord | null;
    selectedRelation?: LineageRelationRecord | null;
    availableFilters: LineageAvailableFiltersRecord;
    empty?: LineageEmptyRecord | null;
}

export interface LineageVersionRecord {
    versionId: number;
    versionNo?: number | null;
    taskType?: string | null;
    status?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    sourceCategoryName?: string | null;
    appliedAt?: number | null;
}

export interface LineageSummaryRecord {
    nodeCount: number;
    relationCount: number;
    confirmedNodeCount: number;
    confirmedRelationCount: number;
    focusNodeId?: number | null;
    focusRelationId?: number | null;
}

export interface LineageNodeRecord {
    id: string;
    nodeId: number;
    nodeKey?: string | null;
    name?: string | null;
    nodeType?: string | null;
    generation?: number | null;
    gender?: string | null;
    confirmationStatus?: string | null;
    confidence?: number | null;
    sourceRefsJson?: string | null;
    sourceRefs: LineageSourceRefRecord[];
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    x?: number | null;
    y?: number | null;
}

export interface LineageRelationRecord {
    id: string;
    relationId: number;
    sourceNodeId?: number | null;
    sourceNodeName?: string | null;
    targetNodeId?: number | null;
    targetNodeName?: string | null;
    relationType?: string | null;
    relationLabel?: string | null;
    confirmationStatus?: string | null;
    confidence?: number | null;
    sourceRefsJson?: string | null;
    sourceRefs: LineageSourceRefRecord[];
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
}

export interface LineageSourceRefRecord {
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceTitle?: string | null;
    snippet?: string | null;
    href?: string | null;
}

export interface LineageAvailableFiltersRecord {
    versions: LineageVersionRecord[];
    nodeTypes: string[];
    relationTypes: string[];
    confirmationStatuses: string[];
}

export interface LineageEmptyRecord {
    reason:
        "NO_VERSION" | "NO_LINEAGE_DATA" | "FILTER_NO_RESULT" | "NO_PERMISSION" | "ERROR" | string;
    title: string;
    description?: string | null;
    actionLabel?: string | null;
    actionHref?: string | null;
}
