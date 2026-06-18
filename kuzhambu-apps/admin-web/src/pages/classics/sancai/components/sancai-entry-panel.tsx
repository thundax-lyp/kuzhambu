import { PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Skeleton, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryModel } from "./sancai-entry-model";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import * as entryService from "../services/sancai-entry-service";
import type { SancaiEntryRecord, SancaiVolumeRecord } from "../sancai-types";

const { Text, Title } = Typography;

interface SancaiEntryPanelProps {
    categoryId: number | null;
    isCatalogLoading: boolean;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    onClearEntry: () => void;
    onSelectEntry: (entry: SancaiEntryRecord) => void;
    refreshVersion: number;
    selectedEntryId: number | null;
    volumeId: number | null;
    volumes: SancaiVolumeRecord[];
}

export const SancaiEntryPanel = ({
    categoryId,
    isCatalogLoading,
    keyword,
    lifecycleStatus,
    onClearEntry,
    onSelectEntry,
    refreshVersion,
    selectedEntryId,
    volumeId,
    volumes
}: SancaiEntryPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [isCreating, setIsCreating] = useState(false);
    const entriesQuery = useQuery({
        queryKey: [
            "classics",
            "sancai",
            "entries",
            "list",
            categoryId,
            volumeId,
            keyword,
            lifecycleStatus,
            refreshVersion
        ],
        queryFn: () =>
            entryService.list({
                categoryId,
                volumeId,
                keyword,
                lifecycleStatus,
                sortDirection: "ASC"
            }),
        enabled: categoryId !== null && volumeId !== null,
        retry: false
    });
    const entries = entriesQuery.data || [];
    const selectedEntry = isCreating
        ? undefined
        : entries.find((entry) => entry.id === selectedEntryId);
    const isLoading = isCatalogLoading || entriesQuery.isLoading;
    const addEntryMutation = useMutation({
        mutationFn: entryService.add,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            setIsCreating(false);
            messageApi.success("三才图会条目已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增失败");
        }
    });
    const updateEntryMutation = useMutation({
        mutationFn: entryService.update,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            messageApi.success("三才图会条目已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteEntryMutation = useMutation({
        mutationFn: entryService.deleteById,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            onClearEntry();
            messageApi.success("三才图会条目已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const sortEntryMutation = useMutation({
        mutationFn: entryService.sort,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] });
            messageApi.success("三才图会条目顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序保存失败");
        }
    });

    const startCreate = () => {
        if (!volumeId) {
            messageApi.warning("请先选择卷目");
            return;
        }
        onClearEntry();
        setIsCreating(true);
    };

    const selectEntry = (entry: SancaiEntryRecord) => {
        setIsCreating(false);
        onSelectEntry(entry);
    };

    const submitEntry = (form: SancaiEntryFormValues) => {
        if (isCreating) {
            if (!volumeId) {
                messageApi.warning("请先选择卷目");
                return;
            }
            addEntryMutation.mutate({
                volumeId,
                title: form.title,
                originalText: form.originalText,
                translationText: form.translationText,
                summary: form.summary,
                lifecycleStatus: "DRAFT",
                visibility: form.visibility,
                translationStatus: "PENDING",
                imageStatus: "PENDING",
                visualAssetStatus: "PENDING",
                refinementStatus: "PENDING"
            });
            return;
        }
        if (!selectedEntry) {
            return;
        }
        updateEntryMutation.mutate({
            id: selectedEntry.id,
            volumeId: selectedEntry.volumeId,
            title: form.title,
            originalText: form.originalText,
            translationText: form.translationText,
            summary: form.summary,
            lifecycleStatus: selectedEntry.lifecycleStatus,
            visibility: form.visibility,
            translationStatus: selectedEntry.translationStatus,
            imageStatus: selectedEntry.imageStatus,
            visualAssetStatus: selectedEntry.visualAssetStatus,
            refinementStatus: selectedEntry.refinementStatus
        });
    };

    const deleteEntry = (entry: SancaiEntryRecord) => {
        confirm.danger({
            title: "删除三才图会条目",
            message: `确认删除 ${entry.title?.trim() || `条目 ${entry.id}`}？`,
            description: "删除后该条目将不再出现在当前卷目下。",
            okText: "删除",
            onConfirm: () => deleteEntryMutation.mutateAsync(entry.id)
        });
    };

    const sortEntry = (
        sourceEntry: SancaiEntryRecord,
        targetEntry: SancaiEntryRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (sourceEntry.id === targetEntry.id) {
            return;
        }
        const remainingEntries = entries.filter((entry) => entry.id !== sourceEntry.id);
        const targetIndex = remainingEntries.findIndex((entry) => entry.id === targetEntry.id);
        if (targetIndex < 0) {
            return;
        }
        const insertIndex = position === "before" ? targetIndex : targetIndex + 1;
        const sortedEntries = [...remainingEntries];
        sortedEntries.splice(insertIndex, 0, sourceEntry);
        sortEntryMutation.mutate({
            orderedIds: sortedEntries.map((entry) => entry.id),
            sortDirection: "ASC"
        });
    };

    return (
        <div className="sancai-content-grid">
            <section className="sancai-list-panel">
                <div className="sancai-panel-heading">
                    <Title level={3}>条目</Title>
                    <div className="sancai-heading-actions">
                        <Text type="secondary">{entries.length} 条</Text>
                        <Button
                            aria-label="新增三才图会条目"
                            title="新增条目"
                            icon={<PlusOutlined />}
                            size="small"
                            onClick={startCreate}
                        />
                    </div>
                </div>
                {entriesQuery.isError ? (
                    <Alert
                        className="sancai-alert"
                        type="warning"
                        showIcon
                        message="三才图会条目加载失败"
                        description="请确认后台条目接口可用后刷新页面。"
                    />
                ) : null}
                <SancaiEntryList
                    entries={entries}
                    isLoading={isLoading || sortEntryMutation.isPending}
                    volumes={volumes}
                    onDelete={deleteEntry}
                    onSort={sortEntry}
                    onView={selectEntry}
                />
            </section>

            <aside className="sancai-detail-panel">
                <div className="sancai-panel-heading">
                    <Title level={3}>详情</Title>
                </div>
                {isLoading ? (
                    <Skeleton active paragraph={{ rows: 6 }} />
                ) : (
                    <SancaiEntryModel
                        key={isCreating ? "create" : selectedEntry?.id ?? "empty"}
                        entry={selectedEntry}
                        isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                        mode={isCreating ? "create" : "edit"}
                        onSubmit={submitEntry}
                    />
                )}
            </aside>
        </div>
    );
};
