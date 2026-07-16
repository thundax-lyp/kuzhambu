import { EyeOutlined, ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import {
    App,
    Card,
    DatePicker,
    Descriptions,
    Form,
    Input,
    InputNumber,
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
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "@/types/page";
import * as service from "./invocations-service";
import type { AiCallRecordPageQuery, AiInvocationSummaryQuery } from "./invocations-service";
import type { AiCallRecord, AiTopCapabilityRecord } from "./invocations-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./invocations-page.css";

const { RangePicker } = DatePicker;

type DateRangeValue = [Dayjs | null, Dayjs | null] | null;
type SummaryFormValues = AiInvocationSummaryQuery & { period?: DateRangeValue };
type CallsFormValues = AiCallRecordPageQuery & { requestedAt?: DateRangeValue };

const SCOPE_OPTIONS = [
    { label: "classics", value: "classics" },
    { label: "knowledge", value: "knowledge" },
    { label: "discovery", value: "discovery" },
    { label: "platform", value: "platform" }
];

const SERVICE_ROLE_OPTIONS = [
    { label: "PRIMARY", value: "PRIMARY" },
    { label: "BACKUP", value: "BACKUP" }
];

const STATUS_OPTIONS = [
    { label: "SUCCEEDED", value: "SUCCEEDED" },
    { label: "FAILED", value: "FAILED" },
    { label: "PENDING", value: "PENDING" },
    { label: "RUNNING", value: "RUNNING" }
];

const defaultPeriod: DateRangeValue = [dayjs().subtract(7, "day"), dayjs()];

const readCallId = (call: AiCallRecord) => call.callIdText || String(call.callId);

const rangeToIso = (range?: DateRangeValue) => ({
    start: range?.[0]?.toISOString() || null,
    end: range?.[1]?.toISOString() || null
});

const optionToBoolean = (value?: string) => {
    if (value === "true") {
        return true;
    }
    if (value === "false") {
        return false;
    }
    return null;
};

const formatDateTime = (value?: string | null) => {
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

const formatMoney = (value?: number | string | null) => {
    if (value == null || value === "") {
        return "0";
    }
    return String(value);
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
        scope: values.scope || null,
        capability: values.capability || null,
        serviceRole: values.serviceRole || null
    };
};

const buildCallsQuery = (
    values: CallsFormValues,
    pageNo: number,
    pageSize: number
): AiCallRecordPageQuery => {
    const range = rangeToIso(values.requestedAt);
    return {
        scope: values.scope || null,
        capability: values.capability || null,
        contentType: values.contentType || null,
        contentId: values.contentId || null,
        status: values.status || null,
        serviceRole: values.serviceRole || null,
        modelName: values.modelName || null,
        fallbackUsed: optionToBoolean(String(values.fallbackUsed ?? "")),
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
    const [selectedCall, setSelectedCall] = useState<AiCallRecord | null>(null);

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
            label: `${record.name} / ${record.capability}`,
            value: record.capability
        }));
    }, [capabilitiesQuery.data]);

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

    const topCapabilityColumns: ColumnsType<AiTopCapabilityRecord> = [
        {
            title: "capability",
            dataIndex: "capability",
            key: "capability"
        },
        {
            title: "invocationCount",
            dataIndex: "invocationCount",
            key: "invocationCount"
        }
    ];

    const callColumns: ColumnsType<AiCallRecord> = [
        {
            title: "callId",
            dataIndex: "callIdText",
            key: "callId",
            render: (_, record) => readCallId(record)
        },
        {
            title: "scope",
            dataIndex: "scope",
            key: "scope"
        },
        {
            title: "capability",
            dataIndex: "capability",
            key: "capability"
        },
        {
            title: "contentType",
            dataIndex: "contentType",
            key: "contentType",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "contentId",
            dataIndex: "contentId",
            key: "contentId",
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "serviceRole",
            dataIndex: "serviceRole",
            key: "serviceRole",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "modelName",
            dataIndex: "modelName",
            key: "modelName",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "status",
            dataIndex: "status",
            key: "status",
            render: (status?: string | null) => (
                <Tag color={status === "SUCCEEDED" ? "green" : "red"}>{status || "-"}</Tag>
            )
        },
        {
            title: "fallbackUsed",
            dataIndex: "fallbackUsed",
            key: "fallbackUsed",
            render: (value?: boolean | null) => (value ? "是" : "否")
        },
        {
            title: "latencyMs",
            dataIndex: "latencyMs",
            key: "latencyMs",
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "costAmount",
            dataIndex: "costAmount",
            key: "costAmount",
            render: formatMoney
        },
        {
            title: "requestedAt",
            dataIndex: "requestedAt",
            key: "requestedAt",
            render: formatDateTime
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuButton
                    name="详情"
                    icon={<EyeOutlined />}
                    onClick={() => setSelectedCall(record)}
                >
                    详情
                </KuzhambuButton>
            )
        }
    ];

    return (
        <KuzhambuPage
            className="invocations-page"
            eyebrow="AI"
            title="AI 调用统计"
            description="查看调用指标、能力排行、调用记录和详情"
            actions={
                <Tooltip title="刷新">
                    <KuzhambuButton
                        name="刷新"
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
            <Card className="invocations-filter-card">
                <Form
                    form={summaryForm}
                    layout="inline"
                    className="invocations-filter-form"
                    initialValues={{ period: defaultPeriod, bucketType: "DAY" }}
                >
                    <Form.Item label="周期" name="period">
                        <RangePicker aria-label="周期" showTime />
                    </Form.Item>
                    <Form.Item label="bucketType" name="bucketType">
                        <Select
                            className="invocations-filter-control"
                            options={[
                                { label: "DAY", value: "DAY" },
                                { label: "HOUR", value: "HOUR" }
                            ]}
                        />
                    </Form.Item>
                    <Form.Item label="scope" name="scope">
                        <Select
                            allowClear
                            className="invocations-filter-control"
                            options={SCOPE_OPTIONS}
                        />
                    </Form.Item>
                    <Form.Item label="capability" name="capability">
                        <Select
                            allowClear
                            className="invocations-filter-control"
                            options={capabilityOptions}
                        />
                    </Form.Item>
                    <Form.Item label="serviceRole" name="serviceRole">
                        <Select
                            allowClear
                            className="invocations-filter-control"
                            options={SERVICE_ROLE_OPTIONS}
                        />
                    </Form.Item>
                    <Form.Item>
                        <KuzhambuButton
                            name="刷新"
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
                    <Statistic title="invocationCount" value={summary?.invocationCount || 0} />
                </Card>
                <Card>
                    <Statistic
                        title="succeededInvocationCount"
                        value={summary?.succeededInvocationCount || 0}
                    />
                </Card>
                <Card>
                    <Statistic
                        title="failedInvocationCount"
                        value={summary?.failedInvocationCount || 0}
                    />
                </Card>
                <Card>
                    <Statistic title="avgLatencyMs" value={summary?.avgLatencyMs || 0} />
                </Card>
                <Card>
                    <Statistic
                        title="totalCostAmount"
                        value={formatMoney(summary?.totalCostAmount)}
                    />
                </Card>
            </div>

            <Card className="invocations-section-card" title="能力排行">
                <Table<AiTopCapabilityRecord>
                    aria-label="AI 能力排行"
                    rowKey="capability"
                    columns={topCapabilityColumns}
                    dataSource={summary?.topCapabilities || []}
                    loading={summaryResult.isFetching}
                    pagination={false}
                    size="small"
                />
            </Card>

            <Card className="invocations-section-card" title="调用记录">
                <Form form={callsForm} layout="inline" className="invocations-filter-form">
                    <Form.Item label="status" name="status">
                        <Select
                            allowClear
                            className="invocations-filter-control"
                            options={STATUS_OPTIONS}
                        />
                    </Form.Item>
                    <Form.Item label="contentType" name="contentType">
                        <Input className="invocations-filter-control" />
                    </Form.Item>
                    <Form.Item label="contentId" name="contentId">
                        <InputNumber className="invocations-filter-control" />
                    </Form.Item>
                    <Form.Item label="modelName" name="modelName">
                        <Input className="invocations-filter-control" />
                    </Form.Item>
                    <Form.Item label="fallbackUsed" name="fallbackUsed">
                        <Select
                            allowClear
                            className="invocations-filter-control"
                            options={[
                                { label: "是", value: "true" },
                                { label: "否", value: "false" }
                            ]}
                        />
                    </Form.Item>
                    <Form.Item label="requestedAt" name="requestedAt">
                        <RangePicker aria-label="requestedAt" showTime />
                    </Form.Item>
                    <Form.Item>
                        <KuzhambuSpace>
                            <KuzhambuButton
                                name="查询"
                                type="primary"
                                onClick={() => void searchCalls()}
                            >
                                查询
                            </KuzhambuButton>
                            <KuzhambuButton name="重置" onClick={resetCalls}>
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
                    onChange={handleTableChange}
                />
            </Card>

            <KuzhambuDrawer
                open={Boolean(selectedCall)}
                title="调用详情"
                size="large"
                onClose={() => setSelectedCall(null)}
            >
                {selectedCall ? <CallDetail call={selectedCall} /> : null}
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};

const CallDetail = ({ call }: { call: AiCallRecord }) => (
    <Descriptions column={1} size="small">
        <Descriptions.Item label="requestId">{call.requestId || "-"}</Descriptions.Item>
        <Descriptions.Item label="traceId">{call.traceId || "-"}</Descriptions.Item>
        <Descriptions.Item label="promptVersionId">{call.promptVersionId || "-"}</Descriptions.Item>
        <Descriptions.Item label="streamUsed">{call.streamUsed ? "是" : "否"}</Descriptions.Item>
        <Descriptions.Item label="streamCompleted">
            {call.streamCompleted ? "是" : "否"}
        </Descriptions.Item>
        <Descriptions.Item label="inputTokens">{call.inputTokens ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="outputTokens">{call.outputTokens ?? "-"}</Descriptions.Item>
        <Descriptions.Item label="failureStage">{call.failureStage || "-"}</Descriptions.Item>
        <Descriptions.Item label="resultFormat">{call.resultFormat || "-"}</Descriptions.Item>
        <Descriptions.Item label="errorType">{call.errorType || "-"}</Descriptions.Item>
        <Descriptions.Item label="errorMessage">{call.errorMessage || "-"}</Descriptions.Item>
        <Descriptions.Item label="warningsJson">
            <pre className="invocations-warnings">{formatWarnings(call.warningsJson)}</pre>
        </Descriptions.Item>
    </Descriptions>
);
