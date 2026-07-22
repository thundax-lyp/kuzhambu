import { useSearchParams } from "react-router-dom";
import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { FileText } from "lucide-react";
import { Card } from "@/components/ui/card";
import type { SancaiEntryRecord } from "@/pages/classics/sancai-types";
import * as searchItemService from "./search-item-service";
import type { DiscoverySearchItemModel, DiscoverySearchItemType } from "./search-item-service";
import type { DiscoverySearchPreviewResponse } from "@/pages/discovery/search/search-types";

import "./search-item-page.css";

const readEntryIdParam = (searchParams: URLSearchParams) => {
    const value = Number.parseInt(searchParams.get("id") ?? "", 10);
    return Number.isFinite(value) && value > 0 ? value : null;
};

const readItemTypeParam = (searchParams: URLSearchParams): DiscoverySearchItemType => {
    const value = searchParams.get("type");
    return searchItemService.isDiscoverySearchItemType(value) ? value : "SANCAI_ENTRY";
};

const readTitle = (entry: SancaiEntryRecord) => entry.title?.trim() || `条目 ${entry.id}`;

const readAuthor = (entry: SancaiEntryRecord) => entry.authorName?.trim() || entry.author?.trim();

const readSourceTitle = (entry: SancaiEntryRecord) => entry.sourceTitle?.trim() || "三才图会";

const SEARCH_ITEM_TYPE_LABELS: Record<DiscoverySearchItemType, string> = {
    MING_CUSTOMS: "明代习俗",
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王圻文档"
};

const INTERNAL_TAG_LABELS: Record<string, string> = {
    FESTIVAL: "岁时节令",
    RITUAL: "礼制"
};

const hasChineseText = (value: string) => /[\u3400-\u9fff]/u.test(value);

const isInternalCode = (value: string) => /^[A-Z][A-Z0-9_/-]*$/u.test(value);

const readDisplayTagName = (value?: string | null) => {
    const tagName = value?.trim();
    if (!tagName) {
        return null;
    }
    if (INTERNAL_TAG_LABELS[tagName]) {
        return INTERNAL_TAG_LABELS[tagName];
    }
    if (hasChineseText(tagName)) {
        return tagName;
    }
    return isInternalCode(tagName) ? null : tagName;
};

const readModelTitle = (model: DiscoverySearchItemModel) => {
    switch (model.type) {
        case "SANCAI_ENTRY":
            return readTitle(model.entry);
        case "WANGQI_DOCUMENT":
        case "MING_CUSTOMS":
            return model.preview.title?.trim() || `条目 ${model.preview.contentId ?? ""}`.trim();
    }
};

const escapeHtml = (value: string) =>
    value
        .replace(/&/gu, "&amp;")
        .replace(/</gu, "&lt;")
        .replace(/>/gu, "&gt;")
        .replace(/"/gu, "&quot;")
        .replace(/'/gu, "&#39;");

const renderInlineMarkdown = (value: string) => {
    return escapeHtml(value)
        .replace(/\*\*([^*]+)\*\*/gu, "<strong>$1</strong>")
        .replace(/`([^`]+)`/gu, "<code>$1</code>");
};

const markdownToHtml = (markdown: string) => {
    const blocks: string[] = [];
    const pendingList: string[] = [];
    const flushList = () => {
        if (pendingList.length) {
            blocks.push(`<ul>${pendingList.splice(0).join("")}</ul>`);
        }
    };

    markdown.split(/\r?\n/u).forEach((line) => {
        const trimmedLine = line.trim();
        if (!trimmedLine) {
            flushList();
            return;
        }
        const heading = /^(#{1,3})\s+(.+)$/u.exec(trimmedLine);
        if (heading) {
            flushList();
            const level = heading[1].length + 1;
            blocks.push(`<h${level}>${renderInlineMarkdown(heading[2])}</h${level}>`);
            return;
        }
        const listItem = /^[-*]\s+(.+)$/u.exec(trimmedLine);
        if (listItem) {
            pendingList.push(`<li>${renderInlineMarkdown(listItem[1])}</li>`);
            return;
        }
        flushList();
        blocks.push(`<p>${renderInlineMarkdown(trimmedLine)}</p>`);
    });
    flushList();
    return blocks.join("");
};

const sanitizeHtml = (html: string) => {
    const template = document.createElement("template");
    template.innerHTML = html;
    template.content
        .querySelectorAll("script,style,iframe,object,embed,link,meta")
        .forEach((node) => {
            node.remove();
        });
    template.content.querySelectorAll("*").forEach((element) => {
        Array.from(element.attributes).forEach((attribute) => {
            const name = attribute.name.toLowerCase();
            const value = attribute.value.trim();
            if (name.startsWith("on")) {
                element.removeAttribute(attribute.name);
                return;
            }
            if ((name === "href" || name === "src") && /^javascript:/iu.test(value)) {
                element.removeAttribute(attribute.name);
            }
        });
    });
    return template.innerHTML;
};

const RichTextSection = ({
    html,
    markdown,
    text,
    title
}: {
    html?: string | null;
    markdown?: string | null;
    text?: string | null;
    title: string;
}) => {
    const richHtml = useMemo(() => {
        if (html?.trim()) {
            return sanitizeHtml(html);
        }
        if (markdown?.trim()) {
            return sanitizeHtml(markdownToHtml(markdown));
        }
        return null;
    }, [html, markdown]);
    const textValue = text?.trim();
    if (!richHtml && !textValue) {
        return null;
    }
    return (
        <section className="discovery-search-item-section">
            <h2>{title}</h2>
            {richHtml ? (
                <div
                    className="discovery-search-item-richtext"
                    dangerouslySetInnerHTML={{ __html: richHtml }}
                />
            ) : (
                <p>{textValue}</p>
            )}
        </section>
    );
};

const renderSearchItem = (model: DiscoverySearchItemModel) => {
    switch (model.type) {
        case "SANCAI_ENTRY":
            return <SancaiEntryRenderer entry={model.entry} />;
        case "WANGQI_DOCUMENT":
        case "MING_CUSTOMS":
            return <SearchPreviewRenderer preview={model.preview} type={model.type} />;
    }
};

const SearchPreviewRenderer = ({
    preview,
    type
}: {
    preview: DiscoverySearchPreviewResponse;
    type: Exclude<DiscoverySearchItemType, "SANCAI_ENTRY">;
}) => {
    const metaLabel =
        readDisplayTagName(preview.categoryName) ?? readDisplayTagName(preview.categoryCode);
    const tagNames = Array.from(
        new Set(
            (preview.tagNames ?? [])
                .map((tagName) => readDisplayTagName(tagName))
                .filter((tagName): tagName is string => Boolean(tagName))
                .filter((tagName) => tagName !== metaLabel)
        )
    );

    return (
        <article className="discovery-search-item-article">
            <div className="discovery-search-item-tags discovery-search-item-tags--meta">
                <span className="discovery-search-item-tag discovery-search-item-tag--source">
                    {SEARCH_ITEM_TYPE_LABELS[type]}
                </span>
                {metaLabel ? (
                    <span className="discovery-search-item-tag discovery-search-item-tag--meta">
                        {metaLabel}
                    </span>
                ) : null}
            </div>

            {tagNames.length ? (
                <div className="discovery-search-item-tags" aria-label="内容标签">
                    {tagNames.map((tagName) => (
                        <span key={tagName} className="discovery-search-item-tag">
                            {tagName}
                        </span>
                    ))}
                </div>
            ) : null}

            <RichTextSection text={preview.summary} title="提要" />
            <RichTextSection markdown={preview.bodyText} text={preview.bodyText} title="正文" />
        </article>
    );
};

const SancaiEntryRenderer = ({ entry }: { entry: SancaiEntryRecord }) => {
    const coverImage = entry.images?.find((image) => image.currentUsed) || entry.images?.[0];
    const author = readAuthor(entry);
    const visualDescription =
        entry.currentVisualAsset?.visualDescription || entry.currentVisualAsset?.fusionDescription;

    return (
        <article className="discovery-search-item-article">
            <div className="discovery-search-item-tags discovery-search-item-tags--meta">
                <span className="discovery-search-item-tag discovery-search-item-tag--source">
                    {readSourceTitle(entry)}
                </span>
                {author ? (
                    <span className="discovery-search-item-tag discovery-search-item-tag--meta">
                        作者：{author}
                    </span>
                ) : null}
            </div>

            {coverImage?.previewUrl ? (
                <figure className="discovery-search-item-cover">
                    <img alt={coverImage.title || readTitle(entry)} src={coverImage.previewUrl} />
                    {coverImage.title ? <figcaption>{coverImage.title}</figcaption> : null}
                </figure>
            ) : null}

            {entry.tags?.length ? (
                <div className="discovery-search-item-tags" aria-label="内容标签">
                    {entry.tags.map((tag) => (
                        <span key={tag.id ?? tag.tagName} className="discovery-search-item-tag">
                            {tag.tagName}
                        </span>
                    ))}
                </div>
            ) : null}

            <RichTextSection
                html={entry.summaryHtml}
                markdown={entry.summaryMarkdown}
                text={entry.summary}
                title="提要"
            />
            <RichTextSection
                html={entry.bodyHtml || entry.originalHtml}
                markdown={entry.bodyMarkdown || entry.originalMarkdown}
                text={entry.originalText}
                title="原文"
            />
            <RichTextSection
                html={entry.translationHtml}
                markdown={entry.translationMarkdown}
                text={entry.translationText}
                title="译文"
            />
            <RichTextSection
                markdown={entry.currentVisualAsset?.imageAnalysisMarkdown}
                text={visualDescription}
                title="图像说明"
            />
        </article>
    );
};

export const DiscoverySearchItemPage = () => {
    const [searchParams] = useSearchParams();
    const entryId = readEntryIdParam(searchParams);
    const itemType = readItemTypeParam(searchParams);
    const itemQuery = useQuery({
        enabled: entryId !== null,
        queryFn: () => searchItemService.getSearchItem(itemType, entryId ?? 0),
        queryKey: ["portal", "discovery", "search-item", itemType, entryId]
    });
    const item = itemQuery.data;

    return (
        <main className="discovery-search-item-page">
            <Card className="discovery-search-item-card" aria-label="检索内容详情">
                <div className="discovery-search-item-heading">
                    <FileText size={22} />
                    <div>
                        <h1>{item ? readModelTitle(item) : "内容详情"}</h1>
                    </div>
                </div>

                {!entryId ? <p className="discovery-search-item-muted">缺少有效内容 ID</p> : null}
                {itemQuery.isLoading ? (
                    <p className="discovery-search-item-muted">正在加载内容...</p>
                ) : null}
                {itemQuery.isError ? (
                    <p className="discovery-search-item-muted">内容暂时不可用</p>
                ) : null}
                {item ? renderSearchItem(item) : null}
            </Card>
        </main>
    );
};
