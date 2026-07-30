import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { App, Empty, Form, Input } from "antd";
import { useMemo, useState, type ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuModal,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableSortPosition,
    KuzhambuButton,
    KuzhambuCard
} from "@/components";

import * as contentService from "./classics-content-service";
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
    contentId: string;
    contentType: ClassicsContentType;
    id?: string | null;
    tagId?: string | null;
    tagNameSnapshot: string;
}

const getActiveTags = (tags: ClassicsContentTagRecord[] | unknown) =>
    (Array.isArray(tags) ? tags : []).filter((tag) => (tag.status || "ACTIVE") !== "REMOVED");

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "AI":
            return "AI";
        case "AI_EXTRACTED":
            return "AI 提取";
        case "MANUAL":
            return "手工";
        default:
            return source || "—";
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
    const [isEditorOpen, setIsEditorOpen] = useState(false);
    const [editingTag, setEditingTag] = useState<ClassicsContentTagRecord | undefined>(undefined);
    const [form] = Form.useForm<TagEditorValues>();

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
            setIsEditorOpen(false);
            messageApi.success("标签已添加");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "添加标签失败");
        }
    });

    const updateMutation = useMutation({
        mutationFn: (request: ClassicsContentTagCommand) =>
            contentService.updateTag({
                ...request,
                contentId,
                contentType,
                id: request.id ?? editingTag?.id
            }),
        onSuccess: async () => {
            await notifyChanged();
            setIsEditorOpen(false);
            messageApi.success("标签已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "更新标签失败");
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

    const sortMutation = useMutation({
        mutationFn: contentService.sortTags,
        onSuccess: async () => {
            await notifyChanged();
            messageApi.success("标签顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
        }
    });

    const openCreate = () => {
        setEditingTag(undefined);
        setIsEditorOpen(true);
        form.setFieldsValue({
            contentId,
            contentType,
            tagNameSnapshot: ""
        });
    };

    const openEdit = (tag: ClassicsContentTagRecord) => {
        setEditingTag(tag);
        setIsEditorOpen(true);
        form.setFieldsValue({
            contentId,
            contentType,
            id: tag.id,
            tagId: tag.tagId,
            tagNameSnapshot: tag.tagNameSnapshot || ""
        });
    };

    const markRemoved = async (tag: ClassicsContentTagRecord) => {
        if (!tag.id) {
            return;
        }
        await deleteMutation.mutateAsync({ id: tag.id });
    };

    const submitTag = async () => {
        const formValues = await form.validateFields();
        const command: ClassicsContentTagCommand = {
            ...formValues,
            id: editingTag?.id,
            tagId: formValues.tagId,
            tagNameSnapshot: formValues.tagNameSnapshot.trim(),
            source: "MANUAL",
            status: "ACTIVE"
        };

        if (editingTag) {
            await updateMutation.mutateAsync(command);
            return;
        }

        await addMutation.mutateAsync(command);
    };

    const submitSort = (
        sourceTag: ClassicsContentTagRecord,
        targetTag: ClassicsContentTagRecord,
        sortDirection: KuzhambuTableSortPosition
    ) => {
        if (!sourceTag.id || !targetTag.id || sourceTag.id === targetTag.id) {
            return;
        }

        const filtered = [...tags];
        const sourceTagId = String(sourceTag.id);
        const targetTagId = String(targetTag.id);
        const sourceIndex = filtered.findIndex((tag) => String(tag.id) === sourceTagId);
        const targetIndex = filtered.findIndex((tag) => String(tag.id) === targetTagId);
        if (sourceIndex < 0 || targetIndex < 0) {
            return;
        }

        const sorted = [...filtered];
        const [sourceItem] = sorted.splice(sourceIndex, 1);
        const insertIndex = sortDirection === "before" ? targetIndex : targetIndex + 1;
        sorted.splice(insertIndex, 0, sourceItem);

        sortMutation.mutate({
            contentId,
            contentType,
            orderedIds: sorted
                .map((tag) => tag.id)
                .filter((id) => id != null && String(id).length > 0)
                .map(String),
            sortDirection: "ASC"
        });
    };

    const closeEditor = () => {
        setIsEditorOpen(false);
        setEditingTag(undefined);
        form.resetFields();
    };

    if (tagsQuery.isError) {
        return <Empty description="标签列表加载失败" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }

    const cardTitle = showHeader ? panelTitle || "内容标签" : undefined;

    return (
        <KuzhambuCard size="small" title={cardTitle}>
            <KuzhambuSpace orientation="vertical" size={16}>
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-common-classics-content-tag-action-button"
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={openCreate}
                    >
                        添加标签
                    </KuzhambuButton>
                    {toolbarExtra}
                </KuzhambuSpace>

                <KuzhambuTable
                    ariaLabel="标签列表"
                    dataSource={tags}
                    columns={[
                        {
                            key: "tagNameSnapshot",
                            title: "标签",
                            render: (_value, tag) => tag.tagNameSnapshot || "-"
                        },
                        {
                            key: "source",
                            title: "来源",
                            render: (_value, tag) => readSourceLabel(tag.source)
                        },
                        {
                            key: "actions",
                            title: "操作",
                            width: 180,
                            render: (_value, tag) => (
                                <KuzhambuSpace size="small" orientation="horizontal">
                                    <KuzhambuButton
                                        testId="classics-common-classics-content-tag-action-button-2"
                                        icon={<EditOutlined />}
                                        size="small"
                                        onClick={() => openEdit(tag)}
                                    >
                                        更换
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        testId="classics-common-classics-content-tag-action-button-3"
                                        danger
                                        icon={<DeleteOutlined />}
                                        loading={deleteMutation.isPending}
                                        size="small"
                                        onClick={() => markRemoved(tag)}
                                    >
                                        移除
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                            )
                        }
                    ]}
                    rowKey="id"
                    loading={tagsQuery.isLoading}
                    locale={{
                        emptyText: tagsQuery.isFetching ? (
                            "加载中"
                        ) : (
                            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
                        )
                    }}
                    pagination={false}
                    sortable
                    onSort={submitSort}
                />

                <KuzhambuModal
                    testId="classics-content-tag-editor-modal"
                    destroyOnHidden
                    okButtonProps={{
                        loading: addMutation.isPending || updateMutation.isPending
                    }}
                    open={isEditorOpen}
                    title={editingTag ? "更换标签" : "添加标签"}
                    onCancel={closeEditor}
                    onOk={submitTag}
                >
                    <KuzhambuForm
                        form={form}
                        initialValues={{
                            contentId,
                            contentType
                        }}
                        labelWrap
                    >
                        <KuzhambuFormItem
                            label={editingTag ? "目标标签" : "标签名称"}
                            name="tagNameSnapshot"
                            layoutSize="large"
                            rules={[{ required: true, message: "请输入标签名称" }]}
                            extra="输入已有标签名会绑定已有标签；输入新标签名会创建并绑定到当前内容。"
                        >
                            <Input
                                aria-label={editingTag ? "目标标签" : "标签名称"}
                                placeholder={
                                    editingTag ? "请输入要绑定的目标标签" : "请输入要添加的标签"
                                }
                            />
                        </KuzhambuFormItem>
                    </KuzhambuForm>
                </KuzhambuModal>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
