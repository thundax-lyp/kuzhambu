export interface AiCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}

export interface AiCapabilityMappingRecord {
    mappingId?: string | null;
    scope: string;
    capability: string;
    modelId: string;
    enabled: boolean;
    configuredAt?: string | null;
}

export interface AiCapabilityModelRecord {
    modelId: string;
    serviceId: string;
    modelName: string;
    displayName?: string | null;
    capabilityTags: string[];
    defaultParamsJson?: string | null;
    description?: string | null;
    enabled: boolean;
    registeredAt?: string | null;
}
