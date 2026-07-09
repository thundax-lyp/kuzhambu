import { postJson } from "@/api/http";
import type {
    AiPromptActionStatusRecord,
    AiPromptCapabilityRecord,
    AiPromptTemplateRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "./prompts-types";

export interface AiPromptTemplateQuery {
    scope?: string | null;
    capability?: string | null;
}

export interface AiPromptTemplateChangeCommand {
    templateId?: number | null;
    scope: string;
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
    templateId: number;
}

export interface AiPromptVariableValidateCommand {
    templateId: number;
    providedNames: string[];
}

export interface AiPromptVersionCompareCommand {
    templateId: number;
    leftVersionNo: number;
    rightVersionNo: number;
}

export interface AiPromptVersionRollbackCommand {
    templateId: number;
    versionNo: number;
}

export interface AiPromptSuggestionCommand {
    templateId: number;
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

export const getPromptTemplateByScope = (query: AiPromptTemplateQuery) => {
    return postJson<AiPromptTemplateRecord, AiPromptTemplateQuery>(
        "/ai/prompt/template/get-by-scope",
        {
            body: query
        }
    );
};

export const changePromptTemplate = (command: AiPromptTemplateChangeCommand) => {
    return postJson<AiPromptTemplateRecord, AiPromptTemplateChangeCommand>(
        "/ai/prompt/template/save",
        {
            body: command
        }
    );
};

export const getCurrentPromptVersion = (templateId: number) => {
    return postJson<AiPromptVersionRecord, AiPromptTemplateIdCommand>(
        "/ai/prompt/version/current",
        {
            body: { templateId }
        }
    );
};

export const listPromptVersions = (templateId: number) => {
    return postJson<AiPromptVersionRecord[], AiPromptTemplateIdCommand>("/ai/prompt/version/list", {
        body: { templateId }
    });
};

export const previewPromptVersionCompare = (command: AiPromptVersionCompareCommand) => {
    return postJson<AiPromptVersionRecord[], AiPromptVersionCompareCommand>(
        "/ai/prompt/version/compare",
        {
            body: command
        }
    );
};

export const changePromptVersionRollback = (command: AiPromptVersionRollbackCommand) => {
    return postJson<AiPromptVersionRecord, AiPromptVersionRollbackCommand>(
        "/ai/prompt/version/rollback",
        {
            body: command
        }
    );
};

export const listPromptVariables = (templateId: number) => {
    return postJson<AiPromptVariableRecord[], AiPromptTemplateIdCommand>(
        "/ai/prompt/variable/list",
        {
            body: { templateId }
        }
    );
};

export const confirmPromptVariables = (command: AiPromptVariableValidateCommand) => {
    return postJson<boolean, AiPromptVariableValidateCommand>("/ai/prompt/variable/validate", {
        body: command
    });
};

export const regeneratePromptSuggestion = (command: AiPromptSuggestionCommand) => {
    return postJson<AiPromptVersionRecord, AiPromptSuggestionCommand>(
        "/ai/prompt/optimization/suggest",
        {
            body: command
        }
    );
};

export const getPromptActionStatus = (query: AiPromptTemplateQuery) => {
    return postJson<AiPromptActionStatusRecord, AiPromptTemplateQuery>("/ai/config/action/status", {
        body: query
    });
};
