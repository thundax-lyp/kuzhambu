import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Tabs, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import {
    KuzhambuPage,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuAlert,
    KuzhambuCard
} from "@/components";

import { isPositiveDecimalId, normalizeNullableId } from "@/types/id";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { QualityReportGenerateForm } from "./components/quality-report-generate-form";
import { QualityReportAnnotationTable } from "./components/quality-report-annotation-table";
import { QualityReportHistoryTable } from "./components/quality-report-history-table";
import { QualityReportIssueTable } from "./components/quality-report-issue-table";
import { QualityReportSourceTable } from "./components/quality-report-source-table";
import { QualityReportSummary } from "./components/quality-report-summary";
import * as service from "./quality-report-service";
import type {
    QualityReportDetailRecord,
    QualityReportRecord,
    QualityReportSourceDetailRecord,
    ReextractLowQualityCategoryRecord
} from "./quality-report-types";

import "./quality-report-page.css";

const { Text, Title } = Typography;
const QUALITY_REEXTRACT_TASK_TYPE = "GRAPH";
const QUALITY_REEXTRACT_MODEL_ID = "1";
const QUALITY_REEXTRACT_MODEL_NAME = "gpt-5.5";
const QUALITY_REEXTRACT_PROMPT_MESSAGES_JSON =
    '[{"role":"system","content":"extract knowledge graph from quality report low quality category"}]';
const QUALITY_REEXTRACT_INPUT_PAYLOAD_JSON = '{"triggerSource":"QUALITY_REPORT"}';

const readGraphVersionIdFromSearch = () => {
    if (typeof window === "undefined") {
        return null;
    }
    const graphVersionId = new URLSearchParams(window.location.search)
        .get("graphVersionId")
        ?.trim();
    return isPositiveDecimalId(graphVersionId) ? graphVersionId : null;
};

const readRegenerateModeFromSearch = () => {
    if (typeof window === "undefined") {
        return false;
    }
    return new URLSearchParams(window.location.search).get("regenerate") === "1";
};

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

export const QualityReportPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canView = hasPermission("knowledge:quality-report:view");
    const canGenerate = hasPermission("knowledge:quality-report:generate");
    const canReextract = hasPermission("knowledge:graph:edit");
    const [regenerateMode] = useState(() => readRegenerateModeFromSearch());
    const [graphVersionId, setGraphVersionId] = useState<string | null>(() =>
        readGraphVersionIdFromSearch()
    );
    const [detailReport, setDetailReport] = useState<QualityReportDetailRecord | null>(null);
    const [latestReextractTask, setLatestReextractTask] =
        useState<ReextractLowQualityCategoryRecord | null>(null);

    const latestReportQuery = useQuery({
        queryKey: ["knowledge", "quality-report", "latest", graphVersionId],
        queryFn: () => service.getLatestReport({ graphVersionId }),
        enabled: canView,
        retry: false
    });
    const reportPageQuery = useQuery({
        queryKey: ["knowledge", "quality-report", "page", graphVersionId],
        queryFn: () =>
            service.pageReports({
                pageNo: DEFAULT_PAGE_NO,
                pageSize: DEFAULT_PAGE_SIZE,
                graphVersionId
            }),
        enabled: canView,
        retry: false
    });
    const reportDetailMutation = useMutation({
        mutationFn: (report: QualityReportRecord) =>
            service.getReportDetail({ reportId: report.reportId }),
        onSuccess: setDetailReport,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "加载质量报告详情失败");
        }
    });
    const generateMutation = useMutation({
        mutationFn: (targetGraphVersionId: string) =>
            service.generateReport({
                graphVersionId: targetGraphVersionId,
                generatedBy: 1
            }),
        onSuccess: async (detail) => {
            setDetailReport(detail);
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

    const currentReportDetail = detailReport || latestReportQuery.data || null;
    const currentReport = currentReportDetail?.report || null;
    const staleGraphVersionId =
        normalizeNullableId(currentReport?.graphVersionId) || graphVersionId || null;
    const reportItems = reportPageQuery.data?.records || [];

    const reextractMutation = useMutation({
        mutationFn: (sourceDetail: QualityReportSourceDetailRecord) => {
            if (!currentReport?.reportId) {
                throw new Error("缺少质量报告 ID");
            }
            if (!sourceDetail.sourceCategoryCode) {
                throw new Error("缺少来源门类编码");
            }
            return service.reextractLowQualityCategory({
                reportId: currentReport.reportId,
                sourceCategoryCode: sourceDetail.sourceCategoryCode,
                taskType: QUALITY_REEXTRACT_TASK_TYPE,
                replaceUnconfirmedOnly: true,
                modelId: QUALITY_REEXTRACT_MODEL_ID,
                modelName: QUALITY_REEXTRACT_MODEL_NAME,
                promptMessagesJson: QUALITY_REEXTRACT_PROMPT_MESSAGES_JSON,
                inputPayloadJson: QUALITY_REEXTRACT_INPUT_PAYLOAD_JSON,
                requestedBy: "1"
            });
        },
        onSuccess: (task) => {
            setLatestReextractTask(task);
            messageApi.success("低质量门类重提取任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "创建重提取任务失败");
        }
    });

    const openReextractConfirm = (sourceDetail: QualityReportSourceDetailRecord) => {
        const sourceTitle =
            sourceDetail.sourceCategoryName || sourceDetail.sourceCategoryCode || "来源明细";
        confirm.danger({
            title: "确认重提取低质量门类",
            message: `确认重提取 ${sourceTitle}？`,
            description: "系统将创建图谱抽取任务，只替换未确认的实体、关系和谱系候选。",
            okText: "重提取",
            onConfirm: () => reextractMutation.mutateAsync(sourceDetail)
        });
    };

    return (
        <KuzhambuPage
            className="quality-report-page knowledge-quality-report-page"
            description="基于人工质量标注、精修状态和正式图谱事实生成质量报告快照。"
            title="质量报告"
        >
            <KuzhambuSpace
                className="knowledge-quality-report-layout"
                orientation="vertical"
                size={16}
            >
                <KuzhambuAlert
                    banner
                    title="质量报告由人工标注和正式图谱状态生成，用于展示当前 Knowledge 质量治理快照。"
                    type="info"
                />

                <KuzhambuCard className="knowledge-quality-report-card" title="生成报告">
                    <QualityReportGenerateForm
                        disabled={!canGenerate}
                        graphVersionId={graphVersionId}
                        loading={generateMutation.isPending}
                        submitLabel={regenerateMode ? "重新生成报告" : "生成报告"}
                        onChange={setGraphVersionId}
                        onGenerate={() => generateMutation.mutate(graphVersionId || "")}
                    />
                </KuzhambuCard>

                {currentReport ? (
                    <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                        {currentReportDetail?.stale ? (
                            <KuzhambuAlert
                                action={
                                    <KuzhambuButton
                                        testId="knowledge-quality-report-quality-report-action-button"
                                        disabled={!canGenerate || !staleGraphVersionId}
                                        loading={generateMutation.isPending}
                                        size="small"
                                        onClick={() => {
                                            if (staleGraphVersionId) {
                                                setGraphVersionId(staleGraphVersionId);
                                                generateMutation.mutate(staleGraphVersionId);
                                            }
                                        }}
                                    >
                                        重新生成报告
                                    </KuzhambuButton>
                                }
                                description={`最新精修时间：${formatTimestamp(currentReportDetail.lastRefinementAppliedAt)}`}
                                showIcon
                                title="该版本质量报告早于最新精修应用"
                                type="warning"
                            />
                        ) : null}
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
                        {latestReextractTask ? (
                            <KuzhambuAlert
                                action={
                                    <KuzhambuButton
                                        testId="knowledge-quality-report-quality-report-action-button-2"
                                        href="/knowledge/graph-extraction"
                                        size="small"
                                    >
                                        打开任务台账
                                    </KuzhambuButton>
                                }
                                className="knowledge-quality-report-reextract-alert"
                                description={
                                    <div className="knowledge-quality-report-reextract-fields">
                                        <Text>任务号：{latestReextractTask.taskId || "-"}</Text>
                                        <Text>任务类型：{latestReextractTask.taskType || "-"}</Text>
                                        <Text>
                                            触发来源：{latestReextractTask.triggerSource || "-"}
                                        </Text>
                                        <Text>批次号：{latestReextractTask.batchJobId || "-"}</Text>
                                    </div>
                                }
                                showIcon
                                title="低质量门类重提取任务已创建"
                                type="success"
                            />
                        ) : null}
                        <KuzhambuCard className="knowledge-quality-report-card" title="报告详情">
                            <Tabs
                                items={[
                                    {
                                        key: "issues",
                                        label: "问题清单",
                                        children: (
                                            <QualityReportIssueTable
                                                issues={currentReportDetail?.issues || []}
                                            />
                                        )
                                    },
                                    {
                                        key: "sources",
                                        label: "来源明细",
                                        children: (
                                            <QualityReportSourceTable
                                                canReextract={canReextract}
                                                reextractingDetailId={
                                                    reextractMutation.isPending
                                                        ? reextractMutation.variables?.detailId
                                                        : null
                                                }
                                                reportNo={currentReport.reportNo}
                                                sourceDetails={
                                                    currentReportDetail?.sourceDetails || []
                                                }
                                                onReextract={openReextractConfirm}
                                            />
                                        )
                                    },
                                    {
                                        key: "annotations",
                                        label: "人工标注",
                                        children: (
                                            <QualityReportAnnotationTable
                                                annotations={currentReportDetail?.annotations || []}
                                            />
                                        )
                                    }
                                ]}
                            />
                        </KuzhambuCard>
                    </KuzhambuSpace>
                ) : (
                    <KuzhambuCard className="knowledge-quality-report-card">
                        <Empty
                            className="knowledge-quality-report-empty"
                            description="尚未生成质量报告"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    </KuzhambuCard>
                )}

                <section aria-labelledby="knowledge-quality-report-history">
                    <div className="knowledge-quality-report-section-header">
                        <Title id="knowledge-quality-report-history" level={4}>
                            历史报告
                        </Title>
                        <Text type="secondary">查看已发布报告并切换摘要上下文。</Text>
                    </div>
                    <KuzhambuCard className="knowledge-quality-report-card" variant="borderless">
                        <QualityReportHistoryTable
                            loading={reportPageQuery.isLoading || reportDetailMutation.isPending}
                            reports={reportItems}
                            onView={(report) => reportDetailMutation.mutate(report)}
                        />
                    </KuzhambuCard>
                </section>
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
