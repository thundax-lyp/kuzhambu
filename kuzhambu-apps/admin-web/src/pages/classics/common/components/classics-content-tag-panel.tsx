import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { App, Button, Card, Empty, Form, Input, Modal, Select } from "antd";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import * as contentService from "../classics-content-service";
import type {
    ClassicsContentTagRecord,
    ClassicsContentType
} from "../classics-content-types";
import {
    type ClassicsContentTagCommand,
    type ClassicsContentTagPayload
} from "../classics-content-service";

interface ClassicsContentTagPanelProps {
    contentId: number;
    contentType: ClassicsContentType;
    onChanged?: () => void;
    panelTitle?: string;
}

interface TagEditorValues {
    contentId: number;
    contentType: ClassicsContentType;
    id?: number | null;
    tagId?: number | null;
    tagNameSnapshot: string;
    source: string;
    status: string;
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

const readStatusLabel = (status?: string | null) => {
    switch (status) {
        case "ACTIVE":
            return "启用";
        case "REMOVED":
            return "已移除";
        default:
            return status || "—";
    }
};

export const ClassicsContentTagPanel = ({
    contentId,
    contentType,
    onChanged,
    panelTitle
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
        mutationFn: (request: ClassicsContentTagPayload) =>
            contentService.addTag({
                ...request,
                contentId,
                contentType
            }),
        onSuccess: async () => {
            await notifyChanged();
            setIsEditorOpen(false);
            messageApi.success("标签已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增标签失败");
        }
    });

    const updateMutation = useMutation({
        mutationFn: (request: ClassicsContentTagPayload) =>
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
            source: "MANUAL",
            status: "ACTIVE",
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
            source: tag.source || "MANUAL",
            status: tag.status || "ACTIVE",
            tagNameSnapshot: tag.tagNameSnapshot || ""
        });
    };

    const markRemoved = async (tag: ClassicsContentTagRecord) => {
        await updateMutation.mutateAsync({
            contentId,
            contentType,
            id: tag.id,
            tagId: tag.tagId,
            tagNameSnapshot: tag.tagNameSnapshot || "",
            source: tag.source || "MANUAL",
            status: "REMOVED"
        });
    };

    const submitTag = async () => {
        const formValues = await form.validateFields();
        const command: ClassicsContentTagCommand = {
            ...formValues,
            id: editingTag?.id,
            tagId: formValues.tagId,
            tagNameSnapshot: formValues.tagNameSnapshot.trim(),
            source: formValues.source || "MANUAL",
            status: formValues.status || "ACTIVE"
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
        const sourceIndex = filtered.findIndex((tag) => tag.id === sourceTag.id);
        const targetIndex = filtered.findIndex((tag) => tag.id === targetTag.id);
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
                .filter((id): id is number => typeof id === "number"),
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

    return (
        <Card size="small" title={panelTitle || "标签治理"}>
            <KuzhambuSpace orientation="vertical" size={16}>
                <div>
                    <Button
                        aria-label="新增标签"
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={openCreate}
                    >
                        新增标签
                    </Button>
                </div>

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
                            key: "status",
                            title: "状态",
                            render: (_value, tag) => readStatusLabel(tag.status)
                        },
                        {
                            key: "actions",
                            title: "操作",
                            width: 180,
                            render: (_value, tag) => (
                                <KuzhambuSpace size="small" orientation="horizontal">
                                    <Button
                                        aria-label={`编辑标签 ${tag.id}`}
                                        icon={<EditOutlined />}
                                        size="small"
                                        onClick={() => openEdit(tag)}
                                    >
                                        编辑
                                    </Button>
                                    <Button
                                        aria-label={`移除标签 ${tag.id}`}
                                        danger
                                        icon={<DeleteOutlined />}
                                        size="small"
                                        onClick={() => markRemoved(tag)}
                                    >
                                        移除
                                    </Button>
                                </KuzhambuSpace>
                            )
                        }
                    ]}
                    rowKey="id"
                    loading={tagsQuery.isLoading}
                    locale={{
                        emptyText: tagsQuery.isFetching
                            ? "加载中"
                            : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
                    }}
                    pagination={false}
                    sortable
                    onSort={submitSort}
                />

                <Modal
                    destroyOnHidden
                    okButtonProps={{
                        loading: addMutation.isPending || updateMutation.isPending
                    }}
                    open={isEditorOpen}
                    title={editingTag ? "编辑标签" : "新增标签"}
                    onCancel={closeEditor}
                    onOk={submitTag}
                >
                    <Form
                        form={form}
                        initialValues={{
                            contentId,
                            contentType,
                            source: "MANUAL",
                            status: "ACTIVE"
                        }}
                        labelCol={{ span: 6 }}
                        labelWrap
                        wrapperCol={{ span: 18 }}
                    >
                        <Form.Item
                            label="标签名称"
                            name="tagNameSnapshot"
                            rules={[{ required: true, message: "请输入标签名称" }]}
                        >
                            <Input aria-label="标签名称" placeholder="请输入标签名称" />
                        </Form.Item>
                        <Form.Item label="来源" name="source">
                            <Select
                                aria-label="标签来源"
                                options={[
                                    { label: "手工", value: "MANUAL" },
                                    { label: "AI 提取", value: "AI_EXTRACTED" }
                                ]}
                            />
                        </Form.Item>
                        <Form.Item label="状态" name="status">
                            <Select
                                aria-label="标签状态"
                                options={[{ label: "启用", value: "ACTIVE" }, { label: "已移除", value: "REMOVED" }]}
                            />
                        </Form.Item>
                    </Form>
                </Modal>
            </KuzhambuSpace>
        </Card>
    );
};
