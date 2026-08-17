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
    listMaterialTree: async (query = {}) => {
        if (!query.parentId || query.parentId === "root") {
            return [
                {
                    contentType: "SANCAI_ENTRY",
                    id: "type:SANCAI_ENTRY",
                    leaf: false,
                    nodeType: "contentType",
                    parentId: "root",
                    title: "三才图会"
                }
            ];
        }
        if (query.parentId === "type:SANCAI_ENTRY") {
            return [
                {
                    categoryCode: "天文",
                    contentType: "SANCAI_ENTRY",
                    id: "type:SANCAI_ENTRY:category:%E5%A4%A9%E6%96%87",
                    leaf: false,
                    nodeType: "category",
                    parentId: "type:SANCAI_ENTRY",
                    title: "天文"
                }
            ];
        }
        return [
            {
                categoryCode: "天文",
                contentType: "SANCAI_ENTRY",
                id: `${query.parentId}:volume:%E5%8D%B7%E4%B8%80`,
                leaf: true,
                nodeType: "volume",
                parentId: query.parentId,
                title: "卷一",
                volumeCode: "卷一"
            }
        ];
    },
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
    previewPublication: async (command) => ({
        edges: [],
        issues: [],
        materialLockVersion: "4",
        materialRef: command.contentRef,
        nodes: [],
        previewToken: "preview-token",
        publishable: true
    }),
    publishMaterial: async (command) => ({
        contentRef: command.contentRef,
        createdEdgeCount: "0",
        createdNodeCount: "0",
        materialStatus: "PUBLISHED",
        reusedEdgeCount: "0",
        reusedNodeCount: "0",
        success: true
    }),
    previewBatchPublication: async (command) => ({
        materials: (command.contentRefs ?? []).map((contentRef) => ({
            contentRef,
            result: {
                edges: [],
                issues: [],
                materialLockVersion: "4",
                materialRef: contentRef,
                nodes: [],
                previewToken: `preview-${contentRef.contentRefId}`,
                publishable: true
            },
            success: true
        }))
    }),
    publishBatch: async (command) => ({
        materials: command.materials.map((material) => ({
            contentRef: material.contentRef,
            result: {
                contentRef: material.contentRef,
                createdEdgeCount: "0",
                createdNodeCount: "0",
                materialStatus: "PUBLISHED",
                reusedEdgeCount: "0",
                reusedNodeCount: "0",
                success: true
            },
            success: true
        }))
    }),
    previewWithdrawal: async () => ({}),
    withdrawMaterial: async (command) => ({
        contentRef: command.contentRef,
        contentType: command.contentRef.contentType,
        id: command.contentRef.contentRefId,
        lockVersion: command.materialLockVersion,
        status: "DRAFT",
        title: command.contentRef.contentRefId
    }),
    precheckDeletion: async () => ({ executable: true }),
    previewBatchWithdrawal: async () => graphBatchWithdrawalPreview,
    withdrawBatch: async () => graphBatchWithdrawalResult
};
