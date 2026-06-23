import { Alert, Card, Empty, Space, Tabs, Typography } from "antd";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import "./graph-results-page.css";

const { Paragraph, Text, Title } = Typography;

export const GraphResultsPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");

    return (
        <KuzhambuPage
            className="graph-results-page knowledge-graph-results-page"
            description="以图谱版本为主入口，独立审阅正式实体、关系和世系结果。"
            eyebrow="Knowledge / Graph Results"
            title="正式结果读取"
        >
            <Space orientation="vertical" size={16} className="knowledge-graph-results-layout">
                <Alert
                    banner
                    className="knowledge-graph-results-banner"
                    message="本页将作为正式结果审阅台，与 taxonomy 治理台和抽取任务台保持独立边界。"
                    type="info"
                />

                <section aria-labelledby="graph-results-overview-section">
                    <div className="knowledge-graph-results-section-header">
                        <Title id="graph-results-overview-section" level={4}>
                            结果入口
                        </Title>
                        <Text type="secondary">
                            图谱版本列表会作为主入口，再下钻查看正式实体、关系和世系结果。
                        </Text>
                    </div>
                    <Paragraph className="knowledge-graph-results-helper">
                        当前先搭建独立页组骨架和路由入口，下一步会补齐版本列表、详情抽屉以及版本下钻的正式结果视图。
                    </Paragraph>
                </section>

                <Card className="knowledge-graph-results-shell" variant="borderless">
                    <Tabs
                        defaultActiveKey="versions"
                        items={[
                            {
                                key: "versions",
                                label: "图谱版本",
                                children: (
                                    <Empty
                                        description={
                                            canViewGraph
                                                ? "图谱版本列表即将接入，支持按任务类型、状态和来源内容筛选。"
                                                : "当前账号暂无知识图谱查看权限。"
                                        }
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    />
                                )
                            },
                            {
                                key: "entities",
                                label: "正式实体",
                                children: (
                                    <Empty
                                        description="实体列表将从版本详情下钻进入，默认展示确认状态、版本关联和来源引用。"
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    />
                                )
                            },
                            {
                                key: "relations",
                                label: "正式关系",
                                children: (
                                    <Empty
                                        description="关系列表将与版本详情联动，重点展示关系类型、证据和确认状态。"
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    />
                                )
                            },
                            {
                                key: "lineage",
                                label: "正式世系",
                                children: (
                                    <Empty
                                        description="世系节点和关系会拆成独立读视图，强调版本来源和确认状态。"
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    />
                                )
                            }
                        ]}
                    />
                </Card>
            </Space>
        </KuzhambuPage>
    );
};
