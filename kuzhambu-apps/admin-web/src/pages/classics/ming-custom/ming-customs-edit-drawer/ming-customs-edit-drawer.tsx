import { Form, Input, Typography } from "antd";
import type { ReactNode } from "react";
import { useEffect } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuRichContentViewer,
    KuzhambuSelect
} from "@/components";
import type { MingCustomsCommand } from "@/pages/classics/ming-custom/ming-custom-service";
import type { MingCustomsRecord } from "@/pages/classics/ming-custom/ming-custom-types";
import type { DictItem } from "@/types/dict";
import "./ming-customs-edit-drawer.css";

const { Text } = Typography;
const { TextArea } = Input;

interface MingCustomsFormValues {
    category: string;
    chapter: string;
    content: string;
    contentFormat: string;
    originalExcerpts: string;
    section: string;
    summary: string;
    title: string;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const toMingCustomsFormValues = (record?: MingCustomsRecord | null): MingCustomsFormValues => ({
    category: record?.category || "",
    chapter: record?.chapter || "",
    content: record?.content || "",
    contentFormat: record?.contentFormat || "MARKDOWN",
    originalExcerpts: record?.originalExcerpts || "",
    section: record?.section || "",
    summary: record?.summary || "",
    title: record?.title || ""
});

const toMingCustomsCommand = (
    values: MingCustomsFormValues,
    record?: MingCustomsRecord | null
): MingCustomsCommand => ({
    id: record?.id,
    title: normalizeText(values.title),
    category: normalizeText(values.category),
    chapter: normalizeText(values.chapter),
    section: normalizeText(values.section),
    summary: normalizeText(values.summary),
    contentFormat: normalizeText(values.contentFormat) || "MARKDOWN",
    content: normalizeText(values.content),
    originalExcerpts: normalizeText(values.originalExcerpts)
});

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
            <KuzhambuForm<MingCustomsFormValues>
                form={form}
                className="ming-customs-edit-drawer-form"
            >
                <KuzhambuFormItem
                    name="title"
                    label="标题"
                    rules={[{ required: true, message: "请输入标题" }]}
                >
                    <Input aria-label="明代习俗标题" maxLength={100} showCount />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="category"
                    label="分类"
                    rules={[{ required: true, message: "请选择分类" }]}
                >
                    <KuzhambuSelect
                        aria-label="明代习俗编辑分类"
                        options={categoryOptions.map((option) => ({
                            label: option.label,
                            value: option.value
                        }))}
                        placeholder="选择分类"
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="chapter" label="章节">
                    <Input aria-label="明代习俗章节" maxLength={100} showCount />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="section" label="小节">
                    <Input aria-label="明代习俗小节" maxLength={100} showCount />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="summary" label="概述" layoutSize="large">
                    <TextArea
                        aria-label="明代习俗概述"
                        autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                        maxLength={500}
                        showCount
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="contentFormat" label="正文格式">
                    <KuzhambuSelect
                        aria-label="明代习俗正文格式"
                        options={[
                            { label: "Markdown", value: "MARKDOWN" },
                            { label: "HTML", value: "HTML" },
                            { label: "纯文本", value: "TEXT" }
                        ]}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="content" label="正文" layoutSize="large">
                    <TextArea
                        aria-label="明代习俗正文"
                        autoSize={resolveTextAreaAutoSize({ minRows: 8, maxRows: 14 })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem colon={false} label="预览" layoutSize="large">
                    <section
                        className="ming-customs-edit-drawer-preview"
                        aria-label="明代习俗正文预览"
                    >
                        <Text strong>正文预览</Text>
                        <KuzhambuRichContentViewer
                            className="ming-customs-edit-drawer-preview-content"
                            content={content}
                            format={contentFormat}
                        />
                    </section>
                </KuzhambuFormItem>
                <KuzhambuFormItem name="originalExcerpts" label="原文摘录" layoutSize="large">
                    <TextArea
                        aria-label="明代习俗原文摘录"
                        autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
            {afterForm}
        </KuzhambuDrawer>
    );
};
