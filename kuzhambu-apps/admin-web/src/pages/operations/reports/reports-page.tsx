import {
    DownloadOutlined,
    FileDoneOutlined,
    FileTextOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card, DatePicker, Descriptions, InputNumber, Select, Spin, Typography } from "antd";
import type { Dayjs } from "dayjs";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type { Page } from "@/types/page";
import * as service from "./reports-service";
import type { OperationsReportGenerateCommand, OperationsReportPageQuery } from "./reports-service";
import type {
    OperationsReportFormat,
    OperationsReportRecord,
    OperationsReportStatus,
    OperationsReportType
} from "./reports-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./reports-page.css";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";

const { RangePicker } = DatePicker;
const { Text } = Typography;

const reportTypeOptions = [
    { label: "全部", value: "ALL" },
    { label: "周报", value: "WEEKLY" },
    { label: "月报", value: "MONTHLY" }
];

const formatOptions = [
    { label: "全部", value: "ALL" },
    { label: "HTML", value: "HTML" },
    { label: "PDF", value: "PDF" }
];

const statusOptions = [
    { label: "全部", value: "ALL" },
    { label: "等待中", value: "PENDING" },
    { label: "生成中", value: "RUNNING" },
    { label: "已完成", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" }
];

const defaultGenerateForm: OperationsReportGenerateCommand = {
    reportType: "WEEKLY",
    format: "PDF",
    periodStart: "",
    periodEnd: ""
};

const normalizeOption = (value?: string | null) => {
    return value === "ALL" ? undefined : value || undefined;
};

const toIsoRange = (dates: [Dayjs | null, Dayjs | null] | null) => {
    return {
        periodStart: dates?.[0]?.toISOString(),
        periodEnd: dates?.[1]?.toISOString()
    };
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

const statusTone = (status?: string | null) => {
    if (status === "SUCCEEDED") {
        return "success";
    }
    if (status === "FAILED") {
        return "danger";
    }
    if (status === "RUNNING" || status === "PENDING") {
        return "warning";
    }
    return "neutral";
};

const reportTypeText = (value?: string | null) => {
    if (value === "WEEKLY") {
        return "周报";
    }
    if (value === "MONTHLY") {
        return "月报";
    }
    return value || "-";
};

const failureReasonText = (value?: string | null) => {
    return value || "未返回失败原因";
};

const canDownloadReport = (record?: OperationsReportRecord | null) => {
    return Boolean(
        record?.reportId && record.reportStatus === "SUCCEEDED" && record.storageObjectId
    );
};

const hasRunningRecord = (page?: Page<OperationsReportRecord>) => {
    return Boolean(
        page?.records?.some((record) => ["PENDING", "RUNNING"].includes(record.reportStatus || ""))
    );
};

const buildPageQuery = (filter: OperationsReportPageQuery, pageNo: number, pageSize: number) => ({
    reportType: normalizeOption(filter.reportType),
    format: normalizeOption(filter.format),
    reportStatus: normalizeOption(filter.reportStatus),
    requesterUserId: filter.requesterUserId ?? undefined,
    periodStart: filter.periodStart || undefined,
    periodEnd: filter.periodEnd || undefined,
    pageNo,
    pageSize
});

export const OperationsReportsPage = () => {
    const queryClient = useQueryClient();
    const canViewReport = hasPermission("operations:report:view");
    const canGenerateReport = hasPermission("operations:report:generate");
    const [filter, setFilter] = useState<OperationsReportPageQuery>({});
    const [pageNo, setPageNo] = useState(DEFAULT_PAGE_NO);
    const [generateForm, setGenerateForm] =
        useState<OperationsReportGenerateCommand>(defaultGenerateForm);
    const [detailReportId, setDetailReportId] = useState<number | null>(null);

    const reportPageQuery = useQuery({
        queryKey: ["operations", "report", "page", filter, pageNo],
        queryFn: () => service.pageReports(buildPageQuery(filter, pageNo, DEFAULT_PAGE_SIZE)),
        enabled: canViewReport,
        refetchInterval: (query) => {
            const page = query.state.data as Page<OperationsReportRecord> | undefined;
            return hasRunningRecord(page) ? 5000 : false;
        },
        retry: false
    });

    const reportDetailQuery = useQuery({
        queryKey: ["operations", "report", "detail", detailReportId],
        queryFn: () => service.getReportDetail({ reportId: detailReportId as number }),
        enabled: detailReportId !== null,
        retry: false
    });

    const generateMutation = useMutation({
        mutationFn: (command: OperationsReportGenerateCommand) => service.generateReport(command),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["operations", "report", "page"] });
        }
    });

    const reportPage = reportPageQuery.data;
    const records = reportPage?.records || [];
    const totalCount = reportPage?.totalCount ?? reportPage?.count ?? 0;
    const totalPage = reportPage?.totalPage || 1;
    const detailRecord = reportDetailQuery.data;

    const updateFilter = (patch: Partial<OperationsReportPageQuery>) => {
        setFilter((currentFilter) => ({
            ...currentFilter,
            ...patch
        }));
        setPageNo(DEFAULT_PAGE_NO);
    };

    const submitGenerate = () => {
        if (!generateForm.periodStart || !generateForm.periodEnd) {
            return;
        }
        generateMutation.mutate(generateForm);
    };

    const closeDetail = () => {
        setDetailReportId(null);
    };

    return (
        <KuzhambuPage
            className="reports-page operations-reports-page"
            title="报表管理"
            description="生成周报、月报，查看 HTML/PDF 产物状态，并定位失败原因。"
            actions={
                <KuzhambuButton
                    testId="operations-reports-reports-action-button"
                    type="primary"
                    icon={<FileDoneOutlined />}
                    disabled={!canGenerateReport}
                    onClick={submitGenerate}
                >
                    生成报表
                </KuzhambuButton>
            }
        >
            {!canViewReport ? (
                <KuzhambuAlert type="warning" showIcon title="缺少 operations:report:view 权限" />
            ) : null}

            <section className="operations-reports-section">
                <Card title="筛选记录" size="small">
                    <KuzhambuSpace className="operations-reports-controls" size={12} wrap>
                        <label className="operations-reports-field">
                            <Text type="secondary">报表类型</Text>
                            <Select
                                aria-label="报表类型"
                                options={reportTypeOptions}
                                value={filter.reportType || "ALL"}
                                onChange={(value) => updateFilter({ reportType: value })}
                            />
                        </label>
                        <label className="operations-reports-field">
                            <Text type="secondary">导出格式</Text>
                            <Select
                                aria-label="导出格式"
                                options={formatOptions}
                                value={filter.format || "ALL"}
                                onChange={(value) => updateFilter({ format: value })}
                            />
                        </label>
                        <label className="operations-reports-field">
                            <Text type="secondary">状态</Text>
                            <Select
                                aria-label="状态"
                                options={statusOptions}
                                value={filter.reportStatus || "ALL"}
                                onChange={(value) => updateFilter({ reportStatus: value })}
                            />
                        </label>
                        <label className="operations-reports-field operations-reports-field-compact">
                            <Text type="secondary">请求人用户 ID</Text>
                            <InputNumber
                                aria-label="请求人用户 ID"
                                min={1}
                                value={filter.requesterUserId}
                                onChange={(value) =>
                                    updateFilter({ requesterUserId: value ?? undefined })
                                }
                            />
                        </label>
                        <label className="operations-reports-field operations-reports-field-range">
                            <Text type="secondary">统计周期</Text>
                            <RangePicker
                                aria-label="统计周期"
                                showTime
                                onChange={(dates) => updateFilter(toIsoRange(dates))}
                            />
                        </label>
                        <KuzhambuButton
                            testId="operations-reports-reports-query-button"
                            icon={<ReloadOutlined />}
                            onClick={() => reportPageQuery.refetch()}
                        >
                            查询
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </Card>
            </section>

            <section className="operations-reports-section">
                <Card title="生成报表" size="small">
                    <KuzhambuSpace className="operations-reports-controls" size={12} wrap>
                        <label className="operations-reports-field">
                            <Text type="secondary">生成类型</Text>
                            <Select
                                aria-label="生成类型"
                                options={reportTypeOptions.filter(
                                    (option) => option.value !== "ALL"
                                )}
                                value={generateForm.reportType}
                                onChange={(value: OperationsReportType) =>
                                    setGenerateForm((currentForm) => ({
                                        ...currentForm,
                                        reportType: value
                                    }))
                                }
                            />
                        </label>
                        <label className="operations-reports-field">
                            <Text type="secondary">生成格式</Text>
                            <Select
                                aria-label="生成格式"
                                options={formatOptions.filter((option) => option.value !== "ALL")}
                                value={generateForm.format}
                                onChange={(value: OperationsReportFormat) =>
                                    setGenerateForm((currentForm) => ({
                                        ...currentForm,
                                        format: value
                                    }))
                                }
                            />
                        </label>
                        <label className="operations-reports-field operations-reports-field-range">
                            <Text type="secondary">生成周期</Text>
                            <RangePicker
                                aria-label="生成周期"
                                showTime
                                onChange={(dates) => {
                                    const range = toIsoRange(dates);
                                    setGenerateForm((currentForm) => ({
                                        ...currentForm,
                                        periodStart: range.periodStart || "",
                                        periodEnd: range.periodEnd || ""
                                    }));
                                }}
                            />
                        </label>
                        <KuzhambuButton
                            testId="operations-reports-reports-action-button-2"
                            type="primary"
                            icon={<FileTextOutlined />}
                            disabled={
                                !canGenerateReport ||
                                !generateForm.periodStart ||
                                !generateForm.periodEnd
                            }
                            loading={generateMutation.isPending}
                            onClick={submitGenerate}
                        >
                            提交生成
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </Card>
            </section>

            <section className="operations-reports-section">
                <Card title="记录列表" size="small">
                    <div className="operations-reports-list-meta">
                        <Text type="secondary">共 {totalCount} 条记录</Text>
                        {reportPageQuery.isFetching ? <Spin size="small" /> : null}
                    </div>
                    <table className="operations-reports-table">
                        <thead>
                            <tr>
                                <th>报表 ID</th>
                                <th>类型</th>
                                <th>格式</th>
                                <th>统计周期</th>
                                <th>状态</th>
                                <th>请求人</th>
                                <th>请求时间</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            {records.length ? (
                                records.map((record) => (
                                    <tr key={record.reportId}>
                                        <td>{record.reportId}</td>
                                        <td>{reportTypeText(record.reportType)}</td>
                                        <td>{record.format || "-"}</td>
                                        <td>
                                            {formatDateTime(record.periodStart)} 至{" "}
                                            {formatDateTime(record.periodEnd)}
                                        </td>
                                        <td>
                                            <KuzhambuSpace size={6} wrap>
                                                <KuzhambuTag type={statusTone(record.reportStatus)}>
                                                    {record.reportStatus || "-"}
                                                </KuzhambuTag>
                                                {record.reportStatus === "FAILED" ? (
                                                    <Text type="danger">
                                                        {failureReasonText(record.failureReason)}
                                                    </Text>
                                                ) : null}
                                            </KuzhambuSpace>
                                        </td>
                                        <td>{record.requesterUserId ?? "-"}</td>
                                        <td>{formatDateTime(record.requestedAt)}</td>
                                        <td>
                                            <KuzhambuSpace size={8} wrap>
                                                <KuzhambuButton
                                                    testId="operations-reports-reports-detail-button"
                                                    size="small"
                                                    onClick={() =>
                                                        setDetailReportId(record.reportId)
                                                    }
                                                >
                                                    详情
                                                </KuzhambuButton>
                                                {canDownloadReport(record) ? (
                                                    <KuzhambuButton
                                                        testId="operations-reports-reports-download-button"
                                                        size="small"
                                                        icon={<DownloadOutlined />}
                                                        href={service.toReportDownloadUrl(
                                                            record.reportId
                                                        )}
                                                    >
                                                        下载
                                                    </KuzhambuButton>
                                                ) : null}
                                            </KuzhambuSpace>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={8}>暂无报表记录</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                    <KuzhambuSpace className="operations-reports-pagination" size={8}>
                        <KuzhambuButton
                            testId="operations-reports-reports-previous-page-button"
                            disabled={pageNo <= DEFAULT_PAGE_NO}
                            onClick={() => setPageNo(pageNo - 1)}
                        >
                            上一页
                        </KuzhambuButton>
                        <Text>
                            第 {pageNo} / {totalPage} 页
                        </Text>
                        <KuzhambuButton
                            testId="operations-reports-reports-next-page-button"
                            disabled={pageNo >= totalPage}
                            onClick={() => setPageNo(pageNo + 1)}
                        >
                            下一页
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </Card>
            </section>

            <KuzhambuDrawer
                testId="operations-reports-reports-drawer"
                title="报表详情"
                open={detailReportId !== null}
                onClose={closeDetail}
                size="middle"
                destroyOnClose
            >
                {reportDetailQuery.isLoading ? <Spin /> : null}
                {detailRecord ? (
                    <KuzhambuSpace
                        orientation="vertical"
                        size={16}
                        className="operations-reports-detail"
                    >
                        {detailRecord.reportStatus === "FAILED" ? (
                            <KuzhambuAlert
                                type="error"
                                showIcon
                                title="报表生成失败"
                                description={failureReasonText(detailRecord.failureReason)}
                            />
                        ) : null}
                        <Descriptions column={1} bordered size="small">
                            <Descriptions.Item label="报表 ID">
                                {detailRecord.reportId}
                            </Descriptions.Item>
                            <Descriptions.Item label="报表类型">
                                {reportTypeText(detailRecord.reportType)}
                            </Descriptions.Item>
                            <Descriptions.Item label="导出格式">
                                {detailRecord.format || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="状态">
                                <KuzhambuTag
                                    type={statusTone(
                                        detailRecord.reportStatus as OperationsReportStatus
                                    )}
                                >
                                    {detailRecord.reportStatus || "-"}
                                </KuzhambuTag>
                            </Descriptions.Item>
                            <Descriptions.Item label="统计周期">
                                {formatDateTime(detailRecord.periodStart)} 至{" "}
                                {formatDateTime(detailRecord.periodEnd)}
                            </Descriptions.Item>
                            <Descriptions.Item label="请求 ID">
                                {detailRecord.requestId || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="Trace ID">
                                {detailRecord.traceId || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="模板版本">
                                {detailRecord.templateVersion || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="产物文件">
                                {detailRecord.artifactFilename || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="请求人">
                                {detailRecord.requesterUserId ?? "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="完成时间">
                                {formatDateTime(detailRecord.completedAt)}
                            </Descriptions.Item>
                        </Descriptions>
                        {canDownloadReport(detailRecord) ? (
                            <KuzhambuButton
                                testId="operations-reports-reports-action-button-3"
                                type="primary"
                                icon={<DownloadOutlined />}
                                href={service.toReportDownloadUrl(detailRecord.reportId)}
                            >
                                下载报表
                            </KuzhambuButton>
                        ) : null}
                    </KuzhambuSpace>
                ) : null}
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
