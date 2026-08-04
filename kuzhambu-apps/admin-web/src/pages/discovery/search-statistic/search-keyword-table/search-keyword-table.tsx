import { Typography } from "antd";

const { Text } = Typography;

export interface SearchKeywordBarItem {
    count: number;
    index: number;
    queryText: string;
    widthPercent: number;
}

interface SearchKeywordTableProps {
    topQueryBars: SearchKeywordBarItem[];
}

export const SearchKeywordTable = ({ topQueryBars }: SearchKeywordTableProps) => {
    return (
        <div className="search-statistics-top-query-chart">
            <Text strong>热门搜索词 Top 10</Text>
            {topQueryBars.length ? (
                <div
                    aria-label="热门搜索词前10名柱状图"
                    className="search-statistics-top-query-bars"
                    role="list"
                >
                    {topQueryBars.map((topQuery, index) => (
                        <div
                            className="search-statistics-top-query-bar-row"
                            key={`${topQuery.queryText}-${topQuery.index}`}
                            role="listitem"
                        >
                            <span className="search-statistics-top-query-rank">{index + 1}</span>
                            <span
                                className="search-statistics-top-query-label"
                                title={topQuery.queryText}
                            >
                                {topQuery.queryText}
                            </span>
                            <div aria-hidden="true" className="search-statistics-top-query-track">
                                <div
                                    className="search-statistics-top-query-bar"
                                    style={{ width: `${topQuery.widthPercent}%` }}
                                />
                            </div>
                            <Text className="search-statistics-top-query-count" type="secondary">
                                {topQuery.count} 次
                            </Text>
                        </div>
                    ))}
                </div>
            ) : (
                <Text type="secondary">暂无热门搜索词。</Text>
            )}
        </div>
    );
};
