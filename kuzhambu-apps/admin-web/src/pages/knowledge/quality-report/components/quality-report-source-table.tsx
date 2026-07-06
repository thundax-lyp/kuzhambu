import { Button, Table, Tag } from "antd";
import { KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import type { ColumnsType } from "antd/es/table";
import type { QualityReportSourceDetailRecord } from "../quality-report-types";

interface QualityReportSourceTableProps {
    sourceDetails: QualityReportSourceDetailRecord[];
}

export const QualityReportSourceTable = ({ sourceDetails }: QualityReportSourceTableProps) => {
    const columns: ColumnsType<QualityReportSourceDetailRecord> = [
        { title: "来源类型", dataIndex: "sourceContentType", key: "sourceContentType" },
        { title: "来源 ID", dataIndex: "sourceContentId", key: "sourceContentId" },
        { title: "门类", dataIndex: "sourceCategoryName", key: "sourceCategoryName" },
        { title: "图谱版本", dataIndex: "graphVersionId", key: "graphVersionId" },
        { title: "标注数", dataIndex: "annotationCount", key: "annotationCount" },
        { title: "问题数", dataIndex: "issueCount", key: "issueCount" },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            render: (status?: string | null) => <Tag color="blue">{status || "-"}</Tag>
        },
        {
            title: "操作",
            key: "actions",
            render: (_, sourceDetail) => (
                <KuzhambuSpaceCompact>
                    <Button disabled={!sourceDetail.href} href={sourceDetail.href || undefined}>
                        打开
                    </Button>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <Table<QualityReportSourceDetailRecord>
            aria-label="知识质量报告来源明细表格"
            columns={columns}
            dataSource={sourceDetails}
            pagination={false}
            rowKey={(sourceDetail) => sourceDetail.detailId}
        />
    );
};
