import { postJson } from "@/api/http";
import { normalizeId } from "@/types/id";
import type {
    AiBusinessConfigCapabilityRecord,
    AiBusinessConfigModelRecord,
    AiBusinessConfigPromptRecord,
    AiBusinessConfigRecord
} from "./business-config-types";

export interface AiBusinessConfigQuery {
    capability?: string | null;
    enabled?: boolean | null;
}

export interface AiBusinessConfigChangeCommand {
    id?: string | null;
    capability: string;
    promptTemplateId: string;
    modelId: string;
    defaultParamsJson?: string | null;
    enabled: boolean;
}

export const listBusinessConfigs = (query: AiBusinessConfigQuery = {}) => {
    return postJson<AiBusinessConfigRecord[], AiBusinessConfigQuery>(
        "/ai/config/business-config/list",
        {
            body: query
        }
    ).then((configs) => (configs || []).map(normalizeBusinessConfigRecord));
};

export const createBusinessConfig = (command: AiBusinessConfigChangeCommand) => {
    return postJson<AiBusinessConfigRecord, AiBusinessConfigChangeCommand>(
        "/ai/config/business-config/create",
        {
            body: command
        }
    ).then(normalizeBusinessConfigRecord);
};

export const changeBusinessConfig = (command: AiBusinessConfigChangeCommand) => {
    return postJson<AiBusinessConfigRecord, AiBusinessConfigChangeCommand>(
        "/ai/config/business-config/update",
        {
            body: command
        }
    ).then(normalizeBusinessConfigRecord);
};

export const deleteBusinessConfig = (id: string) => {
    return postJson<boolean, { id: string }>("/ai/config/business-config/delete", {
        body: { id }
    });
};

export const listBusinessConfigCapabilities = () => {
    return postJson<AiBusinessConfigCapabilityRecord[], { enabled: boolean }>(
        "/ai/config/capability/list",
        {
            body: { enabled: true }
        }
    );
};

export const listBusinessConfigModels = () => {
    return postJson<AiBusinessConfigModelRecord[], { enabled: boolean }>("/ai/config/model/list", {
        body: { enabled: true }
    }).then((models) => (models || []).map(normalizeBusinessConfigModelRecord));
};

export const listBusinessConfigPrompts = () => {
    return postJson<AiBusinessConfigPromptRecord[], { enabled: boolean }>(
        "/ai/config/prompt/template/list",
        {
            body: { enabled: true }
        }
    ).then((prompts) => (prompts || []).map(normalizeBusinessConfigPromptRecord));
};

const normalizeBusinessConfigRecord = (config: AiBusinessConfigRecord): AiBusinessConfigRecord => ({
    ...config,
    id: normalizeId(config?.id),
    promptTemplateId: normalizeId(config?.promptTemplateId),
    modelId: normalizeId(config?.modelId)
});

const normalizeBusinessConfigModelRecord = (
    model: AiBusinessConfigModelRecord
): AiBusinessConfigModelRecord => ({
    ...model,
    id: normalizeId(model?.id)
});

const normalizeBusinessConfigPromptRecord = (
    prompt: AiBusinessConfigPromptRecord
): AiBusinessConfigPromptRecord => ({
    ...prompt,
    id: normalizeId(prompt?.id)
});
