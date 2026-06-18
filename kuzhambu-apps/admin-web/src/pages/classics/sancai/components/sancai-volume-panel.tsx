import { MenuOutlined, PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { DictItem } from "@/types/dict";
import type { SancaiVolumeFormValues } from "./sancai-form-values";
import { SancaiVolumeList } from "./sancai-volume-list";
import { SancaiVolumeModel } from "./sancai-volume-model";
import { SancaiVolumeSortModel } from "./sancai-volume-sort-model";
import * as volumeService from "../services/sancai-volume-service";
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

const fallbackVolumeTypeOptions: DictItem[] = [
    { label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" },
    { label: "辅助卷目", type: "SANCAI_VOLUME_TYPE", value: "AUXILIARY" }
];

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
    const typesQuery = useQuery<DictItem[]>({
        queryKey: ["classics", "sancai", "volumes", "types"],
        queryFn: volumeService.listTypes,
        retry: false
    });
    const volumeTypeItems = typesQuery.data?.length ? typesQuery.data : fallbackVolumeTypeOptions;

    const closeModel = () => {
        setIsModelOpen(false);
        setEditingVolume(null);
    };

    const afterChanged = async (message: string) => {
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "volumes"] });
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
        closeModel();
        messageApi.success(message);
    };

    const addMutation = useMutation({
        mutationFn: volumeService.add,
        onSuccess: () => afterChanged("三才图会卷目已新增"),
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目新增失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: volumeService.update,
        onSuccess: () => afterChanged("三才图会卷目已更新"),
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目更新失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: volumeService.deleteById,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "volumes"] });
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            messageApi.success("三才图会卷目已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目删除失败");
        }
    });
    const sortMutation = useMutation({
        mutationFn: volumeService.sort,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "volumes"] });
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            closeSort();
            messageApi.success("三才图会卷目顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目排序保存失败");
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

    const readValidCategoryId = (form: SancaiVolumeFormValues) => {
        const categoryId = form.categoryId ?? selectedCategory?.id ?? null;
        if (!categoryId || !categories.some((category) => category.id === categoryId)) {
            messageApi.warning("请选择有效门类");
            return null;
        }
        return categoryId;
    };

    const submitVolume = (form: SancaiVolumeFormValues) => {
        const categoryId = readValidCategoryId(form);
        if (!categoryId) {
            return;
        }
        const request = {
            id: editingVolume?.id,
            categoryId,
            title: form.title,
            volumeType: form.volumeType
        };
        if (editingVolume) {
            updateMutation.mutate(request);
            return;
        }
        addMutation.mutate(request);
    };

    const confirmDelete = (volume: SancaiVolumeRecord) => {
        confirm.danger({
            title: "删除三才图会卷目",
            message: `确认删除 ${readTitle(volume, "卷")}？`,
            description: "仅空卷目可删除。若该卷下仍有关联条目，接口会拒绝删除。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(volume.id)
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

    const submitSort = (orderedIds: number[]) => {
        sortMutation.mutate({
            orderedIds,
            sortDirection: "ASC"
        });
    };

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
            <div className="sancai-catalog-scroll">
                <SancaiVolumeList
                    isLoading={isLoading}
                    selectedVolume={selectedVolume}
                    volumes={volumes}
                    onDelete={confirmDelete}
                    onEdit={startEdit}
                    onSelect={onSelect}
                />
            </div>
            {isModelOpen ? (
                <SancaiVolumeModel
                    categories={categories}
                    fallbackCategoryId={selectedCategory?.id ?? null}
                    isSubmitting={addMutation.isPending || updateMutation.isPending}
                    volume={editingVolume}
                    volumeTypeOptions={volumeTypeItems}
                    onCancel={closeModel}
                    onSubmit={submitVolume}
                />
            ) : null}
            {isSortOpen ? (
                <SancaiVolumeSortModel
                    isSubmitting={sortMutation.isPending}
                    volumes={volumes}
                    onCancel={closeSort}
                    onSubmit={submitSort}
                />
            ) : null}
        </section>
    );
};
