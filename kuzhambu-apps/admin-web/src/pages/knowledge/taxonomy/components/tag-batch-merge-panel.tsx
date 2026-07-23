import { Descriptions, Empty, Select, Typography } from "antd";
import { useState } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuList, KuzhambuListItem } from "@/components/kuzhambu-list";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { TagBatchMergeCommand } from "../taxonomy-service";
import type {
    TagAliasRecord,
    TagBatchMergePreviewRecord,
    TagContentRefRecord,
    TagRecord
} from "../taxonomy-types";

const { Text } = Typography;

interface TagBatchMergePanelProps {
    applying: boolean;
    candidateTargetTags: TagRecord[];
    open: boolean;
    preview?: TagBatchMergePreviewRecord | null;
    previewing: boolean;
    selectedSourceTagIds: string[];
    selectedSourceTags: TagRecord[];
    onApply: (request: TagBatchMergeCommand) => void;
    onClose: () => void;
    onPreview: (request: TagBatchMergeCommand) => void;
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

const readTagOptionLabel = (tag: TagRecord) => {
    return `${tag.name}（${tag.id}）`;
};

const readContentSummary = (contentRef: TagContentRefRecord) => {
    return `${contentRef.contentTitle || contentRef.contentId || contentRef.id} · ${contentRef.contentType || "-"}`;
};

const renderTagList = (tags: TagRecord[]) => {
    if (!tags.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无源标签" />;
    }
    return (
        <KuzhambuList
            dataSource={tags}
            renderItem={(tag) => (
                <KuzhambuListItem key={tag.id}>
                    <KuzhambuSpace orientation="vertical" size={0}>
                        <Text strong>{tag.name}</Text>
                        <Text type="secondary">{tag.id}</Text>
                    </KuzhambuSpace>
                </KuzhambuListItem>
            )}
            size="small"
        />
    );
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
                    <KuzhambuSpace orientation="vertical" size={0}>
                        <Text strong>{alias.name}</Text>
                        <Text type="secondary">来源：{readSourceLabel(alias.source)}</Text>
                    </KuzhambuSpace>
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
                    <KuzhambuSpace orientation="vertical" size={0}>
                        <Text>{readContentSummary(contentRef)}</Text>
                        <Text type="secondary">来源：{readSourceLabel(contentRef.source)}</Text>
                    </KuzhambuSpace>
                </KuzhambuListItem>
            )}
            size="small"
        />
    );
};

export const TagBatchMergePanel = ({
    applying,
    candidateTargetTags,
    open,
    preview,
    previewing,
    selectedSourceTagIds,
    selectedSourceTags,
    onApply,
    onClose,
    onPreview
}: TagBatchMergePanelProps) => {
    const [targetTagId, setTargetTagId] = useState<string>();
    const submitDisabled = !targetTagId || selectedSourceTagIds.length === 0;

    const buildRequest = (): TagBatchMergeCommand | null => {
        if (submitDisabled) {
            return null;
        }
        return {
            sourceTagIds: selectedSourceTagIds,
            targetTagId: targetTagId!
        };
    };

    const previewImpact = () => {
        const request = buildRequest();
        if (request) {
            onPreview(request);
        }
    };

    const applyMerge = () => {
        const request = buildRequest();
        if (request) {
            onApply(request);
        }
    };

    return (
        <KuzhambuDrawer
            testId="knowledge-taxonomy-tag-batch-merge-panel-drawer"
            className="knowledge-taxonomy-tag-batch-merge-panel"
            title="批量合并标签"
            open={open}
            size="large"
            onClose={onClose}
            footerActions={[
                {
                    testId: "knowledge-taxonomy-tag-batch-merge-cancel-button",
                    title: "取消",
                    disabled: previewing || applying,
                    action: onClose
                },
                {
                    testId: "knowledge-taxonomy-tag-batch-merge-action-button",
                    title: "预览影响",
                    loading: previewing,
                    disabled: submitDisabled,
                    action: previewImpact
                },
                {
                    testId: "knowledge-taxonomy-tag-batch-merge-action-button-2",
                    title: "执行批量合并",
                    type: "primary",
                    danger: true,
                    loading: applying,
                    disabled: submitDisabled || !preview,
                    action: applyMerge
                }
            ]}
        >
            <div className="knowledge-taxonomy-tag-batch-panel">
                <div className="knowledge-taxonomy-tag-batch-field">
                    <Text strong>源标签</Text>
                    {renderTagList(selectedSourceTags)}
                </div>
                <div className="knowledge-taxonomy-tag-batch-field">
                    <Text strong>目标标签</Text>
                    <Select
                        aria-label="批量合并目标标签"
                        placeholder="选择接收治理结果的目标标签"
                        showSearch
                        optionFilterProp="label"
                        value={targetTagId}
                        options={candidateTargetTags.map((tag) => ({
                            label: readTagOptionLabel(tag),
                            value: tag.id
                        }))}
                        onChange={setTargetTagId}
                    />
                </div>

                {preview ? (
                    <div className="knowledge-taxonomy-tag-batch-preview">
                        <Descriptions column={2} size="small" bordered>
                            <Descriptions.Item label="源标签数">
                                {preview.sourceTags?.length ?? selectedSourceTagIds.length}
                            </Descriptions.Item>
                            <Descriptions.Item label="目标标签">
                                {preview.targetTag?.name || preview.targetTag?.id || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="待审核记录数">
                                {preview.pendingReviewCount ?? 0}
                            </Descriptions.Item>
                            <Descriptions.Item label="治理记录数">
                                {preview.governedRecordCount ?? 0}
                            </Descriptions.Item>
                        </Descriptions>

                        <div className="knowledge-taxonomy-tag-batch-preview-grid">
                            <div>
                                <Text strong>待迁移别名</Text>
                                {renderAliasList(preview.aliasesToMerge)}
                            </div>
                            <div>
                                <Text strong>受影响内容引用</Text>
                                {renderContentRefList(preview.impactedContentRefs)}
                            </div>
                        </div>
                    </div>
                ) : (
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description="选择目标标签后，可先预览影响再执行批量合并。"
                    />
                )}
            </div>
        </KuzhambuDrawer>
    );
};
