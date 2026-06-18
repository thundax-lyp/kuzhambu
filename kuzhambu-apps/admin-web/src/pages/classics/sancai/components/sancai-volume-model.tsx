import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Input, Modal, Select } from "antd";
import { useState } from "react";
import type { DictItem } from "@/types/dict";
import { toVolumeFormValues, type SancaiVolumeFormValues } from "./sancai-form-values";
import * as service from "../sancai-service";
import type { SancaiCategoryRecord, SancaiVolumeRecord } from "../sancai-types";

interface SancaiVolumeModelProps {
    categories: SancaiCategoryRecord[];
    fallbackCategoryId: number | null;
    onCancel: () => void;
    volume: SancaiVolumeRecord | null;
}

const fallbackVolumeTypeOptions: DictItem[] = [
    { label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" },
    { label: "辅助卷目", type: "SANCAI_VOLUME_TYPE", value: "AUXILIARY" }
];

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

export const SancaiVolumeModel = ({
    categories,
    fallbackCategoryId,
    onCancel,
    volume
}: SancaiVolumeModelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<SancaiVolumeFormValues>(() =>
        toVolumeFormValues(volume ?? undefined, fallbackCategoryId)
    );
    const typesQuery = useQuery<DictItem[]>({
        queryKey: ["classics", "sancai", "volumes", "types"],
        queryFn: service.listVolumeTypes,
        retry: false
    });
    const volumeTypeItems = typesQuery.data?.length
        ? typesQuery.data
        : fallbackVolumeTypeOptions;

    const afterChanged = async () => {
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "volumes"] });
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
        onCancel();
        messageApi.success("三才图会卷目已保存");
    };

    const addMutation = useMutation({
        mutationFn: service.addVolume,
        onSuccess: afterChanged,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目新增失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: service.updateVolume,
        onSuccess: afterChanged,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "卷目更新失败");
        }
    });

    const readValidCategoryId = () => {
        const categoryId = form.categoryId ?? fallbackCategoryId;
        if (!categoryId || !categories.some((category) => category.id === categoryId)) {
            messageApi.warning("请选择有效门类");
            return null;
        }
        return categoryId;
    };

    const persist = () => {
        const categoryId = readValidCategoryId();
        if (!categoryId) {
            return;
        }
        const request = {
            id: volume?.id,
            categoryId,
            title: form.title,
            volumeType: form.volumeType
        };
        if (volume) {
            updateMutation.mutate(request);
            return;
        }
        addMutation.mutate(request);
    };

    return (
        <Modal
            title={volume ? "编辑卷目" : "新增卷目"}
            open
            footer={null}
            destroyOnHidden
            onCancel={onCancel}
        >
            <SancaiVolumeForm
                volume={volume}
                categories={categories}
                form={form}
                isSubmitting={addMutation.isPending || updateMutation.isPending}
                isTypeLoading={typesQuery.isFetching}
                volumeTypeItems={volumeTypeItems}
                onChange={(values) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        ...values
                    }))
                }
                onSubmit={persist}
            />
        </Modal>
    );
};

const SancaiVolumeForm = ({
    categories,
    form,
    isSubmitting,
    isTypeLoading,
    onChange,
    onSubmit,
    volume,
    volumeTypeItems
}: {
    categories: SancaiCategoryRecord[];
    form: SancaiVolumeFormValues;
    isSubmitting: boolean;
    isTypeLoading: boolean;
    onChange: (values: Partial<SancaiVolumeFormValues>) => void;
    onSubmit: () => void;
    volume: SancaiVolumeRecord | null;
    volumeTypeItems: DictItem[];
}) => {
    return (
        <div className="sancai-category-editor" aria-label={volume ? "编辑卷目" : "新增卷目"}>
            <Input
                aria-label="三才图会卷目标题"
                placeholder="卷目标题"
                value={form.title}
                onChange={(event) => onChange({ title: event.target.value })}
            />
            <Select
                aria-label="三才图会卷目所属门类"
                placeholder="所属门类"
                value={form.categoryId}
                options={categories.map((category) => ({
                    label: readTitle(category, "门类"),
                    value: category.id
                }))}
                onChange={(categoryId) => onChange({ categoryId })}
            />
            <Select
                aria-label="三才图会卷目类型"
                value={form.volumeType}
                options={volumeTypeItems}
                loading={isTypeLoading}
                onChange={(volumeType) => onChange({ volumeType })}
            />
            <Button
                className="sancai-modal-submit"
                aria-label={volume ? `保存卷目 ${readTitle(volume, "卷")}` : "保存新增卷目"}
                loading={isSubmitting}
                type="primary"
                onClick={onSubmit}
            >
                保存
            </Button>
        </div>
    );
};
