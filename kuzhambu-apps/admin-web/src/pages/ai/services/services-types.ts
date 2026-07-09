export type AiServiceRole = "PRIMARY" | "BACKUP";

export interface AiServiceConfigRecord {
    serviceId: number;
    serviceRole: AiServiceRole;
    apiSource: string;
    baseUrl: string;
    apiKeyConfigured: boolean;
    enabled: boolean;
    status: string;
    lastCheckedAt?: string | null;
    configuredAt?: string | null;
}
