import { Card, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { KnowledgeSyncItemPageRecord, KnowledgeSyncItemRecord } from "../qa-console-types";
import { KuzhambuSelect } from "@/components/kuzhambu-select";

const { Text } = Typography;

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

interface QaSyncTableProps {
    contentType?: string;
    loading: boolean;
    onContentTypeChange: (value?: string) => void;
    onPageChange: (pageNo: number) => void;
    onQuery: () => void;
    onRebuild: () => void;
    onSyncItem: (record: KnowledgeSyncItemRecord) => void;
    onSyncStatusChange: (value?: string) => void;
    pageData?: KnowledgeSyncItemPageRecord;
    pageNo: number;
    pageSize: number;
    rebuildLoading: boolean;
    syncItems: KnowledgeSyncItemRecord[];
    syncLoading: boolean;
    syncStatus?: string;
}

export const QaSyncTable = ({
    contentType,
    loading,
    onContentTypeChange,
    onPageChange,
    onQuery,
    onRebuild,
    onSyncItem,
    onSyncStatusChange,
    pageData,
    pageNo,
    pageSize,
    rebuildLoading,
    syncItems,
    syncLoading,
    syncStatus
}: QaSyncTableProps) => {
    const columns: ColumnsType<KnowledgeSyncItemRecord> = [
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
            width: 260,
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
            fixed: "right",
            key: "actions",
            render: (_, record) => (
                <KuzhambuButton
                    testId="discovery-qa-console-qa-console-sync-button"
                    loading={syncLoading}
                    onClick={() => onSyncItem(record)}
                    size="small"
                >
                    同步
                </KuzhambuButton>
            )
        }
    ];

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <Card title="知识文档" size="small">
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
                                onChange={onContentTypeChange}
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
                                onChange={onSyncStatusChange}
                                style={{ width: 160 }}
                            />
                        </label>
                        <KuzhambuButton
                            testId="discovery-qa-console-qa-console-query-sync-button"
                            loading={loading}
                            onClick={onQuery}
                            type="primary"
                        >
                            查询
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="discovery-qa-console-qa-console-rebuild-knowledge-base-button"
                            danger
                            loading={rebuildLoading}
                            onClick={onRebuild}
                        >
                            全部同步
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </Card>
            <Card title="同步记录" size="small">
                <Table
                    aria-label="知识同步表格"
                    columns={columns}
                    dataSource={syncItems}
                    pagination={{
                        current: pageData?.pageNo ?? pageNo,
                        onChange: onPageChange,
                        pageSize,
                        showTotal: (total) => `共 ${total} 条`,
                        showSizeChanger: false,
                        total: pageData?.totalCount ?? pageData?.count ?? 0
                    }}
                    rowKey={formatSyncItemKey}
                    scroll={{ x: 900 }}
                    size="small"
                />
            </Card>
        </KuzhambuSpace>
    );
};
