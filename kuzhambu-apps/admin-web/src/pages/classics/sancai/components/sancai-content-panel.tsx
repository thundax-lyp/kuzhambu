import { PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import type { SancaiContentFormValues } from "./sancai-form-values";
import { SancaiContentList } from "./sancai-content-list";
import { SancaiContentModel } from "./sancai-content-model";
import * as contentService from "../services/sancai-content-service";
import type { SancaiContentRecord, SancaiEntryRecord } from "../sancai-types";

const { Text, Title } = Typography;

interface SancaiContentPanelProps {
    defaultCreateOpen?: boolean;
    entry: SancaiEntryRecord | null;
    refreshVersion: number;
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readQuestion = (content: SancaiContentRecord) => {
    return content.question?.trim() || `内容 ${content.id}`;
};

export const SancaiContentPanel = ({
    defaultCreateOpen = false,
    entry,
    refreshVersion
}: SancaiContentPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [editingContent, setEditingContent] = useState<SancaiContentRecord | null>(null);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const entryId = entry?.id ?? null;
    const contentsQuery = useQuery({
        queryKey: ["classics", "sancai", "contents", entryId, refreshVersion],
        queryFn: () => contentService.listByEntry(entryId ?? 0),
        enabled: entryId !== null,
        retry: false
    });
    const contents = contentsQuery.data || [];

    const closeModel = () => {
        setIsModelOpen(false);
        setEditingContent(null);
    };

    const invalidateContents = async () => {
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "contents", entryId] });
    };

    const addMutation = useMutation({
        mutationFn: contentService.add,
        onSuccess: async () => {
            await invalidateContents();
            closeModel();
            messageApi.success("三才图会内容已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "内容新增失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: contentService.update,
        onSuccess: async () => {
            await invalidateContents();
            closeModel();
            messageApi.success("三才图会内容已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "内容更新失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: contentService.deleteById,
        onSuccess: async () => {
            await invalidateContents();
            messageApi.success("三才图会内容已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "内容删除失败");
        }
    });
    const sortMutation = useMutation({
        mutationFn: contentService.sort,
        onSuccess: async () => {
            await invalidateContents();
            messageApi.success("三才图会内容顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "内容排序保存失败");
        }
    });

    const startCreate = () => {
        if (!entryId) {
            messageApi.warning("请先选择条目");
            return;
        }
        setEditingContent(null);
        setIsModelOpen(true);
    };

    const startEdit = (content: SancaiContentRecord) => {
        setEditingContent(content);
        setIsModelOpen(true);
    };

    const submitContent = (form: SancaiContentFormValues) => {
        if (!entryId) {
            messageApi.warning("请先选择条目");
            return;
        }
        const request = {
            id: editingContent?.id,
            entryId,
            question: form.question,
            answer: form.answer,
            source: "MANUAL"
        };
        if (editingContent) {
            updateMutation.mutate(request);
            return;
        }
        addMutation.mutate(request);
    };

    const confirmDelete = (content: SancaiContentRecord) => {
        confirm.danger({
            title: "删除三才图会内容",
            message: `确认删除 ${readQuestion(content)}？`,
            description: "删除后该问答内容将不再出现在当前条目下。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(content.id)
        });
    };

    const sortContent = (
        sourceContent: SancaiContentRecord,
        targetContent: SancaiContentRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (!entryId || sourceContent.id === targetContent.id) {
            return;
        }
        const remainingContents = contents.filter((content) => content.id !== sourceContent.id);
        const targetIndex = remainingContents.findIndex((content) => content.id === targetContent.id);
        if (targetIndex < 0) {
            return;
        }
        const insertIndex = position === "before" ? targetIndex : targetIndex + 1;
        const sortedContents = [...remainingContents];
        sortedContents.splice(insertIndex, 0, sourceContent);
        sortMutation.mutate({
            entryId,
            orderedIds: sortedContents.map((content) => content.id),
            sortDirection: "ASC"
        });
    };

    return (
        <section className="sancai-list-panel">
            <div className="sancai-panel-heading">
                <div>
                    <Title level={3}>内容</Title>
                    <Text type="secondary">{entry ? readTitle(entry, "条目") : "请选择条目"}</Text>
                </div>
                <div className="sancai-heading-actions">
                    <Text type="secondary">{contents.length} 条</Text>
                    <Button
                        aria-label="新增三才图会内容"
                        title="新增内容"
                        icon={<PlusOutlined />}
                        size="small"
                        onClick={startCreate}
                    />
                </div>
            </div>
            {contentsQuery.isError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    message="三才图会内容加载失败"
                    description="请确认后台内容接口可用后刷新页面。"
                />
            ) : null}
            <SancaiContentList
                contents={contents}
                isLoading={contentsQuery.isLoading || sortMutation.isPending}
                onDelete={confirmDelete}
                onEdit={startEdit}
                onSort={sortContent}
            />
            {isModelOpen ? (
                <SancaiContentModel
                    content={editingContent}
                    isSubmitting={addMutation.isPending || updateMutation.isPending}
                    onCancel={closeModel}
                    onSubmit={submitContent}
                />
            ) : null}
        </section>
    );
};
