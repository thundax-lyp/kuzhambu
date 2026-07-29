import { Typography } from "antd";
import { KuzhambuButton, KuzhambuTable, type KuzhambuTableProps, KuzhambuTag } from "@/components";

import type { WangqiDocumentRecord } from "./wangqi-types";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    document: 600,
    documentTime: 180,
    visibility: 120
};

const visibilityLabels: Record<string, string> = {
    PUBLIC: "公开",
    PRIVATE: "私有"
};

const visibilityTagType = (visibility?: string | null) => {
    return visibility === "PUBLIC" ? "success" : "neutral";
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "未填写";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    return `${year}/${month}`;
};

const readPrimaryEventTime = (record: WangqiDocumentRecord) => {
    const event = record.events?.[0];
    return (
        event?.occurredLabel ||
        (event?.occurredAt ? formatDateTime(event.occurredAt) : formatDateTime(record.documentTime))
    );
};

const readSummaryLines = (summary?: string | null) => {
    return (summary || "")
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter(Boolean)
        .slice(0, 3);
};

interface WangqiDocumentTableProps {
    canChangeDocumentVisibility: boolean;
    canExport?: boolean;
    canShare?: boolean;
    dataSource: WangqiDocumentRecord[];
    isBatchSharing: boolean;
    isBatchVisibilityChanging: boolean;
    loading?: boolean;
    onChangeSelectedVisibility: (visibility: "PRIVATE" | "PUBLIC") => void;
    onDelete: (record: WangqiDocumentRecord) => void;
    onExport: (record: WangqiDocumentRecord) => void;
    onOpenEdit: (record: WangqiDocumentRecord) => void;
    onOpenBatchCandidateDrawer: () => void;
    onShare: (record: WangqiDocumentRecord) => void;
    onShareSelectedDocuments: () => void;
    onSelectedDocumentIdsChange: (ids: string[]) => void;
    onSortDirectionChange: (sortDirection: "ASC" | "DESC") => void;
    pagination: KuzhambuTableProps<WangqiDocumentRecord>["pagination"];
    selectedDocumentIds: string[];
    sortDirection: "ASC" | "DESC";
}

export const WangqiDocumentTable = ({
    canChangeDocumentVisibility,
    canExport = true,
    canShare = true,
    dataSource,
    isBatchSharing,
    isBatchVisibilityChanging,
    loading = false,
    onChangeSelectedVisibility,
    onDelete,
    onExport,
    onOpenEdit,
    onOpenBatchCandidateDrawer,
    onShare,
    onShareSelectedDocuments,
    onSelectedDocumentIdsChange,
    onSortDirectionChange,
    pagination,
    selectedDocumentIds,
    sortDirection
}: WangqiDocumentTableProps) => {
    const columns: KuzhambuTableProps<WangqiDocumentRecord>["columns"] = [
        {
            title: "文档",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.document,
            render: (title: string | null | undefined, record) => {
                const summaryLines = readSummaryLines(record.summary);
                return (
                    <span className="wangqi-document-title-cell">
                        <KuzhambuButton
                            testId={`wangqi-document-edit-${record.id}-button`}
                            type="link"
                            className="wangqi-document-title-link"
                            onClick={() => onOpenEdit(record)}
                        >
                            <span className="wangqi-document-title-text">
                                {title || "未命名文档"}
                            </span>
                        </KuzhambuButton>
                        {summaryLines.length ? (
                            <Text type="secondary" className="wangqi-document-summary-preview">
                                {summaryLines.map((line, index) => (
                                    <span
                                        key={`${index}-${line}`}
                                        className="wangqi-document-summary-line"
                                    >
                                        {line}
                                    </span>
                                ))}
                            </Text>
                        ) : null}
                    </span>
                );
            }
        },
        {
            title: "事件时间",
            dataIndex: "documentTime",
            key: "documentTime",
            sorter: true,
            sortDirections: ["descend", "ascend"],
            sortOrder: sortDirection === "ASC" ? "ascend" : "descend",
            width: DEFAULT_COLUMN_WIDTHS.documentTime,
            render: (_value, record) => readPrimaryEventTime(record)
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
            key: "actions",
            options: (record) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${record.title || "未命名文档"}`,
                    onClick: () => onOpenEdit(record)
                },
                {
                    key: "share",
                    text: "分享",
                    ariaLabel: `分享 ${record.title || "未命名文档"}`,
                    disabled: !canShare,
                    onClick: () => onShare(record)
                },
                {
                    key: "export",
                    text: "导出",
                    ariaLabel: `导出 ${record.title || "未命名文档"}`,
                    disabled: !canExport,
                    onClick: () => onExport(record)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${record.title || "未命名文档"}`,
                    onClick: () => onDelete(record)
                }
            ]
        }
    ];

    return (
        <KuzhambuTable<WangqiDocumentRecord>
            ariaLabel="王圻文档表格"
            rowKey={(record) => String(record.id ?? "")}
            loading={loading}
            dataSource={dataSource}
            columns={columns}
            toolbar={{
                leading: (
                    <Text type="secondary">
                        已选 {selectedDocumentIds.length} / 当前页 {dataSource.length}
                    </Text>
                ),
                actions: [
                    {
                        testId: "classics-wangqi-wangqi-batch-share-button",
                        title: "分享文档",
                        disabled: !selectedDocumentIds.length || !canShare,
                        loading: isBatchSharing,
                        action: onShareSelectedDocuments
                    },
                    {
                        testId: "classics-wangqi-wangqi-action-button",
                        title: "候选治理",
                        disabled: !selectedDocumentIds.length || !canChangeDocumentVisibility,
                        action: onOpenBatchCandidateDrawer
                    },
                    {
                        testId: "classics-wangqi-wangqi-batch-public-button",
                        title: "设为公开",
                        disabled: !selectedDocumentIds.length || !canChangeDocumentVisibility,
                        loading: isBatchVisibilityChanging,
                        action: () => onChangeSelectedVisibility("PUBLIC")
                    },
                    {
                        testId: "classics-wangqi-wangqi-batch-private-button",
                        title: "设为私有",
                        disabled: !selectedDocumentIds.length || !canChangeDocumentVisibility,
                        loading: isBatchVisibilityChanging,
                        action: () => onChangeSelectedVisibility("PRIVATE")
                    }
                ]
            }}
            onChange={(_pagination, _filters, sorter) => {
                const activeSorter = Array.isArray(sorter) ? sorter[0] : sorter;
                if (activeSorter?.columnKey !== "documentTime" || !activeSorter.order) {
                    return;
                }
                onSortDirectionChange(activeSorter.order === "ascend" ? "ASC" : "DESC");
            }}
            pagination={pagination}
            rowSelection={{
                selectedRowKeys: selectedDocumentIds,
                onChange: (keys) => onSelectedDocumentIdsChange(keys.map(String))
            }}
        />
    );
};
