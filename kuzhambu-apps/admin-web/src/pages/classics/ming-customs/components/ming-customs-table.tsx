import { Typography } from "antd";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuTag
} from "@/components";
import type { ClassicsBatchOperationRecord } from "@/pages/classics/common/classics-content-types";

import type { MingCustomsRecord } from "../ming-customs-types";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    title: 260,
    category: 140,
    chapter: 160,
    section: 140,
    visibility: 120,
    summary: 320
};

const visibilityLabels: Record<string, string> = {
    PUBLIC: "公开",
    PRIVATE: "私有"
};

const visibilityTagType = (visibility?: string | null) => {
    return visibility === "PUBLIC" ? "success" : "neutral";
};

interface MingCustomsTableProps {
    batchShareResult: ClassicsBatchOperationRecord | null;
    batchVisibilityResult: ClassicsBatchOperationRecord | null;
    canChangeEntryVisibility?: boolean;
    canExport?: boolean;
    canShare?: boolean;
    categoryLabels: Record<string, string>;
    dataSource: MingCustomsRecord[];
    loading?: boolean;
    onBatchCandidate: () => void;
    onChangeSelectedVisibility: (visibility: "PRIVATE" | "PUBLIC") => void;
    onDelete: (record: MingCustomsRecord) => void;
    onOpenEdit: (record: MingCustomsRecord) => void;
    onExport: (record: MingCustomsRecord) => void;
    onSelectedEntryIdsChange: (ids: string[]) => void;
    onShare: (record: MingCustomsRecord) => void;
    onShareSelectedEntries: () => void;
    pagination: KuzhambuTableProps<MingCustomsRecord>["pagination"];
    selectedEntryIds: string[];
    sharing?: boolean;
    visibilityChanging?: boolean;
}

const renderBatchResultDescription = (result: ClassicsBatchOperationRecord) => {
    if (!result.failures.length) {
        return "全部选中明代习俗已处理完成。";
    }

    return result.failures
        .map(
            (item) =>
                `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
        )
        .join("；");
};

export const MingCustomsTable = ({
    batchShareResult,
    batchVisibilityResult,
    canChangeEntryVisibility = true,
    canExport = true,
    canShare = true,
    categoryLabels,
    dataSource,
    loading = false,
    onBatchCandidate,
    onChangeSelectedVisibility,
    onDelete,
    onOpenEdit,
    onExport,
    onSelectedEntryIdsChange,
    onShare,
    onShareSelectedEntries,
    pagination,
    selectedEntryIds,
    sharing = false,
    visibilityChanging = false
}: MingCustomsTableProps) => {
    const columns: KuzhambuTableProps<MingCustomsRecord>["columns"] = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.title,
            render: (title: string | null | undefined, record) => (
                <KuzhambuButton
                    testId={`ming-customs-edit-${record.id}-button`}
                    type="link"
                    className="ming-customs-title-link"
                    onClick={() => onOpenEdit(record)}
                >
                    <Text strong>{title || "未命名条目"}</Text>
                </KuzhambuButton>
            )
        },
        {
            title: "分类",
            dataIndex: "category",
            key: "category",
            width: DEFAULT_COLUMN_WIDTHS.category,
            render: (category?: string | null) =>
                category ? (
                    <KuzhambuTag type="info">{categoryLabels[category] ?? category}</KuzhambuTag>
                ) : (
                    "未分类"
                )
        },
        {
            title: "章节",
            dataIndex: "chapter",
            key: "chapter",
            width: DEFAULT_COLUMN_WIDTHS.chapter,
            render: (chapter?: string | null) => chapter || "未填写"
        },
        {
            title: "小节",
            dataIndex: "section",
            key: "section",
            width: DEFAULT_COLUMN_WIDTHS.section,
            render: (section?: string | null) => section || "未填写"
        },
        {
            title: "可见性",
            dataIndex: "visibility",
            key: "visibility",
            width: DEFAULT_COLUMN_WIDTHS.visibility,
            render: (visibility?: string | null) => (
                <KuzhambuTag type={visibilityTagType(visibility)}>
                    {visibility ? (visibilityLabels[visibility] ?? visibility) : "未设置"}
                </KuzhambuTag>
            )
        },
        {
            title: "摘要",
            dataIndex: "summary",
            key: "summary",
            width: DEFAULT_COLUMN_WIDTHS.summary,
            ellipsis: true,
            render: (summary?: string | null) => summary || <Text type="secondary">暂无摘要</Text>
        },
        {
            key: "actions",
            options: (record) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${record.title || "未命名条目"}`,
                    onClick: () => onOpenEdit(record)
                },
                {
                    key: "share",
                    text: "分享",
                    ariaLabel: `分享 ${record.title || "未命名条目"}`,
                    disabled: !canShare,
                    onClick: () => onShare(record)
                },
                {
                    key: "export",
                    text: "导出",
                    ariaLabel: `导出 ${record.title || "未命名条目"}`,
                    disabled: !canExport,
                    onClick: () => onExport(record)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${record.title || "未命名条目"}`,
                    onClick: () => onDelete(record)
                }
            ]
        }
    ];

    return (
        <>
            {batchShareResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    title={`批量分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
                    description={
                        batchShareResult.failures.length
                            ? renderBatchResultDescription(batchShareResult)
                            : "全部选中明代习俗已创建分享记录。"
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
                            ? renderBatchResultDescription(batchVisibilityResult)
                            : "全部选中明代习俗已更新可见性。"
                    }
                />
            ) : null}
            <KuzhambuTable<MingCustomsRecord>
                ariaLabel="明代习俗表格"
                rowKey={(record) => String(record.id ?? "")}
                loading={loading}
                dataSource={dataSource}
                columns={columns}
                pagination={pagination}
                toolbar={{
                    leading: (
                        <KuzhambuSpace wrap>
                            <Text type="secondary">当前页已选 {selectedEntryIds.length} 条</Text>
                        </KuzhambuSpace>
                    ),
                    actions: [
                        {
                            testId: "classics-ming-customs-ming-customs-batch-share-button",
                            title: "分享",
                            disabled: !selectedEntryIds.length || !canShare,
                            loading: sharing,
                            action: onShareSelectedEntries
                        },
                        {
                            testId: "classics-ming-customs-ming-customs-batch-public-button",
                            title: "公开",
                            disabled: !selectedEntryIds.length || !canChangeEntryVisibility,
                            loading: visibilityChanging,
                            action: () => onChangeSelectedVisibility("PUBLIC")
                        },
                        {
                            testId: "classics-ming-customs-ming-customs-batch-private-button",
                            title: "私有",
                            disabled: !selectedEntryIds.length || !canChangeEntryVisibility,
                            loading: visibilityChanging,
                            action: () => onChangeSelectedVisibility("PRIVATE")
                        },
                        {
                            testId: "classics-ming-customs-ming-customs-action-button-2",
                            title: "候选治理",
                            disabled: !selectedEntryIds.length || !canChangeEntryVisibility,
                            action: onBatchCandidate
                        }
                    ]
                }}
                rowSelection={{
                    selectedRowKeys: selectedEntryIds,
                    onChange: (keys) => onSelectedEntryIdsChange(keys.map(String))
                }}
            />
        </>
    );
};
