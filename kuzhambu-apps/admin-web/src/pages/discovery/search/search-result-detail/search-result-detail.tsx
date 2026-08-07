import { useQuery } from "@tanstack/react-query";
import { Descriptions, Empty, Spin, Typography } from "antd";
import { KuzhambuDrawer, KuzhambuTag } from "@/components";
import * as service from "@/pages/discovery/search/search-service";
import type { SearchResultEntry } from "@/pages/discovery/search/search-result-table";

const PREVIEW_PARAGRAPH_MAX_LENGTH = 360;

const chunkPreviewParagraph = (paragraph: string) => {
    if (paragraph.length <= PREVIEW_PARAGRAPH_MAX_LENGTH) {
        return [paragraph];
    }

    const sentences =
        paragraph.match(/[^。！？!?；;]+[。！？!?；;]?/gu)?.map((sentence) => sentence.trim()) ??
        [];
    const units = sentences.length > 1 ? sentences : Array.from(paragraph);
    const paragraphs: string[] = [];
    let currentParagraph = "";

    units.forEach((unit) => {
        if (
            currentParagraph &&
            currentParagraph.length + unit.length > PREVIEW_PARAGRAPH_MAX_LENGTH
        ) {
            paragraphs.push(currentParagraph);
            currentParagraph = unit;
            return;
        }
        currentParagraph += unit;
    });

    if (currentParagraph) {
        paragraphs.push(currentParagraph);
    }

    return paragraphs;
};

const splitPreviewBody = (value?: string | null) => {
    const bodyText = (value ?? "").trim();
    if (!bodyText) {
        return [];
    }

    return bodyText
        .split(/\r?\n+/u)
        .map((paragraph) => paragraph.trim())
        .filter(Boolean)
        .flatMap(chunkPreviewParagraph);
};

interface SearchResultDetailProps {
    previewResult?: SearchResultEntry | null;
    onClose: () => void;
}

const KNOWLEDGE_BASE_LABELS = new Map([
    ["SANCAI_ENTRY", "三才图会"],
    ["WANGQI_DOCUMENT", "王圻文档"],
    ["MING_CUSTOMS", "明代习俗"]
]);

const toKnowledgeBaseLabel = (value?: string | null) => {
    return (value ? KNOWLEDGE_BASE_LABELS.get(value) : null) || value || "-";
};

export const SearchResultDetail = ({ previewResult, onClose }: SearchResultDetailProps) => {
    const contentId = previewResult?.item.contentId ?? null;
    const contentType = previewResult?.item.contentType ?? null;
    const previewQuery = useQuery({
        enabled: contentId !== null && contentType !== null,
        queryFn: () =>
            service.previewSearchResult({
                contentId: contentId ?? "",
                contentType: contentType ?? ""
            }),
        queryKey: ["discovery-search", "preview", contentType, contentId]
    });
    const previewData = previewQuery.data;
    const errorMessage = previewQuery.error instanceof Error ? previewQuery.error.message : null;
    const open = previewResult !== null && previewResult !== undefined;
    const previewTitle = previewData?.title || previewResult?.item.title || "搜索命中预览";
    const previewBodyParagraphs = splitPreviewBody(previewData?.bodyText);
    const previewMetaItems = [
        {
            key: "knowledgeBase",
            label: "知识库",
            children: toKnowledgeBaseLabel(previewData?.knowledgeBase)
        },
        {
            key: "category",
            label: "门类",
            children: previewData?.categoryName || previewData?.categoryCode || "-"
        }
    ];

    return (
        <KuzhambuDrawer
            destroyOnHidden
            open={open}
            size="large"
            testId="discovery-search-preview-drawer"
            title={previewTitle}
            footerActions={[
                {
                    testId: "discovery-search-preview-close-button",
                    title: "关闭预览",
                    action: onClose
                }
            ]}
            onClose={onClose}
        >
            <Spin spinning={previewQuery.isFetching}>
                {errorMessage ? <Empty description={`预览失败：${errorMessage}`} /> : null}
                {!errorMessage && open && !previewData && !previewQuery.isFetching ? (
                    <Empty description="当前内容不可预览或已经不可见" />
                ) : null}
                {previewData ? (
                    <div className="search-page-preview">
                        <Descriptions bordered column={1} items={previewMetaItems} size="small" />
                        {previewData.summary ? (
                            <section className="search-page-preview-section">
                                <Typography.Title level={5}>摘要</Typography.Title>
                                <Typography.Paragraph>{previewData.summary}</Typography.Paragraph>
                            </section>
                        ) : null}
                        <section className="search-page-preview-section">
                            <Typography.Title level={5}>正文</Typography.Title>
                            {previewBodyParagraphs.length ? (
                                previewBodyParagraphs.map((paragraph, index) => (
                                    <Typography.Paragraph key={`preview-body-${index}`}>
                                        {paragraph}
                                    </Typography.Paragraph>
                                ))
                            ) : (
                                <Typography.Paragraph>暂无正文。</Typography.Paragraph>
                            )}
                        </section>
                        {previewData.tagNames?.length ? (
                            <div className="search-page-preview-tags">
                                {previewData.tagNames.map((tagName) => (
                                    <KuzhambuTag type="info" key={tagName}>
                                        {tagName}
                                    </KuzhambuTag>
                                ))}
                            </div>
                        ) : null}
                    </div>
                ) : null}
            </Spin>
        </KuzhambuDrawer>
    );
};
