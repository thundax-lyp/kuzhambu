import DOMPurify from "dompurify";
import { marked } from "marked";
import "./kuzhambu-rich-content-viewer.css";

export type KuzhambuRichContentFormat = "HTML" | "MARKDOWN" | "TEXT";

export interface KuzhambuRichContentViewerProps {
    className?: string;
    content?: string | null;
    format?: KuzhambuRichContentFormat | string | null;
}

const normalizeContent = (content: string) => {
    return Array.from(content)
        .filter((character) => {
            const codePoint = character.codePointAt(0) ?? 0;
            return codePoint === 9 || codePoint === 10 || codePoint === 13 || codePoint >= 32;
        })
        .join("");
};

const escapeHtml = (content: string) => {
    return content
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
};

const renderMarkdown = (content: string) => {
    return marked.parse(content, {
        async: false,
        breaks: true,
        gfm: true
    }) as string;
};

const renderText = (content: string) => {
    return escapeHtml(content).replace(/\n/g, "<br />");
};

const toSanitizedHtml = (content: string, format: KuzhambuRichContentViewerProps["format"]) => {
    const normalizedContent = normalizeContent(content);
    if (format === "HTML") {
        return DOMPurify.sanitize(normalizedContent);
    }
    if (format === "TEXT") {
        return DOMPurify.sanitize(renderText(normalizedContent));
    }
    return DOMPurify.sanitize(renderMarkdown(normalizedContent));
};

// AI NOTE: This is the only shared renderer for user/content-provided rich text.
// Always sanitize rendered HTML here; never bypass it with page-level dangerouslySetInnerHTML.
// Pages choose format and content, but this component owns normalization and XSS-safe rendering.
export const KuzhambuRichContentViewer = ({
    className,
    content,
    format = "MARKDOWN"
}: KuzhambuRichContentViewerProps) => {
    const html = toSanitizedHtml(content ?? "", format);
    const rootClassName = className
        ? `kuzhambu-rich-content-viewer ${className}`
        : "kuzhambu-rich-content-viewer";

    return <div className={rootClassName} dangerouslySetInnerHTML={{ __html: html }} />;
};
