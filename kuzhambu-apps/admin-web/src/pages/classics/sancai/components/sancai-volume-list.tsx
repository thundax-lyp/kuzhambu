import { DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { Button, Empty, Skeleton } from "antd";
import type { SancaiVolumeRecord } from "../sancai-types";

interface SancaiVolumeListProps {
    isLoading: boolean;
    onDelete: (volume: SancaiVolumeRecord) => void;
    onEdit: (volume: SancaiVolumeRecord) => void;
    onSelect: (volume: SancaiVolumeRecord) => void;
    selectedVolume: SancaiVolumeRecord | null;
    volumes: SancaiVolumeRecord[];
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readVolumeTypeLabel = (volume: SancaiVolumeRecord) => {
    return volume.volumeType === "AUXILIARY" ? "辅助卷目" : "正式卷目";
};

export const SancaiVolumeList = ({
    isLoading,
    onDelete,
    onEdit,
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

    return (
        <div className="sancai-volume-list" aria-label="三才图会卷目">
            {volumes.map((volume) => {
                const title = readTitle(volume, "卷");
                return (
                    <div
                        className={
                            volume.id === selectedVolume?.id
                                ? "sancai-catalog-row sancai-catalog-row-active"
                                : "sancai-catalog-row"
                        }
                        key={volume.id}
                    >
                        <button
                            className="sancai-catalog-item"
                            type="button"
                            aria-label={`选择卷目 ${title}`}
                            aria-pressed={volume.id === selectedVolume?.id}
                            onClick={() => onSelect(volume)}
                        >
                            <span className="sancai-volume-main">
                                <span
                                    className={
                                        volume.volumeType === "AUXILIARY"
                                            ? "sancai-category-type-dot sancai-category-type-dot-auxiliary"
                                            : "sancai-category-type-dot sancai-category-type-dot-formal"
                                    }
                                    aria-label={`卷目类型 ${readVolumeTypeLabel(volume)}`}
                                />
                                <span>{title}</span>
                            </span>
                        </button>
                        <div className="sancai-catalog-actions" aria-label={`${title} 操作`}>
                            <Button
                                aria-label={`编辑卷目 ${title}`}
                                icon={<EditOutlined />}
                                size="small"
                                type="text"
                                onClick={() => onEdit(volume)}
                            />
                            <Button
                                aria-label={`删除卷目 ${title}`}
                                danger
                                icon={<DeleteOutlined />}
                                size="small"
                                type="text"
                                onClick={() => onDelete(volume)}
                            />
                        </div>
                    </div>
                );
            })}
        </div>
    );
};
