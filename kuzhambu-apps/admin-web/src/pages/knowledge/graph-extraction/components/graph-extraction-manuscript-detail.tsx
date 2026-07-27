import { Card, Descriptions, Empty, Tag, Typography } from "antd";
import { KuzhambuButton, KuzhambuSpace } from "@/components";

import type {
    GraphExtractionTaskType,
    GraphWorkbenchCandidateRecord,
    GraphWorkbenchManuscriptNode,
    GraphWorkbenchManuscriptRecord,
    GraphWorkbenchStatus
} from "../graph-extraction-types";
import { GraphExtractionCandidatePreview } from "./graph-extraction-candidate-preview";

interface GraphExtractionManuscriptDetailProps {
    applying?: boolean;
    canApply?: boolean;
    canEdit?: boolean;
    candidate?: GraphWorkbenchCandidateRecord | null;
    candidateLoading?: boolean;
    detail?: GraphWorkbenchManuscriptRecord | null;
    extracting?: boolean;
    selectedNode?: GraphWorkbenchManuscriptNode | null;
    onApplyCandidate: (taskId: number) => void;
    onExtract: (taskType: GraphExtractionTaskType) => void;
}

const { Text } = Typography;

const STATUS_LABELS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "未抽取"],
    ["EXTRACTING", "抽取中"],
    ["EXTRACTION_FAILED", "抽取失败"],
    ["CANDIDATE_READY", "待应用"],
    ["APPLIED", "待精修"],
    ["REFINING", "精修中"],
    ["REFINED", "已精修"],
    ["QUALITY_ISSUE", "质量异常"]
]);

const STATUS_COLORS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "default"],
    ["EXTRACTING", "processing"],
    ["EXTRACTION_FAILED", "error"],
    ["CANDIDATE_READY", "warning"],
    ["APPLIED", "blue"],
    ["REFINING", "purple"],
    ["REFINED", "success"],
    ["QUALITY_ISSUE", "error"]
]);

const statusLabel = (status?: GraphWorkbenchStatus | null) =>
    STATUS_LABELS.get(status || "") || status || "未知";

const statusColor = (status?: GraphWorkbenchStatus | null) =>
    STATUS_COLORS.get(status || "") || "default";

const hasCandidate = (candidate?: GraphWorkbenchCandidateRecord | null) =>
    Boolean(candidate?.taskId && candidate.aiCandidateId);

export const GraphExtractionManuscriptDetail = ({
    applying = false,
    canApply = false,
    canEdit = false,
    candidate,
    candidateLoading = false,
    detail,
    extracting = false,
    selectedNode,
    onApplyCandidate,
    onExtract
}: GraphExtractionManuscriptDetailProps) => {
    if (!selectedNode) {
        return (
            <Card className="graph-extraction-create-card" variant="borderless">
                <Empty
                    description="请选择一篇稿件查看图谱状态"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            </Card>
        );
    }

    const latestVersionId = detail?.latestGraphVersion?.versionId;
    const candidateTaskId = candidate?.taskId || detail?.latestExtractionTask?.taskId;
    const numericCandidateTaskId = Number(candidateTaskId);
    const canApplyCandidate =
        Number.isFinite(numericCandidateTaskId) &&
        (detail?.graphStatus === "CANDIDATE_READY" || candidate?.status === "SUCCEEDED");

    return (
        <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
            <Card
                className="graph-extraction-create-card"
                title={detail?.title || selectedNode.title || "稿件图谱详情"}
                variant="borderless"
                extra={
                    <Tag color={statusColor(detail?.graphStatus || selectedNode.graphStatus)}>
                        {statusLabel(detail?.graphStatus || selectedNode.graphStatus)}
                    </Tag>
                }
            >
                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Descriptions column={{ xs: 1, md: 2 }} size="small">
                        <Descriptions.Item label="来源类型">
                            {detail?.sourceContentType || selectedNode.sourceContentType || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="来源 ID">
                            {detail?.sourceContentId || selectedNode.sourceContentId || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="来源路径">
                            {detail?.sourcePath || selectedNode.sourcePath || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="内容版本">
                            {detail?.currentVersionNo || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="最近任务">
                            {detail?.latestExtractionTask?.taskId || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="最新图谱版本">
                            {latestVersionId || "-"}
                        </Descriptions.Item>
                    </Descriptions>
                    {detail?.summary ? <Text type="secondary">{detail.summary}</Text> : null}
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-manuscript-extract-button"
                            disabled={!canEdit}
                            type="primary"
                            loading={extracting}
                            onClick={() => onExtract("GRAPH")}
                        >
                            抽取图谱
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-manuscript-apply-candidate-button"
                            disabled={!canApply || !canApplyCandidate || !hasCandidate(candidate)}
                            loading={applying}
                            onClick={() => onApplyCandidate(numericCandidateTaskId)}
                        >
                            应用候选
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-manuscript-view-version-button"
                            disabled={!latestVersionId}
                            href={latestVersionId ? "/knowledge/graph-results" : undefined}
                        >
                            查看结果
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-manuscript-refinement-button"
                            disabled={!latestVersionId}
                            href={
                                latestVersionId
                                    ? `/knowledge/refinement?graphVersionId=${encodeURIComponent(
                                          String(latestVersionId)
                                      )}`
                                    : undefined
                            }
                        >
                            进入精修
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </Card>
            <GraphExtractionCandidatePreview candidate={candidate} loading={candidateLoading} />
        </KuzhambuSpace>
    );
};
