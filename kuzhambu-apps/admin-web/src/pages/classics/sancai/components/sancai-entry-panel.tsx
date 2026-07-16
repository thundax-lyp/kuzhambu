import { UploadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Badge, Card, Empty, Image, Typography, Upload } from "antd";
import { useCallback, useMemo, useRef, useState } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { hasPermission } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import { AiRefinementStreamPanel } from "@/pages/classics/common/components/ai-refinement-stream-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/components/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/components/classics-content-tag-panel";
import { AiCandidateBatchDrawer } from "@/pages/classics/common/components/ai-candidate-batch-drawer";
import { hasClassicsContentPermission } from "@/pages/classics/common/classics-content-types";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryModel } from "./sancai-entry-model";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import { SancaiVersionHistoryPanel } from "./sancai-version-history-panel";
import { useSancaiEntryPanelState } from "../hooks/use-sancai-entry-panel-state";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiCategoryRecord,
    SancaiContentVersionRecord,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord,
    SancaiVolumeRecord
} from "../sancai-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;

const EXPORT_PAGE_SIZE = 8;
const TASK_POLL_INTERVAL_MS = 3000;
const IMAGE_ACCEPT = ".jpg,.jpeg,.png,.gif,.webp";

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
};

interface SancaiEntryLifecycleAction {
    confirmDescription: string;
    confirmMessage: string;
    confirmTitle: string;
    okText: string;
    successMessage: string;
    targetStatus: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}

const formatImageSize = (size?: number | null) => {
    if (!size) {
        return "-";
    }
    if (size < 1024) {
        return `${size} B`;
    }
    if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`;
    }
    return `${(size / 1024 / 1024).toFixed(1)} MB`;
};

const resolveImagePreviewUrl = (entryId: number | null, image: SancaiEntryImageRecord) => {
    if (!entryId || !image.id) {
        return undefined;
    }
    return toAuthenticatedResourceUrl(
        entryService.getImageContentUrl({
            entryId,
            imageId: image.id,
            mode: "preview"
        })
    );
};

interface SancaiEntryPanelProps {
    categories?: SancaiCategoryRecord[];
    categoryId: number | null;
    defaultCreateOpen?: boolean;
    exportJobsDrawerOpen?: boolean;
    isCatalogLoading: boolean;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    refreshVersion: number;
    volumeId: number | null;
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
    const [isCreating, setIsCreating] = useState(defaultCreateOpen);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const [editingEntry, setEditingEntry] = useState<SancaiEntryRecord | null>(null);
    const [selectedVersionId, setSelectedVersionId] = useState<number | null>(null);
    const [batchCandidateContentIds, setBatchCandidateContentIds] = useState<number[]>([]);
    const [batchCandidateTitleById, setBatchCandidateTitleById] = useState<Record<number, string>>(
        {}
    );
    const [batchCandidateDrawerOpen, setBatchCandidateDrawerOpen] = useState(false);
    const [internalExportJobsDrawerOpen, setInternalExportJobsDrawerOpen] = useState(false);
    const candidatePanelRef = useRef<HTMLDivElement | null>(null);
    const tagPanelRef = useRef<HTMLDivElement | null>(null);
    const categoryOptions = useMemo(
        () =>
            categories.map((category) => ({
                label: category.title?.trim() || `门类 ${category.id}`,
                value: category.id
            })),
        [categories]
    );
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
    const imagesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "images", selectedEntryId],
        queryFn: () => entryService.listImages(selectedEntryId ?? 0),
        enabled: isModelOpen && !isCreating && Boolean(selectedEntryId),
        retry: false
    });
    const entryImages = useMemo(
        () =>
            [...(imagesQuery.data || [])].sort((left, right) => {
                if ((left.priority ?? 0) !== (right.priority ?? 0)) {
                    return (left.priority ?? 0) - (right.priority ?? 0);
                }
                return left.id - right.id;
            }),
        [imagesQuery.data]
    );
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
        async (entryId: number) => {
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
        setSelectedVisualAsset,
        selectedVisualAssetId,
        streamingRefinementTask,
        streamEvents,
        isStreamingRefinementTask,
        streamErrorText,
        creatingRefinementCapability,
        retryingRefinementTaskId,
        invalidateSancaiContentGovernance,
        invalidateSancaiContentCandidates,
        refreshSancaiEntryDetail,
        createRefinementTask,
        retryRefinementTask,
        closeStreamingRefinementTask,
        refreshAfterVisualAssetCandidateHandled,
        resetHandledSucceededTaskIds
    } = useSancaiEntryPanelState({
        queryClient,
        messageApi,
        selectedEntry,
        selectedEntryId,
        currentUserId: currentUserQuery.data?.id,
        refinementTasks,
        invalidateEntries,
        invalidateRefinementTasks
    });
    const invalidateExportJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "exports", "jobs"]
        });
    };
    const invalidateEntryImages = async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "images", selectedEntryId]
            }),
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] })
        ]);
    };

    const invalidateBatchCandidateData = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] }),
            refreshSancaiEntryDetail(),
            invalidateSancaiContentCandidates(selectedVisualAssetId),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "SANCAI_ENTRY"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "SANCAI_ENTRY", selectedEntryId]
            })
        ]);
    }, [
        queryClient,
        refreshSancaiEntryDetail,
        invalidateSancaiContentCandidates,
        selectedVisualAssetId,
        selectedEntryId
    ]);

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
    const updateVisualAssetMutation = useMutation({
        mutationFn: entryService.updateVisualAsset,
        onSuccess: async () => {
            await Promise.all([refreshSancaiEntryDetail(), invalidateEntries()]);
            messageApi.success("三才视觉处理已采纳");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉处理采纳失败");
        }
    });
    const changeCurrentVisualAssetMutation = useMutation({
        mutationFn: entryService.changeCurrentVisualAsset,
        onSuccess: async () => {
            await Promise.all([refreshSancaiEntryDetail(), invalidateEntries()]);
            messageApi.success("当前视觉处理版本已切换");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "视觉处理切换失败");
        }
    });
    const changeCurrentImageMutation = useMutation({
        mutationFn: entryService.changeCurrentImage,
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("封面图已切换");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "封面图切换失败");
        }
    });
    const uploadImageMutation = useMutation({
        mutationFn: (file: File) => {
            if (!selectedEntryId) {
                throw new Error("请先选择条目");
            }
            return entryService.uploadImage({
                currentUsed: false,
                entryId: selectedEntryId,
                file,
                imageType: "ORIGINAL",
                title: file.name
            });
        },
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("图片已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "图片上传失败");
        }
    });
    const sortImagesMutation = useMutation({
        mutationFn: entryService.sortImages,
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("图片顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "图片排序失败");
        }
    });
    const deleteImageMutation = useMutation({
        mutationFn: entryService.deleteImage,
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("图片已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "图片删除失败");
        }
    });

    const selectEntry = (entry: SancaiEntryRecord) => {
        setIsCreating(false);
        setSelectedVisualAsset(null);
        setEditingEntry(entry);
        setSelectedVersionId(null);
        setIsModelOpen(true);
    };

    const closeModel = () => {
        closeStreamingRefinementTask();
        setIsCreating(false);
        setSelectedVisualAsset(null);
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
            onConfirm: () => deleteExportMutation.mutateAsync(job.id as number)
        });
    };
    const deleteExportJobs = (jobs: ClassicsExportJobRecord[]) => {
        const ids = jobs.map((job) => job.id).filter((id): id is number => id != null);
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
    const downloadImage = (image: SancaiEntryImageRecord) => {
        if (!selectedEntryId) {
            return;
        }
        window.open(
            entryService.getImageContentUrl({
                entryId: selectedEntryId,
                imageId: image.id,
                mode: "download"
            }),
            "_blank",
            "noopener,noreferrer"
        );
    };
    const changeCurrentImage = (image: SancaiEntryImageRecord) => {
        if (!selectedEntryId || image.currentUsed) {
            return;
        }
        changeCurrentImageMutation.mutate({
            entryId: selectedEntryId,
            imageId: image.id
        });
    };
    const deleteImage = (image: SancaiEntryImageRecord) => {
        if (!selectedEntryId) {
            return;
        }
        const title = readImageTitle(image);
        confirm.danger({
            title: "删除三才图会图片",
            message: `确认删除 ${title}？`,
            description: image.currentUsed
                ? "删除当前封面图后，系统会按剩余排序第一张自动设为封面图。"
                : "删除后当前条目的图片列表会立即刷新。",
            okText: "删除",
            onConfirm: () =>
                deleteImageMutation.mutateAsync({
                    entryId: selectedEntryId,
                    imageId: image.id
                })
        });
    };
    const sortImage = (
        sourceImage: SancaiEntryImageRecord,
        targetImage: SancaiEntryImageRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (!selectedEntryId) {
            return;
        }
        if (sourceImage.id === targetImage.id) {
            return;
        }
        const remainingImages = entryImages.filter((image) => image.id !== sourceImage.id);
        const targetIndex = remainingImages.findIndex((image) => image.id === targetImage.id);
        if (targetIndex < 0) {
            return;
        }
        const insertIndex = position === "before" ? targetIndex : targetIndex + 1;
        const nextImages = [...remainingImages];
        nextImages.splice(insertIndex, 0, sourceImage);
        sortImagesMutation.mutate({
            entryId: selectedEntryId,
            orderedIds: nextImages.map((item) => item.id),
            sortDirection: "ASC"
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
    const isExportJobsDrawerOpen = exportJobsDrawerOpen ?? internalExportJobsDrawerOpen;
    const setExportJobsOpen = onExportJobsDrawerOpenChange ?? setInternalExportJobsDrawerOpen;

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
                onShare={shareEntry}
                onBatchCandidateGovernance={openBatchCandidateDrawer}
                onSort={sortEntry}
                onView={selectEntry}
            />
            <KuzhambuDrawer
                destroyOnClose={false}
                open={isExportJobsDrawerOpen}
                size="large"
                title="导出任务"
                footer={
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-close-button"
                        type="primary"
                        onClick={() => setExportJobsOpen(false)}
                    >
                        关闭
                    </KuzhambuButton>
                }
                onClose={() => setExportJobsOpen(false)}
            >
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
                    loading={
                        exportsQuery.isLoading ||
                        exportEntryMutation.isPending ||
                        deleteExportMutation.isPending
                    }
                    sectionTitle="任务列表"
                    onDownload={(job) => {
                        if (job.downloadUrl) {
                            window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                        }
                    }}
                    onDelete={canManageGeneratedArtifacts ? deleteExportJob : undefined}
                    onBatchDelete={canManageGeneratedArtifacts ? deleteExportJobs : undefined}
                    onRefresh={() => {
                        void invalidateExportJobs();
                    }}
                />
            </KuzhambuDrawer>
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
            <SancaiEntryModel
                key={modelKey}
                categoryOptions={categoryOptions}
                entry={selectedEntry}
                isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                isUpdatingVisualAsset={updateVisualAssetMutation.isPending}
                initialCategoryId={categoryId}
                initialVolumeId={volumeId}
                mode={isCreating ? "create" : "edit"}
                open={isModelOpen && !isLoading}
                volumes={volumes}
                onCancel={closeModel}
                onSubmit={submitEntry}
                onUseVisualAsset={switchVisualAsset}
                onUpdateVisualAsset={updateVisualAsset}
                onSelectedVisualAssetChange={setSelectedVisualAsset}
                onCreateVisualAssetTask={(capability, asset) => {
                    createRefinementTask(capability, asset);
                }}
                onCreateTranslationTask={() => createRefinementTask("translate")}
                isCreatingTranslationTask={creatingRefinementCapability === "translate"}
                translationTasks={refinementTasks.filter((task) => task.capability === "translate")}
                creatingVisualAssetCapability={
                    creatingRefinementCapability === "image_analysis" ||
                    creatingRefinementCapability === "fusion" ||
                    creatingRefinementCapability === "visual" ||
                    creatingRefinementCapability === "image_gen"
                        ? creatingRefinementCapability
                        : null
                }
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
                imageContent={
                    !isCreating && selectedEntry ? (
                        <div
                            className="sancai-entry-image-manager"
                            aria-label="三才图会图片管理"
                            aria-busy={imagesQuery.isLoading}
                        >
                            <div className="sancai-entry-image-toolbar">
                                <Upload
                                    aria-label="上传图片"
                                    accept={IMAGE_ACCEPT}
                                    showUploadList={false}
                                    beforeUpload={(file) => {
                                        uploadImageMutation.mutate(file);
                                        return Upload.LIST_IGNORE;
                                    }}
                                >
                                    <KuzhambuButton
                                        testId="classics-sancai-sancai-entry-action-button"
                                        icon={<UploadOutlined />}
                                        loading={uploadImageMutation.isPending}
                                        type="primary"
                                    >
                                        上传图片
                                    </KuzhambuButton>
                                </Upload>
                            </div>
                            {entryImages.length > 0 ? (
                                <Image.PreviewGroup>
                                    <KuzhambuTable
                                        className="sancai-image-table"
                                        ariaLabel="三才图会图片列表"
                                        columns={
                                            [
                                                {
                                                    title: "图片",
                                                    key: "image",
                                                    render: (_, image) => {
                                                        const imageTitle = readImageTitle(image);
                                                        const thumbnail = (
                                                            <Image
                                                                className={
                                                                    image.currentUsed
                                                                        ? "sancai-entry-image-cover-thumbnail"
                                                                        : undefined
                                                                }
                                                                width={132}
                                                                height={88}
                                                                src={resolveImagePreviewUrl(
                                                                    selectedEntryId,
                                                                    image
                                                                )}
                                                                alt={imageTitle}
                                                            />
                                                        );
                                                        return (
                                                            <div className="sancai-entry-image-cell">
                                                                {image.currentUsed ? (
                                                                    <span className="sancai-entry-image-cover">
                                                                        <Badge.Ribbon text="封面">
                                                                            {thumbnail}
                                                                        </Badge.Ribbon>
                                                                    </span>
                                                                ) : (
                                                                    thumbnail
                                                                )}
                                                                <span className="sancai-entry-image-meta">
                                                                    <Text strong>{imageTitle}</Text>
                                                                    <Text type="secondary">
                                                                        {formatImageSize(
                                                                            image.size
                                                                        )}
                                                                    </Text>
                                                                </span>
                                                            </div>
                                                        );
                                                    }
                                                },
                                                {
                                                    inlineLimit: 4,
                                                    key: "actions",
                                                    title: "操作",
                                                    width: 220,
                                                    options: (image) => [
                                                        {
                                                            key: "download",
                                                            text: "下载",
                                                            ariaLabel: `下载 ${readImageTitle(image)}`,
                                                            onClick: () => downloadImage(image)
                                                        },
                                                        {
                                                            key: "cover",
                                                            text: "封面",
                                                            ariaLabel: `设为封面 ${readImageTitle(image)}`,
                                                            disabled: Boolean(image.currentUsed),
                                                            onClick: () => changeCurrentImage(image)
                                                        },
                                                        {
                                                            type: "divider"
                                                        },
                                                        {
                                                            key: "delete",
                                                            text: "删除",
                                                            type: "danger",
                                                            ariaLabel: `删除 ${readImageTitle(image)}`,
                                                            disabled: deleteImageMutation.isPending,
                                                            onClick: () => deleteImage(image)
                                                        }
                                                    ]
                                                }
                                            ] satisfies KuzhambuTableProps<SancaiEntryImageRecord>["columns"]
                                        }
                                        dataSource={entryImages}
                                        pagination={false}
                                        rowKey="id"
                                        size="small"
                                        scroll={{ x: 640 }}
                                        sortable
                                        onSort={sortImage}
                                    />
                                </Image.PreviewGroup>
                            ) : (
                                <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description="暂无图片"
                                />
                            )}
                        </div>
                    ) : null
                }
                refinementContent={
                    !isCreating && selectedEntry ? (
                        <>
                            <Card size="small" title="AI 精修任务">
                                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                                    <KuzhambuButton
                                        testId="classics-sancai-sancai-entry-action-button-2"
                                        type="primary"
                                        loading={creatingRefinementCapability === "summary"}
                                        onClick={() => createRefinementTask("summary")}
                                    >
                                        摘要
                                    </KuzhambuButton>
                                </div>
                                <div style={{ marginTop: 12, display: "grid", gap: 8 }}>
                                    {refinementTasks
                                        .filter(
                                            (task) =>
                                                task.capability === "translate" ||
                                                task.capability === "summary" ||
                                                task.capability === "image_analysis" ||
                                                task.capability === "visual" ||
                                                task.capability === "fusion" ||
                                                task.capability === "image_gen"
                                        )
                                        .slice(0, 6)
                                        .map((task) => {
                                            const failureText =
                                                aiRefinementTaskService.getTaskFailureText(
                                                    task.failureStage,
                                                    task.errorType,
                                                    task.errorMessage
                                                );
                                            return (
                                                <Card
                                                    key={task.taskId}
                                                    size="small"
                                                    bodyStyle={{ padding: 12 }}
                                                >
                                                    <div
                                                        style={{
                                                            display: "flex",
                                                            justifyContent: "space-between",
                                                            gap: 12,
                                                            alignItems: "center",
                                                            flexWrap: "wrap"
                                                        }}
                                                    >
                                                        <div>
                                                            {aiRefinementTaskService.getTaskCapabilityLabel(
                                                                task.capability
                                                            )}
                                                            ：{task.status}
                                                            {task.resultPreview
                                                                ? ` / ${task.resultPreview}`
                                                                : ""}
                                                        </div>
                                                        {aiRefinementTaskService.getTaskRetryable(
                                                            task.status,
                                                            task.capability
                                                        ) ? (
                                                            <KuzhambuButton
                                                                testId="classics-sancai-sancai-entry-retry-button"
                                                                size="small"
                                                                loading={
                                                                    retryingRefinementTaskId ===
                                                                    task.taskId
                                                                }
                                                                onClick={() =>
                                                                    retryRefinementTask(task)
                                                                }
                                                            >
                                                                重试
                                                            </KuzhambuButton>
                                                        ) : null}
                                                    </div>
                                                    {failureText ? (
                                                        <Alert
                                                            showIcon
                                                            type="error"
                                                            style={{ marginTop: 8 }}
                                                            message="失败原因"
                                                            description={failureText}
                                                        />
                                                    ) : null}
                                                </Card>
                                            );
                                        })}
                                </div>
                            </Card>
                            {streamingRefinementTask ? (
                                <AiRefinementStreamPanel
                                    events={streamEvents}
                                    isStreaming={isStreamingRefinementTask}
                                    streamErrorText={streamErrorText}
                                    task={streamingRefinementTask}
                                    onClose={closeStreamingRefinementTask}
                                    onRetry={() => retryRefinementTask(streamingRefinementTask)}
                                    onViewCandidate={() => {
                                        void invalidateSancaiContentCandidates(
                                            streamingRefinementTask.objectId ??
                                                selectedVisualAssetId
                                        );
                                        candidatePanelRef.current?.scrollIntoView({
                                            block: "start",
                                            behavior: "smooth"
                                        });
                                        candidatePanelRef.current?.focus();
                                    }}
                                />
                            ) : null}
                            {selectedVisualAssetId ? (
                                <div
                                    ref={candidatePanelRef}
                                    className="sancai-candidate-panel-anchor"
                                    tabIndex={-1}
                                >
                                    <AiCandidatePanel
                                        capabilities={[
                                            "image_analysis",
                                            "visual",
                                            "fusion",
                                            "image_gen"
                                        ]}
                                        contentId={selectedEntry.id}
                                        contentType="SANCAI_ENTRY"
                                        objectId={selectedVisualAssetId}
                                        onApplied={async () => {
                                            await refreshAfterVisualAssetCandidateHandled();
                                        }}
                                        onRejected={async () => {
                                            await refreshAfterVisualAssetCandidateHandled();
                                        }}
                                    />
                                </div>
                            ) : null}
                        </>
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
                                panelTitle="三才图会标签治理"
                                onChanged={invalidateSancaiContentGovernance}
                            />
                        </div>
                    ) : null
                }
                versionContent={
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
