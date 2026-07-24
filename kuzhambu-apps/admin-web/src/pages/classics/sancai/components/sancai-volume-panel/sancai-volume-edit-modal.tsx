import { Input } from "antd";
import { useState } from "react";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import type { DictItem } from "@/types/dict";
import { toVolumeFormValues, type SancaiVolumeFormValues } from "./sancai-volume-form-values";
import type {
    SancaiCategoryRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSelect } from "@/components/kuzhambu-select";

interface SancaiVolumeEditModalProps {
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

export const SancaiVolumeEditModal = ({
    categories,
    fallbackCategoryId,
    isSubmitting,
    onCancel,
    onSubmit,
    volume,
    volumeTypeOptions
}: SancaiVolumeEditModalProps) => {
    const [form, setForm] = useState<SancaiVolumeFormValues>(() =>
        toVolumeFormValues(volume ?? undefined, fallbackCategoryId)
    );

    return (
        <KuzhambuModal
            testId="classics-sancai-sancai-volume-editor-modal"
            title={volume ? "编辑卷目" : "新增卷目"}
            open
            footer={
                <div className="sancai-modal-footer">
                    <KuzhambuButton
                        testId="classics-sancai-sancai-volume-cancel-button"
                        onClick={onCancel}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-volume-action-button"
                        loading={isSubmitting}
                        type="primary"
                        onClick={() => onSubmit(form)}
                    >
                        保存
                    </KuzhambuButton>
                </div>
            }
            destroyOnHidden
            onCancel={onCancel}
        >
            <SancaiVolumeForm
                volume={volume}
                categories={categories}
                form={form}
                volumeTypeItems={volumeTypeOptions}
                onChange={(values) =>
                    setForm((currentForm) => ({
                        ...currentForm,
                        ...values
                    }))
                }
            />
        </KuzhambuModal>
    );
};

const SancaiVolumeForm = ({
    categories,
    form,
    onChange,
    volume,
    volumeTypeItems
}: {
    categories: SancaiCategoryRecord[];
    form: SancaiVolumeFormValues;
    onChange: (values: Partial<SancaiVolumeFormValues>) => void;
    volume: SancaiVolumeRecord | null;
    volumeTypeItems: DictItem[];
}) => {
    return (
        <KuzhambuForm
            aria-label={volume ? "编辑卷目" : "新增卷目"}
            className="sancai-category-edit-modal sancai-editor-form"
            component="div"
        >
            <KuzhambuFormItem label="卷目标题" layoutSize="large">
                <Input
                    aria-label="三才图会卷目标题"
                    placeholder="卷目标题"
                    value={form.title}
                    onChange={(event) => onChange({ title: event.target.value })}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="所属门类" layoutSize="large">
                <KuzhambuSelect
                    aria-label="三才图会卷目所属门类"
                    placeholder="所属门类"
                    value={form.categoryId}
                    options={categories.map((category) => ({
                        label: readTitle(category, "门类"),
                        value: category.id
                    }))}
                    onChange={(categoryId) => onChange({ categoryId })}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="卷目类型" layoutSize="large">
                <KuzhambuSelect
                    aria-label="三才图会卷目类型"
                    value={form.volumeType}
                    options={volumeTypeItems}
                    onChange={(volumeType) => onChange({ volumeType })}
                />
            </KuzhambuFormItem>
        </KuzhambuForm>
    );
};
