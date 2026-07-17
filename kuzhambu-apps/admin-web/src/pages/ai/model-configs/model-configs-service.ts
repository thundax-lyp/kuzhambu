import { postJson } from "@/api/http";
import type { AiModelRecord } from "./model-configs-types";

export interface AiModelListQuery {
    apiSource?: string | null;
    enabled?: boolean | null;
}

export interface AiModelChangeCommand {
    id?: number | null;
    apiSource: string;
    baseUrl: string;
    apiKey?: string | null;
    modelName: string;
    displayName?: string | null;
    capabilities: string[];
    defaultParamsJson?: string | null;
    description?: string | null;
    enabled: boolean;
}

export const listModelConfigs = (query: AiModelListQuery = {}) => {
    return postJson<AiModelRecord[], AiModelListQuery>("/ai/config/model/list", {
        body: query
    });
};

export const createModelConfig = (command: AiModelChangeCommand) => {
    return postJson<AiModelRecord, AiModelChangeCommand>("/ai/config/model/create", {
        body: command
    });
};

export const changeModelConfig = (command: AiModelChangeCommand) => {
    return postJson<AiModelRecord, AiModelChangeCommand>("/ai/config/model/update", {
        body: command
    });
};

export const deleteModelConfig = (id: number) => {
    return postJson<boolean, { id: number }>("/ai/config/model/delete", {
        body: { id }
    });
};
