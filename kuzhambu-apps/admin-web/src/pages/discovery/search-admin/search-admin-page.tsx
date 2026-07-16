import { useMutation } from "@tanstack/react-query";
import { Card, Checkbox, Descriptions, Input, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useState } from "react";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as service from "./search-admin-service";
import type {
    DiscoverySearchAnalysisSummaryRecord,
    DiscoverySearchLogDetailRecord,
    DiscoverySearchLogPageRecord,
    DiscoverySearchLogRecord
} from "./search-admin-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./search-admin-page.css";

const { Text, Title } = Typography;

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

export const SearchAdminPage = () => {
    const [queryText, setQueryText] = useState("礼器");
    const [operatorId, setOperatorId] = useState("");
    const [pageNo, setPageNo] = useState("1");
    const [pageSize, setPageSize] = useState("10");
    const [intentTypes, setIntentTypes] = useState("REWRITE");
    const [searchStatuses, setSearchStatuses] = useState("SUCCESS");
    const [dateFrom, setDateFrom] = useState("");
    const [dateTo, setDateTo] = useState("");
    const [searchLogId, setSearchLogId] = useState("LOG-1001");
    const [confirmRebuild, setConfirmRebuild] = useState(true);
    const [pageResult, setPageResult] = useState<DiscoverySearchLogPageRecord | null>(null);
    const [detailResult, setDetailResult] = useState<DiscoverySearchLogDetailRecord | null>(null);
    const [rebuildResult, setRebuildResult] = useState<number | null>(null);
    const [analysisResult, setAnalysisResult] =
        useState<DiscoverySearchAnalysisSummaryRecord | null>(null);

    const pageMutation = useMutation({
        mutationFn: service.pageSearchLogs,
        onSuccess: (nextPage) => {
            setPageResult(nextPage);
        }
    });
    const detailMutation = useMutation({
        mutationFn: service.getSearchLogDetail,
        onSuccess: (nextDetail) => {
            setDetailResult(nextDetail);
        }
    });
    const rebuildMutation = useMutation({
        mutationFn: service.rebuildSearchIndex,
        onSuccess: (nextCount) => {
            setRebuildResult(nextCount);
        }
    });
    const analysisMutation = useMutation({
        mutationFn: service.getSearchAnalysisSummary,
        onSuccess: (nextSummary) => {
            setAnalysisResult(nextSummary);
        }
    });

    const pageColumns: ColumnsType<DiscoverySearchLogRecord> = [
        { title: "日志号", dataIndex: "searchLogId", key: "searchLogId", width: 160 },
        { title: "搜索词", dataIndex: "queryText", key: "queryText", width: 180 },
        { title: "回显词", dataIndex: "displayQueryText", key: "displayQueryText", width: 180 },
        { title: "意图", dataIndex: "intentType", key: "intentType", width: 120 },
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

    return (
        <main className="kuzhambu-page discovery-admin-page search-admin-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Text className="kuzhambu-page-eyebrow">Discovery / Search Admin</Text>
                        <Title level={2}>搜索调试台</Title>
                        <Text type="secondary">查看搜索日志、打开日志详情并手动触发索引重建。</Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Card title="搜索分析摘要" size="small">
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <KuzhambuSpace wrap>
                                <KuzhambuButton
                                    testId="discovery-search-admin-search-admin-action-button"
                                    loading={analysisMutation.isPending}
                                    onClick={() =>
                                        analysisMutation.mutate({
                                            dateFrom: dateFrom || null,
                                            dateTo: dateTo || null
                                        })
                                    }
                                    type="primary"
                                >
                                    刷新分析
                                </KuzhambuButton>
                                <Text type="secondary">
                                    分析范围复用下方搜索日志的起始时间和结束时间。
                                </Text>
                            </KuzhambuSpace>
                            <Descriptions
                                bordered
                                column={4}
                                items={[
                                    {
                                        key: "searchCount",
                                        label: "搜索次数",
                                        children: analysisResult?.searchCount ?? 0
                                    },
                                    {
                                        key: "failedSearchCount",
                                        label: "失败次数",
                                        children: analysisResult?.failedSearchCount ?? 0
                                    },
                                    {
                                        key: "zeroResultSearchCount",
                                        label: "零结果次数",
                                        children: analysisResult?.zeroResultSearchCount ?? 0
                                    },
                                    {
                                        key: "clickCount",
                                        label: "点击次数",
                                        children: analysisResult?.clickCount ?? 0
                                    }
                                ]}
                                size="small"
                            />
                            <div className="search-admin-top-query-list">
                                <Text strong>热门搜索词</Text>
                                {(analysisResult?.topQueries ?? []).length ? (
                                    <ol>
                                        {(analysisResult?.topQueries ?? []).map(
                                            (topQuery, index) => (
                                                <li
                                                    key={`${topQuery.queryText ?? "query"}-${index}`}
                                                >
                                                    <span>{topQuery.queryText ?? "-"}</span>
                                                    <Text type="secondary">
                                                        {topQuery.count ?? 0} 次
                                                    </Text>
                                                </li>
                                            )
                                        )}
                                    </ol>
                                ) : (
                                    <Text type="secondary">暂无热门搜索词。</Text>
                                )}
                            </div>
                        </KuzhambuSpace>
                    </Card>

                    <Card title="搜索日志分页" size="small">
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <KuzhambuSpace wrap>
                                <label>
                                    <Text type="secondary">搜索词</Text>
                                    <Input
                                        aria-label="搜索词"
                                        value={queryText}
                                        onChange={(event) => setQueryText(event.target.value)}
                                        style={{ width: 180 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">操作者</Text>
                                    <Input
                                        aria-label="操作者"
                                        value={operatorId}
                                        onChange={(event) => setOperatorId(event.target.value)}
                                        style={{ width: 160 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">意图</Text>
                                    <Input
                                        aria-label="意图"
                                        value={intentTypes}
                                        onChange={(event) => setIntentTypes(event.target.value)}
                                        style={{ width: 160 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">状态</Text>
                                    <Input
                                        aria-label="状态"
                                        value={searchStatuses}
                                        onChange={(event) => setSearchStatuses(event.target.value)}
                                        style={{ width: 160 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">起始时间</Text>
                                    <Input
                                        aria-label="起始时间"
                                        value={dateFrom}
                                        onChange={(event) => setDateFrom(event.target.value)}
                                        style={{ width: 200 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">结束时间</Text>
                                    <Input
                                        aria-label="结束时间"
                                        value={dateTo}
                                        onChange={(event) => setDateTo(event.target.value)}
                                        style={{ width: 200 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">页码</Text>
                                    <Input
                                        aria-label="页码"
                                        value={pageNo}
                                        onChange={(event) => setPageNo(event.target.value)}
                                        style={{ width: 100 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">页大小</Text>
                                    <Input
                                        aria-label="页大小"
                                        value={pageSize}
                                        onChange={(event) => setPageSize(event.target.value)}
                                        style={{ width: 100 }}
                                    />
                                </label>
                            </KuzhambuSpace>

                            <KuzhambuSpace wrap>
                                <KuzhambuButton
                                    testId="discovery-search-admin-search-admin-query-logs-button"
                                    loading={pageMutation.isPending}
                                    onClick={() =>
                                        pageMutation.mutate({
                                            dateFrom: dateFrom || null,
                                            dateTo: dateTo || null,
                                            intentTypes: intentTypes
                                                .split(/[,\s]+/gu)
                                                .map((value) => value.trim())
                                                .filter(Boolean),
                                            operatorId: operatorId || null,
                                            pageNo: Number.parseInt(pageNo, 10) || 1,
                                            pageSize: Number.parseInt(pageSize, 10) || 10,
                                            queryText: queryText || null,
                                            searchStatuses: searchStatuses
                                                .split(/[,\s]+/gu)
                                                .map((value) => value.trim())
                                                .filter(Boolean)
                                        })
                                    }
                                    type="primary"
                                >
                                    查询日志
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="discovery-search-admin-search-admin-clear-result-button"
                                    onClick={() => setPageResult(null)}
                                >
                                    清空结果
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </KuzhambuSpace>

                        <Table
                            aria-label="搜索日志表格"
                            columns={pageColumns}
                            dataSource={pageResult?.records ?? []}
                            pagination={false}
                            rowKey={(record) =>
                                record.searchLogId || `${record.queryText}-${record.createdAt}`
                            }
                            size="small"
                            style={{ marginTop: 16 }}
                        />
                        <Text type="secondary">
                            {pageResult
                                ? `共 ${pageResult.count ?? pageResult.totalCount ?? 0} 条记录`
                                : "暂无日志。"}
                        </Text>
                    </Card>

                    <Card title="日志详情" size="small">
                        <KuzhambuSpace wrap>
                            <label>
                                <Text type="secondary">日志号</Text>
                                <Input
                                    aria-label="日志号"
                                    value={searchLogId}
                                    onChange={(event) => setSearchLogId(event.target.value)}
                                    style={{ width: 220 }}
                                />
                            </label>
                            <KuzhambuButton
                                testId="discovery-search-admin-search-admin-view-detail-button"
                                loading={detailMutation.isPending}
                                onClick={() =>
                                    detailMutation.mutate({
                                        searchLogId
                                    })
                                }
                                type="primary"
                            >
                                查看详情
                            </KuzhambuButton>
                        </KuzhambuSpace>

                        <Descriptions
                            bordered
                            column={2}
                            items={[
                                {
                                    key: "searchLogId",
                                    label: "日志号",
                                    children: detailResult?.searchLogId ?? "-"
                                },
                                {
                                    key: "queryText",
                                    label: "搜索词",
                                    children: detailResult?.queryText ?? "-"
                                },
                                {
                                    key: "normalizedQueryText",
                                    label: "清洗词",
                                    children: detailResult?.normalizedQueryText ?? "-"
                                },
                                {
                                    key: "displayQueryText",
                                    label: "回显词",
                                    children: detailResult?.displayQueryText ?? "-"
                                },
                                {
                                    key: "intentType",
                                    label: "意图",
                                    children: detailResult?.intentType ?? "-"
                                },
                                {
                                    key: "searchStatus",
                                    label: "状态",
                                    children: detailResult?.searchStatus ?? "-"
                                },
                                {
                                    key: "resultTotalCount",
                                    label: "总结果",
                                    children: detailResult?.resultTotalCount ?? "-"
                                },
                                {
                                    key: "groupTotalCount",
                                    label: "分组数",
                                    children: detailResult?.groupTotalCount ?? "-"
                                },
                                {
                                    key: "requestId",
                                    label: "请求号",
                                    children: detailResult?.requestId ?? "-"
                                },
                                {
                                    key: "traceId",
                                    label: "链路号",
                                    children: detailResult?.traceId ?? "-"
                                }
                            ]}
                            size="small"
                            style={{ marginTop: 16 }}
                        />
                        <KuzhambuSpace
                            orientation="vertical"
                            size={8}
                            style={{ marginTop: 16, width: "100%" }}
                        >
                            <Text strong>失败信息 / 检索范围</Text>
                            <Text type="secondary">{detailResult?.failureCode ?? "-"}</Text>
                            <Text>{detailResult?.failureMessage ?? "-"}</Text>
                            <Text code style={{ whiteSpace: "pre-wrap" }}>
                                {detailResult?.searchScopesJson ?? "-"}
                            </Text>
                        </KuzhambuSpace>
                    </Card>

                    <Card title="索引重建" size="small">
                        <KuzhambuSpace align="center" wrap>
                            <Checkbox
                                checked={confirmRebuild}
                                onChange={(event) => setConfirmRebuild(event.target.checked)}
                            >
                                确认全量重建
                            </Checkbox>
                            <KuzhambuButton
                                testId="discovery-search-admin-search-admin-trigger-rebuild-button"
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
                        <Text type="secondary">
                            {rebuildResult !== null
                                ? `重建结果：${rebuildResult}`
                                : "尚未触发重建。"}
                        </Text>
                    </Card>
                </KuzhambuSpace>
            </section>
        </main>
    );
};
