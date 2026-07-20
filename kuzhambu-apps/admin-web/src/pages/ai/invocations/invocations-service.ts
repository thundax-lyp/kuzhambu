import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    AiCallRecord,
    AiCallSummaryRecord,
    AiInvocationCapabilityRecord
} from "./invocations-types";

export interface AiInvocationSummaryQuery {
    periodStart?: string | null;
    periodEnd?: string | null;
    bucketType?: string | null;
    capability?: string | null;
}

export interface AiCallRecordPageQuery {
    status?: string | null;
    requestedAtStart?: string | null;
    requestedAtEnd?: string | null;
    pageNo?: number;
    pageSize?: number;
}

export const listInvocationCapabilities = () => {
    return postJson<AiInvocationCapabilityRecord[], { enabled: boolean }>(
        "/ai/config/capability/list",
        {
            body: { enabled: true }
        }
    );
};

export const getInvocationSummary = (query: AiInvocationSummaryQuery = {}) => {
    return postJson<AiCallSummaryRecord, AiInvocationSummaryQuery>("/ai/invocation/call/summary", {
        body: query
    });
};

export const pageInvocationCalls = (query: AiCallRecordPageQuery = {}) => {
    return postJson<Page<AiCallRecord>, AiCallRecordPageQuery>("/ai/invocation/call/page", {
        body: query
    });
};
