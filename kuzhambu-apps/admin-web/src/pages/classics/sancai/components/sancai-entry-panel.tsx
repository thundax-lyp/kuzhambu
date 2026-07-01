import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Card } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import { ClassicsShowcaseJobSection } from "@/pages/classics/common/components/classics-showcase-job-section";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/components/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/components/classics-content-tag-panel";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryModel } from "./sancai-entry-model";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import { SancaiVersionHistoryPanel } from "./sancai-version-history-panel";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord,
    SancaiVolumeRecord
} from "../sancai-types";

const EXPORT_PAGE_SIZE = 8;
const SHOWCASE_PAGE_SIZE = 8;
const TASK_POLL_INTERVAL_MS = 3000;
const DEFAULT_REFINEMENT_MODEL_ID = 1;
const DEFAULT_REFINEMENT_MODEL_NAME = "gpt-5.5";
const DEFAULT_REFINEMENT_SERVICE_ROLE = "PRIMARY";

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const buildPromptMessagesJson = (capability: "translate" | "summary", entry: SancaiEntryRecord) => {
    if (capability === "translate") {
        return JSON.stringify([
            {
                role: "system",
                content: "你是古籍翻译助手，请输出可直接展示的现代汉语译文。"
            },
            {
                role: "user",
                content: entry.originalText?.trim() || ""
            }
        ]);
    }
    return JSON.stringify([
        {
            role: "system",
            content: "你是古籍摘要助手，请输出可直接展示的简明中文摘要。"
        },
        {
            role: "user",
            content: JSON.stringify({
                title: entry.title,
                originalText: entry.originalText,
                translationText: entry.translationText
            })
        }
    ]);
};

const buildInputPayloadJson = (capability: "translate" | "summary", entry: SancaiEntryRecord) => {
    return JSON.stringify({
        capability,
        contentId: entry.id,
        contentType: "SANCAI_ENTRY",
        originalText: entry.originalText,
        summary: entry.summary,
        title: entry.title,
        translationText: entry.translationText
    });
};

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
    const [creatingRefinementCapability, setCreatingRefinementCapability] = useState<
        "translate" | "summary" | null
    >(null);
    const handledSucceededTaskIdsRef = useRef<Set<number>>(new Set());
    const [isCreating, setIsCreating] = useState(defaultCreateOpen);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const [editingEntry, setEditingEntry] = useState<SancaiEntryRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);
    const currentUserQuery = useQuery({
        queryKey: ["sys", "current-user", "info"],
        queryFn: currentUserService.getCurrentUserInfo,
        retry: false
    });
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
    const selectedEntryId = selectedEntry?.id ?? null;
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
    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "sancai", "refinement", "tasks", selectedEntry?.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                contentType: "SANCAI_ENTRY",
                contentId: selectedEntry?.id ?? 0,
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
    const showcasesQuery = useQuery({
        queryKey: ["classics", "sancai", "showcases", "jobs"],
        queryFn: () =>
            entryService.pageShowcases({
                pageNo: 1,
                pageSize: SHOWCASE_PAGE_SIZE
            }),
        retry: false
    });
    const showcaseJobs = showcasesQuery.data?.records || [];
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
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "exports", "jobs"]
        });
    };
    const invalidateShowcaseJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "showcases", "jobs"]
        });
    };
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
    const invalidateSancaiContentGovernance = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "SANCAI_ENTRY"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "SANCAI_ENTRY"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "SANCAI_ENTRY", selectedEntryId]
            })
        ]);
    }, [queryClient, selectedEntryId]);
    const invalidateRefinementTasks = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "refinement", "tasks", selectedEntry?.id]
        });
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
            messageApi.success("导出任务已提交，请到下方任务列表查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "导出提交失败");
        }
    });
    const showcaseEntryMutation = useMutation({
        mutationFn: (entry: SancaiEntryRecord) => {
            const title = `${readEntryTitle(entry)} 静态展示`;
            return entryService.requestShowcase({
                scopeJson: JSON.stringify({
                    title,
                    entries: [
                        {
                            id: entry.id,
                            title: entry.title,
                            volumeId: entry.volumeId
                        }
                    ]
                }),
                entryCount: 1,
                visibilityRiskStatus: "PUBLIC_ONLY"
            });
        },
        onSuccess: async () => {
            await invalidateShowcaseJobs();
            messageApi.success("三才静态展示任务已提交，请到下方任务列表查看进度。");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "静态展示提交失败");
        }
    });
    const createRefinementTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onMutate: (command) => {
            if (command.capability === "translate" || command.capability === "summary") {
                setCreatingRefinementCapability(command.capability);
            }
        },
        onSuccess: async (_, command) => {
            await invalidateRefinementTasks();
            messageApi.success(
                command.capability === "translate" ? "译文任务已创建" : "摘要任务已创建"
            );
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 精修任务创建失败");
        },
        onSettled: () => {
            setCreatingRefinementCapability(null);
        }
    });
    const updateVisualAssetMutation = useMutation({
        mutationFn: entryService.updateVisualAsset,
        onSuccess: async () => {
            await Promise.all([refreshSancaiEntryDetail(), invalidateEntries()]);
            messageApi.success("三才视觉资产已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉资产保存失败");
        }
    });
    const changeCurrentVisualAssetMutation = useMutation({
        mutationFn: entryService.changeCurrentVisualAsset,
        onSuccess: async () => {
            await Promise.all([refreshSancaiEntryDetail(), invalidateEntries()]);
            messageApi.success("当前视觉资产版本已切换");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉资产切换失败");
        }
    });

    useEffect(() => {
        const newlySucceededTaskIds = refinementTasks
            .filter(
                (task) =>
                    (task.status === "SUCCEEDED" || task.status === "PARTIAL") &&
                    typeof task.taskId === "number" &&
                    !handledSucceededTaskIdsRef.current.has(task.taskId)
            )
            .map((task) => task.taskId);
        if (!newlySucceededTaskIds.length) {
            return;
        }
        newlySucceededTaskIds.forEach((taskId) => handledSucceededTaskIdsRef.current.add(taskId));
        void Promise.all([
            refreshSancaiEntryDetail(),
            invalidateEntries(),
            invalidateSancaiContentGovernance()
        ]);
    }, [
        invalidateEntries,
        invalidateSancaiContentGovernance,
        refreshSancaiEntryDetail,
        refinementTasks
    ]);

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
        handledSucceededTaskIdsRef.current.clear();
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

    const exportEntry = (entry: SancaiEntryRecord) => {
        exportEntryMutation.mutate(entry);
    };
    const showcaseEntry = (entry: SancaiEntryRecord) => {
        if (!entry.id) {
            return;
        }
        showcaseEntryMutation.mutate(entry);
    };

    const createRefinementTask = (capability: "translate" | "summary") => {
        if (!selectedEntry?.id) {
            return;
        }
        if (!currentUserQuery.data?.id) {
            messageApi.warning("当前用户信息尚未加载完成");
            return;
        }
        if (!selectedEntry.originalText?.trim()) {
            messageApi.warning("当前条目缺少原文，无法创建 AI 精修任务");
            return;
        }
        createRefinementTaskMutation.mutate({
            capability,
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: selectedEntry.id,
            objectId: null,
            requestedBy: Number(currentUserQuery.data.id),
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId("sancai-task"),
            traceId: createEventId("sancai-trace"),
            promptMessagesJson: buildPromptMessagesJson(capability, selectedEntry),
            promptVariablesJson: JSON.stringify({
                title: selectedEntry.title
            }),
            inputPayloadJson: buildInputPayloadJson(capability, selectedEntry),
            locale: "zh-CN"
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
    const updateVisualAsset = (asset: SancaiVisualAssetRecord) => {
        updateVisualAssetMutation.mutate({
            visualAssetId: asset.visualAssetId ?? asset.id ?? null,
            entryId: asset.entryId ?? selectedEntryId,
            versionNo: asset.versionNo,
            status: asset.status,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId,
            generatedImageStorageObjectId: asset.generatedImageStorageObjectId,
            currentUsed: asset.currentUsed,
            textWeight: asset.textWeight,
            imageWeight: asset.imageWeight,
            imageAnalysisMarkdown: asset.imageAnalysisMarkdown,
            fusionDescription: asset.fusionDescription,
            visualDescription: asset.visualDescription,
            generationParamsJson: asset.generationParamsJson
        });
    };
    const switchVisualAsset = (asset: SancaiVisualAssetRecord) => {
        const visualAssetId = asset.visualAssetId ?? asset.id;
        const entryId = asset.entryId ?? selectedEntryId;
        if (!visualAssetId || !entryId) {
            return;
        }
        changeCurrentVisualAssetMutation.mutate({
            entryId,
            visualAssetId
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
                    title="三才图会条目加载失败"
                    description="请确认后台条目接口可用后刷新页面。"
                />
            ) : null}
            {showcasesQuery.isError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="静态展示任务列表加载失败"
                    description="请确认后台静态展示任务接口可用后刷新页面。"
                />
            ) : null}
            {exportsQuery.isError ? (
                <Alert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="导出任务列表加载失败"
                    description="请确认后台导出任务接口可用后刷新页面。"
                />
            ) : null}
            <ClassicsExportJobSection
                items={exportJobs}
                loading={exportsQuery.isLoading || exportEntryMutation.isPending}
                onDownload={(job) => {
                    if (job.downloadUrl) {
                        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                    }
                }}
                onRefresh={() => {
                    void invalidateExportJobs();
                }}
            />
            <ClassicsShowcaseJobSection
                items={showcaseJobs}
                loading={showcasesQuery.isLoading || showcaseEntryMutation.isPending}
                onDownload={(job) => {
                    if (job.downloadUrl) {
                        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                    }
                }}
                onRefresh={() => {
                    void invalidateShowcaseJobs();
                }}
            />
            <SancaiEntryList
                entries={entries}
                isLoading={isLoading || sortEntryMutation.isPending}
                volumes={volumes}
                onDelete={deleteEntry}
                onExport={exportEntry}
                onShowcase={showcaseEntry}
                onShare={shareEntry}
                onSort={sortEntry}
                onView={selectEntry}
            />
            <SancaiEntryModel
                key={modelKey}
                entry={selectedEntry}
                isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                isSwitchingVisualAsset={changeCurrentVisualAssetMutation.isPending}
                isUpdatingVisualAsset={updateVisualAssetMutation.isPending}
                mode={isCreating ? "create" : "edit"}
                open={isModelOpen && !isLoading}
                onCancel={closeModel}
                onSubmit={submitEntry}
                onUseVisualAsset={switchVisualAsset}
                onUpdateVisualAsset={updateVisualAsset}
                afterForm={
                    !isCreating && selectedEntry ? (
                        <>
                            <Card size="small" title="AI 精修任务">
                                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                                    <Button
                                        type="primary"
                                        loading={creatingRefinementCapability === "translate"}
                                        onClick={() => createRefinementTask("translate")}
                                    >
                                        创建译文任务
                                    </Button>
                                    <Button
                                        loading={creatingRefinementCapability === "summary"}
                                        onClick={() => createRefinementTask("summary")}
                                    >
                                        创建摘要任务
                                    </Button>
                                </div>
                                <div style={{ marginTop: 12, display: "grid", gap: 8 }}>
                                    {refinementTasks
                                        .filter(
                                            (task) =>
                                                task.capability === "translate" ||
                                                task.capability === "summary"
                                        )
                                        .slice(0, 4)
                                        .map((task) => (
                                            <div key={task.taskId}>
                                                {task.capability}：{task.status}
                                                {task.resultPreview
                                                    ? ` / ${task.resultPreview}`
                                                    : ""}
                                            </div>
                                        ))}
                                </div>
                            </Card>
                            <AiCandidatePanel
                                capabilities={["translate", "summary", "tags", "qa"]}
                                contentId={selectedEntry.id}
                                contentType="SANCAI_ENTRY"
                                onApplied={async () => {
                                    await Promise.all([
                                        refreshSancaiEntryDetail(),
                                        invalidateEntries(),
                                        invalidateSancaiContentGovernance()
                                    ]);
                                }}
                            />
                            <ClassicsContentTagPanel
                                contentId={selectedEntry.id}
                                contentType="SANCAI_ENTRY"
                                panelTitle="三才图会标签治理"
                                onChanged={invalidateSancaiContentGovernance}
                            />
                            <ClassicsContentQaPanel
                                contentId={selectedEntry.id}
                                contentType="SANCAI_ENTRY"
                                panelTitle="三才图会问答对治理"
                                onChanged={invalidateSancaiContentGovernance}
                            />
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
                        </>
                    ) : null
                }
            />
        </>
    );
};
