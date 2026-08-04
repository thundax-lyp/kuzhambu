import { Typography } from "antd";
import { KuzhambuSpace } from "@/components";
import { SearchKeywordTable } from "@/pages/discovery/search-statistic/search-keyword-table";
import type { SearchKeywordBarItem } from "@/pages/discovery/search-statistic/search-keyword-table";

const { Text } = Typography;

interface SearchStatisticsMetricItem {
    key: string;
    label: string;
    value: number;
}

interface SearchTrendPanelProps {
    summaryMetrics: SearchStatisticsMetricItem[];
    topQueryBars: SearchKeywordBarItem[];
}

export const SearchTrendPanel = ({ summaryMetrics, topQueryBars }: SearchTrendPanelProps) => {
    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <div className="search-statistics-summary-metrics">
                {summaryMetrics.map((metric) => (
                    <article className="search-statistics-summary-metric" key={metric.key}>
                        <Text type="secondary">{metric.label}</Text>
                        <strong>{metric.value}</strong>
                    </article>
                ))}
            </div>
            <SearchKeywordTable topQueryBars={topQueryBars} />
        </KuzhambuSpace>
    );
};
