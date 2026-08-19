import { ApiError, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphBatchExtractionResultRecord,
    GraphBatchPublicationPreviewRecord,
    GraphBatchPublicationResultRecord,
    GraphBatchWithdrawalPreviewRecord,
    GraphBatchWithdrawalResultRecord,
    GraphContentRefRecord,
    GraphContentType,
    GraphMaterialDetailRecord,
    GraphMaterialListRecord,
    GraphMaterialRecord,
    GraphMaterialStatus,
    GraphMaterialTreeNodeRecord,
    GraphPublicationConfirmationRecord,
    GraphPublicationPreviewRecord,
    GraphPublicationResultRecord,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "./graph-material-types";

const MATERIAL_PAGE_PATH = "/knowledge/graph/material/page";
const MATERIAL_TREE_PATH = "/knowledge/graph/material/tree/list";
const MATERIAL_GET_PATH = "/knowledge/graph/material/get";
const EXTRACTION_CREATE_PATH = "/knowledge/graph/material/extraction/create";
const EXTRACTION_RETRY_PATH = "/knowledge/graph/task/retry";
const CANDIDATE_MERGE_PATH = "/knowledge/graph/task/candidate/apply";
const BATCH_EXTRACTION_CREATE_PATH = "/knowledge/graph/task/batch/create";
const PUBLICATION_PREVIEW_PATH = "/knowledge/graph/publication/preview";
const PUBLICATION_PUBLISH_PATH = "/knowledge/graph/publication/publish";
const BATCH_PUBLICATION_PREVIEW_PATH = "/knowledge/graph/publication/batch/preview";
const BATCH_PUBLICATION_PUBLISH_PATH = "/knowledge/graph/publication/batch/publish";
const WITHDRAWAL_PREVIEW_PATH = "/knowledge/graph/publication/withdrawal/preview";
const WITHDRAWAL_WITHDRAW_PATH = "/knowledge/graph/publication/withdrawal/withdraw";
const BATCH_WITHDRAWAL_PREVIEW_PATH = "/knowledge/graph/publication/batch/withdrawal/preview";
const BATCH_WITHDRAWAL_WITHDRAW_PATH = "/knowledge/graph/publication/batch/withdrawal/withdraw";
const MATERIAL_NODE_CREATE_PATH = "/knowledge/graph/material/node/create";
const MATERIAL_NODE_UPDATE_PATH = "/knowledge/graph/material/node/update";
const MATERIAL_NODE_DELETE_PATH = "/knowledge/graph/material/node/delete";
const MATERIAL_NODE_MERGE_APPLY_PATH = "/knowledge/graph/material/node/merge/apply";
const MATERIAL_EDGE_CREATE_PATH = "/knowledge/graph/material/edge/create";
const MATERIAL_EDGE_UPDATE_PATH = "/knowledge/graph/material/edge/update";
const MATERIAL_EDGE_DELETE_PATH = "/knowledge/graph/material/edge/delete";

interface GraphContentRefCommand {
    contentRef: GraphContentRefRecord;
}

interface GraphExtractionCreateCommand extends GraphContentRefCommand {
    idempotencyKey: string;
}

export interface GraphExtractionRetryCommand {
    expectedExecutionStatus: "FAILED";
    taskId: string;
    taskLockVersion: string;
}

interface GraphCandidateApplyPayloadCommand extends GraphMaterialCandidateApplyCommand {
    expectedDisposition: "PENDING";
    expectedExecutionStatus: "SUCCEEDED";
    idempotencyKey: string;
}

interface GraphBatchExtractionCreateCommand {
    idempotencyKey: string;
    selection: {
        contentRefs?: GraphContentRefRecord[];
        volumeCode?: string;
    };
}

interface GraphBatchWithdrawalPreviewCommand {
    contentRefs: GraphContentRefRecord[];
}

interface GraphBatchWithdrawalPayloadCommand {
    idempotencyKey: string;
    materials: Array<{
        contentRef: GraphContentRefRecord;
        materialLockVersion: string;
    }>;
}

interface GraphPublicationPreviewCommand {
    contentRef: GraphContentRefRecord;
}

type GraphPublicationPublishCommand = GraphPublicationConfirmationRecord;

interface GraphBatchPublicationPreviewCommand {
    contentRefs: GraphContentRefRecord[];
}

interface GraphBatchPublicationPublishCommand {
    materials: GraphPublicationConfirmationRecord[];
}

interface GraphWithdrawalPreviewCommand {
    contentRef: GraphContentRefRecord;
}

interface GraphWithdrawalPayloadCommand {
    contentRef: GraphContentRefRecord;
    idempotencyKey: string;
    materialLockVersion: string;
}

export interface GraphMaterialPageQuery {
    categoryCode?: string;
    contentType?: GraphContentType;
    keyword?: string;
    pageNo?: number;
    pageSize?: number;
    status?: GraphMaterialStatus;
    taskDisposition?: GraphTaskDisposition;
    taskExecutionStatus?: GraphTaskExecutionStatus;
    volumeCode?: string;
}

export interface GraphMaterialContentRefCommand {
    contentRef: GraphContentRefRecord;
}

export interface GraphMaterialCandidateApplyCommand {
    applyMode: "MERGE" | "REPLACE";
    materialLockVersion: string;
    taskId: string;
    taskLockVersion: string;
}

export interface GraphMaterialTreeQuery {
    parentId?: string;
}

export interface GraphMaterialBatchExtractionCommand {
    contentRefs?: GraphContentRefRecord[];
    volumeCode?: string;
}

export interface GraphMaterialBatchWithdrawalCommand {
    materials: Array<{
        contentRef: GraphContentRefRecord;
        materialLockVersion: string;
    }>;
}

export type GraphMaterialPublicationConfirmationCommand = GraphPublicationConfirmationRecord;

export interface GraphMaterialWithdrawalCommand {
    contentRef: GraphContentRefRecord;
    materialLockVersion: string;
}

export interface GraphMaterialNodeCommand {
    contentRef: GraphContentRefRecord;
    materialLockVersion: string;
    node: {
        id?: string;
        name: string;
        nodeType: string;
        properties: Record<string, unknown>;
        source: "MANUAL" | "MATERIAL";
    };
}

export interface GraphMaterialEdgeCommand {
    contentRef: GraphContentRefRecord;
    edge: {
        id?: string;
        qualifiers: Record<string, unknown>;
        relationType: string;
        source: "MANUAL" | "MATERIAL";
        sourceNodeId: string;
        targetNodeId: string;
    };
    materialLockVersion: string;
}

export interface GraphMaterialObjectDeleteCommand {
    contentRef: GraphContentRefRecord;
    materialLockVersion: string;
    objectId: string;
}

export interface GraphMaterialNodeMergeCommand {
    contentRef: GraphContentRefRecord;
    materialLockVersion: string;
    mergedNodeIds: string[];
    retainedNodeId: string;
}

export interface GraphMaterialService {
    createBatchExtraction: (
        command: GraphMaterialBatchExtractionCommand
    ) => Promise<GraphBatchExtractionResultRecord>;
    createExtraction: (
        command: GraphMaterialContentRefCommand
    ) => Promise<GraphBatchExtractionResultRecord["materials"][number]["result"]>;
    retryExtraction: (
        command: GraphExtractionRetryCommand
    ) => Promise<GraphBatchExtractionResultRecord["materials"][number]["result"]>;
    getMaterial: (command: GraphMaterialContentRefCommand) => Promise<GraphMaterialDetailRecord>;
    createMaterialNode: (command: GraphMaterialNodeCommand) => Promise<GraphMaterialDetailRecord>;
    updateMaterialNode: (command: GraphMaterialNodeCommand) => Promise<GraphMaterialDetailRecord>;
    deleteMaterialNode: (
        command: GraphMaterialObjectDeleteCommand
    ) => Promise<GraphMaterialDetailRecord>;
    createMaterialEdge: (command: GraphMaterialEdgeCommand) => Promise<GraphMaterialDetailRecord>;
    updateMaterialEdge: (command: GraphMaterialEdgeCommand) => Promise<GraphMaterialDetailRecord>;
    deleteMaterialEdge: (
        command: GraphMaterialObjectDeleteCommand
    ) => Promise<GraphMaterialDetailRecord>;
    mergeMaterialNodes: (
        command: GraphMaterialNodeMergeCommand
    ) => Promise<GraphMaterialDetailRecord>;
    listMaterialTree: (query?: GraphMaterialTreeQuery) => Promise<GraphMaterialTreeNodeRecord[]>;
    applyCandidate: (command: GraphMaterialCandidateApplyCommand) => Promise<unknown>;
    pageMaterials: (query?: GraphMaterialPageQuery) => Promise<Page<GraphMaterialListRecord>>;
    previewBatchPublication: (
        command: Pick<GraphMaterialBatchExtractionCommand, "contentRefs">
    ) => Promise<GraphBatchPublicationPreviewRecord>;
    previewBatchWithdrawal: (
        command: Pick<GraphMaterialBatchExtractionCommand, "contentRefs">
    ) => Promise<GraphBatchWithdrawalPreviewRecord>;
    previewPublication: (
        command: GraphMaterialContentRefCommand
    ) => Promise<GraphPublicationPreviewRecord>;
    previewWithdrawal: (command: GraphMaterialContentRefCommand) => Promise<unknown>;
    publishBatch: (command: {
        materials: GraphMaterialPublicationConfirmationCommand[];
    }) => Promise<GraphBatchPublicationResultRecord>;
    publishMaterial: (
        command: GraphMaterialPublicationConfirmationCommand
    ) => Promise<GraphPublicationResultRecord>;
    withdrawMaterial: (command: GraphMaterialWithdrawalCommand) => Promise<GraphMaterialRecord>;
    withdrawBatch: (
        command: GraphMaterialBatchWithdrawalCommand
    ) => Promise<GraphBatchWithdrawalResultRecord>;
}

const createIdempotencyKey = () => {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `idem-${Date.now()}-${Math.random().toString(36).slice(2)}`;
};

const toPage = <TRecord>(page: {
    count: number;
    pageNo: number;
    pageSize: number;
    records: TRecord[];
    totalPage: number;
}): Page<TRecord> => ({
    count: page.count,
    pageNo: page.pageNo,
    pageSize: page.pageSize,
    records: page.records,
    totalCount: page.count,
    totalPage: page.totalPage
});

const assertBatchExtractionSelection = (command: GraphMaterialBatchExtractionCommand) => {
    const hasContentRefs = Boolean(command.contentRefs?.length);
    const hasVolumeCode = Boolean(command.volumeCode);
    if (hasContentRefs === hasVolumeCode) {
        throw new ApiError("GRAPH_TASK_SELECTION_INVALID", "批量提取必须且只能选择素材或整卷");
    }
};

export const httpGraphMaterialService: GraphMaterialService = {
    pageMaterials: async (query = {}) => {
        const page = await postJson<
            {
                count: number;
                pageNo: number;
                pageSize: number;
                records: GraphMaterialListRecord[];
                totalPage: number;
            },
            GraphMaterialPageQuery
        >(MATERIAL_PAGE_PATH, {
            body: query
        });
        return toPage(page);
    },
    listMaterialTree: (query = {}) =>
        postJson<GraphMaterialTreeNodeRecord[], GraphMaterialTreeQuery>(MATERIAL_TREE_PATH, {
            body: query
        }),
    getMaterial: (command) =>
        postJson<GraphMaterialDetailRecord, GraphContentRefCommand>(MATERIAL_GET_PATH, {
            body: command
        }),
    createMaterialNode: (command) =>
        postJson<GraphMaterialDetailRecord, GraphMaterialNodeCommand>(MATERIAL_NODE_CREATE_PATH, {
            body: command
        }),
    updateMaterialNode: (command) =>
        postJson<GraphMaterialDetailRecord, GraphMaterialNodeCommand>(MATERIAL_NODE_UPDATE_PATH, {
            body: command
        }),
    deleteMaterialNode: ({ contentRef, materialLockVersion, objectId }) =>
        postJson<
            GraphMaterialDetailRecord,
            { contentRef: GraphContentRefRecord; materialLockVersion: string; nodeId: string }
        >(MATERIAL_NODE_DELETE_PATH, {
            body: { contentRef, materialLockVersion, nodeId: objectId }
        }),
    createMaterialEdge: (command) =>
        postJson<GraphMaterialDetailRecord, GraphMaterialEdgeCommand>(MATERIAL_EDGE_CREATE_PATH, {
            body: command
        }),
    updateMaterialEdge: (command) =>
        postJson<GraphMaterialDetailRecord, GraphMaterialEdgeCommand>(MATERIAL_EDGE_UPDATE_PATH, {
            body: command
        }),
    deleteMaterialEdge: ({ contentRef, materialLockVersion, objectId }) =>
        postJson<
            GraphMaterialDetailRecord,
            { contentRef: GraphContentRefRecord; materialLockVersion: string; edgeId: string }
        >(MATERIAL_EDGE_DELETE_PATH, {
            body: { contentRef, materialLockVersion, edgeId: objectId }
        }),
    mergeMaterialNodes: (command) =>
        postJson<GraphMaterialDetailRecord, GraphMaterialNodeMergeCommand>(
            MATERIAL_NODE_MERGE_APPLY_PATH,
            { body: command }
        ),
    createExtraction: (command) =>
        postJson<
            GraphBatchExtractionResultRecord["materials"][number]["result"],
            GraphExtractionCreateCommand
        >(EXTRACTION_CREATE_PATH, {
            body: {
                ...command,
                idempotencyKey: createIdempotencyKey()
            }
        }),
    retryExtraction: (command) =>
        postJson<
            GraphBatchExtractionResultRecord["materials"][number]["result"],
            GraphExtractionRetryCommand & { idempotencyKey: string }
        >(EXTRACTION_RETRY_PATH, {
            body: { ...command, idempotencyKey: createIdempotencyKey() }
        }),
    applyCandidate: (command) =>
        postJson<unknown, GraphCandidateApplyPayloadCommand>(CANDIDATE_MERGE_PATH, {
            body: {
                ...command,
                expectedDisposition: "PENDING",
                expectedExecutionStatus: "SUCCEEDED",
                idempotencyKey: createIdempotencyKey()
            }
        }),
    createBatchExtraction: (command) => {
        assertBatchExtractionSelection(command);
        return postJson<GraphBatchExtractionResultRecord, GraphBatchExtractionCreateCommand>(
            BATCH_EXTRACTION_CREATE_PATH,
            {
                body: {
                    idempotencyKey: createIdempotencyKey(),
                    selection: {
                        contentRefs: command.contentRefs,
                        volumeCode: command.volumeCode
                    }
                }
            }
        );
    },
    previewPublication: (command) =>
        postJson<GraphPublicationPreviewRecord, GraphPublicationPreviewCommand>(
            PUBLICATION_PREVIEW_PATH,
            {
                body: command
            }
        ),
    publishMaterial: (command) =>
        postJson<GraphPublicationResultRecord, GraphPublicationPublishCommand>(
            PUBLICATION_PUBLISH_PATH,
            {
                body: command
            }
        ),
    previewBatchPublication: (command) =>
        postJson<GraphBatchPublicationPreviewRecord, GraphBatchPublicationPreviewCommand>(
            BATCH_PUBLICATION_PREVIEW_PATH,
            {
                body: {
                    contentRefs: command.contentRefs ?? []
                }
            }
        ),
    publishBatch: (command) =>
        postJson<GraphBatchPublicationResultRecord, GraphBatchPublicationPublishCommand>(
            BATCH_PUBLICATION_PUBLISH_PATH,
            {
                body: command
            }
        ),
    previewWithdrawal: (command) =>
        postJson<unknown, GraphWithdrawalPreviewCommand>(WITHDRAWAL_PREVIEW_PATH, {
            body: command
        }),
    withdrawMaterial: (command) =>
        postJson<GraphMaterialRecord, GraphWithdrawalPayloadCommand>(WITHDRAWAL_WITHDRAW_PATH, {
            body: {
                ...command,
                idempotencyKey: createIdempotencyKey()
            }
        }),
    previewBatchWithdrawal: (command) =>
        postJson<GraphBatchWithdrawalPreviewRecord, GraphBatchWithdrawalPreviewCommand>(
            BATCH_WITHDRAWAL_PREVIEW_PATH,
            {
                body: {
                    contentRefs: command.contentRefs ?? []
                }
            }
        ),
    withdrawBatch: (command) =>
        postJson<GraphBatchWithdrawalResultRecord, GraphBatchWithdrawalPayloadCommand>(
            BATCH_WITHDRAWAL_WITHDRAW_PATH,
            {
                body: {
                    ...command,
                    idempotencyKey: createIdempotencyKey()
                }
            }
        )
};

export const pageMaterials = httpGraphMaterialService.pageMaterials;
export const listMaterialTree = httpGraphMaterialService.listMaterialTree;
export const getMaterial = httpGraphMaterialService.getMaterial;
export const createMaterialNode = httpGraphMaterialService.createMaterialNode;
export const updateMaterialNode = httpGraphMaterialService.updateMaterialNode;
export const deleteMaterialNode = httpGraphMaterialService.deleteMaterialNode;
export const createMaterialEdge = httpGraphMaterialService.createMaterialEdge;
export const updateMaterialEdge = httpGraphMaterialService.updateMaterialEdge;
export const deleteMaterialEdge = httpGraphMaterialService.deleteMaterialEdge;
export const mergeMaterialNodes = httpGraphMaterialService.mergeMaterialNodes;
export const createExtraction = httpGraphMaterialService.createExtraction;
export const retryExtraction = httpGraphMaterialService.retryExtraction;
export const createBatchExtraction = httpGraphMaterialService.createBatchExtraction;
export const applyCandidate = httpGraphMaterialService.applyCandidate;
export const previewPublication = httpGraphMaterialService.previewPublication;
export const publishMaterial = httpGraphMaterialService.publishMaterial;
export const previewBatchPublication = httpGraphMaterialService.previewBatchPublication;
export const publishBatch = httpGraphMaterialService.publishBatch;
export const previewWithdrawal = httpGraphMaterialService.previewWithdrawal;
export const withdrawMaterial = httpGraphMaterialService.withdrawMaterial;
export const previewBatchWithdrawal = httpGraphMaterialService.previewBatchWithdrawal;
export const withdrawBatch = httpGraphMaterialService.withdrawBatch;
