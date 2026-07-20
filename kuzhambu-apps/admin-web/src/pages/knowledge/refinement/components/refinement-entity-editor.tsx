import { Form, Input, InputNumber } from "antd";
import { useEffect } from "react";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import type { RefinementEntityRecord } from "../refinement-types";

interface RefinementEntityEditorProps {
    open: boolean;
    saving?: boolean;
    entity?: RefinementEntityRecord | null;
    onCancel: () => void;
    onSubmit: (values: RefinementEntityRecord) => void;
}

export const RefinementEntityEditor = ({
    open,
    saving = false,
    entity,
    onCancel,
    onSubmit
}: RefinementEntityEditorProps) => {
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
            testId="knowledge-refinement-entity-editor-modal"
            title={entity?.draftId ? "编辑实体草稿" : "新增实体草稿"}
            open={open}
            confirmLoading={saving}
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            <Form form={form} layout="vertical" onFinish={onSubmit}>
                <Form.Item hidden name="entityId">
                    <Input />
                </Form.Item>
                <Form.Item hidden name="entityKey">
                    <Input />
                </Form.Item>
                <Form.Item hidden name="draftId">
                    <InputNumber />
                </Form.Item>
                <Form.Item
                    label="名称"
                    name="name"
                    rules={[{ required: true, message: "请输入名称" }]}
                >
                    <Input />
                </Form.Item>
                <Form.Item
                    label="实体类型"
                    name="entityType"
                    rules={[{ required: true, message: "请输入实体类型" }]}
                >
                    <Input />
                </Form.Item>
                <Form.Item label="描述" name="description">
                    <Input.TextArea rows={4} />
                </Form.Item>
                <Form.Item label="来源引用 JSON" name="sourceRefsJson">
                    <Input.TextArea rows={3} />
                </Form.Item>
                <Form.Item label="排序" name="sortOrder">
                    <InputNumber min={1} style={{ width: "100%" }} />
                </Form.Item>
            </Form>
        </KuzhambuModal>
    );
};
