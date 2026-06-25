export interface DiscoverySearchRequest {
    categoryCodes?: string[] | null;
    contentStatuses?: string[] | null;
    dateFrom?: string | null;
    dateTo?: string | null;
    knowledgeBases?: string[] | null;
    pageNo?: number | null;
    pageSize?: number | null;
    queryText: string;
    tagNames?: string[] | null;
    visibilityScopes?: string[] | null;
}

export interface DiscoverySearchClickRequest {
    contentDomain: string;
    contentId: string;
    contentTitle?: string | null;
    contentType: string;
    groupRank: number;
    resultGroupKey: string;
    resultRank: number;
    searchLogId: string;
    targetPath?: string | null;
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
    queryText?: string | null;
    searchLogId?: string | null;
    totalCount?: number | null;
}
