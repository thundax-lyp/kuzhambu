import { postJson } from "@/api/http";
import { normalizeId } from "@/types/id";
import type {
    AiPromptCapabilityRecord,
    AiPromptCapabilityVariableRecord,
    AiPromptTemplateRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "./prompt-types";

export interface AiPromptTemplateQuery {
    capability?: string | null;
    enabled?: boolean | null;
}

export interface AiPromptTemplateChangeCommand {
    id?: string | null;
    capability: string;
    name: string;
    description?: string | null;
    enabled?: boolean | null;
    messageTemplatesJson: string;
    variablesSnapshotJson?: string | null;
    outputSchemaJson?: string | null;
    changeSummary?: string | null;
    variables: AiPromptVariableRecord[];
}

export interface AiPromptTemplateIdCommand {
    id: string;
}

export interface AiPromptVariableValidateCommand {
    id: string;
    providedNames: string[];
}

export interface AiPromptVersionCompareCommand {
    id: string;
    leftVersionNo: number;
    rightVersionNo: number;
}

export interface AiPromptVersionRollbackCommand {
    id: string;
    versionNo: number;
}

export interface AiPromptSuggestionCommand {
    id: string;
    changeSummary?: string | null;
}

export interface AiPromptTemplateStatusCommand {
    id: string;
    enabled: boolean;
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
        "/ai/config/prompt/template/capability/get",
        {
            body: query
        }
    ).then(normalizePromptTemplateRecord);
};

export const listPromptTemplates = (query: AiPromptTemplateQuery = {}) => {
    return postJson<AiPromptTemplateRecord[], AiPromptTemplateQuery>(
        "/ai/config/prompt/template/list",
        {
            body: query
        }
    ).then((templates) => (templates || []).map(normalizePromptTemplateRecord));
};

export const changePromptTemplate = (command: AiPromptTemplateChangeCommand) => {
    return postJson<AiPromptTemplateRecord, AiPromptTemplateChangeCommand>(
        "/ai/config/prompt/template/update",
        {
            body: command
        }
    ).then(normalizePromptTemplateRecord);
};

export const changePromptTemplateStatus = (command: AiPromptTemplateStatusCommand) => {
    return postJson<boolean, AiPromptTemplateStatusCommand>(
        "/ai/config/prompt/template/status/update",
        { body: command }
    );
};

export const deletePromptTemplate = (id: string) => {
    return postJson<boolean, AiPromptTemplateIdCommand>("/ai/config/prompt/template/delete", {
        body: { id }
    });
};

export const listPromptCapabilityVariables = (capability: string) => {
    return postJson<AiPromptCapabilityVariableRecord[], { capability: string }>(
        "/ai/config/prompt/capability-variable/list",
        { body: { capability } }
    );
};

export const getCurrentPromptVersion = (templateId: string) => {
    return postJson<AiPromptVersionRecord, AiPromptTemplateIdCommand>(
        "/ai/config/prompt/version/latest",
        {
            body: { id: templateId }
        }
    ).then(normalizePromptVersionRecord);
};

export const listPromptVersions = (templateId: string) => {
    return postJson<AiPromptVersionRecord[], AiPromptTemplateIdCommand>(
        "/ai/config/prompt/version/list",
        {
            body: { id: templateId }
        }
    ).then((versions) => (versions || []).map(normalizePromptVersionRecord));
};

export const previewPromptVersionCompare = (command: AiPromptVersionCompareCommand) => {
    return postJson<AiPromptVersionRecord[], AiPromptVersionCompareCommand>(
        "/ai/config/prompt/version/compare/get",
        {
            body: command
        }
    ).then((versions) => (versions || []).map(normalizePromptVersionRecord));
};

export const changePromptVersionRollback = (command: AiPromptVersionRollbackCommand) => {
    return postJson<AiPromptVersionRecord, AiPromptVersionRollbackCommand>(
        "/ai/config/prompt/version/rollback/update",
        {
            body: command
        }
    ).then(normalizePromptVersionRecord);
};

export const listPromptVariables = (templateId: string) => {
    return postJson<AiPromptVariableRecord[], AiPromptTemplateIdCommand>(
        "/ai/config/prompt/variable/list",
        {
            body: { id: templateId }
        }
    ).then((variables) => (variables || []).map(normalizePromptVariableRecord));
};

export const confirmPromptVariables = (command: AiPromptVariableValidateCommand) => {
    return postJson<boolean, AiPromptVariableValidateCommand>(
        "/ai/config/prompt/variable/validation/get",
        {
            body: command
        }
    );
};

export const regeneratePromptSuggestion = (command: AiPromptSuggestionCommand) => {
    return postJson<AiPromptVersionRecord, AiPromptSuggestionCommand>(
        "/ai/config/prompt/optimization/suggestion/create",
        {
            body: command
        }
    ).then(normalizePromptVersionRecord);
};

const normalizePromptTemplateRecord = (
    template: AiPromptTemplateRecord
): AiPromptTemplateRecord => ({
    ...template,
    id: normalizeId(template?.id)
});

const normalizePromptVersionRecord = (version: AiPromptVersionRecord): AiPromptVersionRecord => ({
    ...version,
    id: normalizeId(version?.id),
    templateId: normalizeId(version?.templateId)
});

const normalizePromptVariableRecord = (
    variable: AiPromptVariableRecord
): AiPromptVariableRecord => ({
    ...variable,
    id: normalizeId(variable?.id),
    templateId: normalizeId(variable?.templateId)
});
