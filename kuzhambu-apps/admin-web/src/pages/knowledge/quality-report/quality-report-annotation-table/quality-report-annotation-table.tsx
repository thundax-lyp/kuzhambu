import { Table, Tag } from "antd";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { QualityReportAnnotationRecord } from "../quality-report-types";

interface QualityReportAnnotationTableProps {
    annotations: QualityReportAnnotationRecord[];
}

const readStatusColor = (status?: string | null) => {
    if (status === "PASSED") {
        return "green";
    }
    if (status === "IGNORED") {
        return "default";
    }
    return "orange";
};

export const QualityReportAnnotationTable = ({
    annotations
}: QualityReportAnnotationTableProps) => {
    const columns: ColumnsType<QualityReportAnnotationRecord> = [
        { title: "对象类型", dataIndex: "objectType", key: "objectType" },
        { title: "对象 key", dataIndex: "objectKey", key: "objectKey" },
        {
            title: "状态",
            dataIndex: "annotationStatus",
            key: "annotationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        { title: "标签", dataIndex: "annotationLabel", key: "annotationLabel" },
        { title: "说明", dataIndex: "comment", key: "comment", ellipsis: true }
    ];

    return (
        <Table<QualityReportAnnotationRecord>
            aria-label="知识质量报告人工标注表格"
            columns={columns}
            dataSource={annotations}
            pagination={false}
            rowKey={(annotation) => normalizeId(annotation.annotationId)}
        />
    );
};
