export interface AiCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}

export interface AiCapabilityMappingRecord {
    mappingId?: number | null;
    scope: string;
    capability: string;
    modelId: number;
    enabled: boolean;
    configuredAt?: string | null;
}

export interface AiCapabilityModelRecord {
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
