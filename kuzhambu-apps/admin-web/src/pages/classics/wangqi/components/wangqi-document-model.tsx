import { DatePicker, Form, Input, InputNumber, Select, Switch, Typography } from "antd";
import { useEffect } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuRichContentViewer } from "@/components/kuzhambu-rich-content-viewer";
import {
    toWangqiDocumentCommand,
    toWangqiDocumentFormValues,
    type WangqiDocumentFormValues
} from "./wangqi-document-form-values";
import type { WangqiDocumentCommand } from "../wangqi-service";
import type { WangqiDocumentRecord } from "../wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;
const { TextArea } = Input;

export interface WangqiDocumentModelProps {
    afterForm?: ReactNode;
    document?: WangqiDocumentRecord | null;
    loading?: boolean;
    mode: "create" | "edit";
    open: boolean;
    saving?: boolean;
    onClose: () => void;
    onSave: (command: WangqiDocumentCommand) => void;
}

export const WangqiDocumentModel = ({
    afterForm,
    document,
    loading = false,
    mode,
    open,
    saving = false,
    onClose,
    onSave
}: WangqiDocumentModelProps) => {
    const [form] = Form.useForm<WangqiDocumentFormValues>();
    const content = Form.useWatch("content", form);
    const contentFormat = Form.useWatch("contentFormat", form);
    const storageObjectId = Form.useWatch("storageObjectId", form);

    useEffect(() => {
        if (!open) {
            return;
        }
        form.setFieldsValue(toWangqiDocumentFormValues(mode === "edit" ? document : null));
    }, [document, form, mode, open]);

    const saveDocument = async () => {
        const values = await form.validateFields();
        onSave(toWangqiDocumentCommand(values, mode === "edit" ? document : null));
    };

    return (
        <KuzhambuDrawer
            title={mode === "create" ? "新增王圻文档" : "编辑王圻文档"}
            open={open}
            size="middle"
            loading={loading}
            destroyOnHidden
            onClose={onClose}
            footer={
                <div className="wangqi-document-model-footer">
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-document-cancel-button"
                        onClick={onClose}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-document-create-button"
                        type="primary"
                        loading={saving}
                        onClick={saveDocument}
                    >
                        保存
                    </KuzhambuButton>
                </div>
            }
        >
            <Form<WangqiDocumentFormValues>
                form={form}
                layout="vertical"
                className="wangqi-document-model-form"
            >
                <Form.Item
                    name="title"
                    label="标题"
                    rules={[{ required: true, message: "请输入标题" }]}
                >
                    <Input aria-label="王圻文档标题" maxLength={120} showCount />
                </Form.Item>
                <Form.Item name="summary" label="摘要">
                    <TextArea
                        aria-label="王圻文档摘要"
                        autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                        maxLength={500}
                        showCount
                    />
                </Form.Item>
                <div className="wangqi-document-model-grid">
                    <Form.Item name="contentFormat" label="正文格式">
                        <Select
                            aria-label="王圻文档正文格式"
                            options={[
                                { label: "Markdown", value: "MARKDOWN" },
                                { label: "HTML", value: "HTML" }
                            ]}
                        />
                    </Form.Item>
                    <Form.Item name="documentTime" label="文档时间">
                        <DatePicker
                            aria-label="王圻文档时间"
                            showTime
                            className="wangqi-document-model-date-picker"
                        />
                    </Form.Item>
                    <Form.Item name="isPublic" label="可见性" valuePropName="checked">
                        <Switch
                            aria-label="王圻文档公开状态"
                            checkedChildren="公开"
                            unCheckedChildren="私有"
                        />
                    </Form.Item>
                </div>
                <Form.Item name="content" label="正文">
                    <TextArea
                        aria-label="王圻文档正文"
                        autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 18 })}
                    />
                </Form.Item>
                <section className="wangqi-document-model-preview" aria-label="王圻文档正文预览">
                    <Text strong>正文预览</Text>
                    <KuzhambuRichContentViewer
                        className="wangqi-document-model-preview-content"
                        content={content}
                        format={contentFormat}
                    />
                </section>
                <Form.Item name="storageObjectId" label="原始文件对象 ID">
                    <InputNumber
                        aria-label="王圻原始文件对象 ID"
                        min={1}
                        precision={0}
                        className="wangqi-document-model-storage-input"
                    />
                </Form.Item>
                <section
                    className="wangqi-document-model-source-file"
                    aria-label="王圻原始文件状态"
                >
                    <Text strong>原始文件</Text>
                    <Text type={storageObjectId ? undefined : "secondary"}>
                        {storageObjectId ? `已关联对象 ${storageObjectId}` : "未关联原始文件"}
                    </Text>
                </section>
            </Form>
            {afterForm}
        </KuzhambuDrawer>
    );
};
