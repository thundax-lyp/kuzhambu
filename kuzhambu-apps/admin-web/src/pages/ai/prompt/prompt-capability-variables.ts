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
    CLASSICS_IMAGE_DESCRIBE: [
        variable("title", "内容标题"),
        variable("contextText", "上下文文本"),
        variable("imageDescription", "图像识别或人工补充的画面描述")
    ],
    CLASSICS_IMAGE_GENERATE: [
        variable("title", "内容标题"),
        variable("sourceText", "用于生成图像的来源文本"),
        variable("styleGuide", "图像风格与生成约束")
    ],
    CLASSICS_IMAGE_PROMPT_FUSION: [
        variable("title", "内容标题"),
        variable("sourceText", "原始文本"),
        variable("translationText", "译文文本"),
        variable("imageAnalysis", "图像分析结果")
    ],
    CLASSICS_QA: [variable("title", "内容标题"), variable("document", "用于问答的文档内容", true)],
    CLASSICS_SUMMARY: [
        variable("contentType", "内容类型", true),
        variable("title", "内容标题"),
        variable("categoryPath", "内容所属分类路径"),
        variable("originalText", "原文内容"),
        variable("translationText", "译文内容"),
        variable("bodyText", "正文内容"),
        variable("existingSummary", "已有摘要")
    ],
    CLASSICS_TAG_EXTRACT: [
        variable("title", "内容标题"),
        variable("categoryPath", "内容所属分类路径"),
        variable("document", "用于生成标签的文档内容", true)
    ],
    CLASSICS_TRANSLATE: [
        variable("contentType", "内容类型", true),
        variable("title", "内容标题"),
        variable("contextPath", "上下文路径"),
        variable("sourceText", "待翻译的源文本", true)
    ],
    CLASSICS_VISUAL_DESCRIBE: [
        variable("title", "内容标题"),
        variable("fusionText", "融合后的文本内容"),
        variable("styleGuide", "视觉描述风格要求")
    ],
    DISCOVERY_ANSWER_GENERATION: [
        variable("question", "用户问题", true),
        variable("sources", "检索召回的参考来源", true),
        variable("answerStyle", "回答风格要求")
    ],
    DISCOVERY_QUERY_UNDERSTANDING: [
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
