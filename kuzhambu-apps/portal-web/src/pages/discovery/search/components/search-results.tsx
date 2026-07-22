import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";
import type {
    DiscoverySearchGroupResponse,
    DiscoverySearchItemResponse
} from "@/pages/discovery/search/search-types";
import { splitList } from "@/pages/discovery/search/search-utils";

const DISPLAY_LABELS: Record<string, string> = {
    CLASSICS: "古籍内容",
    MING_CUSTOMS: "明代习俗",
    PRIVATE: "内部可见",
    PUBLIC: "公开可见",
    PUBLISHED: "已发布",
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王圻文档"
};

const SEARCH_ITEM_CONTENT_TYPES = new Set(["SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS"]);

export interface DiscoverySearchResult {
    group: DiscoverySearchGroupResponse;
    groupIndex: number;
    item: DiscoverySearchItemResponse;
    itemIndex: number;
}

interface SearchResultListProps {
    queryText: string;
    results: DiscoverySearchResult[];
    onResultClick: (group: DiscoverySearchGroupResponse, item: DiscoverySearchItemResponse) => void;
}

export const SearchResultList = ({ queryText, results, onResultClick }: SearchResultListProps) => {
    if (!results.length) {
        return null;
    }

    return (
        <Card className="portal-discovery-group">
            <div className="portal-discovery-hit-list">
                {results.map(({ group, groupIndex, item, itemIndex }) => {
                    const hitKey = `${group.groupKey || `group-${groupIndex}`}-${item.resultRank ?? itemIndex}`;
                    const targetPath = toSearchItemPath(item.contentType, item.contentId);
                    const content = (
                        <div className="portal-discovery-hit-body">
                            <div className="portal-discovery-hit-title">
                                <div className="portal-discovery-hit-tags">
                                    <span>{toDisplayLabel(item.contentDomain, "其他来源")}</span>
                                    <span>{toDisplayLabel(item.contentType, "其他类型")}</span>
                                </div>
                                <h3>
                                    {renderQueryHighlight(item.title || "未命名结果", queryText)}
                                </h3>
                            </div>
                            <p className="portal-discovery-hit-summary">
                                {renderHighlightText(item.highlightText) ||
                                    renderQueryHighlight(item.summary || "", queryText) ||
                                    "暂无摘要"}
                            </p>
                        </div>
                    );

                    if (targetPath) {
                        return (
                            <Link
                                aria-label={`打开搜索结果：${item.title || "未命名结果"}`}
                                className="portal-discovery-hit"
                                key={hitKey}
                                rel="noreferrer"
                                target="_blank"
                                to={targetPath}
                                onClick={() => onResultClick(group, item)}
                            >
                                {content}
                            </Link>
                        );
                    }

                    return (
                        <button
                            aria-label={`打开搜索结果：${item.title || "未命名结果"}`}
                            className="portal-discovery-hit"
                            key={hitKey}
                            type="button"
                            onClick={() => onResultClick(group, item)}
                        >
                            {content}
                        </button>
                    );
                })}
            </div>
        </Card>
    );
};

const toDisplayLabel = (value?: string | null, fallback = "未标注") => {
    if (!value) {
        return fallback;
    }

    return DISPLAY_LABELS[value] ?? fallback;
};

const toSearchItemPath = (contentType?: string | null, contentId?: number | string | null) => {
    if (contentType && SEARCH_ITEM_CONTENT_TYPES.has(contentType) && contentId) {
        return `/discovery/search-item?type=${encodeURIComponent(contentType)}&id=${encodeURIComponent(String(contentId))}`;
    }

    return null;
};

const renderHighlightText = (highlightText: string | null | undefined) => {
    if (!highlightText) {
        return null;
    }

    const nodes: Array<string | JSX.Element> = [];
    const markPattern = /<mark>(.*?)<\/mark>/giu;
    let lastIndex = 0;
    let match = markPattern.exec(highlightText);

    while (match) {
        if (match.index > lastIndex) {
            nodes.push(highlightText.slice(lastIndex, match.index));
        }

        nodes.push(<mark key={`mark-${match.index}`}>{match[1]}</mark>);
        lastIndex = match.index + match[0].length;
        match = markPattern.exec(highlightText);
    }

    if (lastIndex < highlightText.length) {
        nodes.push(highlightText.slice(lastIndex));
    }

    return nodes;
};

const renderQueryHighlight = (text: string, queryText: string) => {
    const terms = splitList(queryText).filter((term) => term.length > 0);
    if (!terms.length) {
        return text;
    }

    const pattern = new RegExp(`(${terms.map(escapeRegExp).join("|")})`, "giu");
    const nodes: Array<string | JSX.Element> = [];
    let lastIndex = 0;
    let match = pattern.exec(text);

    while (match) {
        if (match.index > lastIndex) {
            nodes.push(text.slice(lastIndex, match.index));
        }

        nodes.push(<mark key={`query-mark-${match.index}`}>{match[0]}</mark>);
        lastIndex = match.index + match[0].length;
        match = pattern.exec(text);
    }

    if (lastIndex < text.length) {
        nodes.push(text.slice(lastIndex));
    }

    return nodes.length ? nodes : text;
};

const escapeRegExp = (value: string) => {
    return value.replace(/[.*+?^${}()|[\]\\]/gu, "\\$&");
};
