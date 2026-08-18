import { useMutation } from "@tanstack/react-query";
import type { Key } from "react";
import { useMemo, useState } from "react";
import { RobotOutlined } from "@ant-design/icons";
import { App, Typography } from "antd";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { buildReuseConflictDecisions } from "@/pages/knowledge/graph-material/graph-publication-conflicts";
import { MaterialDetailDrawer } from "./material-detail-drawer";
import type {
    GraphMaterialListRecord,
    GraphMaterialRecord,
    GraphMaterialStatus,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";
import {
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuTable,
    KuzhambuTag,
    type KuzhambuTableProps
} from "@/components";
import "./material-list-panel.css";

const { Text } = Typography;

const STATUS_LABELS: Readonly<Record<GraphMaterialStatus, string>> = {
    DRAFT: "草稿",
    READY: "待发布",
    FAILED: "失败",
    PUBLISHED: "已发布",
    PUBLISHING: "发布中",
    WITHDRAWING: "撤回中"
};

const STATUS_TYPES: Readonly<
    Record<GraphMaterialStatus, "accent" | "info" | "success" | "warning" | "danger">
> = {
    DRAFT: "accent",
    READY: "info",
    FAILED: "danger",
    PUBLISHED: "success",
    PUBLISHING: "info",
    WITHDRAWING: "warning"
};

const TASK_STATUS_LABELS: Readonly<Record<GraphTaskExecutionStatus, string>> = {
    CANCELLED: "已取消",
    FAILED: "已失败",
    PENDING: "待执行",
    RUNNING: "运行中",
    SUCCEEDED: "已成功"
};

const TASK_STATUS_TYPES: Readonly<
    Record<GraphTaskExecutionStatus, "neutral" | "info" | "success" | "warning" | "danger">
> = {
    CANCELLED: "neutral",
    FAILED: "danger",
    PENDING: "warning",
    RUNNING: "info",
    SUCCEEDED: "success"
};

const DISPOSITION_LABELS: Readonly<Record<GraphTaskDisposition, string>> = {
    ADOPTED_MERGE: "合并采纳",
    ADOPTED_REPLACE: "替换采纳",
    DISCARDED: "已丢弃",
    PENDING: "待处置",
    SUPERSEDED: "已替代"
};

const DEFAULT_COLUMN_WIDTHS = {
    status: 80,
    taskSummary: 160
};

const readRecordKey = (record: GraphMaterialListRecord) =>
    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`;

const hasActiveExtractionTask = (record: GraphMaterialListRecord) =>
    record.latestTask?.executionStatus === "PENDING" ||
    record.latestTask?.executionStatus === "RUNNING";

const publicationActionFor = (material?: GraphMaterialRecord | null) => {
    if (!material || material.status === "PUBLISHING" || material.status === "WITHDRAWING") {
        return null;
    }
    if (material.status === "PUBLISHED") {
        return "withdraw";
    }
    return material.status === "READY" ? "publish" : null;
};

const readSelectedPublicationAction = (
    canPublishSelectedMaterials: boolean,
    canWithdrawSelectedMaterials: boolean
) => {
    if (canWithdrawSelectedMaterials) {
        return "withdraw";
    }
    if (canPublishSelectedMaterials) {
        return "publish";
    }
    return null;
};

const readBatchPublicationDisabledReason = (canEditGraph: boolean, selectedRecordCount: number) => {
    if (!canEditGraph) {
        return "需要图谱编辑权限";
    }
    if (selectedRecordCount === 0) {
        return "请先选择素材";
    }
    return "仅可对状态一致且可发布或可撤回的素材执行批量操作";
};

interface MaterialListPanelProps {
    canEditGraph?: boolean;
    dataSource: GraphMaterialListRecord[];
    loading?: boolean;
    onRefreshMaterials: () => Promise<unknown>;
    pagination?: KuzhambuTableProps<GraphMaterialListRecord>["pagination"];
    showPlaceholder?: boolean;
}

export const MaterialListPanel = ({
    canEditGraph = true,
    dataSource,
    loading = false,
    onRefreshMaterials,
    pagination,
    showPlaceholder = false
}: MaterialListPanelProps) => {
    const { message: messageApi } = App.useApp();
    const [activeRecord, setActiveRecord] = useState<GraphMaterialListRecord | null>(null);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const batchExtractionMutation = useMutation({
        mutationFn: service.createBatchExtraction,
        onSuccess: (result) => {
            setSelectedRowKeys([]);
            void onRefreshMaterials();
            const failures = result.materials.filter((material) => !material.success);
            if (failures.length === 0) {
                messageApi.success(`已创建 ${result.materials.length} 个抽取任务`);
                return;
            }
            const failureSummary = failures
                .map(
                    (material) =>
                        `${material.contentRef.contentType} #${material.contentRef.contentRefId}：${material.failureMessage ?? "创建失败"}`
                )
                .join("；");
            messageApi.warning(
                `已创建 ${result.materials.length - failures.length} 个任务，${failureSummary}`
            );
        },
        onError: (error) =>
            messageApi.error(error instanceof Error ? error.message : "创建抽取任务失败")
    });
    const retryExtractionMutation = useMutation({
        mutationFn: service.retryExtraction,
        onSuccess: (task) => {
            void onRefreshMaterials();
            messageApi.success(`抽取任务已重试 #${task?.id ?? "-"}`);
        },
        onError: (error) =>
            messageApi.error(error instanceof Error ? error.message : "重试抽取任务失败")
    });
    const publicationMutation = useMutation({
        mutationFn: async (record: GraphMaterialListRecord) => {
            if (!record.material) {
                throw new Error("素材尚未初始化，无法发布。");
            }
            if (publicationActionFor(record.material) === "withdraw") {
                await service.previewWithdrawal({ contentRef: record.material.contentRef });
                return service.withdrawMaterial({
                    contentRef: record.material.contentRef,
                    materialLockVersion: record.material.lockVersion ?? ""
                });
            }
            const preview = await service.previewPublication({
                contentRef: record.material.contentRef
            });
            if (!preview.publishable) {
                throw new Error(preview.issues[0]?.message ?? "发布预检未通过。");
            }
            return service.publishMaterial({
                conflictDecisions: buildReuseConflictDecisions(preview),
                contentRef: preview.materialRef,
                materialLockVersion: preview.materialLockVersion,
                previewToken: preview.previewToken
            });
        },
        onSuccess: (_, record) => {
            messageApi.success(
                publicationActionFor(record.material) === "withdraw" ? "素材已撤销" : "素材已发布"
            );
            void onRefreshMaterials();
        },
        onError: (error) =>
            messageApi.error(error instanceof Error ? error.message : "素材发布操作失败")
    });
    const batchPublicationMutation = useMutation({
        mutationFn: async ({
            action,
            records
        }: {
            action: "publish" | "withdraw";
            records: GraphMaterialListRecord[];
        }) => {
            const materials = records
                .map((record) => record.material)
                .filter(
                    (material): material is GraphMaterialRecord =>
                        material !== null && material !== undefined
                );
            if (materials.length !== records.length) {
                throw new Error("存在未初始化素材，无法执行批量发布操作。");
            }
            if (action === "withdraw") {
                const preview = await service.previewBatchWithdrawal({
                    contentRefs: materials.map((material) => material.contentRef)
                });
                const blocked = preview.materials.find((item) => !item.success);
                if (blocked) {
                    throw new Error(blocked.failureMessage ?? "存在无法撤销的素材。");
                }
                return service.withdrawBatch({
                    materials: materials.map((material) => ({
                        contentRef: material.contentRef,
                        materialLockVersion: material.lockVersion ?? ""
                    }))
                });
            }
            const preview = await service.previewBatchPublication({
                contentRefs: materials.map((material) => material.contentRef)
            });
            const blocked = preview.materials.find(
                (item) => !item.success || !item.result?.publishable
            );
            if (blocked) {
                throw new Error(blocked.failureMessage ?? "存在未通过预检的素材，无法批量发布。");
            }
            return service.publishBatch({
                materials: preview.materials.flatMap((item) => {
                    const result = item.result;
                    return result
                        ? [
                              {
                                  conflictDecisions: buildReuseConflictDecisions(result),
                                  contentRef: result.materialRef,
                                  materialLockVersion: result.materialLockVersion,
                                  previewToken: result.previewToken
                              }
                          ]
                        : [];
                })
            });
        },
        onSuccess: (result, variables) => {
            const failures = result.materials.filter((material) => !material.success);
            messageApi[failures.length === 0 ? "success" : "warning"](
                failures.length === 0
                    ? `${variables.action === "publish" ? "发布" : "撤销"} ${result.materials.length} 个素材成功`
                    : `${variables.action === "publish" ? "发布" : "撤销"}完成，${failures.length} 个素材失败`
            );
            setSelectedRowKeys([]);
            void onRefreshMaterials();
        },
        onError: (error) =>
            messageApi.error(error instanceof Error ? error.message : "批量发布操作失败")
    });
    const recordByKey = useMemo(
        () =>
            new Map(
                dataSource.map((record) => [
                    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`,
                    record
                ])
            ),
        [dataSource]
    );
    const selectedRecords = selectedRowKeys
        .map((key) => recordByKey.get(String(key)))
        .filter((record): record is GraphMaterialListRecord => record !== undefined);
    const retryableSelectedRecords = selectedRecords.filter(
        (record) => record.latestTask?.executionStatus === "FAILED"
    );
    const extractableSelectedRecords = selectedRecords.filter(
        (record) =>
            record.latestTask?.executionStatus !== "FAILED" && !hasActiveExtractionTask(record)
    );
    const canPublishSelectedMaterials =
        selectedRecords.length > 0 &&
        selectedRecords.every((record) => publicationActionFor(record.material) === "publish");
    const canWithdrawSelectedMaterials =
        selectedRecords.length > 0 &&
        selectedRecords.every((record) => publicationActionFor(record.material) === "withdraw");
    const selectedPublicationAction = readSelectedPublicationAction(
        canPublishSelectedMaterials,
        canWithdrawSelectedMaterials
    );
    const batchPublicationDisabledReason = readBatchPublicationDisabledReason(
        canEditGraph,
        selectedRecords.length
    );
    const isTableMutating =
        batchExtractionMutation.isPending ||
        retryExtractionMutation.isPending ||
        publicationMutation.isPending ||
        batchPublicationMutation.isPending;

    const closeMaterialDetailDrawer = () => {
        setActiveRecord(null);
    };
    const extractMaterial = (record: GraphMaterialListRecord) => {
        if (record.latestTask?.executionStatus === "FAILED") {
            return retryExtractionMutation.mutateAsync({
                expectedExecutionStatus: "FAILED",
                taskId: record.latestTask.id,
                taskLockVersion: record.latestTask.lockVersion
            });
        }
        return batchExtractionMutation.mutateAsync({ contentRefs: [record.source.contentRef] });
    };
    const extractSelectedMaterials = () => {
        const skippedCount =
            selectedRecords.length -
            retryableSelectedRecords.length -
            extractableSelectedRecords.length;
        if (skippedCount > 0) {
            messageApi.info(`${skippedCount} 个素材已有执行中的提取任务，已跳过。`);
        }
        void (async () => {
            try {
                await Promise.all([
                    ...(extractableSelectedRecords.length > 0
                        ? [
                              batchExtractionMutation.mutateAsync({
                                  contentRefs: extractableSelectedRecords.map(
                                      (record) => record.source.contentRef
                                  )
                              })
                          ]
                        : []),
                    ...retryableSelectedRecords.map((record) =>
                        retryExtractionMutation.mutateAsync({
                            expectedExecutionStatus: "FAILED",
                            taskId: record.latestTask!.id,
                            taskLockVersion: record.latestTask!.lockVersion
                        })
                    )
                ]);
                setSelectedRowKeys([]);
            } catch {
                // The individual mutation reports the error and keeps the selection for another try.
            }
        })();
    };
    const columns: KuzhambuTableProps<GraphMaterialListRecord>["columns"] = [
        {
            title: "标题",
            key: "title",
            render: (_, record) => (
                <a
                    href="#"
                    aria-label={`打开素材 ${record.source.title}`}
                    data-testid={`knowledge-graph-material-open-${record.source.contentRef.contentRefId}-link`}
                    onClick={(event) => {
                        event.preventDefault();
                        setActiveRecord(record);
                    }}
                >
                    {record.source.title}
                </a>
            )
        },
        {
            title: "发布",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, record) =>
                record.material ? (
                    <KuzhambuTag type={STATUS_TYPES[record.material.status]}>
                        {STATUS_LABELS[record.material.status]}
                    </KuzhambuTag>
                ) : (
                    <KuzhambuTag type="neutral">未抽取</KuzhambuTag>
                )
        },
        {
            title: "提取状态",
            key: "taskSummary",
            width: DEFAULT_COLUMN_WIDTHS.taskSummary,
            render: (_, record) => {
                const task = record.latestTask;
                if (!task) {
                    return <KuzhambuTag type="neutral">无任务</KuzhambuTag>;
                }
                return (
                    <KuzhambuSpace orientation="vertical" size={2}>
                        <KuzhambuTag type={TASK_STATUS_TYPES[task.executionStatus]}>
                            {TASK_STATUS_LABELS[task.executionStatus]}
                        </KuzhambuTag>
                        <Text type="secondary">第 {task.attemptNo} 次</Text>
                        {task.disposition ? (
                            <Text type="secondary">{DISPOSITION_LABELS[task.disposition]}</Text>
                        ) : null}
                    </KuzhambuSpace>
                );
            }
        },
        {
            key: "actions",
            options: (record) => [
                {
                    key: "view",
                    text: "查看",
                    testId: "knowledge-graph-material-view-button",
                    ariaLabel: `查看素材 ${record.source.title}`,
                    onClick: () => setActiveRecord(record)
                },
                {
                    key: "extract",
                    text: record.latestTask?.executionStatus === "FAILED" ? "重试" : "提取",
                    testId: "knowledge-graph-material-extract-button",
                    ariaLabel:
                        record.latestTask?.executionStatus === "FAILED"
                            ? `重试 ${record.source.title}`
                            : `提取 ${record.source.title}`,
                    disabled:
                        !canEditGraph ||
                        hasActiveExtractionTask(record) ||
                        batchExtractionMutation.isPending ||
                        retryExtractionMutation.isPending,
                    onClick: () => void extractMaterial(record)
                },
                {
                    key: "publication",
                    text: publicationActionFor(record.material) === "withdraw" ? "撤回" : "发布",
                    ariaLabel:
                        publicationActionFor(record.material) === "withdraw"
                            ? `撤回素材 ${record.source.title}`
                            : `发布素材 ${record.source.title}`,
                    disabled:
                        !canEditGraph ||
                        publicationMutation.isPending ||
                        publicationActionFor(record.material) === null,
                    onClick: () => publicationMutation.mutate(record)
                }
            ]
        }
    ];

    return (
        <div className="graph-material-list-panel">
            {showPlaceholder ? (
                <div className="graph-material-list-placeholder" aria-label="图谱素材列表占位">
                    请选择左侧目录叶子节点查看素材列表
                </div>
            ) : (
                <KuzhambuTable<GraphMaterialListRecord>
                    ariaLabel="图谱素材列表"
                    batchActionBar={{
                        actions: (
                            <KuzhambuSpace>
                                <KuzhambuButton
                                    disabled={
                                        !canEditGraph ||
                                        (extractableSelectedRecords.length === 0 &&
                                            retryableSelectedRecords.length === 0)
                                    }
                                    icon={<RobotOutlined />}
                                    testId="knowledge-graph-material-batch-extract-button"
                                    type="primary"
                                    onClick={extractSelectedMaterials}
                                >
                                    {retryableSelectedRecords.length > 0 &&
                                    extractableSelectedRecords.length === 0
                                        ? "批量重试"
                                        : "批量提取"}
                                </KuzhambuButton>
                                <KuzhambuButton
                                    ariaLabel={
                                        selectedPublicationAction
                                            ? selectedPublicationAction === "withdraw"
                                                ? "批量撤回素材"
                                                : "批量发布素材"
                                            : batchPublicationDisabledReason
                                    }
                                    disabled={
                                        !selectedPublicationAction ||
                                        batchPublicationMutation.isPending
                                    }
                                    loading={batchPublicationMutation.isPending}
                                    testId="knowledge-graph-material-batch-publication-button"
                                    title={
                                        selectedPublicationAction
                                            ? undefined
                                            : batchPublicationDisabledReason
                                    }
                                    onClick={() => {
                                        if (!selectedPublicationAction) {
                                            return;
                                        }
                                        batchPublicationMutation.mutate({
                                            action: selectedPublicationAction,
                                            records: selectedRecords
                                        });
                                    }}
                                >
                                    {selectedPublicationAction === "withdraw"
                                        ? "批量撤回"
                                        : "批量发布"}
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        ),
                        selectedCount: selectedRowKeys.length
                    }}
                    columns={columns}
                    dataSource={dataSource}
                    loading={loading || isTableMutating}
                    pagination={pagination}
                    rowKey={readRecordKey}
                    rowSelection={{
                        selectedRowKeys,
                        onChange: setSelectedRowKeys
                    }}
                    scroll={{ x: 720 }}
                    locale={{
                        emptyText: loading ? "图谱素材加载中..." : "暂无图谱素材"
                    }}
                />
            )}
            <MaterialDetailDrawer
                record={activeRecord}
                onClose={closeMaterialDetailDrawer}
                onRefreshMaterials={onRefreshMaterials}
            />
        </div>
    );
};
