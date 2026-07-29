import { Table, Tag } from "antd";
import type { ColumnsType, TablePaginationConfig } from "antd/es/table";
import dayjs from "dayjs";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "@/types/page";
import type { Page } from "@/types/page";
import type { AiInvocationLogRecord } from "./invocations-types";

const DATE_TIME_FORMAT = "YYYYMMDD HH:mm";

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

interface InvocationTableProps {
    invocationLogPage?: Page<AiInvocationLogRecord>;
    currentPageNo?: number;
    currentPageSize?: number;
    formatCapability: (capability?: string | null) => string;
    loading: boolean;
    onChange: (pagination: TablePaginationConfig) => void;
    onOpenDetail: (call: AiInvocationLogRecord) => void;
}

export const InvocationTable = ({
    invocationLogPage,
    currentPageNo = DEFAULT_PAGE_NO,
    currentPageSize = DEFAULT_PAGE_SIZE,
    formatCapability,
    loading,
    onChange,
    onOpenDetail
}: InvocationTableProps) => {
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
            className: "invocations-nowrap-column",
            render: formatDateTime
        }
    ];

    return (
        <Table<AiInvocationLogRecord>
            aria-label="AI 调用记录"
            rowKey={readCallId}
            className="invocations-table"
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
            onChange={onChange}
        />
    );
};
