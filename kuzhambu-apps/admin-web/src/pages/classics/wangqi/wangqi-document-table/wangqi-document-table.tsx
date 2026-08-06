import { Typography } from "antd";
import { KuzhambuButton, KuzhambuTable, type KuzhambuTableProps, KuzhambuTag } from "@/components";

import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    document: 600,
    documentTime: 180
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

const isPublicationTransitionActive = (record: WangqiDocumentRecord) =>
    Boolean(record.transitionStatus && record.transitionStatus !== "NONE");

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
    canChangeDocumentPublication: boolean;
    canExport?: boolean;
    dataSource: WangqiDocumentRecord[];
    isPublicationChanging: boolean;
    loading?: boolean;
    onDelete: (record: WangqiDocumentRecord) => void;
    onExport: (record: WangqiDocumentRecord) => void;
    onOpenEdit: (record: WangqiDocumentRecord) => void;
    onOpenBatchCandidateDrawer: () => void;
    onPublicationAction: (record: WangqiDocumentRecord, action: "PUBLISH" | "OFFLINE") => void;
    onPublicationBatch: (action: "PUBLISH" | "OFFLINE") => void;
    onSelectedDocumentIdsChange: (ids: string[]) => void;
    onSortDirectionChange: (sortDirection: "ASC" | "DESC") => void;
    pagination: KuzhambuTableProps<WangqiDocumentRecord>["pagination"];
    selectedDocumentIds: string[];
    sortDirection: "ASC" | "DESC";
}

export const WangqiDocumentTable = ({
    canChangeDocumentPublication,
    canExport = true,
    dataSource,
    isPublicationChanging,
    loading = false,
    onDelete,
    onExport,
    onOpenEdit,
    onOpenBatchCandidateDrawer,
    onPublicationAction,
    onPublicationBatch,
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
                            disabled={isPublicationTransitionActive(record)}
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
            title: "发布状态",
            key: "publicationStatus",
            width: 130,
            render: (_, record) => (
                <span>
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
                </span>
            )
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
                        ariaLabel: `编辑 ${record.title || "未命名文档"}`,
                        disabled: isTransitionActive,
                        onClick: () => onOpenEdit(record)
                    },
                    {
                        key: "publication",
                        text: publicationAction === "PUBLISH" ? "发布" : "下线",
                        ariaLabel: `${publicationAction === "PUBLISH" ? "发布" : "下线"} ${record.title || "未命名文档"}`,
                        disabled: isTransitionActive,
                        onClick: () => onPublicationAction(record, publicationAction)
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
                        disabled: isTransitionActive || record.lifecycleStatus === "PUBLISHED",
                        onClick: () => onDelete(record)
                    }
                ];
            }
        }
    ];

    return (
        <>
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
                            testId: "classics-wangqi-batch-publish-button",
                            title: "批量发布",
                            disabled: !selectedDocumentIds.length || !canChangeDocumentPublication,
                            loading: isPublicationChanging,
                            action: () => onPublicationBatch("PUBLISH")
                        },
                        {
                            testId: "classics-wangqi-batch-offline-button",
                            title: "批量下线",
                            disabled: !selectedDocumentIds.length || !canChangeDocumentPublication,
                            loading: isPublicationChanging,
                            action: () => onPublicationBatch("OFFLINE")
                        },
                        {
                            testId: "classics-wangqi-wangqi-action-button",
                            title: "候选治理",
                            disabled: !selectedDocumentIds.length || !canChangeDocumentPublication,
                            action: onOpenBatchCandidateDrawer
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
                    onChange: (keys) => onSelectedDocumentIdsChange(keys.map(String)),
                    getCheckboxProps: (record) => ({
                        disabled: isPublicationTransitionActive(record)
                    })
                }}
            />
        </>
    );
};
