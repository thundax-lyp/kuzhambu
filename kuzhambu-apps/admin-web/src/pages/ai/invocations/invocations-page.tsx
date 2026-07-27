import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { App, Form, Statistic, Tooltip } from "antd";
import type { TablePaginationConfig } from "antd/es/table";
import dayjs from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage, KuzhambuTabs, KuzhambuButton, KuzhambuCard } from "@/components";

import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { InvocationDetailDrawer } from "./components/invocation-detail-drawer";
import {
    InvocationFilterPanel,
    type InvocationLogFilterValues,
    type InvocationDateRangeValue,
    type InvocationSummaryFilterValues
} from "./components/invocation-filter-panel";
import { InvocationTable } from "./components/invocation-table";
import * as service from "./invocations-service";
import type { AiInvocationLogPageQuery, AiInvocationSummaryQuery } from "./invocations-service";
import type { AiInvocationLogRecord } from "./invocations-types";

import "./invocations-page.css";

const CAPABILITY_LABELS: Record<string, string> = {
    classics_summary: "古籍摘要",
    classics_tags: "古籍标签",
    classics_tagging: "古籍标签",
    classics_qa: "古籍问答",
    classics_translate: "古籍翻译",
    classics_image_describe: "古籍图片理解",
    classics_image_generate: "古籍图片生成",
    classics_image_prompt_fusion: "古籍图文融合",
    classics_visual_describe: "古籍视觉描述",
    discovery_query_understanding: "查询理解",
    discovery_answer_generation: "回答生成",
    knowledge_graph_extract: "知识图谱抽取",
    platform_prompt_optimize: "提示词优化",
    platform_version_summary: "版本摘要"
};

const defaultPeriod: InvocationDateRangeValue = [dayjs().subtract(7, "day"), dayjs()];

const rangeToIso = (range?: InvocationDateRangeValue) => ({
    start: range?.[0]?.toISOString() || null,
    end: range?.[1]?.toISOString() || null
});

const buildSummaryQuery = (values: InvocationSummaryFilterValues): AiInvocationSummaryQuery => {
    const range = rangeToIso(values.period);
    return {
        periodStart: range.start,
        periodEnd: range.end,
        bucketType: values.bucketType || "DAY",
        capability: values.capability || null
    };
};

const buildInvocationLogQuery = (
    values: InvocationLogFilterValues,
    pageNo: number,
    pageSize: number
): AiInvocationLogPageQuery => {
    const range = rangeToIso(values.requestedAt);
    return {
        status: values.status || null,
        requestedAtStart: range.start,
        requestedAtEnd: range.end,
        pageNo,
        pageSize
    };
};

export const InvocationsPage = () => {
    const { message } = App.useApp();
    const [summaryForm] = Form.useForm<InvocationSummaryFilterValues>();
    const [callsForm] = Form.useForm<InvocationLogFilterValues>();
    const canViewInvocation = hasPermission("ai:invocation:view");
    const [detailInvocationLog, setDetailInvocationLog] = useState<AiInvocationLogRecord | null>(
        null
    );
    const [summaryQuery, setSummaryQuery] = useState<AiInvocationSummaryQuery>(() =>
        buildSummaryQuery({ period: defaultPeriod, bucketType: "DAY" })
    );
    const [invocationLogQuery, setInvocationLogQuery] = useState<AiInvocationLogPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });

    const invocationCapabilitiesQuery = useQuery({
        queryKey: ["ai", "invocations", "capabilities"],
        queryFn: service.listInvocationCapabilities,
        enabled: canViewInvocation,
        retry: false
    });

    const invocationSummaryQuery = useQuery({
        queryKey: ["ai", "invocations", "summary", summaryQuery],
        queryFn: () => service.getInvocationSummary(summaryQuery),
        enabled: canViewInvocation,
        retry: false
    });

    const invocationLogPageQuery = useQuery({
        queryKey: ["ai", "invocations", "calls", invocationLogQuery],
        queryFn: () => service.pageInvocationLogs(invocationLogQuery),
        enabled: canViewInvocation,
        retry: false
    });

    const capabilityOptions = useMemo(() => {
        return (invocationCapabilitiesQuery.data || []).map((record) => ({
            label: record.name || CAPABILITY_LABELS[record.capability] || record.capability,
            value: record.capability
        }));
    }, [invocationCapabilitiesQuery.data]);

    const capabilityLabelMap = useMemo(() => {
        return (invocationCapabilitiesQuery.data || []).reduce<Record<string, string>>(
            (labels, record) => ({
                ...labels,
                [record.capability]:
                    record.name || CAPABILITY_LABELS[record.capability] || record.capability
            }),
            { ...CAPABILITY_LABELS }
        );
    }, [invocationCapabilitiesQuery.data]);

    const formatCapability = (capability?: string | null) => {
        if (!capability) {
            return "-";
        }
        return capabilityLabelMap[capability] || capability;
    };

    useEffect(() => {
        if (invocationSummaryQuery.isError) {
            const error = invocationSummaryQuery.error;
            message.error(error instanceof Error ? error.message : "调用统计加载失败");
        }
    }, [message, invocationSummaryQuery.error, invocationSummaryQuery.isError]);

    useEffect(() => {
        if (invocationLogPageQuery.isError) {
            const error = invocationLogPageQuery.error;
            message.error(error instanceof Error ? error.message : "调用记录加载失败");
        }
    }, [invocationLogPageQuery.error, invocationLogPageQuery.isError, message]);

    const refreshSummary = async () => {
        const values = await summaryForm.validateFields();
        setSummaryQuery(buildSummaryQuery(values));
    };

    const searchCalls = async (
        pageNo = DEFAULT_PAGE_NO,
        pageSize = invocationLogQuery.pageSize || DEFAULT_PAGE_SIZE
    ) => {
        const values = await callsForm.validateFields();
        setInvocationLogQuery(buildInvocationLogQuery(values, pageNo, pageSize));
    };

    const resetCalls = () => {
        callsForm.resetFields();
        setInvocationLogQuery({ pageNo: DEFAULT_PAGE_NO, pageSize: DEFAULT_PAGE_SIZE });
    };

    const handleTableChange = (pagination: TablePaginationConfig) => {
        const nextPageNo = pagination.current || DEFAULT_PAGE_NO;
        const nextPageSize = pagination.pageSize || DEFAULT_PAGE_SIZE;
        void searchCalls(nextPageNo, nextPageSize);
    };

    const summary = invocationSummaryQuery.data;
    const invocationLogPage = invocationLogPageQuery.data;

    const topCapabilities = summary?.topCapabilities || [];
    const topCapabilityMaxCount = Math.max(
        ...topCapabilities.map((record) => record.invocationCount),
        1
    );
    const summaryInitialValues: InvocationSummaryFilterValues = {
        period: defaultPeriod,
        bucketType: "DAY"
    };

    return (
        <>
            <KuzhambuPage
                className="invocations-page"
                title="调用统计"
                description="查看调用指标、能力排行、调用记录和详情"
                actions={
                    <Tooltip title="刷新">
                        <KuzhambuButton
                            testId="ai-invocations-invocations-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={
                                invocationSummaryQuery.isFetching ||
                                invocationLogPageQuery.isFetching
                            }
                            onClick={() => {
                                void invocationSummaryQuery.refetch();
                                void invocationLogPageQuery.refetch();
                            }}
                        />
                    </Tooltip>
                }
            >
                <KuzhambuTabs
                    testId="ai-invocations-invocations-tabs"
                    items={[
                        {
                            key: "summary",
                            label: "统计概览",
                            children: (
                                <>
                                    <InvocationFilterPanel
                                        callsForm={callsForm}
                                        capabilityOptions={capabilityOptions}
                                        summaryForm={summaryForm}
                                        summaryInitialValues={summaryInitialValues}
                                        type="summary"
                                        onRefreshSummary={() => void refreshSummary()}
                                        onResetCalls={resetCalls}
                                        onSearchCalls={() => void searchCalls()}
                                    />

                                    <div className="invocations-metrics">
                                        <KuzhambuCard>
                                            <Statistic
                                                title="调用次数"
                                                value={summary?.invocationCount || 0}
                                            />
                                        </KuzhambuCard>
                                        <KuzhambuCard>
                                            <Statistic
                                                title="成功调用次数"
                                                value={summary?.succeededInvocationCount || 0}
                                            />
                                        </KuzhambuCard>
                                        <KuzhambuCard>
                                            <Statistic
                                                title="失败调用次数"
                                                value={summary?.failedInvocationCount || 0}
                                            />
                                        </KuzhambuCard>
                                        <KuzhambuCard>
                                            <Statistic
                                                title="平均耗时毫秒"
                                                value={summary?.avgLatencyMs || 0}
                                            />
                                        </KuzhambuCard>
                                    </div>

                                    <KuzhambuCard
                                        className="invocations-section-card"
                                        title="能力排行"
                                    >
                                        <div
                                            aria-label="AI 能力排行"
                                            className="invocations-capability-bars"
                                        >
                                            {topCapabilities.length > 0 ? (
                                                topCapabilities.map((record) => (
                                                    <div
                                                        className="invocations-capability-bar-row"
                                                        key={record.capability}
                                                    >
                                                        <div className="invocations-capability-bar-label">
                                                            {formatCapability(record.capability)}
                                                        </div>
                                                        <div className="invocations-capability-bar-track">
                                                            <div
                                                                className="invocations-capability-bar-fill"
                                                                style={{
                                                                    width: `${Math.max(
                                                                        (record.invocationCount /
                                                                            topCapabilityMaxCount) *
                                                                            100,
                                                                        4
                                                                    )}%`
                                                                }}
                                                            />
                                                        </div>
                                                        <div className="invocations-capability-bar-value">
                                                            {record.invocationCount}
                                                        </div>
                                                    </div>
                                                ))
                                            ) : (
                                                <div className="invocations-capability-bar-empty">
                                                    暂无能力排行
                                                </div>
                                            )}
                                        </div>
                                    </KuzhambuCard>
                                </>
                            )
                        },
                        {
                            key: "calls",
                            label: "调用记录",
                            children: (
                                <KuzhambuCard className="invocations-section-card">
                                    <InvocationFilterPanel
                                        callsForm={callsForm}
                                        capabilityOptions={capabilityOptions}
                                        summaryForm={summaryForm}
                                        summaryInitialValues={summaryInitialValues}
                                        type="calls"
                                        onRefreshSummary={() => void refreshSummary()}
                                        onResetCalls={resetCalls}
                                        onSearchCalls={() => void searchCalls()}
                                    />

                                    <InvocationTable
                                        invocationLogPage={invocationLogPage}
                                        currentPageNo={invocationLogQuery.pageNo || DEFAULT_PAGE_NO}
                                        currentPageSize={
                                            invocationLogQuery.pageSize || DEFAULT_PAGE_SIZE
                                        }
                                        formatCapability={formatCapability}
                                        loading={invocationLogPageQuery.isFetching}
                                        onChange={handleTableChange}
                                        onOpenDetail={setDetailInvocationLog}
                                    />
                                </KuzhambuCard>
                            )
                        }
                    ]}
                />
            </KuzhambuPage>
            <InvocationDetailDrawer
                call={detailInvocationLog}
                open={Boolean(detailInvocationLog)}
                onClose={() => setDetailInvocationLog(null)}
            />
        </>
    );
};
