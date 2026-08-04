import { Typography } from "antd";
import type { ReactNode } from "react";

const { Text } = Typography;

interface WangqiDocumentVersionsSectionProps {
    content?: ReactNode;
}

export const WangqiDocumentVersionsSection = ({ content }: WangqiDocumentVersionsSectionProps) => {
    return content || <Text type="secondary">暂无版本</Text>;
};
