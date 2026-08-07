import { Form, Input } from "antd";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuModal,
    KuzhambuButton,
    KuzhambuSelect
} from "@/components";

import type { DictItem } from "@/types/dict";
import {
    toCategoryFormValues,
    type SancaiCategoryFormValues
} from "./sancai-category-edit-modal-form-values";
import type { SancaiCategoryRecord } from "@/pages/classics/sancai/sancai-types";

interface SancaiCategoryEditDrawerModalProps {
    category: SancaiCategoryRecord | null;
    categoryTypeOptions: DictItem[];
    isSubmitting: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiCategoryFormValues) => void;
}

export const SancaiCategoryEditDrawerModal = ({
    category,
    categoryTypeOptions,
    isSubmitting,
    onCancel,
    onSubmit
}: SancaiCategoryEditDrawerModalProps) => {
    const [form] = Form.useForm<SancaiCategoryFormValues>();
    const initialValues = toCategoryFormValues(category ?? undefined);

    const submitForm = () => {
        form.validateFields().then((values) => {
            onSubmit(values);
        });
    };

    return (
        <KuzhambuModal
            testId="classics-sancai-sancai-category-edit-modal"
            title={category ? "编辑门类" : "新增门类"}
            open
            footer={
                <div className="sancai-modal-footer">
                    <KuzhambuButton
                        testId="classics-sancai-sancai-category-cancel-button"
                        onClick={onCancel}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-category-action-button"
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
            <KuzhambuForm
                form={form}
                aria-label={category ? "编辑门类" : "新增门类"}
                className="sancai-category-edit-modal sancai-editor-form"
                component="div"
                itemGap="none"
                mobileItemDisplay="block"
                initialValues={initialValues}
            >
                <KuzhambuFormItem name="title" label="门类标题" layoutSize="large">
                    <Input
                        className="sancai-category-edit-control"
                        aria-label="三才图会门类标题"
                        placeholder="门类标题"
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="categoryType" label="门类类型" layoutSize="large">
                    <KuzhambuSelect
                        controlClassName="sancai-category-edit-control"
                        aria-label="三才图会门类类型"
                        options={categoryTypeOptions}
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuModal>
    );
};
