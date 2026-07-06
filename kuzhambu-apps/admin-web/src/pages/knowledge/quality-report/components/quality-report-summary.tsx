import { Card, Col, Row, Statistic } from "antd";
import type { QualityReportRecord } from "../quality-report-types";

interface QualityReportSummaryProps {
    report?: QualityReportRecord | null;
}

const formatRate = (value?: number | null) => `${((value || 0) * 100).toFixed(0)}%`;

export const QualityReportSummary = ({ report = null }: QualityReportSummaryProps) => {
    return (
        <Row gutter={[16, 16]}>
            <Col xs={24} sm={12} lg={6}>
                <Card className="knowledge-quality-report-stat-card">
                    <Statistic title="实体覆盖率" value={formatRate(report?.entityCoverageRate)} />
                </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
                <Card className="knowledge-quality-report-stat-card">
                    <Statistic
                        title="关系准确率"
                        value={formatRate(report?.relationAccuracyRate)}
                    />
                </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
                <Card className="knowledge-quality-report-stat-card">
                    <Statistic title="世系覆盖率" value={formatRate(report?.lineageCoverageRate)} />
                </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
                <Card className="knowledge-quality-report-stat-card">
                    <Statistic title="完整度" value={formatRate(report?.completenessRate)} />
                </Card>
            </Col>
        </Row>
    );
};
