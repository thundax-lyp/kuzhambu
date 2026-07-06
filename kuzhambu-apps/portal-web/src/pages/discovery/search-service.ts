import { postJson } from "@/api/http";
import type {
    DiscoverySearchClickRequest,
    DiscoverySearchRequest,
    DiscoverySearchResponse
} from "./search-types";

export const toDiscoverySearchPayload = (
    request: DiscoverySearchRequest
): DiscoverySearchRequest => ({
    categoryCodes: request.categoryCodes,
    contentStatuses: request.contentStatuses,
    dateFrom: request.dateFrom,
    dateTo: request.dateTo,
    knowledgeBases: request.knowledgeBases,
    pageNo: request.pageNo,
    pageSize: request.pageSize,
    queryText: request.queryText,
    tagNames: request.tagNames,
    visibilityScopes: request.visibilityScopes
});

export const searchDiscovery = (request: DiscoverySearchRequest) => {
    return postJson<DiscoverySearchResponse, DiscoverySearchRequest>(
        "/portal/discovery/search/search",
        toDiscoverySearchPayload(request)
    );
};

export const recordSearchClick = (request: DiscoverySearchClickRequest) => {
    return postJson<boolean, DiscoverySearchClickRequest>(
        "/portal/discovery/search/click",
        request
    );
};
