import { Typography } from "antd";
import type { ReactNode } from "react";

const { Text } = Typography;

interface WangqiDocumentTagsSectionProps {
    content?: ReactNode;
}

export const WangqiDocumentTagsSection = ({ content }: WangqiDocumentTagsSectionProps) => {
    return content || <Text type="secondary">暂无标签</Text>;
};
