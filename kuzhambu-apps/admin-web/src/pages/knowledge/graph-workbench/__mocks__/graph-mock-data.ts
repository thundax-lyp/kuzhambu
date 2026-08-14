export const graphWorkbenchMockData = {
    metrics: {
        nodeCount: 200,
        relationCount: 356,
        coveredMaterialCount: 42,
        orphanNodeCount: 3,
        missingCoreRelationCount: 5
    },
    seedNodes: [
        {
            id: "node-li-bai",
            label: "李白",
            categoryCode: "person",
            isFaded: true,
            sourceName: "唐诗素材-001",
            qualityTodo: "补充生平关系"
        },
        {
            id: "node-du-fu",
            label: "杜甫",
            categoryCode: "person",
            isFaded: true,
            sourceName: "唐诗素材-002",
            qualityTodo: "确认人物别名"
        },
        {
            id: "node-orphan",
            label: "孤立节点",
            categoryCode: "person",
            isFaded: true,
            isOrphan: true,
            sourceName: "唐诗素材-003",
            qualityTodo: "补充核心关系"
        }
    ],
    edgeBatches: [
        {
            nodes: [
                { id: "node-tang-poetry", label: "唐诗", categoryCode: "work", isFaded: false }
            ],
            edges: [
                {
                    id: "edge-li-bai-tang-poetry",
                    source: "node-li-bai",
                    target: "node-tang-poetry",
                    predicate: "创作"
                }
            ]
        },
        {
            nodes: [{ id: "node-shi-xian", label: "诗仙", categoryCode: "person", isFaded: false }],
            edges: [
                {
                    id: "edge-li-bai-shi-xian",
                    source: "node-li-bai",
                    target: "node-shi-xian",
                    predicate: "称号"
                }
            ]
        }
    ]
} as const;
