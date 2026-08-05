import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { App, Tooltip } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage, KuzhambuTabs, KuzhambuButton } from "@/components";

import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { InvocationCallsTab } from "./invocation-calls-tab";
import { InvocationDetailDrawer } from "./invocation-detail-drawer";
import { InvocationSummaryTab } from "./invocation-summary-tab";
import * as service from "./invocation-service";
import type { AiInvocationLogPageQuery, AiInvocationSummaryQuery } from "./invocation-service";
import type { AiInvocationLogRecord } from "./invocation-types";

import "./invocation-page.css";

const CAPABILITY_LABELS: Record<string, string> = {
    CLASSICS_SUMMARY: "古籍摘要",
    CLASSICS_TAG_EXTRACT: "古籍标签",
    CLASSICS_QA: "古籍问答",
    CLASSICS_TRANSLATE: "古籍翻译",
    CLASSICS_IMAGE_DESCRIBE: "古籍图片理解",
    CLASSICS_IMAGE_GENERATE: "古籍图片生成",
    CLASSICS_IMAGE_PROMPT_FUSION: "古籍图文融合",
    CLASSICS_VISUAL_DESCRIBE: "古籍视觉描述",
    DISCOVERY_QUERY_UNDERSTANDING: "查询理解",
    DISCOVERY_ANSWER_GENERATION: "回答生成",
    KNOWLEDGE_GRAPH_EXTRACT: "知识图谱抽取",
    PROMPT_SUGGEST: "提示词优化",
    PLATFORM_VERSION_SUMMARY: "版本摘要"
};

type InvocationDateRangeValue = [Dayjs | null, Dayjs | null] | null;

type InvocationSummaryFilterValues = AiInvocationSummaryQuery & {
    period?: InvocationDateRangeValue;
};

const buildSummaryQuery = (values: InvocationSummaryFilterValues): AiInvocationSummaryQuery => {
    const range = {
        start: values.period?.[0]?.toISOString() || null,
        end: values.period?.[1]?.toISOString() || null
    };
    return {
        periodStart: range.start,
        periodEnd: range.end,
        bucketType: values.bucketType || "DAY",
        capability: values.capability || null
    };
};

export const InvocationPage = () => {
    const { message } = App.useApp();
    const canViewInvocation = hasPermission("ai:invocation:view");
    const [detailInvocationLog, setDetailInvocationLog] = useState<AiInvocationLogRecord | null>(
        null
    );
    const [summaryQuery, setSummaryQuery] = useState<AiInvocationSummaryQuery>(() =>
        buildSummaryQuery({ period: [dayjs().subtract(7, "day"), dayjs()], bucketType: "DAY" })
    );
    const [invocationLogQuery, setInvocationLogQuery] = useState<AiInvocationLogPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });

    const invocationCapabilitiesQuery = useQuery({
        queryKey: ["ai", "invocation", "capabilities"],
        queryFn: service.listInvocationCapabilities,
        enabled: canViewInvocation,
        retry: false
    });

    const invocationSummaryQuery = useQuery({
        queryKey: ["ai", "invocation", "summary", summaryQuery],
        queryFn: () => service.getInvocationSummary(summaryQuery),
        enabled: canViewInvocation,
        retry: false
    });

    const invocationLogPageQuery = useQuery({
        queryKey: ["ai", "invocation", "calls", invocationLogQuery],
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

    const refreshSummary = (values: InvocationSummaryFilterValues) => {
        setSummaryQuery(buildSummaryQuery(values));
    };

    const summary = invocationSummaryQuery.data;
    const invocationLogPage = invocationLogPageQuery.data;

    return (
        <>
            <KuzhambuPage
                className="invocation-page"
                title="调用统计"
                description="查看调用指标、能力排行、调用记录和详情"
                actions={
                    <Tooltip title="刷新">
                        <KuzhambuButton
                            testId="ai-invocation-invocation-refresh-button"
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
                    testId="ai-invocation-invocation-tabs"
                    items={[
                        {
                            key: "summary",
                            label: "统计概览",
                            children: (
                                <InvocationSummaryTab
                                    capabilityOptions={capabilityOptions}
                                    formatCapability={formatCapability}
                                    summary={summary}
                                    onRefreshSummary={refreshSummary}
                                />
                            )
                        },
                        {
                            key: "calls",
                            label: "调用记录",
                            children: (
                                <InvocationCallsTab
                                    formatCapability={formatCapability}
                                    invocationLogPage={invocationLogPage}
                                    loading={invocationLogPageQuery.isFetching}
                                    onOpenDetail={setDetailInvocationLog}
                                    onSearchCalls={setInvocationLogQuery}
                                />
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
