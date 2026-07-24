import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import type { ColumnsType } from "antd/es/table";
import type { QualityReportIssueRecord } from "../quality-report-types";

interface QualityReportIssueTableProps {
    issues: QualityReportIssueRecord[];
}

const readSeverityColor = (severity?: string | null) => {
    if (severity === "high") {
        return "red";
    }
    if (severity === "medium") {
        return "orange";
    }
    return "blue";
};

export const QualityReportIssueTable = ({ issues }: QualityReportIssueTableProps) => {
    const columns: ColumnsType<QualityReportIssueRecord> = [
        {
            title: "级别",
            dataIndex: "severity",
            key: "severity",
            render: (severity?: string | null) => (
                <Tag color={readSeverityColor(severity)}>{severity || "-"}</Tag>
            )
        },
        { title: "类型", dataIndex: "issueType", key: "issueType" },
        {
            title: "对象",
            key: "object",
            render: (_, issue) => `${issue.objectType || "-"} / ${issue.objectKey || "-"}`
        },
        { title: "标题", dataIndex: "title", key: "title" },
        { title: "说明", dataIndex: "description", key: "description", ellipsis: true },
        { title: "建议", dataIndex: "suggestion", key: "suggestion", ellipsis: true },
        {
            title: "操作",
            key: "actions",
            render: (_, issue) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-quality-report-quality-report-issue-action-button"
                        disabled={!issue.href}
                        href={issue.href || undefined}
                    >
                        打开
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <Table<QualityReportIssueRecord>
            aria-label="知识质量报告问题清单表格"
            columns={columns}
            dataSource={issues}
            pagination={false}
            rowKey={(issue) => issue.issueId}
        />
    );
};
