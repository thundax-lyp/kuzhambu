import type { AiServiceRole } from "../services/services-types";

export interface AiModelRecord {
    modelId: number;
    serviceId: number;
    modelName: string;
    displayName?: string | null;
    capabilityTags: string[];
    defaultParamsJson?: string | null;
    description?: string | null;
    enabled: boolean;
    registeredAt?: string | null;
}

export interface AiModelCheckRecord {
    checkId: number;
    modelId: number;
    serviceId: number;
    modelName: string;
    status: string;
    latencyMs?: number | null;
    errorType?: string | null;
    errorMessage?: string | null;
    checkedAt?: string | null;
}

export interface AiModelServiceRecord {
    serviceId: number;
    serviceRole: AiServiceRole;
    apiSource: string;
    baseUrl: string;
    enabled: boolean;
    status: string;
}
