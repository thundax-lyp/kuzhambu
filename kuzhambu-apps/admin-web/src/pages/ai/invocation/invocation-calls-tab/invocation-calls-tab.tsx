import { DatePicker, Table, Tag } from "antd";
import { Form } from "antd";
import type { ColumnsType, TablePaginationConfig } from "antd/es/table";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import { useState } from "react";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "@/types/page";
import type { Page } from "@/types/page";
import type { AiInvocationLogPageQuery } from "@/pages/ai/invocation/invocation-service";
import type { AiInvocationLogRecord } from "@/pages/ai/invocation/invocation-types";

import "./invocation-calls-tab.css";

const { RangePicker } = DatePicker;

const DATE_TIME_FORMAT = "YYYYMMDD HH:mm";

type InvocationDateRangeValue = [Dayjs | null, Dayjs | null] | null;

type InvocationLogFilterValues = AiInvocationLogPageQuery & {
    requestedAt?: InvocationDateRangeValue;
};

const STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "待处理", value: "PENDING" },
    { label: "运行中", value: "RUNNING" }
];

const readCallId = (call: AiInvocationLogRecord) => call.callIdText || String(call.callId);

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

const buildInvocationLogQuery = (
    values: InvocationLogFilterValues,
    pageNo: number,
    pageSize: number
): AiInvocationLogPageQuery => {
    const range = {
        start: values.requestedAt?.[0]?.toISOString() || null,
        end: values.requestedAt?.[1]?.toISOString() || null
    };
    return {
        status: values.status || null,
        requestedAtStart: range.start,
        requestedAtEnd: range.end,
        pageNo,
        pageSize
    };
};

interface InvocationCallsTabProps {
    formatCapability: (capability?: string | null) => string;
    invocationLogPage?: Page<AiInvocationLogRecord>;
    loading: boolean;
    onOpenDetail: (record: AiInvocationLogRecord) => void;
    onSearchCalls: (query: AiInvocationLogPageQuery) => void;
}

export const InvocationCallsTab = ({
    formatCapability,
    invocationLogPage,
    loading,
    onOpenDetail,
    onSearchCalls
}: InvocationCallsTabProps) => {
    const [callsForm] = Form.useForm<InvocationLogFilterValues>();
    const [currentPageNo, setCurrentPageNo] = useState(DEFAULT_PAGE_NO);
    const [currentPageSize, setCurrentPageSize] = useState(DEFAULT_PAGE_SIZE);

    const invocationLogColumns: ColumnsType<AiInvocationLogRecord> = [
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
            className: "invocation-nowrap-column",
            render: formatDateTime
        }
    ];

    const searchCalls = async (pageNo = DEFAULT_PAGE_NO, pageSize = currentPageSize) => {
        const values = await callsForm.validateFields();
        setCurrentPageNo(pageNo);
        setCurrentPageSize(pageSize);
        onSearchCalls(buildInvocationLogQuery(values, pageNo, pageSize));
    };

    const resetCalls = () => {
        callsForm.resetFields();
        setCurrentPageNo(DEFAULT_PAGE_NO);
        setCurrentPageSize(DEFAULT_PAGE_SIZE);
        onSearchCalls({ pageNo: DEFAULT_PAGE_NO, pageSize: DEFAULT_PAGE_SIZE });
    };

    const handleTableChange = (pagination: TablePaginationConfig) => {
        const nextPageNo = pagination.current || DEFAULT_PAGE_NO;
        const nextPageSize = pagination.pageSize || DEFAULT_PAGE_SIZE;
        void searchCalls(nextPageNo, nextPageSize);
    };

    return (
        <KuzhambuCard className="invocation-section-card">
            <KuzhambuForm className="invocation-calls-filter-form" form={callsForm}>
                <KuzhambuFormItem label="状态" name="status" layoutSize="small">
                    <KuzhambuSelect
                        allowClear
                        className="invocation-calls-filter-control"
                        options={STATUS_OPTIONS}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="请求时间" name="requestedAt" layoutSize="middle">
                    <RangePicker
                        aria-label="请求时间"
                        format={DATE_TIME_FORMAT}
                        showTime
                        style={{ width: "100%" }}
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
            <KuzhambuSpace>
                <KuzhambuButton
                    testId="ai-invocation-invocation-query-button"
                    type="primary"
                    onClick={() => void searchCalls()}
                >
                    查询
                </KuzhambuButton>
                <KuzhambuButton testId="ai-invocation-invocation-reset-button" onClick={resetCalls}>
                    重置
                </KuzhambuButton>
            </KuzhambuSpace>

            <Table<AiInvocationLogRecord>
                aria-label="AI 调用记录"
                rowKey={readCallId}
                className="invocation-table"
                columns={invocationLogColumns}
                dataSource={invocationLogPage?.records || []}
                loading={loading}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    pageSizeOptions: PAGE_SIZE_OPTIONS,
                    showSizeChanger: true,
                    total: invocationLogPage?.totalCount ?? invocationLogPage?.count ?? 0
                }}
                onRow={(record) => ({
                    onClick: () => onOpenDetail(record)
                })}
                onChange={handleTableChange}
            />
        </KuzhambuCard>
    );
};
