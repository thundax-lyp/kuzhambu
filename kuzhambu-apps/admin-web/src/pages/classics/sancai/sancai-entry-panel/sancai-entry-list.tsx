import { App, Empty, Skeleton, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { isSameId } from "@/types/id";
import {
    KuzhambuAlert,
    KuzhambuTable,
    type KuzhambuTableProps,
    type KuzhambuTableSortPosition,
    KuzhambuSpace
} from "@/components";

import { ClassicsPublicationErrorAlert } from "@/pages/classics/common/classics-publication-error-alert";
import { hasClassicsContentPermission } from "@/pages/classics/common/classics-content-types";
import type {
    SancaiEntryRecord,
    SancaiPublicationBatchRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";

export type SancaiPublicationAction = "PUBLISH" | "OFFLINE";

const { Text } = Typography;

interface SancaiEntryListProps {
    entries: SancaiEntryRecord[];
    isLoading: boolean;
    onPublicationAction: (entry: SancaiEntryRecord, action: SancaiPublicationAction) => void;
    onPublicationBatch: (entries: SancaiEntryRecord[], action: SancaiPublicationAction) => void;
    publicationBatchResult?: SancaiPublicationBatchRecord | null;
    onDelete: (entry: SancaiEntryRecord) => void;
    onExport: (entry: SancaiEntryRecord) => void;
    onRefresh: () => void;
    onBatchCandidateGovernance: (entries: SancaiEntryRecord[]) => void;
    onVisual: (entry: SancaiEntryRecord) => void;
    onSort: (
        sourceEntry: SancaiEntryRecord,
        targetEntry: SancaiEntryRecord,
        position: KuzhambuTableSortPosition
    ) => void;
    onView: (entry: SancaiEntryRecord) => void;
    volumes: SancaiVolumeRecord[];
}

const readTitle = (value: { id: string; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const getPublicationAction = (entry: SancaiEntryRecord): SancaiPublicationAction | null => {
    if (entry.lifecycleStatus === "PUBLISHED") {
        return "OFFLINE";
    }
    if (
        entry.lifecycleStatus === "DRAFT" ||
        entry.lifecycleStatus === "OFFLINE" ||
        entry.lifecycleStatus === "ERROR"
    ) {
        return "PUBLISH";
    }
    return null;
};

const isPublicationTransitionActive = (entry: SancaiEntryRecord) =>
    Boolean(entry.transitionStatus && entry.transitionStatus !== "NONE");

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无摘要";
};

const readVolumeTitle = (entry: SancaiEntryRecord, volumes: SancaiVolumeRecord[]) => {
    const volume = volumes.find((item) => isSameId(item.id, entry.volumeId));
    return volume ? readTitle(volume, "卷") : `卷 ${entry.volumeId || "-"}`;
};

const statusTagMeta: Record<string, { color: string; label: string }> = {
    DRAFT: { color: "gold", label: "草稿" },
    ERROR: { color: "red", label: "异常" },
    OFFLINE: { color: "default", label: "已下线" },
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
    onPublicationAction,
    onPublicationBatch,
    publicationBatchResult,
    onDelete,
    onExport,
    onRefresh,
    onBatchCandidateGovernance,
    onVisual,
    onSort,
    onView,
    volumes
}: SancaiEntryListProps) => {
    const { message: messageApi } = App.useApp();
    const [selectedRowsState, setSelectedRowsState] = useState<{
        keys: string[];
        scopeKey: string;
    }>({ keys: [], scopeKey: "" });
    const canExportEntries = hasClassicsContentPermission("SANCAI_ENTRY", "export", hasPermission);
    const canEditEntries = hasClassicsContentPermission("SANCAI_ENTRY", "edit", hasPermission);
    const canRunVisualProcessing =
        canEditEntries &&
        hasPermission("ai:refinement:edit") &&
        hasPermission("classics:content:edit");

    const currentPageSelectionScopeKey = useMemo(
        () => entries.map((entry) => String(entry.id ?? "")).join("|"),
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
        () =>
            entries.filter(
                (entry) => entry.id != null && selectedRowKeys.includes(String(entry.id))
            ),
        [entries, selectedRowKeys]
    );
    const openBatchCandidateGovernance = () => {
        if (!canEditEntries) {
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
            render: (_, entry) => {
                const title = readTitle(entry, "条目");
                return (
                    <div className="sancai-entry-title-cell">
                        <a
                            href="#"
                            aria-label={`打开条目 ${title}`}
                            onClick={(event) => {
                                event.preventDefault();
                                onView(entry);
                            }}
                        >
                            {title}
                        </a>
                    </div>
                );
            }
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
            width: 320,
            render: (_, entry) => <Text type="secondary">{readEntrySummary(entry)}</Text>
        },
        {
            title: "状态",
            dataIndex: "lifecycleStatus",
            key: "status",
            width: 120,
            render: (_, entry) => (
                <KuzhambuSpace orientation="vertical" size={2}>
                    {renderStatusTag(entry.lifecycleStatus)}
                    {entry.transitionStatus && entry.transitionStatus !== "NONE" ? (
                        <Tag color="processing">
                            {entry.transitionStatus === "PUBLISHING" ? "发布中" : "下线中"}
                        </Tag>
                    ) : null}
                </KuzhambuSpace>
            )
        },
        {
            inlineLimit: 6,
            key: "actions",
            options: (entry) => {
                const publicationAction = getPublicationAction(entry);
                const isTransitionActive = isPublicationTransitionActive(entry);
                const viewOrEditText = canEditEntries && !isTransitionActive ? "编辑" : "查看";
                return [
                    {
                        key: "view",
                        text: viewOrEditText,
                        ariaLabel: `${viewOrEditText} ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-view-button`,
                        onClick: () => onView(entry)
                    },
                    {
                        key: "export",
                        text: "导出",
                        ariaLabel: `导出 ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-export-button`,
                        disabled: !canExportEntries,
                        onClick: () => onExport(entry)
                    },
                    {
                        key: "visual",
                        text: "视觉",
                        ariaLabel: `视觉处理 ${readTitle(entry, "条目")}`,
                        testId: `sancai-entry-${entry.id}-visual-button`,
                        disabled: !canRunVisualProcessing || isTransitionActive,
                        onClick: () => onVisual(entry)
                    },
                    ...(publicationAction
                        ? [
                              {
                                  key: "publication",
                                  text: publicationAction === "PUBLISH" ? "发布" : "下线",
                                  ariaLabel: `${publicationAction === "PUBLISH" ? "发布" : "下线"} ${readTitle(entry, "条目")}`,
                                  testId: `sancai-entry-${entry.id}-lifecycle-button`,
                                  disabled: !canEditEntries || isTransitionActive,
                                  onClick: () => onPublicationAction(entry, publicationAction)
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
                        disabled: isTransitionActive || entry.lifecycleStatus === "PUBLISHED",
                        onClick: () => onDelete(entry)
                    }
                ];
            }
        }
    ];

    return (
        <div className="sancai-entry-table-wrap">
            <ClassicsPublicationErrorAlert items={entries} />
            {publicationBatchResult ? (
                <KuzhambuAlert
                    showIcon
                    type={publicationBatchResult.rejectedCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    title={`批量发布操作：接受 ${publicationBatchResult.acceptedCount}，拒绝 ${publicationBatchResult.rejectedCount}`}
                    description={publicationBatchResult.items
                        .filter((item) => !item.accepted)
                        .map((item) => `#${item.contentId}: ${item.reason || "请求被拒绝"}`)
                        .join("；")}
                />
            ) : null}
            <KuzhambuTable
                className="sancai-entry-table"
                ariaLabel="三才图会条目表格"
                columns={columns}
                dataSource={entries}
                toolbar={{
                    leading: (
                        <KuzhambuSpace wrap>
                            <Text type="secondary">当前页已选 {selectedEntries.length} 条</Text>
                        </KuzhambuSpace>
                    ),
                    actions: [
                        {
                            testId: "classics-sancai-batch-publish-button",
                            title: "批量发布",
                            disabled: !selectedEntries.length || !canEditEntries,
                            action: () => onPublicationBatch(selectedEntries, "PUBLISH")
                        },
                        {
                            testId: "classics-sancai-batch-offline-button",
                            title: "批量下线",
                            disabled: !selectedEntries.length || !canEditEntries,
                            action: () => onPublicationBatch(selectedEntries, "OFFLINE")
                        },
                        {
                            testId: "classics-sancai-sancai-entry-action-button-3",
                            title: "候选治理",
                            disabled: !selectedEntries.length || !canEditEntries,
                            action: openBatchCandidateGovernance
                        },
                        {
                            testId: "classics-sancai-sancai-entry-refresh-button",
                            title: "刷新",
                            action: onRefresh
                        }
                    ]
                }}
                pagination={{
                    showTotal: (total) => `${total} 条稿件`
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: (keys) =>
                        setSelectedRowsState({
                            keys: keys.map(String),
                            scopeKey: currentPageSelectionScopeKey
                        }),
                    getCheckboxProps: (entry) => ({
                        disabled: isPublicationTransitionActive(entry)
                    })
                }}
                rowKey={(entry) => String(entry.id ?? "")}
                size="middle"
                scroll={{ x: 760 }}
                sortable
                onSort={onSort}
            />
        </div>
    );
};
