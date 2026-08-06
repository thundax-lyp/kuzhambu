import { postJson } from "@/api/http";
import { normalizeId } from "@/types/id";
import type { AiModelRecord } from "./ai-model-types";

export interface AiModelListQuery {
    apiSource?: string | null;
    enabled?: boolean | null;
}

export interface AiModelChangeCommand {
    id?: string | null;
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

export interface AiModelBatchChangeCommand {
    commands: AiModelChangeCommand[];
}

export interface AiModelBatchDeleteCommand {
    ids: string[];
}

export const listAiModels = (query: AiModelListQuery = {}) => {
    return postJson<AiModelRecord[], AiModelListQuery>("/ai/config/model/list", {
        body: query
    }).then((models) => (models || []).map(normalizeAiModelRecord));
};

export const createAiModel = (command: AiModelChangeCommand) => {
    return postJson<AiModelRecord, AiModelChangeCommand>("/ai/config/model/create", {
        body: command
    }).then(normalizeAiModelRecord);
};

export const changeAiModel = (command: AiModelChangeCommand) => {
    return postJson<AiModelRecord, AiModelChangeCommand>("/ai/config/model/update", {
        body: command
    }).then(normalizeAiModelRecord);
};

export const changeAiModels = (command: AiModelBatchChangeCommand) => {
    return Promise.allSettled(command.commands.map(changeAiModel));
};

export const deleteAiModel = (id: string) => {
    return postJson<boolean, { id: string }>("/ai/config/model/delete", {
        body: { id }
    });
};

export const deleteAiModels = (command: AiModelBatchDeleteCommand) => {
    return Promise.allSettled(command.ids.map(deleteAiModel));
};

const normalizeAiModelRecord = (model: AiModelRecord): AiModelRecord => ({
    ...model,
    id: normalizeId(model?.id)
});
