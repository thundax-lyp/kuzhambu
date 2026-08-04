import { Table, Tag } from "antd";
import { normalizeId } from "@/types/id";
import type { ColumnsType } from "antd/es/table";
import type { QualityAnnotationRecord } from "../refinement-types";
import { KuzhambuButton } from "@/components";

interface RefinementQualityAnnotationTableProps {
    annotations: QualityAnnotationRecord[];
    loading?: boolean;
    onEdit: (annotation: QualityAnnotationRecord) => void;
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

export const RefinementQualityAnnotationTable = ({
    annotations,
    loading = false,
    onEdit
}: RefinementQualityAnnotationTableProps) => {
    const columns: ColumnsType<QualityAnnotationRecord> = [
        { title: "对象类型", dataIndex: "objectType", key: "objectType" },
        { title: "对象键", dataIndex: "objectKey", key: "objectKey" },
        {
            title: "状态",
            dataIndex: "annotationStatus",
            key: "annotationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        { title: "标签", dataIndex: "annotationLabel", key: "annotationLabel" },
        { title: "备注", dataIndex: "comment", key: "comment", ellipsis: true },
        {
            title: "操作",
            key: "actions",
            render: (_, annotation) => (
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-quality-annotation-annotate-button"
                    onClick={() => onEdit(annotation)}
                >
                    标注
                </KuzhambuButton>
            )
        }
    ];

    return (
        <Table<QualityAnnotationRecord>
            aria-label="知识图谱精修质量标注表格"
            columns={columns}
            dataSource={annotations}
            loading={loading}
            pagination={false}
            rowKey={(annotation) => normalizeId(annotation.annotationId)}
        />
    );
};
