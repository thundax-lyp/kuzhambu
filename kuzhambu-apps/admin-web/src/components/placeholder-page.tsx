import { Card, Space, Typography } from "antd";

const { Title, Text, Paragraph } = Typography;

interface PlaceholderPageProps {
    title: string;
    domain: string;
    description: string;
}

export const PlaceholderPage = ({ title, domain, description }: PlaceholderPageProps) => {
    return (
        <Card className="panel">
            <Space orientation="vertical" size={8}>
                <Text className="eyebrow">{domain}</Text>
                <Title level={2}>{title}</Title>
                <Paragraph>{description}</Paragraph>
            </Space>
        </Card>
    );
};
