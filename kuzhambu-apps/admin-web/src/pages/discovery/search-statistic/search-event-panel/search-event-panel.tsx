import { useQuery } from "@tanstack/react-query";
import { DatePicker, Input, Typography } from "antd";
import type { Dayjs } from "dayjs";
import type { Key } from "react";
import { useState } from "react";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps
} from "@/components";
import { SearchEventDetail } from "@/pages/discovery/search-statistic/search-event-detail";
import * as service from "@/pages/discovery/search-statistic/search-statistic-service";
import type { DiscoverySearchEventRecord } from "@/pages/discovery/search-statistic/search-statistic-types";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";

const { Text } = Typography;
const { RangePicker } = DatePicker;
const DATE_TIME_FORMAT = "YYYY-MM-DD HH:mm:ss";

type DateRangeValue = [Dayjs | null, Dayjs | null] | null;

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

const readRecordKey = (record: DiscoverySearchEventRecord): string =>
    record.id || `${record.queryText || "query"}-${record.createdAt || "unknown"}`;

const isSameEventQuery = (
    left: service.DiscoverySearchEventPageQuery,
    right: service.DiscoverySearchEventPageQuery
) => JSON.stringify(left) === JSON.stringify(right);

export const SearchEventPanel = () => {
    const [queryText, setQueryText] = useState("礼器");
    const [searchStatuses, setSearchStatuses] = useState("SUCCESS");
    const [dateRange, setDateRange] = useState<DateRangeValue>(null);
    const [eventQuery, setEventQuery] = useState<service.DiscoverySearchEventPageQuery | null>(
        null
    );
    const [expandedRowKeys, setExpandedRowKeys] = useState<Key[]>([]);
    const eventPageQuery = useQuery({
        enabled: eventQuery !== null,
        queryFn: () => service.pageSearchEvents(eventQuery ?? {}),
        queryKey: ["discovery-search-statistics", "events", eventQuery],
        retry: false
    });
    const pageResult = eventPageQuery.data;
    const currentPageSize = pageResult?.pageSize ?? eventQuery?.pageSize ?? DEFAULT_PAGE_SIZE;
    const hasActiveFilters = Boolean(
        queryText.trim() || searchStatuses.trim() || dateRange?.[0] || dateRange?.[1]
    );
    const buildEventQuery = (
        nextQuery: Partial<service.DiscoverySearchEventPageQuery> = {}
    ): service.DiscoverySearchEventPageQuery => ({
        dateFrom: dateRange?.[0]?.format(DATE_TIME_FORMAT) || null,
        dateTo: dateRange?.[1]?.format(DATE_TIME_FORMAT) || null,
        pageNo: eventQuery?.pageNo ?? DEFAULT_PAGE_NO,
        pageSize: eventQuery?.pageSize ?? DEFAULT_PAGE_SIZE,
        queryText: queryText.trim() || null,
        searchStatuses: searchStatuses
            .split(/[,\s]+/gu)
            .map((value) => value.trim())
            .filter(Boolean),
        ...nextQuery
    });
    const resetFilters = () => {
        setQueryText("");
        setSearchStatuses("");
        setDateRange(null);
        setEventQuery(null);
        setExpandedRowKeys([]);
    };
    const queryEvents = () => {
        const nextQuery = buildEventQuery({ pageNo: DEFAULT_PAGE_NO });
        setExpandedRowKeys([]);
        if (eventQuery && isSameEventQuery(eventQuery, nextQuery)) {
            void eventPageQuery.refetch();
            return;
        }
        setEventQuery(nextQuery);
    };
    const columns: KuzhambuTableProps<DiscoverySearchEventRecord>["columns"] = [
        { title: "检索编号", dataIndex: "id", key: "id", width: 160 },
        { title: "搜索词", dataIndex: "queryText", key: "queryText" },
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
    const totalCount = pageResult?.totalCount ?? pageResult?.count ?? 0;

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <KuzhambuCard className="search-statistics-record-filter-card" size="small">
                <KuzhambuSpace className="search-statistics-record-filter-form" wrap>
                    <label>
                        <Text className="search-statistics-filter-label" type="secondary">
                            搜索词
                        </Text>
                        <Input
                            allowClear
                            aria-label="搜索词"
                            className="search-statistics-record-filter-input"
                            value={queryText}
                            onChange={(event) => setQueryText(event.target.value)}
                            style={{ width: 160 }}
                        />
                    </label>
                    <label>
                        <Text className="search-statistics-filter-label" type="secondary">
                            状态
                        </Text>
                        <Input
                            allowClear
                            aria-label="状态"
                            className="search-statistics-record-filter-input"
                            value={searchStatuses}
                            onChange={(event) => setSearchStatuses(event.target.value)}
                            style={{ width: 136 }}
                        />
                    </label>
                    <label className="search-statistics-record-filter-range-field">
                        <Text className="search-statistics-filter-label" type="secondary">
                            时间范围
                        </Text>
                        <RangePicker
                            aria-label="检索记录时间范围"
                            className="search-statistics-record-range-picker"
                            format={DATE_TIME_FORMAT}
                            showTime
                            value={dateRange}
                            onChange={setDateRange}
                        />
                    </label>
                    <KuzhambuSpace className="search-statistics-record-filter-actions">
                        <KuzhambuButton
                            testId="discovery-search-statistics-search-statistics-query-events-button"
                            loading={eventPageQuery.isFetching}
                            onClick={queryEvents}
                            type="primary"
                        >
                            查询记录
                        </KuzhambuButton>
                        <KuzhambuButton
                            ariaLabel="重置"
                            disabled={!hasActiveFilters && eventQuery === null}
                            testId="discovery-search-statistics-search-statistics-clear-result-button"
                            onClick={resetFilters}
                        >
                            重置
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </KuzhambuCard>

            <KuzhambuCard className="search-statistics-record-table-card" size="small">
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    {eventPageQuery.isError ? <Text type="danger">检索记录加载失败。</Text> : null}
                    <KuzhambuTable
                        ariaLabel="检索记录表格"
                        columns={columns}
                        dataSource={pageResult?.records ?? []}
                        expandable={{
                            expandedRowKeys,
                            expandedRowRender: (record) => <SearchEventDetail record={record} />,
                            onExpand: (expanded, record) => {
                                const recordKey = readRecordKey(record);
                                setExpandedRowKeys((currentKeys) =>
                                    expanded
                                        ? [
                                              ...currentKeys.filter((key) => key !== recordKey),
                                              recordKey
                                          ]
                                        : currentKeys.filter((key) => key !== recordKey)
                                );
                            }
                        }}
                        loading={eventPageQuery.isFetching}
                        pagination={{
                            current: pageResult?.pageNo ?? eventQuery?.pageNo ?? DEFAULT_PAGE_NO,
                            pageSize: currentPageSize,
                            total: totalCount,
                            showSizeChanger: true,
                            showTotal: (total) => `共 ${total} 条`,
                            onChange: (pageNo, pageSize) => {
                                setExpandedRowKeys([]);
                                setEventQuery((currentQuery) => ({
                                    ...(currentQuery ?? buildEventQuery()),
                                    pageNo,
                                    pageSize
                                }));
                            }
                        }}
                        rowKey={readRecordKey}
                        size="small"
                        scroll={{ x: 1120 }}
                    />
                    <Text type="secondary">
                        {pageResult ? `共 ${totalCount} 条记录` : "暂无检索记录。"}
                    </Text>
                </KuzhambuSpace>
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
