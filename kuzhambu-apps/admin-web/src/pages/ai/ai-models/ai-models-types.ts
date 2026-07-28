export interface AiModelRecord {
    id: string;
    apiSource: string;
    baseUrl: string;
    apiKeyConfigured?: boolean | null;
    modelName: string;
    displayName?: string | null;
    capabilities: string[];
    defaultParamsJson?: string | null;
    description?: string | null;
    enabled: boolean;
    registeredAt?: string | null;
}
