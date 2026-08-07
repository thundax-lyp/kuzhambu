import { Empty, Tag } from "antd";
import {
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuExpandableText
} from "@/components";

import type {
    GraphWorkbenchManuscriptNode,
    GraphWorkbenchManuscriptRecord,
    GraphWorkbenchRelationRecord,
    GraphWorkbenchStatus
} from "../graph-extraction-types";
import { CurrentGraphPanel } from "./current-graph-panel";

interface GraphExtractionManuscriptDetailProps {
    canEdit?: boolean;
    currentGraphLoading?: boolean;
    currentGraphRelations?: GraphWorkbenchRelationRecord[];
    detail?: GraphWorkbenchManuscriptRecord | null;
    selectedNode?: GraphWorkbenchManuscriptNode | null;
    onOpenExtractionDialog: () => void;
}

const STATUS_LABELS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "未抽取"],
    ["EXTRACTING", "抽取中"],
    ["EXTRACTION_FAILED", "未抽取"],
    ["CANDIDATE_READY", "已抽取"],
    ["APPLIED", "已抽取"],
    ["REFINING", "抽取中"],
    ["REFINED", "已抽取"],
    ["QUALITY_ISSUE", "已抽取"]
]);

const STATUS_COLORS = new Map<GraphWorkbenchStatus, string>([
    ["NOT_EXTRACTED", "default"],
    ["EXTRACTING", "processing"],
    ["EXTRACTION_FAILED", "default"],
    ["CANDIDATE_READY", "success"],
    ["APPLIED", "success"],
    ["REFINING", "processing"],
    ["REFINED", "success"],
    ["QUALITY_ISSUE", "success"]
]);

const statusLabel = (status?: GraphWorkbenchStatus | null) =>
    STATUS_LABELS.get(status || "") || status || "未知";

const statusColor = (status?: GraphWorkbenchStatus | null) =>
    STATUS_COLORS.get(status || "") || "default";

export const GraphExtractionManuscriptDetail = ({
    canEdit = false,
    currentGraphLoading = false,
    currentGraphRelations = [],
    detail,
    selectedNode,
    onOpenExtractionDialog
}: GraphExtractionManuscriptDetailProps) => {
    if (!selectedNode) {
        return (
            <KuzhambuCard className="graph-extraction-create-card" variant="borderless">
                <Empty
                    description="请选择一篇稿件查看图谱状态"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            </KuzhambuCard>
        );
    }

    const latestVersionId = detail?.latestGraphVersion?.versionId;

    return (
        <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
            <KuzhambuCard
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
                    <KuzhambuDescriptions
                        ariaLabel="稿件图谱基础信息"
                        column={{ xs: 1, md: 2 }}
                        size="small"
                        items={[
                            {
                                key: "path",
                                label: "路径",
                                children: detail?.sourcePath || selectedNode.sourcePath || "-"
                            },
                            {
                                key: "version",
                                label: "版本",
                                children: detail?.currentVersionNo || latestVersionId || "-"
                            }
                        ]}
                    />
                    {detail?.summary ? (
                        <KuzhambuExpandableText
                            collapsedRows={2}
                            content={detail.summary}
                            testId="knowledge-graph-extraction-manuscript-summary"
                        />
                    ) : null}
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-manuscript-extract-button"
                            disabled={!canEdit}
                            type="primary"
                            onClick={onOpenExtractionDialog}
                        >
                            抽取图谱
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuSpace>
            </KuzhambuCard>
            <CurrentGraphPanel
                loading={currentGraphLoading}
                relations={currentGraphRelations}
                versionId={latestVersionId}
            />
        </KuzhambuSpace>
    );
};
