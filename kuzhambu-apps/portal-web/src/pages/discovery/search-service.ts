import { postJson } from "@/api/http";
import type {
    DiscoverySearchClickRequest,
    DiscoverySearchRequest,
    DiscoverySearchResponse
} from "./search-types";

export const searchDiscovery = (request: DiscoverySearchRequest) => {
    return postJson<DiscoverySearchResponse, DiscoverySearchRequest>(
        "/portal/discovery/search/search",
        request
    );
};

export const recordSearchClick = (request: DiscoverySearchClickRequest) => {
    return postJson<boolean, DiscoverySearchClickRequest>(
        "/portal/discovery/search/click",
        request
    );
};
