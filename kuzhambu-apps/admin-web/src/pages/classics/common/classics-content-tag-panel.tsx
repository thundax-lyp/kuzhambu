import { PlusOutlined } from "@ant-design/icons";
import { App, Empty, Input, Pagination, Spin, Typography } from "antd";
import { useMemo, useState, type ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuList,
    KuzhambuListItem,
    KuzhambuModal,
    KuzhambuSpace,
    KuzhambuSpaceCompact,
    KuzhambuTag
} from "@/components";

import * as contentService from "./classics-content-service";
import * as taxonomyService from "@/pages/knowledge/taxonomy/taxonomy-service";
import type { ClassicsContentTagRecord, ClassicsContentType } from "./classics-content-types";
import { type ClassicsContentTagCommand } from "./classics-content-service";
import "./classics-content-tag-panel.css";

interface ClassicsContentTagPanelProps {
    contentId: string;
    contentType: ClassicsContentType;
    onChanged?: () => void;
    panelTitle?: string;
    showHeader?: boolean;
    toolbarExtra?: ReactNode;
}

const TAG_PICKER_PAGE_SIZE = 20;
const TAG_PICKER_COLUMN_SIZE = 10;

const getActiveTags = (tags: ClassicsContentTagRecord[] | unknown) =>
    (Array.isArray(tags) ? tags : []).filter((tag) => (tag.status || "ACTIVE") !== "REMOVED");

const normalizeTagName = (value?: string | null) => value?.trim() || "";

const readSourceType = (source?: string | null) => {
    switch (source) {
        case "AI":
        case "AI_EXTRACTED":
            return "accent";
        case "MANUAL":
            return "info";
        default:
            return "neutral";
    }
};

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "AI":
        case "AI_EXTRACTED":
            return "AI";
        case "MANUAL":
            return "手工";
        default:
            return source || "未知";
    }
};

export const ClassicsContentTagPanel = ({
    contentId,
    contentType,
    onChanged,
    panelTitle,
    showHeader = true,
    toolbarExtra
}: ClassicsContentTagPanelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [addModalOpen, setAddModalOpen] = useState(false);
    const [tagKeywordInput, setTagKeywordInput] = useState("");
    const [tagSearchKeyword, setTagSearchKeyword] = useState("");
    const [tagPickerPageNo, setTagPickerPageNo] = useState(1);

    const queryKey = ["classics", "content", "tags", contentType, contentId] as const;

    const tagsQuery = useQuery({
        queryKey,
        queryFn: () =>
            contentService.listTags({
                contentType,
                contentId
            }),
        enabled: Boolean(contentType && contentId),
        retry: false
    });

    const tags = useMemo(() => getActiveTags(tagsQuery.data), [tagsQuery.data]);
    const taxonomyTagsQuery = useQuery({
        queryKey: [
            "knowledge",
            "taxonomy",
            "tags",
            "content-picker",
            tagSearchKeyword,
            tagPickerPageNo
        ],
        queryFn: () =>
            taxonomyService.pageTags({
                pageNo: tagPickerPageNo,
                pageSize: TAG_PICKER_PAGE_SIZE,
                name: tagSearchKeyword,
                status: "ENABLED",
                sortDirection: "ASC"
            }),
        enabled: addModalOpen && Boolean(contentId),
        retry: false
    });
    const existingTagNameKeys = useMemo(
        () =>
            new Set(
                tags
                    .map((tag) => normalizeTagName(tag.tagNameSnapshot).toLocaleLowerCase())
                    .filter(Boolean)
            ),
        [tags]
    );
    const tagPickerRecords = taxonomyTagsQuery.data?.records || [];
    const tagPickerLeftRecords = tagPickerRecords.slice(0, TAG_PICKER_COLUMN_SIZE);
    const tagPickerRightRecords = tagPickerRecords.slice(TAG_PICKER_COLUMN_SIZE);
    const tagPickerTotal = taxonomyTagsQuery.data?.totalCount ?? taxonomyTagsQuery.data?.count ?? 0;

    const refreshTags = async () => {
        await queryClient.invalidateQueries({ queryKey });
    };

    const notifyChanged = async () => {
        await refreshTags();
        if (onChanged) {
            onChanged();
        }
    };

    const addMutation = useMutation({
        mutationFn: (request: ClassicsContentTagCommand) =>
            contentService.addTag({
                ...request,
                contentId,
                contentType
            }),
        onSuccess: async () => {
            await notifyChanged();
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "添加标签失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: contentService.deleteTag,
        onSuccess: async () => {
            await notifyChanged();
            messageApi.success("标签已移除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "移除标签失败");
        }
    });

    const resetTagPicker = () => {
        setTagKeywordInput("");
        setTagSearchKeyword("");
        setTagPickerPageNo(1);
    };

    const searchTags = () => {
        setTagSearchKeyword(normalizeTagName(tagKeywordInput));
        setTagPickerPageNo(1);
    };

    const addTagByName = async (tagName: string) => {
        const normalizedTagName = normalizeTagName(tagName);
        if (!normalizedTagName) {
            messageApi.warning("请先输入标签名");
            return;
        }
        if (existingTagNameKeys.has(normalizedTagName.toLocaleLowerCase())) {
            messageApi.info("该标签已选择");
            return;
        }
        await addMutation.mutateAsync({
            contentId,
            contentType,
            tagNameSnapshot: normalizedTagName,
            source: "MANUAL",
            status: "ACTIVE"
        });
        setAddModalOpen(false);
        resetTagPicker();
        messageApi.success(`已添加标签：${normalizedTagName}`);
    };

    const markRemoved = async (tag: ClassicsContentTagRecord) => {
        if (!tag.id) {
            return;
        }
        await deleteMutation.mutateAsync({ id: tag.id });
    };

    const renderTagPickerItem = (tag: { id?: string | null; name?: string | null }) => {
        const tagName = normalizeTagName(tag.name);
        const selected = existingTagNameKeys.has(tagName.toLocaleLowerCase());
        return (
            <KuzhambuListItem
                className="classics-content-tag-picker-item"
                extra={
                    selected ? (
                        <KuzhambuTag type="success">已选择</KuzhambuTag>
                    ) : (
                        <Typography.Link
                            onClick={() => {
                                void addTagByName(tagName);
                            }}
                        >
                            选择
                        </Typography.Link>
                    )
                }
            >
                <span className="classics-content-tag-picker-name">{tagName || "-"}</span>
            </KuzhambuListItem>
        );
    };

    if (tagsQuery.isError) {
        return <Empty description="标签列表加载失败" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }

    const cardTitle = showHeader ? panelTitle || "内容标签" : undefined;
    const actionButtons = (
        <KuzhambuSpace wrap size={8}>
            {toolbarExtra}
            <KuzhambuButton
                testId="classics-common-classics-content-tag-open-add-button"
                icon={<PlusOutlined />}
                type="primary"
                onClick={() => {
                    resetTagPicker();
                    setAddModalOpen(true);
                }}
            >
                添加
            </KuzhambuButton>
        </KuzhambuSpace>
    );

    return (
        <KuzhambuCard size="small" title={cardTitle} extra={showHeader ? actionButtons : null}>
            <KuzhambuSpace orientation="vertical" size={16}>
                {!showHeader ? actionButtons : null}

                {tagsQuery.isLoading ? (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="加载中" />
                ) : tags.length ? (
                    <KuzhambuSpace wrap aria-label="标签列表">
                        {tags.map((tag) => {
                            const tagName = normalizeTagName(tag.tagNameSnapshot) || "-";
                            return (
                                <KuzhambuTag
                                    key={tag.id ?? `${tagName}-${tag.source ?? ""}`}
                                    closable={Boolean(tag.id)}
                                    type={readSourceType(tag.source)}
                                    onClose={(event) => {
                                        event.preventDefault();
                                        void markRemoved(tag);
                                    }}
                                >
                                    {tagName}
                                    <span style={{ marginLeft: 6, opacity: 0.72 }}>
                                        {readSourceLabel(tag.source)}
                                    </span>
                                </KuzhambuTag>
                            );
                        })}
                    </KuzhambuSpace>
                ) : (
                    <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
                    </div>
                )}
            </KuzhambuSpace>
            <KuzhambuModal
                testId="classics-content-tag-add-modal"
                destroyOnHidden
                footer={
                    <KuzhambuSpace className="classics-content-tag-picker-footer">
                        <KuzhambuButton
                            testId="classics-content-tag-close-button"
                            onClick={() => {
                                setAddModalOpen(false);
                                resetTagPicker();
                            }}
                        >
                            关闭
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
                open={addModalOpen}
                title="添加标签"
                width={760}
                onCancel={() => {
                    setAddModalOpen(false);
                    resetTagPicker();
                }}
            >
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    <KuzhambuSpaceCompact className="classics-content-tag-picker-search">
                        <Input
                            aria-label="添加标签"
                            placeholder="输入标签名或搜索关键词"
                            value={tagKeywordInput}
                            onChange={(event) => setTagKeywordInput(event.target.value)}
                            onPressEnter={searchTags}
                        />
                        <KuzhambuButton
                            testId="classics-content-tag-search-button"
                            onClick={searchTags}
                        >
                            搜索
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-content-tag-add-input-button"
                            icon={<PlusOutlined />}
                            loading={addMutation.isPending}
                            type="primary"
                            onClick={() => {
                                void addTagByName(tagKeywordInput);
                            }}
                        >
                            添加
                        </KuzhambuButton>
                    </KuzhambuSpaceCompact>
                    <div className="classics-content-tag-picker">
                        <Typography.Text strong>标签库候选</Typography.Text>
                        {taxonomyTagsQuery.isError ? (
                            <div className="classics-content-tag-picker-empty">
                                <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description="标签库候选加载失败"
                                />
                            </div>
                        ) : !taxonomyTagsQuery.isFetching && !tagPickerRecords.length ? (
                            <div className="classics-content-tag-picker-empty">
                                <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description="暂无候选标签"
                                />
                            </div>
                        ) : (
                            <Spin spinning={taxonomyTagsQuery.isFetching}>
                                <div className="classics-content-tag-picker-columns">
                                    <KuzhambuList
                                        ariaLabel="标签库候选左列"
                                        bordered
                                        className="classics-content-tag-picker-list"
                                        dataSource={tagPickerLeftRecords}
                                        empty={null}
                                        itemKey={(tag) => tag.id ?? tag.name ?? ""}
                                        renderItem={renderTagPickerItem}
                                        size="small"
                                    />
                                    <KuzhambuList
                                        ariaLabel="标签库候选右列"
                                        bordered
                                        className="classics-content-tag-picker-list"
                                        dataSource={tagPickerRightRecords}
                                        empty={null}
                                        itemKey={(tag) => tag.id ?? tag.name ?? ""}
                                        renderItem={renderTagPickerItem}
                                        size="small"
                                    />
                                </div>
                            </Spin>
                        )}
                        <Pagination
                            align="end"
                            current={tagPickerPageNo}
                            pageSize={TAG_PICKER_PAGE_SIZE}
                            showSizeChanger={false}
                            size="small"
                            total={tagPickerTotal}
                            onChange={(pageNo) => setTagPickerPageNo(pageNo)}
                        />
                    </div>
                </KuzhambuSpace>
            </KuzhambuModal>
        </KuzhambuCard>
    );
};
