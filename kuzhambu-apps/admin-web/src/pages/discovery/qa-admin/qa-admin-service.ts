import { postJson } from "@/api/http";
import type {
    DiscoveryQaSessionExportRecord,
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSourceRecord,
    KnowledgeHealthRecord,
    KnowledgeSyncItemPageRecord,
    KnowledgeSyncItemRecord,
    ProviderTraceRecord
} from "./qa-admin-types";

export interface DiscoveryQaSessionGetCommand {
    sessionId: string;
}

export interface DiscoveryQaSessionDeleteCommand {
    requesterUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaSessionExportCommand {
    format?: string | null;
    requesterUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaSourceListCommand {
    messageId: string;
}

export interface DiscoveryQaTraceGetCommand {
    traceId: string;
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

export const deleteQaSession = (command: DiscoveryQaSessionDeleteCommand) => {
    return postJson<void, DiscoveryQaSessionDeleteCommand>("/discovery/qa-admin/session/delete", {
        body: command
    });
};

export const createQaSessionExport = (command: DiscoveryQaSessionExportCommand) => {
    return postJson<DiscoveryQaSessionExportRecord, DiscoveryQaSessionExportCommand>(
        "/discovery/qa-admin/session/export",
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
