import { Empty, Skeleton, Typography } from "antd";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import type { SancaiEntryRecord, SancaiVolumeRecord } from "../sancai-types";

const { Text } = Typography;

interface SancaiEntryListProps {
    currentPageNo: number;
    currentPageSize: number;
    entries: SancaiEntryRecord[];
    isLoading: boolean;
    onPageChange: (pageNo: number, pageSize: number) => void;
    onView: (entry: SancaiEntryRecord) => void;
    totalCount: number;
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

export const SancaiEntryList = ({
    currentPageNo,
    currentPageSize,
    entries,
    isLoading,
    onPageChange,
    onView,
    totalCount,
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
                    <Text strong>{readTitle(entry, "条目")}</Text>
                    <Text type="secondary">ID {entry.id}</Text>
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
            title: "状态",
            dataIndex: "lifecycleStatus",
            key: "status",
            width: 120,
            render: (status?: string | null) => (
                <span className="sancai-entry-status">{status || "UNKNOWN"}</span>
            )
        },
        {
            title: "摘要",
            key: "summary",
            render: (_, entry) => <Text type="secondary">{readEntrySummary(entry)}</Text>
        },
        {
            key: "actions",
            options: (entry) => [
                {
                    key: "view",
                    text: "查看",
                    ariaLabel: `查看 ${readTitle(entry, "条目")}`,
                    onClick: () => onView(entry)
                }
            ]
        }
    ];

    return (
        <div className="sancai-entry-table-wrap">
            <KuzhambuTable
                className="sancai-entry-table"
                aria-label="三才图会条目表格"
                columns={columns}
                dataSource={entries}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    onChange: onPageChange
                }}
                rowKey="id"
                size="middle"
                scroll={{ x: 760 }}
            />
        </div>
    );
};
