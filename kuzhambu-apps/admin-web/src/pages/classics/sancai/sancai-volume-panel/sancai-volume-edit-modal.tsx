import { Form, Input } from "antd";
import type { FormInstance } from "antd";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuModal,
    KuzhambuButton,
    KuzhambuSelect
} from "@/components";

import type { DictItem } from "@/types/dict";
import {
    toVolumeFormValues,
    type SancaiVolumeFormValues
} from "./sancai-volume-edit-modal-form-values";
import type {
    SancaiCategoryRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";

interface SancaiVolumeEditModalProps {
    categories: SancaiCategoryRecord[];
    fallbackCategoryId: string | null;
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiVolumeFormValues) => void;
    volume: SancaiVolumeRecord | null;
    volumeTypeOptions: DictItem[];
}

const readTitle = (value: { id: string; title?: string | null }, fallback: string) => {
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
    const [form] = Form.useForm<SancaiVolumeFormValues>();
    const initialValues = toVolumeFormValues(volume ?? undefined, fallbackCategoryId);

    const submitForm = () => {
        form.validateFields().then((values) => {
            onSubmit(values);
        });
    };

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
                        onClick={submitForm}
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
                initialValues={initialValues}
                volumeTypeItems={volumeTypeOptions}
            />
        </KuzhambuModal>
    );
};

const SancaiVolumeForm = ({
    categories,
    form,
    initialValues,
    volume,
    volumeTypeItems
}: {
    categories: SancaiCategoryRecord[];
    form: FormInstance<SancaiVolumeFormValues>;
    initialValues: SancaiVolumeFormValues;
    volume: SancaiVolumeRecord | null;
    volumeTypeItems: DictItem[];
}) => {
    return (
        <KuzhambuForm
            form={form}
            aria-label={volume ? "编辑卷目" : "新增卷目"}
            className="sancai-category-edit-modal sancai-editor-form"
            component="div"
            itemGap="none"
            mobileItemDisplay="block"
            initialValues={initialValues}
        >
            <KuzhambuFormItem name="title" label="卷目标题" layoutSize="large">
                <Input
                    className="sancai-category-edit-control"
                    aria-label="三才图会卷目标题"
                    placeholder="卷目标题"
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="categoryId" label="所属门类" layoutSize="large">
                <KuzhambuSelect
                    className="sancai-category-edit-control"
                    aria-label="三才图会卷目所属门类"
                    placeholder="所属门类"
                    options={categories.map((category) => ({
                        label: readTitle(category, "门类"),
                        value: category.id
                    }))}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="volumeType" label="卷目类型" layoutSize="large">
                <KuzhambuSelect
                    className="sancai-category-edit-control"
                    aria-label="三才图会卷目类型"
                    options={volumeTypeItems}
                />
            </KuzhambuFormItem>
        </KuzhambuForm>
    );
};
