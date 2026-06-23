import { ApartmentOutlined, NodeIndexOutlined, ShareAltOutlined } from "@ant-design/icons";
import { Alert, Card, Col, Empty, Row, Space, Tag, Typography } from "antd";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import "./graph-extraction-page.css";

const { Paragraph, Text, Title } = Typography;

const TASK_TYPE_CARDS = [
    {
        description: "为单条内容提交实体关系抽取任务，适合先做轻量关系核查。",
        icon: <ShareAltOutlined />,
        key: "RELATION",
        title: "关系抽取"
    },
    {
        description: "为指定内容生成正式知识图谱候选，后续可进入候选应用闭环。",
        icon: <NodeIndexOutlined />,
        key: "GRAPH",
        title: "图谱抽取"
    },
    {
        description: "为人物或谱系内容提交世系抽取任务，沉淀节点与关系候选。",
        icon: <ApartmentOutlined />,
        key: "LINEAGE",
        title: "世系抽取"
    }
];

export const GraphExtractionPage = () => {
    return (
        <KuzhambuPage
            className="graph-extraction-page knowledge-graph-extraction-page"
            description="统一管理 Knowledge 抽取任务、候选结果和正式应用动作。"
            eyebrow="Knowledge / Graph Extraction"
            title="知识抽取任务"
        >
            <Space direction="vertical" size={16} className="knowledge-graph-extraction-layout">
                <Alert
                    banner
                    className="knowledge-graph-extraction-banner"
                    message="本页先提供任务骨架，下一步会接通创建动作、任务列表和候选应用。"
                    type="info"
                />

                <section aria-labelledby="graph-extraction-create-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-create-section" level={4}>
                            创建抽取任务
                        </Title>
                        <Text type="secondary">三类抽取任务共用统一任务台账和候选应用链路。</Text>
                    </div>
                    <Row gutter={[16, 16]}>
                        {TASK_TYPE_CARDS.map((item) => (
                            <Col key={item.key} xs={24} md={8}>
                                <Card
                                    className="knowledge-graph-extraction-card"
                                    variant="borderless"
                                >
                                    <Space direction="vertical" size={12}>
                                        <Tag color="blue">{item.key}</Tag>
                                        <div className="knowledge-graph-extraction-card-title">
                                            <span className="knowledge-graph-extraction-card-icon">
                                                {item.icon}
                                            </span>
                                            <Title level={5}>{item.title}</Title>
                                        </div>
                                        <Paragraph className="knowledge-graph-extraction-card-description">
                                            {item.description}
                                        </Paragraph>
                                    </Space>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                </section>

                <section aria-labelledby="graph-extraction-task-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-task-section" level={4}>
                            任务列表
                        </Title>
                        <Text type="secondary">
                            将展示任务状态、AI 候选关联、失败原因和应用时间线。
                        </Text>
                    </div>
                    <Card className="knowledge-graph-extraction-placeholder" variant="borderless">
                        <Empty
                            description="任务列表与详情抽屉将在下一步接入。"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    </Card>
                </section>
            </Space>
        </KuzhambuPage>
    );
};
