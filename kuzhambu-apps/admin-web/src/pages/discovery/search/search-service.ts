import { postJson } from "@/api/http";
import type { DiscoverySearchPreviewRecord, DiscoverySearchResultRecord } from "./search-types";

export interface DiscoverySearchQuery {
    categoryCodes: string[];
    dateFrom: string | null;
    dateTo: string | null;
    knowledgeBases: string[];
    pageNo: number;
    pageSize: number;
    queryText: string;
    tagNames: string[];
}

export interface DiscoverySearchClickEventCommand {
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

export interface DiscoverySearchPreviewQuery {
    contentId: string;
    contentType: string;
}

export const searchDiscovery = (query: DiscoverySearchQuery) => {
    return postJson<DiscoverySearchResultRecord, DiscoverySearchQuery>("/discovery/search/search", {
        body: query
    });
};

export const clickSearchResult = (command: DiscoverySearchClickEventCommand) => {
    return postJson<boolean, DiscoverySearchClickEventCommand>("/discovery/search/click", {
        body: command
    });
};

export const previewSearchResult = (query: DiscoverySearchPreviewQuery) => {
    return postJson<DiscoverySearchPreviewRecord, DiscoverySearchPreviewQuery>(
        "/discovery/search/preview",
        {
            body: query
        }
    );
};
