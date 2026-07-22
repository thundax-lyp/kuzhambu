import type { ReactNode } from "react";
import { Card, Statistic, Typography } from "antd";

const { Text } = Typography;

interface DashboardMetricCardProps {
    prefix: ReactNode;
    title: string;
    value: string | number;
    secondaryText?: string;
    suffix?: ReactNode;
}

export const DashboardMetricCard = ({
    prefix,
    title,
    value,
    secondaryText,
    suffix
}: DashboardMetricCardProps) => {
    return (
        <Card className="operations-dashboard-metric-card">
            <Statistic title={title} value={value} suffix={suffix} prefix={prefix} />
            {secondaryText ? <Text type="secondary">{secondaryText}</Text> : null}
        </Card>
    );
};
