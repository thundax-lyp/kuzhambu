import { ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import {
    App,
    Card,
    DatePicker,
    Descriptions,
    Form,
    Select,
    Statistic,
    Table,
    Tag,
    Tooltip
} from "antd";
import type { ColumnsType, TablePaginationConfig } from "antd/es/table";
import type { Dayjs } from "dayjs";
import dayjs from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTabs } from "@/components/kuzhambu-tabs";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "@/types/page";
import * as service from "./invocations-service";
import type { AiCallRecordPageQuery, AiInvocationSummaryQuery } from "./invocations-service";
import type { AiCallRecord } from "./invocations-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./invocations-page.css";

const { RangePicker } = DatePicker;

type DateRangeValue = [Dayjs | null, Dayjs | null] | null;
type SummaryFormValues = AiInvocationSummaryQuery & { period?: DateRangeValue };
type CallsFormValues = AiCallRecordPageQuery & { requestedAt?: DateRangeValue };
const DATE_TIME_FORMAT = "YYYYMMDD HH:mm";

const STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "待处理", value: "PENDING" },
    { label: "运行中", value: "RUNNING" }
];

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

const defaultPeriod: DateRangeValue = [dayjs().subtract(7, "day"), dayjs()];

const readCallId = (call: AiCallRecord) => call.callIdText || String(call.callId);

const rangeToIso = (range?: DateRangeValue) => ({
    start: range?.[0]?.toISOString() || null,
    end: range?.[1]?.toISOString() || null
});

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = dayjs(value);
    if (!timestamp.isValid()) {
        return value;
    }
    return timestamp.format(DATE_TIME_FORMAT);
};

const formatStatus = (status?: string | null) => {
    if (status === "SUCCEEDED") {
        return "成功";
    }
    if (status === "FAILED") {
        return "失败";
    }
    if (status === "PENDING") {
        return "待处理";
    }
    if (status === "RUNNING") {
        return "运行中";
    }
    return status || "-";
};

const formatWarnings = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    try {
        return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
        return value;
    }
};

const buildSummaryQuery = (values: SummaryFormValues): AiInvocationSummaryQuery => {
    const range = rangeToIso(values.period);
    return {
        periodStart: range.start,
        periodEnd: range.end,
        bucketType: values.bucketType || "DAY",
        capability: values.capability || null
    };
};

const buildCallsQuery = (
    values: CallsFormValues,
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
    const [summaryForm] = Form.useForm<SummaryFormValues>();
    const [callsForm] = Form.useForm<CallsFormValues>();
    const canViewInvocation = hasPermission("ai:invocation:view");
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

    const callColumns: ColumnsType<AiCallRecord> = [
        {
            title: "能力",
            dataIndex: "capability",
            key: "capability",
            render: formatCapability
        },
        {
            title: "内容类型",
            dataIndex: "contentType",
            key: "contentType",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "内容ID",
            dataIndex: "contentId",
            key: "contentId",
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "模型名称",
            dataIndex: "modelName",
            key: "modelName",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            render: (status?: string | null) => (
                <Tag color={status === "SUCCEEDED" ? "green" : "red"}>{formatStatus(status)}</Tag>
            )
        },
        {
            title: "耗时毫秒",
            dataIndex: "latencyMs",
            key: "latencyMs",
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "请求时间",
            dataIndex: "requestedAt",
            key: "requestedAt",
            className: "invocations-nowrap-column",
            render: formatDateTime
        }
    ];
    const topCapabilities = summary?.topCapabilities || [];
    const topCapabilityMaxCount = Math.max(
        ...topCapabilities.map((record) => record.invocationCount),
        1
    );

    return (
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
                                <Card className="invocations-filter-card">
                                    <Form
                                        form={summaryForm}
                                        layout="inline"
                                        className="invocations-filter-form"
                                        initialValues={{
                                            period: defaultPeriod,
                                            bucketType: "DAY"
                                        }}
                                    >
                                        <Form.Item label="周期" name="period">
                                            <RangePicker
                                                aria-label="周期"
                                                format={DATE_TIME_FORMAT}
                                                showTime
                                            />
                                        </Form.Item>
                                        <Form.Item label="统计粒度" name="bucketType">
                                            <Select
                                                className="invocations-filter-control"
                                                options={[
                                                    { label: "按天", value: "DAY" },
                                                    { label: "按小时", value: "HOUR" }
                                                ]}
                                            />
                                        </Form.Item>
                                        <Form.Item label="能力" name="capability">
                                            <Select
                                                allowClear
                                                className="invocations-filter-control"
                                                options={capabilityOptions}
                                            />
                                        </Form.Item>
                                        <Form.Item>
                                            <KuzhambuButton
                                                testId="ai-invocations-invocations-refresh-button-2"
                                                type="primary"
                                                onClick={() => void refreshSummary()}
                                            >
                                                刷新
                                            </KuzhambuButton>
                                        </Form.Item>
                                    </Form>
                                </Card>

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
                                <Form
                                    form={callsForm}
                                    layout="inline"
                                    className="invocations-filter-form"
                                >
                                    <Form.Item label="状态" name="status">
                                        <Select
                                            allowClear
                                            className="invocations-filter-control"
                                            options={STATUS_OPTIONS}
                                        />
                                    </Form.Item>
                                    <Form.Item label="请求时间" name="requestedAt">
                                        <RangePicker
                                            aria-label="请求时间"
                                            format={DATE_TIME_FORMAT}
                                            showTime
                                        />
                                    </Form.Item>
                                    <Form.Item>
                                        <KuzhambuSpace>
                                            <KuzhambuButton
                                                testId="ai-invocations-invocations-query-button"
                                                type="primary"
                                                onClick={() => void searchCalls()}
                                            >
                                                查询
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                testId="ai-invocations-invocations-reset-button"
                                                onClick={resetCalls}
                                            >
                                                重置
                                            </KuzhambuButton>
                                        </KuzhambuSpace>
                                    </Form.Item>
                                </Form>

                                <Table<AiCallRecord>
                                    aria-label="AI 调用记录"
                                    rowKey={readCallId}
                                    className="invocations-table"
                                    columns={callColumns}
                                    dataSource={callPage?.records || []}
                                    loading={callsResult.isFetching}
                                    pagination={{
                                        current: callsQuery.pageNo || DEFAULT_PAGE_NO,
                                        pageSize: callsQuery.pageSize || DEFAULT_PAGE_SIZE,
                                        pageSizeOptions: PAGE_SIZE_OPTIONS,
                                        showSizeChanger: true,
                                        total: callPage?.totalCount ?? callPage?.count ?? 0
                                    }}
                                    expandable={{
                                        expandedRowRender: (record) => <CallDetail call={record} />,
                                        expandRowByClick: true,
                                        rowExpandable: () => true
                                    }}
                                    onChange={handleTableChange}
                                />
                            </Card>
                        )
                    }
                ]}
            />
        </KuzhambuPage>
    );
};

const CallDetail = ({ call }: { call: AiCallRecord }) => (
    <Descriptions column={1} size="small">
        <Descriptions.Item label="请求ID">{call.requestId || "-"}</Descriptions.Item>
        <Descriptions.Item label="链路ID">{call.traceId || "-"}</Descriptions.Item>
        <Descriptions.Item label="提示词版本ID">{call.promptVersionId || "-"}</Descriptions.Item>
        <Descriptions.Item label="是否流式">{call.streamUsed ? "是" : "否"}</Descriptions.Item>
        <Descriptions.Item label="流式是否完成">
            {call.streamCompleted ? "是" : "否"}
        </Descriptions.Item>
        <Descriptions.Item label="输入 Tokens">{call.inputTokens ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="输出 Tokens">{call.outputTokens ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="失败阶段">{call.failureStage || "-"}</Descriptions.Item>
        <Descriptions.Item label="结果格式">{call.resultFormat || "-"}</Descriptions.Item>
        <Descriptions.Item label="错误类型">{call.errorType || "-"}</Descriptions.Item>
        <Descriptions.Item label="错误信息">{call.errorMessage || "-"}</Descriptions.Item>
        <Descriptions.Item label="警告 JSON">
            <pre className="invocations-warnings">{formatWarnings(call.warningsJson)}</pre>
        </Descriptions.Item>
    </Descriptions>
);
