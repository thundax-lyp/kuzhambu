import type { Page } from "@/types/page";
import type {
    GraphBatchExtractionResultRecord,
    GraphBatchWithdrawalPreviewRecord,
    GraphBatchWithdrawalResultRecord,
    GraphContentRefRecord,
    GraphMaterialDetailRecord,
    GraphMaterialListRecord,
    GraphMaterialRecord,
    GraphMaterialStatsRecord,
    GraphMaterialTaskSummaryRecord,
    GraphSourceRecord
} from "@/pages/knowledge/graph-material/graph-material-types";
import type { GraphExtractionTaskRecord } from "@/pages/knowledge/graph-extraction/graph-extraction-types";

const sanciaRef: GraphContentRefRecord = {
    contentRefId: "1001",
    contentType: "SANCAI_ENTRY"
};
const publishedRef: GraphContentRefRecord = {
    contentRefId: "1002",
    contentType: "SANCAI_ENTRY"
};
const failedRef: GraphContentRefRecord = {
    contentRefId: "1003",
    contentType: "WANGQI_DOCUMENT"
};
const runningRef: GraphContentRefRecord = {
    contentRefId: "1004",
    contentType: "MING_CUSTOMS"
};
const unavailableRef: GraphContentRefRecord = {
    contentRefId: "1005",
    contentType: "SANCAI_ENTRY"
};

export const graphMaterialMockSources: GraphSourceRecord[] = [
    {
        category: "天文",
        contentRef: sanciaRef,
        contentType: "SANCAI_ENTRY",
        summary: "三才图会天文类条目",
        title: "三才图会 天文一",
        volume: "卷一"
    },
    {
        category: "人物",
        contentRef: publishedRef,
        contentType: "SANCAI_ENTRY",
        summary: "已发布的人物类素材",
        title: "三才图会 人物一",
        volume: "卷二"
    },
    {
        category: "方志",
        contentRef: failedRef,
        contentType: "WANGQI_DOCUMENT",
        summary: "抽取失败的王祺文档",
        title: "王祺札记 山川",
        volume: "册一"
    },
    {
        category: "风俗",
        contentRef: runningRef,
        contentType: "MING_CUSTOMS",
        summary: "运行中的明代风俗素材",
        title: "明代风俗 婚礼",
        volume: "礼俗"
    },
    {
        category: "不可见",
        contentRef: unavailableRef,
        contentType: "SANCAI_ENTRY",
        summary: "来源不可见素材",
        title: "三才图会 隐藏稿",
        volume: "卷三"
    }
];

export const graphMaterialMockMaterials: GraphMaterialRecord[] = [
    {
        category: "人物",
        contentRef: publishedRef,
        contentType: "SANCAI_ENTRY",
        id: "2002",
        lockVersion: "4",
        publishedAt: "1723852820000",
        status: "PUBLISHED",
        title: "三才图会 人物一",
        volume: "卷二"
    },
    {
        category: "方志",
        contentRef: failedRef,
        contentType: "WANGQI_DOCUMENT",
        failedOperation: "PUBLISH",
        failureReason: "发布预览存在未解决冲突。",
        id: "2003",
        lockVersion: "6",
        status: "FAILED",
        title: "王祺札记 山川",
        volume: "册一"
    },
    {
        category: "风俗",
        contentRef: runningRef,
        contentType: "MING_CUSTOMS",
        id: "2004",
        lockVersion: "2",
        status: "READY",
        title: "明代风俗 婚礼",
        volume: "礼俗"
    }
];

export const graphMaterialMockStats: Record<string, GraphMaterialStatsRecord> = {
    "1002": {
        activeTaskCount: "0",
        calculatedAt: "1723852820000",
        draftEdgeCount: "98",
        draftNodeCount: "64",
        failedTaskCount: "0",
        pendingReviewTaskCount: "0",
        publicationContributionCount: "162",
        publishedEdgeCount: "98",
        publishedNodeCount: "64",
        statsRevision: "4"
    },
    "1003": {
        activeTaskCount: "0",
        calculatedAt: "1723852810000",
        draftEdgeCount: "18",
        draftNodeCount: "12",
        failedTaskCount: "1",
        pendingReviewTaskCount: "0",
        publicationContributionCount: "0",
        publishedEdgeCount: "0",
        publishedNodeCount: "0",
        statsRevision: "5"
    },
    "1004": {
        activeTaskCount: "1",
        calculatedAt: "1723852800000",
        draftEdgeCount: "4",
        draftNodeCount: "3",
        failedTaskCount: "0",
        pendingReviewTaskCount: "0",
        publicationContributionCount: "0",
        publishedEdgeCount: "0",
        publishedNodeCount: "0",
        statsRevision: "1"
    }
};

export const graphExtractionMockTasks: GraphExtractionTaskRecord[] = [
    {
        attemptNo: "1",
        batchId: null,
        candidateId: "8001",
        completedAt: "1723852810000",
        currentStage: "CANDIDATE_READY",
        disposition: "PENDING",
        executionStatus: "SUCCEEDED",
        id: "7001",
        lockVersion: "5",
        materialRef: sanciaRef,
        progress: 100,
        purgeAfter: null,
        requestedAt: "1723852800000",
        resultSummary: { edgeCount: 18, nodeCount: 12, warningCount: 1 }
    },
    {
        attemptNo: "2",
        batchId: "batch-001",
        completedAt: null,
        currentStage: "EXTRACTING_RELATIONS",
        disposition: null,
        executionStatus: "RUNNING",
        id: "7002",
        lockVersion: "2",
        materialRef: runningRef,
        progress: 48,
        purgeAfter: null,
        requestedAt: "1723852830000"
    },
    {
        attemptNo: "1",
        batchId: "batch-001",
        completedAt: "1723852840000",
        currentStage: "VALIDATE",
        disposition: null,
        executionStatus: "FAILED",
        failureReason: "候选实体名称缺少身份限定。",
        id: "7003",
        lockVersion: "3",
        materialRef: failedRef,
        progress: 74,
        purgeAfter: null,
        requestedAt: "1723852830000"
    },
    {
        attemptNo: "1",
        batchId: null,
        candidateId: "8004",
        completedAt: "1723852790000",
        currentStage: "DISPOSED",
        disposedAt: "1723852795000",
        disposition: "ADOPTED_MERGE",
        executionStatus: "SUCCEEDED",
        id: "7004",
        lockVersion: "6",
        materialRef: publishedRef,
        progress: 100,
        purgeAfter: "1724457600000",
        requestedAt: "1723852780000",
        resultSummary: { edgeCount: 8, nodeCount: 6, warningCount: 0 }
    },
    {
        attemptNo: "1",
        batchId: null,
        candidateId: "8005",
        completedAt: "1723852790000",
        currentStage: "CANDIDATE_READY",
        disposition: "PENDING",
        executionStatus: "SUCCEEDED",
        id: "7005",
        lockVersion: "1",
        materialRef: unavailableRef,
        progress: 100,
        purgeAfter: null,
        requestedAt: "1723852780000",
        resultSummary: { edgeCount: 0, nodeCount: 0, warningCount: 1 }
    }
];

const findMaterial = (contentRef: GraphContentRefRecord) =>
    graphMaterialMockMaterials.find(
        (material) =>
            material.contentRef.contentType === contentRef.contentType &&
            material.contentRef.contentRefId === contentRef.contentRefId
    );

const toMaterialTaskSummary = (
    task?: GraphExtractionTaskRecord | null
): GraphMaterialTaskSummaryRecord | null => {
    if (
        !task?.id ||
        !task.materialRef ||
        !task.lockVersion ||
        !task.executionStatus ||
        !task.attemptNo ||
        task.progress === undefined ||
        !task.currentStage
    ) {
        return null;
    }
    return {
        attemptNo: task.attemptNo,
        batchId: task.batchId,
        completedAt:
            task.completedAt === undefined || task.completedAt === null
                ? task.completedAt
                : String(task.completedAt),
        currentStage: task.currentStage,
        disposition: task.disposition ?? null,
        executionStatus: task.executionStatus,
        failureReason: task.failureReason,
        id: task.id,
        lockVersion: task.lockVersion,
        materialRef: task.materialRef,
        progress: task.progress,
        purgeAfter: task.purgeAfter,
        requestedAt:
            task.requestedAt === undefined || task.requestedAt === null
                ? task.requestedAt
                : String(task.requestedAt)
    };
};

export const graphMaterialMockListRecords: GraphMaterialListRecord[] = graphMaterialMockSources.map(
    (source) => {
        const material = findMaterial(source.contentRef) ?? null;
        return {
            latestTask:
                toMaterialTaskSummary(
                    graphExtractionMockTasks.find(
                        (task) =>
                            task.materialRef?.contentType === source.contentRef.contentType &&
                            task.materialRef?.contentRefId === source.contentRef.contentRefId
                    )
                ) ?? null,
            material,
            materialStats: graphMaterialMockStats[source.contentRef.contentRefId] ?? null,
            source
        };
    }
);

export const graphMaterialMockDetails: GraphMaterialDetailRecord[] =
    graphMaterialMockListRecords.map((record) => ({
        edges: [
            {
                id: `edge-${record.source.contentRef.contentRefId}`,
                qualifiers: { evidence: "正文段落" },
                relationType: "MENTIONS",
                source: "AI",
                sourceNodeId: `node-${record.source.contentRef.contentRefId}-1`,
                targetNodeId: `node-${record.source.contentRef.contentRefId}-2`
            }
        ],
        material: record.material,
        materialStats: record.materialStats,
        latestTaskCandidate: record.latestTask
            ? {
                  candidateId: `candidate-${record.latestTask.id}`,
                  resultFormat: "GRAPH_DOCUMENT_V1",
                  resultSummaryJson: JSON.stringify({
                      edges: [
                          {
                              id: "edge-1",
                              relationType: "MENTIONS",
                              sourceId: "node-1",
                              targetId: "node-2"
                          }
                      ],
                      nodes: [
                          { id: "node-1", name: record.source.title },
                          { id: "node-2", name: record.source.category ?? "未知分类" }
                      ]
                  })
              }
            : null,
        nodes: [
            {
                id: `node-${record.source.contentRef.contentRefId}-1`,
                name: record.source.title,
                nodeType: "WORK",
                properties: {},
                source: "AI"
            },
            {
                id: `node-${record.source.contentRef.contentRefId}-2`,
                name: record.source.category ?? "未知分类",
                nodeType: "CATEGORY",
                properties: {},
                source: "AI"
            }
        ],
        source: record.source,
        taskSummary: {
            activeTaskCount: record.materialStats?.activeTaskCount ?? "0",
            failedTaskCount: record.materialStats?.failedTaskCount ?? "0",
            latestTask: record.latestTask,
            pendingReviewTaskCount: record.materialStats?.pendingReviewTaskCount ?? "0",
            totalTaskCount: record.latestTask ? "1" : "0"
        }
    }));

export const graphBatchExtractionResult: GraphBatchExtractionResultRecord = {
    batchId: "batch-001",
    materials: [
        {
            contentRef: sanciaRef,
            result: toMaterialTaskSummary(graphExtractionMockTasks[0]) ?? undefined,
            success: true
        },
        {
            contentRef: failedRef,
            failureCode: "GRAPH_TASK_ACTIVE_EXISTS",
            failureMessage: "素材已有活动任务。",
            success: false
        }
    ]
};

export const graphBatchWithdrawalPreview: GraphBatchWithdrawalPreviewRecord = {
    materials: [
        {
            contentRef: publishedRef,
            result: {
                edgeMappingCount: "4",
                governedEdges: "1",
                governedNodes: "2",
                materialRef: publishedRef,
                nodeMappingCount: "6"
            },
            success: true
        }
    ]
};

export const graphBatchWithdrawalResult: GraphBatchWithdrawalResultRecord = {
    batchId: "withdraw-batch-001",
    materials: [
        {
            contentRef: publishedRef,
            result: {
                ...graphMaterialMockMaterials[0],
                status: "READY"
            },
            success: true
        }
    ]
};

export const toMockPage = <TRecord>(
    records: TRecord[],
    pageNo = 1,
    pageSize = 20
): Page<TRecord> => ({
    count: records.length,
    pageNo,
    pageSize,
    records,
    totalCount: records.length,
    totalPage: Math.max(1, Math.ceil(records.length / pageSize))
});

export const graphMaterialMockData = {
    batchPublicationResults: [
        { materialId: "2004", status: "PUBLISHED" },
        {
            failureReason: "发布预览存在未解决冲突。",
            materialId: "2003",
            status: "FAILED"
        }
    ],
    materials: graphMaterialMockMaterials,
    publicationPreview: [
        { color: "green", id: "preview-create", status: "CREATE" },
        { color: "orange", id: "preview-link", status: "LINK" },
        { color: "red", id: "preview-conflict", status: "CONFLICT" },
        { color: "blue", id: "preview-published", status: "PUBLISHED" }
    ]
} as const;
