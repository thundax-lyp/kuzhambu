import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { QualityReportRecord } from "../quality-report-types";

interface QualityReportHistoryTableProps {
    loading?: boolean;
    reports: QualityReportRecord[];
    onView: (report: QualityReportRecord) => void;
}

const readStatusColor = (status?: string | null) => (status === "PUBLISHED" ? "green" : "default");

const formatDate = (value?: number | string | null) => {
    if (!value) {
        return "-";
    }
    return new Date(value).toLocaleString();
};

export const QualityReportHistoryTable = ({
    loading = false,
    reports,
    onView
}: QualityReportHistoryTableProps) => {
    const columns: ColumnsType<QualityReportRecord> = [
        { title: "报告编号", dataIndex: "reportNo", key: "reportNo" },
        { title: "图谱版本", dataIndex: "graphVersionId", key: "graphVersionId" },
        { title: "来源门类", dataIndex: "sourceCategoryName", key: "sourceCategoryName" },
        {
            title: "状态",
            dataIndex: "reportStatus",
            key: "reportStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        { title: "问题数", dataIndex: "issueCount", key: "issueCount" },
        {
            title: "生成时间",
            dataIndex: "generatedAt",
            key: "generatedAt",
            render: formatDate
        },
        {
            title: "操作",
            key: "actions",
            render: (_, report) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-quality-report-quality-report-history-view-button"
                        onClick={() => onView(report)}
                    >
                        查看
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <Table<QualityReportRecord>
            aria-label="知识质量报告历史表格"
            columns={columns}
            dataSource={reports}
            loading={loading}
            pagination={false}
            rowKey={(report) => normalizeId(report.reportId)}
        />
    );
};
