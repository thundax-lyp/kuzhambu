import { Form, Input, InputNumber } from "antd";
import { useEffect } from "react";
import { KuzhambuForm, KuzhambuFormHiddenItem, KuzhambuFormItem } from "@/components/kuzhambu-form";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import type { RefinementEntityRecord } from "../refinement-types";

interface RefinementEntityEditModalProps {
    open: boolean;
    saving?: boolean;
    entity?: RefinementEntityRecord | null;
    onCancel: () => void;
    onSubmit: (values: RefinementEntityRecord) => void;
}

export const RefinementEntityEditModal = ({
    open,
    saving = false,
    entity,
    onCancel,
    onSubmit
}: RefinementEntityEditModalProps) => {
    const [form] = Form.useForm<RefinementEntityRecord>();

    useEffect(() => {
        if (open) {
            form.setFieldsValue(entity || {});
        } else {
            form.resetFields();
        }
    }, [entity, form, open]);

    return (
        <KuzhambuModal
            testId="knowledge-refinement-entity-edit-modal"
            title={entity?.draftId ? "编辑实体草稿" : "新增实体草稿"}
            open={open}
            confirmLoading={saving}
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            <KuzhambuForm form={form} onFinish={onSubmit}>
                <KuzhambuFormHiddenItem name="entityId">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormHiddenItem name="entityKey">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormHiddenItem name="draftId">
                    <InputNumber />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem
                    label="名称"
                    name="name"
                    rules={[{ required: true, message: "请输入名称" }]}
                >
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="实体类型"
                    name="entityType"
                    rules={[{ required: true, message: "请输入实体类型" }]}
                >
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="描述" name="description" layoutSize="large">
                    <Input.TextArea rows={4} />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="来源引用 JSON" name="sourceRefsJson" layoutSize="large">
                    <Input.TextArea rows={3} />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="排序" name="sortOrder">
                    <InputNumber min={1} style={{ width: "100%" }} />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuModal>
    );
};
