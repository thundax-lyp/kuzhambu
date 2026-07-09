export interface AiPromptTemplateRecord {
    templateId?: number | null;
    scope?: string | null;
    capability?: string | null;
    name?: string | null;
    description?: string | null;
    status?: string | null;
    currentVersionNo?: number | null;
    registeredAt?: string | null;
}

export interface AiPromptVersionRecord {
    promptVersionId?: number | null;
    templateId?: number | null;
    versionNo?: number | null;
    messageTemplatesJson?: string | null;
    variablesSnapshotJson?: string | null;
    outputSchemaJson?: string | null;
    current?: boolean | null;
    changeSummary?: string | null;
    registeredAt?: string | null;
}

export interface AiPromptVariableRecord {
    variableId?: number | null;
    templateId?: number | null;
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

export interface AiPromptActionStatusRecord {
    scope?: string | null;
    capability?: string | null;
    available?: boolean | null;
    unavailableReason?: string | null;
    checkedAt?: string | null;
}
