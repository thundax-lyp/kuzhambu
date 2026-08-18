import { Empty, Typography } from "antd";
import {
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTag,
    KuzhambuTimeline
} from "@/components";
import type {
    GraphMaterialDetailRecord,
    GraphMaterialStatus,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";
import "./material-overview-section.css";

const { Text } = Typography;

const SOURCE_TYPE_LABELS: Readonly<Record<string, string>> = {
    MING_CUSTOMS: "明代风俗",
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王祺文献"
};

const MATERIAL_STATUS_LABELS: Readonly<Record<GraphMaterialStatus, string>> = {
    DRAFT: "草稿",
    READY: "待发布",
    FAILED: "失败",
    PUBLISHED: "已发布",
    PUBLISHING: "发布中",
    WITHDRAWING: "撤回中"
};

const MATERIAL_STATUS_TYPES: Readonly<
    Record<GraphMaterialStatus, "accent" | "info" | "success" | "warning" | "danger">
> = {
    DRAFT: "accent",
    READY: "info",
    FAILED: "danger",
    PUBLISHED: "success",
    PUBLISHING: "info",
    WITHDRAWING: "warning"
};

const TASK_STATUS_LABELS: Readonly<Record<GraphTaskExecutionStatus, string>> = {
    CANCELLED: "已取消",
    FAILED: "已失败",
    PENDING: "待执行",
    RUNNING: "运行中",
    SUCCEEDED: "已成功"
};

interface MaterialOverviewSectionProps {
    detail: GraphMaterialDetailRecord | null;
}

const formatTimestamp = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Number(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Date(timestamp).toLocaleString("zh-CN", { hour12: false });
};

const readSourceTypeLabel = (contentType: string) => SOURCE_TYPE_LABELS[contentType] || contentType;

const readMaterialStatus = (detail: GraphMaterialDetailRecord) => {
    if (!detail.material) {
        return <KuzhambuTag type="warning">未初始化</KuzhambuTag>;
    }
    return (
        <KuzhambuTag type={MATERIAL_STATUS_TYPES[detail.material.status]}>
            {MATERIAL_STATUS_LABELS[detail.material.status]}
        </KuzhambuTag>
    );
};

const buildRecentActivityItems = (detail: GraphMaterialDetailRecord) => {
    const items = [];
    const latestTask = detail.taskSummary?.latestTask;
    if (latestTask) {
        items.push({
            children: (
                <KuzhambuSpace orientation="vertical" size={2}>
                    <Text>
                        最近任务 #{latestTask.id} / {TASK_STATUS_LABELS[latestTask.executionStatus]}
                    </Text>
                    <Text type="secondary">
                        {latestTask.currentStage}，进度 {latestTask.progress}%，请求于{" "}
                        {formatTimestamp(latestTask.requestedAt)}
                    </Text>
                </KuzhambuSpace>
            )
        });
    }
    if (detail.material?.publishedAt) {
        items.push({
            children: <Text>最近发布于 {formatTimestamp(detail.material.publishedAt)}</Text>
        });
    }
    if (detail.materialStats?.calculatedAt) {
        items.push({
            children: <Text>统计计算于 {formatTimestamp(detail.materialStats.calculatedAt)}</Text>
        });
    }
    return items;
};

export const MaterialOverviewSection = ({ detail }: MaterialOverviewSectionProps) => {
    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-material-detail-overview-section"
                description="请选择素材查看概览。"
            />
        );
    }

    const recentActivityItems = buildRecentActivityItems(detail);

    return (
        <KuzhambuSpace
            className="knowledge-graph-material-overview-section"
            data-testid="knowledge-graph-material-detail-overview-section"
            orientation="vertical"
            size={12}
        >
            <KuzhambuCard title="素材来源" size="small">
                <KuzhambuDescriptions
                    ariaLabel="素材来源"
                    column={2}
                    items={[
                        { label: "标题", children: detail.source.title },
                        {
                            label: "来源类型",
                            children: readSourceTypeLabel(detail.source.contentType)
                        },
                        { label: "卷册", children: detail.source.volume ?? "-" },
                        { label: "状态", children: readMaterialStatus(detail) },
                        { label: "摘要", children: detail.source.summary ?? "-", span: 2 }
                    ]}
                    size="small"
                    bordered
                />
            </KuzhambuCard>

            <KuzhambuCard title="图谱统计" size="small">
                <KuzhambuDescriptions
                    ariaLabel="图谱统计"
                    column={3}
                    items={[
                        {
                            label: "草稿节点",
                            children: detail.materialStats?.draftNodeCount ?? "0"
                        },
                        {
                            label: "草稿关系",
                            children: detail.materialStats?.draftEdgeCount ?? "0"
                        },
                        {
                            label: "发布贡献",
                            children: detail.materialStats?.publicationContributionCount ?? "0"
                        },
                        {
                            label: "已发布节点",
                            children: detail.materialStats?.publishedNodeCount ?? "0"
                        },
                        {
                            label: "已发布关系",
                            children: detail.materialStats?.publishedEdgeCount ?? "0"
                        },
                        { label: "统计版本", children: detail.materialStats?.statsRevision ?? "-" }
                    ]}
                    size="small"
                    bordered
                />
            </KuzhambuCard>

            <KuzhambuCard title="最近活动" size="small">
                {recentActivityItems.length > 0 ? (
                    <KuzhambuTimeline
                        testId="knowledge-graph-material-overview-activity"
                        items={recentActivityItems}
                    />
                ) : (
                    <Text type="secondary">暂无活动</Text>
                )}
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
