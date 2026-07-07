import {
    AppstoreOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    DatabaseOutlined,
    HeartOutlined,
    ReloadOutlined,
    SearchOutlined,
    WarningOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Card, Empty, Segmented, Spin, Statistic, Typography } from "antd";
import { useState } from "react";
import { Link } from "react-router-dom";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import * as service from "./dashboard-service";
import type { OperationsDashboardOverviewQuery } from "./dashboard-service";
import type {
    OperationsDashboardPeriodType,
    OperationsHealthAlertRecord,
    OperationsHealthSummaryRecord
} from "./dashboard-types";
import "./dashboard-page.css";

const { Text, Title } = Typography;

const periodOptions: Array<{ label: string; value: OperationsDashboardPeriodType }> = [
    { label: "近 7 天", value: "WEEK" },
    { label: "近 30 天", value: "MONTH" }
];

const operationEntries = [
    {
        description: "查看所有长任务、筛选执行状态并打开任务详情",
        icon: <ClockCircleOutlined />,
        title: "任务台账",
        to: "/operations/tasks"
    },
    {
        description: "查看备份、恢复记录并发起手动备份",
        icon: <DatabaseOutlined />,
        title: "备份恢复",
        to: "/operations/backup-restore"
    },
    {
        description: "查看清理任务、失败项并触发维护清理",
        icon: <AppstoreOutlined />,
        title: "清理维护",
        to: "/operations/cleanup"
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

const alertLevelTone = (level?: string | null) => {
    if (level === "CRITICAL") {
        return "danger";
    }
    if (level === "WARNING") {
        return "warning";
    }
    return "neutral";
};

const formatAlertStatus = (status?: string | null) => {
    if (status === "ACTIVE") {
        return "未确认";
    }
    if (status === "ACKED") {
        return "已确认";
    }
    if (status === "RECOVERED") {
        return "已恢复";
    }
    return status || "-";
};

const resolveAlertActionPath = (alert: OperationsHealthAlertRecord) => {
    const sourceType = (alert.sourceRefType || "").toUpperCase();
    const target = (alert.recoveryTarget || "").toLowerCase();
    if (sourceType.includes("TASK") || target.includes("task")) {
        return "/operations/tasks";
    }
    if (
        sourceType.includes("BACKUP") ||
        sourceType.includes("RESTORE") ||
        target.includes("backup")
    ) {
        return "/operations/backup-restore";
    }
    if (sourceType.includes("CLEANUP") || target.includes("cleanup")) {
        return "/operations/cleanup";
    }
    return "/operations/dashboard";
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

const TrendPanel = ({ items, title }: TrendPanelProps) => {
    const records = items || [];
    const maxValue = Math.max(...records.map((record) => normalizeNumber(record.count)), 0);

    return (
        <Card className="operations-dashboard-section-card" size="small" title={title}>
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
        </Card>
    );
};

interface RankingListProps {
    emptyText: string;
    items: Array<{ label: string; meta: string; value: number }>;
    title: string;
}

const RankingList = ({ emptyText, items, title }: RankingListProps) => {
    return (
        <Card className="operations-dashboard-section-card" size="small" title={title}>
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
        </Card>
    );
};

export const OperationsDashboardPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canViewDashboard = hasPermission("operations:dashboard:view");
    const canManageHealthAlert = hasPermission("operations:health:manage");
    const [periodType, setPeriodType] = useState<OperationsDashboardPeriodType>("WEEK");
    const [alertDrawerOpen, setAlertDrawerOpen] = useState(false);
    const [selectedHealth, setSelectedHealth] = useState<OperationsHealthSummaryRecord | null>(
        null
    );

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
        enabled: canViewDashboard,
        retry: false
    });

    const alertQuery = useQuery({
        queryKey: ["operations", "health", "alerts", "dashboard"],
        queryFn: () =>
            service.getHealthAlerts({
                pageNo: 1,
                pageSize: 20
            }),
        enabled: canViewDashboard,
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
        await queryClient.invalidateQueries({ queryKey: ["operations", "health", "trend"] });
        await queryClient.invalidateQueries({ queryKey: ["operations", "health", "alerts"] });
    };

    const overview = dashboardQuery.data;
    const healthSummaries = overview?.healthSummaries || [];
    const healthTrend = trendQuery.data || [];
    const healthAlerts = alertQuery.data?.records || [];
    const openHealthAlerts = healthAlerts.filter((alert) => alert.alertStatus !== "RECOVERED");
    const latestOpenAlert = overview?.latestAlert || openHealthAlerts[0] || null;
    const unhealthyCount = normalizeNumber(overview?.unhealthyComponentCount);
    const failedTaskCount = normalizeNumber(overview?.failedTaskCount);
    const runningTaskCount = normalizeNumber(overview?.runningTaskCount);
    const activeAlertCount = normalizeNumber(overview?.activeAlertCount || openHealthAlerts.length);
    const criticalAlertCount = normalizeNumber(
        overview?.criticalAlertCount ||
            openHealthAlerts.filter((alert) => alert.alertLevel === "CRITICAL").length
    );
    const isLoading = dashboardQuery.isLoading || trendQuery.isLoading || alertQuery.isLoading;

    const topContents =
        overview?.topContents?.map((item) => ({
            label: item.title || `内容 #${item.contentId || "-"}`,
            meta: item.contentType || "内容",
            value: normalizeNumber(item.visitCount)
        })) || [];
    const topQueries =
        overview?.topQueries?.map((item) => ({
            label: item.queryText || "-",
            meta: "搜索词",
            value: normalizeNumber(item.count)
        })) || [];
    const topTags =
        overview?.topTags?.map((item) => ({
            label: item.tagName || "-",
            meta: "标签覆盖",
            value: normalizeNumber(item.contentRefCount)
        })) || [];
    const topAiCapabilities =
        overview?.topAiCapabilities?.map((item) => ({
            label: item.capability || "-",
            meta: "AI 能力",
            value: normalizeNumber(item.invocationCount)
        })) || [];

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
                    <Button
                        icon={<ReloadOutlined />}
                        onClick={() => void refreshDashboard()}
                        disabled={!canViewDashboard}
                    >
                        刷新
                    </Button>
                </KuzhambuSpace>
            }
            className="dashboard-page operations-dashboard-page"
            description="集中查看内容、发现、健康巡检和长任务状态，作为运维处理入口。"
            eyebrow="Operations"
            title="运营看板"
        >
            {!canViewDashboard ? (
                <Empty description="缺少 operations:dashboard:view 权限" />
            ) : (
                <div className="operations-dashboard-content">
                    {isLoading && !overview ? <Spin size="large" /> : null}

                    {openHealthAlerts.length || activeAlertCount ? (
                        <Alert
                            action={
                                <Button size="small" onClick={() => setAlertDrawerOpen(true)}>
                                    查看告警
                                </Button>
                            }
                            className="operations-dashboard-alert-banner"
                            description={
                                latestOpenAlert
                                    ? `最新：${latestOpenAlert.message || latestOpenAlert.component || "-"}`
                                    : undefined
                            }
                            message={`健康告警 ${formatNumber(activeAlertCount)} 个，严重 ${formatNumber(criticalAlertCount)} 个`}
                            showIcon
                            type={criticalAlertCount > 0 ? "error" : "warning"}
                        />
                    ) : null}

                    <section className="operations-dashboard-metrics" aria-label="核心指标">
                        <Card className="operations-dashboard-metric-card">
                            <Statistic
                                title="内容总量"
                                value={formatNumber(overview?.contentCount)}
                                prefix={<DatabaseOutlined />}
                            />
                            <Text type="secondary">
                                译文 {formatNumber(overview?.translatedContentCount)}，图像就绪{" "}
                                {formatNumber(overview?.imageReadyContentCount)}，分享访问{" "}
                                {formatNumber(overview?.shareVisitCount)}
                            </Text>
                        </Card>
                        <Card className="operations-dashboard-metric-card">
                            <Statistic
                                title="搜索 / 问答"
                                value={`${formatNumber(overview?.searchCount)} / ${formatNumber(overview?.qaCount)}`}
                                prefix={<SearchOutlined />}
                            />
                            <Text type="secondary">
                                平均搜索延迟 {formatLatency(overview?.avgSearchLatencyMs)}
                            </Text>
                        </Card>
                        <Card className="operations-dashboard-metric-card">
                            <Statistic
                                title="AI 调用成功率"
                                value={formatNumber(overview?.aiSucceededInvocationCount)}
                                suffix={`/ ${formatNumber(overview?.aiInvocationCount)}`}
                                prefix={<CheckCircleOutlined />}
                            />
                            <Text type="secondary">
                                失败 {formatNumber(overview?.aiFailedInvocationCount)}，平均延迟{" "}
                                {formatLatency(overview?.aiAvgLatencyMs)}，成本{" "}
                                {formatNumber(overview?.aiTotalCostAmount)}
                            </Text>
                        </Card>
                        <Card className="operations-dashboard-metric-card">
                            <Statistic
                                title="异常组件 / 失败任务"
                                value={`${formatNumber(unhealthyCount)} / ${formatNumber(failedTaskCount)}`}
                                prefix={<WarningOutlined />}
                            />
                            <Text type="secondary">
                                运行中任务 {formatNumber(runningTaskCount)}
                            </Text>
                        </Card>
                    </section>

                    <section className="operations-dashboard-grid operations-dashboard-grid-two">
                        <TrendPanel items={overview?.contentGrowthSeries} title="内容增长趋势" />
                        <TrendPanel items={overview?.searchTrendSeries} title="搜索趋势" />
                    </section>

                    <section className="operations-dashboard-grid operations-dashboard-grid-two">
                        <Card
                            className="operations-dashboard-section-card"
                            size="small"
                            title="健康巡检"
                        >
                            {healthSummaries.length ? (
                                <div className="operations-dashboard-health-list">
                                    {healthSummaries.map((summary) => (
                                        <button
                                            className="operations-dashboard-health-item"
                                            key={summary.checkId}
                                            onClick={() => setSelectedHealth(summary)}
                                            type="button"
                                        >
                                            <span>
                                                <HeartOutlined />
                                                <Text strong>
                                                    {summary.component || "未知组件"}
                                                </Text>
                                            </span>
                                            <KuzhambuTag type={statusTone(summary.healthStatus)}>
                                                {summary.healthStatus || "UNKNOWN"}
                                            </KuzhambuTag>
                                            <Text type="secondary">
                                                {formatLatency(summary.latencyMs)}
                                            </Text>
                                        </button>
                                    ))}
                                </div>
                            ) : (
                                <Empty
                                    description="暂无健康摘要"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                />
                            )}
                        </Card>
                        <Card
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
                                                    UP {formatNumber(bucket.upCount)}
                                                </KuzhambuTag>
                                                <KuzhambuTag type="warning">
                                                    DEGRADED {formatNumber(bucket.degradedCount)}
                                                </KuzhambuTag>
                                                <KuzhambuTag type="danger">
                                                    DOWN {formatNumber(bucket.downCount)}
                                                </KuzhambuTag>
                                                <Text type="secondary">
                                                    平均 {formatLatency(bucket.avgLatencyMs)}
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
                        </Card>
                    </section>

                    <section className="operations-dashboard-grid operations-dashboard-grid-three">
                        <RankingList
                            emptyText="暂无内容排行"
                            items={topContents}
                            title="热门内容"
                        />
                        <RankingList emptyText="暂无查询排行" items={topQueries} title="热门搜索" />
                        <RankingList emptyText="暂无标签排行" items={topTags} title="标签覆盖" />
                        <RankingList
                            emptyText="暂无 AI 能力排行"
                            items={topAiCapabilities}
                            title="AI 能力"
                        />
                    </section>

                    <section>
                        <Title level={5}>运维入口</Title>
                        <div className="operations-dashboard-entry-grid">
                            {operationEntries.map((entry) => (
                                <Link
                                    className="operations-dashboard-entry"
                                    key={entry.to}
                                    to={entry.to}
                                >
                                    <Card size="small">
                                        <KuzhambuSpace size={8}>
                                            {entry.icon}
                                            <Text strong>{entry.title}</Text>
                                        </KuzhambuSpace>
                                        <Text type="secondary">{entry.description}</Text>
                                    </Card>
                                </Link>
                            ))}
                        </div>
                    </section>

                    <KuzhambuDrawer
                        open={alertDrawerOpen}
                        onClose={() => setAlertDrawerOpen(false)}
                        size="small"
                        title="健康告警"
                    >
                        <div className="operations-dashboard-alert-list">
                            {openHealthAlerts.length ? (
                                openHealthAlerts.map((alert) => (
                                    <Card
                                        className="operations-dashboard-alert-card"
                                        key={alert.alertId}
                                        size="small"
                                    >
                                        <div className="operations-dashboard-alert-card-header">
                                            <div>
                                                <Text strong>{alert.component || "未知组件"}</Text>
                                                <Text type="secondary">
                                                    {alert.message || "未返回告警消息"}
                                                </Text>
                                            </div>
                                            <KuzhambuTag type={alertLevelTone(alert.alertLevel)}>
                                                {alert.alertLevel || "UNKNOWN"}
                                            </KuzhambuTag>
                                        </div>
                                        <div className="operations-dashboard-alert-meta">
                                            <Text type="secondary">
                                                状态：{formatAlertStatus(alert.alertStatus)}
                                            </Text>
                                            <Text type="secondary">
                                                最近触发：{formatDateTime(alert.lastTriggeredAt)}
                                            </Text>
                                            <Text type="secondary">
                                                来源：{alert.sourceRefType || "-"} #
                                                {alert.sourceRefId || "-"}
                                            </Text>
                                        </div>
                                        <Alert
                                            description={alert.suggestion || "暂无处置建议"}
                                            message={
                                                alert.failureReason ||
                                                alert.recoveryAction ||
                                                "处置建议"
                                            }
                                            showIcon
                                            type={
                                                alert.alertLevel === "CRITICAL"
                                                    ? "error"
                                                    : "warning"
                                            }
                                        />
                                        <div className="operations-dashboard-alert-actions">
                                            <Button size="small">
                                                <Link to={resolveAlertActionPath(alert)}>
                                                    去处理
                                                </Link>
                                            </Button>
                                            {canManageHealthAlert &&
                                            alert.alertStatus === "ACTIVE" ? (
                                                <Button
                                                    loading={confirmAlertMutation.isPending}
                                                    onClick={() =>
                                                        confirmAlertMutation.mutate({
                                                            alertId: alert.alertId
                                                        })
                                                    }
                                                    size="small"
                                                >
                                                    确认
                                                </Button>
                                            ) : null}
                                            {canManageHealthAlert ? (
                                                <Button
                                                    loading={recoverAlertMutation.isPending}
                                                    onClick={() =>
                                                        recoverAlertMutation.mutate({
                                                            alertId: alert.alertId
                                                        })
                                                    }
                                                    size="small"
                                                    type="primary"
                                                >
                                                    标记恢复
                                                </Button>
                                            ) : null}
                                        </div>
                                    </Card>
                                ))
                            ) : (
                                <Empty
                                    description="暂无未恢复告警"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                />
                            )}
                        </div>
                    </KuzhambuDrawer>

                    <KuzhambuDrawer
                        open={selectedHealth !== null}
                        onClose={() => setSelectedHealth(null)}
                        size="middle"
                        title={
                            selectedHealth
                                ? `${selectedHealth.component || "未知组件"} 健康明细`
                                : "健康明细"
                        }
                    >
                        <div className="operations-dashboard-health-detail">
                            <KuzhambuSpace size={8} wrap>
                                <Text>状态</Text>
                                <KuzhambuTag type={statusTone(selectedHealth?.healthStatus)}>
                                    {selectedHealth?.healthStatus || "-"}
                                </KuzhambuTag>
                            </KuzhambuSpace>
                            <Text>采集来源：{selectedHealth?.probeSource || "-"}</Text>
                            <Text>采集目标：{selectedHealth?.probeTarget || "-"}</Text>
                            <Text>延迟：{formatLatency(selectedHealth?.latencyMs)}</Text>
                            <Text>检查时间：{formatDateTime(selectedHealth?.checkedAt)}</Text>
                            <Text>消息：{selectedHealth?.message || "-"}</Text>
                        </div>
                    </KuzhambuDrawer>
                </div>
            )}
        </KuzhambuPage>
    );
};
