export type SancaiAiTextField = "translate" | "summary";

const AI_TEXT_FIELD_CONFIG: Record<
    SancaiAiTextField,
    {
        actionLabel: string;
        aiLabel: string;
        applyMessage: string;
        candidateChangeSummary: string;
        currentLabel: string;
        emptyText: string;
        fieldLabel: string;
        loadingText: string;
        modalTitle: string;
        sourceLabel: string;
        taskLabel: string;
    }
> = {
    translate: {
        actionLabel: "翻译",
        aiLabel: "AI译文",
        applyMessage: "译文已写入基础信息",
        candidateChangeSummary: "AI 应用：译文",
        currentLabel: "当前译文",
        emptyText: "暂无候选译文，可先保留当前译文或稍后重试",
        fieldLabel: "AI翻译",
        loadingText: "AI 翻译生成中...",
        modalTitle: "AI翻译",
        sourceLabel: "原文",
        taskLabel: "翻译"
    },
    summary: {
        actionLabel: "摘要",
        aiLabel: "AI摘要",
        applyMessage: "摘要已写入基础信息",
        candidateChangeSummary: "AI 应用：摘要",
        currentLabel: "当前摘要",
        emptyText: "暂无候选摘要，可先保留当前摘要或稍后重试",
        fieldLabel: "AI摘要",
        loadingText: "AI 摘要生成中...",
        modalTitle: "AI摘要",
        sourceLabel: "原文",
        taskLabel: "摘要"
    }
};

export const readSancaiAiTextFieldConfig = (field: SancaiAiTextField) => {
    return AI_TEXT_FIELD_CONFIG[field];
};
