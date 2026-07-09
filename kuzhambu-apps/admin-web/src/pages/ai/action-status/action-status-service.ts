import { postJson } from "@/api/http";
import type { AiActionCapabilityRecord, AiActionStatusRecord } from "./action-status-types";

export interface AiActionStatusQuery {
    scope?: string | null;
    capability?: string | null;
    available?: boolean | null;
}

export interface AiActionStatusRefreshCommand {
    scope: string;
    capability: string;
}

export const listActionCapabilities = () => {
    return postJson<AiActionCapabilityRecord[], { enabled: boolean }>(
        "/ai/config/capability/list",
        {
            body: { enabled: true }
        }
    );
};

export const listActionStatuses = (query: AiActionStatusQuery = {}) => {
    return postJson<AiActionStatusRecord[], AiActionStatusQuery>("/ai/config/action/status/list", {
        body: query
    });
};

export const refreshActionStatus = (command: AiActionStatusRefreshCommand) => {
    return postJson<AiActionStatusRecord, AiActionStatusRefreshCommand>(
        "/ai/config/action/status/refresh",
        {
            body: command
        }
    );
};
