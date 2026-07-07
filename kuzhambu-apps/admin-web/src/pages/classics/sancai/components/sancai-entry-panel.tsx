import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Card, Empty, Input, Switch, Tag, Typography, Upload } from "antd";
import { useCallback, useMemo, useRef, useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import { ClassicsShowcaseJobSection } from "@/pages/classics/common/components/classics-showcase-job-section";
import { AiCandidatePanel } from "@/pages/classics/common/components/ai-candidate-panel";
import { AiRefinementStreamPanel } from "@/pages/classics/common/components/ai-refinement-stream-panel";
import { ClassicsContentQaPanel } from "@/pages/classics/common/components/classics-content-qa-panel";
import { ClassicsContentTagPanel } from "@/pages/classics/common/components/classics-content-tag-panel";
import { SancaiEntryImagePreview } from "./sancai-entry-image-preview";
import { SancaiEntryList } from "./sancai-entry-list";
import { SancaiEntryModel } from "./sancai-entry-model";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import { SancaiVersionHistoryPanel } from "./sancai-version-history-panel";
import { useSancaiEntryPanelState } from "../hooks/use-sancai-entry-panel-state";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiContentVersionRecord,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord,
    SancaiVolumeRecord
} from "../sancai-types";

const { Text } = Typography;

const EXPORT_PAGE_SIZE = 8;
const SHOWCASE_PAGE_SIZE = 8;
const TASK_POLL_INTERVAL_MS = 3000;
const IMAGE_ACCEPT = ".jpg,.jpeg,.png,.gif,.webp";

const readEntryTitle = (entry: SancaiEntryRecord) => {
    return entry.title?.trim() || `条目 ${entry.id}`;
};

const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
};

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
    const [previewImageId, setPreviewImageId] = useState<number | null>(null);
    const [imageUploadTitle, setImageUploadTitle] = useState("");
    const [imageUploadType, setImageUploadType] = useState("ORIGINAL");
    const [imageUploadCurrentUsed, setImageUploadCurrentUsed] = useState(true);
    const candidatePanelRef = useRef<HTMLDivElement | null>(null);
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
        entryTagNames,
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
    const invalidateShowcaseJobs = async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "showcases", "jobs"]
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
    const changeCurrentImageMutation = useMutation({
        mutationFn: entryService.changeCurrentImage,
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("当前配图已切换");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "配图切换失败");
        }
    });
    const uploadImageMutation = useMutation({
        mutationFn: (file: File) => {
            if (!selectedEntryId) {
                throw new Error("请先选择条目");
            }
            return entryService.uploadImage({
                currentUsed: imageUploadCurrentUsed,
                entryId: selectedEntryId,
                file,
                imageType: imageUploadType,
                title: imageUploadTitle.trim() || file.name
            });
        },
        onSuccess: async () => {
            setImageUploadTitle("");
            await invalidateEntryImages();
            messageApi.success("配图已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "配图上传失败");
        }
    });
    const sortImagesMutation = useMutation({
        mutationFn: entryService.sortImages,
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("配图顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "配图排序失败");
        }
    });
    const deleteImageMutation = useMutation({
        mutationFn: entryService.deleteImage,
        onSuccess: async () => {
            await invalidateEntryImages();
            messageApi.success("配图已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "配图删除失败");
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
        setPreviewImageId(null);
        resetHandledSucceededTaskIds();
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
    const previewImage = (image: SancaiEntryImageRecord) => {
        setPreviewImageId(image.id);
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
            title: "删除三才图会配图",
            message: `确认删除 ${title}？`,
            description: "删除后当前条目的配图列表会立即刷新；若删除当前图，系统会自动补位。",
            okText: "删除",
            onConfirm: () =>
                deleteImageMutation.mutateAsync({
                    entryId: selectedEntryId,
                    imageId: image.id
                })
        });
    };
    const moveImage = (image: SancaiEntryImageRecord, direction: "up" | "down") => {
        if (!selectedEntryId) {
            return;
        }
        const currentIndex = entryImages.findIndex((item) => item.id === image.id);
        if (currentIndex < 0) {
            return;
        }
        const targetIndex = direction === "up" ? currentIndex - 1 : currentIndex + 1;
        if (targetIndex < 0 || targetIndex >= entryImages.length) {
            return;
        }
        const nextImages = [...entryImages];
        [nextImages[currentIndex], nextImages[targetIndex]] = [
            nextImages[targetIndex],
            nextImages[currentIndex]
        ];
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
                entryTags={entryTagNames}
                isSubmitting={addEntryMutation.isPending || updateEntryMutation.isPending}
                isSwitchingVisualAsset={changeCurrentVisualAssetMutation.isPending}
                isUpdatingVisualAsset={updateVisualAssetMutation.isPending}
                mode={isCreating ? "create" : "edit"}
                open={isModelOpen && !isLoading}
                onCancel={closeModel}
                onSubmit={submitEntry}
                onUseVisualAsset={switchVisualAsset}
                onUpdateVisualAsset={updateVisualAsset}
                onSelectedVisualAssetChange={setSelectedVisualAsset}
                onCreateVisualAssetTask={(capability, asset) => {
                    createRefinementTask(capability, asset);
                }}
                creatingVisualAssetCapability={
                    creatingRefinementCapability === "image_analysis" ||
                    creatingRefinementCapability === "fusion" ||
                    creatingRefinementCapability === "visual" ||
                    creatingRefinementCapability === "image_gen"
                        ? creatingRefinementCapability
                        : null
                }
                afterForm={
                    !isCreating && selectedEntry ? (
                        <>
                            <Card
                                size="small"
                                title="配图管理"
                                aria-label="三才图会配图管理"
                                loading={imagesQuery.isLoading}
                            >
                                <div className="sancai-image-upload-bar">
                                    <Input
                                        aria-label="图片标题"
                                        value={imageUploadTitle}
                                        placeholder="图片标题"
                                        onChange={(event) =>
                                            setImageUploadTitle(event.target.value)
                                        }
                                    />
                                    <label className="sancai-image-type-field">
                                        <span>图片类型</span>
                                        <select
                                            aria-label="图片类型"
                                            value={imageUploadType}
                                            onChange={(event) =>
                                                setImageUploadType(event.target.value)
                                            }
                                        >
                                            <option value="ORIGINAL">ORIGINAL</option>
                                            <option value="GENERATED">GENERATED</option>
                                        </select>
                                    </label>
                                    <label className="sancai-image-current-field">
                                        <span>上传后设为当前使用</span>
                                        <Switch
                                            aria-label="上传后设为当前使用"
                                            checked={imageUploadCurrentUsed}
                                            onChange={setImageUploadCurrentUsed}
                                        />
                                    </label>
                                    <Upload
                                        aria-label="上传配图"
                                        accept={IMAGE_ACCEPT}
                                        showUploadList={false}
                                        beforeUpload={(file) => {
                                            uploadImageMutation.mutate(file);
                                            return Upload.LIST_IGNORE;
                                        }}
                                    >
                                        <Button loading={uploadImageMutation.isPending}>
                                            上传配图
                                        </Button>
                                    </Upload>
                                </div>
                                {entryImages.length > 0 ? (
                                    <div className="sancai-image-card-list">
                                        {entryImages.map((image, index) => {
                                            const title = readImageTitle(image);
                                            return (
                                                <article
                                                    key={image.id}
                                                    className="sancai-image-card"
                                                    aria-label={`配图 ${title}`}
                                                >
                                                    <div className="sancai-image-card-main">
                                                        <div>
                                                            <Text strong>{title}</Text>
                                                            <KuzhambuSpace wrap>
                                                                {image.currentUsed ? (
                                                                    <Tag color="green">
                                                                        当前使用
                                                                    </Tag>
                                                                ) : null}
                                                                <Text type="secondary">
                                                                    {image.imageType || "UNKNOWN"}
                                                                </Text>
                                                                <Text type="secondary">
                                                                    优先级 {image.priority ?? "-"}
                                                                </Text>
                                                                <Text type="secondary">
                                                                    {formatImageSize(image.size)}
                                                                </Text>
                                                            </KuzhambuSpace>
                                                        </div>
                                                        <KuzhambuSpace wrap>
                                                            <Button
                                                                size="small"
                                                                onClick={() => previewImage(image)}
                                                            >
                                                                预览图片
                                                            </Button>
                                                            <Button
                                                                size="small"
                                                                onClick={() => downloadImage(image)}
                                                            >
                                                                下载图片
                                                            </Button>
                                                            <Button
                                                                size="small"
                                                                disabled={Boolean(
                                                                    image.currentUsed
                                                                )}
                                                                loading={
                                                                    changeCurrentImageMutation.isPending
                                                                }
                                                                onClick={() =>
                                                                    changeCurrentImage(image)
                                                                }
                                                            >
                                                                设为当前使用图片
                                                            </Button>
                                                            <Button
                                                                danger
                                                                size="small"
                                                                loading={
                                                                    deleteImageMutation.isPending
                                                                }
                                                                onClick={() => deleteImage(image)}
                                                            >
                                                                删除图片
                                                            </Button>
                                                            <Button
                                                                size="small"
                                                                disabled={
                                                                    index === 0 ||
                                                                    sortImagesMutation.isPending
                                                                }
                                                                onClick={() =>
                                                                    moveImage(image, "up")
                                                                }
                                                            >
                                                                上移图片
                                                            </Button>
                                                            <Button
                                                                size="small"
                                                                disabled={
                                                                    index ===
                                                                        entryImages.length - 1 ||
                                                                    sortImagesMutation.isPending
                                                                }
                                                                onClick={() =>
                                                                    moveImage(image, "down")
                                                                }
                                                            >
                                                                下移图片
                                                            </Button>
                                                        </KuzhambuSpace>
                                                    </div>
                                                    <Text type="secondary">
                                                        存储对象：{image.storageObjectId ?? "-"} /
                                                        文件：
                                                        {image.originalFilename || "-"}
                                                    </Text>
                                                </article>
                                            );
                                        })}
                                    </div>
                                ) : (
                                    <Empty
                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        description="暂无配图"
                                    />
                                )}
                            </Card>
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
                                                            <Button
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
                                                            </Button>
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
            <SancaiEntryImagePreview
                key={previewImageId ?? "closed"}
                entryId={selectedEntryId}
                images={entryImages}
                openImageId={previewImageId}
                onClose={() => setPreviewImageId(null)}
            />
        </>
    );
};
