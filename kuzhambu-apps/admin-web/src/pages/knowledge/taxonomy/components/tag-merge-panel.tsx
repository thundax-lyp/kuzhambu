import { Alert, Button, Card, Descriptions, Empty, Select, Space, Typography } from "antd";
import { useState } from "react";
import { KuzhambuList, KuzhambuListItem } from "@/components/kuzhambu-list";
import type { TagMergeCommand } from "../taxonomy-service";
import type {
    TagAliasRecord,
    TagContentRefRecord,
    TagMergePreviewRecord,
    TagRecord
} from "../taxonomy-types";

const { Paragraph, Text, Title } = Typography;

interface TagMergePanelProps {
    applying: boolean;
    canEditTag: boolean;
    preview?: TagMergePreviewRecord | null;
    previewing: boolean;
    tags: TagRecord[];
    onApply: (request: TagMergeCommand) => void;
    onPreview: (request: TagMergeCommand) => void;
}

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "MANUAL":
            return "人工";
        case "AI_EXTRACTED":
            return "AI 提取";
        default:
            return source || "-";
    }
};

const readContentSummary = (contentRef: TagContentRefRecord) => {
    return `${contentRef.contentTitle || contentRef.contentId || contentRef.id} · ${contentRef.contentType || "-"}`;
};

const readTagOptionLabel = (tag: TagRecord) => {
    return `${tag.name}（${tag.id}）`;
};

const readTagSummary = (tag?: TagRecord | null) => {
    if (!tag) {
        return "-";
    }
    return `${tag.name}（${tag.id}）`;
};

export const TagMergePanel = ({
    applying,
    canEditTag,
    preview,
    previewing,
    tags,
    onApply,
    onPreview
}: TagMergePanelProps) => {
    const [sourceTagId, setSourceTagId] = useState<string>();
    const [targetTagId, setTargetTagId] = useState<string>();
    const mergeDisabled = !sourceTagId || !targetTagId || sourceTagId === targetTagId;

    const previewMerge = () => {
        if (mergeDisabled) {
            return;
        }
        onPreview({
            sourceTagId: sourceTagId!,
            targetTagId: targetTagId!
        });
    };

    const applyMerge = () => {
        if (mergeDisabled) {
            return;
        }
        onApply({
            sourceTagId: sourceTagId!,
            targetTagId: targetTagId!
        });
    };

    const renderAliasList = (aliases?: TagAliasRecord[] | null) => {
        if (!aliases?.length) {
            return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="无待迁移别名" />;
        }
        return (
            <KuzhambuList
                dataSource={aliases}
                renderItem={(alias) => (
                    <KuzhambuListItem key={alias.id}>
                        <Space orientation="vertical" size={0}>
                            <Text strong>{alias.name}</Text>
                            <Text type="secondary">来源：{readSourceLabel(alias.source)}</Text>
                        </Space>
                    </KuzhambuListItem>
                )}
                size="small"
            />
        );
    };

    const renderContentRefList = (contentRefs?: TagContentRefRecord[] | null) => {
        if (!contentRefs?.length) {
            return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="无受影响内容引用" />;
        }
        return (
            <KuzhambuList
                dataSource={contentRefs}
                renderItem={(contentRef) => (
                    <KuzhambuListItem key={contentRef.id}>
                        <Space orientation="vertical" size={0}>
                            <Text>{readContentSummary(contentRef)}</Text>
                            <Text type="secondary">来源：{readSourceLabel(contentRef.source)}</Text>
                        </Space>
                    </KuzhambuListItem>
                )}
                size="small"
            />
        );
    };

    return (
        <Card className="knowledge-taxonomy-merge-panel" variant="borderless">
            <Space orientation="vertical" size={16} style={{ width: "100%" }}>
                <div className="knowledge-taxonomy-merge-header">
                    <div>
                        <Title level={4}>标签合并治理</Title>
                        <Paragraph type="secondary">
                            先预览别名和内容引用影响，再执行源标签并入目标标签。
                        </Paragraph>
                    </div>
                    <Space wrap>
                        <Button
                            onClick={previewMerge}
                            loading={previewing}
                            disabled={mergeDisabled}
                        >
                            预览合并影响
                        </Button>
                        <Button
                            type="primary"
                            danger
                            onClick={applyMerge}
                            loading={applying}
                            disabled={mergeDisabled || !canEditTag}
                        >
                            执行标签合并
                        </Button>
                    </Space>
                </div>

                <div className="knowledge-taxonomy-merge-controls">
                    <div className="knowledge-taxonomy-merge-field">
                        <Text strong>源标签</Text>
                        <Select
                            aria-label="源标签"
                            placeholder="选择要并入的源标签"
                            showSearch
                            optionFilterProp="label"
                            value={sourceTagId}
                            disabled={!canEditTag}
                            options={tags.map((tag) => ({
                                label: readTagOptionLabel(tag),
                                value: tag.id
                            }))}
                            onChange={setSourceTagId}
                        />
                    </div>
                    <div className="knowledge-taxonomy-merge-field">
                        <Text strong>目标标签</Text>
                        <Select
                            aria-label="目标标签"
                            placeholder="选择接收治理结果的目标标签"
                            showSearch
                            optionFilterProp="label"
                            value={targetTagId}
                            disabled={!canEditTag}
                            options={tags
                                .filter((tag) => tag.id !== sourceTagId)
                                .map((tag) => ({
                                    label: readTagOptionLabel(tag),
                                    value: tag.id
                                }))}
                            onChange={setTargetTagId}
                        />
                    </div>
                </div>

                {sourceTagId && targetTagId && sourceTagId === targetTagId ? (
                    <Alert title="源标签和目标标签不能相同" type="warning" showIcon />
                ) : null}

                {preview ? (
                    <div className="knowledge-taxonomy-merge-preview">
                        <Descriptions column={2} size="small" bordered>
                            <Descriptions.Item label="源标签">
                                {readTagSummary(preview.sourceTag)}
                            </Descriptions.Item>
                            <Descriptions.Item label="目标标签">
                                {readTagSummary(preview.targetTag)}
                            </Descriptions.Item>
                            <Descriptions.Item label="待审核记录数">
                                {preview.pendingReviewCount ?? 0}
                            </Descriptions.Item>
                            <Descriptions.Item label="治理记录数">
                                {preview.governedRecordCount ?? 0}
                            </Descriptions.Item>
                        </Descriptions>

                        <div className="knowledge-taxonomy-merge-preview-grid">
                            <Card size="small" title="待迁移别名">
                                {renderAliasList(preview.aliasesToMerge)}
                            </Card>
                            <Card size="small" title="受影响内容引用">
                                {renderContentRefList(preview.impactedContentRefs)}
                            </Card>
                        </div>
                    </div>
                ) : (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description="选择源标签和目标标签后，可先预览治理影响。"
                    />
                )}
            </Space>
        </Card>
    );
};
