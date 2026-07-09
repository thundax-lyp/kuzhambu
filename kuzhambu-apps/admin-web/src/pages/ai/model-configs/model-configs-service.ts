import { postJson } from "@/api/http";
import type {
    AiModelCheckRecord,
    AiModelRecord,
    AiModelServiceRecord
} from "./model-configs-types";

export interface AiModelListQuery {
    serviceId?: number | null;
    enabled?: boolean | null;
}

export interface AiModelChangeCommand {
    modelId?: number | null;
    serviceId: number;
    modelName: string;
    displayName?: string | null;
    capabilityTags: string[];
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

export const deleteModelConfig = (modelId: number) => {
    return postJson<boolean, { modelId: number }>("/ai/config/model/delete", {
        body: { modelId }
    });
};

export const refreshModelCheck = (modelId: number) => {
    return postJson<AiModelCheckRecord, { modelId: number }>("/ai/config/model/check", {
        body: { modelId }
    });
};

export const listModelCheckRecords = (modelId: number) => {
    return postJson<AiModelCheckRecord[], { modelId: number }>("/ai/config/model/check-records", {
        body: { modelId }
    });
};

export const getModelServiceByRole = (serviceRole: "PRIMARY" | "BACKUP") => {
    return postJson<AiModelServiceRecord, { serviceRole: "PRIMARY" | "BACKUP" }>(
        "/ai/config/service/get-by-role",
        {
            body: { serviceRole }
        }
    );
};

export const listModelServices = async () => {
    const [primary, backup] = await Promise.all([
        getModelServiceByRole("PRIMARY"),
        getModelServiceByRole("BACKUP")
    ]);
    return [primary, backup].filter((record): record is AiModelServiceRecord =>
        Boolean(record?.serviceId)
    );
};
