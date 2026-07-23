import { Typography } from "antd";
import type { ReactNode } from "react";
import "./wangqi-document-source-section.css";

const { Text } = Typography;

interface WangqiDocumentSourceSectionProps {
    content?: ReactNode;
}

export const WangqiDocumentSourceSection = ({ content }: WangqiDocumentSourceSectionProps) => {
    return content ? (
        <div className="wangqi-document-edit-drawer-source-file">{content}</div>
    ) : (
        <Text type="secondary">暂无原始文件</Text>
    );
};
