import {
    BlockOutlined,
    BoldOutlined,
    FileTextOutlined,
    OrderedListOutlined,
    UnorderedListOutlined
} from "@ant-design/icons";
import { Markdown } from "@tiptap/markdown";
import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { DatePicker, Form, Input, Select, Switch, Typography } from "antd";
import { useEffect, useMemo } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;
const { TextArea } = Input;

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

interface WangqiDocumentBasicSectionProps {
    mode: "create" | "edit";
    summaryLocked?: boolean;
    onOpenSummaryModal: () => void;
}

export const WangqiDocumentBasicSection = ({
    mode,
    summaryLocked = false,
    onOpenSummaryModal
}: WangqiDocumentBasicSectionProps) => {
    return (
        <div className="wangqi-document-edit-drawer-basic">
            <div className="wangqi-document-edit-drawer-basic-head">
                <Form.Item
                    name="title"
                    label="标题"
                    rules={[{ required: true, message: "请输入标题" }]}
                >
                    <Input aria-label="王圻文档标题" maxLength={120} showCount />
                </Form.Item>
                <div className="wangqi-document-edit-drawer-grid">
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
                            className="wangqi-document-edit-drawer-date-picker"
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
            <div className="wangqi-document-edit-drawer-text-fields">
                <Form.Item label="摘要" className="wangqi-document-edit-drawer-form-item-top">
                    <div className="wangqi-document-summary-field">
                        {mode === "edit" ? (
                            <div className="wangqi-document-summary-field-action">
                                <KuzhambuButton
                                    testId="classics-wangqi-document-summary-ai-button"
                                    type="primary"
                                    ariaLabel="AI 摘要"
                                    icon={<FileTextOutlined />}
                                    onClick={onOpenSummaryModal}
                                >
                                    AI 摘要
                                </KuzhambuButton>
                            </div>
                        ) : null}
                        <Form.Item name="summary" noStyle>
                            <TextArea
                                aria-label="王圻文档摘要"
                                autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                                disabled={summaryLocked}
                                maxLength={500}
                                showCount
                            />
                        </Form.Item>
                    </div>
                </Form.Item>
                <Form.Item
                    name="content"
                    label="正文"
                    className="wangqi-document-edit-drawer-form-item-top"
                >
                    <WangqiRichTextEditor />
                </Form.Item>
            </div>
        </div>
    );
};
