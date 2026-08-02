import {
    AppstoreOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    DatabaseOutlined,
    FileTextOutlined,
    HeartOutlined,
    ReloadOutlined,
    SearchOutlined,
    SecurityScanOutlined,
    WarningOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Segmented, Spin, Typography } from "antd";
import { type ReactNode, useState } from "react";
import { Link } from "react-router-dom";
import { hasPermission } from "@/auth/permission-storage";
import { DashboardAlertDrawer } from "./dashboard-alert-drawer";
import { DashboardMetricCard } from "./dashboard-metric-card";
import * as service from "./dashboard-service";
import type { OperationsDashboardOverviewQuery } from "./dashboard-service";
import type {
    OperationsDashboardPeriodType,
    OperationsHealthAlertRecord,
    OperationsHealthSummaryRecord
} from "./dashboard-types";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuDrawer,
    KuzhambuPage,
    KuzhambuSpace,
    KuzhambuTag,
    KuzhambuCard
} from "@/components";

import "./dashboard-page.css";

const { Text, Title } = Typography;

const periodOptions: Array<{ label: string; value: OperationsDashboardPeriodType }> = [
    { label: "近 7 天", value: "WEEK" },
    { label: "近 30 天", value: "MONTH" }
];

interface OperationEntry {
    description: string;
    icon: ReactNode;
    permissions: string[];
    testId: string;
    title: string;
    to: string;
}

const operationEntries: OperationEntry[] = [
    {
        description: "查看所有长任务、筛选执行状态并打开任务详情",
        icon: <ClockCircleOutlined />,
        title: "任务台账",
        to: "/operations/tasks",
        permissions: ["operations:task:view"],
        testId: "operations-entry-tasks"
    },
    {
        description: "查看备份、恢复记录并发起手动备份",
        icon: <DatabaseOutlined />,
        title: "备份恢复",
        to: "/operations/backup-restore",
        permissions: ["operations:backup:view", "operations:restore:view"],
        testId: "operations-entry-backup-restore"
    },
    {
        description: "查看清理任务、失败项并触发维护清理",
        icon: <AppstoreOutlined />,
        title: "清理维护",
        to: "/operations/cleanup",
        permissions: ["operations:cleanup:view"],
        testId: "operations-entry-cleanup"
    },
    {
        description: "查看 System 提供的系统运行与访问日志",
        icon: <SecurityScanOutlined />,
        title: "系统日志",
        to: "/system/logs",
        permissions: ["system:log:view"],
        testId: "operations-entry-system-log"
    },
    {
        description: "查看业务对象变更审计与操作者追踪",
        icon: <FileTextOutlined />,
        title: "审计日志",
        to: "/audit/logs",
        permissions: ["audit:view"],
        testId: "operations-entry-audit-log"
    }
];

const normalizeNumber = (value?: number | null) => {
    if (typeof value !== "number" || !Number.isFinite(value)) {
        return 0;
    }
    return value;
};

const formatNumber = (value?: number | null) => {
    const normalizedValue = normalizeNumber(value);
    return new Intl.NumberFormat("zh-CN").format(normalizedValue);
};

const formatLatency = (value?: number | null) => {
    if (typeof value !== "number" || !Number.isFinite(value)) {
        return "-";
    }
    return `${Math.round(value)} ms`;
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
    const normalizedStatus = (status || "").toUpperCase();
    if (["OK", "UP", "ACTIVE", "SUCCEEDED"].includes(normalizedStatus)) {
        return "success";
    }
    if (["WARN", "DEGRADED", "RUNNING", "QUEUED"].includes(normalizedStatus)) {
        return "warning";
    }
    if (["ERROR", "FAILED", "FAIL", "DOWN"].includes(normalizedStatus)) {
        return "danger";
    }
    return "neutral";
};

const filterAlertsByComponent = (
    alerts: OperationsHealthAlertRecord[],
    component?: string | null
) => {
    if (!component) {
        return [];
    }
    return alerts.filter((alert) => alert.component === component);
};

const buildOverviewQuery = (
    periodType: OperationsDashboardPeriodType
): OperationsDashboardOverviewQuery => {
    return { periodType };
};

const calculateBarWidth = (value: number, maxValue: number) => {
    if (maxValue <= 0) {
        return "0%";
    }
    return `${Math.max(6, Math.round((value / maxValue) * 100))}%`;
};

interface TrendPanelProps {
    items?: Array<{ bucket?: string | null; count?: number | null }> | null;
    title: string;
}

interface DashboardPermissionCapabilities {
    canViewDashboard: boolean;
    canViewClassicsContentSummary: boolean;
    canViewDiscoverySearchSummary: boolean;
    canViewDiscoveryQaSummary: boolean;
    canViewAiInvocationSummary: boolean;
    canViewKnowledgeTaxonomySummary: boolean;
    canViewHealthSummary: boolean;
    canManageHealthAlert: boolean;
    canViewTaskSummary: boolean;
    hasAnyChartPermission: boolean;
}

const TrendPanel = ({ items, title }: TrendPanelProps) => {
    const records = items || [];
    const maxValue = Math.max(...records.map((record) => normalizeNumber(record.count)), 0);

    return (
        <KuzhambuCard className="operations-dashboard-section-card" size="small" title={title}>
            {records.length ? (
                <div className="operations-dashboard-trend-bars">
                    {records.map((record, index) => {
                        const count = normalizeNumber(record.count);
                        const bucket = record.bucket || `#${index + 1}`;
                        return (
                            <div
                                className="operations-dashboard-trend-row"
                                key={`${bucket}-${index}`}
                            >
                                <Text className="operations-dashboard-trend-label">{bucket}</Text>
                                <div className="operations-dashboard-trend-track">
                                    <span style={{ width: calculateBarWidth(count, maxValue) }} />
                                </div>
                                <Text className="operations-dashboard-trend-count">
                                    {formatNumber(count)}
                                </Text>
                            </div>
                        );
                    })}
                </div>
            ) : (
                <Empty description="暂无趋势数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
        </KuzhambuCard>
    );
};

interface RankingListProps {
    emptyText: string;
    items: Array<{ label: string; meta: string; value: number }>;
    title: string;
}

const RankingList = ({ emptyText, items, title }: RankingListProps) => {
    return (
        <KuzhambuCard className="operations-dashboard-section-card" size="small" title={title}>
            {items.length ? (
                <ol className="operations-dashboard-ranking-list">
                    {items.map((item) => (
                        <li key={`${item.label}-${item.meta}`}>
                            <div>
                                <Text strong>{item.label}</Text>
                                <Text type="secondary">{item.meta}</Text>
                            </div>
                            <Text>{formatNumber(item.value)}</Text>
                        </li>
                    ))}
                </ol>
            ) : (
                <Empty description={emptyText} image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
        </KuzhambuCard>
    );
};

export const OperationsDashboardPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const permissions = resolveDashboardPermissionCapabilities();
    const canViewDashboard = permissions.canViewDashboard;
    const canViewHealthPage = permissions.canViewHealthSummary;
    const canViewClassicsContentSummary = permissions.canViewClassicsContentSummary;
    const canManageHealthAlert = permissions.canManageHealthAlert;
    const canViewDiscoverySearchSummary = permissions.canViewDiscoverySearchSummary;
    const canViewDiscoveryQaSummary = permissions.canViewDiscoveryQaSummary;
    const canViewAiInvocationSummary = permissions.canViewAiInvocationSummary;
    const canViewKnowledgeTaxonomySummary = permissions.canViewKnowledgeTaxonomySummary;
    const canViewTaskSummary = permissions.canViewTaskSummary;
    const visibleOperationEntries = operationEntries.filter((entry) =>
        entry.permissions.some((permission) => hasPermission(permission))
    );
    const [periodType, setPeriodType] = useState<OperationsDashboardPeriodType>("WEEK");
    const [alertDrawerOpen, setAlertDrawerOpen] = useState(false);
    const [detailHealth, setDetailHealth] = useState<OperationsHealthSummaryRecord | null>(null);

    const dashboardQuery = useQuery({
        queryKey: ["operations", "dashboard", "overview", periodType],
        queryFn: () => service.getDashboardOverview(buildOverviewQuery(periodType)),
        enabled: canViewDashboard,
        retry: false
    });

    const trendQuery = useQuery({
        queryKey: ["operations", "health", "trend", periodType],
        queryFn: () =>
            service.getHealthTrend({
                bucketType: periodType === "WEEK" ? "DAY" : "DAY"
            }),
        enabled: canViewDashboard && canViewHealthPage,
        retry: false
    });

    const alertQuery = useQuery({
        queryKey: ["operations", "health", "alerts", "dashboard"],
        queryFn: () =>
            service.getHealthAlerts({
                pageNo: 1,
                pageSize: 20
            }),
        enabled: canViewDashboard && canViewHealthPage,
        retry: false
    });

    const refreshAlerts = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["operations", "health", "alerts"] }),
            queryClient.invalidateQueries({ queryKey: ["operations", "dashboard", "overview"] })
        ]);
    };

    const confirmAlertMutation = useMutation({
        mutationFn: service.confirmHealthAlert,
        onSuccess: async () => {
            await refreshAlerts();
            messageApi.success("告警已确认");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "告警确认失败");
        }
    });

    const recoverAlertMutation = useMutation({
        mutationFn: service.recoverHealthAlert,
        onSuccess: async () => {
            await refreshAlerts();
            messageApi.success("告警已标记恢复");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "告警恢复失败");
        }
    });

    const refreshDashboard = async () => {
        await queryClient.invalidateQueries({ queryKey: ["operations", "dashboard", "overview"] });
        if (canViewHealthPage) {
            await queryClient.invalidateQueries({ queryKey: ["operations", "health", "trend"] });
            await queryClient.invalidateQueries({ queryKey: ["operations", "health", "alerts"] });
        }
    };

    const overview = dashboardQuery.data;
    const healthSummaries = (canViewHealthPage ? overview?.healthSummaries : null) || [];
    const healthTrend = canViewHealthPage ? trendQuery.data || [] : [];
    const healthAlerts = canViewHealthPage ? alertQuery.data?.records || [] : [];
    const openHealthAlerts = healthAlerts.filter((alert) => alert.alertStatus !== "RECOVERED");
    const latestOpenAlert = canViewHealthPage
        ? overview?.latestAlert || openHealthAlerts[0] || null
        : null;
    const unhealthyCount = normalizeNumber(overview?.unhealthyComponentCount);
    const failedTaskCount = normalizeNumber(overview?.failedTaskCount);
    const runningTaskCount = normalizeNumber(overview?.runningTaskCount);
    const activeAlertCount = canViewHealthPage
        ? normalizeNumber(overview?.activeAlertCount || openHealthAlerts.length)
        : 0;
    const criticalAlertCount = canViewHealthPage
        ? normalizeNumber(
              overview?.criticalAlertCount ||
                  openHealthAlerts.filter((alert) => alert.alertLevel === "CRITICAL").length
          )
        : 0;
    const canRenderHealthAlertBanner =
        canViewHealthPage && (openHealthAlerts.length > 0 || activeAlertCount > 0);
    const hasHealthOverviewSummary = canViewHealthPage && overview?.latestAlert != null;
    const canRenderHealthBanner = canRenderHealthAlertBanner || hasHealthOverviewSummary;
    const visibleHealthSummaries = canViewHealthPage ? healthSummaries : [];
    const visibleHealthAlerts = canViewHealthPage ? openHealthAlerts : [];

    const selectedHealthAlertInfo =
        detailHealth != null
            ? filterAlertsByComponent(visibleHealthAlerts, detailHealth?.component)
            : [];

    const isLoading =
        dashboardQuery.isLoading ||
        (canViewHealthPage && (trendQuery.isLoading || alertQuery.isLoading));

    const topContents =
        canViewClassicsContentSummary && overview?.topContents
            ? overview.topContents.map((item) => ({
                  label: item.title || `内容 #${item.contentId || "-"}`,
                  meta: item.contentType || "内容",
                  value: normalizeNumber(item.visitCount)
              }))
            : [];
    const topQueries =
        canViewDiscoverySearchSummary && overview?.topQueries
            ? overview.topQueries.map((item) => ({
                  label: item.queryText || "-",
                  meta: "搜索词",
                  value: normalizeNumber(item.count)
              }))
            : [];
    const topTags =
        canViewKnowledgeTaxonomySummary && overview?.topTags
            ? overview.topTags.map((item) => ({
                  label: item.tagName || "-",
                  meta: "标签覆盖",
                  value: normalizeNumber(item.contentRefCount)
              }))
            : [];
    const topAiCapabilities =
        canViewAiInvocationSummary && overview?.topAiCapabilities
            ? overview.topAiCapabilities.map((item) => ({
                  label: item.capability || "-",
                  meta: "AI 能力",
                  value: normalizeNumber(item.invocationCount)
              }))
            : [];

    const shouldRenderContentMetricCard = canViewClassicsContentSummary;
    const shouldRenderSearchQaMetricCard =
        canViewDiscoverySearchSummary || canViewDiscoveryQaSummary;
    const shouldRenderAiMetricCard = canViewAiInvocationSummary;
    const shouldRenderHealthTaskMetricCard = canViewHealthPage || canViewTaskSummary;

    const contentMetricSecondary = [
        canViewClassicsContentSummary
            ? `译文 ${formatNumber(overview?.translatedContentCount)}`
            : null,
        canViewClassicsContentSummary
            ? `图像就绪 ${formatNumber(overview?.imageReadyContentCount)}`
            : null
    ]
        .filter((text): text is string => text != null)
        .join("，");

    let searchQaTitle = canViewDiscoveryQaSummary ? "问答" : "搜索";
    let searchQaStatisticValue = formatNumber(
        canViewDiscoverySearchSummary ? overview?.searchCount : overview?.qaCount
    );
    if (canViewDiscoverySearchSummary && canViewDiscoveryQaSummary) {
        searchQaTitle = "搜索 / 问答";
        searchQaStatisticValue = `${formatNumber(overview?.searchCount)} / ${formatNumber(
            overview?.qaCount
        )}`;
    }
    const searchQaSecondaryText = canViewDiscoverySearchSummary
        ? `平均搜索延迟 ${formatLatency(overview?.avgSearchLatencyMs)}`
        : "";

    let healthTaskTitle = "";
    if (canViewTaskSummary) {
        healthTaskTitle = "失败任务";
    } else if (canViewHealthPage) {
        healthTaskTitle = "异常组件";
    }
    let healthTaskStatisticValue = "";
    if (canViewHealthPage && canViewTaskSummary) {
        healthTaskTitle = "异常组件 / 失败任务";
        healthTaskStatisticValue = `${formatNumber(unhealthyCount)} / ${formatNumber(failedTaskCount)}`;
    } else if (canViewHealthPage) {
        healthTaskStatisticValue = formatNumber(unhealthyCount);
    } else if (canViewTaskSummary) {
        healthTaskStatisticValue = formatNumber(failedTaskCount);
    }
    const healthTaskSecondaryText = canViewTaskSummary
        ? `运行中任务 ${formatNumber(runningTaskCount)}`
        : "";

    const shouldRenderContentTrendPanel = canViewClassicsContentSummary;
    const shouldRenderSearchTrendPanel = canViewDiscoverySearchSummary;
    const shouldRenderQaTrendPanel = canViewDiscoveryQaSummary;
    const shouldRenderTagTrendPanel = canViewKnowledgeTaxonomySummary;
    const shouldRenderTrendPanel =
        shouldRenderContentTrendPanel ||
        shouldRenderSearchTrendPanel ||
        shouldRenderQaTrendPanel ||
        shouldRenderTagTrendPanel;

    const shouldRenderTopContentsRanking = canViewClassicsContentSummary;
    const shouldRenderTopQueriesRanking = canViewDiscoverySearchSummary;
    const shouldRenderTopTagsRanking = canViewKnowledgeTaxonomySummary;
    const shouldRenderTopAiCapabilitiesRanking = canViewAiInvocationSummary;
    const shouldRenderRankingSection =
        shouldRenderTopContentsRanking ||
        shouldRenderTopQueriesRanking ||
        shouldRenderTopTagsRanking ||
        shouldRenderTopAiCapabilitiesRanking;
    const shouldRenderHealthSection = canViewHealthPage;
    const shouldRenderAnyChartSections =
        shouldRenderContentMetricCard ||
        shouldRenderSearchQaMetricCard ||
        shouldRenderAiMetricCard ||
        shouldRenderHealthTaskMetricCard ||
        shouldRenderTrendPanel ||
        shouldRenderHealthSection ||
        shouldRenderRankingSection;

    return (
        <KuzhambuPage
            actions={
                <KuzhambuSpace size={10} wrap>
                    <Segmented
                        aria-label="看板周期"
                        options={periodOptions}
                        value={periodType}
                        onChange={(value) => setPeriodType(value as OperationsDashboardPeriodType)}
                    />
                    <KuzhambuButton
                        testId="operations-dashboard-dashboard-refresh-button"
                        icon={<ReloadOutlined />}
                        onClick={() => void refreshDashboard()}
                        disabled={!canViewDashboard}
                    >
                        刷新
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
            className="dashboard-page operations-dashboard-page"
            description="集中查看内容、发现、健康巡检和长任务状态，作为运维处理入口。"
            title="运营看板"
        >
            {!canViewDashboard ? (
                <Empty description="缺少 operations:dashboard:view 权限" />
            ) : (
                <div className="operations-dashboard-content">
                    {isLoading && !overview ? <Spin size="large" /> : null}

                    {canRenderHealthBanner ? (
                        <KuzhambuAlert
                            action={
                                <KuzhambuButton
                                    testId="operations-dashboard-dashboard-view-alerts-button"
                                    size="small"
                                    onClick={() => setAlertDrawerOpen(true)}
                                >
                                    查看告警
                                </KuzhambuButton>
                            }
                            className="operations-dashboard-alert-banner"
                            description={
                                latestOpenAlert
                                    ? `最新：${latestOpenAlert.message || latestOpenAlert.component || "-"}`
                                    : undefined
                            }
                            title={`健康告警 ${formatNumber(activeAlertCount)} 个，严重 ${formatNumber(criticalAlertCount)} 个`}
                            showIcon
                            type={criticalAlertCount > 0 ? "error" : "warning"}
                        />
                    ) : null}
                    {shouldRenderAnyChartSections ? (
                        <>
                            <section className="operations-dashboard-metrics" aria-label="核心指标">
                                {shouldRenderContentMetricCard ? (
                                    <DashboardMetricCard
                                        title="内容总量"
                                        value={formatNumber(overview?.contentCount)}
                                        prefix={<DatabaseOutlined />}
                                        secondaryText={contentMetricSecondary}
                                    />
                                ) : null}
                                {shouldRenderSearchQaMetricCard ? (
                                    <DashboardMetricCard
                                        title={searchQaTitle}
                                        value={searchQaStatisticValue}
                                        prefix={<SearchOutlined />}
                                        secondaryText={searchQaSecondaryText}
                                    />
                                ) : null}
                                {shouldRenderAiMetricCard ? (
                                    <DashboardMetricCard
                                        title="AI 调用成功率"
                                        value={formatNumber(overview?.aiSucceededInvocationCount)}
                                        suffix={`/ ${formatNumber(overview?.aiInvocationCount)}`}
                                        prefix={<CheckCircleOutlined />}
                                        secondaryText={`失败 ${formatNumber(overview?.aiFailedInvocationCount)}，平均延迟 ${formatLatency(overview?.aiAvgLatencyMs)}，成本 ${formatNumber(overview?.aiTotalCostAmount)}`}
                                    />
                                ) : null}
                                {shouldRenderHealthTaskMetricCard ? (
                                    <DashboardMetricCard
                                        title={healthTaskTitle}
                                        value={healthTaskStatisticValue}
                                        prefix={<WarningOutlined />}
                                        secondaryText={healthTaskSecondaryText}
                                    />
                                ) : null}
                            </section>

                            {shouldRenderTrendPanel ? (
                                <section className="operations-dashboard-grid operations-dashboard-grid-two">
                                    {shouldRenderContentTrendPanel ? (
                                        <TrendPanel
                                            items={overview?.contentGrowthSeries}
                                            title="内容增长趋势"
                                        />
                                    ) : null}
                                    {shouldRenderSearchTrendPanel ? (
                                        <TrendPanel
                                            items={overview?.searchTrendSeries}
                                            title="搜索趋势"
                                        />
                                    ) : null}
                                    {shouldRenderQaTrendPanel ? (
                                        <TrendPanel
                                            items={overview?.qaTrendSeries}
                                            title="问答趋势"
                                        />
                                    ) : null}
                                    {shouldRenderTagTrendPanel ? (
                                        <TrendPanel
                                            items={overview?.tagGrowthSeries}
                                            title="标签增长趋势"
                                        />
                                    ) : null}
                                </section>
                            ) : null}

                            {shouldRenderHealthSection ? (
                                <section className="operations-dashboard-grid operations-dashboard-grid-two">
                                    {canViewHealthPage ? (
                                        <KuzhambuCard
                                            className="operations-dashboard-section-card"
                                            extra={
                                                canViewHealthPage ? (
                                                    <Link to="/operations/health">查看全部</Link>
                                                ) : null
                                            }
                                            size="small"
                                            title="健康巡检"
                                        >
                                            {visibleHealthSummaries.length ? (
                                                <div className="operations-dashboard-health-list">
                                                    {visibleHealthSummaries.map((summary) => {
                                                        const summaryAlertCount =
                                                            filterAlertsByComponent(
                                                                visibleHealthAlerts,
                                                                summary.component
                                                            ).length;
                                                        return (
                                                            <button
                                                                className="operations-dashboard-health-item"
                                                                key={summary.checkId}
                                                                onClick={() =>
                                                                    setDetailHealth(summary)
                                                                }
                                                                type="button"
                                                            >
                                                                <span>
                                                                    <HeartOutlined />
                                                                    <Text strong>
                                                                        {summary.component ||
                                                                            "未知组件"}
                                                                    </Text>
                                                                </span>
                                                                {summaryAlertCount ? (
                                                                    <KuzhambuTag type="danger">
                                                                        告警{" "}
                                                                        {formatNumber(
                                                                            summaryAlertCount
                                                                        )}
                                                                    </KuzhambuTag>
                                                                ) : null}
                                                                <KuzhambuTag
                                                                    type={statusTone(
                                                                        summary.healthStatus
                                                                    )}
                                                                >
                                                                    {summary.healthStatus ||
                                                                        "UNKNOWN"}
                                                                </KuzhambuTag>
                                                                <Text type="secondary">
                                                                    {formatLatency(
                                                                        summary.latencyMs
                                                                    )}
                                                                </Text>
                                                            </button>
                                                        );
                                                    })}
                                                </div>
                                            ) : (
                                                <Empty
                                                    description="暂无健康摘要"
                                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                                />
                                            )}
                                        </KuzhambuCard>
                                    ) : null}
                                    {canViewHealthPage ? (
                                        <KuzhambuCard
                                            className="operations-dashboard-section-card"
                                            size="small"
                                            title="健康趋势"
                                        >
                                            {healthTrend.length ? (
                                                <div className="operations-dashboard-health-trend">
                                                    {healthTrend.map((bucket) => (
                                                        <div key={bucket.bucket || "unknown"}>
                                                            <Text>{bucket.bucket || "-"}</Text>
                                                            <KuzhambuSpace size={6} wrap>
                                                                <KuzhambuTag type="success">
                                                                    UP{" "}
                                                                    {formatNumber(bucket.upCount)}
                                                                </KuzhambuTag>
                                                                <KuzhambuTag type="warning">
                                                                    DEGRADED{" "}
                                                                    {formatNumber(
                                                                        bucket.degradedCount
                                                                    )}
                                                                </KuzhambuTag>
                                                                <KuzhambuTag type="danger">
                                                                    DOWN{" "}
                                                                    {formatNumber(bucket.downCount)}
                                                                </KuzhambuTag>
                                                                <Text type="secondary">
                                                                    平均{" "}
                                                                    {formatLatency(
                                                                        bucket.avgLatencyMs
                                                                    )}
                                                                </Text>
                                                            </KuzhambuSpace>
                                                        </div>
                                                    ))}
                                                </div>
                                            ) : (
                                                <Empty
                                                    description="暂无健康趋势"
                                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                                />
                                            )}
                                        </KuzhambuCard>
                                    ) : null}
                                </section>
                            ) : null}

                            {shouldRenderRankingSection ? (
                                <section className="operations-dashboard-grid operations-dashboard-grid-three">
                                    {shouldRenderTopContentsRanking ? (
                                        <RankingList
                                            emptyText="暂无内容排行"
                                            items={topContents}
                                            title="热门内容"
                                        />
                                    ) : null}
                                    {shouldRenderTopQueriesRanking ? (
                                        <RankingList
                                            emptyText="暂无查询排行"
                                            items={topQueries}
                                            title="热门搜索"
                                        />
                                    ) : null}
                                    {shouldRenderTopTagsRanking ? (
                                        <RankingList
                                            emptyText="暂无标签排行"
                                            items={topTags}
                                            title="标签覆盖"
                                        />
                                    ) : null}
                                    {shouldRenderTopAiCapabilitiesRanking ? (
                                        <RankingList
                                            emptyText="暂无 AI 能力排行"
                                            items={topAiCapabilities}
                                            title="AI 能力"
                                        />
                                    ) : null}
                                </section>
                            ) : null}
                        </>
                    ) : (
                        <Empty
                            description="当前账号暂无可查看的看板图表"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    )}

                    <section>
                        <Title level={5}>运维入口</Title>
                        <div className="operations-dashboard-entry-grid">
                            {visibleOperationEntries.length ? (
                                visibleOperationEntries.map((entry) => (
                                    <Link
                                        className="operations-dashboard-entry"
                                        data-testid={entry.testId}
                                        key={entry.to}
                                        to={entry.to}
                                    >
                                        <KuzhambuCard size="small">
                                            <KuzhambuSpace size={8}>
                                                {entry.icon}
                                                <Text strong>{entry.title}</Text>
                                            </KuzhambuSpace>
                                            <Text type="secondary">{entry.description}</Text>
                                        </KuzhambuCard>
                                    </Link>
                                ))
                            ) : (
                                <Empty description="暂无可访问的运维入口" />
                            )}
                        </div>
                    </section>

                    <DashboardAlertDrawer
                        alerts={visibleHealthAlerts}
                        canManageHealthAlert={canManageHealthAlert}
                        isConfirmingAlert={confirmAlertMutation.isPending}
                        isRecoveringAlert={recoverAlertMutation.isPending}
                        open={alertDrawerOpen}
                        onClose={() => setAlertDrawerOpen(false)}
                        onConfirmAlert={(alert) =>
                            confirmAlertMutation.mutate({
                                alertId: alert.alertId
                            })
                        }
                        onRecoverAlert={(alert) =>
                            recoverAlertMutation.mutate({
                                alertId: alert.alertId
                            })
                        }
                    />

                    <KuzhambuDrawer
                        testId="operations-dashboard-dashboard-2-drawer"
                        open={detailHealth !== null}
                        onClose={() => setDetailHealth(null)}
                        size="middle"
                        title={
                            detailHealth
                                ? `${detailHealth.component || "未知组件"} 健康明细`
                                : "健康明细"
                        }
                    >
                        <div className="operations-dashboard-health-detail">
                            <KuzhambuSpace size={8} wrap>
                                <Text>状态</Text>
                                <KuzhambuTag type={statusTone(detailHealth?.healthStatus)}>
                                    {detailHealth?.healthStatus || "-"}
                                </KuzhambuTag>
                            </KuzhambuSpace>
                            <Text>采集来源：{detailHealth?.probeSource || "-"}</Text>
                            <Text>采集目标：{detailHealth?.probeTarget || "-"}</Text>
                            <Text>延迟：{formatLatency(detailHealth?.latencyMs)}</Text>
                            <Text>检查时间：{formatDateTime(detailHealth?.checkedAt)}</Text>
                            <Text>消息：{detailHealth?.message || "-"}</Text>
                            <div className="operations-dashboard-health-related-alerts">
                                <div className="operations-dashboard-health-related-alerts-header">
                                    <Text strong>关联告警</Text>
                                    <KuzhambuButton
                                        testId="operations-dashboard-dashboard-action-button-3"
                                        onClick={() => setAlertDrawerOpen(true)}
                                        size="small"
                                        type="link"
                                    >
                                        查看全部告警
                                    </KuzhambuButton>
                                </div>
                                {selectedHealthAlertInfo.length ? (
                                    selectedHealthAlertInfo.map((alert) => (
                                        <KuzhambuAlert
                                            className="operations-dashboard-health-related-alert"
                                            description={alert.suggestion || "暂无处置建议"}
                                            key={alert.alertId}
                                            title={alert.message || "未返回告警消息"}
                                            showIcon
                                            type={
                                                alert.alertLevel === "CRITICAL"
                                                    ? "error"
                                                    : "warning"
                                            }
                                        />
                                    ))
                                ) : (
                                    <Empty
                                        description="暂无关联告警"
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    />
                                )}
                            </div>
                        </div>
                    </KuzhambuDrawer>
                </div>
            )}
        </KuzhambuPage>
    );
};

const resolveDashboardPermissionCapabilities = (): DashboardPermissionCapabilities => {
    const hasAnyChartPermission =
        hasPermission("classics:content:view") ||
        hasPermission("classics:sancai:view") ||
        hasPermission("classics:wangqi:view") ||
        hasPermission("classics:mingcustoms:view") ||
        hasPermission("discovery:search:view") ||
        hasPermission("discovery:qa:view") ||
        hasPermission("ai:invocation:view") ||
        hasPermission("knowledge:taxonomy:view") ||
        hasPermission("operations:health:view") ||
        hasPermission("operations:task:view");

    return {
        canViewDashboard: hasPermission("operations:dashboard:view"),
        canViewClassicsContentSummary:
            hasPermission("classics:content:view") ||
            hasPermission("classics:sancai:view") ||
            hasPermission("classics:wangqi:view") ||
            hasPermission("classics:mingcustoms:view"),
        canViewDiscoverySearchSummary: hasPermission("discovery:search:view"),
        canViewDiscoveryQaSummary: hasPermission("discovery:qa:view"),
        canViewAiInvocationSummary: hasPermission("ai:invocation:view"),
        canViewKnowledgeTaxonomySummary: hasPermission("knowledge:taxonomy:view"),
        canViewHealthSummary: hasPermission("operations:health:view"),
        canManageHealthAlert: hasPermission("operations:health:manage"),
        canViewTaskSummary: hasPermission("operations:task:view"),
        hasAnyChartPermission
    };
};
