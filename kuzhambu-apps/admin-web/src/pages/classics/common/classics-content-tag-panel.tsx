import { PlusOutlined } from "@ant-design/icons";
import { App, Empty, Form } from "antd";
import { useMemo, useState, type ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuModal,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";

import * as contentService from "./classics-content-service";
import * as taxonomyService from "@/pages/knowledge/taxonomy/taxonomy-service";
import type { ClassicsContentTagRecord, ClassicsContentType } from "./classics-content-types";
import { type ClassicsContentTagCommand } from "./classics-content-service";

interface ClassicsContentTagPanelProps {
    contentId: string;
    contentType: ClassicsContentType;
    onChanged?: () => void;
    panelTitle?: string;
    showHeader?: boolean;
    toolbarExtra?: ReactNode;
}

interface TagEditorValues {
    tagNames?: string[];
}

const getActiveTags = (tags: ClassicsContentTagRecord[] | unknown) =>
    (Array.isArray(tags) ? tags : []).filter((tag) => (tag.status || "ACTIVE") !== "REMOVED");

const normalizeTagName = (value?: string | null) => value?.trim() || "";

const uniqueTagNames = (values: Array<string | null | undefined>) => {
    const seen = new Set<string>();
    return values.map(normalizeTagName).filter((value) => {
        if (!value) {
            return false;
        }
        const key = value.toLocaleLowerCase();
        if (seen.has(key)) {
            return false;
        }
        seen.add(key);
        return true;
    });
};

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
    const [form] = Form.useForm<TagEditorValues>();
    const [addModalOpen, setAddModalOpen] = useState(false);
    const [tagSearchText, setTagSearchText] = useState("");

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
        queryKey: ["knowledge", "taxonomy", "tags", "content-picker", tagSearchText],
        queryFn: () =>
            taxonomyService.pageTags({
                pageNo: 1,
                pageSize: 20,
                name: tagSearchText,
                status: "ENABLED",
                sortDirection: "ASC"
            }),
        enabled: Boolean(contentId),
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
    const tagOptions = useMemo(() => {
        const optionNames = uniqueTagNames([
            ...(taxonomyTagsQuery.data?.records || []).map((tag) => tag.name),
            ...tags.map((tag) => tag.tagNameSnapshot)
        ]);
        return optionNames.map((tagName) => ({ label: tagName, value: tagName }));
    }, [tags, taxonomyTagsQuery.data?.records]);

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

    const submitTags = async () => {
        const formValues = await form.validateFields();
        const tagNames = uniqueTagNames(formValues.tagNames || []).filter(
            (tagName) => !existingTagNameKeys.has(tagName.toLocaleLowerCase())
        );
        if (!tagNames.length) {
            messageApi.info("没有需要添加的新标签");
            form.resetFields();
            return;
        }

        await Promise.all(
            tagNames.map((tagName) =>
                addMutation.mutateAsync({
                    contentId,
                    contentType,
                    tagNameSnapshot: tagName,
                    source: "MANUAL",
                    status: "ACTIVE"
                })
            )
        );
        form.resetFields();
        setTagSearchText("");
        setAddModalOpen(false);
        messageApi.success(`已添加 ${tagNames.length} 个标签`);
    };

    const markRemoved = async (tag: ClassicsContentTagRecord) => {
        if (!tag.id) {
            return;
        }
        await deleteMutation.mutateAsync({ id: tag.id });
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
                onClick={() => setAddModalOpen(true)}
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
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
                )}
            </KuzhambuSpace>
            <KuzhambuModal
                testId="classics-content-tag-add-modal"
                destroyOnHidden
                open={addModalOpen}
                title="添加标签"
                okText="添加"
                confirmLoading={addMutation.isPending}
                onCancel={() => {
                    setAddModalOpen(false);
                    form.resetFields();
                    setTagSearchText("");
                }}
                onOk={submitTags}
            >
                <KuzhambuForm form={form} labelWrap>
                    <KuzhambuFormItem
                        label="标签"
                        name="tagNames"
                        layoutSize="large"
                        extra="输入已有标签名会绑定已有标签；输入新标签名会创建并绑定到当前内容。"
                    >
                        <KuzhambuSelect<string[]>
                            aria-label="添加标签"
                            mode="tags"
                            options={tagOptions}
                            placeholder="输入标签后回车，或从列表选择"
                            style={{ width: "100%" }}
                            tokenSeparators={[",", "，", "\n"]}
                            onSearch={setTagSearchText}
                        />
                    </KuzhambuFormItem>
                </KuzhambuForm>
            </KuzhambuModal>
        </KuzhambuCard>
    );
};
