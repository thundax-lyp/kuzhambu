import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    AiInvocationLogRecord,
    AiInvocationSummaryRecord,
    AiInvocationCapabilityRecord
} from "./invocations-types";

export interface AiInvocationSummaryQuery {
    periodStart?: string | null;
    periodEnd?: string | null;
    bucketType?: string | null;
    capability?: string | null;
}

export interface AiInvocationLogPageQuery {
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
    return postJson<AiInvocationSummaryRecord, AiInvocationSummaryQuery>(
        "/ai/invocation/invocation-log/summary",
        {
            body: query
        }
    );
};

export const pageInvocationLogs = (query: AiInvocationLogPageQuery = {}) => {
    return postJson<Page<AiInvocationLogRecord>, AiInvocationLogPageQuery>(
        "/ai/invocation/invocation-log/page",
        {
            body: query
        }
    );
};
