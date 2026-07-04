import { postJson } from "@/api/http";
import type {
    DiscoveryQaChatCompletionRequest,
    DiscoveryQaChatCompletionResponse,
    DiscoveryQaGetSessionRequest,
    DiscoveryQaOpenSessionRequest,
    DiscoveryQaOpenSessionResponse,
    DiscoveryQaSessionPageQuery,
    DiscoveryQaSessionPageResponse
} from "./qa-types";

export const openQaSession = (request: DiscoveryQaOpenSessionRequest) => {
    return postJson<DiscoveryQaOpenSessionResponse, DiscoveryQaOpenSessionRequest>(
        "/portal/discovery/qa/session/open",
        request
    );
};

export const pageQaSessions = (query: DiscoveryQaSessionPageQuery) => {
    return postJson<DiscoveryQaSessionPageResponse, DiscoveryQaSessionPageQuery>(
        "/portal/discovery/qa/session/page",
        query
    );
};

export const getQaSession = (request: DiscoveryQaGetSessionRequest) => {
    return postJson<DiscoveryQaOpenSessionResponse, DiscoveryQaGetSessionRequest>(
        "/portal/discovery/qa/session/get",
        request
    );
};

export const createQaChatCompletion = (request: DiscoveryQaChatCompletionRequest) => {
    return postJson<DiscoveryQaChatCompletionResponse, DiscoveryQaChatCompletionRequest>(
        "/portal/discovery/qa/chat/completions",
        request
    );
};
