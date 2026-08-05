import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { type KuzhambuTableSortPosition, KuzhambuAlert } from "@/components";

import { hasPermission } from "@/auth/permission-storage";
import { isSameId } from "@/types/id";
import * as exportService from "@/pages/classics/common/classics-export-service";
import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";
import { AiCandidateBatchDrawer } from "@/pages/classics/common/ai-candidate-batch-drawer";
import { hasClassicsContentPermission } from "@/pages/classics/common/classics-content-types";
import { SancaiEntryList, type SancaiPublicationAction } from "./sancai-entry-list";
import { SancaiEntryEditDrawer } from "./sancai-entry-edit-drawer";
import { SancaiEntryExportActions } from "./sancai-entry-export-actions";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/sancai-entry-panel/sancai-entry-edit-drawer/sancai-entry-edit-drawer-form-values";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiCategoryRecord,
    SancaiEntryRecord,
    SancaiPublicationBatchRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";

import "./sancai-entry-panel.css";

const EXPORT_PAGE_SIZE = 8;

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

interface SancaiEntryPanelProps {
    categories?: SancaiCategoryRecord[];
    categoryId: string | null;
    defaultCreateOpen?: boolean;
    exportJobsDrawerOpen?: boolean;
    isCatalogLoading: boolean;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    refreshVersion: number;
    volumeId: string | null;
    volumes: SancaiVolumeRecord[];
    onExportJobsDrawerOpenChange?: (open: boolean) => void;
}

export const SancaiEntryPanel = ({
    categories = [],
    categoryId,
    defaultCreateOpen = false,
    exportJobsDrawerOpen,
    isCatalogLoading,
    keyword,
    lifecycleStatus,
    refreshVersion,
    volumeId,
    volumes,
    onExportJobsDrawerOpenChange
}: SancaiEntryPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const navigate = useNavigate();
    const [isCreating, setIsCreating] = useState(defaultCreateOpen);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const [editingEntry, setEditingEntry] = useState<SancaiEntryRecord | null>(null);
    const [batchCandidateContentIds, setBatchCandidateContentIds] = useState<string[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<string, string>>(
        {}
    );
    const [batchCandidateDrawerOpen, setBatchCandidateDrawerOpen] = useState(false);
    const [publicationBatchResult, setPublicationBatchResult] =
        useState<SancaiPublicationBatchRecord | null>(null);
    const [internalExportJobsDrawerOpen, setInternalExportJobsDrawerOpen] = useState(false);
    const categoryOptions = useMemo(
        () =>
            categories.map((category) => ({
                label: category.title?.trim() || `门类 ${category.id}`,
                value: category.id
            })),
        [categories]
    );
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
        queryFn: () => entryService.get(editingEntry?.id ?? ""),
        enabled: isModelOpen && !isCreating && Boolean(editingEntry?.id),
        retry: false
    });
    const selectedEntry = isCreating ? undefined : (detailQuery.data ?? editingEntry ?? undefined);
    const isSelectedEntryReadOnly = Boolean(
        selectedEntry?.transitionStatus && selectedEntry.transitionStatus !== "NONE"
    );
    const selectedEntryId = selectedEntry?.id ?? null;
    const canEditEntries = hasClassicsContentPermission("SANCAI_ENTRY", "edit", hasPermission);
    const canManageGeneratedArtifacts = hasClassicsContentPermission(
        "SANCAI_ENTRY",
        "export",
        hasPermission
    );
    const exportsQuery = useQuery({
        queryKey: ["classics", "sancai", "exports", "jobs"],
        queryFn: () =>
            exportService.page({
                pageNo: 1,
                pageSize: EXPORT_PAGE_SIZE,
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET"
            }),
        retry: false
    });
    const exportJobs = exportsQuery.data?.records || [];
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
    const invalidateEntries = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] }),
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "exports", "jobs"] })
        ]);
    }, [queryClient]);
    const refreshAfterPublicationChange = useCallback(
        async (entryId: string) => {
            const refreshes = [invalidateEntries()];
            if (isModelOpen && !isCreating && selectedEntry?.id === entryId) {
                refreshes.push(
                    queryClient.invalidateQueries({
                        queryKey: ["classics", "sancai", "entries", "detail", entryId]
                    }),
                    queryClient.invalidateQueries({
                        queryKey: ["classics", "sancai", "entries", "versions", entryId]
                    })
                );
            }
            await Promise.all(refreshes);
        },
        [invalidateEntries, isCreating, isModelOpen, queryClient, selectedEntry?.id]
    );
    const refreshSancaiEntryDetail = useCallback(async () => {
        if (!selectedEntryId) {
            return;
        }
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "detail", selectedEntryId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "versions", selectedEntryId]
            })
        ]);
    }, [queryClient, selectedEntryId]);
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "exports", "jobs"]
        });
    };

    const invalidateBatchCandidateData = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] }),
            refreshSancaiEntryDetail(),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "SANCAI_ENTRY", selectedEntryId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "SANCAI_ENTRY"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "SANCAI_ENTRY", selectedEntryId]
            })
        ]);
    }, [queryClient, refreshSancaiEntryDetail, selectedEntryId]);

    const openBatchCandidateDrawer = (selectedEntries: SancaiEntryRecord[]) => {
        if (!selectedEntries.length) {
            return;
        }
        const contentIds = selectedEntries.map((entry) => entry.id);
        const contentTitleById = Object.fromEntries(
            selectedEntries.map((entry) => [entry.id, readEntryTitle(entry)])
        );

        setBatchCandidateContentIds(contentIds);
        setBatchCandidateTitleById(contentTitleById);
        setBatchCandidateDrawerOpen(true);
    };
    const addEntryMutation = useMutation({
        mutationFn: entryService.add,
        onSuccess: async () => {
            await invalidateEntries();
            setIsCreating(false);
            setIsModelOpen(false);
            setEditingEntry(null);
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
            messageApi.success("三才图会条目已保存，归属卷已更新");
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
    const publicationMutation = useMutation({
        mutationFn: ({
            entry,
            action
        }: {
            entry: SancaiEntryRecord;
            action: SancaiPublicationAction;
        }) =>
            action === "PUBLISH"
                ? entryService.publish({ id: entry.id })
                : entryService.submitOffline({ id: entry.id }),
        onSuccess: async (result) => {
            await refreshAfterPublicationChange(result.contentId);
            messageApi.success("发布状态变更请求已接受");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "发布状态变更失败");
        }
    });
    const publicationBatchMutation = useMutation({
        mutationFn: ({
            entries,
            action
        }: {
            entries: SancaiEntryRecord[];
            action: SancaiPublicationAction;
        }) => {
            const command = { ids: entries.map((entry) => entry.id) };
            return action === "PUBLISH"
                ? entryService.publishBatch(command)
                : entryService.submitOfflineBatch(command);
        },
        onSuccess: async (result) => {
            setPublicationBatchResult(result);
            await invalidateEntries();
            messageApi.success(
                `批量请求完成：接受 ${result.acceptedCount}，拒绝 ${result.rejectedCount}`
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量发布状态变更失败");
        }
    });
    const exportEntryMutation = useMutation({
        mutationFn: (entry: SancaiEntryRecord) => {
            const title = `${readEntryTitle(entry)} 导出`;
            return exportService.create({
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: JSON.stringify({
                    title,
                    ids: [entry.id]
                })
            });
        },
        onSuccess: async () => {
            await invalidateExportJobs();
            messageApi.success("导出任务已提交，请到任务抽屉查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出提交失败");
        }
    });
    const deleteExportMutation = useMutation({
        mutationFn: exportService.deleteById,
        onSuccess: async () => {
            await invalidateExportJobs();
            messageApi.success("导出记录已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出记录删除失败");
        }
    });
    const selectEntry = (entry: SancaiEntryRecord) => {
        setIsCreating(false);
        setEditingEntry(entry);
        setIsModelOpen(true);
    };

    const closeModel = () => {
        setIsCreating(false);
        setEditingEntry(null);
        setIsModelOpen(false);
    };

    const submitEntry = (form: SancaiEntryFormValues) => {
        if (isCreating) {
            if (!form.volumeId) {
                messageApi.warning("请先选择卷目");
                return;
            }
            addEntryMutation.mutate({
                volumeId: form.volumeId,
                title: form.title,
                originalText: form.originalText,
                translationText: form.translationText,
                summary: form.summary,
                lifecycleStatus: "DRAFT",
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
            volumeId: form.volumeId,
            title: form.title,
            originalText: form.originalText,
            translationText: form.translationText,
            summary: form.summary,
            lifecycleStatus: selectedEntry.lifecycleStatus,
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
    const changePublicationStatus = (entry: SancaiEntryRecord, action: SancaiPublicationAction) => {
        const actionText = action === "PUBLISH" ? "发布" : "下线";
        confirm.danger({
            title: `${actionText}三才图会条目`,
            message: `确认${actionText} ${readEntryTitle(entry)}？`,
            description: "请求提交后由后台任务异步同步搜索与知识库状态。",
            okText: actionText,
            onConfirm: () => publicationMutation.mutateAsync({ entry, action })
        });
    };
    const changePublicationStatusBatch = (
        selectedEntries: SancaiEntryRecord[],
        action: SancaiPublicationAction
    ) => {
        const actionText = action === "PUBLISH" ? "发布" : "下线";
        confirm.danger({
            title: `批量${actionText}三才图会条目`,
            message: `确认${actionText}选中的 ${selectedEntries.length} 条稿件？`,
            description: "服务端将逐条受理，并返回每条稿件的接受或拒绝原因。",
            okText: `批量${actionText}`,
            onConfirm: () =>
                publicationBatchMutation.mutateAsync({ entries: selectedEntries, action })
        });
    };

    const exportEntry = (entry: SancaiEntryRecord) => {
        exportEntryMutation.mutate(entry);
    };
    const openVisualPage = (entry: SancaiEntryRecord) => {
        navigate(`/classics/sancai/visual?entryId=${encodeURIComponent(entry.id)}`);
    };
    const deleteExportJob = (job: ClassicsExportJobRecord) => {
        if (!job.id) {
            return;
        }
        confirm.danger({
            title: "删除导出记录",
            message: `确认删除导出任务 #${job.id}？`,
            description:
                "删除后该记录会从列表移除，并释放其导出产物引用；已无引用的文件对象会进入 Storage 删除流程。",
            okText: "删除",
            onConfirm: () => deleteExportMutation.mutateAsync(job.id ?? "")
        });
    };
    const deleteExportJobs = (jobs: ClassicsExportJobRecord[]) => {
        const ids = jobs.map((job) => job.id).filter((id): id is string => Boolean(id));
        if (!ids.length) {
            return;
        }
        confirm.danger({
            title: "批量删除导出记录",
            message: `确认删除 ${ids.length} 条导出记录？`,
            description: "批量删除逐条释放导出产物引用；单条仍被其他业务引用的文件不会被强制删除。",
            okText: "删除",
            onConfirm: async () => {
                await Promise.all(ids.map((id) => exportService.deleteById(id)));
                await invalidateExportJobs();
                messageApi.success(`已删除 ${ids.length} 条导出记录`);
            }
        });
    };
    const sortEntry = (
        sourceEntry: SancaiEntryRecord,
        targetEntry: SancaiEntryRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (isSameId(sourceEntry.id, targetEntry.id)) {
            return;
        }
        const remainingEntries = entries.filter((entry) => !isSameId(entry.id, sourceEntry.id));
        const targetIndex = remainingEntries.findIndex((entry) =>
            isSameId(entry.id, targetEntry.id)
        );
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
    const isExportJobsDrawerOpen = exportJobsDrawerOpen ?? internalExportJobsDrawerOpen;
    const setExportJobsOpen = onExportJobsDrawerOpenChange ?? setInternalExportJobsDrawerOpen;

    return (
        <>
            {entriesQuery.isError ? (
                <KuzhambuAlert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="三才图会条目加载失败"
                    description="请确认后台条目接口可用后刷新页面。"
                />
            ) : null}
            <SancaiEntryList
                entries={entries}
                isLoading={isLoading || sortEntryMutation.isPending}
                volumes={volumes}
                onPublicationAction={changePublicationStatus}
                onPublicationBatch={changePublicationStatusBatch}
                publicationBatchResult={publicationBatchResult}
                onDelete={deleteEntry}
                onExport={exportEntry}
                onRefresh={() => {
                    void entriesQuery.refetch();
                }}
                onBatchCandidateGovernance={openBatchCandidateDrawer}
                onVisual={openVisualPage}
                onSort={sortEntry}
                onView={selectEntry}
            />
            <SancaiEntryExportActions
                canManageGeneratedArtifacts={canManageGeneratedArtifacts}
                exportJobs={exportJobs}
                isError={exportsQuery.isError}
                loading={
                    exportsQuery.isLoading ||
                    exportEntryMutation.isPending ||
                    deleteExportMutation.isPending
                }
                open={isExportJobsDrawerOpen}
                onBatchDelete={deleteExportJobs}
                onClose={() => setExportJobsOpen(false)}
                onDelete={deleteExportJob}
                onRefresh={() => {
                    void invalidateExportJobs();
                }}
            />
            <AiCandidateBatchDrawer
                contentIds={batchCandidateContentIds}
                capabilities={[
                    "translate",
                    "summary",
                    "tags",
                    "qa",
                    "image_analysis",
                    "fusion",
                    "visual",
                    "image_gen"
                ]}
                contentTitleById={batchCandidateTitleById}
                contentType="SANCAI_ENTRY"
                canEdit={canEditEntries}
                open={batchCandidateDrawerOpen}
                onChanged={invalidateBatchCandidateData}
                onClose={() => setBatchCandidateDrawerOpen(false)}
            />
            <SancaiEntryEditDrawer
                key={modelKey}
                categoryOptions={categoryOptions}
                entry={selectedEntry}
                isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                initialCategoryId={categoryId}
                initialVolumeId={volumeId}
                mode={isCreating ? "create" : "edit"}
                open={isModelOpen && !isLoading}
                readOnly={isSelectedEntryReadOnly}
                volumes={volumes}
                onCancel={closeModel}
                onSubmit={submitEntry}
                onEntryChanged={invalidateEntries}
            />
        </>
    );
};
