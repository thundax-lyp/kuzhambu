import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { App, Card, Form, Statistic, Tooltip } from "antd";
import type { TablePaginationConfig } from "antd/es/table";
import dayjs from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuTabs } from "@/components/kuzhambu-tabs";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { InvocationDetailDrawer } from "./components/invocation-detail-drawer";
import {
    InvocationFilterPanel,
    type InvocationCallsFilterValues,
    type InvocationDateRangeValue,
    type InvocationSummaryFilterValues
} from "./components/invocation-filter-panel";
import { InvocationTable } from "./components/invocation-table";
import * as service from "./invocations-service";
import type { AiCallRecordPageQuery, AiInvocationSummaryQuery } from "./invocations-service";
import type { AiCallRecord } from "./invocations-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
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

const buildCallsQuery = (
    values: InvocationCallsFilterValues,
    pageNo: number,
    pageSize: number
): AiCallRecordPageQuery => {
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
    const [callsForm] = Form.useForm<InvocationCallsFilterValues>();
    const canViewInvocation = hasPermission("ai:invocation:view");
    const [detailCall, setDetailCall] = useState<AiCallRecord | null>(null);
    const [summaryQuery, setSummaryQuery] = useState<AiInvocationSummaryQuery>(() =>
        buildSummaryQuery({ period: defaultPeriod, bucketType: "DAY" })
    );
    const [callsQuery, setCallsQuery] = useState<AiCallRecordPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });

    const capabilitiesQuery = useQuery({
        queryKey: ["ai", "invocations", "capabilities"],
        queryFn: service.listInvocationCapabilities,
        enabled: canViewInvocation,
        retry: false
    });

    const summaryResult = useQuery({
        queryKey: ["ai", "invocations", "summary", summaryQuery],
        queryFn: () => service.getInvocationSummary(summaryQuery),
        enabled: canViewInvocation,
        retry: false
    });

    const callsResult = useQuery({
        queryKey: ["ai", "invocations", "calls", callsQuery],
        queryFn: () => service.pageInvocationCalls(callsQuery),
        enabled: canViewInvocation,
        retry: false
    });

    const capabilityOptions = useMemo(() => {
        return (capabilitiesQuery.data || []).map((record) => ({
            label: record.name || CAPABILITY_LABELS[record.capability] || record.capability,
            value: record.capability
        }));
    }, [capabilitiesQuery.data]);

    const capabilityLabelMap = useMemo(() => {
        return (capabilitiesQuery.data || []).reduce<Record<string, string>>(
            (labels, record) => ({
                ...labels,
                [record.capability]:
                    record.name || CAPABILITY_LABELS[record.capability] || record.capability
            }),
            { ...CAPABILITY_LABELS }
        );
    }, [capabilitiesQuery.data]);

    const formatCapability = (capability?: string | null) => {
        if (!capability) {
            return "-";
        }
        return capabilityLabelMap[capability] || capability;
    };

    useEffect(() => {
        if (summaryResult.isError) {
            const error = summaryResult.error;
            message.error(error instanceof Error ? error.message : "调用统计加载失败");
        }
    }, [message, summaryResult.error, summaryResult.isError]);

    useEffect(() => {
        if (callsResult.isError) {
            const error = callsResult.error;
            message.error(error instanceof Error ? error.message : "调用记录加载失败");
        }
    }, [callsResult.error, callsResult.isError, message]);

    const refreshSummary = async () => {
        const values = await summaryForm.validateFields();
        setSummaryQuery(buildSummaryQuery(values));
    };

    const searchCalls = async (
        pageNo = DEFAULT_PAGE_NO,
        pageSize = callsQuery.pageSize || DEFAULT_PAGE_SIZE
    ) => {
        const values = await callsForm.validateFields();
        setCallsQuery(buildCallsQuery(values, pageNo, pageSize));
    };

    const resetCalls = () => {
        callsForm.resetFields();
        setCallsQuery({ pageNo: DEFAULT_PAGE_NO, pageSize: DEFAULT_PAGE_SIZE });
    };

    const handleTableChange = (pagination: TablePaginationConfig) => {
        const nextPageNo = pagination.current || DEFAULT_PAGE_NO;
        const nextPageSize = pagination.pageSize || DEFAULT_PAGE_SIZE;
        void searchCalls(nextPageNo, nextPageSize);
    };

    const summary = summaryResult.data;
    const callPage = callsResult.data;

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
                            loading={summaryResult.isFetching || callsResult.isFetching}
                            onClick={() => {
                                void summaryResult.refetch();
                                void callsResult.refetch();
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
                                        <Card>
                                            <Statistic
                                                title="调用次数"
                                                value={summary?.invocationCount || 0}
                                            />
                                        </Card>
                                        <Card>
                                            <Statistic
                                                title="成功调用次数"
                                                value={summary?.succeededInvocationCount || 0}
                                            />
                                        </Card>
                                        <Card>
                                            <Statistic
                                                title="失败调用次数"
                                                value={summary?.failedInvocationCount || 0}
                                            />
                                        </Card>
                                        <Card>
                                            <Statistic
                                                title="平均耗时毫秒"
                                                value={summary?.avgLatencyMs || 0}
                                            />
                                        </Card>
                                    </div>

                                    <Card className="invocations-section-card" title="能力排行">
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
                                    </Card>
                                </>
                            )
                        },
                        {
                            key: "calls",
                            label: "调用记录",
                            children: (
                                <Card className="invocations-section-card">
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
                                        callPage={callPage}
                                        currentPageNo={callsQuery.pageNo || DEFAULT_PAGE_NO}
                                        currentPageSize={callsQuery.pageSize || DEFAULT_PAGE_SIZE}
                                        formatCapability={formatCapability}
                                        loading={callsResult.isFetching}
                                        onChange={handleTableChange}
                                        onOpenDetail={setDetailCall}
                                    />
                                </Card>
                            )
                        }
                    ]}
                />
            </KuzhambuPage>
            <InvocationDetailDrawer
                call={detailCall}
                open={Boolean(detailCall)}
                onClose={() => setDetailCall(null)}
            />
        </>
    );
};
