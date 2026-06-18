import { ArrowDownOutlined, ArrowUpOutlined, MenuOutlined } from "@ant-design/icons";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { App, Button, Empty, Modal } from "antd";
import type { DragEvent } from "react";
import { useState } from "react";
import * as service from "../sancai-service";
import type { SancaiVolumeRecord } from "../sancai-types";

interface SancaiVolumeSortModelProps {
    onCancel: () => void;
    volumes: SancaiVolumeRecord[];
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readVolumeTypeLabel = (volume: SancaiVolumeRecord) => {
    return volume.volumeType === "AUXILIARY" ? "辅助卷目" : "正式卷目";
};

export const SancaiVolumeSortModel = ({
    onCancel,
    volumes
}: SancaiVolumeSortModelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [sortedVolumes, setSortedVolumes] = useState<SancaiVolumeRecord[]>(() => volumes);
    const [draggedVolumeId, setDraggedVolumeId] = useState<number | null>(null);

    const sortMutation = useMutation({
        mutationFn: service.sortVolumes,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "volumes"] });
            setDraggedVolumeId(null);
            onCancel();
            messageApi.success("三才图会卷目顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目排序保存失败");
        }
    });

    const moveInSortForm = (volumeId: number, direction: -1 | 1) => {
        setSortedVolumes((currentVolumes) => {
            const index = currentVolumes.findIndex((volume) => volume.id === volumeId);
            const nextIndex = index + direction;
            if (index < 0 || nextIndex < 0 || nextIndex >= currentVolumes.length) {
                return currentVolumes;
            }
            const nextVolumes = [...currentVolumes];
            const [volume] = nextVolumes.splice(index, 1);
            nextVolumes.splice(nextIndex, 0, volume);
            return nextVolumes;
        });
    };

    const dropInSortForm = (targetVolumeId: number) => {
        if (draggedVolumeId === null || draggedVolumeId === targetVolumeId) {
            return;
        }
        setSortedVolumes((currentVolumes) => {
            const draggedVolume = currentVolumes.find((volume) => volume.id === draggedVolumeId);
            const targetIndex = currentVolumes.findIndex((volume) => volume.id === targetVolumeId);
            if (!draggedVolume || targetIndex < 0) {
                return currentVolumes;
            }
            const remainingVolumes = currentVolumes.filter((volume) => volume.id !== draggedVolumeId);
            remainingVolumes.splice(targetIndex, 0, draggedVolume);
            return remainingVolumes;
        });
        setDraggedVolumeId(null);
    };

    const persistSort = () => {
        sortMutation.mutate({
            orderedIds: sortedVolumes.map((volume) => volume.id),
            sortDirection: "ASC"
        });
    };

    return (
        <Modal
            title="调整三才图会卷目顺序"
            open
            okText="保存"
            cancelText="取消"
            confirmLoading={sortMutation.isPending}
            onCancel={onCancel}
            onOk={persistSort}
            okButtonProps={{
                "aria-label": "保存三才图会卷目顺序"
            }}
            cancelButtonProps={{
                "aria-label": "取消调整三才图会卷目顺序"
            }}
        >
            <SancaiVolumeSortList
                draggedVolumeId={draggedVolumeId}
                isSubmitting={sortMutation.isPending}
                volumes={sortedVolumes}
                onDragOver={(event) => event.preventDefault()}
                onDragStart={setDraggedVolumeId}
                onDrop={dropInSortForm}
                onMove={moveInSortForm}
            />
        </Modal>
    );
};

const SancaiVolumeSortList = ({
    draggedVolumeId,
    isSubmitting,
    onDragOver,
    onDragStart,
    onDrop,
    onMove,
    volumes
}: {
    draggedVolumeId: number | null;
    isSubmitting: boolean;
    onDragOver: (event: DragEvent<HTMLDivElement>) => void;
    onDragStart: (volumeId: number) => void;
    onDrop: (targetVolumeId: number) => void;
    onMove: (volumeId: number, direction: -1 | 1) => void;
    volumes: SancaiVolumeRecord[];
}) => {
    if (!volumes.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无可排序卷目" />;
    }

    return (
        <div className="sancai-category-sort-list" aria-label="三才图会卷目排序列表">
            {volumes.map((volume, index) => {
                const title = readTitle(volume, "卷目");
                return (
                    <div
                        className={
                            draggedVolumeId === volume.id
                                ? "sancai-category-sort-item sancai-category-sort-item-dragging"
                                : "sancai-category-sort-item"
                        }
                        draggable
                        key={volume.id}
                        role="listitem"
                        aria-label={`卷目排序项 ${title}`}
                        onDragStart={() => onDragStart(volume.id)}
                        onDragOver={onDragOver}
                        onDrop={() => onDrop(volume.id)}
                    >
                        <span className="sancai-category-sort-handle" aria-label={`拖动卷目 ${title}`}>
                            <MenuOutlined />
                        </span>
                        <span className="sancai-category-sort-title">
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
                        <div className="sancai-category-sort-actions">
                            <Button
                                aria-label={`上移卷目 ${title}`}
                                icon={<ArrowUpOutlined />}
                                disabled={isSubmitting || index === 0}
                                size="small"
                                type="text"
                                onClick={() => onMove(volume.id, -1)}
                            />
                            <Button
                                aria-label={`下移卷目 ${title}`}
                                icon={<ArrowDownOutlined />}
                                disabled={isSubmitting || index === volumes.length - 1}
                                size="small"
                                type="text"
                                onClick={() => onMove(volume.id, 1)}
                            />
                        </div>
                    </div>
                );
            })}
        </div>
    );
};
