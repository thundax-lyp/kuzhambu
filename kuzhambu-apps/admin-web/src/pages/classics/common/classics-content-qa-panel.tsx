import { PlusOutlined, ReloadOutlined } from "@ant-design/icons";
import { App, Empty, Typography } from "antd";
import { useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    KuzhambuButton,
    KuzhambuExpandableText,
    KuzhambuSpace,
    KuzhambuSpaceCompact,
    KuzhambuTable,
    KuzhambuTag,
    type KuzhambuTagType
} from "@/components";

import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { ClassicsContentQaEditorModal } from "./classics-content-qa-editor-modal";
import * as contentService from "./classics-content-service";
import type { ClassicsContentQaPairRecord, ClassicsContentType } from "./classics-content-types";
import { type ClassicsContentQaPairCommand } from "./classics-content-service";

interface ClassicsContentQaPanelProps {
    contentId: string;
    contentType: ClassicsContentType;
    onChanged?: () => void;
    panelTitle?: string;
    readOnly?: boolean;
    toolbarExtra?: ReactNode;
}

const QA_TABLE_PAGE_SIZE = 10;

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "AI":
            return "AI";
        case "AI_EXTRACTED":
            return "AI";
        case "MANUAL":
            return "人工";
        default:
            return source || "—";
    }
};

const readSourceTagType = (source?: string | null): KuzhambuTagType => {
    switch (source) {
        case "AI":
        case "AI_EXTRACTED":
            return "accent";
        case "MANUAL":
            return "neutral";
        default:
            return "info";
    }
};

export const ClassicsContentQaPanel = ({
    contentId,
    contentType,
    onChanged,
    readOnly = false,
    toolbarExtra
}: ClassicsContentQaPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [isEditorOpen, setIsEditorOpen] = useState(false);
    const [editingQaPair, setEditingQaPair] = useState<ClassicsContentQaPairRecord | undefined>(
        undefined
    );

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
            messageApi.success("问答已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "更新问答失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: (request: { id: string }) => contentService.deleteQaPair(request),
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
    };

    const openEdit = (qaPair: ClassicsContentQaPairRecord) => {
        setEditingQaPair(qaPair);
        setIsEditorOpen(true);
    };

    const submitQaPair = async (values: { answer: string; question: string }) => {
        const command: ClassicsContentQaPairCommand = {
            contentId,
            contentType,
            id: editingQaPair?.id,
            question: values.question,
            answer: values.answer,
            source: "MANUAL"
        };

        if (editingQaPair) {
            await updateMutation.mutateAsync(command);
            return;
        }

        await addMutation.mutateAsync(command);
    };

    const closeEditor = () => {
        setIsEditorOpen(false);
        setEditingQaPair(undefined);
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
        <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
            {!readOnly ? (
                <div style={{ display: "flex", justifyContent: "flex-end" }}>
                    <KuzhambuSpaceCompact>
                        {toolbarExtra}
                        <KuzhambuButton
                            testId="classics-common-classics-content-qa-action-button"
                            icon={<PlusOutlined />}
                            type={toolbarExtra ? "default" : "primary"}
                            onClick={openCreate}
                        >
                            新增
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="classics-common-classics-content-qa-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={qaPairsQuery.isFetching}
                            onClick={refreshQaPairs}
                        >
                            刷新
                        </KuzhambuButton>
                    </KuzhambuSpaceCompact>
                </div>
            ) : null}

            <KuzhambuTable
                ariaLabel="问答对列表"
                dataSource={qaPairs}
                columns={[
                    {
                        key: "qa",
                        title: "内容",
                        render: (_value, pair) => (
                            <KuzhambuSpace
                                orientation="vertical"
                                size={6}
                                style={{ width: "100%" }}
                            >
                                <Typography.Text
                                    strong
                                >{`问：${pair.question || "-"}`}</Typography.Text>
                                <div style={{ color: "rgba(0, 0, 0, 0.65)" }}>
                                    <KuzhambuExpandableText
                                        content={`答：${pair.answer || "-"}`}
                                        collapsedRows={2}
                                    />
                                </div>
                            </KuzhambuSpace>
                        )
                    },
                    {
                        key: "source",
                        title: "来源",
                        width: 96,
                        render: (_value, pair) => (
                            <KuzhambuTag type={readSourceTagType(pair.source)}>
                                {readSourceLabel(pair.source)}
                            </KuzhambuTag>
                        )
                    },
                    {
                        key: "actions",
                        title: "操作",
                        options: (pair) => [
                            {
                                key: "edit",
                                text: "编辑",
                                disabled: readOnly,
                                testId: `classics-common-classics-content-qa-edit-${pair.id}-button`,
                                onClick: () => openEdit(pair)
                            },
                            {
                                key: "delete-divider",
                                type: "divider"
                            },
                            {
                                key: "delete",
                                text: "删除",
                                type: "danger",
                                disabled: readOnly || deleteMutation.isPending,
                                testId: `classics-common-classics-content-qa-delete-${pair.id}-button`,
                                onClick: () => deleteQaPair(pair)
                            }
                        ]
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
                minColumnWidth={120}
                pagination={{
                    pageSize: QA_TABLE_PAGE_SIZE
                }}
            />

            <ClassicsContentQaEditorModal
                confirmLoading={addMutation.isPending || updateMutation.isPending}
                open={isEditorOpen}
                qaPair={editingQaPair}
                onCancel={closeEditor}
                onSubmit={submitQaPair}
            />
        </KuzhambuSpace>
    );
};
