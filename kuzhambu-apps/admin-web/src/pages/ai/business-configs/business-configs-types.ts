export interface AiBusinessConfigRecord {
    id?: number | null;
    capability: string;
    promptTemplateId: number;
    modelId: number;
    defaultParamsJson?: string | null;
    enabled?: boolean | null;
    configuredAt?: string | null;
}

export interface AiBusinessConfigCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}

export interface AiBusinessConfigModelRecord {
    id: number;
    apiSource: string;
    baseUrl: string;
    modelName: string;
    displayName?: string | null;
    capabilities: string[];
    defaultParamsJson?: string | null;
    description?: string | null;
    enabled: boolean;
    registeredAt?: string | null;
}

export interface AiBusinessConfigPromptRecord {
    id?: number | null;
    capability?: string | null;
    name?: string | null;
    description?: string | null;
    enabled?: boolean | null;
    currentVersionNo?: number | null;
    registeredAt?: string | null;
}
