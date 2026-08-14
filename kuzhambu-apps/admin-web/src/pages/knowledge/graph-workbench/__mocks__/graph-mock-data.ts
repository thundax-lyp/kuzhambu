export const graphWorkbenchMockData = {
    metrics: {
        nodeCount: 200,
        relationCount: 356,
        coveredMaterialCount: 42,
        orphanNodeCount: 3,
        missingCoreRelationCount: 5
    },
    seedNodes: [
        { id: "node-li-bai", label: "李白", isFaded: true },
        { id: "node-du-fu", label: "杜甫", isFaded: true },
        { id: "node-orphan", label: "孤立节点", isFaded: true }
    ],
    edgeBatches: [
        {
            nodes: [{ id: "node-tang-poetry", label: "唐诗", isFaded: false }],
            edges: [
                { id: "edge-li-bai-tang-poetry", source: "node-li-bai", target: "node-tang-poetry" }
            ]
        },
        {
            nodes: [{ id: "node-shi-xian", label: "诗仙", isFaded: false }],
            edges: [{ id: "edge-li-bai-shi-xian", source: "node-li-bai", target: "node-shi-xian" }]
        }
    ]
} as const;
