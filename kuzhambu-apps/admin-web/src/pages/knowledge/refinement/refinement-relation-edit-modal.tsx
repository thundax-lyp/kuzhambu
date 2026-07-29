import { Form, Input, InputNumber } from "antd";
import { useEffect } from "react";
import {
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuModal
} from "@/components";

import type { RefinementRelationRecord } from "./refinement-types";

interface RefinementRelationEditModalProps {
    open: boolean;
    saving?: boolean;
    relation?: RefinementRelationRecord | null;
    onCancel: () => void;
    onSubmit: (values: RefinementRelationRecord) => void;
}

export const RefinementRelationEditModal = ({
    open,
    saving = false,
    relation,
    onCancel,
    onSubmit
}: RefinementRelationEditModalProps) => {
    const [form] = Form.useForm<RefinementRelationRecord>();

    useEffect(() => {
        if (open) {
            form.setFieldsValue(relation || {});
        } else {
            form.resetFields();
        }
    }, [form, open, relation]);

    return (
        <KuzhambuModal
            testId="knowledge-refinement-relation-edit-modal"
            confirmLoading={saving}
            forceRender
            open={open}
            title={relation?.draftId ? "编辑关系草稿" : "新增关系草稿"}
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            <KuzhambuForm form={form} onFinish={onSubmit}>
                <KuzhambuFormHiddenItem name="relationId">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormHiddenItem name="relationKey">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem
                    label="源实体 Key"
                    name="sourceEntityKey"
                    rules={[{ required: true }]}
                >
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="目标实体 Key"
                    name="targetEntityKey"
                    rules={[{ required: true }]}
                >
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="源名称" name="sourceName">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="目标名称" name="targetName">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="关系类型" name="relationType" rules={[{ required: true }]}>
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="证据" name="evidence" layoutSize="large">
                    <Input.TextArea rows={3} />
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
