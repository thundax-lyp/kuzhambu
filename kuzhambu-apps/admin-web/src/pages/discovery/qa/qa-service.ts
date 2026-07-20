import { postJson } from "@/api/http";
import type {
    DiscoveryQaChatCompletionRecord,
    DiscoveryQaChatMessage,
    DiscoveryQaChatMetadata,
    DiscoveryQaExportSessionRecord,
    DiscoveryQaSessionPageRecord,
    DiscoveryQaSessionRecord
} from "./qa-types";

export interface DiscoveryQaOpenSessionCommand {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    ownerUserId?: number | null;
    requestId?: string | null;
    scope?: string | null;
    title?: string | null;
    traceId?: string | null;
}

export interface DiscoveryQaSessionPageQuery {
    ownerUserId?: number | null;
    pageNo?: number | null;
    pageSize?: number | null;
    scope?: string | null;
}

export interface DiscoveryQaGetSessionQuery {
    ownerUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaExportSessionCommand {
    format?: string | null;
    ownerUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaChatCompletionCommand {
    messages: DiscoveryQaChatMessage[];
    metadata?: DiscoveryQaChatMetadata | null;
    model: string;
    options?: Record<string, unknown> | null;
    requestId?: string | null;
    sessionId: string;
    stream: boolean;
    traceId?: string | null;
}

export const createQaSession = (command: DiscoveryQaOpenSessionCommand) => {
    return postJson<DiscoveryQaSessionRecord, DiscoveryQaOpenSessionCommand>(
        "/portal/discovery/qa/session/open",
        {
            body: command
        }
    );
};

export const pageQaSessions = (query: DiscoveryQaSessionPageQuery) => {
    return postJson<DiscoveryQaSessionPageRecord, DiscoveryQaSessionPageQuery>(
        "/portal/discovery/qa/session/page",
        {
            body: query
        }
    );
};

export const getQaSession = (query: DiscoveryQaGetSessionQuery) => {
    return postJson<DiscoveryQaSessionRecord, DiscoveryQaGetSessionQuery>(
        "/portal/discovery/qa/session/get",
        {
            body: query
        }
    );
};

export const createQaSessionExport = (command: DiscoveryQaExportSessionCommand) => {
    return postJson<DiscoveryQaExportSessionRecord, DiscoveryQaExportSessionCommand>(
        "/portal/discovery/qa/session/export",
        {
            body: command
        }
    );
};

export const createQaChatCompletion = (command: DiscoveryQaChatCompletionCommand) => {
    return postJson<DiscoveryQaChatCompletionRecord, DiscoveryQaChatCompletionCommand>(
        "/portal/discovery/qa/chat/completions",
        {
            body: command
        }
    );
};
