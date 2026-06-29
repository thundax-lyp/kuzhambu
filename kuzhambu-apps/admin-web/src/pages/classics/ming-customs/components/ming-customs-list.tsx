import { Button, Typography } from "antd";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
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

export interface MingCustomsListProps {
    categoryLabels: Record<string, string>;
    dataSource: MingCustomsRecord[];
    loading?: boolean;
    onDelete: (record: MingCustomsRecord) => void;
    onOpenEdit: (record: MingCustomsRecord) => void;
    onExport: (record: MingCustomsRecord) => void;
    onShare: (record: MingCustomsRecord) => void;
    pagination: KuzhambuTableProps<MingCustomsRecord>["pagination"];
}

export const MingCustomsList = ({
    categoryLabels,
    dataSource,
    loading = false,
    onDelete,
    onOpenEdit,
    onExport,
    onShare,
    pagination
}: MingCustomsListProps) => {
    const columns: KuzhambuTableProps<MingCustomsRecord>["columns"] = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: DEFAULT_COLUMN_WIDTHS.title,
            render: (title: string | null | undefined, record) => (
                <Button
                    type="link"
                    className="ming-customs-title-link"
                    aria-label={`编辑明代习俗 ${title || "未命名条目"}`}
                    onClick={() => onOpenEdit(record)}
                >
                    <Text strong>{title || "未命名条目"}</Text>
                </Button>
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
                    onClick: () => onShare(record)
                },
                {
                    key: "export",
                    text: "导出",
                    ariaLabel: `导出 ${record.title || "未命名条目"}`,
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
        <KuzhambuTable<MingCustomsRecord>
            ariaLabel="明代习俗表格"
            rowKey="id"
            loading={loading}
            dataSource={dataSource}
            columns={columns}
            pagination={pagination}
        />
    );
};
