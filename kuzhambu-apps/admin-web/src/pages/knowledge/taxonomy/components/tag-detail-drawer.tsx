import { Button, Descriptions, Empty, Input, Typography } from "antd";
import { useState, type ReactNode } from "react";
import { KuzhambuList, KuzhambuListItem, KuzhambuListMeta } from "@/components/kuzhambu-list";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { TagAliasList } from "./tag-alias-list";
import type {
    TagAliasCreateCommand,
    TagAliasRemoveCommand,
    TagDeprecateCommand,
    TagReviewCommand
} from "../taxonomy-service";
import type { TagDetailRecord } from "../taxonomy-types";

const { Paragraph, Text, Title } = Typography;
const { TextArea } = Input;

interface TagDetailDrawerProps {
    canEditAliases?: boolean;
    canDeprecateTag?: boolean;
    creatingAlias?: boolean;
    deprecating?: boolean;
    loading?: boolean;
    open: boolean;
    removingAliasId?: string | null;
    reviewMode?: boolean;
    reviewing?: boolean;
    tagDetail?: TagDetailRecord | null;
    onCreateAlias?: (request: TagAliasCreateCommand) => void;
    onApprove?: (request: TagReviewCommand) => void;
    onClose: () => void;
    onDeprecate?: (request: TagDeprecateCommand) => void;
    onReject?: (request: TagReviewCommand) => void;
    onRemoveAlias?: (request: TagAliasRemoveCommand) => void;
}

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime())
        ? "-"
        : date.toLocaleString("zh-CN", {
              hour12: false
          });
};

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

const readReviewStatusLabel = (status?: string | null) => {
    switch (status) {
        case "APPROVED":
            return "已通过";
        case "REJECTED":
            return "已拒绝";
        case "PENDING":
            return "待审核";
        default:
            return status || "-";
    }
};

const readContentTypeLabel = (contentType?: string | null) => {
    switch (contentType) {
        case "SANCAI_ENTRY":
            return "三才图会";
        case "WANGQI_DOCUMENT":
            return "王圻文档";
        case "MING_CUSTOM":
            return "明代民俗";
        default:
            return contentType || "-";
    }
};

export const TagDetailDrawer = ({
    canEditAliases = false,
    canDeprecateTag = false,
    creatingAlias = false,
    deprecating = false,
    loading = false,
    open,
    removingAliasId,
    reviewMode = false,
    reviewing = false,
    tagDetail,
    onCreateAlias,
    onApprove,
    onClose,
    onDeprecate,
    onReject,
    onRemoveAlias
}: TagDetailDrawerProps) => {
    const [reviewNote, setReviewNote] = useState("");
    const tag = tagDetail?.tag;
    const aliases = tagDetail?.aliases || [];
    const contentRefs = tagDetail?.contentRefs || [];

    const approveTag = () => {
        if (!tag || !onApprove) {
            return;
        }
        onApprove({
            id: tag.id,
            decision: "APPROVE",
            reviewNote: reviewNote.trim() || undefined
        });
    };

    const rejectTag = () => {
        if (!tag || !onReject) {
            return;
        }
        onReject({
            id: tag.id,
            decision: "REJECT",
            reviewNote: reviewNote.trim() || undefined
        });
    };

    const deprecateTag = () => {
        if (!tag || !onDeprecate) {
            return;
        }
        onDeprecate({ id: tag.id });
    };

    const closeDrawer = () => {
        setReviewNote("");
        onClose();
    };

    let footer: ReactNode;
    if (reviewMode) {
        footer = (
            <div className="knowledge-taxonomy-tag-detail-footer">
                <Button disabled={reviewing} onClick={closeDrawer}>
                    关闭
                </Button>
                <KuzhambuSpace>
                    <Button type="primary" loading={reviewing} onClick={approveTag}>
                        通过
                    </Button>
                    <Button danger loading={reviewing} onClick={rejectTag}>
                        拒绝
                    </Button>
                </KuzhambuSpace>
            </div>
        );
    } else if (canDeprecateTag) {
        footer = (
            <div className="knowledge-taxonomy-tag-detail-footer">
                <Button disabled={deprecating} onClick={closeDrawer}>
                    关闭
                </Button>
                <Button danger loading={deprecating} onClick={deprecateTag}>
                    废弃标签
                </Button>
            </div>
        );
    }

    return (
        <KuzhambuDrawer
            className="knowledge-taxonomy-tag-detail-drawer"
            title={reviewMode ? "审核标签" : "标签详情"}
            open={open}
            size="large"
            loading={loading}
            onClose={closeDrawer}
            footer={footer}
        >
            {tag ? (
                <div className="knowledge-taxonomy-tag-detail">
                    <Descriptions column={2} size="small" bordered>
                        <Descriptions.Item label="标签名">{tag.name}</Descriptions.Item>
                        <Descriptions.Item label="分类">
                            {tag.categoryName || tag.categoryId || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="状态">{tag.status || "-"}</Descriptions.Item>
                        <Descriptions.Item label="审核状态">
                            {readReviewStatusLabel(tag.reviewStatus)}
                        </Descriptions.Item>
                        <Descriptions.Item label="来源">
                            {readSourceLabel(tag.source)}
                        </Descriptions.Item>
                        <Descriptions.Item label="内容引用数量">
                            {tag.contentRefCount ?? 0}
                        </Descriptions.Item>
                        <Descriptions.Item label="创建时间">
                            {formatTimestamp(tag.createdAt)}
                        </Descriptions.Item>
                        <Descriptions.Item label="审核时间">
                            {formatTimestamp(tag.reviewedAt)}
                        </Descriptions.Item>
                        <Descriptions.Item label="描述" span={2}>
                            <Paragraph className="knowledge-taxonomy-tag-detail-description">
                                {tag.description || "暂无描述"}
                            </Paragraph>
                        </Descriptions.Item>
                    </Descriptions>

                    <div className="knowledge-taxonomy-tag-detail-section">
                        <Title level={5}>标签别名</Title>
                        <TagAliasList
                            aliases={aliases}
                            canEdit={canEditAliases}
                            loading={loading}
                            removingAliasId={removingAliasId}
                            saving={creatingAlias}
                            tagId={tag.id}
                            onCreate={onCreateAlias}
                            onRemove={onRemoveAlias}
                        />
                    </div>

                    <div className="knowledge-taxonomy-tag-detail-section">
                        <Title level={5}>内容引用</Title>
                        {contentRefs.length > 0 ? (
                            <KuzhambuList
                                dataSource={contentRefs}
                                renderItem={(contentRef) => (
                                    <KuzhambuListItem key={contentRef.id}>
                                        <KuzhambuListMeta
                                            title={contentRef.contentTitle || contentRef.id}
                                            description={
                                                <KuzhambuSpace wrap>
                                                    <Text type="secondary">
                                                        类型：
                                                        {readContentTypeLabel(
                                                            contentRef.contentType
                                                        )}
                                                    </Text>
                                                    <Text type="secondary">
                                                        内容ID：{contentRef.contentId || "-"}
                                                    </Text>
                                                    <Text type="secondary">
                                                        来源：{readSourceLabel(contentRef.source)}
                                                    </Text>
                                                </KuzhambuSpace>
                                            }
                                        />
                                    </KuzhambuListItem>
                                )}
                            />
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description="暂无内容引用"
                            />
                        )}
                    </div>

                    {reviewMode ? (
                        <div className="knowledge-taxonomy-tag-detail-section">
                            <Title level={5}>审核说明</Title>
                            <TextArea
                                rows={4}
                                maxLength={512}
                                showCount
                                value={reviewNote}
                                placeholder="拒绝时必须填写审核说明；通过时可选填写。"
                                onChange={(event) => setReviewNote(event.target.value)}
                            />
                        </div>
                    ) : null}
                </div>
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签详情" />
            )}
        </KuzhambuDrawer>
    );
};
