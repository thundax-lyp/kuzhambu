import { Empty, Pagination, Spin, Tag } from "antd";
import type {
    DiscoverySearchGroupRecord,
    DiscoverySearchItemRecord
} from "@/pages/discovery/search/search-types";

export interface SearchResultEntry {
    group: DiscoverySearchGroupRecord;
    item: DiscoverySearchItemRecord;
    key: string;
}

interface SearchResultTableProps {
    currentPageNo: number;
    currentPageSize: number;
    isError: boolean;
    isPending: boolean;
    queryText: string;
    results: SearchResultEntry[];
    shouldShowZeroResult: boolean;
    totalCount: number;
    onChangePage: (pageNo?: number, pageSize?: number) => void;
    onOpenPreview: (result: SearchResultEntry) => void;
}

const splitList = (value: string) => {
    const tokens = value
        .split(/[\n,，、\s]+/gu)
        .map((token) => token.trim())
        .filter(Boolean);
    return Array.from(new Set(tokens));
};

const escapeRegExp = (value: string) => {
    return value.replace(/[.*+?^${}()|[\]\\]/gu, "\\$&");
};

const renderHighlightText = (highlightText?: string | null) => {
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
    const terms = splitList(queryText);
    if (terms.length === 0) {
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
    return nodes.length > 0 ? nodes : text;
};

const toPlainHighlightText = (value?: string | null) => {
    return (value ?? "").replace(/<mark>(.*?)<\/mark>/giu, "$1").trim();
};

export const SearchResultTable = ({
    currentPageNo,
    currentPageSize,
    isError,
    isPending,
    queryText,
    results,
    shouldShowZeroResult,
    totalCount,
    onChangePage,
    onOpenPreview
}: SearchResultTableProps) => {
    const renderResultTitle = (result: SearchResultEntry) => {
        const title = result.item.title || "未命名结果";
        const titleContent = renderQueryHighlight(title, queryText);

        return (
            <button
                aria-label={`打开搜索预览：${title}`}
                className="search-page-result-title-button"
                type="button"
                onClick={() => onOpenPreview(result)}
            >
                {titleContent}
            </button>
        );
    };
    const renderResultSummary = (result: SearchResultEntry) => {
        const title = (result.item.title || "").trim();
        const highlightText = toPlainHighlightText(result.item.highlightText);
        const summary = (result.item.summary || "").trim();
        const summaryText = highlightText || summary;
        if (!summaryText || summaryText === title) {
            return null;
        }

        return renderHighlightText(result.item.highlightText) || summary;
    };

    return (
        <Spin spinning={isPending}>
            <section className="search-page-results" aria-label="检索结果">
                {isError ? <Empty description="检索失败，请稍后重试" /> : null}
                {!isError && results.length === 0 ? (
                    <Empty
                        description={shouldShowZeroResult ? "没有找到匹配内容" : "暂无搜索结果"}
                    />
                ) : null}
                {!isError && results.length > 0 ? (
                    <>
                        <div className="search-page-result-list">
                            {results.map((result) => {
                                const summary = renderResultSummary(result);
                                return (
                                    <article className="search-page-result-item" key={result.key}>
                                        <h3 className="search-page-result-title">
                                            {renderResultTitle(result)}
                                        </h3>
                                        {summary ? (
                                            <p className="search-page-result-summary">{summary}</p>
                                        ) : null}
                                        <div className="search-page-result-meta">
                                            <Tag color="blue">
                                                {result.group.groupTitle ||
                                                    result.group.groupKey ||
                                                    "未分组"}
                                            </Tag>
                                        </div>
                                    </article>
                                );
                            })}
                        </div>
                        <Pagination
                            className="search-page-pagination"
                            current={currentPageNo}
                            pageSize={currentPageSize}
                            showSizeChanger
                            showTotal={(total) => `共 ${total} 条`}
                            total={totalCount}
                            onChange={onChangePage}
                        />
                    </>
                ) : null}
            </section>
        </Spin>
    );
};
