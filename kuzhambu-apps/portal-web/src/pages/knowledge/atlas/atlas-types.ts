export interface KnowledgeAtlasQuery {
    level?: "overview" | "category" | "detail";
    categoryCode?: string | null;
    entityId?: number | null;
    knowledgeBase?: string | null;
    keyword?: string | null;
    tag?: string | null;
    timeRange?: string | null;
}

export interface KnowledgeAtlasBreadcrumbItem {
    level: string;
    label: string;
    href: string;
}

export interface KnowledgeAtlasOverviewCategoryCard {
    categoryCode: string;
    categoryName: string;
    entityCount: number;
    relationCount: number;
    appliedVersionCount: number;
    latestVersionNo: number | null;
    entryHref: string;
}

export interface KnowledgeAtlasOverviewView {
    summaryTitle: string;
    summarySubtitle: string;
    categoryCards: KnowledgeAtlasOverviewCategoryCard[];
}

export interface KnowledgeAtlasCategoryEntityHighlight {
    entityId: string;
    entityName: string;
    entityType: string;
    confirmationStatus: string;
    entryHref: string;
}

export interface KnowledgeAtlasFocusNode {
    id: string;
    title: string;
    type: string;
    summary: string;
    status: string;
    confidence: number;
    coverImageUrl: string | null;
}

export interface KnowledgeAtlasRelationItem {
    sourceId: string;
    sourceLabel: string;
    relationLabel: string;
    targetId: string;
    targetLabel: string;
    relationType: string;
    weight: number;
}

export interface KnowledgeAtlasRelationGroup {
    groupKey: string;
    groupLabel: string;
    relations: KnowledgeAtlasRelationItem[];
}

export interface KnowledgeAtlasSourceReference {
    sourceId: string | null;
    sourceTitle: string;
    sourceType: string;
    snippet: string;
    updatedAt: number | null;
    href: string;
}

export interface KnowledgeAtlasRelatedTag {
    tagId: string;
    tagName: string;
    tagCategory: string;
    score: number;
}

export interface KnowledgeAtlasTimelineItem {
    timeLabel: string;
    title: string;
    description: string;
    href: string;
}

export interface KnowledgeAtlasCategoryView {
    categoryCode: string;
    categoryName: string;
    latestVersionId: number | null;
    latestVersionNo: number | null;
    entityHighlights: KnowledgeAtlasCategoryEntityHighlight[];
    relationGroups: KnowledgeAtlasRelationGroup[];
    sourceReferences: KnowledgeAtlasSourceReference[];
}

export interface KnowledgeAtlasDetailView {
    focusNode: KnowledgeAtlasFocusNode;
    relationGroups: KnowledgeAtlasRelationGroup[];
    sourceReferences: KnowledgeAtlasSourceReference[];
    timelineItems: KnowledgeAtlasTimelineItem[];
    relatedTags: KnowledgeAtlasRelatedTag[];
}

export interface KnowledgeAtlasAvailableFilters {
    knowledgeBases: string[];
    entityTypes: string[];
    relationTypes: string[];
    tagNames: string[];
    timeRanges: string[];
}

export interface KnowledgeAtlasCanvasNode {
    id: string;
    kind: "root" | "category" | "entity" | string;
    label: string;
    subtitle: string | null;
    metricLabel: string | null;
    metricValue: number | null;
    status: string | null;
    categoryCode: string | null;
    entityId: number | null;
    href: string | null;
    weight: number | null;
    x: number | null;
    y: number | null;
}

export interface KnowledgeAtlasCanvasEdge {
    id: string;
    source: string;
    target: string;
    label: string | null;
    relationType: string | null;
    weight: number | null;
    dashed: boolean;
}

export interface KnowledgeAtlasCanvasView {
    mode: "overview" | "category" | "detail" | string;
    title: string;
    description: string;
    focusNodeId: string | null;
    empty: boolean;
    emptyTitle: string | null;
    emptyDescription: string | null;
    nodes: KnowledgeAtlasCanvasNode[];
    edges: KnowledgeAtlasCanvasEdge[];
}

export interface KnowledgeAtlasResponse {
    currentLevel: "overview" | "category" | "detail";
    breadcrumbItems: KnowledgeAtlasBreadcrumbItem[];
    overviewView: KnowledgeAtlasOverviewView | null;
    categoryView: KnowledgeAtlasCategoryView | null;
    detailView: KnowledgeAtlasDetailView | null;
    availableFilters: KnowledgeAtlasAvailableFilters;
    canvasView: KnowledgeAtlasCanvasView | null;
}
