import { ApiError } from "@/api/http";
import type { GraphMaterialService } from "@/pages/knowledge/graph-material/graph-material-service";
import {
    graphBatchExtractionResult,
    graphBatchWithdrawalPreview,
    graphBatchWithdrawalResult,
    graphMaterialMockDetails,
    graphMaterialMockListRecords,
    toMockPage
} from "./graph-mock-data";

export const mockGraphMaterialService: GraphMaterialService = {
    pageMaterials: async (query = {}) => {
        const keyword = query.keyword?.trim();
        const records = keyword
            ? graphMaterialMockListRecords.filter((record) => record.source.title.includes(keyword))
            : graphMaterialMockListRecords;
        return toMockPage(records, query.pageNo, query.pageSize);
    },
    getMaterial: async (command) => {
        const detail = graphMaterialMockDetails.find(
            (record) =>
                record.source.contentRef.contentType === command.contentRef.contentType &&
                record.source.contentRef.contentRefId === command.contentRef.contentRefId
        );
        if (!detail) {
            throw new ApiError("GRAPH_MATERIAL_NOT_FOUND", "素材不存在或不可见");
        }
        return detail;
    },
    createExtraction: async (command) => {
        const result = graphBatchExtractionResult.materials.find(
            (material) =>
                material.contentRef.contentType === command.contentRef.contentType &&
                material.contentRef.contentRefId === command.contentRef.contentRefId
        );
        if (result && !result.success) {
            throw new ApiError(
                result.failureCode ?? "GRAPH_TASK_ACTIVE_EXISTS",
                result.failureMessage ?? "创建失败"
            );
        }
        return result?.result ?? graphBatchExtractionResult.materials[0].result;
    },
    createBatchExtraction: async () => graphBatchExtractionResult,
    previewBatchWithdrawal: async () => graphBatchWithdrawalPreview,
    withdrawBatch: async () => graphBatchWithdrawalResult
};
