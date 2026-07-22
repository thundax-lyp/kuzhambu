import { useMutation, useQuery } from "@tanstack/react-query";
import { Card, Checkbox, Descriptions, Progress, Segmented, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import type { Key } from "react";
import { useState } from "react";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { SearchStatisticsFilterPanel } from "./components/search-statistics-filter-panel";
import type { SearchStatisticsDateRangeValue } from "./components/search-statistics-filter-panel";
import { SearchTrendPanel } from "./components/search-trend-panel";
import * as service from "./search-statistics-service";
import type {
    DiscoverySearchEventPageQuery,
    DiscoverySearchStatisticsSummaryQuery
} from "./search-statistics-service";
import type {
    DiscoverySearchStatisticsSummaryRecord,
    DiscoverySearchEventDetailRecord,
    DiscoverySearchEventPageRecord,
    DiscoverySearchEventRecord
} from "./search-statistics-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./search-statistics-page.css";

const { Text, Title } = Typography;

type SearchStatisticsPanel = "summary" | "records" | "rebuild";

const TOP_QUERY_LIMIT = 10;
const DATE_TIME_FORMAT = "YYYY-MM-DD HH:mm:ss";

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

const formatTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

const normalizeSearch = (value: string) => {
    const normalizedValue = value.trim();
    return normalizedValue || null;
};

const rangeToQuery = (range: SearchStatisticsDateRangeValue) => ({
    dateFrom: range?.[0]?.format(DATE_TIME_FORMAT) || null,
    dateTo: range?.[1]?.format(DATE_TIME_FORMAT) || null
});

const readRecordKey = (record: DiscoverySearchEventRecord): string =>
    record.searchEventId || `${record.queryText || "query"}-${record.createdAt || "unknown"}`;

export const SearchStatisticsPage = () => {
    const [queryText, setQueryText] = useState("礼器");
    const [searchStatuses, setSearchStatuses] = useState("SUCCESS");
    const [summaryDateRange, setSummaryDateRange] = useState<SearchStatisticsDateRangeValue>(null);
    const [summaryQuery, setSummaryQuery] = useState<DiscoverySearchStatisticsSummaryQuery>(
        rangeToQuery(null)
    );
    const [recordDateRange, setRecordDateRange] = useState<SearchStatisticsDateRangeValue>(null);
    const [eventQuery, setEventQuery] = useState<DiscoverySearchEventPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        queryText: "礼器",
        searchStatuses: ["SUCCESS"]
    });
    const [expandedRowKeys, setExpandedRowKeys] = useState<Key[]>([]);
    const [confirmRebuild, setConfirmRebuild] = useState(true);
    const [activePanel, setActivePanel] = useState<SearchStatisticsPanel>("summary");
    const [pageResult, setPageResult] = useState<DiscoverySearchEventPageRecord | null>(null);
    const [detailResults, setDetailResults] = useState<
        Record<string, DiscoverySearchEventDetailRecord | null>
    >({});
    const [rebuildResult, setRebuildResult] = useState<number | null>(null);
    const analysisQuery = useQuery({
        queryKey: ["discovery-search-statistics", "summary", summaryQuery],
        queryFn: () => service.getSearchStatisticsSummary(summaryQuery),
        retry: false
    });
    const analysisResult = analysisQuery.data ?? null;
    const topQueryBars = buildTopQueryBars(analysisResult);
    const summaryMetrics = [
        {
            key: "searchCount",
            label: "搜索次数",
            value: analysisResult?.searchCount ?? 0
        },
        {
            key: "failedSearchCount",
            label: "失败次数",
            value: analysisResult?.failedSearchCount ?? 0
        },
        {
            key: "zeroResultSearchCount",
            label: "零结果次数",
            value: analysisResult?.zeroResultSearchCount ?? 0
        },
        {
            key: "clickCount",
            label: "点击次数",
            value: analysisResult?.clickCount ?? 0
        }
    ];

    const pageMutation = useMutation({
        mutationFn: service.pageSearchEvents,
        onSuccess: (nextPage) => {
            setPageResult(nextPage);
        }
    });
    const detailMutation = useMutation({
        mutationFn: service.getSearchEventDetail,
        onSuccess: (nextDetail, variables) => {
            setDetailResults((currentDetails) => ({
                ...currentDetails,
                [variables.searchEventId]: nextDetail
            }));
        }
    });
    const rebuildMutation = useMutation({
        mutationFn: service.rebuildSearchIndex,
        onSuccess: (nextCount) => {
            setRebuildResult(nextCount);
        }
    });
    let rebuildStatusText = "尚未触发重建。";
    let rebuildProgressPercent = 0;
    let rebuildProgressStatus: "active" | "exception" | "normal" | "success" = "normal";

    if (rebuildMutation.isPending) {
        rebuildStatusText = "重建触发中，请等待任务完成。";
        rebuildProgressPercent = 50;
        rebuildProgressStatus = "active";
    } else if (rebuildMutation.isError) {
        rebuildStatusText = "重建触发失败。";
        rebuildProgressStatus = "exception";
    } else if (rebuildResult !== null) {
        rebuildStatusText = `重建结果：${rebuildResult}`;
        rebuildProgressPercent = 100;
        rebuildProgressStatus = "success";
    }
    const shouldShowRebuildProgress =
        rebuildMutation.isPending || rebuildMutation.isError || rebuildResult !== null;

    const pageColumns: ColumnsType<DiscoverySearchEventRecord> = [
        { title: "检索编号", dataIndex: "searchEventId", key: "searchEventId", width: 160 },
        { title: "搜索词", dataIndex: "queryText", key: "queryText", width: 180 },
        { title: "回显词", dataIndex: "displayQueryText", key: "displayQueryText", width: 180 },
        { title: "状态", dataIndex: "searchStatus", key: "searchStatus", width: 120 },
        { title: "结果数", dataIndex: "resultTotalCount", key: "resultTotalCount", width: 100 },
        { title: "分组数", dataIndex: "groupTotalCount", key: "groupTotalCount", width: 100 },
        {
            title: "创建时间",
            dataIndex: "createdAt",
            key: "createdAt",
            width: 180,
            render: (value?: string | null) => formatTime(value)
        }
    ];
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || eventQuery.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || eventQuery.pageSize || DEFAULT_PAGE_SIZE;
    const hasActiveEventFilters = Boolean(
        queryText.trim() || searchStatuses.trim() || recordDateRange?.[0] || recordDateRange?.[1]
    );

    const buildEventQuery = (
        nextQuery: Partial<DiscoverySearchEventPageQuery> = {}
    ): DiscoverySearchEventPageQuery => ({
        ...rangeToQuery(recordDateRange),
        pageNo: eventQuery.pageNo || DEFAULT_PAGE_NO,
        pageSize: eventQuery.pageSize || DEFAULT_PAGE_SIZE,
        queryText: normalizeSearch(queryText),
        searchStatuses: searchStatuses
            .split(/[,\s]+/gu)
            .map((value) => value.trim())
            .filter(Boolean),
        ...nextQuery
    });

    const loadEventPage = (nextQuery: DiscoverySearchEventPageQuery) => {
        setEventQuery(nextQuery);
        pageMutation.mutate(nextQuery);
    };

    const queryEvents = () => {
        loadEventPage(buildEventQuery({ pageNo: DEFAULT_PAGE_NO }));
    };

    const refreshSummary = () => {
        const nextQuery = rangeToQuery(summaryDateRange);
        setSummaryQuery(nextQuery);
        if (
            nextQuery.dateFrom === summaryQuery.dateFrom &&
            nextQuery.dateTo === summaryQuery.dateTo
        ) {
            void analysisQuery.refetch();
        }
    };

    const resetEventFilters = () => {
        setQueryText("");
        setSearchStatuses("");
        setRecordDateRange(null);
        const nextQuery: DiscoverySearchEventPageQuery = {
            pageNo: DEFAULT_PAGE_NO,
            pageSize: eventQuery.pageSize || DEFAULT_PAGE_SIZE
        };
        setExpandedRowKeys([]);
        setPageResult(null);
        setEventQuery(nextQuery);
    };

    const expandRecord = (expanded: boolean, record: DiscoverySearchEventRecord) => {
        const recordKey = readRecordKey(record);
        setExpandedRowKeys((currentKeys) =>
            expanded
                ? [...currentKeys.filter((key) => key !== recordKey), recordKey]
                : currentKeys.filter((key) => key !== recordKey)
        );

        if (expanded && record.searchEventId && !(record.searchEventId in detailResults)) {
            detailMutation.mutate({
                searchEventId: record.searchEventId
            });
        }
    };

    const renderRecordDetail = (record: DiscoverySearchEventRecord) => {
        const detail =
            record.searchEventId && record.searchEventId in detailResults
                ? detailResults[record.searchEventId]
                : null;

        if (record.searchEventId && detailMutation.isPending) {
            return <Text type="secondary">详情加载中...</Text>;
        }

        return (
            <div className="search-statistics-record-detail">
                <Descriptions
                    bordered
                    column={2}
                    items={[
                        {
                            key: "searchEventId",
                            label: "检索编号",
                            children: detail?.searchEventId ?? record.searchEventId ?? "-"
                        },
                        {
                            key: "queryText",
                            label: "搜索词",
                            children: detail?.queryText ?? record.queryText ?? "-"
                        },
                        {
                            key: "normalizedQueryText",
                            label: "清洗词",
                            children: detail?.normalizedQueryText ?? "-"
                        },
                        {
                            key: "displayQueryText",
                            label: "回显词",
                            children: detail?.displayQueryText ?? record.displayQueryText ?? "-"
                        },
                        {
                            key: "intentType",
                            label: "意图",
                            children: detail?.intentType ?? record.intentType ?? "-"
                        },
                        {
                            key: "状态",
                            label: "状态",
                            children: detail?.searchStatus ?? record.searchStatus ?? "-"
                        },
                        {
                            key: "resultTotalCount",
                            label: "总结果",
                            children: detail?.resultTotalCount ?? record.resultTotalCount ?? "-"
                        },
                        {
                            key: "groupTotalCount",
                            label: "分组数",
                            children: detail?.groupTotalCount ?? record.groupTotalCount ?? "-"
                        },
                        {
                            key: "requestId",
                            label: "请求号",
                            children: detail?.requestId ?? "-"
                        },
                        {
                            key: "traceId",
                            label: "链路号",
                            children: detail?.traceId ?? "-"
                        }
                    ]}
                    size="small"
                />
                <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                    <Text strong>失败信息 / 检索范围</Text>
                    <Text type="secondary">{detail?.failureCode ?? "-"}</Text>
                    <Text>{detail?.failureMessage ?? "-"}</Text>
                    <Text code style={{ whiteSpace: "pre-wrap" }}>
                        {detail?.searchScopesJson ?? "-"}
                    </Text>
                </KuzhambuSpace>
            </div>
        );
    };

    return (
        <main className="kuzhambu-page discovery-admin-page search-statistics-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Title level={2}>检索统计</Title>
                        <Text type="secondary">
                            查看检索统计、打开检索记录详情并手动触发索引重建。
                        </Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Segmented
                        className="search-statistics-segmented"
                        options={[
                            { label: "统计摘要", value: "summary" },
                            { label: "检索记录", value: "records" },
                            { label: "索引重建", value: "rebuild" }
                        ]}
                        value={activePanel}
                        onChange={(value) => setActivePanel(value as SearchStatisticsPanel)}
                    />

                    {activePanel === "summary" ? (
                        <section className="search-statistics-summary-panel">
                            <KuzhambuSpace
                                orientation="vertical"
                                size={12}
                                style={{ width: "100%" }}
                            >
                                <SearchStatisticsFilterPanel
                                    dateTimeFormat={DATE_TIME_FORMAT}
                                    loading={analysisQuery.isFetching}
                                    mode="summary"
                                    summaryDateRange={summaryDateRange}
                                    onRefreshSummary={refreshSummary}
                                    onSummaryDateRangeChange={setSummaryDateRange}
                                />
                                <SearchTrendPanel
                                    summaryMetrics={summaryMetrics}
                                    topQueryBars={topQueryBars}
                                />
                            </KuzhambuSpace>
                        </section>
                    ) : null}

                    {activePanel === "records" ? (
                        <>
                            <SearchStatisticsFilterPanel
                                dateTimeFormat={DATE_TIME_FORMAT}
                                hasActiveEventFilters={hasActiveEventFilters}
                                loading={pageMutation.isPending}
                                mode="records"
                                pageLoaded={Boolean(pageResult)}
                                queryText={queryText}
                                recordDateRange={recordDateRange}
                                searchStatuses={searchStatuses}
                                onQueryEvents={queryEvents}
                                onQueryTextChange={setQueryText}
                                onRecordDateRangeChange={setRecordDateRange}
                                onResetEventFilters={resetEventFilters}
                                onSearchStatusesChange={setSearchStatuses}
                            />

                            <Card className="search-statistics-record-table-card" size="small">
                                <KuzhambuSpace
                                    orientation="vertical"
                                    size={12}
                                    style={{ width: "100%" }}
                                >
                                    <Table
                                        aria-label="检索记录表格"
                                        columns={pageColumns}
                                        dataSource={pageResult?.records ?? []}
                                        expandable={{
                                            expandedRowKeys,
                                            expandedRowRender: renderRecordDetail,
                                            onExpand: expandRecord
                                        }}
                                        loading={pageMutation.isPending}
                                        pagination={{
                                            current: currentPageNo,
                                            pageSize: currentPageSize,
                                            total: totalCount,
                                            showSizeChanger: true,
                                            showTotal: (total) => `共 ${total} 条`,
                                            onChange: (nextPageNo, nextPageSize) => {
                                                loadEventPage(
                                                    buildEventQuery({
                                                        pageNo: nextPageNo,
                                                        pageSize: nextPageSize
                                                    })
                                                );
                                            }
                                        }}
                                        rowKey={readRecordKey}
                                        size="small"
                                        scroll={{ x: 1120 }}
                                    />
                                    <Text type="secondary">
                                        {pageResult
                                            ? `共 ${pageResult.count ?? pageResult.totalCount ?? 0} 条记录`
                                            : "暂无检索记录。"}
                                    </Text>
                                </KuzhambuSpace>
                            </Card>
                        </>
                    ) : null}

                    {activePanel === "rebuild" ? (
                        <Card size="small">
                            <KuzhambuSpace
                                orientation="vertical"
                                size={8}
                                style={{ width: "100%" }}
                            >
                                <KuzhambuSpace align="center" wrap>
                                    <Checkbox
                                        checked={confirmRebuild}
                                        onChange={(event) =>
                                            setConfirmRebuild(event.target.checked)
                                        }
                                    >
                                        确认全量重建
                                    </Checkbox>
                                    <KuzhambuButton
                                        testId="discovery-search-statistics-search-statistics-trigger-rebuild-button"
                                        danger
                                        loading={rebuildMutation.isPending}
                                        onClick={() =>
                                            rebuildMutation.mutate({
                                                confirm: confirmRebuild
                                            })
                                        }
                                    >
                                        触发重建
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                                <Text className="search-statistics-rebuild-status" type="secondary">
                                    {rebuildStatusText}
                                </Text>
                                {shouldShowRebuildProgress ? (
                                    <Progress
                                        aria-label="索引重建进度"
                                        percent={rebuildProgressPercent}
                                        showInfo={false}
                                        status={rebuildProgressStatus}
                                    />
                                ) : null}
                            </KuzhambuSpace>
                        </Card>
                    ) : null}
                </KuzhambuSpace>
            </section>
        </main>
    );
};
