import type { OpenClientStatus } from "./open-client-service";

export interface OpenClientRecord {
    id: string;
    name: string;
    status?: OpenClientStatus | string | null;
    apiKey?: string | null;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[] | null;
}

export interface OpenClientSecretRecord {
    id: string;
    apiKey?: string | null;
    apiSecret?: string | null;
}
