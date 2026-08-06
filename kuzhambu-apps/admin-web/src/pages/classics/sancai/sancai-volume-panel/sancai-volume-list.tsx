import { Empty, Skeleton, Tag } from "antd";
import {
    KuzhambuTable,
    type KuzhambuTableProps,
    type KuzhambuTableSortPosition
} from "@/components";

import type { SancaiVolumeRecord } from "@/pages/classics/sancai/sancai-types";

interface SancaiVolumeListProps {
    isLoading: boolean;
    onDelete: (volume: SancaiVolumeRecord) => void;
    onEdit: (volume: SancaiVolumeRecord) => void;
    onSort: (
        sourceVolume: SancaiVolumeRecord,
        targetVolume: SancaiVolumeRecord,
        position: KuzhambuTableSortPosition
    ) => void;
    onSelect: (volume: SancaiVolumeRecord) => void;
    selectedVolume: SancaiVolumeRecord | null;
    volumes: SancaiVolumeRecord[];
}

const readTitle = (value: { id: string; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readVolumeTypeLabel = (volume: SancaiVolumeRecord) => {
    return volume.volumeType === "AUXILIARY" ? "辅助卷目" : "正式卷目";
};

const readVolumeTypeColor = (volume: SancaiVolumeRecord) => {
    return volume.volumeType === "AUXILIARY" ? "gold" : "green";
};

export const SancaiVolumeList = ({
    isLoading,
    onDelete,
    onEdit,
    onSort,
    onSelect,
    selectedVolume,
    volumes
}: SancaiVolumeListProps) => {
    if (isLoading) {
        return <Skeleton active paragraph={{ rows: 5 }} />;
    }

    if (!volumes.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无卷目" />;
    }

    const columns: KuzhambuTableProps<SancaiVolumeRecord>["columns"] = [
        {
            title: "卷目",
            key: "title",
            render: (_, volume) => {
                const title = readTitle(volume, "卷");
                return (
                    <span className="sancai-volume-main">
                        <a
                            href="#"
                            aria-label={`打开卷目 ${title}`}
                            onClick={(event) => {
                                event.preventDefault();
                                onSelect(volume);
                            }}
                        >
                            {title}
                        </a>
                    </span>
                );
            }
        },
        {
            title: "类型",
            key: "volumeType",
            width: 160,
            render: (_, volume) => (
                <Tag color={readVolumeTypeColor(volume)}>{readVolumeTypeLabel(volume)}</Tag>
            )
        },
        {
            key: "actions",
            options: (volume) => {
                const title = readTitle(volume, "卷");
                return [
                    {
                        key: "edit",
                        text: "编辑",
                        ariaLabel: `编辑卷目 ${title}`,
                        onClick: () => onEdit(volume)
                    },
                    { type: "divider" },
                    {
                        danger: true,
                        key: "delete",
                        text: "删除",
                        ariaLabel: `删除卷目 ${title}`,
                        onClick: () => onDelete(volume)
                    }
                ];
            }
        }
    ];

    return (
        <KuzhambuTable
            className="sancai-volume-table"
            ariaLabel="三才图会卷目表格"
            columns={columns}
            dataSource={volumes}
            pagination={false}
            rowKey="id"
            size="middle"
            sortable
            onSort={onSort}
            onRow={(volume) => ({
                "aria-label": `卷目 ${readTitle(volume, "卷")}`,
                className:
                    volume.id === selectedVolume?.id
                        ? "sancai-volume-table-row sancai-volume-table-row-active"
                        : "sancai-volume-table-row"
            })}
        />
    );
};
