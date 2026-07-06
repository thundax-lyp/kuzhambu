import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Card, Empty, Tabs, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { QualityReportGenerateForm } from "./components/quality-report-generate-form";
import { QualityReportAnnotationTable } from "./components/quality-report-annotation-table";
import { QualityReportHistoryTable } from "./components/quality-report-history-table";
import { QualityReportIssueTable } from "./components/quality-report-issue-table";
import { QualityReportSourceTable } from "./components/quality-report-source-table";
import { QualityReportSummary } from "./components/quality-report-summary";
import * as service from "./quality-report-service";
import type { QualityReportDetailRecord, QualityReportRecord } from "./quality-report-types";
import "./quality-report-page.css";

const { Text, Title } = Typography;

export const QualityReportPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canView = hasPermission("knowledge:quality-report:view");
    const canGenerate = hasPermission("knowledge:quality-report:generate");
    const [graphVersionId, setGraphVersionId] = useState<number | null>(null);
    const [selectedDetail, setSelectedDetail] = useState<QualityReportDetailRecord | null>(null);

    const latestReportQuery = useQuery({
        queryKey: ["knowledge", "quality-report", "latest"],
        queryFn: () => service.getLatestReport({}),
        enabled: canView,
        retry: false
    });
    const reportPageQuery = useQuery({
        queryKey: ["knowledge", "quality-report", "page"],
        queryFn: () =>
            service.pageReports({
                pageNo: DEFAULT_PAGE_NO,
                pageSize: DEFAULT_PAGE_SIZE
            }),
        enabled: canView,
        retry: false
    });
    const detailMutation = useMutation({
        mutationFn: (report: QualityReportRecord) =>
            service.getReportDetail({ reportId: report.reportId }),
        onSuccess: setSelectedDetail,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "加载质量报告详情失败");
        }
    });
    const generateMutation = useMutation({
        mutationFn: () =>
            service.generateReport({
                graphVersionId: graphVersionId || 0,
                generatedBy: 1
            }),
        onSuccess: async (detail) => {
            setSelectedDetail(detail);
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "quality-report", "latest"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "quality-report", "page"]
                })
            ]);
            messageApi.success("质量报告已生成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "生成质量报告失败");
        }
    });

    const currentDetail = selectedDetail || latestReportQuery.data || null;
    const currentReport = currentDetail?.report || null;
    const reportItems = reportPageQuery.data?.records || [];

    return (
        <KuzhambuPage
            className="quality-report-page knowledge-quality-report-page"
            description="基于人工质量标注、精修状态和正式图谱事实生成质量报告快照。"
            eyebrow="Knowledge / Quality Report"
            title="质量报告"
        >
            <KuzhambuSpace
                className="knowledge-quality-report-layout"
                orientation="vertical"
                size={16}
            >
                <Alert
                    banner
                    title="质量报告由人工标注和正式图谱状态生成，用于展示当前 Knowledge 质量治理快照。"
                    type="info"
                />

                <Card className="knowledge-quality-report-card" title="生成报告">
                    <QualityReportGenerateForm
                        disabled={!canGenerate}
                        graphVersionId={graphVersionId}
                        loading={generateMutation.isPending}
                        onChange={setGraphVersionId}
                        onGenerate={() => generateMutation.mutate()}
                    />
                </Card>

                {currentReport ? (
                    <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                        <section aria-labelledby="knowledge-quality-report-summary">
                            <div className="knowledge-quality-report-section-header">
                                <Title id="knowledge-quality-report-summary" level={4}>
                                    报告摘要
                                </Title>
                                <Text type="secondary">
                                    {currentReport.reportNo || `#${currentReport.reportId}`}
                                </Text>
                            </div>
                            <QualityReportSummary report={currentReport} />
                        </section>
                        <Card className="knowledge-quality-report-card" title="报告详情">
                            <Tabs
                                items={[
                                    {
                                        key: "issues",
                                        label: "问题清单",
                                        children: (
                                            <QualityReportIssueTable
                                                issues={currentDetail?.issues || []}
                                            />
                                        )
                                    },
                                    {
                                        key: "sources",
                                        label: "来源明细",
                                        children: (
                                            <QualityReportSourceTable
                                                sourceDetails={currentDetail?.sourceDetails || []}
                                            />
                                        )
                                    },
                                    {
                                        key: "annotations",
                                        label: "人工标注",
                                        children: (
                                            <QualityReportAnnotationTable
                                                annotations={currentDetail?.annotations || []}
                                            />
                                        )
                                    }
                                ]}
                            />
                        </Card>
                    </KuzhambuSpace>
                ) : (
                    <Card className="knowledge-quality-report-card">
                        <Empty
                            className="knowledge-quality-report-empty"
                            description="尚未生成质量报告"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    </Card>
                )}

                <section aria-labelledby="knowledge-quality-report-history">
                    <div className="knowledge-quality-report-section-header">
                        <Title id="knowledge-quality-report-history" level={4}>
                            历史报告
                        </Title>
                        <Text type="secondary">查看已发布报告并切换摘要上下文。</Text>
                    </div>
                    <Card className="knowledge-quality-report-card" variant="borderless">
                        <QualityReportHistoryTable
                            loading={reportPageQuery.isLoading || detailMutation.isPending}
                            reports={reportItems}
                            onView={(report) => detailMutation.mutate(report)}
                        />
                    </Card>
                </section>
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
