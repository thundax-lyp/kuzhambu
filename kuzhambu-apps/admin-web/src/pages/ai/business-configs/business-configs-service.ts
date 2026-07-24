import { postJson } from "@/api/http";
import type {
    AiBusinessConfigCapabilityRecord,
    AiBusinessConfigModelRecord,
    AiBusinessConfigPromptRecord,
    AiBusinessConfigRecord
} from "./business-configs-types";

export interface AiBusinessConfigQuery {
    capability?: string | null;
    enabled?: boolean | null;
}

export interface AiBusinessConfigChangeCommand {
    id?: number | null;
    capability: string;
    promptTemplateId: number;
    modelId: number;
    defaultParamsJson?: string | null;
    enabled: boolean;
}

export const listBusinessConfigs = (query: AiBusinessConfigQuery = {}) => {
    return postJson<AiBusinessConfigRecord[], AiBusinessConfigQuery>(
        "/ai/config/business-config/list",
        {
            body: query
        }
    );
};

export const createBusinessConfig = (command: AiBusinessConfigChangeCommand) => {
    return postJson<AiBusinessConfigRecord, AiBusinessConfigChangeCommand>(
        "/ai/config/business-config/create",
        {
            body: command
        }
    );
};

export const changeBusinessConfig = (command: AiBusinessConfigChangeCommand) => {
    return postJson<AiBusinessConfigRecord, AiBusinessConfigChangeCommand>(
        "/ai/config/business-config/update",
        {
            body: command
        }
    );
};

export const deleteBusinessConfig = (id: number) => {
    return postJson<boolean, { id: number }>("/ai/config/business-config/delete", {
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
    });
};

export const listBusinessConfigPrompts = () => {
    return postJson<AiBusinessConfigPromptRecord[], { enabled: boolean }>(
        "/ai/config/prompt/template/list",
        {
            body: { enabled: true }
        }
    );
};
