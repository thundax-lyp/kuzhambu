import { postJson } from "@/api/http";
import { normalizeId } from "@/types/id";
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
    mappingId?: string | null;
    scope: string;
    capability: string;
    modelId: string;
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
    ).then((mappings) => (mappings || []).map(normalizeCapabilityMappingRecord));
};

export const changeCapabilityMapping = (command: AiCapabilityMappingChangeCommand) => {
    return postJson<{ id: string }, AiCapabilityMappingChangeCommand>(
        "/ai/config/capability/mapping/save",
        {
            body: command
        }
    ).then((result) => ({ ...result, id: normalizeId(result?.id) }));
};

export const listEnabledModels = (query: AiCapabilityModelQuery = { enabled: true }) => {
    return postJson<AiCapabilityModelRecord[], AiCapabilityModelQuery>("/ai/config/model/list", {
        body: query
    }).then((models) => (models || []).map(normalizeCapabilityModelRecord));
};

const normalizeCapabilityMappingRecord = (
    mapping: AiCapabilityMappingRecord
): AiCapabilityMappingRecord => ({
    ...mapping,
    mappingId: normalizeId(mapping?.mappingId),
    modelId: normalizeId(mapping?.modelId)
});

const normalizeCapabilityModelRecord = (
    model: AiCapabilityModelRecord
): AiCapabilityModelRecord => ({
    ...model,
    modelId: normalizeId(model?.modelId),
    serviceId: normalizeId(model?.serviceId)
});
