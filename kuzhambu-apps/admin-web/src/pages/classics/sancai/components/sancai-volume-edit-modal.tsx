import { Form, Input, Select } from "antd";
import { useState } from "react";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import type { DictItem } from "@/types/dict";
import { toVolumeFormValues, type SancaiVolumeFormValues } from "./sancai-form-values";
import type { SancaiCategoryRecord, SancaiVolumeRecord } from "../sancai-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

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
        <Form
            aria-label={volume ? "编辑卷目" : "新增卷目"}
            className="sancai-category-edit-modal sancai-editor-form"
            component="div"
            labelCol={{ flex: "72px" }}
            layout="horizontal"
        >
            <Form.Item label="卷目标题">
                <Input
                    aria-label="三才图会卷目标题"
                    placeholder="卷目标题"
                    value={form.title}
                    onChange={(event) => onChange({ title: event.target.value })}
                />
            </Form.Item>
            <Form.Item label="所属门类">
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
            </Form.Item>
            <Form.Item label="卷目类型">
                <Select
                    aria-label="三才图会卷目类型"
                    value={form.volumeType}
                    options={volumeTypeItems}
                    onChange={(volumeType) => onChange({ volumeType })}
                />
            </Form.Item>
        </Form>
    );
};
