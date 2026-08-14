export const graphGovernanceMockData = {
    nodes: [
        { id: "published-node-li-bai", name: "李白", type: "人物", sourceCount: 3 },
        { id: "published-node-tang-poetry", name: "唐诗", type: "作品分类", sourceCount: 5 }
    ],
    relations: [
        {
            id: "published-relation-li-bai-tang-poetry",
            sourceId: "published-node-li-bai",
            targetId: "published-node-tang-poetry",
            type: "创作"
        }
    ],
    failureMessage: "治理记录加载失败，请重试。"
} as const;
