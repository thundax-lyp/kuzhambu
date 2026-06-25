import { postJson } from "@/api/http";
import type {
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSourceRecord,
    DiscoveryQaTraceRecord
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

export const getQaSessionDetail = (command: DiscoveryQaSessionGetCommand) => {
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
    return postJson<DiscoveryQaTraceRecord, DiscoveryQaTraceGetCommand>(
        "/discovery/qa-admin/trace/get",
        {
            body: command
        }
    );
};
