export interface AiActionStatusRecord {
    scope?: string | null;
    capability?: string | null;
    available?: boolean | null;
    unavailableReason?: string | null;
    checkedAt?: string | null;
}

export interface AiActionCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}
