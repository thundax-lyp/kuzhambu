import {
    DeleteOutlined,
    EditOutlined,
    MenuOutlined,
    PlusOutlined
} from "@ant-design/icons";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { App, Button, Empty, Skeleton, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { SancaiVolumeModel } from "./sancai-volume-model";
import { SancaiVolumeSortModel } from "./sancai-volume-sort-model";
import * as service from "../sancai-service";
import type { SancaiCategoryRecord, SancaiVolumeRecord } from "../sancai-types";

const { Text, Title } = Typography;

interface SancaiVolumePanelProps {
    categories: SancaiCategoryRecord[];
    isLoading: boolean;
    onSelect: (volume: SancaiVolumeRecord) => void;
    selectedCategory: SancaiCategoryRecord | null;
    selectedVolume: SancaiVolumeRecord | null;
    volumes: SancaiVolumeRecord[];
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readVolumeTypeLabel = (volume: SancaiVolumeRecord) => {
    return volume.volumeType === "AUXILIARY" ? "辅助卷目" : "正式卷目";
};

export const SancaiVolumePanel = ({
    categories,
    isLoading,
    onSelect,
    selectedCategory,
    selectedVolume,
    volumes
}: SancaiVolumePanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [editingVolume, setEditingVolume] = useState<SancaiVolumeRecord | null>(null);
    const [isModelOpen, setIsModelOpen] = useState(false);
    const [isSortOpen, setIsSortOpen] = useState(false);

    const closeModel = () => {
        setIsModelOpen(false);
        setEditingVolume(null);
    };

    const deleteMutation = useMutation({
        mutationFn: service.removeVolume,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "volumes"] });
            messageApi.success("三才图会卷目已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目删除失败");
        }
    });

    const startCreate = () => {
        if (!selectedCategory) {
            messageApi.warning("请先选择门类");
            return;
        }
        setEditingVolume(null);
        setIsModelOpen(true);
    };

    const startEdit = (volume: SancaiVolumeRecord) => {
        setEditingVolume(volume);
        setIsModelOpen(true);
    };

    const confirmDelete = (volume: SancaiVolumeRecord) => {
        confirm.danger({
            title: "删除三才图会卷目",
            message: `确认删除 ${readTitle(volume, "卷")}？`,
            description: "仅空卷目可删除。若该卷下仍有关联条目，接口会拒绝删除。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync({ id: volume.id })
        });
    };

    const openSort = () => {
        if (!selectedCategory) {
            messageApi.warning("请先选择门类");
            return;
        }
        setIsSortOpen(true);
    };

    const closeSort = () => {
        setIsSortOpen(false);
    };

    const volumeContent = (() => {
        if (isLoading) {
            return <Skeleton active paragraph={{ rows: 5 }} />;
        }
        if (!volumes.length) {
            return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无卷目" />;
        }
        return (
            <div className="sancai-volume-list" aria-label="三才图会卷目">
                {volumes.map((volume) => (
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
                            aria-label={`选择卷目 ${readTitle(volume, "卷")}`}
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
                                <span>{readTitle(volume, "卷")}</span>
                            </span>
                        </button>
                        <div
                            className="sancai-catalog-actions"
                            aria-label={`${readTitle(volume, "卷")} 操作`}
                        >
                            <Button
                                aria-label={`编辑卷目 ${readTitle(volume, "卷")}`}
                                icon={<EditOutlined />}
                                size="small"
                                type="text"
                                onClick={() => startEdit(volume)}
                            />
                            <Button
                                aria-label={`删除卷目 ${readTitle(volume, "卷")}`}
                                danger
                                icon={<DeleteOutlined />}
                                size="small"
                                type="text"
                                onClick={() => confirmDelete(volume)}
                            />
                        </div>
                    </div>
                ))}
            </div>
        );
    })();

    return (
        <section className="sancai-catalog-column">
            <div className="sancai-panel-heading">
                <Title level={3}>卷目</Title>
                <div className="sancai-heading-actions">
                    <Text type="secondary">{volumes.length} 卷</Text>
                    <Button
                        aria-label="调整三才图会卷目顺序"
                        title="调整卷目顺序"
                        icon={<MenuOutlined />}
                        size="small"
                        onClick={openSort}
                    />
                    <Button
                        aria-label="新增三才图会卷目"
                        title="新增卷目"
                        icon={<PlusOutlined />}
                        size="small"
                        onClick={startCreate}
                    />
                </div>
            </div>
            <div className="sancai-catalog-scroll">{volumeContent}</div>
            {isModelOpen ? (
                <SancaiVolumeModel
                    categories={categories}
                    fallbackCategoryId={selectedCategory?.id ?? null}
                    volume={editingVolume}
                    onCancel={closeModel}
                />
            ) : null}
            {isSortOpen ? <SancaiVolumeSortModel volumes={volumes} onCancel={closeSort} /> : null}
        </section>
    );
};
