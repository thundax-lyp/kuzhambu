import { Col, Row, Statistic } from "antd";
import type { RefinementProgressSummary } from "@/pages/knowledge/refinement/refinement-types";
import { KuzhambuCard } from "@/components";

interface RefinementProgressSummaryProps {
    summary?: RefinementProgressSummary | null;
}

export const RefinementProgressSummaryPanel = ({ summary }: RefinementProgressSummaryProps) => {
    return (
        <Row gutter={[12, 12]}>
            <Col xs={12} md={6}>
                <KuzhambuCard size="small">
                    <Statistic title="实体待确认" value={summary?.entityPendingCount ?? 0} />
                </KuzhambuCard>
            </Col>
            <Col xs={12} md={6}>
                <KuzhambuCard size="small">
                    <Statistic title="实体已确认" value={summary?.entityConfirmedCount ?? 0} />
                </KuzhambuCard>
            </Col>
            <Col xs={12} md={6}>
                <KuzhambuCard size="small">
                    <Statistic title="关系待确认" value={summary?.relationPendingCount ?? 0} />
                </KuzhambuCard>
            </Col>
            <Col xs={12} md={6}>
                <KuzhambuCard size="small">
                    <Statistic title="关系已确认" value={summary?.relationConfirmedCount ?? 0} />
                </KuzhambuCard>
            </Col>
        </Row>
    );
};
