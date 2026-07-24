import { Card, Empty, Typography } from "antd";
import { Link } from "react-router-dom";
import { KuzhambuAlert, KuzhambuButton, KuzhambuDrawer, KuzhambuTag } from "@/components";

import type { OperationsHealthAlertRecord } from "./dashboard-types";

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

interface DashboardAlertDrawerProps {
    alerts: OperationsHealthAlertRecord[];
    canManageHealthAlert: boolean;
    isConfirmingAlert: boolean;
    isRecoveringAlert: boolean;
    open: boolean;
    onClose: () => void;
    onConfirmAlert: (alert: OperationsHealthAlertRecord) => void;
    onRecoverAlert: (alert: OperationsHealthAlertRecord) => void;
}

export const DashboardAlertDrawer = ({
    alerts,
    canManageHealthAlert,
    isConfirmingAlert,
    isRecoveringAlert,
    open,
    onClose,
    onConfirmAlert,
    onRecoverAlert
}: DashboardAlertDrawerProps) => {
    return (
        <KuzhambuDrawer
            testId="operations-dashboard-dashboard-1-drawer"
            open={open}
            onClose={onClose}
            size="small"
            title="健康告警"
        >
            <div className="operations-dashboard-alert-list">
                {alerts.length ? (
                    alerts.map((alert) => (
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
                                    来源：{alert.sourceRefType || "-"} #{alert.sourceRefId || "-"}
                                </Text>
                            </div>
                            <KuzhambuAlert
                                description={alert.suggestion || "暂无处置建议"}
                                title={alert.failureReason || alert.recoveryAction || "处置建议"}
                                showIcon
                                type={alert.alertLevel === "CRITICAL" ? "error" : "warning"}
                            />
                            <div className="operations-dashboard-alert-actions">
                                <KuzhambuButton
                                    testId="operations-dashboard-dashboard-resolve-button"
                                    size="small"
                                >
                                    <Link to={resolveAlertActionPath(alert)}>去处理</Link>
                                </KuzhambuButton>
                                {canManageHealthAlert && alert.alertStatus === "ACTIVE" ? (
                                    <KuzhambuButton
                                        testId="operations-dashboard-dashboard-action-button"
                                        loading={isConfirmingAlert}
                                        onClick={() => onConfirmAlert(alert)}
                                        size="small"
                                    >
                                        确认
                                    </KuzhambuButton>
                                ) : null}
                                {canManageHealthAlert ? (
                                    <KuzhambuButton
                                        testId="operations-dashboard-dashboard-action-button-2"
                                        loading={isRecoveringAlert}
                                        onClick={() => onRecoverAlert(alert)}
                                        size="small"
                                        type="primary"
                                    >
                                        标记恢复
                                    </KuzhambuButton>
                                ) : null}
                            </div>
                        </Card>
                    ))
                ) : (
                    <Empty description="暂无未恢复告警" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                )}
            </div>
        </KuzhambuDrawer>
    );
};
