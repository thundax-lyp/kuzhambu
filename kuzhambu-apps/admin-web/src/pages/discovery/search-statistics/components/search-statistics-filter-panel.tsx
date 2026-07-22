import { Card, DatePicker, Input, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";

const { Text } = Typography;
const { RangePicker } = DatePicker;

export type SearchStatisticsDateRangeValue = [Dayjs | null, Dayjs | null] | null;

interface SearchStatisticsFilterPanelProps {
    dateTimeFormat: string;
    hasActiveEventFilters?: boolean;
    loading: boolean;
    mode: "records" | "summary";
    pageLoaded?: boolean;
    queryText?: string;
    recordDateRange?: SearchStatisticsDateRangeValue;
    searchStatuses?: string;
    summaryDateRange?: SearchStatisticsDateRangeValue;
    onQueryEvents?: () => void;
    onRefreshSummary?: () => void;
    onResetEventFilters?: () => void;
    onRecordDateRangeChange?: (value: SearchStatisticsDateRangeValue) => void;
    onSearchStatusesChange?: (value: string) => void;
    onSummaryDateRangeChange?: (value: SearchStatisticsDateRangeValue) => void;
    onQueryTextChange?: (value: string) => void;
}

export const SearchStatisticsFilterPanel = ({
    dateTimeFormat,
    hasActiveEventFilters = false,
    loading,
    mode,
    pageLoaded = false,
    queryText = "",
    recordDateRange = null,
    searchStatuses = "",
    summaryDateRange = null,
    onQueryEvents,
    onRefreshSummary,
    onResetEventFilters,
    onRecordDateRangeChange,
    onSearchStatusesChange,
    onSummaryDateRangeChange,
    onQueryTextChange
}: SearchStatisticsFilterPanelProps) => {
    if (mode === "summary") {
        return (
            <Card size="small">
                <KuzhambuSpace className="search-statistics-summary-filter" wrap>
                    <label>
                        <Text type="secondary">起始时间</Text>
                        <RangePicker
                            aria-label="统计时间范围"
                            format={dateTimeFormat}
                            showTime
                            value={summaryDateRange}
                            onChange={(value) => onSummaryDateRangeChange?.(value)}
                        />
                    </label>
                    <KuzhambuButton
                        ariaLabel="统计"
                        testId="discovery-search-statistics-search-statistics-action-button"
                        loading={loading}
                        onClick={onRefreshSummary}
                        type="primary"
                    >
                        统计
                    </KuzhambuButton>
                </KuzhambuSpace>
            </Card>
        );
    }

    return (
        <Card className="search-statistics-record-filter-card" size="small">
            <KuzhambuSpace className="search-statistics-record-filter-form" wrap>
                <label>
                    <Text type="secondary">搜索词</Text>
                    <Input
                        allowClear
                        aria-label="搜索词"
                        value={queryText}
                        onChange={(event) => onQueryTextChange?.(event.target.value)}
                        style={{ width: 160 }}
                    />
                </label>
                <label>
                    <Text type="secondary">状态</Text>
                    <Input
                        allowClear
                        aria-label="状态"
                        value={searchStatuses}
                        onChange={(event) => onSearchStatusesChange?.(event.target.value)}
                        style={{ width: 136 }}
                    />
                </label>
                <label>
                    <Text type="secondary">时间范围</Text>
                    <RangePicker
                        aria-label="检索记录时间范围"
                        format={dateTimeFormat}
                        showTime
                        value={recordDateRange}
                        onChange={(value) => onRecordDateRangeChange?.(value)}
                    />
                </label>
                <KuzhambuSpace className="search-statistics-record-filter-actions">
                    <KuzhambuButton
                        testId="discovery-search-statistics-search-statistics-query-events-button"
                        loading={loading}
                        onClick={onQueryEvents}
                        type="primary"
                    >
                        查询记录
                    </KuzhambuButton>
                    <KuzhambuButton
                        ariaLabel="重置"
                        disabled={!hasActiveEventFilters && !pageLoaded}
                        testId="discovery-search-statistics-search-statistics-clear-result-button"
                        onClick={onResetEventFilters}
                    >
                        重置
                    </KuzhambuButton>
                </KuzhambuSpace>
            </KuzhambuSpace>
        </Card>
    );
};
