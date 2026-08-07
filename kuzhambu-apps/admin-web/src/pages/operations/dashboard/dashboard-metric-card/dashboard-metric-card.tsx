import type { ReactNode } from "react";
import { Statistic, Typography } from "antd";
import { KuzhambuCard } from "@/components";

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
        <KuzhambuCard
            className="operations-dashboard-metric-card"
            styles={{ body: { display: "grid", gap: 10, minHeight: 128 } }}
        >
            <Statistic title={title} value={value} suffix={suffix} prefix={prefix} />
            {secondaryText ? <Text type="secondary">{secondaryText}</Text> : null}
        </KuzhambuCard>
    );
};
