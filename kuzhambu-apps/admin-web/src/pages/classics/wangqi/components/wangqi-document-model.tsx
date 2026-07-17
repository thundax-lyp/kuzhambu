import {
    BlockOutlined,
    BoldOutlined,
    OrderedListOutlined,
    UnorderedListOutlined
} from "@ant-design/icons";
import { Markdown } from "@tiptap/markdown";
import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { DatePicker, Form, Input, Segmented, Select, Switch, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
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

type WangqiDocumentModelSection = "basic" | "refinement" | "tags" | "qa" | "source" | "versions";

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

interface WangqiRichTextEditorProps {
    value?: string;
    onChange?: (value: string) => void;
}

const WangqiRichTextEditor = ({ value, onChange }: WangqiRichTextEditorProps) => {
    const extensions = useMemo(() => [StarterKit, Markdown], []);
    const editor = useEditor({
        extensions,
        content: value || "",
        contentType: "markdown",
        editorProps: {
            attributes: {
                "aria-label": "王圻文档正文",
                class: "wangqi-rich-text-editor-content"
            }
        },
        immediatelyRender: false,
        onUpdate: ({ editor: currentEditor }) => {
            onChange?.(currentEditor.getMarkdown());
        }
    });

    useEffect(() => {
        if (!editor || value === editor.getMarkdown()) {
            return;
        }
        editor.commands.setContent(value || "", { contentType: "markdown" });
    }, [editor, value]);

    const runCommand = (command: () => void) => {
        command();
        editor?.commands.focus();
    };

    return (
        <div className="wangqi-rich-text-editor" aria-label="王圻 Tiptap 编辑器">
            <div className="wangqi-rich-text-editor-toolbar">
                <KuzhambuButton
                    testId="classics-wangqi-markdown-heading-button"
                    className={
                        editor?.isActive("heading", { level: 2 })
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<Text>H2</Text>}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleHeading({ level: 2 }).run())
                    }
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-bold-button"
                    className={
                        editor?.isActive("bold")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<BoldOutlined />}
                    onClick={() => runCommand(() => editor?.chain().focus().toggleBold().run())}
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-list-button"
                    className={
                        editor?.isActive("bulletList")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<UnorderedListOutlined />}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleBulletList().run())
                    }
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-ordered-list-button"
                    className={
                        editor?.isActive("orderedList")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<OrderedListOutlined />}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleOrderedList().run())
                    }
                />
                <KuzhambuButton
                    testId="classics-wangqi-markdown-quote-button"
                    className={
                        editor?.isActive("blockquote")
                            ? "wangqi-rich-text-editor-toolbar-button-active"
                            : undefined
                    }
                    icon={<BlockOutlined />}
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleBlockquote().run())
                    }
                />
            </div>
            <EditorContent editor={editor} />
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
        <div className="wangqi-document-model-basic">
            <div className="wangqi-document-model-basic-head">
                <Form.Item
                    name="title"
                    label="标题"
                    rules={[{ required: true, message: "请输入标题" }]}
                >
                    <Input aria-label="王圻文档标题" maxLength={120} showCount />
                </Form.Item>
                <div className="wangqi-document-model-grid">
                    <Form.Item name="contentFormat" label="格式">
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
            </div>
            <div className="wangqi-document-model-text-fields">
                <Form.Item
                    name="summary"
                    label="摘要"
                    className="wangqi-document-model-form-item-top"
                >
                    <TextArea
                        aria-label="王圻文档摘要"
                        autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                        maxLength={500}
                        showCount
                    />
                </Form.Item>
                <Form.Item
                    name="content"
                    label="正文"
                    className="wangqi-document-model-form-item-top"
                >
                    <WangqiRichTextEditor />
                </Form.Item>
            </div>
        </div>
    );

    const sectionContent: Record<WangqiDocumentModelSection, ReactNode> = {
        basic: basicContent,
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
                colon={false}
                labelCol={{ flex: "88px" }}
                layout="horizontal"
                className="wangqi-document-model-form"
            >
                <div className="wangqi-document-model-section">{sectionContent[activeSection]}</div>
            </Form>
        </KuzhambuDrawer>
    );
};
