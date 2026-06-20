import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App } from "antd";
import { useState } from "react";
import * as shareService from "@/api/classics/share-service";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryModel } from "./sancai-entry-model";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import { SancaiVersionHistoryPanel } from "./sancai-version-history-panel";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "../sancai-types";

interface SancaiEntryPanelProps {
    categoryId: number | null;
    defaultCreateOpen?: boolean;
    isCatalogLoading: boolean;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    refreshVersion: number;
    volumeId: number | null;
    volumes: SancaiVolumeRecord[];
}

export const SancaiEntryPanel = ({
    categoryId,
    defaultCreateOpen = false,
    isCatalogLoading,
    keyword,
    lifecycleStatus,
    refreshVersion,
    volumeId,
    volumes
}: SancaiEntryPanelProps) => {
    const { message: messageApi, modal: modalApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [isCreating, setIsCreating] = useState(defaultCreateOpen);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const [editingEntry, setEditingEntry] = useState<SancaiEntryRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);
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
    const detailQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "detail", editingEntry?.id],
        queryFn: () => entryService.get(editingEntry?.id ?? 0),
        enabled: isModelOpen && !isCreating && Boolean(editingEntry?.id),
        retry: false
    });
    const selectedEntry = isCreating ? undefined : (detailQuery.data ?? editingEntry ?? undefined);
    const versionsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "versions", selectedEntry?.id],
        queryFn: () => entryService.listVersions(selectedEntry?.id ?? 0),
        enabled: isModelOpen && !isCreating && Boolean(selectedEntry?.id),
        retry: false
    });
    const versionDetailQuery = useQuery({
        queryKey: [
            "classics",
            "sancai",
            "entries",
            "version",
            selectedEntry?.id,
            selectedVersionId
        ],
        queryFn: () => entryService.getVersion(selectedEntry?.id ?? 0, selectedVersionId ?? 0),
        enabled: isModelOpen && Boolean(selectedEntry?.id && selectedVersionId),
        retry: false
    });
    const versions = versionsQuery.data || [];
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => version.id === selectedVersionId) ||
        null;
    let modelKey = "empty";
    if (isCreating) {
        modelKey = "create";
    } else if (selectedEntry) {
        modelKey = [
            selectedEntry.id,
            selectedEntry.currentVersionId ?? "no-version",
            selectedEntry.contentUpdatedAt ?? "no-content-time"
        ].join(":");
    }
    const isLoading = isCatalogLoading || entriesQuery.isLoading;
    const invalidateEntries = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] })
        ]);
    };
    const addEntryMutation = useMutation({
        mutationFn: entryService.add,
        onSuccess: async () => {
            await invalidateEntries();
            setIsCreating(false);
            setIsModelOpen(false);
            setEditingEntry(null);
            setSelectedVersionId(null);
            messageApi.success("三才图会条目已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增失败");
        }
    });
    const updateEntryMutation = useMutation({
        mutationFn: entryService.update,
        onSuccess: async () => {
            await invalidateEntries();
            setIsModelOpen(false);
            setEditingEntry(null);
            setSelectedVersionId(null);
            messageApi.success("三才图会条目已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });
    const deleteEntryMutation = useMutation({
        mutationFn: entryService.deleteById,
        onSuccess: async () => {
            await invalidateEntries();
            setEditingEntry(null);
            setSelectedVersionId(null);
            messageApi.success("三才图会条目已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });
    const sortEntryMutation = useMutation({
        mutationFn: entryService.sort,
        onSuccess: async () => {
            await invalidateEntries();
            messageApi.success("三才图会条目顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序保存失败");
        }
    });
    const shareEntryMutation = useMutation({
        mutationFn: shareService.create,
        onSuccess: (share) => {
            if (typeof navigator.clipboard?.writeText === "function") {
                void navigator.clipboard.writeText(share.shareUrl);
                messageApi.success("分享链接已复制");
                return;
            }
            messageApi.success(`分享链接：${share.shareUrl}`);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "分享创建失败");
        }
    });
    const resetVersionMutation = useMutation({
        mutationFn: ({ entryId, versionId }: { entryId: number; versionId: number }) =>
            entryService.resetVersion(entryId, versionId),
        onSuccess: async () => {
            setSelectedVersionId(null);
            await invalidateEntries();
            modalApi.success({
                title: "三才图会版本已恢复",
                content: "已生成新的正式版本，并已将条目移动到恢复快照所在卷目的末尾。"
            });
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "版本恢复失败");
        }
    });

    const selectEntry = (entry: SancaiEntryRecord) => {
        setIsCreating(false);
        setEditingEntry(entry);
        setSelectedVersionId(null);
        setIsModelOpen(true);
    };

    const closeModel = () => {
        setIsCreating(false);
        setEditingEntry(null);
        setSelectedVersionId(null);
        setIsModelOpen(false);
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

    const shareEntry = (entry: SancaiEntryRecord) => {
        const title = entry.title?.trim() || `条目 ${entry.id}`;
        shareEntryMutation.mutate({
            targets: [
                {
                    contentId: entry.id,
                    contentType: "SANCAI_ENTRY"
                }
            ],
            title: `${title} 分享`,
            visibility: "PUBLIC"
        });
    };

    const resetVersion = (version: SancaiContentVersionRecord) => {
        if (!selectedEntry?.id) {
            return;
        }
        confirm.danger({
            title: "恢复三才图会版本",
            message: `确认恢复版本 ${version.versionNo ?? version.id}？`,
            description: "恢复后会产生新的正式版本，并刷新条目详情、列表和版本历史。",
            okText: "恢复",
            onConfirm: () =>
                resetVersionMutation.mutateAsync({
                    entryId: selectedEntry.id,
                    versionId: version.id
                })
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
        <>
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
                onShare={shareEntry}
                onSort={sortEntry}
                onView={selectEntry}
            />
            <SancaiEntryModel
                key={modelKey}
                entry={selectedEntry}
                isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                mode={isCreating ? "create" : "edit"}
                open={isModelOpen && !isLoading}
                onCancel={closeModel}
                onSubmit={submitEntry}
                afterForm={
                    !isCreating && selectedEntry ? (
                        <SancaiVersionHistoryPanel
                            currentEntry={selectedEntry}
                            detailLoading={versionDetailQuery.isLoading}
                            listLoading={versionsQuery.isLoading}
                            resetting={resetVersionMutation.isPending}
                            selectedVersion={selectedVersion}
                            versions={versions}
                            onSelectVersion={(version) => setSelectedVersionId(version.id)}
                            onResetVersion={resetVersion}
                        />
                    ) : null
                }
            />
        </>
    );
};
