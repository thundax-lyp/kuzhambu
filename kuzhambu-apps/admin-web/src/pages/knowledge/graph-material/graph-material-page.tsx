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
import { MaterialBatchActions } from "./material-batch-actions";
import { MaterialDetailDrawer } from "./material-detail-drawer";
import { MaterialFilters } from "./material-filters";
import { MaterialTable } from "./material-table";
import * as service from "./graph-material-service";
import type { GraphMaterialPageQuery } from "./graph-material-service";
import type {
    GraphContentRefRecord,
    GraphMaterialBatchPublicationResult,
    GraphMaterialDrawerSection,
    GraphMaterialListRecord,
    GraphMaterialRecord
} from "./graph-material-types";
import "./graph-material-page.css";

const toMaterialRowKey = (record: GraphMaterialListRecord) =>
    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`;

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

const EMPTY_MATERIAL_RECORDS: GraphMaterialListRecord[] = [];

export const GraphMaterialPage = () => {
    const navigate = useNavigate();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const [selectedMaterialKeys, setSelectedMaterialKeys] = useState<Key[]>([]);
    const [isBatchPublishing, setIsBatchPublishing] = useState(false);
    const [activeMaterial, setActiveMaterial] = useState<GraphMaterialRecord | null>(null);
    const [activeMaterialSection, setActiveMaterialSection] =
        useState<GraphMaterialDrawerSection>("OVERVIEW");
    const [query, setQuery] = useState<GraphMaterialPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const materialPageQuery = useQuery({
        enabled: canViewGraph,
        queryFn: () => service.pageMaterials(query),
        queryKey: ["knowledge", "graph-material", "page", query]
    });
    const materialDetailQuery = useQuery({
        enabled: activeMaterial !== null,
        queryFn: () => {
            if (!activeMaterial) {
                throw new Error("未选择素材");
            }
            return service.getMaterial({ contentRef: activeMaterial.contentRef });
        },
        queryKey: ["knowledge", "graph-material", "detail", activeMaterial?.contentRef]
    });
    const pageResult = materialPageQuery.data;
    const records = pageResult?.records ?? EMPTY_MATERIAL_RECORDS;
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
    const publishSelectedMaterials = async (
        targetRecords: GraphMaterialListRecord[]
    ): Promise<GraphMaterialBatchPublicationResult[]> => {
        const recordsWithMaterial = targetRecords.filter((record) => record.material?.id);
        if (recordsWithMaterial.length === 0) {
            return [];
        }
        setIsBatchPublishing(true);
        try {
            const preview = await service.previewBatchPublication({
                contentRefs: recordsWithMaterial.map((record) => record.source.contentRef)
            });
            const previewFailures: GraphMaterialBatchPublicationResult[] = [];
            const confirmations = preview.materials.flatMap((item) => {
                const record = recordsWithMaterial.find(
                    (candidate) =>
                        candidate.source.contentRef.contentType === item.contentRef.contentType &&
                        candidate.source.contentRef.contentRefId === item.contentRef.contentRefId
                );
                if (!record?.material?.id) {
                    return [];
                }
                if (!item.success || !item.result?.publishable) {
                    previewFailures.push({
                        failureReason:
                            item.failureMessage ??
                            item.result?.issues?.[0]?.message ??
                            "发布预检未通过。",
                        materialId: record.material.id,
                        status: "FAILED"
                    });
                    return [];
                }
                return [
                    {
                        conflictDecisions: [],
                        contentRef: item.result.materialRef,
                        materialLockVersion: item.result.materialLockVersion,
                        previewToken: item.result.previewToken
                    }
                ];
            });
            if (confirmations.length === 0) {
                return previewFailures;
            }
            const publishResult = await service.publishBatch({ materials: confirmations });
            const publishedResults = publishResult.materials.flatMap(
                (item): GraphMaterialBatchPublicationResult[] => {
                    const record = recordsWithMaterial.find(
                        (candidate) =>
                            candidate.source.contentRef.contentType ===
                                item.contentRef.contentType &&
                            candidate.source.contentRef.contentRefId ===
                                item.contentRef.contentRefId
                    );
                    if (!record?.material?.id) {
                        return [];
                    }
                    return [
                        {
                            failureReason:
                                item.failureMessage ?? item.result?.failureMessage ?? undefined,
                            materialId: record.material.id,
                            status: item.success && item.result?.success ? "PUBLISHED" : "FAILED"
                        }
                    ];
                }
            );
            await materialPageQuery.refetch();
            return [...previewFailures, ...publishedResults];
        } finally {
            setIsBatchPublishing(false);
        }
    };
    const viewSelectedTasks = (contentRefs: GraphContentRefRecord[]) => {
        const params = new URLSearchParams();
        params.set("contentRefs", JSON.stringify(contentRefs));
        navigate(`/knowledge/graph-extraction?${params.toString()}`);
    };
    const openMaterialDetailDrawer = (material: GraphMaterialRecord) => {
        setActiveMaterial(material);
        setActiveMaterialSection("OVERVIEW");
    };
    const closeMaterialDetailDrawer = () => {
        setActiveMaterial(null);
        setActiveMaterialSection("OVERVIEW");
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
                    canApplyGraph={canEditGraph}
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
                            onOpenMaterial={openMaterialDetailDrawer}
                            onSelectionChange={(keys) => setSelectedMaterialKeys(keys)}
                            onViewTasks={navigate}
                            selectedRowKeys={selectedMaterialKeys}
                        />
                    </KuzhambuCard>
                ) : null}
            </KuzhambuSpace>
            <MaterialDetailDrawer
                activeSection={activeMaterialSection}
                detail={materialDetailQuery.data ?? null}
                error={materialDetailQuery.error}
                loading={materialDetailQuery.isFetching}
                material={activeMaterial}
                open={activeMaterial !== null}
                onClose={closeMaterialDetailDrawer}
                onRetry={() => void materialDetailQuery.refetch()}
                onDeletePrecheck={(contentRef) => service.precheckDeletion({ contentRef })}
                onPublish={async (detail) => {
                    if (!detail.material?.lockVersion) {
                        throw new Error("素材缺少锁版本，无法发布。");
                    }
                    const preview = await service.previewPublication({
                        contentRef: detail.material.contentRef
                    });
                    if (!preview.publishable) {
                        throw new Error(preview.issues[0]?.message ?? "发布预检未通过。");
                    }
                    await service.publishMaterial({
                        conflictDecisions: [],
                        contentRef: preview.materialRef,
                        materialLockVersion: preview.materialLockVersion,
                        previewToken: preview.previewToken
                    });
                    await Promise.all([materialPageQuery.refetch(), materialDetailQuery.refetch()]);
                }}
                onWithdraw={async (detail) => {
                    if (!detail.material?.lockVersion) {
                        throw new Error("素材缺少锁版本，无法撤回。");
                    }
                    await service.previewWithdrawal({ contentRef: detail.material.contentRef });
                    await service.withdrawMaterial({
                        contentRef: detail.material.contentRef,
                        materialLockVersion: detail.material.lockVersion
                    });
                    await Promise.all([materialPageQuery.refetch(), materialDetailQuery.refetch()]);
                }}
                onSectionChange={setActiveMaterialSection}
            />
        </KuzhambuPage>
    );
};
