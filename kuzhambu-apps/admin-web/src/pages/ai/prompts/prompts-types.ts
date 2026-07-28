export interface AiPromptTemplateRecord {
    id?: string | null;
    capability?: string | null;
    name?: string | null;
    description?: string | null;
    enabled?: boolean | null;
    currentVersionNo?: number | null;
    registeredAt?: string | null;
}

export interface AiPromptVersionRecord {
    id?: string | null;
    templateId?: string | null;
    versionNo?: number | null;
    messageTemplatesJson?: string | null;
    variablesSnapshotJson?: string | null;
    outputSchemaJson?: string | null;
    changeSummary?: string | null;
    registeredAt?: string | null;
}

export interface AiPromptVariableRecord {
    id?: string | null;
    templateId?: string | null;
    variableName: string;
    required: boolean;
    description?: string | null;
    priority?: number | null;
}

export interface AiPromptCapabilityRecord {
    capability: string;
    name: string;
    requiredTags: string[];
    outputMode: string;
    enabled: boolean;
    priority: number;
}
