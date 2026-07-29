import { Typography } from "antd";

const { Paragraph, Text, Title } = Typography;

export const GraphResultsToolbar = () => (
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
            当前页已以图谱版本作为主入口，管理员可以从版本详情下钻审阅实体、关系和世系正式结果。
        </Paragraph>
    </section>
);
