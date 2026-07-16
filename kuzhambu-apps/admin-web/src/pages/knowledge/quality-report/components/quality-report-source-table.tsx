import { ReloadOutlined } from "@ant-design/icons";
import { Table, Tag, Tooltip } from "antd";
import { KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import type { ColumnsType } from "antd/es/table";
import type { QualityReportSourceDetailRecord } from "../quality-report-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface QualityReportSourceTableProps {
    canReextract?: boolean;
    reextractingDetailId?: number | null;
    reportNo?: string | null;
    sourceDetails: QualityReportSourceDetailRecord[];
    onReextract?: (sourceDetail: QualityReportSourceDetailRecord) => void;
}

const readReextractDisabledReason = (
    sourceDetail: QualityReportSourceDetailRecord,
    reportNo?: string | null,
    canReextract = false
) => {
    if (!canReextract) {
        return "缺少图谱编辑权限";
    }
    if (!reportNo) {
        return "缺少报告编号";
    }
    if (!sourceDetail.sourceCategoryCode) {
        return "缺少门类编码";
    }
    if (!sourceDetail.issueCount || sourceDetail.issueCount <= 0) {
        return "该来源明细没有质量问题";
    }
    return "";
};

export const QualityReportSourceTable = ({
    canReextract = false,
    reextractingDetailId = null,
    reportNo = null,
    sourceDetails,
    onReextract
}: QualityReportSourceTableProps) => {
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
            render: (_, sourceDetail) => {
                const disabledReason = readReextractDisabledReason(
                    sourceDetail,
                    reportNo,
                    canReextract
                );
                const reextractDisabled = Boolean(disabledReason);
                return (
                    <KuzhambuSpaceCompact className="knowledge-quality-report-source-actions">
                        <KuzhambuButton
                            testId="knowledge-quality-report-quality-report-source-action-button"
                            disabled={!sourceDetail.href}
                            href={sourceDetail.href || undefined}
                        >
                            打开
                        </KuzhambuButton>
                        <Tooltip title={disabledReason || "从质量报告低质量门类重提取"}>
                            <span>
                                <KuzhambuButton
                                    testId="knowledge-quality-report-quality-report-source-action-button-2"
                                    disabled={reextractDisabled}
                                    icon={<ReloadOutlined />}
                                    loading={reextractingDetailId === sourceDetail.detailId}
                                    onClick={() => onReextract?.(sourceDetail)}
                                >
                                    重提取
                                </KuzhambuButton>
                            </span>
                        </Tooltip>
                    </KuzhambuSpaceCompact>
                );
            }
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
