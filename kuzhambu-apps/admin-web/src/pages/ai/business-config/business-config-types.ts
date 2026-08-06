export interface AiBusinessConfigRecord {
    id?: string | null;
    capability: string;
    promptTemplateId: string;
    modelId: string;
    defaultParamsJson?: string | null;
    enabled?: boolean | null;
    configuredAt?: string | null;
}

export interface AiBusinessConfigCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    requiredModelCapabilities: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}

export interface AiBusinessConfigModelRecord {
    id: string;
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
    id?: string | null;
    capability?: string | null;
    name?: string | null;
    description?: string | null;
    enabled?: boolean | null;
    currentVersionNo?: number | null;
    registeredAt?: string | null;
}
