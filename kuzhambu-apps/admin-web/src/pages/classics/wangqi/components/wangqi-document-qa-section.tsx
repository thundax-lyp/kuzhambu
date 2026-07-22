import { Typography } from "antd";
import type { ReactNode } from "react";

const { Text } = Typography;

interface WangqiDocumentQaSectionProps {
    content?: ReactNode;
}

export const WangqiDocumentQaSection = ({ content }: WangqiDocumentQaSectionProps) => {
    return content || <Text type="secondary">暂无问答</Text>;
};
