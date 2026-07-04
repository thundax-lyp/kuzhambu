import { postJson } from "@/api/http";
import type {
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSourceRecord,
    KnowledgeHealthRecord,
    KnowledgeSyncItemPageRecord,
    KnowledgeSyncItemRecord,
    ProviderTraceRecord
} from "./qa-admin-types";

export interface DiscoveryQaSessionGetCommand {
    sessionId: number;
}

export interface DiscoveryQaSourceListCommand {
    messageId: number;
}

export interface DiscoveryQaTraceGetCommand {
    traceId: number;
}

export interface KnowledgeRebuildCommand {
    requestId?: string | null;
    traceId?: string | null;
}

export interface KnowledgeSyncContentCommand {
    contentId: number;
    contentType: string;
    currentVersionNo?: number | null;
    requestId?: string | null;
    traceId?: string | null;
}

export interface KnowledgeSyncItemPageQuery {
    contentType?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
    syncStatus?: string | null;
}

export const getKnowledgeHealth = () => {
    return postJson<KnowledgeHealthRecord>("/discovery/qa-admin/knowledge/health");
};

export const rebuildKnowledge = (command: KnowledgeRebuildCommand = {}) => {
    return postJson<number, KnowledgeRebuildCommand>("/discovery/qa-admin/knowledge/rebuild", {
        body: command
    });
};

export const createKnowledgeSync = (command: KnowledgeSyncContentCommand) => {
    return postJson<KnowledgeSyncItemRecord, KnowledgeSyncContentCommand>(
        "/discovery/qa-admin/knowledge/sync",
        {
            body: command
        }
    );
};

export const pageKnowledgeSyncItems = (query: KnowledgeSyncItemPageQuery = {}) => {
    return postJson<KnowledgeSyncItemPageRecord, KnowledgeSyncItemPageQuery>(
        "/discovery/qa-admin/knowledge/sync/page",
        {
            body: query
        }
    );
};

export const getQaSession = (command: DiscoveryQaSessionGetCommand) => {
    return postJson<DiscoveryQaSessionDetailRecord, DiscoveryQaSessionGetCommand>(
        "/discovery/qa-admin/session/get",
        {
            body: command
        }
    );
};

export const listQaSources = (command: DiscoveryQaSourceListCommand) => {
    return postJson<DiscoveryQaSourceRecord[], DiscoveryQaSourceListCommand>(
        "/discovery/qa-admin/source/list",
        {
            body: command
        }
    );
};

export const getQaTrace = (command: DiscoveryQaTraceGetCommand) => {
    return postJson<ProviderTraceRecord, DiscoveryQaTraceGetCommand>(
        "/discovery/qa-admin/trace/get",
        {
            body: command
        }
    );
};

export const getQaSessionDetail = getQaSession;
