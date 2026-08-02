export interface DiscoverySearchRequest {
    categoryCodes: string[];
    dateFrom: string | null;
    dateTo: string | null;
    knowledgeBases: string[];
    pageNo: number;
    pageSize: number;
    queryText: string;
    tagNames: string[];
}

export interface DiscoverySearchClickEventRequest {
    contentDomain: string;
    contentId: string;
    contentTitle?: string | null;
    contentType: string;
    groupRank: number;
    resultGroupKey: string;
    resultRank: number;
    searchEventId: string;
    targetPath?: string | null;
}

export interface DiscoverySearchPreviewRequest {
    contentId: string;
    contentType: string;
}

export interface DiscoverySearchPreviewResponse {
    bodyText?: string | null;
    categoryCode?: string | null;
    categoryName?: string | null;
    contentDomain?: string | null;
    contentId?: string | null;
    contentType?: string | null;
    knowledgeBase?: string | null;
    publishedAt?: number | null;
    sourceVersionNo?: number | null;
    summary?: string | null;
    tagNames?: string[] | null;
    targetPath?: string | null;
    title?: string | null;
    updatedAt?: number | null;
}

export interface DiscoverySearchItemResponse {
    contentDomain?: string | null;
    contentId?: string | null;
    contentType?: string | null;
    highlightText?: string | null;
    groupRank?: number | null;
    resultRank?: number | null;
    summary?: string | null;
    targetPath?: string | null;
    title?: string | null;
}

export interface DiscoverySearchGroupResponse {
    count?: number | null;
    groupKey?: string | null;
    groupTitle?: string | null;
    items?: DiscoverySearchItemResponse[] | null;
}

export interface DiscoverySearchResponse {
    displayQueryText?: string | null;
    groupCount?: number | null;
    groups?: DiscoverySearchGroupResponse[] | null;
    id?: string | null;
    queryText?: string | null;
    totalCount?: number | null;
}
