import {
    BlockOutlined,
    BoldOutlined,
    OrderedListOutlined,
    UnorderedListOutlined
} from "@ant-design/icons";
import { DatePicker, Form, Input, Segmented, Select, Switch, Typography } from "antd";
import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
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

type WangqiDocumentModelSection =
    "basic" | "summary" | "content" | "refinement" | "tags" | "qa" | "source" | "versions";

export interface WangqiDocumentModelProps {
    document?: WangqiDocumentRecord | null;
    loading?: boolean;
    mode: "create" | "edit";
    open: boolean;
    qaContent?: ReactNode;
    refinementContent?: ReactNode;
    saving?: boolean;
    sourceFileContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    onClose: () => void;
    onSave: (command: WangqiDocumentCommand) => void;
}

interface WangqiMarkdownEditorProps {
    value?: string;
    onChange?: (value: string) => void;
}

const appendMarkdown = (value: string | undefined, markdown: string) => {
    const currentValue = value || "";
    return currentValue ? `${currentValue}\n${markdown}` : markdown;
};

const WangqiMarkdownEditor = ({ value, onChange }: WangqiMarkdownEditorProps) => {
    const insertMarkdown = (markdown: string) => {
        onChange?.(appendMarkdown(value, markdown));
    };

    return (
        <div className="wangqi-markdown-editor" aria-label="王圻 Markdown 编辑器">
            <div className="wangqi-markdown-editor-toolbar">
                <KuzhambuButton
                    testId="classics-wangqi-markdown-heading-button"
                    icon={<Text>H2</Text>}
                    onClick={() => insertMarkdown("## 小标题")}
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-bold-button"
                    icon={<BoldOutlined />}
                    onClick={() => insertMarkdown("**重点文字**")}
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-list-button"
                    icon={<UnorderedListOutlined />}
                    onClick={() => insertMarkdown("- 列表项")}
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-ordered-list-button"
                    icon={<OrderedListOutlined />}
                    onClick={() => insertMarkdown("1. 列表项")}
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-quote-button"
                    icon={<BlockOutlined />}
                    onClick={() => insertMarkdown("> 引文")}
                />
            </div>
            <TextArea
                aria-label="王圻文档正文"
                value={value}
                className="wangqi-markdown-editor-input"
                autoSize={resolveTextAreaAutoSize({ minRows: 18, maxRows: 28 })}
                onChange={(event) => onChange?.(event.target.value)}
            />
        </div>
    );
};

export const WangqiDocumentModel = ({
    document,
    loading = false,
    mode,
    open,
    qaContent,
    refinementContent,
    saving = false,
    sourceFileContent,
    tagContent,
    versionContent,
    onClose,
    onSave
}: WangqiDocumentModelProps) => {
    const [form] = Form.useForm<WangqiDocumentFormValues>();
    const [activeSection, setActiveSection] = useState<WangqiDocumentModelSection>("basic");

    useEffect(() => {
        if (!open) {
            return;
        }
        form.setFieldsValue(toWangqiDocumentFormValues(mode === "edit" ? document : null));
    }, [document, form, mode, open]);

    const closeModel = () => {
        setActiveSection("basic");
        onClose();
    };

    const saveDocument = async () => {
        const values = await form.validateFields();
        onSave(toWangqiDocumentCommand(values, mode === "edit" ? document : null));
    };

    const sectionOptions = [
        { label: "基础信息", value: "basic" },
        { label: "摘要", value: "summary" },
        { label: "正文", value: "content" },
        ...(mode === "edit"
            ? [
                  { label: "内容处理", value: "refinement" },
                  { label: "标签", value: "tags" },
                  { label: "问答", value: "qa" },
                  { label: "文件", value: "source" },
                  { label: "版本", value: "versions" }
              ]
            : [])
    ];

    const basicContent = (
        <>
            <Form.Item
                name="title"
                label="标题"
                rules={[{ required: true, message: "请输入标题" }]}
            >
                <Input aria-label="王圻文档标题" maxLength={120} showCount />
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
                        picker="month"
                        format="YYYY-MM"
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
        </>
    );

    const summaryContent = (
        <Form.Item name="summary" label="摘要">
            <TextArea
                aria-label="王圻文档摘要"
                autoSize={resolveTextAreaAutoSize({ minRows: 12, maxRows: 20 })}
                maxLength={500}
                showCount
            />
        </Form.Item>
    );

    const contentEditor = (
        <Form.Item name="content" label="正文">
            <WangqiMarkdownEditor />
        </Form.Item>
    );

    const sectionContent: Record<WangqiDocumentModelSection, ReactNode> = {
        basic: basicContent,
        summary: summaryContent,
        content: contentEditor,
        refinement: refinementContent || <Text type="secondary">暂无内容处理任务</Text>,
        tags: tagContent || <Text type="secondary">暂无标签</Text>,
        qa: qaContent || <Text type="secondary">暂无问答</Text>,
        source: sourceFileContent || <Text type="secondary">暂无原始文件</Text>,
        versions: versionContent || <Text type="secondary">暂无版本</Text>
    };

    return (
        <KuzhambuDrawer
            title={mode === "create" ? "新增王圻文档" : "编辑王圻文档"}
            open={open}
            size="large"
            loading={loading}
            destroyOnHidden
            extra={
                <Segmented
                    className="wangqi-document-model-sections"
                    options={sectionOptions}
                    value={activeSection}
                    onChange={(value) => setActiveSection(value as WangqiDocumentModelSection)}
                />
            }
            onClose={closeModel}
            footer={
                <div className="wangqi-document-model-footer">
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-document-cancel-button"
                        onClick={closeModel}
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
                <div className="wangqi-document-model-section">{sectionContent[activeSection]}</div>
            </Form>
        </KuzhambuDrawer>
    );
};
