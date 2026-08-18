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
    GraphDeletionPrecheckRecord,
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
const DELETION_PRECHECK_PATH = "/knowledge/graph/deletion-change/precheck";

interface GraphContentRefCommand {
    contentRef: GraphContentRefRecord;
}

interface GraphExtractionCreateCommand extends GraphContentRefCommand {
    idempotencyKey: string;
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

interface GraphDeletionPrecheckCommand {
    contentRef: GraphContentRefRecord;
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

export interface GraphMaterialService {
    createBatchExtraction: (
        command: GraphMaterialBatchExtractionCommand
    ) => Promise<GraphBatchExtractionResultRecord>;
    createExtraction: (
        command: GraphMaterialContentRefCommand
    ) => Promise<GraphBatchExtractionResultRecord["materials"][number]["result"]>;
    getMaterial: (command: GraphMaterialContentRefCommand) => Promise<GraphMaterialDetailRecord>;
    listMaterialTree: (query?: GraphMaterialTreeQuery) => Promise<GraphMaterialTreeNodeRecord[]>;
    applyCandidate: (command: GraphMaterialCandidateApplyCommand) => Promise<unknown>;
    pageMaterials: (query?: GraphMaterialPageQuery) => Promise<Page<GraphMaterialListRecord>>;
    precheckDeletion: (
        command: GraphMaterialContentRefCommand
    ) => Promise<GraphDeletionPrecheckRecord>;
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
    pageNo: string;
    pageSize: string;
    records: TRecord[];
    totalCount: string;
    totalPage: string;
}): Page<TRecord> => ({
    count: Number(page.totalCount),
    pageNo: Number(page.pageNo),
    pageSize: Number(page.pageSize),
    records: page.records,
    totalCount: Number(page.totalCount),
    totalPage: Number(page.totalPage)
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
                pageNo: string;
                pageSize: string;
                records: GraphMaterialListRecord[];
                totalCount: string;
                totalPage: string;
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
    precheckDeletion: (command) =>
        postJson<GraphDeletionPrecheckRecord, GraphDeletionPrecheckCommand>(
            DELETION_PRECHECK_PATH,
            {
                body: command
            }
        ),
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
export const createExtraction = httpGraphMaterialService.createExtraction;
export const createBatchExtraction = httpGraphMaterialService.createBatchExtraction;
export const applyCandidate = httpGraphMaterialService.applyCandidate;
export const previewPublication = httpGraphMaterialService.previewPublication;
export const publishMaterial = httpGraphMaterialService.publishMaterial;
export const previewBatchPublication = httpGraphMaterialService.previewBatchPublication;
export const publishBatch = httpGraphMaterialService.publishBatch;
export const previewWithdrawal = httpGraphMaterialService.previewWithdrawal;
export const withdrawMaterial = httpGraphMaterialService.withdrawMaterial;
export const precheckDeletion = httpGraphMaterialService.precheckDeletion;
export const previewBatchWithdrawal = httpGraphMaterialService.previewBatchWithdrawal;
export const withdrawBatch = httpGraphMaterialService.withdrawBatch;
