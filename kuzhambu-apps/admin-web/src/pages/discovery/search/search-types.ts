export interface DiscoverySearchItemRecord {
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

export interface DiscoverySearchGroupRecord {
    count?: number | null;
    groupKey?: string | null;
    groupTitle?: string | null;
    items?: DiscoverySearchItemRecord[] | null;
}

export interface DiscoverySearchResultRecord {
    displayQueryText?: string | null;
    groupCount?: number | null;
    groups?: DiscoverySearchGroupRecord[] | null;
    queryText?: string | null;
    searchLogId?: string | null;
    totalCount?: number | null;
}
