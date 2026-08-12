import { useMutation, useQuery } from "@tanstack/react-query";
import { App, Tag, Typography } from "antd";
import { useState } from "react";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import * as service from "@/pages/discovery/qa-console/qa-console-service";
import type { KnowledgeSyncItemRecord } from "@/pages/discovery/qa-console/qa-console-types";

const { Text } = Typography;
const DEFAULT_PAGE_SIZE = 10;

const CONTENT_TYPE_OPTIONS = [{ label: "三才图会", value: "SANCAI_ENTRY" }];

const SYNC_STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "已删除", value: "DELETED" },
    { label: "同步中", value: "SYNCING" },
    { label: "待同步", value: "PENDING" }
];

const formatContentType = (value?: string | null) => {
    return CONTENT_TYPE_OPTIONS.find((option) => option.value === value)?.label ?? value ?? "-";
};

const formatSyncStatus = (value?: string | null) => {
    return SYNC_STATUS_OPTIONS.find((option) => option.value === value)?.label ?? value ?? "-";
};

const formatSyncStatusColor = (value?: string | null) => {
    if (value === "SUCCEEDED") {
        return "success";
    }
    if (value === "SYNCING") {
        return "processing";
    }
    if (value === "FAILED") {
        return "error";
    }
    return "default";
};

const formatSyncTitle = (record: KnowledgeSyncItemRecord) => {
    return record.title ?? "-";
};

const formatDate = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const date = typeof value === "number" ? new Date(value) : new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const formatSyncItemKey = (record: KnowledgeSyncItemRecord) => {
    return record.sourceId ?? `${record.contentType ?? "UNKNOWN"}-${record.contentId ?? "0"}`;
};

const isSameSyncQuery = (
    left: service.KnowledgeSyncItemPageQuery,
    right: service.KnowledgeSyncItemPageQuery
) => JSON.stringify(left) === JSON.stringify(right);

export const QaSyncTable = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const [contentType, setContentType] = useState<string | undefined>("SANCAI_ENTRY");
    const [syncStatus, setSyncStatus] = useState<string | undefined>();
    const [query, setQuery] = useState<service.KnowledgeSyncItemPageQuery>({
        contentType: "SANCAI_ENTRY",
        pageNo: 1,
        pageSize: DEFAULT_PAGE_SIZE,
        syncStatus: null
    });
    const syncPageQuery = useQuery({
        queryFn: () => service.pageKnowledgeSyncItems(query),
        queryKey: ["discovery-qa-console", "sync-page", query]
    });
    const syncKnowledgeMutation = useMutation({
        mutationFn: service.updateKnowledge,
        onSuccess: async () => {
            await syncPageQuery.refetch();
            messageApi.success("知识文档同步完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "知识文档同步失败");
        }
    });
    const rebuildMutation = useMutation({
        mutationFn: service.rebuildKnowledge,
        onSuccess: async () => {
            await syncPageQuery.refetch();
            messageApi.success("知识库全量同步已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "知识库全量同步失败");
        }
    });
    const pageData = syncPageQuery.data;
    const syncItems = pageData?.records ?? [];

    const querySyncItems = () => {
        const nextQuery = {
            contentType: contentType?.trim() || undefined,
            pageNo: 1,
            pageSize: DEFAULT_PAGE_SIZE,
            syncStatus: syncStatus?.trim() || null
        };
        if (isSameSyncQuery(query, nextQuery)) {
            void syncPageQuery.refetch();
            return;
        }
        setQuery(nextQuery);
    };

    const confirmRebuild = () => {
        confirm.danger({
            title: "全量同步知识库",
            message: "确认重新同步全部知识文档？",
            description: "该操作会更新知识条目，并删除已不满足同步条件的 Provider 数据。",
            okText: "全部同步",
            onConfirm: () => rebuildMutation.mutateAsync({})
        });
    };

    const columns: KuzhambuTableProps<KnowledgeSyncItemRecord>["columns"] = [
        {
            title: "内容类型",
            dataIndex: "contentType",
            key: "contentType",
            width: 140,
            render: (value?: string | null) => <Tag>{formatContentType(value)}</Tag>
        },
        {
            title: "标题",
            key: "title",
            render: (_, record) => formatSyncTitle(record)
        },
        {
            title: "状态",
            dataIndex: "syncStatus",
            key: "syncStatus",
            width: 120,
            render: (value?: string | null) => (
                <Tag color={formatSyncStatusColor(value)}>{formatSyncStatus(value)}</Tag>
            )
        },
        {
            title: "同步时间",
            dataIndex: "syncedAt",
            key: "syncedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            title: "更新时间",
            dataIndex: "updatedAt",
            key: "updatedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            key: "actions",
            options: (record) => [
                {
                    key: "sync",
                    text: "同步",
                    testId: "discovery-qa-console-qa-console-sync-button",
                    disabled: syncKnowledgeMutation.isPending,
                    onClick: () =>
                        syncKnowledgeMutation.mutate({
                            contentId: record.contentId ?? "",
                            contentType: record.contentType ?? "",
                            currentVersionNo: record.currentVersionNo ?? null
                        })
                }
            ]
        }
    ];

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <KuzhambuCard title="知识文档" size="small">
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    <Text type="secondary">
                        查询同步记录，处理单条失败或过期同步。知识条目、分段和召回配置去
                        FastGPT；批量异常用重建。
                    </Text>
                    <KuzhambuSpace align="end" wrap>
                        <label className="qa-console-form-item">
                            <Text type="secondary">内容类型</Text>
                            <KuzhambuSelect
                                allowClear
                                aria-label="内容类型"
                                options={CONTENT_TYPE_OPTIONS}
                                placeholder="全部类型"
                                value={contentType}
                                onChange={setContentType}
                                style={{ width: 180 }}
                            />
                        </label>
                        <label className="qa-console-form-item">
                            <Text type="secondary">同步状态</Text>
                            <KuzhambuSelect
                                allowClear
                                aria-label="同步状态"
                                options={SYNC_STATUS_OPTIONS}
                                placeholder="全部状态"
                                value={syncStatus}
                                onChange={setSyncStatus}
                                style={{ width: 160 }}
                            />
                        </label>
                        <KuzhambuButton
                            testId="discovery-qa-console-qa-console-query-sync-button"
                            loading={syncPageQuery.isFetching}
                            onClick={querySyncItems}
                            type="primary"
                        >
                            查询
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="discovery-qa-console-qa-console-rebuild-knowledge-base-button"
                            danger
                            loading={rebuildMutation.isPending}
                            onClick={confirmRebuild}
                        >
                            全部同步
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </KuzhambuCard>
            <KuzhambuCard className="qa-console-card-spaced" title="同步记录" size="small">
                <KuzhambuTable
                    ariaLabel="知识同步表格"
                    columns={columns}
                    dataSource={syncItems}
                    pagination={{
                        current: pageData?.pageNo ?? query.pageNo ?? 1,
                        onChange: (pageNo) => setQuery((current) => ({ ...current, pageNo })),
                        pageSize: DEFAULT_PAGE_SIZE,
                        showTotal: (total) => `共 ${total} 条`,
                        showSizeChanger: false,
                        total: pageData?.totalCount ?? pageData?.count ?? 0
                    }}
                    rowKey={formatSyncItemKey}
                    loading={syncPageQuery.isFetching}
                    scroll={{ x: 900 }}
                    size="small"
                />
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
