import { Typography } from "antd";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuTag
} from "@/components";

import type {
    MingCustomsPublicationBatchRecord,
    MingCustomsRecord
} from "@/pages/classics/ming-custom/ming-custom-types";
import "./ming-customs-table.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    category: 140,
    chapter: 160,
    section: 140,
    summary: 320
};

const PUBLICATION_STATUS_LABELS: Readonly<Record<string, string>> = {
    DRAFT: "草稿",
    PUBLISHED: "已发布",
    OFFLINE: "已下线",
    ERROR: "发布异常"
};

const PUBLICATION_TRANSITION_STATUS_LABELS: Readonly<Record<string, string>> = {
    PUBLISHING: "发布中",
    OFFLINING: "下线中"
};

const formatPublicationStatus = (status?: string | null) => {
    const normalizedStatus = status || "DRAFT";
    return PUBLICATION_STATUS_LABELS[normalizedStatus] || normalizedStatus;
};

const formatPublicationTransitionStatus = (status: string) => {
    return PUBLICATION_TRANSITION_STATUS_LABELS[status] || status;
};

const isPublicationTransitionActive = (record: MingCustomsRecord) =>
    Boolean(record.transitionStatus && record.transitionStatus !== "NONE");

interface MingCustomsTableProps {
    publicationBatchResult: MingCustomsPublicationBatchRecord | null;
    canChangeEntryPublication?: boolean;
    canExport?: boolean;
    categoryLabels: Record<string, string>;
    dataSource: MingCustomsRecord[];
    loading?: boolean;
    onBatchCandidate: () => void;
    onDelete: (record: MingCustomsRecord) => void;
    onOpenEdit: (record: MingCustomsRecord) => void;
    onPublicationAction: (record: MingCustomsRecord, action: "PUBLISH" | "OFFLINE") => void;
    onPublicationBatch: (action: "PUBLISH" | "OFFLINE") => void;
    onExport: (record: MingCustomsRecord) => void;
    onSelectedEntryIdsChange: (ids: string[]) => void;
    pagination: KuzhambuTableProps<MingCustomsRecord>["pagination"];
    selectedEntryIds: string[];
    publicationChanging?: boolean;
}

export const MingCustomsTable = ({
    publicationBatchResult,
    canChangeEntryPublication = true,
    canExport = true,
    categoryLabels,
    dataSource,
    loading = false,
    onBatchCandidate,
    onDelete,
    onOpenEdit,
    onPublicationAction,
    onPublicationBatch,
    onExport,
    onSelectedEntryIdsChange,
    pagination,
    selectedEntryIds,
    publicationChanging = false
}: MingCustomsTableProps) => {
    const columns: KuzhambuTableProps<MingCustomsRecord>["columns"] = [
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
            title: "稿件",
            dataIndex: "title",
            key: "title",
            minWidth: 260,
            render: (title: string | null | undefined, record) => {
                const displayTitle = title || "未命名条目";
                return (
                    <KuzhambuButton
                        testId={`ming-customs-edit-${record.id}-button`}
                        type="link"
                        className="ming-customs-title-link"
                        disabled={isPublicationTransitionActive(record)}
                        onClick={() => onOpenEdit(record)}
                    >
                        <Text
                            strong
                            className="ming-customs-title-text"
                            ellipsis={{ tooltip: displayTitle }}
                        >
                            {displayTitle}
                        </Text>
                    </KuzhambuButton>
                );
            }
        },
        {
            title: "发布状态",
            key: "publicationStatus",
            width: 140,
            render: (_, record) => (
                <KuzhambuSpace size={4} wrap>
                    <KuzhambuTag
                        type={
                            record.lifecycleStatus === "PUBLISHED"
                                ? "success"
                                : record.lifecycleStatus === "ERROR"
                                  ? "danger"
                                  : "neutral"
                        }
                    >
                        {formatPublicationStatus(record.lifecycleStatus)}
                    </KuzhambuTag>
                    {record.transitionStatus && record.transitionStatus !== "NONE" ? (
                        <KuzhambuTag type="info">
                            {formatPublicationTransitionStatus(record.transitionStatus)}
                        </KuzhambuTag>
                    ) : null}
                </KuzhambuSpace>
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
            options: (record) => {
                const isTransitionActive = isPublicationTransitionActive(record);
                const publicationAction =
                    record.lifecycleStatus === "PUBLISHED" ? "OFFLINE" : "PUBLISH";
                return [
                    {
                        key: "edit",
                        text: "编辑",
                        ariaLabel: `编辑 ${record.title || "未命名条目"}`,
                        disabled: isTransitionActive,
                        onClick: () => onOpenEdit(record)
                    },
                    {
                        key: "publication",
                        text: publicationAction === "PUBLISH" ? "发布" : "下线",
                        ariaLabel: `${publicationAction === "PUBLISH" ? "发布" : "下线"} ${record.title || "未命名条目"}`,
                        disabled: isTransitionActive,
                        onClick: () => onPublicationAction(record, publicationAction)
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
                        disabled: isTransitionActive || record.lifecycleStatus === "PUBLISHED",
                        onClick: () => onDelete(record)
                    }
                ];
            }
        }
    ];

    return (
        <>
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
                            testId: "classics-ming-customs-batch-publish-button",
                            title: "批量发布",
                            disabled: !selectedEntryIds.length || !canChangeEntryPublication,
                            loading: publicationChanging,
                            action: () => onPublicationBatch("PUBLISH")
                        },
                        {
                            testId: "classics-ming-customs-batch-offline-button",
                            title: "批量下线",
                            disabled: !selectedEntryIds.length || !canChangeEntryPublication,
                            loading: publicationChanging,
                            action: () => onPublicationBatch("OFFLINE")
                        },
                        {
                            testId: "classics-ming-customs-ming-customs-action-button-2",
                            title: "候选治理",
                            disabled: !selectedEntryIds.length || !canChangeEntryPublication,
                            action: onBatchCandidate
                        }
                    ]
                }}
                rowSelection={{
                    selectedRowKeys: selectedEntryIds,
                    onChange: (keys) => onSelectedEntryIdsChange(keys.map(String)),
                    getCheckboxProps: (record) => ({
                        disabled: isPublicationTransitionActive(record)
                    })
                }}
            />
        </>
    );
};
