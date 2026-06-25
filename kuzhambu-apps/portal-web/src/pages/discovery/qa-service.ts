import { postJson } from "@/api/http";
import type {
    DiscoveryQaAskQuestionRequest,
    DiscoveryQaAskQuestionResponse,
    DiscoveryQaOpenSessionRequest,
    DiscoveryQaOpenSessionResponse
} from "./qa-types";

export const openQaSession = (request: DiscoveryQaOpenSessionRequest) => {
    return postJson<DiscoveryQaOpenSessionResponse, DiscoveryQaOpenSessionRequest>(
        "/portal/discovery/qa/session/open",
        request
    );
};

export const askQaQuestion = (request: DiscoveryQaAskQuestionRequest) => {
    return postJson<DiscoveryQaAskQuestionResponse, DiscoveryQaAskQuestionRequest>(
        "/portal/discovery/qa/question/ask",
        request
    );
};
