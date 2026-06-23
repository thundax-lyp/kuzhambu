import { Form, Input, InputNumber, Modal } from "antd";
import { useEffect } from "react";
import type { RefinementRelationRecord } from "../refinement-types";

interface RefinementRelationEditorProps {
    open: boolean;
    saving?: boolean;
    relation?: RefinementRelationRecord | null;
    onCancel: () => void;
    onSubmit: (values: RefinementRelationRecord) => void;
}

export const RefinementRelationEditor = ({
    open,
    saving = false,
    relation,
    onCancel,
    onSubmit
}: RefinementRelationEditorProps) => {
    const [form] = Form.useForm<RefinementRelationRecord>();

    useEffect(() => {
        if (open) {
            form.setFieldsValue(relation || {});
        } else {
            form.resetFields();
        }
    }, [form, open, relation]);

    return (
        <Modal
            title={relation?.draftId ? "编辑关系草稿" : "新增关系草稿"}
            open={open}
            confirmLoading={saving}
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            <Form form={form} layout="vertical" onFinish={onSubmit}>
                <Form.Item hidden name="relationId">
                    <Input />
                </Form.Item>
                <Form.Item hidden name="relationKey">
                    <Input />
                </Form.Item>
                <Form.Item label="源实体 Key" name="sourceEntityKey" rules={[{ required: true }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="目标实体 Key" name="targetEntityKey" rules={[{ required: true }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="源名称" name="sourceName">
                    <Input />
                </Form.Item>
                <Form.Item label="目标名称" name="targetName">
                    <Input />
                </Form.Item>
                <Form.Item label="关系类型" name="relationType" rules={[{ required: true }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="证据" name="evidence">
                    <Input.TextArea rows={3} />
                </Form.Item>
                <Form.Item label="来源引用 JSON" name="sourceRefsJson">
                    <Input.TextArea rows={3} />
                </Form.Item>
                <Form.Item label="排序" name="sortOrder">
                    <InputNumber min={1} style={{ width: "100%" }} />
                </Form.Item>
            </Form>
        </Modal>
    );
};
