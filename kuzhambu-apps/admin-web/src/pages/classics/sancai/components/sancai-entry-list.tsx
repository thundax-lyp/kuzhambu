import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Skeleton, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import {
    hasClassicsContentPermission,
    type ClassicsBatchOperationRecord
} from "@/pages/classics/common/classics-content-types";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiEntryLifecycleStatus,
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "../sancai-types";

const { Text } = Typography;

interface SancaiEntryListProps {
    entries: SancaiEntryRecord[];
    isLoading: boolean;
    onChangeLifecycleStatus: (entry: SancaiEntryRecord, action: SancaiEntryLifecycleAction) => void;
    onDelete: (entry: SancaiEntryRecord) => void;
    onExport: (entry: SancaiEntryRecord) => void;
    onRefresh: () => void;
    onShare: (entry: SancaiEntryRecord) => void;
    onBatchCandidateGovernance: (entries: SancaiEntryRecord[]) => void;
    onSort: (
        sourceEntry: SancaiEntryRecord,
        targetEntry: SancaiEntryRecord,
        position: KuzhambuTableSortPosition
    ) => void;
    onView: (entry: SancaiEntryRecord) => void;
    volumes: SancaiVolumeRecord[];
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

interface SancaiEntryLifecycleAction {
    ariaLabel: string;
    confirmDescription: string;
    confirmMessage: string;
    confirmTitle: string;
    okText: string;
    successMessage: string;
    targetStatus: SancaiEntryLifecycleStatus;
    text: string;
}

const lifecycleActionMeta: Record<
    SancaiEntryLifecycleStatus,
    {
        confirmDescription: string;
        confirmTitle: string;
        confirmVerb: string;
        okText: string;
        successMessage: string;
        targetStatus: SancaiEntryLifecycleStatus;
        text: string;
    } | null
> = {
    ARCHIVED: {
        confirmDescription: "恢复后条目重新进入已发布治理范围。",
        confirmTitle: "恢复发布三才图会条目",
        confirmVerb: "恢复发布",
        okText: "恢复发布",
        successMessage: "三才图会条目已恢复发布",
        targetStatus: "PUBLISHED",
        text: "恢复发布"
    },
    DRAFT: {
        confirmDescription: "发布后条目进入已发布治理范围，公开或私有仍由可见性字段决定。",
        confirmTitle: "发布三才图会条目",
        confirmVerb: "发布",
        okText: "发布",
        successMessage: "三才图会条目已发布",
        targetStatus: "PUBLISHED",
        text: "发布"
    },
    PUBLISHED: {
        confirmDescription: "下线后条目退出默认已发布治理范围和 portal 展示，但仍可继续编辑。",
        confirmTitle: "下线三才图会条目",
        confirmVerb: "下线",
        okText: "下线",
        successMessage: "三才图会条目已下线",
        targetStatus: "ARCHIVED",
        text: "下线"
    }
};

const getSancaiEntryLifecycleAction = (
    entry: SancaiEntryRecord
): SancaiEntryLifecycleAction | null => {
    const status = entry.lifecycleStatus;
    if (status !== "ARCHIVED" && status !== "DRAFT" && status !== "PUBLISHED") {
        return null;
    }
    const meta = lifecycleActionMeta[status];
    if (!meta) {
        return null;
    }
    const title = readTitle(entry, "条目");
    return {
        ariaLabel: `${meta.text} ${title}`,
        confirmDescription: meta.confirmDescription,
        confirmMessage: `确认${meta.confirmVerb} ${title}？`,
        confirmTitle: meta.confirmTitle,
        okText: meta.okText,
        successMessage: meta.successMessage,
        targetStatus: meta.targetStatus,
        text: meta.text
    };
};

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无摘要";
};

const readVolumeTitle = (entry: SancaiEntryRecord, volumes: SancaiVolumeRecord[]) => {
    const volume = volumes.find((item) => item.id === entry.volumeId);
    return volume ? readTitle(volume, "卷") : `卷 ${entry.volumeId || "-"}`;
};

const statusTagMeta: Record<string, { color: string; label: string }> = {
    ARCHIVED: { color: "default", label: "已下线" },
    DRAFT: { color: "gold", label: "草稿" },
    PUBLISHED: { color: "green", label: "已发布" }
};

const renderStatusTag = (status?: string | null) => {
    const normalizedStatus = status || "UNKNOWN";
    const meta = statusTagMeta[normalizedStatus] ?? {
        color: "blue",
        label: normalizedStatus
    };
    return <Tag color={meta.color}>{meta.label}</Tag>;
};

export const SancaiEntryList = ({
    entries,
    isLoading,
    onChangeLifecycleStatus,
    onDelete,
    onExport,
    onRefresh,
    onShare,
    onBatchCandidateGovernance,
    onSort,
    onView,
    volumes
}: SancaiEntryListProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [selectedRowsState, setSelectedRowsState] = useState<{
        keys: number[];
        scopeKey: string;
    }>({ keys: [], scopeKey: "" });
    const [activeBatchId, setActiveBatchId] = useState<number | null>(null);
    const [batchShareResult, setBatchShareResult] = useState<ClassicsBatchOperationRecord | null>(
        null
    );
    const [batchVisibilityResult, setBatchVisibilityResult] =
        useState<ClassicsBatchOperationRecord | null>(null);
    const canShareEntries = hasClassicsContentPermission("SANCAI_ENTRY", "share", hasPermission);
    const canExportEntries = hasClassicsContentPermission("SANCAI_ENTRY", "export", hasPermission);
    const canChangeEntryVisibility = hasClassicsContentPermission(
        "SANCAI_ENTRY",
        "edit",
        hasPermission
    );

    const activeBatchQuery = useQuery({
        queryKey: ["classics", "sancai", "refinement", "batch", activeBatchId],
        queryFn: () => entryService.getRefinementBatch(activeBatchId ?? 0),
        enabled: activeBatchId !== null,
        retry: false,
        refetchInterval: (query) => {
            const status = query.state.data?.status;
            return status === "PENDING" || status === "RUNNING" ? 3000 : false;
        }
    });

    const createBatchMutation = useMutation({
        mutationFn: entryService.createRefinementBatch,
        onSuccess: (batch) => {
            setActiveBatchId(batch.batchId);
            messageApi.success(`批量任务已创建：${batch.batchId}`);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量任务创建失败");
        }
    });

    const cancelBatchMutation = useMutation({
        mutationFn: entryService.cancelRefinementBatch,
        onSuccess: (batch) => {
            setActiveBatchId(batch.batchId);
            void activeBatchQuery.refetch();
            messageApi.success("批量任务已取消");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量任务取消失败");
        }
    });

    const createBatchShareMutation = useMutation({
        mutationFn: shareService.createBatch,
        onSuccess: (result) => {
            setBatchShareResult(result);
            messageApi.success(
                `批量分享完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量分享创建失败");
        }
    });

    const changeVisibilityBatchMutation = useMutation({
        mutationFn: contentService.changeVisibilityBatch,
        onSuccess: async (result) => {
            setBatchVisibilityResult(result);
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            messageApi.success(
                `批量可见性完成：成功 ${result.successCount}，失败 ${result.failureCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量可见性修改失败");
        }
    });

    const currentPageSelectionScopeKey = useMemo(
        () => entries.map((entry) => entry.id).join("|"),
        [entries]
    );
    const selectedRowKeys = useMemo(
        () =>
            selectedRowsState.scopeKey === currentPageSelectionScopeKey
                ? selectedRowsState.keys
                : [],
        [currentPageSelectionScopeKey, selectedRowsState.keys, selectedRowsState.scopeKey]
    );
    const selectedEntries = useMemo(
        () => entries.filter((entry) => selectedRowKeys.includes(entry.id)),
        [entries, selectedRowKeys]
    );
    const activeBatch = activeBatchQuery.data;
    const canCancelBatch =
        activeBatch?.batchId != null &&
        (activeBatch.status === "PENDING" || activeBatch.status === "RUNNING");

    const startBatch = (capability: "image_analysis" | "visual") => {
        if (!selectedEntries.length) {
            messageApi.warning("请先选择当前页要批量处理的条目");
            return;
        }
        createBatchMutation.mutate({
            scope: "classics",
            capability,
            contentType: "SANCAI_ENTRY",
            totalCount: selectedEntries.length
        });
    };

    const startBatchShare = () => {
        if (!canShareEntries) {
            messageApi.warning("当前账号缺少三才图会分享权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择当前页要批量分享的条目");
            return;
        }
        createBatchShareMutation.mutate({
            privateContentConfirmed: false,
            status: "ACTIVE",
            targets: selectedEntries.map((entry) => ({
                contentId: entry.id,
                contentType: "SANCAI_ENTRY"
            })),
            titlePrefix: "三才图会批量分享 - ",
            visibility: "PUBLIC"
        });
    };

    const changeBatchVisibility = (visibility: "PRIVATE" | "PUBLIC") => {
        if (!canChangeEntryVisibility) {
            messageApi.warning("当前账号缺少三才图会编辑权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择当前页要批量修改可见性的条目");
            return;
        }
        changeVisibilityBatchMutation.mutate({
            contentIds: selectedEntries.map((entry) => entry.id),
            contentType: "SANCAI_ENTRY",
            visibility
        });
    };

    const openBatchCandidateGovernance = () => {
        if (!canChangeEntryVisibility) {
            messageApi.warning("当前账号缺少三才图会编辑权限");
            return;
        }
        if (!selectedEntries.length) {
            messageApi.warning("请先选择当前页要批量治理的条目");
            return;
        }
        onBatchCandidateGovernance(selectedEntries);
    };

    if (isLoading) {
        return <Skeleton active paragraph={{ rows: 7 }} />;
    }

    if (!entries.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无符合筛选条件的条目" />;
    }

    const columns: KuzhambuTableProps<SancaiEntryRecord>["columns"] = [
        {
            title: "条目",
            key: "title",
            width: 220,
            render: (_, entry) => (
                <div className="sancai-entry-title-cell">
                    <a
                        href="#"
                        aria-label={`打开条目 ${readTitle(entry, "条目")}`}
                        onClick={(event) => {
                            event.preventDefault();
                            onView(entry);
                        }}
                    >
                        {readTitle(entry, "条目")}
                    </a>
                </div>
            )
        },
        {
            title: "卷",
            key: "volume",
            width: 180,
            render: (_, entry) => <Text>{readVolumeTitle(entry, volumes)}</Text>
        },
        {
            title: "摘要",
            key: "summary",
            render: (_, entry) => <Text type="secondary">{readEntrySummary(entry)}</Text>
        },
        {
            title: "状态",
            dataIndex: "lifecycleStatus",
            key: "status",
            width: 120,
            render: renderStatusTag
        },
        {
            inlineLimit: 6,
            key: "actions",
            options: (entry) => {
                const lifecycleAction = getSancaiEntryLifecycleAction(entry);
                const viewOrEditText = canChangeEntryVisibility ? "编辑" : "查看";
                return [
                    {
                        key: "view",
                        text: viewOrEditText,
                        ariaLabel: `${viewOrEditText} ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-view-button`,
                        onClick: () => onView(entry)
                    },
                    {
                        key: "share",
                        text: "分享",
                        ariaLabel: `分享 ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-share-button`,
                        disabled: !canShareEntries,
                        onClick: () => onShare(entry)
                    },
                    {
                        key: "export",
                        text: "导出",
                        ariaLabel: `导出 ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-export-button`,
                        disabled: !canExportEntries,
                        onClick: () => onExport(entry)
                    },
                    ...(lifecycleAction
                        ? [
                              {
                                  key: "lifecycle",
                                  text: lifecycleAction.text,
                                  ariaLabel: lifecycleAction.ariaLabel,
                                  testId: `sancai-entry-${entry.id}-lifecycle-button`,
                                  disabled: !canChangeEntryVisibility,
                                  onClick: () => onChangeLifecycleStatus(entry, lifecycleAction)
                              }
                          ]
                        : []),
                    { type: "divider" as const },
                    {
                        type: "danger" as const,
                        key: "delete",
                        text: "删除",
                        ariaLabel: `删除 ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-delete-button`,
                        onClick: () => onDelete(entry)
                    }
                ];
            }
        }
    ];

    return (
        <div className="sancai-entry-table-wrap">
            <KuzhambuSpace
                wrap
                style={{ display: "flex", justifyContent: "space-between", marginBottom: 12 }}
            >
                <KuzhambuSpace wrap>
                    <Text type="secondary">当前页已选 {selectedEntries.length} 条</Text>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-action-button"
                        disabled={!selectedEntries.length}
                        loading={createBatchMutation.isPending}
                        onClick={() => startBatch("image_analysis")}
                    >
                        图片理解
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-action-button-2"
                        disabled={!selectedEntries.length}
                        loading={createBatchMutation.isPending}
                        onClick={() => startBatch("visual")}
                    >
                        视觉处理
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-share-button"
                        disabled={!selectedEntries.length || !canShareEntries}
                        loading={createBatchShareMutation.isPending}
                        onClick={startBatchShare}
                    >
                        分享
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-action-button-3"
                        disabled={!selectedEntries.length || !canChangeEntryVisibility}
                        loading={changeVisibilityBatchMutation.isPending}
                        onClick={() => changeBatchVisibility("PUBLIC")}
                    >
                        公开
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-action-button-4"
                        disabled={!selectedEntries.length || !canChangeEntryVisibility}
                        loading={changeVisibilityBatchMutation.isPending}
                        onClick={() => changeBatchVisibility("PRIVATE")}
                    >
                        私有
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-action-button-5"
                        disabled={!selectedEntries.length || !canChangeEntryVisibility}
                        onClick={openBatchCandidateGovernance}
                    >
                        候选治理
                    </KuzhambuButton>
                </KuzhambuSpace>
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-refresh-button"
                        onClick={onRefresh}
                    >
                        刷新
                    </KuzhambuButton>
                    {activeBatch ? (
                        <>
                            <Text type="secondary">
                                批量任务 #{activeBatch.batchId} / {activeBatch.capability} /{" "}
                                {activeBatch.status || "UNKNOWN"}
                            </Text>
                            <Text type="secondary">
                                成功 {activeBatch.successCount ?? 0} / 失败{" "}
                                {activeBatch.failedCount ?? 0} / 取消{" "}
                                {activeBatch.cancelledCount ?? 0}
                            </Text>
                            <KuzhambuButton
                                testId="classics-sancai-sancai-entry-action-button-6"
                                disabled={!canCancelBatch}
                                loading={cancelBatchMutation.isPending}
                                onClick={() => {
                                    if (activeBatch.batchId) {
                                        cancelBatchMutation.mutate(activeBatch.batchId);
                                    }
                                }}
                            >
                                取消批量任务
                            </KuzhambuButton>
                        </>
                    ) : null}
                </KuzhambuSpace>
            </KuzhambuSpace>
            {batchShareResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    title={`批量分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
                    description={
                        batchShareResult.failures.length
                            ? batchShareResult.failures
                                  .map(
                                      (item) =>
                                          `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
                                  )
                                  .join("；")
                            : "全部选中条目已创建分享记录。"
                    }
                />
            ) : null}
            {batchVisibilityResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchVisibilityResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    title={`批量可见性结果：成功 ${batchVisibilityResult.successCount}，失败 ${batchVisibilityResult.failureCount}`}
                    description={
                        batchVisibilityResult.failures.length
                            ? batchVisibilityResult.failures
                                  .map(
                                      (item) =>
                                          `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
                                  )
                                  .join("；")
                            : "全部选中条目已更新可见性。"
                    }
                />
            ) : null}
            <KuzhambuTable
                className="sancai-entry-table"
                ariaLabel="三才图会条目表格"
                columns={columns}
                dataSource={entries}
                pagination={{
                    showTotal: (total) => `${total} 条稿件`
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: (keys) =>
                        setSelectedRowsState({
                            keys: keys.map((key) => Number(key)),
                            scopeKey: currentPageSelectionScopeKey
                        })
                }}
                rowKey="id"
                size="middle"
                scroll={{ x: 760 }}
                sortable
                onSort={onSort}
            />
        </div>
    );
};
