import { Empty, Skeleton, Tag, Typography } from "antd";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import type { SancaiEntryRecord, SancaiVolumeRecord } from "../sancai-types";

const { Text } = Typography;

interface SancaiEntryListProps {
    entries: SancaiEntryRecord[];
    isLoading: boolean;
    onDelete: (entry: SancaiEntryRecord) => void;
    onExport: (entry: SancaiEntryRecord) => void;
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
    onShare,
    onSort,
    onView,
    volumes
}: SancaiEntryListProps) => {
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
            <KuzhambuTable
                className="sancai-entry-table"
                ariaLabel="三才图会条目表格"
                columns={columns}
                dataSource={entries}
                pagination={{
                    showTotal: (total) => `${total} 条稿件`
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
