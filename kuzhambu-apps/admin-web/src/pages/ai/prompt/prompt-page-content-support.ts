import type { Key } from "react";
import { type KuzhambuTagType } from "@/components";
import type { AiPromptTemplateRecord } from "./prompt-types";

export interface PromptFilters {
    capability?: string | null;
    enabled: "ALL" | "ENABLED" | "DISABLED";
}

export interface PromptCapabilityOption {
    label: string;
    value: string;
}

export const DEFAULT_PROMPT_FILTERS: PromptFilters = {
    capability: null,
    enabled: "ALL"
};

export const readCapabilityLabel = (capability?: string | null, name?: string | null) => {
    const label = name?.trim();
    return label || capability || "-";
};

export const readCapabilityDomainTag = (
    capability?: string | null
): { label: string; type: KuzhambuTagType } => {
    if (capability?.startsWith("classics_")) {
        return { label: "古籍", type: "info" };
    }
    if (capability?.startsWith("discovery_")) {
        return { label: "发现", type: "accent" };
    }
    if (capability?.startsWith("knowledge_")) {
        return { label: "知识", type: "success" };
    }
    if (capability?.startsWith("platform_")) {
        return { label: "平台", type: "warning" };
    }
    if (capability?.startsWith("prompt_")) {
        return { label: "提示词", type: "danger" };
    }
    return { label: "其他", type: "neutral" };
};

const readPromptName = (template: AiPromptTemplateRecord) => {
    return template.name?.trim() || template.capability || `模板 ${template.id ?? ""}`;
};

export const readTemplateRowKey = (template: AiPromptTemplateRecord): Key => {
    return template.id || template.capability || "";
};

export const readPromptDisplayName = (
    template: AiPromptTemplateRecord,
    capabilityName?: string | null
) => {
    const name = template.name?.trim();
    if (name && !/\bDefault\b/i.test(name)) {
        return name;
    }
    const capabilityLabel = readCapabilityLabel(template.capability, capabilityName);
    return capabilityLabel === "-" ? readPromptName(template) : `${capabilityLabel}提示词`;
};
