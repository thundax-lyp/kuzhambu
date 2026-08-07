import { useQuery } from "@tanstack/react-query";
import { DatePicker, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { useState } from "react";
import { KuzhambuButton, KuzhambuCard, KuzhambuSpace } from "@/components";
import { SearchTrendPanel } from "@/pages/discovery/search-statistic/search-trend-panel";
import * as service from "@/pages/discovery/search-statistic/search-statistic-service";
import type { DiscoverySearchStatisticsSummaryRecord } from "@/pages/discovery/search-statistic/search-statistic-types";

const { Text } = Typography;
const { RangePicker } = DatePicker;
const DATE_TIME_FORMAT = "YYYY-MM-DD HH:mm:ss";
const TOP_QUERY_LIMIT = 10;

type DateRangeValue = [Dayjs | null, Dayjs | null] | null;

const rangeToQuery = (range: DateRangeValue) => ({
    dateFrom: range?.[0]?.format(DATE_TIME_FORMAT) || null,
    dateTo: range?.[1]?.format(DATE_TIME_FORMAT) || null
});

const buildTopQueryBars = (summary: DiscoverySearchStatisticsSummaryRecord | null) => {
    const topQueries = (summary?.topQueries ?? [])
        .map((topQuery, index) => ({
            count: topQuery.count ?? 0,
            index,
            queryText: topQuery.queryText?.trim() || "-"
        }))
        .sort((left, right) => right.count - left.count)
        .slice(0, TOP_QUERY_LIMIT);
    const maxCount = Math.max(1, ...topQueries.map((topQuery) => topQuery.count));

    return topQueries.map((topQuery) => ({
        ...topQuery,
        widthPercent:
            topQuery.count > 0 ? Math.max(4, Math.round((topQuery.count / maxCount) * 100)) : 0
    }));
};

export const SearchSummaryPanel = () => {
    const [dateRange, setDateRange] = useState<DateRangeValue>(null);
    const [query, setQuery] = useState(() => rangeToQuery(null));
    const summaryQuery = useQuery({
        queryFn: () => service.getSearchStatisticsSummary(query),
        queryKey: ["discovery-search-statistics", "summary", query],
        retry: false
    });
    const summary = summaryQuery.data ?? null;
    const summaryMetrics = [
        { key: "searchCount", label: "搜索次数", value: summary?.searchCount ?? 0 },
        { key: "failedSearchCount", label: "失败次数", value: summary?.failedSearchCount ?? 0 },
        {
            key: "zeroResultSearchCount",
            label: "零结果次数",
            value: summary?.zeroResultSearchCount ?? 0
        },
        { key: "clickCount", label: "点击次数", value: summary?.clickCount ?? 0 }
    ];
    const refreshSummary = () => {
        const nextQuery = rangeToQuery(dateRange);
        if (nextQuery.dateFrom === query.dateFrom && nextQuery.dateTo === query.dateTo) {
            void summaryQuery.refetch();
            return;
        }
        setQuery(nextQuery);
    };

    return (
        <section className="search-statistics-summary-panel">
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <KuzhambuCard size="small">
                    <KuzhambuSpace className="search-statistics-summary-filter" wrap>
                        <label className="search-statistics-summary-filter-field">
                            <Text className="search-statistics-filter-label" type="secondary">
                                起始时间
                            </Text>
                            <RangePicker
                                aria-label="统计时间范围"
                                className="search-statistics-summary-range-picker"
                                format={DATE_TIME_FORMAT}
                                showTime
                                value={dateRange}
                                onChange={setDateRange}
                            />
                        </label>
                        <KuzhambuButton
                            ariaLabel="统计"
                            className="search-statistics-summary-filter-button"
                            testId="discovery-search-statistics-search-statistics-action-button"
                            loading={summaryQuery.isFetching}
                            onClick={refreshSummary}
                            type="primary"
                        >
                            统计
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuCard>
                {summaryQuery.isError ? <Text type="danger">统计摘要加载失败。</Text> : null}
                <SearchTrendPanel
                    summaryMetrics={summaryMetrics}
                    topQueryBars={buildTopQueryBars(summary)}
                />
            </KuzhambuSpace>
        </section>
    );
};
