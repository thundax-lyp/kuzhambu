import { useState } from "react";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";

const PREVIEW_TYPE_LABELS = {
    green: "新增对象",
    orange: "关联已有对象",
    red: "冲突待处理",
    blue: "已发布对象"
} as const;

const PREVIEW_TAG_TYPES = {
    green: "success",
    orange: "warning",
    red: "danger",
    blue: "info"
} as const;

const PUBLICATION_PREVIEW_ITEMS = [
    { color: "green", id: "preview-create" },
    { color: "orange", id: "preview-link" },
    { color: "red", id: "preview-conflict" },
    { color: "blue", id: "preview-published" }
] as const;

interface PublicationPreviewProps {
    canApplyGraph: boolean;
    detail: GraphMaterialDetailRecord | null;
}

const readMaterialStatusLabel = (detail: GraphMaterialDetailRecord | null) => {
    if (!detail?.material) {
        return "未初始化";
    }
    const statusLabels = {
        DRAFT: "草稿",
        FAILED: "失败",
        PUBLISHED: "已发布",
        PUBLISHING: "发布中",
        WITHDRAWING: "撤回中"
    } as const;
    return statusLabels[detail.material.status];
};

export const PublicationPreview = ({ canApplyGraph, detail }: PublicationPreviewProps) => {
    const [isConflictResolved, setIsConflictResolved] = useState(false);
    const [isFrozen, setIsFrozen] = useState(false);
    const [isWithdrawn, setIsWithdrawn] = useState(false);
    const [hasDeletePrecheck, setHasDeletePrecheck] = useState(false);

    const hasMaterial = Boolean(detail?.material);
    const publishedNodeCount = detail?.materialStats?.publishedNodeCount ?? "0";
    const publishedEdgeCount = detail?.materialStats?.publishedEdgeCount ?? "0";

    return (
        <KuzhambuSpace
            data-testid="knowledge-graph-material-detail-publication-changes-section"
            orientation="vertical"
            size={12}
            style={{ width: "100%" }}
        >
            <KuzhambuCard title="发布预览" size="small">
                <KuzhambuSpace orientation="vertical" size={10} style={{ width: "100%" }}>
                    <KuzhambuDescriptions
                        ariaLabel="素材发布状态"
                        column={3}
                        items={[
                            { label: "素材状态", children: readMaterialStatusLabel(detail) },
                            { label: "已发布节点", children: publishedNodeCount },
                            { label: "已发布关系", children: publishedEdgeCount }
                        ]}
                        size="small"
                        bordered
                    />
                    {PUBLICATION_PREVIEW_ITEMS.map((item) => (
                        <KuzhambuSpace key={item.id}>
                            <KuzhambuTag type={PREVIEW_TAG_TYPES[item.color]}>
                                {item.color}
                            </KuzhambuTag>
                            <span>{PREVIEW_TYPE_LABELS[item.color]}</span>
                            {item.color === "red" && !isConflictResolved ? (
                                <KuzhambuButton
                                    size="small"
                                    testId="knowledge-graph-material-resolve-conflict-button"
                                    onClick={() => setIsConflictResolved(true)}
                                >
                                    标记冲突已解决
                                </KuzhambuButton>
                            ) : null}
                        </KuzhambuSpace>
                    ))}
                    {!hasMaterial ? (
                        <KuzhambuAlert
                            title="素材尚未初始化，发布不可用。"
                            type="warning"
                            showIcon
                        />
                    ) : null}
                    {!isConflictResolved ? (
                        <KuzhambuAlert title="存在未解决冲突，发布不可用。" type="error" showIcon />
                    ) : null}
                    {isFrozen ? <KuzhambuAlert title="发布已冻结" type="info" showIcon /> : null}
                    {isWithdrawn ? (
                        <KuzhambuAlert title="素材已撤回" type="warning" showIcon />
                    ) : null}
                    {hasDeletePrecheck ? (
                        <KuzhambuAlert
                            title="删除预检已生成，请在当前发布变更段确认影响。"
                            type="info"
                            showIcon
                        />
                    ) : null}
                    <KuzhambuSpace>
                        <KuzhambuButton
                            disabled={
                                !canApplyGraph ||
                                !hasMaterial ||
                                !isConflictResolved ||
                                isFrozen ||
                                isWithdrawn
                            }
                            testId="knowledge-graph-material-publish-preview-button"
                            type="primary"
                            onClick={() => setIsFrozen(true)}
                        >
                            发布素材
                        </KuzhambuButton>
                        <KuzhambuButton
                            disabled={!canApplyGraph || !hasMaterial || !isFrozen || isWithdrawn}
                            testId="knowledge-graph-material-withdraw-preview-button"
                            onClick={() => setIsWithdrawn(true)}
                        >
                            撤回素材
                        </KuzhambuButton>
                        <KuzhambuButton
                            disabled={!canApplyGraph || !hasMaterial}
                            testId="knowledge-graph-material-delete-precheck-button"
                            danger
                            onClick={() => setHasDeletePrecheck(true)}
                        >
                            删除预检
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
