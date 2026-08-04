export interface PromptCapabilityVariableDefinition {
    description: string;
    required: boolean;
    variableName: string;
}

const variable = (
    variableName: string,
    description: string,
    required = false
): PromptCapabilityVariableDefinition => ({
    description,
    required,
    variableName
});

const PROMPT_CAPABILITY_VARIABLES: Record<string, PromptCapabilityVariableDefinition[]> = {
    classics_image_describe: [
        variable("title", "内容标题"),
        variable("contextText", "上下文文本"),
        variable("imageDescription", "图像识别或人工补充的画面描述")
    ],
    classics_image_generate: [
        variable("title", "内容标题"),
        variable("sourceText", "用于生成图像的来源文本"),
        variable("styleGuide", "图像风格与生成约束")
    ],
    classics_image_prompt_fusion: [
        variable("title", "内容标题"),
        variable("sourceText", "原始文本"),
        variable("translationText", "译文文本"),
        variable("imageAnalysis", "图像分析结果")
    ],
    classics_qa: [variable("title", "内容标题"), variable("document", "用于问答的文档内容", true)],
    classics_summary: [
        variable("contentType", "内容类型", true),
        variable("title", "内容标题"),
        variable("categoryPath", "内容所属分类路径"),
        variable("originalText", "原文内容"),
        variable("translationText", "译文内容"),
        variable("bodyText", "正文内容"),
        variable("existingSummary", "已有摘要")
    ],
    classics_tags: [
        variable("title", "内容标题"),
        variable("categoryPath", "内容所属分类路径"),
        variable("document", "用于生成标签的文档内容", true)
    ],
    classics_translate: [
        variable("contentType", "内容类型", true),
        variable("title", "内容标题"),
        variable("contextPath", "上下文路径"),
        variable("sourceText", "待翻译的源文本", true)
    ],
    classics_visual_describe: [
        variable("title", "内容标题"),
        variable("fusionText", "融合后的文本内容"),
        variable("styleGuide", "视觉描述风格要求")
    ],
    discovery_answer_generation: [
        variable("question", "用户问题", true),
        variable("sources", "检索召回的参考来源", true),
        variable("answerStyle", "回答风格要求")
    ],
    discovery_query_understanding: [
        variable("query", "用户检索查询", true),
        variable("availableScopes", "可用检索范围")
    ]
};

export const getPromptCapabilityVariables = (capability?: string | null) => {
    if (!capability) {
        return [];
    }
    return PROMPT_CAPABILITY_VARIABLES[capability] || [];
};

export const getPromptCapabilityVariableNames = (capability?: string | null) => {
    return getPromptCapabilityVariables(capability).map((item) => item.variableName);
};

export const findUnsupportedPromptVariableNames = (
    capability: string | null | undefined,
    variableNames: string[]
) => {
    const allowedNames = getPromptCapabilityVariableNames(capability);
    if (allowedNames.length === 0) {
        return [];
    }
    const allowedNameSet = new Set(allowedNames);
    return variableNames.filter((variableName) => !allowedNameSet.has(variableName));
};
