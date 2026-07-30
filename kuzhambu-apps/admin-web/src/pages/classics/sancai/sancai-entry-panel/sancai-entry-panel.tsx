import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { type KuzhambuTableSortPosition, KuzhambuAlert } from "@/components";

import { hasPermission } from "@/auth/permission-storage";
import { isSameId } from "@/types/id";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";
import { ClassicsContentQaPanel } from "@/pages/classics/common/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/classics-content-tag-panel";
import { AiCandidateBatchDrawer } from "@/pages/classics/common/ai-candidate-batch-drawer";
import { hasClassicsContentPermission } from "@/pages/classics/common/classics-content-types";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryEditDrawer } from "../components/sancai-entry-edit-drawer";
import { SancaiEntryExportActions } from "../sancai-entry-export-actions";
import { SancaiEntryVersionSection } from "../sancai-entry-version-section";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";
import { useSancaiEntryPanelState } from "@/pages/classics/sancai/hooks/use-sancai-entry-panel-state";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiCategoryRecord,
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";

import "./sancai-entry-panel.css";

const EXPORT_PAGE_SIZE = 8;
const TASK_POLL_INTERVAL_MS = 3000;

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

interface SancaiEntryLifecycleAction {
    confirmDescription: string;
    confirmMessage: string;
    confirmTitle: string;
    okText: string;
    successMessage: string;
    targetStatus: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}

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
    const { message: messageApi, modal: modalApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const navigate = useNavigate();
    const [isCreating, setIsCreating] = useState(defaultCreateOpen);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const [editingEntry, setEditingEntry] = useState<SancaiEntryRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<string | null>(null);
    const [batchCandidateContentIds, setBatchCandidateContentIds] = useState<string[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<string, string>>(
        {}
    );
    const [batchCandidateDrawerOpen, setBatchCandidateDrawerOpen] = useState(false);
    const [internalExportJobsDrawerOpen, setInternalExportJobsDrawerOpen] = useState(false);
    const tagPanelRef = useRef<HTMLDivElement | null>(null);
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
    const selectedEntryId = selectedEntry?.id ?? null;
    const versionsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "versions", selectedEntry?.id],
        queryFn: () => entryService.listVersions(selectedEntry?.id ?? ""),
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
        queryFn: () => entryService.getVersion(selectedEntry?.id ?? "", selectedVersionId ?? ""),
        enabled: isModelOpen && Boolean(selectedEntry?.id && selectedVersionId),
        retry: false
    });
    const versions = versionsQuery.data || [];
    const selectedVersion =
        versionDetailQuery.data ||
        versions.find((version) => isSameId(version.id, selectedVersionId)) ||
        null;
    const canChangeEntryVisibility = hasClassicsContentPermission(
        "SANCAI_ENTRY",
        "edit",
        hasPermission
    );
    const canManageGeneratedArtifacts = hasClassicsContentPermission(
        "SANCAI_ENTRY",
        "export",
        hasPermission
    );
    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "sancai", "refinement", "tasks", selectedEntry?.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                contentType: "SANCAI_ENTRY",
                contentId: selectedEntry?.id ?? "",
                pageNo: 1,
                pageSize: 20
            }),
        enabled: isModelOpen && Boolean(selectedEntry?.id),
        retry: false,
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                ? TASK_POLL_INTERVAL_MS
                : false;
        }
    });
    const refinementTasks = useMemo(
        () => refinementTasksQuery.data?.items || [],
        [refinementTasksQuery.data?.items]
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
    const refreshAfterLifecycleChange = useCallback(
        async (entryId: string) => {
            const refreshes = [invalidateEntries()];
            if (isModelOpen && !isCreating && selectedEntry?.id === entryId) {
                refreshes.push(
                    queryClient.invalidateQueries({
                        queryKey: ["classics", "sancai", "entries", "detail", entryId]
                    }),
                    queryClient.invalidateQueries({
                        queryKey: ["classics", "sancai", "entries", "versions", entryId]
                    }),
                    queryClient.invalidateQueries({
                        queryKey: [
                            "classics",
                            "sancai",
                            "entries",
                            "version",
                            entryId,
                            selectedVersionId
                        ]
                    })
                );
            }
            await Promise.all(refreshes);
        },
        [
            invalidateEntries,
            isCreating,
            isModelOpen,
            queryClient,
            selectedEntry?.id,
            selectedVersionId
        ]
    );
    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "refinement", "tasks", selectedEntry?.id]
        });
    };
    const {
        creatingRefinementCapability,
        invalidateSancaiContentGovernance,
        refreshSancaiEntryDetail,
        createRefinementTask,
        resetHandledSucceededTaskIds
    } = useSancaiEntryPanelState({
        queryClient,
        messageApi,
        selectedEntry,
        selectedEntryId,
        refinementTasks,
        invalidateEntries,
        invalidateRefinementTasks
    });
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
    const changeLifecycleStatusMutation = useMutation({
        mutationFn: entryService.changeLifecycleStatus,
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "生命周期变更失败");
        }
    });
    const resetVersionMutation = useMutation({
        mutationFn: ({ entryId, versionId }: { entryId: string; versionId: string }) =>
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
        setSelectedVersionId(null);
        setIsModelOpen(true);
    };

    const closeModel = () => {
        setIsCreating(false);
        setEditingEntry(null);
        setSelectedVersionId(null);
        resetHandledSucceededTaskIds();
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
            volumeId: form.volumeId,
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
    const changeLifecycleStatus = (
        entry: SancaiEntryRecord,
        action: SancaiEntryLifecycleAction
    ) => {
        confirm.danger({
            title: action.confirmTitle,
            message: action.confirmMessage,
            description: action.confirmDescription,
            okText: action.okText,
            onConfirm: async () => {
                await changeLifecycleStatusMutation.mutateAsync({
                    id: entry.id,
                    lifecycleStatus: action.targetStatus
                });
                await refreshAfterLifecycleChange(entry.id);
                messageApi.success(action.successMessage);
            }
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
                onChangeLifecycleStatus={changeLifecycleStatus}
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
                canEdit={canChangeEntryVisibility}
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
                volumes={volumes}
                onCancel={closeModel}
                onSubmit={submitEntry}
                onCreateTranslationTask={(draft) => createRefinementTask("translate", null, draft)}
                onCreateSummaryTask={(draft) => createRefinementTask("summary", null, draft)}
                isCreatingTranslationTask={creatingRefinementCapability === "translate"}
                isCreatingSummaryTask={creatingRefinementCapability === "summary"}
                translationTasks={refinementTasks.filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "translate"
                )}
                summaryTasks={refinementTasks.filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "summary"
                )}
                qaContent={
                    !isCreating && selectedEntry ? (
                        <ClassicsContentQaPanel
                            contentId={selectedEntry.id}
                            contentType="SANCAI_ENTRY"
                            panelTitle="三才图会问答对治理"
                            onChanged={invalidateSancaiContentGovernance}
                        />
                    ) : null
                }
                tagContent={
                    !isCreating && selectedEntry ? (
                        <div
                            ref={tagPanelRef}
                            className="sancai-candidate-panel-anchor"
                            tabIndex={-1}
                        >
                            <ClassicsContentTagPanel
                                contentId={selectedEntry.id}
                                contentType="SANCAI_ENTRY"
                                panelTitle="当前条目标签"
                                onChanged={invalidateSancaiContentGovernance}
                            />
                        </div>
                    ) : null
                }
                versionContent={
                    <SancaiEntryVersionSection
                        currentEntry={selectedEntry}
                        detailLoading={versionDetailQuery.isLoading}
                        isCreating={isCreating}
                        listLoading={versionsQuery.isLoading}
                        resetting={resetVersionMutation.isPending}
                        selectedVersion={selectedVersion}
                        versions={versions}
                        onSelectVersion={(version) => setSelectedVersionId(version.id)}
                        onResetVersion={resetVersion}
                    />
                }
            />
        </>
    );
};
