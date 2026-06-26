import { Card, Typography } from "antd";
import { KuzhambuSpace } from "@/components/kuzhambu-space";

const { Title, Text, Paragraph } = Typography;

interface PlaceholderPageProps {
    title: string;
    domain: string;
    description: string;
}

export const PlaceholderPage = ({ title, domain, description }: PlaceholderPageProps) => {
    return (
        <Card className="panel">
            <KuzhambuSpace orientation="vertical" size={8}>
                <Text className="eyebrow">{domain}</Text>
                <Title level={2}>{title}</Title>
                <Paragraph>{description}</Paragraph>
            </KuzhambuSpace>
        </Card>
    );
};
