import { Card, DatePicker, Input, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import type { Dayjs } from "dayjs";
import { KuzhambuButton, KuzhambuSpace } from "@/components";

import type {
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSessionPageRecord
} from "../qa-console-types";

const { Text } = Typography;
const { RangePicker } = DatePicker;

const formatSessionStatus = (value?: string | null) => {
    if (value === "OPEN") {
        return "打开";
    }
    if (value === "REMOVED") {
        return "已删除";
    }
    return value ?? "-";
};

const formatDate = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const date = typeof value === "number" ? new Date(value) : new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

interface QaSessionTableProps {
    deleteLoading: boolean;
    exportLoading: boolean;
    loading: boolean;
    onDelete: (sessionId: string) => void;
    onExport: (sessionId: string) => void;
    onLoad: () => void;
    onOpen: (sessionId: string) => void;
    onPageChange: (pageNo: number) => void;
    onRangeChange: (range: [Dayjs | null, Dayjs | null] | null) => void;
    onTitleChange: (title: string) => void;
    operationText: string | null;
    pageData?: DiscoveryQaSessionPageRecord;
    pageNo: number;
    pageSize: number;
    range: [Dayjs | null, Dayjs | null] | null;
    rows: DiscoveryQaSessionDetailRecord[];
    sessionLoading: boolean;
    title: string;
}

export const QaSessionTable = ({
    deleteLoading,
    exportLoading,
    loading,
    onDelete,
    onExport,
    onLoad,
    onOpen,
    onPageChange,
    onRangeChange,
    onTitleChange,
    operationText,
    pageData,
    pageNo,
    pageSize,
    range,
    rows,
    sessionLoading,
    title
}: QaSessionTableProps) => {
    const columns: ColumnsType<DiscoveryQaSessionDetailRecord> = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: 220,
            render: (value?: string | null) => value ?? "-"
        },
        {
            title: "拥有者",
            dataIndex: "ownerUserId",
            key: "ownerUserId",
            width: 120,
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "创建时间",
            dataIndex: "openedAt",
            key: "openedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            width: 96,
            render: (value?: string | null) => (
                <Tag color={value === "REMOVED" ? "default" : "processing"}>
                    {formatSessionStatus(value)}
                </Tag>
            )
        },
        {
            fixed: "right",
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpace size={8}>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-view-session-button"
                        loading={sessionLoading}
                        onClick={(event) => {
                            event.stopPropagation();
                            onOpen(String(record.sessionId ?? ""));
                        }}
                        size="small"
                    >
                        查看
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-export-session-button"
                        loading={exportLoading}
                        onClick={(event) => {
                            event.stopPropagation();
                            onExport(String(record.sessionId ?? ""));
                        }}
                        size="small"
                    >
                        导出
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-delete-session-button"
                        danger
                        disabled={record.status === "REMOVED"}
                        loading={deleteLoading}
                        onClick={(event) => {
                            event.stopPropagation();
                            onDelete(String(record.sessionId ?? ""));
                        }}
                        size="small"
                    >
                        删除
                    </KuzhambuButton>
                </KuzhambuSpace>
            )
        }
    ];

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <Card title="会话管理" size="small">
                <KuzhambuSpace align="end" wrap>
                    <label className="qa-console-form-item">
                        <Text type="secondary">标题</Text>
                        <Input
                            allowClear
                            aria-label="标题"
                            value={title}
                            onChange={(event) => onTitleChange(event.target.value)}
                            style={{ width: 220 }}
                        />
                    </label>
                    <label className="qa-console-form-item">
                        <Text type="secondary">创建时间</Text>
                        <RangePicker
                            aria-label="创建时间"
                            value={range}
                            onChange={(value) => onRangeChange(value)}
                        />
                    </label>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-load-session-button"
                        loading={loading}
                        onClick={onLoad}
                        type="primary"
                    >
                        查询
                    </KuzhambuButton>
                </KuzhambuSpace>
            </Card>

            <Card title="会话记录" size="small">
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    {operationText ? <Text type="secondary">{operationText}</Text> : null}
                    <Table
                        aria-label="问答会话表格"
                        columns={columns}
                        dataSource={rows}
                        pagination={{
                            current: pageData?.pageNo ?? pageNo,
                            onChange: onPageChange,
                            pageSize,
                            showTotal: (total) => `共 ${total} 条`,
                            showSizeChanger: false,
                            total: pageData?.totalCount ?? pageData?.count ?? 0
                        }}
                        rowKey={(record) => record.sessionId ?? "-"}
                        scroll={{ x: 780 }}
                        size="small"
                    />
                </KuzhambuSpace>
            </Card>
        </KuzhambuSpace>
    );
};
