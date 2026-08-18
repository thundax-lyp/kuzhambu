import { Empty } from "antd";
import { KuzhambuCard, KuzhambuDescriptions, KuzhambuSpace, KuzhambuTag } from "@/components";
import type {
    GraphMaterialDetailRecord,
    GraphMaterialStatus
} from "@/pages/knowledge/graph-material/graph-material-types";
import "./material-overview-section.css";

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

interface MaterialOverviewSectionProps {
    detail: GraphMaterialDetailRecord | null;
}

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

export const MaterialOverviewSection = ({ detail }: MaterialOverviewSectionProps) => {
    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-material-detail-overview-section"
                description="请选择素材查看概览。"
            />
        );
    }

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
                            label: "节点数",
                            children: String(detail.nodes.length)
                        },
                        {
                            label: "边数",
                            children: String(detail.edges.length)
                        },
                        {
                            label: "任务数",
                            children: detail.taskSummary?.totalTaskCount ?? "0"
                        }
                    ]}
                    size="small"
                    bordered
                />
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
