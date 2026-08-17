import { ApiError, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    GraphBatchExtractionResultRecord,
    GraphBatchWithdrawalPreviewRecord,
    GraphBatchWithdrawalResultRecord,
    GraphContentRefRecord,
    GraphContentType,
    GraphMaterialDetailRecord,
    GraphMaterialListRecord,
    GraphMaterialRecord,
    GraphMaterialStatus,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "./graph-material-types";

const MATERIAL_PAGE_PATH = "/knowledge/graph/material/page";
const MATERIAL_GET_PATH = "/knowledge/graph/material/get";
const EXTRACTION_CREATE_PATH = "/knowledge/graph/material/extraction/create";
const BATCH_EXTRACTION_CREATE_PATH = "/knowledge/graph/task/batch/create";
const BATCH_WITHDRAWAL_PREVIEW_PATH = "/knowledge/graph/publication/batch/withdrawal/preview";
const BATCH_WITHDRAWAL_WITHDRAW_PATH = "/knowledge/graph/publication/batch/withdrawal/withdraw";

interface GraphApiPage<TRecord> {
    pageNo: string;
    pageSize: string;
    totalCount: string;
    totalPage: string;
    records: TRecord[];
}

interface GraphMaterialPageRequest {
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

interface GraphContentRefRequest {
    contentRef: GraphContentRefRecord;
}

interface GraphExtractionCreateRequest extends GraphContentRefRequest {
    idempotencyKey: string;
}

interface GraphBatchExtractionCreateRequest {
    idempotencyKey: string;
    selection: {
        contentRefs?: GraphContentRefRecord[];
        volumeCode?: string;
    };
}

interface GraphBatchWithdrawalPreviewRequest {
    contentRefs: GraphContentRefRecord[];
}

interface GraphBatchWithdrawalRequest {
    idempotencyKey: string;
    materials: Array<{
        contentRef: GraphContentRefRecord;
        materialLockVersion: string;
    }>;
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

export interface GraphMaterialService {
    createBatchExtraction: (
        command: GraphMaterialBatchExtractionCommand
    ) => Promise<GraphBatchExtractionResultRecord>;
    createExtraction: (
        command: GraphMaterialContentRefCommand
    ) => Promise<GraphBatchExtractionResultRecord["materials"][number]["result"]>;
    getMaterial: (command: GraphMaterialContentRefCommand) => Promise<GraphMaterialDetailRecord>;
    pageMaterials: (query?: GraphMaterialPageQuery) => Promise<Page<GraphMaterialListRecord>>;
    previewBatchWithdrawal: (
        command: Pick<GraphMaterialBatchExtractionCommand, "contentRefs">
    ) => Promise<GraphBatchWithdrawalPreviewRecord>;
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

const toPage = <TRecord>(page: GraphApiPage<TRecord>): Page<TRecord> => ({
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
            GraphApiPage<GraphMaterialListRecord>,
            GraphMaterialPageRequest
        >(MATERIAL_PAGE_PATH, {
            body: query
        });
        return toPage(page);
    },
    getMaterial: (command) =>
        postJson<GraphMaterialDetailRecord, GraphContentRefRequest>(MATERIAL_GET_PATH, {
            body: command
        }),
    createExtraction: (command) =>
        postJson<
            GraphBatchExtractionResultRecord["materials"][number]["result"],
            GraphExtractionCreateRequest
        >(EXTRACTION_CREATE_PATH, {
            body: {
                ...command,
                idempotencyKey: createIdempotencyKey()
            }
        }),
    createBatchExtraction: (command) => {
        assertBatchExtractionSelection(command);
        return postJson<GraphBatchExtractionResultRecord, GraphBatchExtractionCreateRequest>(
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
    previewBatchWithdrawal: (command) =>
        postJson<GraphBatchWithdrawalPreviewRecord, GraphBatchWithdrawalPreviewRequest>(
            BATCH_WITHDRAWAL_PREVIEW_PATH,
            {
                body: {
                    contentRefs: command.contentRefs ?? []
                }
            }
        ),
    withdrawBatch: (command) =>
        postJson<GraphBatchWithdrawalResultRecord, GraphBatchWithdrawalRequest>(
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
export const getMaterial = httpGraphMaterialService.getMaterial;
export const createExtraction = httpGraphMaterialService.createExtraction;
export const createBatchExtraction = httpGraphMaterialService.createBatchExtraction;
export const previewBatchWithdrawal = httpGraphMaterialService.previewBatchWithdrawal;
export const withdrawBatch = httpGraphMaterialService.withdrawBatch;
