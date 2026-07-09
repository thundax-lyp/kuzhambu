import { postJson } from "@/api/http";
import type {
    AiCapabilityMappingRecord,
    AiCapabilityModelRecord,
    AiCapabilityRecord
} from "./capability-mappings-types";

export interface AiCapabilityQuery {
    scope?: string | null;
    capability?: string | null;
    enabled?: boolean | null;
}

export interface AiCapabilityMappingChangeCommand {
    mappingId?: number | null;
    scope: string;
    capability: string;
    modelId: number;
    enabled: boolean;
}

export interface AiCapabilityModelQuery {
    enabled?: boolean | null;
}

export const listCapabilities = (query: AiCapabilityQuery = {}) => {
    return postJson<AiCapabilityRecord[], AiCapabilityQuery>("/ai/config/capability/list", {
        body: query
    });
};

export const listCapabilityMappings = (query: AiCapabilityQuery = {}) => {
    return postJson<AiCapabilityMappingRecord[], AiCapabilityQuery>(
        "/ai/config/capability/mapping/list",
        {
            body: query
        }
    );
};

export const changeCapabilityMapping = (command: AiCapabilityMappingChangeCommand) => {
    return postJson<{ id: number }, AiCapabilityMappingChangeCommand>(
        "/ai/config/capability/mapping/save",
        {
            body: command
        }
    );
};

export const listEnabledModels = (query: AiCapabilityModelQuery = { enabled: true }) => {
    return postJson<AiCapabilityModelRecord[], AiCapabilityModelQuery>("/ai/config/model/list", {
        body: query
    });
};
