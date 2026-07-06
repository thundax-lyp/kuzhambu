export interface DiscoverySearchRequest {
    categoryCodes: string[];
    contentStatuses: string[];
    dateFrom: string | null;
    dateTo: string | null;
    knowledgeBases: string[];
    pageNo: number;
    pageSize: number;
    queryText: string;
    tagNames: string[];
    visibilityScopes: string[];
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
