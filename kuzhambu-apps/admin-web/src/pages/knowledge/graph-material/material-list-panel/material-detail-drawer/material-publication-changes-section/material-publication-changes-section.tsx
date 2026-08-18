import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { buildReuseConflictDecisions } from "@/pages/knowledge/graph-material/graph-publication-conflicts";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import "./material-publication-changes-section.css";

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

interface MaterialPublicationChangesSectionProps {
    detail: GraphMaterialDetailRecord | null;
}

const readMaterialStatusLabel = (detail: GraphMaterialDetailRecord | null) => {
    if (!detail?.material) {
        return "未初始化";
    }
    const statusLabels = {
        DRAFT: "草稿",
        READY: "待发布",
        FAILED: "失败",
        PUBLISHED: "已发布",
        PUBLISHING: "发布中",
        WITHDRAWING: "撤回中"
    } as const;
    return statusLabels[detail.material.status];
};

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

export const MaterialPublicationChangesSection = ({
    detail
}: MaterialPublicationChangesSectionProps) => {
    const queryClient = useQueryClient();
    const canApplyGraph = hasPermission("knowledge:graph:edit");
    const [isConflictResolved, setIsConflictResolved] = useState(false);
    const [isPublishing, setIsPublishing] = useState(false);
    const [isWithdrawing, setIsWithdrawing] = useState(false);
    const [isPrecheckingDeletion, setIsPrecheckingDeletion] = useState(false);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const hasMaterial = Boolean(detail?.material);
    const publishedNodeCount = detail?.materialStats?.publishedNodeCount ?? "0";
    const publishedEdgeCount = detail?.materialStats?.publishedEdgeCount ?? "0";
    const isBusy = isPublishing || isWithdrawing || isPrecheckingDeletion;

    const runMaterialAction = async (action: () => Promise<void>, message: string) => {
        setSuccessMessage(null);
        setErrorMessage(null);
        try {
            await action();
            setSuccessMessage(message);
        } catch (error) {
            setErrorMessage(getErrorMessage(error));
        }
    };

    const publishMaterial = async () => {
        if (!detail?.material?.lockVersion) {
            throw new Error("素材缺少锁版本，无法发布。");
        }
        const preview = await service.previewPublication({
            contentRef: detail.material.contentRef
        });
        if (!preview.publishable) {
            throw new Error(preview.issues[0]?.message ?? "发布预检未通过。");
        }
        await service.publishMaterial({
            conflictDecisions: buildReuseConflictDecisions(preview),
            contentRef: preview.materialRef,
            materialLockVersion: preview.materialLockVersion,
            previewToken: preview.previewToken
        });
        await queryClient.invalidateQueries({ queryKey: ["knowledge", "graph-material"] });
    };

    const runPublishMaterial = async () => {
        if (!detail) {
            return;
        }
        setIsPublishing(true);
        try {
            await runMaterialAction(publishMaterial, "素材已发布");
        } finally {
            setIsPublishing(false);
        }
    };

    const withdrawMaterial = async () => {
        if (!detail?.material?.lockVersion) {
            throw new Error("素材缺少锁版本，无法撤回。");
        }
        await service.previewWithdrawal({ contentRef: detail.material.contentRef });
        await service.withdrawMaterial({
            contentRef: detail.material.contentRef,
            materialLockVersion: detail.material.lockVersion
        });
        await queryClient.invalidateQueries({ queryKey: ["knowledge", "graph-material"] });
    };

    const runWithdrawMaterial = async () => {
        if (!detail) {
            return;
        }
        setIsWithdrawing(true);
        try {
            await runMaterialAction(withdrawMaterial, "素材已撤回");
        } finally {
            setIsWithdrawing(false);
        }
    };

    const precheckDeletion = async () => {
        const contentRef = detail?.source.contentRef;
        if (!contentRef) {
            return;
        }
        setIsPrecheckingDeletion(true);
        try {
            await runMaterialAction(async () => {
                await service.precheckDeletion({ contentRef });
            }, "删除预检已生成，请在当前发布变更段确认影响。");
        } finally {
            setIsPrecheckingDeletion(false);
        }
    };

    return (
        <KuzhambuSpace
            className="knowledge-graph-material-publication-changes-section"
            data-testid="knowledge-graph-material-detail-publication-changes-section"
            orientation="vertical"
            size={12}
        >
            <KuzhambuCard title="发布预览" size="small">
                <KuzhambuSpace
                    className="knowledge-graph-material-publication-changes-section-content"
                    orientation="vertical"
                    size={10}
                >
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
                    {successMessage ? (
                        <KuzhambuAlert title={successMessage} type="success" showIcon />
                    ) : null}
                    {errorMessage ? (
                        <KuzhambuAlert title={errorMessage} type="error" showIcon />
                    ) : null}
                    <KuzhambuSpace>
                        <KuzhambuButton
                            disabled={
                                !canApplyGraph || !hasMaterial || !isConflictResolved || isBusy
                            }
                            loading={isPublishing}
                            testId="knowledge-graph-material-publish-preview-button"
                            type="primary"
                            onClick={() => void runPublishMaterial()}
                        >
                            发布素材
                        </KuzhambuButton>
                        <KuzhambuButton
                            disabled={!canApplyGraph || !hasMaterial || isBusy}
                            loading={isWithdrawing}
                            testId="knowledge-graph-material-withdraw-preview-button"
                            onClick={() => void runWithdrawMaterial()}
                        >
                            撤回素材
                        </KuzhambuButton>
                        <KuzhambuButton
                            disabled={!canApplyGraph || !hasMaterial || isBusy}
                            loading={isPrecheckingDeletion}
                            testId="knowledge-graph-material-delete-precheck-button"
                            danger
                            onClick={() => void precheckDeletion()}
                        >
                            删除预检
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
