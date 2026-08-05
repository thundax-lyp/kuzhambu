import { Form, Input } from "antd";
import { useEffect } from "react";
import { KuzhambuForm, KuzhambuFormItem, KuzhambuModal } from "@/components";
import type { ClassicsContentQaPairRecord } from "../classics-content-types";

interface ClassicsContentQaEditorValues {
    answer: string;
    question: string;
}

interface ClassicsContentQaEditorModalProps {
    confirmLoading?: boolean;
    qaPair?: ClassicsContentQaPairRecord;
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: ClassicsContentQaEditorValues) => Promise<void> | void;
}

export const ClassicsContentQaEditorModal = ({
    confirmLoading = false,
    qaPair,
    open,
    onCancel,
    onSubmit
}: ClassicsContentQaEditorModalProps) => {
    const [form] = Form.useForm<ClassicsContentQaEditorValues>();

    useEffect(() => {
        if (!open) {
            form.resetFields();
            return;
        }

        form.setFieldsValue({
            question: qaPair?.question || "",
            answer: qaPair?.answer || ""
        });
    }, [form, open, qaPair]);

    const submit = async () => {
        const formValues = await form.validateFields();
        await onSubmit({
            question: formValues.question.trim(),
            answer: formValues.answer.trim()
        });
    };

    return (
        <KuzhambuModal
            testId="classics-content-qa-editor-modal"
            destroyOnHidden
            okButtonProps={{
                loading: confirmLoading
            }}
            open={open}
            title={qaPair ? "编辑问答" : "新增问答"}
            onCancel={onCancel}
            onOk={submit}
        >
            <KuzhambuForm form={form} labelWrap>
                <KuzhambuFormItem
                    label="问题"
                    name="question"
                    layoutSize="large"
                    rules={[{ required: true, message: "请输入问题" }]}
                >
                    <Input aria-label="问答问题" placeholder="请输入问题" />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="回答"
                    name="answer"
                    layoutSize="large"
                    rules={[{ required: true, message: "请输入回答" }]}
                >
                    <Input.TextArea aria-label="问答回答" rows={4} placeholder="请输入回答" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuModal>
    );
};
