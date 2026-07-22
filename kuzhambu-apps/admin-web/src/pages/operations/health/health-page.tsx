import { ReloadOutlined, SearchOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, DatePicker, Descriptions, Input, Select, Tooltip, Typography } from "antd";
import { useEffect, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { HealthAlertTable } from "./health-alert-table";
import * as service from "./health-service";
import type { OperationsHealthPageQuery } from "./health-service";
import type {
    OperationsHealthAlertRecord,
    OperationsHealthRecord,
    OperationsHealthStatus
} from "./health-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./health-page.css";

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Paragraph, Text } = Typography;

type CheckedAtRange = [DateLike | null, DateLike | null] | null;

interface DateLike {
    toISOString: () => string;
}

const HEALTH_STATUS_OPTIONS: Array<OperationsHealthStatus | "ALL"> = [
    "ALL",
    "UP",
    "DEGRADED",
    "DOWN"
];
const PROBE_SOURCE_OPTIONS = ["ALL", "LOCAL", "HTTP"];

const trimToNull = (value: string) => {
    const trimmed = value.trim();
    return trimmed || null;
};

const optionToNull = (value: string) => {
    return value === "ALL" ? null : value;
};

const rangeValueToString = (value?: DateLike | null) => {
    return value?.toISOString() || null;
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

const statusTone = (status?: OperationsHealthStatus | null) => {
    if (status === "UP") {
        return "success";
    }
    if (status === "DEGRADED") {
        return "warning";
    }
    if (status === "DOWN") {
        return "danger";
    }
    return "neutral";
};

const formatDetailsJson = (value?: string | null) => {
    if (!value) {
        return null;
    }
    try {
        return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
        return value;
    }
};

const buildQuery = (
    componentKeyword: string,
    healthStatus: string,
    probeSource: string,
    probeTargetKeyword: string,
    checkedAtRange: CheckedAtRange,
    pageNo: number,
    pageSize: number
): OperationsHealthPageQuery => ({
    component: trimToNull(componentKeyword),
    healthStatus: optionToNull(healthStatus) as OperationsHealthStatus | null,
    probeSource: optionToNull(probeSource),
    probeTarget: trimToNull(probeTargetKeyword),
    checkedAtStart: rangeValueToString(checkedAtRange?.[0]),
    checkedAtEnd: rangeValueToString(checkedAtRange?.[1]),
    pageNo,
    pageSize
});

export const OperationsHealthPage = () => {
    const { message } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canViewHealth = hasPermission("operations:health:view");
    const canManageHealth = hasPermission("operations:health:manage");
    const [componentKeyword, setComponentKeyword] = useState("");
    const [healthStatus, setHealthStatus] = useState("ALL");
    const [probeSource, setProbeSource] = useState("ALL");
    const [probeTargetKeyword, setProbeTargetKeyword] = useState("");
    const [checkedAtRange, setCheckedAtRange] = useState<CheckedAtRange>(null);
    const [pageNo, setPageNo] = useState(DEFAULT_PAGE_NO);
    const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
    const [detailHealth, setDetailHealth] = useState<OperationsHealthRecord | null>(null);
    const [alertCheckId, setAlertCheckId] = useState<number | null>(null);
    const [submittedQuery, setSubmittedQuery] = useState<OperationsHealthPageQuery>(() =>
        buildQuery("", "ALL", "ALL", "", null, DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE)
    );

    const healthPageQuery = useQuery({
        queryKey: ["operations", "health", "page", submittedQuery],
        queryFn: () => service.getOperationsHealthPage(submittedQuery),
        enabled: canViewHealth,
        retry: false
    });

    const alertPageQuery = useQuery({
        queryKey: ["operations", "health", "alerts", alertCheckId],
        queryFn: () =>
            service.getOperationsHealthAlerts({
                latestCheckId: alertCheckId,
                pageNo: 1,
                pageSize: 10
            }),
        enabled: alertCheckId !== null,
        retry: false
    });

    const refreshAlerts = async () => {
        await queryClient.invalidateQueries({ queryKey: ["operations", "health", "alerts"] });
    };

    const ackAlertMutation = useMutation({
        mutationFn: service.confirmOperationsHealthAlert,
        onSuccess: async () => {
            await refreshAlerts();
            message.success("告警已确认");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "告警确认失败");
        }
    });

    const recoverAlertMutation = useMutation({
        mutationFn: service.recoverOperationsHealthAlert,
        onSuccess: async () => {
            await Promise.all([
                refreshAlerts(),
                queryClient.invalidateQueries({ queryKey: ["operations", "task", "page"] }),
                queryClient.invalidateQueries({ queryKey: ["operations", "restore", "page"] })
            ]);
            message.success("恢复动作已完成");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "恢复动作失败");
        }
    });

    useEffect(() => {
        if (healthPageQuery.isError) {
            const error = healthPageQuery.error;
            message.error(error instanceof Error ? error.message : "健康记录加载失败");
        }
    }, [healthPageQuery.error, healthPageQuery.isError, message]);

    useEffect(() => {
        if (alertPageQuery.isError) {
            const error = alertPageQuery.error;
            message.error(error instanceof Error ? error.message : "关联告警加载失败");
        }
    }, [alertPageQuery.error, alertPageQuery.isError, message]);

    const submitQuery = (nextPageNo = DEFAULT_PAGE_NO, nextPageSize = pageSize) => {
        setPageNo(nextPageNo);
        setPageSize(nextPageSize);
        setSubmittedQuery(
            buildQuery(
                componentKeyword,
                healthStatus,
                probeSource,
                probeTargetKeyword,
                checkedAtRange,
                nextPageNo,
                nextPageSize
            )
        );
    };

    const resetQuery = () => {
        setComponentKeyword("");
        setHealthStatus("ALL");
        setProbeSource("ALL");
        setProbeTargetKeyword("");
        setCheckedAtRange(null);
        setPageNo(DEFAULT_PAGE_NO);
        setPageSize(DEFAULT_PAGE_SIZE);
        setSubmittedQuery(
            buildQuery("", "ALL", "ALL", "", null, DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE)
        );
    };

    const refreshQuery = () => {
        setSubmittedQuery(
            buildQuery(
                componentKeyword,
                healthStatus,
                probeSource,
                probeTargetKeyword,
                checkedAtRange,
                pageNo,
                pageSize
            )
        );
        void healthPageQuery.refetch();
    };

    const healthPage = healthPageQuery.data;
    const records: OperationsHealthRecord[] = healthPage?.records || [];
    const totalCount = healthPage?.count ?? 0;
    const totalPage = Math.max(1, Math.ceil(totalCount / pageSize));
    const detailsText = formatDetailsJson(detailHealth?.detailsJson);
    const alerts: OperationsHealthAlertRecord[] = alertPageQuery.data?.records || [];

    if (!canViewHealth) {
        return (
            <KuzhambuPage
                className="health-page operations-health-page"
                title="健康检查"
                description="查看组件健康检查、探针来源和运行状态。"
            >
                <Card size="small">缺少 operations:health:view 权限。</Card>
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuPage
            className="health-page operations-health-page"
            title="健康检查"
            description="查看组件健康检查、探针来源和运行状态。"
        >
            <Card className="operations-health-section-card" title="健康记录" size="small">
                <KuzhambuSpace className="operations-health-toolbar" size={8} wrap>
                    <Input
                        aria-label="组件"
                        placeholder="组件"
                        value={componentKeyword}
                        onChange={(event) => setComponentKeyword(event.target.value)}
                        onPressEnter={() => submitQuery()}
                        allowClear
                    />
                    <Select
                        aria-label="健康状态"
                        value={healthStatus}
                        onChange={setHealthStatus}
                        className="operations-health-select"
                    >
                        {HEALTH_STATUS_OPTIONS.map((status) => (
                            <Option key={status} value={status}>
                                {status === "ALL" ? "全部" : status}
                            </Option>
                        ))}
                    </Select>
                    <Select
                        aria-label="探针来源"
                        value={probeSource}
                        onChange={setProbeSource}
                        className="operations-health-select"
                    >
                        {PROBE_SOURCE_OPTIONS.map((source) => (
                            <Option key={source} value={source}>
                                {source === "ALL" ? "全部" : source}
                            </Option>
                        ))}
                    </Select>
                    <Input
                        aria-label="探针目标"
                        placeholder="探针目标"
                        value={probeTargetKeyword}
                        onChange={(event) => setProbeTargetKeyword(event.target.value)}
                        onPressEnter={() => submitQuery()}
                        allowClear
                    />
                    <RangePicker
                        aria-label="检查时间"
                        value={checkedAtRange as never}
                        onChange={(range) => setCheckedAtRange(range as CheckedAtRange)}
                        allowEmpty={[true, true]}
                        showTime
                    />
                    <KuzhambuButton
                        testId="operations-health-health-query-button"
                        icon={<SearchOutlined />}
                        type="primary"
                        onClick={() => submitQuery()}
                    >
                        查询
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="operations-health-health-reset-button"
                        onClick={resetQuery}
                    >
                        重置
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="operations-health-health-refresh-button"
                        icon={<ReloadOutlined />}
                        onClick={refreshQuery}
                    >
                        刷新
                    </KuzhambuButton>
                </KuzhambuSpace>

                <table className="operations-health-table">
                    <thead>
                        <tr>
                            <th>组件</th>
                            <th>健康状态</th>
                            <th>探针来源</th>
                            <th>探针目标</th>
                            <th>耗时</th>
                            <th>消息</th>
                            <th>检查时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        {records.length ? (
                            records.map((record) => (
                                <tr key={record.checkId}>
                                    <td>{record.component || "-"}</td>
                                    <td>
                                        <KuzhambuTag type={statusTone(record.healthStatus)}>
                                            {record.healthStatus || "-"}
                                        </KuzhambuTag>
                                    </td>
                                    <td>{record.probeSource || "-"}</td>
                                    <td className="operations-health-url-cell">
                                        <Tooltip title={record.probeTarget || "-"}>
                                            <span>{record.probeTarget || "-"}</span>
                                        </Tooltip>
                                    </td>
                                    <td>
                                        {record.latencyMs == null ? "-" : `${record.latencyMs} ms`}
                                    </td>
                                    <td className="operations-health-message-cell">
                                        <Tooltip title={record.message || "-"}>
                                            <span>{record.message || "-"}</span>
                                        </Tooltip>
                                    </td>
                                    <td>{formatDateTime(record.checkedAt)}</td>
                                    <td>
                                        <KuzhambuSpace size={4} wrap>
                                            <KuzhambuButton
                                                testId="operations-health-health-detail-button"
                                                size="small"
                                                type="link"
                                                onClick={() => setDetailHealth(record)}
                                            >
                                                详情
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                testId="operations-health-health-view-alerts-button"
                                                size="small"
                                                type="link"
                                                onClick={() => setAlertCheckId(record.checkId)}
                                            >
                                                查看告警
                                            </KuzhambuButton>
                                        </KuzhambuSpace>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td className="operations-health-empty-cell" colSpan={8}>
                                    {healthPageQuery.isLoading ? "加载中..." : "暂无健康记录"}
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>

                <div className="operations-health-pagination">
                    <KuzhambuButton
                        testId="operations-health-health-previous-page-button"
                        disabled={pageNo <= 1}
                        onClick={() => submitQuery(pageNo - 1, pageSize)}
                    >
                        上一页
                    </KuzhambuButton>
                    <Text>
                        第 {pageNo} / {totalPage} 页，共 {totalCount} 条
                    </Text>
                    <KuzhambuButton
                        testId="operations-health-health-next-page-button"
                        disabled={pageNo >= totalPage}
                        onClick={() => submitQuery(pageNo + 1, pageSize)}
                    >
                        下一页
                    </KuzhambuButton>
                    <KuzhambuSpace size={8}>
                        <Text>每页</Text>
                        <Select
                            aria-label="每页条数"
                            value={pageSize}
                            className="operations-health-page-size"
                            onChange={(size) => submitQuery(DEFAULT_PAGE_NO, size)}
                        >
                            {[10, 20, 50].map((size) => (
                                <Option value={size} key={size}>
                                    {size}
                                </Option>
                            ))}
                        </Select>
                        <Text>条</Text>
                    </KuzhambuSpace>
                </div>
            </Card>
            <KuzhambuDrawer
                testId="operations-health-health-1-drawer"
                open={detailHealth !== null}
                size="middle"
                title={detailHealth ? `健康详情 #${detailHealth.checkId}` : "健康详情"}
                onClose={() => setDetailHealth(null)}
            >
                <div className="operations-health-detail">
                    <Descriptions size="small" column={1} bordered>
                        <Descriptions.Item label="检查 ID">
                            {detailHealth?.checkId || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="组件">
                            {detailHealth?.component || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="健康状态">
                            <KuzhambuTag type={statusTone(detailHealth?.healthStatus)}>
                                {detailHealth?.healthStatus || "-"}
                            </KuzhambuTag>
                        </Descriptions.Item>
                        <Descriptions.Item label="耗时">
                            {detailHealth?.latencyMs == null ? "-" : `${detailHealth.latencyMs} ms`}
                        </Descriptions.Item>
                        <Descriptions.Item label="探针来源">
                            {detailHealth?.probeSource || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="探针目标">
                            {detailHealth?.probeTarget || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="检查时间">
                            {formatDateTime(detailHealth?.checkedAt)}
                        </Descriptions.Item>
                    </Descriptions>
                    <div>
                        <Text strong>消息</Text>
                        <Paragraph>{detailHealth?.message || "-"}</Paragraph>
                    </div>
                    <div>
                        <Text strong>诊断 JSON</Text>
                        {detailsText ? (
                            <pre className="operations-health-details-json">{detailsText}</pre>
                        ) : (
                            <Text type="secondary">暂无诊断详情</Text>
                        )}
                    </div>
                </div>
            </KuzhambuDrawer>
            <KuzhambuDrawer
                testId="operations-health-health-2-drawer"
                open={alertCheckId !== null}
                size="middle"
                title={alertCheckId ? `关联告警 #${alertCheckId}` : "关联告警"}
                onClose={() => setAlertCheckId(null)}
            >
                <div className="operations-health-alerts">
                    <HealthAlertTable
                        alerts={alerts}
                        canManageHealth={canManageHealth}
                        isAckingAlert={ackAlertMutation.isPending}
                        isError={alertPageQuery.isError}
                        isLoading={alertPageQuery.isLoading}
                        isRecoveringAlert={recoverAlertMutation.isPending}
                        onAckAlert={(alert) => ackAlertMutation.mutate({ alertId: alert.alertId })}
                        onRecoverAlert={(alert) =>
                            confirm.danger({
                                title: "执行告警恢复",
                                message: `确认处理告警 #${alert.alertId} 吗？`,
                                description: alert.recoveryAction?.startsWith("RUN_")
                                    ? "该操作会触发自动化恢复动作，并回写告警与任务台账。"
                                    : "该操作会将告警标记为已恢复。",
                                okText: "执行恢复",
                                onConfirm: () =>
                                    recoverAlertMutation.mutateAsync({
                                        alertId: alert.alertId
                                    })
                            })
                        }
                    />
                </div>
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
