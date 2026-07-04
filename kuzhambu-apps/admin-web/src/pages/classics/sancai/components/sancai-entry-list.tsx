import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Empty, Skeleton, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import type { ClassicsBatchOperationRecord } from "@/pages/classics/common/classics-content-types";
import * as entryService from "../services/sancai-entry-service";
import type { SancaiEntryRecord, SancaiVolumeRecord } from "../sancai-types";

const { Text } = Typography;

interface SancaiEntryListProps {
    entries: SancaiEntryRecord[];
    isLoading: boolean;
    onDelete: (entry: SancaiEntryRecord) => void;
    onExport: (entry: SancaiEntryRecord) => void;
    onShowcase: (entry: SancaiEntryRecord) => void;
    onShare: (entry: SancaiEntryRecord) => void;
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

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无摘要";
};

const readVolumeTitle = (entry: SancaiEntryRecord, volumes: SancaiVolumeRecord[]) => {
    const volume = volumes.find((item) => item.id === entry.volumeId);
    return volume ? readTitle(volume, "卷") : `卷 ${entry.volumeId || "-"}`;
};

const statusTagMeta: Record<string, { color: string; label: string }> = {
    ARCHIVED: { color: "default", label: "已归档" },
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
    onDelete,
    onExport,
    onShowcase,
    onShare,
    onSort,
    onView,
    volumes
}: SancaiEntryListProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
    const [activeBatchId, setActiveBatchId] = useState<number | null>(null);
    const [batchShareResult, setBatchShareResult] = useState<ClassicsBatchOperationRecord | null>(
        null
    );
    const [batchVisibilityResult, setBatchVisibilityResult] =
        useState<ClassicsBatchOperationRecord | null>(null);

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
            messageApi.warning("请先选择要批量处理的条目");
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
        if (!selectedEntries.length) {
            messageApi.warning("请先选择要批量分享的条目");
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
        if (!selectedEntries.length) {
            messageApi.warning("请先选择要批量修改可见性的条目");
            return;
        }
        changeVisibilityBatchMutation.mutate({
            contentIds: selectedEntries.map((entry) => entry.id),
            contentType: "SANCAI_ENTRY",
            visibility
        });
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
            key: "actions",
            options: (entry) => [
                {
                    key: "share",
                    text: "分享",
                    ariaLabel: `分享 ${readTitle(entry, "条目")}`,
                    onClick: () => onShare(entry)
                },
                {
                    key: "export",
                    text: "导出",
                    ariaLabel: `导出 ${readTitle(entry, "条目")}`,
                    onClick: () => onExport(entry)
                },
                {
                    key: "showcase",
                    text: "生成静态展示",
                    ariaLabel: `生成静态展示 ${readTitle(entry, "条目")}`,
                    onClick: () => onShowcase(entry)
                },
                {
                    key: "view",
                    text: "查看",
                    ariaLabel: `查看 ${readTitle(entry, "条目")}`,
                    onClick: () => onView(entry)
                },
                { type: "divider" },
                {
                    danger: true,
                    key: "delete",
                    text: "删除",
                    ariaLabel: `删除 ${readTitle(entry, "条目")}`,
                    onClick: () => onDelete(entry)
                }
            ]
        }
    ];

    return (
        <div className="sancai-entry-table-wrap">
            <KuzhambuSpace
                wrap
                style={{ display: "flex", justifyContent: "space-between", marginBottom: 12 }}
            >
                <KuzhambuSpace wrap>
                    <Button
                        disabled={!selectedEntries.length}
                        loading={createBatchMutation.isPending}
                        onClick={() => startBatch("image_analysis")}
                    >
                        批量图片理解
                    </Button>
                    <Button
                        disabled={!selectedEntries.length}
                        loading={createBatchMutation.isPending}
                        onClick={() => startBatch("visual")}
                    >
                        批量视觉处理
                    </Button>
                    <Button
                        disabled={!selectedEntries.length}
                        loading={createBatchShareMutation.isPending}
                        onClick={startBatchShare}
                    >
                        批量分享
                    </Button>
                    <Button
                        disabled={!selectedEntries.length}
                        loading={changeVisibilityBatchMutation.isPending}
                        onClick={() => changeBatchVisibility("PUBLIC")}
                    >
                        批量公开
                    </Button>
                    <Button
                        disabled={!selectedEntries.length}
                        loading={changeVisibilityBatchMutation.isPending}
                        onClick={() => changeBatchVisibility("PRIVATE")}
                    >
                        批量私有
                    </Button>
                </KuzhambuSpace>
                {activeBatch ? (
                    <KuzhambuSpace wrap>
                        <Text type="secondary">
                            批量任务 #{activeBatch.batchId} / {activeBatch.capability} /{" "}
                            {activeBatch.status || "UNKNOWN"}
                        </Text>
                        <Text type="secondary">
                            成功 {activeBatch.successCount ?? 0} / 失败{" "}
                            {activeBatch.failedCount ?? 0} / 取消 {activeBatch.cancelledCount ?? 0}
                        </Text>
                        <Button
                            disabled={!canCancelBatch}
                            loading={cancelBatchMutation.isPending}
                            onClick={() => {
                                if (activeBatch.batchId) {
                                    cancelBatchMutation.mutate(activeBatch.batchId);
                                }
                            }}
                        >
                            取消批量任务
                        </Button>
                    </KuzhambuSpace>
                ) : null}
            </KuzhambuSpace>
            {batchShareResult ? (
                <Alert
                    showIcon
                    type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    message={`批量分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
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
                <Alert
                    showIcon
                    type={batchVisibilityResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    message={`批量可见性结果：成功 ${batchVisibilityResult.successCount}，失败 ${batchVisibilityResult.failureCount}`}
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
                    onChange: (keys) => setSelectedRowKeys(keys.map((key) => Number(key)))
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
