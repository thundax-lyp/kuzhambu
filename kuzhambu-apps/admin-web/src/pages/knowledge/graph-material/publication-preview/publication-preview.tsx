import { useState } from "react";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type {
    GraphContentRefRecord,
    GraphDeletionPrecheckRecord,
    GraphMaterialDetailRecord
} from "@/pages/knowledge/graph-material/graph-material-types";

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
    onDeletePrecheck: (contentRef: GraphContentRefRecord) => Promise<GraphDeletionPrecheckRecord>;
    onPublish: (detail: GraphMaterialDetailRecord) => Promise<void>;
    onWithdraw: (detail: GraphMaterialDetailRecord) => Promise<void>;
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

const getErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "请稍后重试。";

export const PublicationPreview = ({
    canApplyGraph,
    detail,
    onDeletePrecheck,
    onPublish,
    onWithdraw
}: PublicationPreviewProps) => {
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
        if (!detail) {
            return;
        }
        setIsPublishing(true);
        try {
            await runMaterialAction(() => onPublish(detail), "素材已发布");
        } finally {
            setIsPublishing(false);
        }
    };

    const withdrawMaterial = async () => {
        if (!detail) {
            return;
        }
        setIsWithdrawing(true);
        try {
            await runMaterialAction(() => onWithdraw(detail), "素材已撤回");
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
                await onDeletePrecheck(contentRef);
            }, "删除预检已生成，请在当前发布变更段确认影响。");
        } finally {
            setIsPrecheckingDeletion(false);
        }
    };

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
                            onClick={() => void publishMaterial()}
                        >
                            发布素材
                        </KuzhambuButton>
                        <KuzhambuButton
                            disabled={!canApplyGraph || !hasMaterial || isBusy}
                            loading={isWithdrawing}
                            testId="knowledge-graph-material-withdraw-preview-button"
                            onClick={() => void withdrawMaterial()}
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
