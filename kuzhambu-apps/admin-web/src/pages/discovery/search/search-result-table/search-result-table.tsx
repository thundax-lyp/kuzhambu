import { Empty, Pagination, Spin, Tag } from "antd";
import type { ReactNode } from "react";
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
    renderHighlightText: (highlightText?: string | null) => ReactNode;
    renderQueryHighlight: (text: string, queryText: string) => ReactNode;
    toPlainHighlightText: (value?: string | null) => string;
}

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
    onOpenPreview,
    renderHighlightText,
    renderQueryHighlight,
    toPlainHighlightText
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
