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
    sourceId: string;
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

export interface KnowledgeAtlasAvailableFilters {
    knowledgeBases: string[];
    entityTypes: string[];
    relationTypes: string[];
    tagNames: string[];
    timeRanges: string[];
}

export interface KnowledgeAtlasResponse {
    focusNode: KnowledgeAtlasFocusNode;
    relationGroups: KnowledgeAtlasRelationGroup[];
    sourceReferences: KnowledgeAtlasSourceReference[];
    relatedTags: KnowledgeAtlasRelatedTag[];
    timelineItems: KnowledgeAtlasTimelineItem[];
    availableFilters: KnowledgeAtlasAvailableFilters;
}
