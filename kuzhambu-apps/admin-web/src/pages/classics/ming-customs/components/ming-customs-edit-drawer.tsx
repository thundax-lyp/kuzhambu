import { Col, Form, Input, Row, Select, Switch, Typography } from "antd";
import type { ReactNode } from "react";
import { useEffect } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuRichContentViewer } from "@/components/kuzhambu-rich-content-viewer";
import type { DictItem } from "@/types/dict";
import {
    toMingCustomsCommand,
    toMingCustomsFormValues,
    type MingCustomsFormValues
} from "./ming-customs-form-values";
import type { MingCustomsCommand } from "../ming-customs-service";
import type { MingCustomsRecord } from "../ming-customs-types";

const { Text } = Typography;
const { TextArea } = Input;

interface MingCustomsEditDrawerProps {
    categoryOptions: DictItem[];
    afterForm?: ReactNode;
    entry?: MingCustomsRecord | null;
    loading?: boolean;
    mode: "create" | "edit";
    open: boolean;
    saving?: boolean;
    onClose: () => void;
    onSave: (command: MingCustomsCommand) => void;
}

export const MingCustomsEditDrawer = ({
    categoryOptions,
    afterForm,
    entry,
    loading = false,
    mode,
    open,
    saving = false,
    onClose,
    onSave
}: MingCustomsEditDrawerProps) => {
    const [form] = Form.useForm<MingCustomsFormValues>();
    const content = Form.useWatch("content", form);
    const contentFormat = Form.useWatch("contentFormat", form);

    useEffect(() => {
        if (!open) {
            return;
        }
        form.setFieldsValue(toMingCustomsFormValues(mode === "edit" ? entry : null));
    }, [entry, form, mode, open]);

    const saveEntry = async () => {
        const values = await form.validateFields();
        onSave(toMingCustomsCommand(values, mode === "edit" ? entry : null));
    };

    return (
        <KuzhambuDrawer
            testId="classics-ming-customs-ming-customs-editor-drawer"
            title={mode === "create" ? "新增明代习俗" : "编辑明代习俗"}
            open={open}
            size="middle"
            loading={loading}
            destroyOnHidden
            onClose={onClose}
            footerActions={[
                {
                    testId: "classics-ming-customs-ming-customs-cancel-button",
                    title: "取消",
                    action: onClose
                },
                {
                    testId: "classics-ming-customs-ming-customs-create-button",
                    title: "保存",
                    type: "primary",
                    loading: saving,
                    action: saveEntry
                }
            ]}
        >
            <Form<MingCustomsFormValues>
                form={form}
                layout="vertical"
                className="ming-customs-edit-drawer-form"
            >
                <Row gutter={16}>
                    <Col xs={24} md={12}>
                        <Form.Item
                            name="title"
                            label="标题"
                            rules={[{ required: true, message: "请输入标题" }]}
                        >
                            <Input aria-label="明代习俗标题" maxLength={100} showCount />
                        </Form.Item>
                    </Col>
                    <Col xs={24} md={12}>
                        <Form.Item
                            name="category"
                            label="分类"
                            rules={[{ required: true, message: "请选择分类" }]}
                        >
                            <Select
                                aria-label="明代习俗编辑分类"
                                options={categoryOptions.map((option) => ({
                                    label: option.label,
                                    value: option.value
                                }))}
                                placeholder="选择分类"
                            />
                        </Form.Item>
                    </Col>
                    <Col xs={24} md={12}>
                        <Form.Item name="chapter" label="章节">
                            <Input aria-label="明代习俗章节" maxLength={100} showCount />
                        </Form.Item>
                    </Col>
                    <Col xs={24} md={12}>
                        <Form.Item name="section" label="小节">
                            <Input aria-label="明代习俗小节" maxLength={100} showCount />
                        </Form.Item>
                    </Col>
                </Row>
                <Form.Item name="summary" label="概述">
                    <TextArea
                        aria-label="明代习俗概述"
                        autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                        maxLength={500}
                        showCount
                    />
                </Form.Item>
                <Row gutter={16}>
                    <Col xs={24} md={12}>
                        <Form.Item name="contentFormat" label="正文格式">
                            <Select
                                aria-label="明代习俗正文格式"
                                options={[
                                    { label: "Markdown", value: "MARKDOWN" },
                                    { label: "HTML", value: "HTML" },
                                    { label: "纯文本", value: "TEXT" }
                                ]}
                            />
                        </Form.Item>
                    </Col>
                    <Col xs={24} md={12}>
                        <Form.Item name="isPublic" label="可见性" valuePropName="checked">
                            <Switch
                                aria-label="明代习俗公开状态"
                                checkedChildren="公开"
                                unCheckedChildren="私有"
                            />
                        </Form.Item>
                    </Col>
                </Row>
                <Form.Item name="content" label="正文">
                    <TextArea
                        aria-label="明代习俗正文"
                        autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                    />
                </Form.Item>
                <section className="ming-customs-edit-drawer-preview" aria-label="明代习俗正文预览">
                    <Text strong>正文预览</Text>
                    <KuzhambuRichContentViewer
                        className="ming-customs-edit-drawer-preview-content"
                        content={content}
                        format={contentFormat}
                    />
                </section>
                <Form.Item name="originalExcerpts" label="原文摘录">
                    <TextArea
                        aria-label="明代习俗原文摘录"
                        autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                    />
                </Form.Item>
            </Form>
            {afterForm}
        </KuzhambuDrawer>
    );
};
