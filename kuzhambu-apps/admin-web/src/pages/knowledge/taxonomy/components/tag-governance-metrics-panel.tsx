import { Button, Card, Empty, List, Space, Typography } from "antd";
import type { TagGovernanceMetricsRecord } from "../taxonomy-types";

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
        <Card
            className="knowledge-taxonomy-metrics-panel"
            title="标签治理统计"
            extra={
                <Button onClick={onRefresh} loading={loading}>
                    刷新统计
                </Button>
            }
            variant="borderless"
        >
            <Space orientation="vertical" size={16} style={{ width: "100%" }}>
                <Paragraph type="secondary">
                    聚焦使用排行、知识库分布、来源占比和月度新增趋势，辅助判断哪些标签适合合并或废弃。
                </Paragraph>

                <div className="knowledge-taxonomy-metrics-grid">
                    <Card size="small" title="标签使用排行">
                        {metrics?.topTags?.length ? (
                            <List
                                dataSource={metrics.topTags}
                                renderItem={(item) => (
                                    <List.Item>
                                        <Space orientation="vertical" size={0}>
                                            <Text strong>{item.tagName || "-"}</Text>
                                            <Text type="secondary">
                                                内容引用：{item.contentRefCount ?? 0}
                                            </Text>
                                        </Space>
                                    </List.Item>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无标签使用排行")
                        )}
                    </Card>

                    <Card size="small" title="知识库分布">
                        {metrics?.categoryDistributions?.length ? (
                            <List
                                dataSource={metrics.categoryDistributions}
                                renderItem={(item) => (
                                    <List.Item>
                                        <Space orientation="vertical" size={0}>
                                            <Text strong>{item.categoryName || "未分类"}</Text>
                                            <Text type="secondary">
                                                标签数量：{item.tagCount ?? 0}
                                            </Text>
                                        </Space>
                                    </List.Item>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无知识库分布")
                        )}
                    </Card>

                    <Card size="small" title="来源占比">
                        {metrics?.sourceRatios?.length ? (
                            <List
                                dataSource={metrics.sourceRatios}
                                renderItem={(item) => (
                                    <List.Item>
                                        <Space orientation="vertical" size={0}>
                                            <Text strong>{readSourceLabel(item.source)}</Text>
                                            <Text type="secondary">
                                                标签数量：{item.tagCount ?? 0}
                                            </Text>
                                        </Space>
                                    </List.Item>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无来源占比")
                        )}
                    </Card>

                    <Card size="small" title="月度新增趋势">
                        {metrics?.monthlyNewTags?.length ? (
                            <List
                                dataSource={metrics.monthlyNewTags}
                                renderItem={(item) => (
                                    <List.Item>
                                        <Space orientation="vertical" size={0}>
                                            <Text strong>{item.month || "-"}</Text>
                                            <Text type="secondary">
                                                新增标签：{item.tagCount ?? 0}
                                            </Text>
                                        </Space>
                                    </List.Item>
                                )}
                                size="small"
                            />
                        ) : (
                            renderEmpty("暂无月度新增趋势")
                        )}
                    </Card>
                </div>
            </Space>
        </Card>
    );
};
