import {
    DownloadOutlined,
    EyeOutlined,
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { Alert, Button, DatePicker, Empty, Input, Select, Tag, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import type { Page } from "@/types/page";
import "./classics-showcase-job-section.css";

const { Text } = Typography;
const { Search } = Input;
const { RangePicker } = DatePicker;

export type SancaiShowcaseRequestedAtRange = [Dayjs | null, Dayjs | null] | null;
export type ShowcaseJobStatus = "REQUESTED" | "PROCESSING" | "COMPLETED" | "FAILED" | "EXPIRED";

export interface ShowcaseJobItem {
    assetCount?: number | null;
    completedAt?: string | null;
    contentType?: string | null;
    contentUrl?: string | null;
    downloadUrl?: string | null;
    entryCount?: number | null;
    failureMessage?: string | null;
    failureType?: string | null;
    filename?: string | null;
    id?: number | null;
    requestedAt?: string | null;
    scopeTitle?: string | null;
    sha256?: string | null;
    sizeBytes?: number | null;
    status?: ShowcaseJobStatus | null;
    storageObjectId?: number | null;
    visibilityRiskStatus?: string | null;
}

const statusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "排队中", value: "REQUESTED" },
    { label: "进行中", value: "PROCESSING" },
    { label: "已完成", value: "COMPLETED" },
    { label: "失败", value: "FAILED" },
    { label: "已过期", value: "EXPIRED" }
];

const riskOptions = [
    { label: "全部风险", value: "ALL" },
    { label: "仅公开内容", value: "PUBLIC_ONLY" },
    { label: "包含私有内容", value: "CONTAINS_PRIVATE" }
];

const noop = () => undefined;

const statusTagType = (status?: string | null) => {
    switch (status) {
        case "COMPLETED":
            return "success";
        case "REQUESTED":
        case "PROCESSING":
            return "processing";
        case "FAILED":
            return "error";
        case "EXPIRED":
            return "warning";
        default:
            return "default";
    }
};

const renderStatusText = (status?: string | null) => {
    const normalized = status || "UNKNOWN";
    return (
        {
            COMPLETED: "已完成",
            REQUESTED: "排队中",
            PROCESSING: "进行中",
            FAILED: "失败",
            EXPIRED: "已过期"
        }[normalized] || normalized
    );
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, "0")}/${String(
        date.getDate()
    ).padStart(2, "0")} ${String(date.getHours()).padStart(2, "0")}:${String(
        date.getMinutes()
    ).padStart(2, "0")}`;
};

const formatSize = (value?: number | null) => {
    if (!value) {
        return "—";
    }
    if (value < 1024) {
        return `${value} B`;
    }
    if (value < 1024 * 1024) {
        return `${(value / 1024).toFixed(1)} KB`;
    }
    return `${(value / 1024 / 1024).toFixed(1)} MB`;
};

const isCompleted = (job: ShowcaseJobItem) => {
    return job.status === "COMPLETED" && Boolean(job.contentUrl || job.downloadUrl);
};

export interface ClassicsShowcaseJobSectionProps {
    creating?: boolean;
    error?: boolean;
    filtersVisible?: boolean;
    keyword?: string;
    loading?: boolean;
    onCreate?: () => void;
    onDownload: (job: ShowcaseJobItem) => void;
    onFilter?: () => void;
    onKeywordChange?: (value: string) => void;
    onPageChange?: (pageNo: number, pageSize: number) => void;
    onPreview: (job: ShowcaseJobItem) => void;
    onRefresh: () => void;
    onRequestedAtRangeChange?: (value: SancaiShowcaseRequestedAtRange) => void;
    onReset?: () => void;
    onStatusChange?: (value: "ALL" | ShowcaseJobStatus) => void;
    onVisibilityRiskStatusChange?: (value: "ALL" | string) => void;
    page?: Page<ShowcaseJobItem>;
    requestedAtRange?: SancaiShowcaseRequestedAtRange;
    status?: "ALL" | ShowcaseJobStatus;
    visibilityRiskStatus?: "ALL" | string;
}

export const ClassicsShowcaseJobSection = ({
    creating = false,
    error = false,
    filtersVisible = true,
    keyword = "",
    loading = false,
    onCreate,
    onDownload,
    onFilter = noop,
    onKeywordChange = noop,
    onPageChange = noop,
    onPreview,
    onRefresh,
    onRequestedAtRangeChange = noop,
    onReset = noop,
    onStatusChange = noop,
    onVisibilityRiskStatusChange = noop,
    page,
    requestedAtRange = null,
    status = "ALL",
    visibilityRiskStatus = "ALL"
}: ClassicsShowcaseJobSectionProps) => {
    const records = page?.records ?? [];
    const columns: KuzhambuTableProps<ShowcaseJobItem>["columns"] = [
        {
            title: "任务 ID",
            dataIndex: "id",
            key: "id",
            width: 96,
            render: (value) => `#${value ?? "—"}`
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            width: 104,
            render: (value?: string | null) => (
                <Tag color={statusTagType(value)}>{renderStatusText(value)}</Tag>
            )
        },
        {
            title: "范围",
            dataIndex: "scopeTitle",
            key: "scopeTitle",
            width: 220,
            render: (value?: string | null) => value || "未命名范围"
        },
        {
            title: "条目数",
            dataIndex: "entryCount",
            key: "entryCount",
            width: 92,
            render: (value) => value ?? 0
        },
        {
            title: "资产数",
            dataIndex: "assetCount",
            key: "assetCount",
            width: 92,
            render: (value) => value ?? 0
        },
        {
            title: "风险",
            dataIndex: "visibilityRiskStatus",
            key: "visibilityRiskStatus",
            width: 130,
            render: (value) => (value === "CONTAINS_PRIVATE" ? "包含私有内容" : "仅公开内容")
        },
        {
            title: "生成时间",
            dataIndex: "requestedAt",
            key: "requestedAt",
            width: 156,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            title: "完成时间",
            dataIndex: "completedAt",
            key: "completedAt",
            width: 156,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            title: "失败原因",
            dataIndex: "failureMessage",
            key: "failureMessage",
            width: 220,
            render: (value: string | null | undefined, record) => value || record.failureType || "—"
        },
        {
            title: "文件",
            dataIndex: "filename",
            key: "filename",
            width: 180,
            render: (value: string | null | undefined, record) => {
                if (!value) {
                    return "—";
                }
                return `${value} · ${formatSize(record.sizeBytes)}`;
            }
        },
        {
            key: "actions",
            title: "操作",
            fixed: "right",
            width: 150,
            render: (_: unknown, record) => {
                const completed = isCompleted(record);
                return (
                    <KuzhambuSpace size={8} wrap={false}>
                        <Button
                            disabled={!completed || !record.contentUrl}
                            icon={<EyeOutlined />}
                            size="small"
                            onClick={() => onPreview(record)}
                        >
                            预览
                        </Button>
                        <Button
                            disabled={!completed || !record.downloadUrl}
                            icon={<DownloadOutlined />}
                            size="small"
                            onClick={() => onDownload(record)}
                        >
                            下载
                        </Button>
                    </KuzhambuSpace>
                );
            }
        }
    ];

    return (
        <section className="classics-showcase-job-section" aria-label="静态展示任务">
            <div className="classics-showcase-job-section-head">
                <Text strong>静态展示任务</Text>
                <KuzhambuSpace size={8} wrap>
                    {onCreate ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            loading={creating}
                            onClick={onCreate}
                        >
                            生成静态展示
                        </Button>
                    ) : null}
                    <Button icon={<ReloadOutlined />} aria-label="刷新" onClick={onRefresh} />
                </KuzhambuSpace>
            </div>
            {filtersVisible ? (
                <div className="classics-showcase-job-section-filters">
                    <Search
                        allowClear
                        aria-label="搜索静态展示任务"
                        placeholder="搜索任务 ID、范围或文件名"
                        prefix={<SearchOutlined />}
                        value={keyword}
                        onChange={(event) => onKeywordChange(event.target.value)}
                        onSearch={onFilter}
                    />
                    <Select
                        aria-label="静态展示任务状态"
                        value={status}
                        options={statusOptions}
                        onChange={onStatusChange}
                    />
                    <Select
                        aria-label="静态展示可见性风险"
                        value={visibilityRiskStatus}
                        options={riskOptions}
                        onChange={onVisibilityRiskStatusChange}
                    />
                    <RangePicker
                        aria-label="静态展示生成时间"
                        showTime
                        value={requestedAtRange}
                        onChange={(value) => onRequestedAtRangeChange(value)}
                    />
                    <KuzhambuSpace size={8} wrap={false}>
                        <Button onClick={onFilter}>筛选</Button>
                        <Button onClick={onReset}>重置</Button>
                    </KuzhambuSpace>
                </div>
            ) : null}
            {error ? (
                <Alert
                    className="classics-showcase-job-section-alert"
                    type="warning"
                    showIcon
                    message="静态展示任务加载失败"
                />
            ) : null}
            <KuzhambuTable<ShowcaseJobItem>
                ariaLabel="静态展示任务列表"
                className="classics-showcase-job-table"
                columns={columns}
                dataSource={records}
                loading={loading}
                locale={{ emptyText: <Empty description="暂无静态展示任务" /> }}
                pagination={{
                    current: page?.pageNo ?? 1,
                    pageSize: page?.pageSize ?? 20,
                    total: page?.totalCount ?? page?.count ?? 0,
                    onChange: onPageChange
                }}
                rowKey={(record) => String(record.id ?? record.requestedAt ?? "showcase-job")}
                scroll={{ x: 1560 }}
            />
        </section>
    );
};
