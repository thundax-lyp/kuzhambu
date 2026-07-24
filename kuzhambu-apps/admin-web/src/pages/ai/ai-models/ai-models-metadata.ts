import { type KuzhambuTagType } from "@/components";

export const DEFAULT_MODEL_PARAMS = "{}";

export const API_SOURCE_OPTIONS = ["OPENAI", "BYTEDANCE"];

export const MODEL_CAPABILITY_OPTIONS = ["TEXT2TEXT", "TEXT2IMAGE", "IMAGE2TEXT", "IMAGE2IMAGE"];

const API_SOURCE_META: Record<
    string,
    {
        label: string;
        type: KuzhambuTagType;
    }
> = {
    OPENAI: { label: "OpenAI 兼容", type: "accent" },
    BYTEDANCE: { label: "火山方舟", type: "warning" }
};

const MODEL_CAPABILITY_META: Record<
    string,
    {
        label: string;
        type: KuzhambuTagType;
    }
> = {
    TEXT2TEXT: { label: "文本生成", type: "accent" },
    TEXT2IMAGE: { label: "文生图", type: "warning" },
    IMAGE2TEXT: { label: "图像理解", type: "info" },
    IMAGE2IMAGE: { label: "图生图", type: "success" }
};

export const normalizeJsonText = (value?: string | null) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : DEFAULT_MODEL_PARAMS;
};

export const readCapabilityMeta = (capability: string) => {
    return MODEL_CAPABILITY_META[capability] ?? { label: capability, type: "neutral" };
};

export const readApiSourceMeta = (apiSource: string) => {
    return API_SOURCE_META[apiSource] ?? { label: apiSource, type: "neutral" };
};
