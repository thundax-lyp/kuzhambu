export interface KnowledgeLineageCanvasRecord {
    version?: KnowledgeLineageVersionRecord | null;
    summary: KnowledgeLineageSummaryRecord;
    nodes: KnowledgeLineageNodeRecord[];
    relations: KnowledgeLineageRelationRecord[];
    selectedNode?: KnowledgeLineageNodeRecord | null;
    selectedRelation?: KnowledgeLineageRelationRecord | null;
    availableFilters: KnowledgeLineageAvailableFiltersRecord;
    empty?: KnowledgeLineageEmptyRecord | null;
}

export interface KnowledgeLineageVersionRecord {
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

export interface KnowledgeLineageSummaryRecord {
    nodeCount: number;
    relationCount: number;
    confirmedNodeCount: number;
    confirmedRelationCount: number;
    focusNodeId?: number | null;
    focusRelationId?: number | null;
}

export interface KnowledgeLineageNodeRecord {
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
    sourceRefs: KnowledgeLineageSourceRefRecord[];
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
    x?: number | null;
    y?: number | null;
}

export interface KnowledgeLineageRelationRecord {
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
    sourceRefs: KnowledgeLineageSourceRefRecord[];
    firstExtractedAt?: number | null;
    lastExtractedAt?: number | null;
}

export interface KnowledgeLineageSourceRefRecord {
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceTitle?: string | null;
    snippet?: string | null;
    href?: string | null;
}

export interface KnowledgeLineageAvailableFiltersRecord {
    versions: KnowledgeLineageVersionRecord[];
    nodeTypes: string[];
    relationTypes: string[];
    confirmationStatuses: string[];
}

export interface KnowledgeLineageEmptyRecord {
    reason:
        "NO_VERSION" | "NO_LINEAGE_DATA" | "FILTER_NO_RESULT" | "NO_PERMISSION" | "ERROR" | string;
    title: string;
    description?: string | null;
    actionLabel?: string | null;
    actionHref?: string | null;
}
