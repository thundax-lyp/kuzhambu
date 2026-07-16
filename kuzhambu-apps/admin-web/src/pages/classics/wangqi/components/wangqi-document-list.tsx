import { Typography } from "antd";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import type { WangqiDocumentRecord } from "../wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    title: 260,
    summary: 340,
    documentTime: 180,
    storageObjectId: 160,
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
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}/${month}/${day}`;
};

export interface WangqiDocumentListProps {
    canExport?: boolean;
    canShare?: boolean;
    dataSource: WangqiDocumentRecord[];
    loading?: boolean;
    onDelete: (record: WangqiDocumentRecord) => void;
    onExport: (record: WangqiDocumentRecord) => void;
    onOpenEdit: (record: WangqiDocumentRecord) => void;
    onShare: (record: WangqiDocumentRecord) => void;
    onSelectedDocumentIdsChange: (ids: number[]) => void;
    onSortDirectionChange: (sortDirection: "ASC" | "DESC") => void;
    pagination: KuzhambuTableProps<WangqiDocumentRecord>["pagination"];
    selectedDocumentIds: number[];
    sortDirection: "ASC" | "DESC";
}

export const WangqiDocumentList = ({
    canExport = true,
    canShare = true,
    dataSource,
    loading = false,
    onDelete,
    onExport,
    onOpenEdit,
    onShare,
    onSelectedDocumentIdsChange,
    onSortDirectionChange,
    pagination,
    selectedDocumentIds,
    sortDirection
}: WangqiDocumentListProps) => {
    const columns: KuzhambuTableProps<WangqiDocumentRecord>["columns"] = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.title,
            render: (title: string | null | undefined, record) => (
                <KuzhambuButton
                    testId={`wangqi-document-edit-${record.id}-button`}
                    type="link"
                    className="wangqi-document-title-link"
                    onClick={() => onOpenEdit(record)}
                >
                    <Text strong>{title || "未命名文档"}</Text>
                </KuzhambuButton>
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
            title: "文档时间",
            dataIndex: "documentTime",
            key: "documentTime",
            sorter: true,
            sortDirections: ["descend", "ascend"],
            sortOrder: sortDirection === "ASC" ? "ascend" : "descend",
            width: DEFAULT_COLUMN_WIDTHS.documentTime,
            render: formatDateTime
        },
        {
            title: "原始文件对象 ID",
            dataIndex: "storageObjectId",
            key: "storageObjectId",
            width: DEFAULT_COLUMN_WIDTHS.storageObjectId,
            render: (storageObjectId?: number | null) =>
                storageObjectId ? storageObjectId : <Text type="secondary">未关联</Text>
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
            rowKey="id"
            loading={loading}
            dataSource={dataSource}
            columns={columns}
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
                onChange: (keys) => onSelectedDocumentIdsChange(keys.map((key) => Number(key)))
            }}
        />
    );
};
