import { Empty, Typography } from "antd";
import {
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuCard
} from "@/components";

import type { TagGovernanceMetricsRecord } from "./taxonomy-types";

const { Paragraph, Text } = Typography;

interface TagGovernanceMetricsPanelProps {
    loading: boolean;
    metrics?: TagGovernanceMetricsRecord | null;
    onRefresh: () => void;
}

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "MANUAL":
            return "人工";
        case "AI_EXTRACTED":
            return "AI 提取";
        default:
            return source || "-";
    }
};

const renderEmpty = (description: string) => {
    return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={description} />;
};

export const TagGovernanceMetricsPanel = ({
    loading,
    metrics,
    onRefresh
}: TagGovernanceMetricsPanelProps) => {
    return (
        <KuzhambuCard
            className="knowledge-taxonomy-metrics-panel"
            title="标签治理统计"
            extra={
                <KuzhambuButton
                    testId="knowledge-taxonomy-tag-governance-metrics-refresh-stats-button"
                    onClick={onRefresh}
                    loading={loading}
                >
                    刷新统计
                </KuzhambuButton>
            }
            variant="borderless"
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <Paragraph type="secondary">
                    聚焦使用排行、知识库分布、来源占比和月度新增趋势，辅助判断哪些标签适合合并或废弃。
                </Paragraph>

                <div className="knowledge-taxonomy-metrics-grid">
                    <KuzhambuCard size="small" title="标签使用排行">
                        {metrics?.topTags?.length ? (
                            <KuzhambuList
                                dataSource={metrics.topTags}
                                renderItem={(item) => (
                                    <KuzhambuListItem>
                                        <KuzhambuSpace orientation="vertical" size={0}>
                                            <Text strong>{item.tagName || "-"}</Text>
                                            <Text type="secondary">
                                                内容引用：{item.contentRefCount ?? 0}
                                            </Text>
                                        </KuzhambuSpace>
                                    </KuzhambuListItem>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无标签使用排行")
                        )}
                    </KuzhambuCard>

                    <KuzhambuCard size="small" title="知识库分布">
                        {metrics?.categoryDistributions?.length ? (
                            <KuzhambuList
                                dataSource={metrics.categoryDistributions}
                                renderItem={(item) => (
                                    <KuzhambuListItem>
                                        <KuzhambuSpace orientation="vertical" size={0}>
                                            <Text strong>{item.categoryName || "未分类"}</Text>
                                            <Text type="secondary">
                                                标签数量：{item.tagCount ?? 0}
                                            </Text>
                                        </KuzhambuSpace>
                                    </KuzhambuListItem>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无知识库分布")
                        )}
                    </KuzhambuCard>

                    <KuzhambuCard size="small" title="来源占比">
                        {metrics?.sourceRatios?.length ? (
                            <KuzhambuList
                                dataSource={metrics.sourceRatios}
                                renderItem={(item) => (
                                    <KuzhambuListItem>
                                        <KuzhambuSpace orientation="vertical" size={0}>
                                            <Text strong>{readSourceLabel(item.source)}</Text>
                                            <Text type="secondary">
                                                标签数量：{item.tagCount ?? 0}
                                            </Text>
                                        </KuzhambuSpace>
                                    </KuzhambuListItem>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无来源占比")
                        )}
                    </KuzhambuCard>

                    <KuzhambuCard size="small" title="月度新增趋势">
                        {metrics?.monthlyNewTags?.length ? (
                            <KuzhambuList
                                dataSource={metrics.monthlyNewTags}
                                renderItem={(item) => (
                                    <KuzhambuListItem>
                                        <KuzhambuSpace orientation="vertical" size={0}>
                                            <Text strong>{item.month || "-"}</Text>
                                            <Text type="secondary">
                                                新增标签：{item.tagCount ?? 0}
                                            </Text>
                                        </KuzhambuSpace>
                                    </KuzhambuListItem>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无月度新增趋势")
                        )}
                    </KuzhambuCard>
                </div>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
