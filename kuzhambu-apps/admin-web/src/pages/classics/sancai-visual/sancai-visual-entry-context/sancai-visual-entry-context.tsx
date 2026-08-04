import { FileImageOutlined, SwapOutlined } from "@ant-design/icons";
import { Tag, Typography } from "antd";
import { KuzhambuButton, KuzhambuSpace } from "@/components";
import type { SancaiEntryRecord } from "@/pages/classics/sancai-visual/sancai-visual-types";

import "./sancai-visual-entry-context.css";

const { Paragraph, Text } = Typography;

const readEntryTitle = (entry: SancaiEntryRecord | null | undefined) => {
    if (!entry) {
        return "未选择稿件";
    }
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const readEntrySummary = (entry: SancaiEntryRecord) => {
    return entry.summary?.trim() || entry.originalText?.trim() || "暂无摘要";
};

const statusTagMeta: Record<string, { color: string; label: string }> = {
    ARCHIVED: { color: "default", label: "已下线" },
    DRAFT: { color: "gold", label: "草稿" },
    PUBLISHED: { color: "green", label: "已发布" }
};

const renderStatusTag = (status?: string | null) => {
    const normalizedStatus = status || "UNKNOWN";
    const meta = statusTagMeta[normalizedStatus] ?? {
        color: "blue",
        label: normalizedStatus
    };
    return <Tag color={meta.color}>{meta.label}</Tag>;
};

interface SancaiVisualEntryContextProps {
    entry: SancaiEntryRecord;
    onSelectEntry: () => void;
}

export const SancaiVisualEntryContext = ({
    entry,
    onSelectEntry
}: SancaiVisualEntryContextProps) => {
    return (
        <div className="sancai-visual-entry-context">
            <div>
                <KuzhambuSpace wrap>
                    <FileImageOutlined />
                    <Text strong>{readEntryTitle(entry)}</Text>
                    {renderStatusTag(entry.lifecycleStatus)}
                </KuzhambuSpace>
                <Paragraph
                    className="sancai-visual-entry-summary"
                    type="secondary"
                    ellipsis={{ rows: 2, expandable: true, symbol: "展开" }}
                >
                    {readEntrySummary(entry)}
                </Paragraph>
            </div>
            <KuzhambuButton
                testId="classics-sancai-visual-entry-context-switch-button"
                icon={<SwapOutlined />}
                onClick={onSelectEntry}
            >
                选择稿件
            </KuzhambuButton>
        </div>
    );
};
