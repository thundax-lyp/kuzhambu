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
                            renderItem={(material) => <GraphMaterialListItem material={material} />}
                        />
                    </KuzhambuCard>
                ) : null}
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};

interface GraphMaterialListItemProps {
    material: GraphMaterialRecord;
}

const GraphMaterialListItem = ({ material }: GraphMaterialListItemProps) => (
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
