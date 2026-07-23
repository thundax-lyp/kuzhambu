import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { App, Card, Empty, Form, Input, Select } from "antd";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import * as contentService from "../classics-content-service";
import type { ClassicsContentQaPairRecord, ClassicsContentType } from "../classics-content-types";
import {
    type ClassicsContentQaPairCommand,
    type ClassicsContentQaPairSortCommand
} from "../classics-content-service";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface ClassicsContentQaPanelProps {
    contentId: number;
    contentType: ClassicsContentType;
    onChanged?: () => void;
    panelTitle?: string;
}

interface QaEditorValues {
    contentId: number;
    contentType: ClassicsContentType;
    id?: number | null;
    question: string;
    answer: string;
    source: string;
}

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

export const ClassicsContentQaPanel = ({
    contentId,
    contentType,
    onChanged,
    panelTitle
}: ClassicsContentQaPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [isEditorOpen, setIsEditorOpen] = useState(false);
    const [editingQaPair, setEditingQaPair] = useState<ClassicsContentQaPairRecord | undefined>(
        undefined
    );
    const [form] = Form.useForm<QaEditorValues>();

    const queryKey = ["classics", "content", "qa-pairs", contentType, contentId] as const;

    const qaPairsQuery = useQuery({
        queryKey,
        queryFn: () =>
            contentService.listQaPairs({
                contentType,
                contentId
            }),
        enabled: Boolean(contentType && contentId),
        retry: false
    });

    const qaPairs = useMemo(
        () => (Array.isArray(qaPairsQuery.data) ? qaPairsQuery.data : []),
        [qaPairsQuery.data]
    );

    const refreshQaPairs = async () => {
        await queryClient.invalidateQueries({ queryKey });
    };

    const notifyChanged = async () => {
        await refreshQaPairs();
        if (onChanged) {
            onChanged();
        }
    };

    const addMutation = useMutation({
        mutationFn: contentService.addQaPair,
        onSuccess: async () => {
            await notifyChanged();
            setIsEditorOpen(false);
            messageApi.success("问答对已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增问答对失败");
        }
    });

    const updateMutation = useMutation({
        mutationFn: (request: ClassicsContentQaPairCommand) =>
            contentService.updateQaPair({
                ...request,
                id: request.id ?? editingQaPair?.id
            }),
        onSuccess: async () => {
            await notifyChanged();
            setIsEditorOpen(false);
            messageApi.success("问答对已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "更新问答对失败");
        }
    });

    const sortMutation = useMutation({
        mutationFn: (request: ClassicsContentQaPairSortCommand) =>
            contentService.sortQaPairs(request),
        onSuccess: async () => {
            await notifyChanged();
            messageApi.success("问答对顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: (request: { id: number }) => contentService.deleteQaPair(request),
        onSuccess: async () => {
            await notifyChanged();
            messageApi.success("问答对已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除问答对失败");
        }
    });

    const openCreate = () => {
        setEditingQaPair(undefined);
        setIsEditorOpen(true);
        form.setFieldsValue({
            contentId,
            contentType,
            source: "MANUAL",
            question: "",
            answer: ""
        });
    };

    const openEdit = (qaPair: ClassicsContentQaPairRecord) => {
        setEditingQaPair(qaPair);
        setIsEditorOpen(true);
        form.setFieldsValue({
            contentId,
            contentType,
            id: qaPair.id,
            question: qaPair.question || "",
            answer: qaPair.answer || "",
            source: qaPair.source || "MANUAL"
        });
    };

    const submitQaPair = async () => {
        const formValues = await form.validateFields();
        const command: ClassicsContentQaPairCommand = {
            contentId,
            contentType,
            id: editingQaPair?.id,
            question: formValues.question.trim(),
            answer: formValues.answer.trim(),
            source: formValues.source || "MANUAL"
        };

        if (editingQaPair) {
            await updateMutation.mutateAsync(command);
            return;
        }

        await addMutation.mutateAsync(command);
    };

    const submitSort = (
        sourcePair: ClassicsContentQaPairRecord,
        targetPair: ClassicsContentQaPairRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (!sourcePair.id || !targetPair.id || sourcePair.id === targetPair.id) {
            return;
        }

        const filtered = [...qaPairs];
        const sourceIndex = filtered.findIndex((pair) => pair.id === sourcePair.id);
        const targetIndex = filtered.findIndex((pair) => pair.id === targetPair.id);
        if (sourceIndex < 0 || targetIndex < 0) {
            return;
        }

        const sorted = [...filtered];
        const [sourceItem] = sorted.splice(sourceIndex, 1);
        const insertIndex = position === "before" ? targetIndex : targetIndex + 1;
        sorted.splice(insertIndex, 0, sourceItem);

        sortMutation.mutate({
            orderedIds: sorted
                .map((pair) => pair.id)
                .filter((id): id is number => typeof id === "number"),
            sortDirection: "ASC"
        });
    };

    const closeEditor = () => {
        setIsEditorOpen(false);
        setEditingQaPair(undefined);
        form.resetFields();
    };

    const deleteQaPair = (qaPair: ClassicsContentQaPairRecord) => {
        if (!qaPair.id) {
            return;
        }
        const qaPairId = qaPair.id;

        confirm.danger({
            title: "确认删除问答对",
            message: "删除后会生成新的正式版本。是否继续？",
            okText: "删除",
            onConfirm: () => deleteMutation.mutate({ id: qaPairId })
        });
    };

    if (qaPairsQuery.isError) {
        return <Empty description="问答列表加载失败" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
    }

    return (
        <Card size="small" title={panelTitle || "问答对治理"}>
            <KuzhambuSpace orientation="vertical" size={16}>
                <div>
                    <KuzhambuButton
                        testId="classics-common-classics-content-qa-action-button"
                        icon={<PlusOutlined />}
                        type="primary"
                        onClick={openCreate}
                    >
                        新增问答对
                    </KuzhambuButton>
                </div>

                <KuzhambuTable
                    ariaLabel="问答对列表"
                    dataSource={qaPairs}
                    columns={[
                        {
                            key: "question",
                            title: "问题",
                            width: 220,
                            render: (_value, pair) => pair.question || "-"
                        },
                        {
                            key: "answer",
                            title: "答案",
                            render: (_value, pair) => pair.answer || "-"
                        },
                        {
                            key: "source",
                            title: "来源",
                            width: 110,
                            render: (_value, pair) => readSourceLabel(pair.source)
                        },
                        {
                            key: "actions",
                            title: "操作",
                            width: 180,
                            render: (_value, pair) => (
                                <KuzhambuSpace size="small" orientation="horizontal">
                                    <KuzhambuButton
                                        testId={`classics-common-classics-content-qa-edit-${pair.id}-button`}
                                        icon={<EditOutlined />}
                                        size="small"
                                        onClick={() => openEdit(pair)}
                                    >
                                        编辑
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        testId={`classics-common-classics-content-qa-delete-${pair.id}-button`}
                                        danger
                                        icon={<DeleteOutlined />}
                                        loading={deleteMutation.isPending}
                                        size="small"
                                        onClick={() => deleteQaPair(pair)}
                                    >
                                        删除
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                            )
                        }
                    ]}
                    rowKey="id"
                    loading={qaPairsQuery.isLoading}
                    locale={{
                        emptyText: qaPairsQuery.isFetching ? (
                            "加载中"
                        ) : (
                            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无问答对" />
                        )
                    }}
                    pagination={false}
                    sortable
                    onSort={submitSort}
                />

                <KuzhambuModal
                    testId="classics-content-qa-editor-modal"
                    destroyOnHidden
                    okButtonProps={{
                        loading: addMutation.isPending || updateMutation.isPending
                    }}
                    open={isEditorOpen}
                    title={editingQaPair ? "编辑问答对" : "新增问答对"}
                    onCancel={closeEditor}
                    onOk={submitQaPair}
                >
                    <KuzhambuForm
                        form={form}
                        initialValues={{
                            contentId,
                            contentType,
                            source: "MANUAL"
                        }}
                        labelWrap
                    >
                        <KuzhambuFormItem
                            label="问题"
                            name="question"
                            layoutSize="large"
                            rules={[{ required: true, message: "请输入问题" }]}
                        >
                            <Input.TextArea
                                aria-label="问答问题"
                                rows={3}
                                placeholder="请输入问题"
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="答案"
                            name="answer"
                            layoutSize="large"
                            rules={[{ required: true, message: "请输入答案" }]}
                        >
                            <Input.TextArea
                                aria-label="问答答案"
                                rows={4}
                                placeholder="请输入答案"
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem label="来源" name="source" layoutSize="large">
                            <Select
                                aria-label="问答来源"
                                options={[
                                    { label: "手工", value: "MANUAL" },
                                    { label: "AI 提取", value: "AI_EXTRACTED" }
                                ]}
                            />
                        </KuzhambuFormItem>
                    </KuzhambuForm>
                </KuzhambuModal>
            </KuzhambuSpace>
        </Card>
    );
};
