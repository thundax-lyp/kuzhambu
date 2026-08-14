import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuListMeta,
    KuzhambuPage,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import { graphMaterialMockData } from "./__mocks__/graph-mock-data";
import { BatchPublicationPanel } from "./batch-publication-panel";
import { MaterialDraftCanvas } from "./material-draft-canvas";
import { MaterialObjectDrawer } from "./material-object-drawer";
import type { GraphMaterialRecord, GraphMaterialStatus } from "./graph-material-types";

const STATUS_LABELS: Record<GraphMaterialStatus, string> = {
    DRAFT: "草稿",
    PUBLISHING: "发布中",
    PUBLISHED: "已发布",
    WITHDRAWING: "撤回中",
    FAILED: "失败"
};

const STATUS_TYPES: Record<
    GraphMaterialStatus,
    "neutral" | "info" | "success" | "warning" | "danger"
> = {
    DRAFT: "neutral",
    PUBLISHING: "info",
    PUBLISHED: "success",
    WITHDRAWING: "warning",
    FAILED: "danger"
};

const canEditDraft = (status: GraphMaterialStatus) => status === "DRAFT" || status === "FAILED";

export const GraphMaterialPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const [isMockFailure, setIsMockFailure] = useState(false);
    const [isMockEmpty, setIsMockEmpty] = useState(false);
    const [selectedMaterialIds, setSelectedMaterialIds] = useState<string[]>([]);
    const [isBatchPanelOpen, setIsBatchPanelOpen] = useState(false);
    const [activeMaterial, setActiveMaterial] = useState<GraphMaterialRecord | null>(null);
    const [activeObjectId, setActiveObjectId] = useState<string | null>(null);
    const materials = graphMaterialMockData.materials as readonly GraphMaterialRecord[];

    if (!canViewGraph) {
        return (
            <KuzhambuPage description="需要知识图谱查看权限。" title="图谱素材库">
                <KuzhambuAlert title="无权查看图谱素材库" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage description="查看素材抽取状态和图谱草稿入口。" title="图谱素材库">
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuSpace>
                    <KuzhambuButton
                        testId="knowledge-graph-material-toggle-mock-empty-button"
                        onClick={() => setIsMockEmpty((value) => !value)}
                    >
                        {isMockEmpty ? "恢复素材列表" : "模拟空态"}
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-material-toggle-mock-failure-button"
                        onClick={() => setIsMockFailure((value) => !value)}
                    >
                        {isMockFailure ? "恢复模拟数据" : "模拟加载失败"}
                    </KuzhambuButton>
                    <KuzhambuButton
                        disabled={selectedMaterialIds.length === 0}
                        testId="knowledge-graph-material-open-batch-publication-button"
                        onClick={() => setIsBatchPanelOpen(true)}
                    >
                        批量发布（{selectedMaterialIds.length}）
                    </KuzhambuButton>
                </KuzhambuSpace>
                {isMockFailure ? (
                    <KuzhambuAlert title="素材库 Mock 数据加载失败" type="error" showIcon />
                ) : null}
                {!isMockFailure && isMockEmpty ? (
                    <KuzhambuAlert title="暂无图谱素材" type="info" showIcon />
                ) : null}
                {!isMockFailure && !isMockEmpty ? (
                    <KuzhambuCard title="素材列表">
                        <KuzhambuList
                            ariaLabel="图谱素材列表"
                            bordered
                            dataSource={[...materials]}
                            itemKey={(material) => material.id}
                            renderItem={(material) => (
                                <GraphMaterialListItem
                                    material={material}
                                    onOpen={() => setActiveMaterial(material)}
                                />
                            )}
                        />
                    </KuzhambuCard>
                ) : null}
                {!isMockFailure && !isMockEmpty ? (
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

interface GraphMaterialListItemProps {
    material: GraphMaterialRecord;
    onOpen: () => void;
}

const GraphMaterialListItem = ({ material, onOpen }: GraphMaterialListItemProps) => (
    <KuzhambuListItem
        extra={
            <KuzhambuSpace>
                <KuzhambuTag type={STATUS_TYPES[material.status]}>
                    {STATUS_LABELS[material.status]}
                </KuzhambuTag>
                {canEditDraft(material.status) ? (
                    <KuzhambuButton
                        testId={`knowledge-graph-material-extract-${material.id}-button`}
                        size="small"
                    >
                        发起抽取任务
                    </KuzhambuButton>
                ) : null}
                <KuzhambuButton
                    testId={`knowledge-graph-material-open-${material.id}-button`}
                    size="small"
                    onClick={onOpen}
                >
                    打开素材
                </KuzhambuButton>
            </KuzhambuSpace>
        }
    >
        <KuzhambuListMeta
            title={material.title}
            description={
                material.failureReason ? (
                    <KuzhambuAlert
                        title={material.failureReason}
                        type="error"
                        showIcon
                        closable={false}
                    />
                ) : (
                    `状态：${STATUS_LABELS[material.status]}`
                )
            }
        />
    </KuzhambuListItem>
);
