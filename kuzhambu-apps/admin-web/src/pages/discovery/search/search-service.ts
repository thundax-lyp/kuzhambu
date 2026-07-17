import { postJson } from "@/api/http";
import type { DiscoverySearchResultRecord } from "./search-types";

export interface DiscoverySearchQuery {
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

export interface DiscoverySearchClickCommand {
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

export const searchDiscovery = (query: DiscoverySearchQuery) => {
    return postJson<DiscoverySearchResultRecord, DiscoverySearchQuery>("/discovery/search/search", {
        body: query
    });
};

export const clickSearchResult = (command: DiscoverySearchClickCommand) => {
    return postJson<boolean, DiscoverySearchClickCommand>("/portal/discovery/search/click", {
        body: command
    });
};
