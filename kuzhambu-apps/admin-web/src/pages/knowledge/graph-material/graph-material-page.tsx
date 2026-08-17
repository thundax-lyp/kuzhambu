import { useMutation, useQuery } from "@tanstack/react-query";
import type { Key } from "react";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuPage,
    KuzhambuSpace
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { graphMaterialMockData } from "./__mocks__/graph-mock-data";
import { MaterialBatchActions } from "./material-batch-actions";
import { MaterialFilters } from "./material-filters";
import { MaterialTable } from "./material-table";
import { MaterialDraftCanvas } from "./material-draft-canvas";
import { MaterialObjectDrawer } from "./material-object-drawer";
import * as service from "./graph-material-service";
import type { GraphMaterialPageQuery } from "./graph-material-service";
import type {
    GraphContentRefRecord,
    GraphMaterialListRecord,
    GraphMaterialRecord
} from "./graph-material-types";
import "./graph-material-page.css";

const toMaterialRowKey = (record: GraphMaterialListRecord) =>
    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`;

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

export const GraphMaterialPage = () => {
    const navigate = useNavigate();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const [selectedMaterialKeys, setSelectedMaterialKeys] = useState<Key[]>([]);
    const [isBatchPublishing, setIsBatchPublishing] = useState(false);
    const [activeMaterial, setActiveMaterial] = useState<GraphMaterialRecord | null>(null);
    const [activeObjectId, setActiveObjectId] = useState<string | null>(null);
    const [query, setQuery] = useState<GraphMaterialPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const materialPageQuery = useQuery({
        enabled: canViewGraph,
        queryFn: () => service.pageMaterials(query),
        queryKey: ["knowledge", "graph-material", "page", query]
    });
    const pageResult = materialPageQuery.data;
    const records = pageResult?.records ?? [];
    const selectedRecords = useMemo(() => {
        const selectedKeys = new Set(selectedMaterialKeys.map(String));
        return records.filter((record) => selectedKeys.has(toMaterialRowKey(record)));
    }, [records, selectedMaterialKeys]);
    const totalCount = pageResult?.totalCount ?? pageResult?.count ?? 0;
    const isInitialError = materialPageQuery.isError && records.length === 0;
    const updateQuery = (nextQuery: GraphMaterialPageQuery) => {
        setSelectedMaterialKeys([]);
        setQuery(nextQuery);
    };
    const batchExtractionMutation = useMutation({
        mutationFn: service.createBatchExtraction,
        onSuccess: () => {
            void materialPageQuery.refetch();
        }
    });
    const batchWithdrawalMutation = useMutation({
        mutationFn: async (targetRecords: GraphMaterialListRecord[]) => {
            const materialsToWithdraw = targetRecords.flatMap((record) =>
                record.material?.lockVersion
                    ? [
                          {
                              contentRef: record.source.contentRef,
                              materialLockVersion: record.material.lockVersion
                          }
                      ]
                    : []
            );
            if (materialsToWithdraw.length === 0) {
                return { materials: [] };
            }
            await service.previewBatchWithdrawal({
                contentRefs: materialsToWithdraw.map((material) => material.contentRef)
            });
            return service.withdrawBatch({ materials: materialsToWithdraw });
        },
        onSuccess: () => {
            void materialPageQuery.refetch();
        }
    });
    const publishSelectedMaterials = async (targetRecords: GraphMaterialListRecord[]) => {
        setIsBatchPublishing(true);
        try {
            const selectedMaterialIds = new Set(
                targetRecords
                    .map((record) => record.material?.id)
                    .filter((id): id is string => Boolean(id))
            );
            return graphMaterialMockData.batchPublicationResults.filter((result) =>
                selectedMaterialIds.has(result.materialId)
            );
        } finally {
            setIsBatchPublishing(false);
        }
    };
    const viewSelectedTasks = (contentRefs: GraphContentRefRecord[]) => {
        const params = new URLSearchParams();
        params.set("contentRefs", JSON.stringify(contentRefs));
        navigate(`/knowledge/graph-extraction?${params.toString()}`);
    };

    if (!canViewGraph) {
        return (
            <KuzhambuPage
                className="graph-material-page"
                description="需要知识图谱查看权限。"
                title="图谱素材库"
            >
                <KuzhambuAlert title="无权查看图谱素材库" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage
            className="graph-material-page"
            description="查看素材抽取状态和图谱草稿入口。"
            title="图谱素材库"
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuCard title="素材筛选">
                    <MaterialFilters
                        loading={materialPageQuery.isFetching}
                        totalCount={totalCount}
                        value={query}
                        onChange={updateQuery}
                    />
                </KuzhambuCard>
                <MaterialBatchActions
                    canApplyGraph={canApplyGraph}
                    extracting={batchExtractionMutation.isPending}
                    publishing={isBatchPublishing}
                    selectedRecords={selectedRecords}
                    withdrawing={batchWithdrawalMutation.isPending}
                    onExtract={(contentRefs) =>
                        batchExtractionMutation.mutateAsync({ contentRefs })
                    }
                    onPublish={publishSelectedMaterials}
                    onViewTasks={viewSelectedTasks}
                    onWithdraw={(targetRecords) =>
                        batchWithdrawalMutation.mutateAsync(targetRecords)
                    }
                />
                {materialPageQuery.isError ? (
                    <KuzhambuAlert
                        action={
                            <KuzhambuButton
                                testId="knowledge-graph-material-retry-page-button"
                                size="small"
                                onClick={() => void materialPageQuery.refetch()}
                            >
                                重试加载素材列表
                            </KuzhambuButton>
                        }
                        description={getErrorMessage(materialPageQuery.error)}
                        title="素材列表加载失败"
                        type="error"
                        showIcon
                    />
                ) : null}
                {!isInitialError ? (
                    <KuzhambuCard title="素材列表">
                        <MaterialTable
                            canOpenMaterial={canViewGraph}
                            canViewTasks={canViewGraph}
                            dataSource={records}
                            loading={materialPageQuery.isLoading}
                            onOpenMaterial={setActiveMaterial}
                            onSelectionChange={(keys) => setSelectedMaterialKeys(keys)}
                            onViewTasks={navigate}
                            selectedRowKeys={selectedMaterialKeys}
                        />
                    </KuzhambuCard>
                ) : null}
                {activeMaterial ? (
                    <MaterialDraftCanvas
                        canApplyGraph={canApplyGraph}
                        canEditGraph={canEditGraph}
                        material={activeMaterial}
                        onClose={() => {
                            setActiveMaterial(null);
                            setActiveObjectId(null);
                        }}
                        onOpenObject={setActiveObjectId}
                    />
                ) : null}
            </KuzhambuSpace>
            <MaterialObjectDrawer
                objectId={activeObjectId}
                onClose={() => setActiveObjectId(null)}
                open={activeObjectId !== null}
            />
        </KuzhambuPage>
    );
};
