import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
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
import { BatchPublicationPanel } from "./batch-publication-panel";
import { MaterialFilters } from "./material-filters";
import { MaterialTable } from "./material-table";
import { MaterialDraftCanvas } from "./material-draft-canvas";
import { MaterialObjectDrawer } from "./material-object-drawer";
import * as service from "./graph-material-service";
import type { GraphMaterialPageQuery } from "./graph-material-service";
import type { GraphMaterialListRecord, GraphMaterialRecord } from "./graph-material-types";
import "./graph-material-page.css";

const toMaterialRecord = (record: GraphMaterialListRecord) => record.material ?? null;

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

export const GraphMaterialPage = () => {
    const navigate = useNavigate();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const [selectedMaterialIds, setSelectedMaterialIds] = useState<string[]>([]);
    const [isBatchPanelOpen, setIsBatchPanelOpen] = useState(false);
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
    const materials = records.map(toMaterialRecord).filter((material) => material !== null);
    const totalCount = pageResult?.totalCount ?? pageResult?.count ?? 0;
    const isInitialError = materialPageQuery.isError && records.length === 0;
    const updateQuery = (nextQuery: GraphMaterialPageQuery) => {
        setSelectedMaterialIds([]);
        setQuery(nextQuery);
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
                <KuzhambuSpace>
                    <KuzhambuButton
                        disabled={selectedMaterialIds.length === 0 || !canApplyGraph}
                        testId="knowledge-graph-material-open-batch-publication-button"
                        onClick={() => setIsBatchPanelOpen(true)}
                    >
                        批量发布（{selectedMaterialIds.length}）
                    </KuzhambuButton>
                </KuzhambuSpace>
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
                            onViewTasks={navigate}
                        />
                    </KuzhambuCard>
                ) : null}
                {!isInitialError && materials.length > 0 ? (
                    <KuzhambuCard title="批量发布选择">
                        <KuzhambuSpace wrap>
                            {materials.map((material) => (
                                <KuzhambuButton
                                    key={material.id}
                                    testId={`knowledge-graph-material-select-${material.id}-button`}
                                    type={
                                        selectedMaterialIds.includes(material.id)
                                            ? "primary"
                                            : "default"
                                    }
                                    onClick={() =>
                                        setSelectedMaterialIds((currentIds) =>
                                            currentIds.includes(material.id)
                                                ? currentIds.filter((id) => id !== material.id)
                                                : [...currentIds, material.id]
                                        )
                                    }
                                >
                                    {selectedMaterialIds.includes(material.id) ? "已选择" : "选择"}{" "}
                                    {material.title}
                                </KuzhambuButton>
                            ))}
                        </KuzhambuSpace>
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
            <BatchPublicationPanel
                materials={selectedMaterialIds
                    .map((id) => materials.find((material) => material.id === id))
                    .filter((material): material is GraphMaterialRecord => material !== undefined)}
                results={graphMaterialMockData.batchPublicationResults}
                canApplyGraph={canApplyGraph}
                onClose={() => setIsBatchPanelOpen(false)}
                open={isBatchPanelOpen}
            />
            <MaterialObjectDrawer
                objectId={activeObjectId}
                onClose={() => setActiveObjectId(null)}
                open={activeObjectId !== null}
            />
        </KuzhambuPage>
    );
};
