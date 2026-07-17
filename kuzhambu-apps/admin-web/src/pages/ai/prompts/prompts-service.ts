import { postJson } from "@/api/http";
import type {
    AiPromptCapabilityRecord,
    AiPromptTemplateRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "./prompts-types";

export interface AiPromptTemplateQuery {
    capability?: string | null;
}

export interface AiPromptTemplateChangeCommand {
    id?: number | null;
    capability: string;
    name: string;
    description?: string | null;
    status?: string | null;
    messageTemplatesJson: string;
    variablesSnapshotJson?: string | null;
    outputSchemaJson?: string | null;
    changeSummary?: string | null;
    variables: AiPromptVariableRecord[];
}

export interface AiPromptTemplateIdCommand {
    id: number;
}

export interface AiPromptVariableValidateCommand {
    id: number;
    providedNames: string[];
}

export interface AiPromptVersionCompareCommand {
    id: number;
    leftVersionNo: number;
    rightVersionNo: number;
}

export interface AiPromptVersionRollbackCommand {
    id: number;
    versionNo: number;
}

export interface AiPromptSuggestionCommand {
    id: number;
    changeSummary?: string | null;
}

export const listPromptCapabilities = () => {
    return postJson<AiPromptCapabilityRecord[], { enabled: boolean }>(
        "/ai/config/capability/list",
        {
            body: { enabled: true }
        }
    );
};

export const getPromptTemplateByCapability = (query: AiPromptTemplateQuery) => {
    return postJson<AiPromptTemplateRecord, AiPromptTemplateQuery>(
        "/ai/config/prompt/template/get-by-capability",
        {
            body: query
        }
    );
};

export const changePromptTemplate = (command: AiPromptTemplateChangeCommand) => {
    return postJson<AiPromptTemplateRecord, AiPromptTemplateChangeCommand>(
        "/ai/config/prompt/template/save",
        {
            body: command
        }
    );
};

export const getCurrentPromptVersion = (templateId: number) => {
    return postJson<AiPromptVersionRecord, AiPromptTemplateIdCommand>(
        "/ai/config/prompt/version/current",
        {
            body: { id: templateId }
        }
    );
};

export const listPromptVersions = (templateId: number) => {
    return postJson<AiPromptVersionRecord[], AiPromptTemplateIdCommand>(
        "/ai/config/prompt/version/list",
        {
            body: { id: templateId }
        }
    );
};

export const previewPromptVersionCompare = (command: AiPromptVersionCompareCommand) => {
    return postJson<AiPromptVersionRecord[], AiPromptVersionCompareCommand>(
        "/ai/config/prompt/version/compare",
        {
            body: command
        }
    );
};

export const changePromptVersionRollback = (command: AiPromptVersionRollbackCommand) => {
    return postJson<AiPromptVersionRecord, AiPromptVersionRollbackCommand>(
        "/ai/config/prompt/version/rollback",
        {
            body: command
        }
    );
};

export const listPromptVariables = (templateId: number) => {
    return postJson<AiPromptVariableRecord[], AiPromptTemplateIdCommand>(
        "/ai/config/prompt/variable/list",
        {
            body: { id: templateId }
        }
    );
};

export const confirmPromptVariables = (command: AiPromptVariableValidateCommand) => {
    return postJson<boolean, AiPromptVariableValidateCommand>(
        "/ai/config/prompt/variable/validate",
        {
            body: command
        }
    );
};

export const regeneratePromptSuggestion = (command: AiPromptSuggestionCommand) => {
    return postJson<AiPromptVersionRecord, AiPromptSuggestionCommand>(
        "/ai/config/prompt/optimization/suggest",
        {
            body: command
        }
    );
};
