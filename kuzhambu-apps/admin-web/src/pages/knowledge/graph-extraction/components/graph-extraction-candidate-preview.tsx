import { Card, Descriptions, Empty, Typography } from "antd";

import type { GraphWorkbenchCandidateSummary } from "../graph-workbench-types";

interface GraphExtractionCandidatePreviewProps {
    candidate?: GraphWorkbenchCandidateSummary | null;
    loading?: boolean;
}

const { Paragraph } = Typography;

export const GraphExtractionCandidatePreview = ({
    candidate,
    loading = false
}: GraphExtractionCandidatePreviewProps) => {
    if (!candidate && !loading) {
        return (
            <Card className="graph-extraction-create-card" title="AI 候选" variant="borderless">
                <Empty description="暂无 AI 候选" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            </Card>
        );
    }

    return (
        <Card
            className="graph-extraction-create-card"
            loading={loading}
            title="AI 候选"
            variant="borderless"
        >
            <Descriptions column={1} size="small">
                <Descriptions.Item label="任务 ID">{candidate?.taskId || "-"}</Descriptions.Item>
                <Descriptions.Item label="候选 ID">
                    {candidate?.aiCandidateId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="任务类型">{candidate?.taskType || "-"}</Descriptions.Item>
                <Descriptions.Item label="候选状态">{candidate?.status || "-"}</Descriptions.Item>
            </Descriptions>
            {candidate?.candidatePayloadJson ? (
                <Paragraph className="graph-extraction-candidate-payload" copyable>
                    {candidate.candidatePayloadJson}
                </Paragraph>
            ) : null}
        </Card>
    );
};
