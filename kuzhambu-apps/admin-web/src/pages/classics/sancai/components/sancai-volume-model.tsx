import { Button, Input, Modal, Select } from "antd";
import { useState } from "react";
import type { DictItem } from "@/types/dict";
import { toVolumeFormValues, type SancaiVolumeFormValues } from "./sancai-form-values";
import type { SancaiCategoryRecord, SancaiVolumeRecord } from "../sancai-types";

interface SancaiVolumeModelProps {
    categories: SancaiCategoryRecord[];
    fallbackCategoryId: number | null;
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiVolumeFormValues) => void;
    volume: SancaiVolumeRecord | null;
    volumeTypeOptions: DictItem[];
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

export const SancaiVolumeModel = ({
    categories,
    fallbackCategoryId,
    isSubmitting,
    onCancel,
    onSubmit,
    volume,
    volumeTypeOptions
}: SancaiVolumeModelProps) => {
    const [form, setForm] = useState<SancaiVolumeFormValues>(() =>
        toVolumeFormValues(volume ?? undefined, fallbackCategoryId)
    );

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
                isSubmitting={isSubmitting}
                volumeTypeItems={volumeTypeOptions}
                onChange={(values) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        ...values
                    }))
                }
                onSubmit={() => onSubmit(form)}
            />
        </Modal>
    );
};

const SancaiVolumeForm = ({
    categories,
    form,
    isSubmitting,
    onChange,
    onSubmit,
    volume,
    volumeTypeItems
}: {
    categories: SancaiCategoryRecord[];
    form: SancaiVolumeFormValues;
    isSubmitting: boolean;
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
