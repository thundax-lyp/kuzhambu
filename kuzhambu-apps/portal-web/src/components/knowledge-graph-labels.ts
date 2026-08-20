const NODE_TYPE_LABELS: Record<string, string> = {
    ANIMAL: "动物",
    BUILDING: "建筑",
    CELESTIAL_BODY: "天体",
    CONCEPT: "概念",
    DEITY: "神祇",
    DYNASTY: "朝代",
    EVENT: "事件",
    GROUP: "群体",
    MATERIAL: "材料",
    NATURAL_PHENOMENON: "自然现象",
    OBJECT: "器物",
    OFFICE: "职官",
    ORGANIZATION: "组织",
    PERSON: "人物",
    PLACE: "地点",
    PLANT: "植物",
    RITUAL: "礼仪",
    WORK: "典籍"
};

const RELATION_LABELS: Record<string, string> = {
    ANCESTOR_OF: "祖先/后裔",
    ASSOCIATED_WITH: "关联",
    AUTHORED: "著作",
    CAUSES: "导致",
    COMPILED: "编纂",
    DEPICTS: "描绘",
    DESCRIBES: "记述",
    HOLDS_OFFICE: "任职",
    LOCATED_IN: "位于",
    MADE_OF: "制成",
    MEMBER_OF: "隶属",
    OCCURS_AT: "发生于",
    PARENT_OF: "父母/子女",
    PARTICIPATED_IN: "参与",
    PART_OF: "组成",
    PRACTICES: "实践",
    RULES: "统治",
    SPOUSE_OF: "配偶",
    SUCCEEDS: "继任",
    USES: "使用",
    WORSHIPS: "崇祀"
};

export const readKnowledgeGraphNodeTypeLabel = (nodeType?: string | null) => {
    return nodeType ? NODE_TYPE_LABELS[nodeType] || nodeType : "对象";
};

export const readKnowledgeGraphRelationLabel = (relationType?: string | null) => {
    return relationType ? RELATION_LABELS[relationType] || relationType : "关联";
};
