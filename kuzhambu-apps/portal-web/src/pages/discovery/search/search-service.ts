import { postJson } from "@/api/http";
import type {
    DiscoverySearchClickEventRequest,
    DiscoverySearchPreviewRequest,
    DiscoverySearchPreviewResponse,
    DiscoverySearchRequest,
    DiscoverySearchResponse
} from "./search-types";

export const toDiscoverySearchPayload = (
    request: DiscoverySearchRequest
): DiscoverySearchRequest => ({
    categoryCodes: request.categoryCodes,
    dateFrom: request.dateFrom,
    dateTo: request.dateTo,
    knowledgeBases: request.knowledgeBases,
    pageNo: request.pageNo,
    pageSize: request.pageSize,
    queryText: request.queryText,
    tagNames: request.tagNames
});

export const searchDiscovery = (request: DiscoverySearchRequest) => {
    return postJson<DiscoverySearchResponse, DiscoverySearchRequest>(
        "/portal/discovery/search/search",
        toDiscoverySearchPayload(request)
    );
};

export const recordSearchClickEvent = (request: DiscoverySearchClickEventRequest) => {
    return postJson<boolean, DiscoverySearchClickEventRequest>(
        "/portal/discovery/search/click",
        request
    );
};

export const previewSearchResult = (request: DiscoverySearchPreviewRequest) => {
    return postJson<DiscoverySearchPreviewResponse, DiscoverySearchPreviewRequest>(
        "/portal/discovery/search/preview",
        request
    );
};
