import { Typography } from "antd";
import { KuzhambuButton, KuzhambuSpace, KuzhambuTag } from "@/components";

import type { OperationsHealthAlertRecord } from "./health-types";

const { Text } = Typography;

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

const alertLevelTone = (level?: string | null) => {
    if (level === "CRITICAL") {
        return "danger";
    }
    if (level === "WARNING") {
        return "warning";
    }
    return "neutral";
};

const alertEmptyText = (loading: boolean) => {
    if (loading) {
        return "加载中...";
    }
    return "暂无关联告警";
};

interface HealthAlertTableProps {
    alerts: OperationsHealthAlertRecord[];
    canManageHealth: boolean;
    isAckingAlert: boolean;
    isError: boolean;
    isLoading: boolean;
    isRecoveringAlert: boolean;
    onAckAlert: (alert: OperationsHealthAlertRecord) => void;
    onRecoverAlert: (alert: OperationsHealthAlertRecord) => void;
}

export const HealthAlertTable = ({
    alerts,
    canManageHealth,
    isAckingAlert,
    isError,
    isLoading,
    isRecoveringAlert,
    onAckAlert,
    onRecoverAlert
}: HealthAlertTableProps) => {
    if (isError) {
        return <Text type="danger">关联告警加载失败</Text>;
    }

    if (!alerts.length) {
        return <Text type="secondary">{alertEmptyText(isLoading)}</Text>;
    }

    return (
        <table className="operations-health-alert-table">
            <thead>
                <tr>
                    <th>级别</th>
                    <th>状态</th>
                    <th>消息</th>
                    <th>建议</th>
                    <th>恢复动作</th>
                    <th>最后触发</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                {alerts.map((alert) => (
                    <tr key={alert.alertId}>
                        <td>
                            <KuzhambuTag type={alertLevelTone(alert.alertLevel)}>
                                {alert.alertLevel || "-"}
                            </KuzhambuTag>
                        </td>
                        <td>{alert.alertStatus || "-"}</td>
                        <td>{alert.message || "-"}</td>
                        <td>{alert.suggestion || "-"}</td>
                        <td>{alert.recoveryAction || "-"}</td>
                        <td>{formatDateTime(alert.lastTriggeredAt)}</td>
                        <td>
                            <KuzhambuSpace size={4} wrap>
                                <KuzhambuButton
                                    testId="operations-health-health-action-button"
                                    disabled={!canManageHealth || alert.alertStatus !== "ACTIVE"}
                                    loading={isAckingAlert}
                                    size="small"
                                    onClick={() => onAckAlert(alert)}
                                >
                                    确认
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="operations-health-health-restore-button"
                                    disabled={!canManageHealth || alert.alertStatus === "RECOVERED"}
                                    loading={isRecoveringAlert}
                                    size="small"
                                    type="primary"
                                    onClick={() => onRecoverAlert(alert)}
                                >
                                    恢复
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
};
