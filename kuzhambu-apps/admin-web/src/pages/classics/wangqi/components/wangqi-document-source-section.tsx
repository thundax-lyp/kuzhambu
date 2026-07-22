import { Typography } from "antd";
import type { ReactNode } from "react";

const { Text } = Typography;

interface WangqiDocumentSourceSectionProps {
    content?: ReactNode;
}

export const WangqiDocumentSourceSection = ({ content }: WangqiDocumentSourceSectionProps) => {
    return content || <Text type="secondary">暂无原始文件</Text>;
};
