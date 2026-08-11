import { postJson } from "@/api/http";
import type {
    DiscoveryQaSessionExportRecord,
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSessionPageRecord,
    KnowledgeHealthRecord,
    KnowledgeSyncItemPageRecord,
    KnowledgeSyncItemRecord
} from "./qa-console-types";

export interface DiscoveryQaSessionGetCommand {
    sessionId: string;
}

export interface DiscoveryQaSessionPageQuery {
    openedAtEnd?: string | null;
    openedAtStart?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
    title?: string | null;
}

export interface DiscoveryQaSessionDeleteCommand {
    requesterUserId?: string | null;
    sessionId: string;
}

export interface DiscoveryQaSessionExportCommand {
    format?: string | null;
    requesterUserId?: string | null;
    sessionId: string;
}

export interface KnowledgeRebuildCommand {
    requestId?: string | null;
    traceId?: string | null;
}

export interface KnowledgeSyncContentCommand {
    contentId: string;
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

export const getKnowledge = () => {
    return postJson<KnowledgeHealthRecord>("/discovery/qa-admin/knowledge/get");
};

export const rebuildKnowledge = (command: KnowledgeRebuildCommand = {}) => {
    return postJson<number, KnowledgeRebuildCommand>("/discovery/qa-admin/knowledge/rebuild", {
        body: command
    });
};

export const updateKnowledge = (command: KnowledgeSyncContentCommand) => {
    return postJson<KnowledgeSyncItemRecord, KnowledgeSyncContentCommand>(
        "/discovery/qa-admin/knowledge/update",
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

export const pageQaSessions = (query: DiscoveryQaSessionPageQuery = {}) => {
    return postJson<DiscoveryQaSessionPageRecord, DiscoveryQaSessionPageQuery>(
        "/discovery/qa-admin/session/page",
        {
            body: query
        }
    );
};

export const deleteQaSession = (command: DiscoveryQaSessionDeleteCommand) => {
    return postJson<void, DiscoveryQaSessionDeleteCommand>("/discovery/qa-admin/session/delete", {
        body: command
    });
};

export const downloadQaSession = (command: DiscoveryQaSessionExportCommand) => {
    return postJson<DiscoveryQaSessionExportRecord, DiscoveryQaSessionExportCommand>(
        "/discovery/qa-admin/session/download",
        {
            body: command
        }
    );
};

export const getQaSessionDetail = getQaSession;
