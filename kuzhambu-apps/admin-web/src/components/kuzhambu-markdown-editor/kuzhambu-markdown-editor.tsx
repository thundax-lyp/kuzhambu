import {
    BlockOutlined,
    BoldOutlined,
    OrderedListOutlined,
    UnorderedListOutlined
} from "@ant-design/icons";
import { Markdown } from "@tiptap/markdown";
import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { Typography } from "antd";
import { useEffect, useMemo } from "react";

import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./kuzhambu-markdown-editor.css";

const { Text } = Typography;

export interface KuzhambuMarkdownEditorProps {
    ariaLabel: string;
    className?: string;
    minHeight?: number;
    testIdPrefix: string;
    value?: string;
    onChange?: (value: string) => void;
}

export const KuzhambuMarkdownEditor = ({
    ariaLabel,
    className,
    minHeight = 240,
    testIdPrefix,
    value,
    onChange
}: KuzhambuMarkdownEditorProps) => {
    const extensions = useMemo(() => [StarterKit, Markdown], []);
    const editor = useEditor({
        extensions,
        content: value || "",
        contentType: "markdown",
        editorProps: {
            attributes: {
                "aria-label": ariaLabel,
                class: "kuzhambu-markdown-editor-content"
            }
        },
        immediatelyRender: false,
        onUpdate: ({ editor: currentEditor }) => onChange?.(currentEditor.getMarkdown())
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

    const rootClassName = ["kuzhambu-markdown-editor", className].filter(Boolean).join(" ");
    const readToolbarButtonClassName = (active?: boolean) =>
        ["kuzhambu-markdown-editor-toolbar-button", active ? "is-active" : undefined]
            .filter(Boolean)
            .join(" ");

    return (
        <div className={rootClassName} aria-label={`${ariaLabel} Markdown 编辑器`}>
            <div className="kuzhambu-markdown-editor-toolbar">
                <KuzhambuButton
                    testId={`${testIdPrefix}-heading-button`}
                    className={readToolbarButtonClassName(
                        editor?.isActive("heading", { level: 2 })
                    )}
                    icon={<Text>H2</Text>}
                    ariaLabel="二级标题"
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleHeading({ level: 2 }).run())
                    }
                />
                <KuzhambuButton
                    testId={`${testIdPrefix}-bold-button`}
                    className={readToolbarButtonClassName(editor?.isActive("bold"))}
                    icon={<BoldOutlined />}
                    ariaLabel="粗体"
                    onClick={() => runCommand(() => editor?.chain().focus().toggleBold().run())}
                />
                <KuzhambuButton
                    testId={`${testIdPrefix}-list-button`}
                    className={readToolbarButtonClassName(editor?.isActive("bulletList"))}
                    icon={<UnorderedListOutlined />}
                    ariaLabel="无序列表"
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleBulletList().run())
                    }
                />
                <KuzhambuButton
                    testId={`${testIdPrefix}-ordered-list-button`}
                    className={readToolbarButtonClassName(editor?.isActive("orderedList"))}
                    icon={<OrderedListOutlined />}
                    ariaLabel="有序列表"
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleOrderedList().run())
                    }
                />
                <KuzhambuButton
                    testId={`${testIdPrefix}-quote-button`}
                    className={readToolbarButtonClassName(editor?.isActive("blockquote"))}
                    icon={<BlockOutlined />}
                    ariaLabel="引用"
                    onClick={() =>
                        runCommand(() => editor?.chain().focus().toggleBlockquote().run())
                    }
                />
            </div>
            <EditorContent editor={editor} style={{ minHeight }} />
        </div>
    );
};
