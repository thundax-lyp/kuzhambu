import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { KuzhambuForm } from "@/components/kuzhambu-form";
import { KuzhambuSegmentedDrawer } from "@/components/kuzhambu-segmented-drawer";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { SancaiEntryBasicSection } from "./sancai-entry-basic-section";
import type { SancaiEntrySummaryModalProps } from "./sancai-entry-summary-text-field";
import { SancaiEntryVisualSection } from "./sancai-entry-visual-section";
import { toEntryFormValues, type SancaiEntryFormValues } from "../sancai-form-values";
import type { SancaiVisualAssetRefinementCapability } from "@/pages/classics/sancai/sancai-entry-service";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-edit-drawer.css";

type SancaiEntryEditDrawerSection = "basic" | "visual" | "tags" | "qa" | "versions";
const AI_TEXT_CANDIDATE_POLL_INTERVAL_MS = 3000;
const RUNNING_REFINEMENT_STATUSES = new Set(["PENDING", "RUNNING"]);

const sortRefinementTasksByNewest = (
    left: AiRefinementTaskRecord,
    right: AiRefinementTaskRecord
) => {
    if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
        return right.requestedAt.localeCompare(left.requestedAt);
    }
    return right.taskId - left.taskId;
};

const selectLatestSummaryCandidate = (candidates: AiCandidateRecord[] | undefined) => {
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                candidate.capability === "summary" &&
                candidate.status === "PENDING" &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) => {
            if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
                return right.requestedAt.localeCompare(left.requestedAt);
            }
            return right.candidateId - left.candidateId;
        })[0];
};

const isRunningRefinementTask = (task?: AiRefinementTaskRecord | null) => {
    return Boolean(task?.status) && RUNNING_REFINEMENT_STATUSES.has(task?.status ?? "");
};

const selectCurrentImage = (images: SancaiEntryImageRecord[]) => {
    return [...images]
        .filter((image) => image.currentUsed !== false)
        .sort((left, right) => (left.priority ?? 0) - (right.priority ?? 0))[0];
};

const resolveImageUrl = (
    entryId: number | undefined,
    image: SancaiEntryImageRecord | undefined,
    mode: SancaiEntryImageContentMode
) => {
    if (!entryId || !image?.id) {
        return undefined;
    }
    return toAuthenticatedResourceUrl(
        entryService.getImageContentUrl({
            entryId,
            imageId: image.id,
            mode
        })
    );
};

const selectCurrentVisualAsset = (assets: SancaiVisualAssetRecord[]) => {
    return [...assets]
        .filter((asset) => asset.currentUsed !== false)
        .sort((left, right) => (right.versionNo ?? 0) - (left.versionNo ?? 0))[0];
};

const readVisualAssetTitle = (asset: SancaiVisualAssetRecord | undefined) => {
    if (!asset) {
        return "未选择视觉处理";
    }
    return `处理记录 ${asset.versionNo ?? asset.visualAssetId ?? asset.id ?? "-"}`;
};

const isSameStorageObjectId = (
    left: number | string | null | undefined,
    right: number | string | null | undefined
) => {
    return left != null && right != null && String(left) === String(right);
};

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

const escapeHtml = (value?: string | number | null) => {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
};

interface SancaiEntryEditDrawerProps {
    imageContent?: ReactNode;
    qaContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
    visualRefinementContent?: ReactNode;
    categoryOptions?: Array<{ label: string; value: number }>;
    entry: SancaiEntryRecord | undefined;
    initialCategoryId?: number | null;
    initialVolumeId?: number | null;
    isSubmitting: boolean;
    isUpdatingVisualAsset?: boolean;
    mode?: "create" | "edit";
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
    onUseVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onUpdateVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onCreateVisualAssetTask?: (
        capability: SancaiVisualAssetRefinementCapability,
        asset: SancaiVisualAssetRecord
    ) => void;
    onCreateTranslationTask?: (draft: SancaiEntryFormValues) => void;
    onCreateSummaryTask?: (draft: SancaiEntryFormValues) => void;
    creatingVisualAssetCapability?: SancaiVisualAssetRefinementCapability | null;
    isCreatingTranslationTask?: boolean;
    isCreatingSummaryTask?: boolean;
    translationTasks?: AiRefinementTaskRecord[];
    summaryTasks?: AiRefinementTaskRecord[];
    onSelectedVisualAssetChange?: (asset: SancaiVisualAssetRecord | null) => void;
    volumes?: Array<{ categoryId?: number | null; id: number; title?: string | null }>;
}

export const SancaiEntryEditDrawer = ({
    imageContent,
    qaContent,
    tagContent,
    versionContent,
    visualRefinementContent,
    categoryOptions = [],
    entry,
    initialCategoryId = null,
    initialVolumeId = null,
    isSubmitting,
    isUpdatingVisualAsset = false,
    mode = "edit",
    open,
    onCancel,
    onSubmit,
    onUseVisualAsset,
    onUpdateVisualAsset,
    onCreateVisualAssetTask,
    onCreateTranslationTask,
    onCreateSummaryTask,
    creatingVisualAssetCapability = null,
    isCreatingTranslationTask = false,
    isCreatingSummaryTask = false,
    translationTasks = [],
    summaryTasks = [],
    onSelectedVisualAssetChange,
    volumes = []
}: SancaiEntryEditDrawerProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<SancaiEntryFormValues>(() =>
        toEntryFormValues(entry, volumes, initialCategoryId, initialVolumeId)
    );
    const [activeSection, setActiveSection] = useState<SancaiEntryEditDrawerSection>("basic");
    const [summaryModalOpen, setSummaryModalOpen] = useState(false);
    const [summaryDraft, setSummaryDraft] = useState("");
    const [loadedSummaryCandidateId, setLoadedSummaryCandidateId] = useState<number | null>(null);
    const entryId = mode === "edit" ? entry?.id : undefined;
    const latestSummaryTask = useMemo(
        () =>
            [...summaryTasks]
                .filter((task) => task.capability === "summary")
                .sort(sortRefinementTasksByNewest)[0] ?? null,
        [summaryTasks]
    );
    const hasRunningSummaryTask = isRunningRefinementTask(latestSummaryTask);
    const syncSummaryTask = useCallback(
        (task: AiRefinementTaskRecord | null) => {
            if (!task || !entryId) {
                return;
            }
            const normalizedTask = { ...task, capability: "summary" };
            queryClient.setQueryData<{
                items?: AiRefinementTaskRecord[];
                [key: string]: unknown;
            }>(["classics", "sancai", "refinement", "tasks", entryId], (currentPage) => {
                if (!currentPage?.items) {
                    return currentPage;
                }
                const taskExists = currentPage.items.some(
                    (item) => item.taskId === normalizedTask.taskId
                );
                return {
                    ...currentPage,
                    items: taskExists
                        ? currentPage.items.map((item) =>
                              item.taskId === normalizedTask.taskId
                                  ? { ...item, ...normalizedTask }
                                  : item
                          )
                        : [normalizedTask, ...currentPage.items]
                };
            });
        },
        [entryId, queryClient]
    );
    const summaryCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "summary", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: "summary",
                status: "PENDING"
            }),
        enabled: summaryModalOpen && Boolean(entryId),
        retry: false,
        refetchInterval: () => {
            return isCreatingSummaryTask || hasRunningSummaryTask
                ? AI_TEXT_CANDIDATE_POLL_INTERVAL_MS
                : false;
        }
    });
    const imagesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "images", entryId],
        queryFn: () => entryService.listImages(entryId ?? 0),
        enabled: open && Boolean(entryId),
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
    const currentImage = selectCurrentImage(entryImages);
    const previewUrl = resolveImageUrl(entryId, currentImage, "preview");
    const downloadUrl = resolveImageUrl(entryId, currentImage, "download");
    const visualAssetsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "visual-assets", entryId],
        queryFn: () => entryService.listVisualAssets(entryId ?? 0),
        enabled: open && Boolean(entryId),
        retry: false
    });
    const visualAssets = useMemo(() => visualAssetsQuery.data || [], [visualAssetsQuery.data]);
    const orderedVisualAssets = useMemo(
        () =>
            [...visualAssets].sort((left, right) => {
                if ((left.versionNo ?? 0) !== (right.versionNo ?? 0)) {
                    return (right.versionNo ?? 0) - (left.versionNo ?? 0);
                }
                return (
                    (right.visualAssetId ?? right.id ?? 0) - (left.visualAssetId ?? left.id ?? 0)
                );
            }),
        [visualAssets]
    );
    const volumeOptions = useMemo(
        () =>
            volumes
                .filter((volume) => volume.categoryId === form.categoryId)
                .map((volume) => ({
                    label: volume.title?.trim() || `卷 ${volume.id}`,
                    value: volume.id
                })),
        [form.categoryId, volumes]
    );
    const currentVisualAsset = useMemo(
        () => selectCurrentVisualAsset(visualAssets),
        [visualAssets]
    );
    const [selectedVisualAssetId, setSelectedVisualAssetId] = useState<number | null>(null);
    const [visualAssetForm, setVisualAssetForm] = useState<SancaiVisualAssetRecord | null>(null);
    const activeVisualAssetId =
        selectedVisualAssetId ??
        currentVisualAsset?.visualAssetId ??
        currentVisualAsset?.id ??
        null;
    const selectedVisualAsset =
        visualAssets.find(
            (asset) => (asset.visualAssetId ?? asset.id ?? null) === activeVisualAssetId
        ) ||
        currentVisualAsset ||
        null;
    const applyAiTextCandidateMutation = useMutation({
        mutationFn: aiCandidateService.apply,
        onSuccess: async (_, command) => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: [
                        "ai",
                        "candidates",
                        "SANCAI_ENTRY",
                        entryId,
                        command.capability,
                        "modal"
                    ]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "entries"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "entries", "versions", entryId]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "refinement", "tasks", entryId]
                })
            ]);
            setForm((currentForm) => ({
                ...currentForm,
                summary: command.resultPayload
            }));
            setSummaryModalOpen(false);
            messageApi.success("摘要已写入基础信息");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 候选应用失败");
        }
    });
    const latestSummaryCandidate = useMemo(
        () => selectLatestSummaryCandidate(summaryCandidatesQuery.data),
        [summaryCandidatesQuery.data]
    );
    const loadedSummaryCandidate = useMemo(() => {
        if (!loadedSummaryCandidateId) {
            return null;
        }
        return (
            (summaryCandidatesQuery.data || []).find(
                (candidate) =>
                    candidate.candidateId === loadedSummaryCandidateId &&
                    candidate.capability === "summary"
            ) ?? null
        );
    }, [summaryCandidatesQuery.data, loadedSummaryCandidateId]);
    const isSummaryApplyDisabled =
        !summaryDraft.trim() ||
        isCreatingSummaryTask ||
        hasRunningSummaryTask ||
        summaryCandidatesQuery.isFetching;

    useEffect(() => {
        if (!summaryModalOpen || !latestSummaryCandidate) {
            return;
        }
        if (latestSummaryCandidate.candidateId === loadedSummaryCandidateId) {
            return;
        }
        const timer = window.setTimeout(() => {
            setLoadedSummaryCandidateId(latestSummaryCandidate.candidateId);
            setSummaryDraft(latestSummaryCandidate.resultPayload?.trim() || "");
        }, 0);
        return () => window.clearTimeout(timer);
    }, [latestSummaryCandidate, loadedSummaryCandidateId, summaryModalOpen]);

    useEffect(() => {
        if (!summaryModalOpen || !latestSummaryTask?.taskId) {
            return;
        }
        if (latestSummaryTask.status !== "SUCCEEDED" && latestSummaryTask.status !== "PARTIAL") {
            return;
        }
        void summaryCandidatesQuery.refetch();
    }, [
        latestSummaryTask?.status,
        latestSummaryTask?.taskId,
        summaryCandidatesQuery,
        summaryModalOpen
    ]);

    useEffect(() => {
        if (onSelectedVisualAssetChange) {
            onSelectedVisualAssetChange(selectedVisualAsset);
        }
    }, [onSelectedVisualAssetChange, selectedVisualAsset]);
    const defaultSourceImage = entryImages.find((image) => image.storageObjectId);
    const visualAssetFormValue = useMemo(() => {
        if (!selectedVisualAsset) {
            return null;
        }
        const selectedId = selectedVisualAsset.visualAssetId ?? selectedVisualAsset.id ?? null;
        const formId = visualAssetForm?.visualAssetId ?? visualAssetForm?.id ?? null;
        const baseForm = formId === selectedId ? visualAssetForm : { ...selectedVisualAsset };
        if (!baseForm) {
            return null;
        }
        if (baseForm.sourceImageStorageObjectId || !defaultSourceImage?.storageObjectId) {
            return baseForm;
        }
        return {
            ...baseForm,
            sourceImageStorageObjectId: defaultSourceImage.storageObjectId,
            sourcePreviewUrl: resolveImageUrl(entryId, defaultSourceImage, "preview"),
            sourceDownloadUrl: resolveImageUrl(entryId, defaultSourceImage, "download")
        };
    }, [defaultSourceImage, entryId, selectedVisualAsset, visualAssetForm]);
    const selectedVisualAssetResourceId =
        selectedVisualAsset?.visualAssetId ?? selectedVisualAsset?.id;
    const selectedSourceStorageObjectId =
        visualAssetFormValue?.sourceImageStorageObjectId ??
        defaultSourceImage?.storageObjectId ??
        null;
    const visualAssetsForSelectedSource = useMemo(
        () =>
            selectedSourceStorageObjectId == null
                ? []
                : orderedVisualAssets.filter(
                      (asset) => asset.sourceImageStorageObjectId === selectedSourceStorageObjectId
                  ),
        [orderedVisualAssets, selectedSourceStorageObjectId]
    );
    const selectedSourceImage = entryImages.find(
        (image) =>
            image.storageObjectId != null &&
            isSameStorageObjectId(image.storageObjectId, selectedSourceStorageObjectId)
    );
    const selectedSourcePreviewUrl = selectedSourceImage
        ? resolveImageUrl(entryId, selectedSourceImage, "preview")
        : undefined;
    const hasSourceVisualImage = Boolean(
        selectedSourceImage ||
        visualAssetFormValue?.sourceImageStorageObjectId ||
        selectedVisualAsset?.sourceImageStorageObjectId
    );
    const hasGeneratedVisualImage = Boolean(
        visualAssetFormValue?.generatedImageStorageObjectId ||
        selectedVisualAsset?.generatedImageStorageObjectId
    );
    const sourcePreviewUrl =
        selectedSourcePreviewUrl ??
        resolveStorageUrl(
            hasSourceVisualImage
                ? (visualAssetFormValue?.sourcePreviewUrl ??
                      selectedVisualAsset?.sourcePreviewUrl ??
                      (entryId && selectedVisualAssetResourceId
                          ? entryService.getVisualAssetContentUrl({
                                entryId,
                                visualAssetId: selectedVisualAssetResourceId,
                                variant: "source"
                            })
                          : undefined))
                : undefined
        );
    const generatedPreviewUrl = resolveStorageUrl(
        hasGeneratedVisualImage
            ? (visualAssetFormValue?.generatedPreviewUrl ??
                  selectedVisualAsset?.generatedPreviewUrl ??
                  (entryId && selectedVisualAssetResourceId
                      ? entryService.getVisualAssetContentUrl({
                            entryId,
                            visualAssetId: selectedVisualAssetResourceId,
                            variant: "generated"
                        })
                      : undefined))
            : undefined
    );
    const saveVisualAsset = () => {
        if (!visualAssetFormValue || !onUpdateVisualAsset) {
            return;
        }
        onUpdateVisualAsset(visualAssetFormValue);
    };
    const selectVisualAsset = (asset: SancaiVisualAssetRecord) => {
        const assetId = asset.visualAssetId ?? asset.id ?? null;
        setSelectedVisualAssetId(assetId);
        setVisualAssetForm({ ...asset });
    };
    const updateVisualAssetForm = (patch: Partial<SancaiVisualAssetRecord>) => {
        setVisualAssetForm((currentForm) => {
            const baseForm = currentForm ?? visualAssetFormValue;
            return baseForm
                ? {
                      ...baseForm,
                      ...patch
                  }
                : currentForm;
        });
    };
    const selectVisualSourceImage = (image: SancaiEntryImageRecord) => {
        if (!image.storageObjectId) {
            return;
        }
        updateVisualAssetForm({
            sourceImageStorageObjectId: image.storageObjectId,
            sourcePreviewUrl: resolveImageUrl(entryId, image, "preview"),
            sourceDownloadUrl: resolveImageUrl(entryId, image, "download")
        });
    };
    const selectVisualSourceImageBySelectValue = (selectValue: string) => {
        const storageObjectId = selectValue.startsWith("storage:")
            ? selectValue.slice("storage:".length)
            : selectValue;
        const image = entryImages.find((entryImage) =>
            isSameStorageObjectId(entryImage.storageObjectId, storageObjectId)
        );
        if (image) {
            selectVisualSourceImage(image);
        }
    };
    const createVisualAssetTask = (capability: SancaiVisualAssetRefinementCapability) => {
        if (!visualAssetFormValue || !onCreateVisualAssetTask) {
            return;
        }
        onCreateVisualAssetTask(capability, visualAssetFormValue);
    };
    const requestSummaryTask = () => {
        if (!entryId) {
            return false;
        }
        if (!onCreateSummaryTask) {
            messageApi.warning("请先保存条目后再使用 AI摘要");
            return false;
        }
        if (!form.originalText?.trim()) {
            messageApi.warning("请先填写原文");
            return false;
        }
        onCreateSummaryTask(form);
        return true;
    };
    const openSummaryModal = () => {
        setSummaryDraft(form.summary || "");
        setLoadedSummaryCandidateId(null);
        setSummaryModalOpen(true);
    };
    const closeSummaryModal = () => {
        setSummaryModalOpen(false);
    };
    const applySummaryDraft = (draft: string, candidate: AiCandidateRecord | null) => {
        if (!entryId) {
            return;
        }
        const resultPayload = draft;
        if (candidate) {
            applyAiTextCandidateMutation.mutate({
                candidateId: candidate.candidateId,
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: "summary",
                objectId: candidate.objectId,
                resultFormat: candidate.resultFormat?.trim() || "TEXT",
                resultPayload,
                changeSummary: "AI 应用：摘要"
            });
            return;
        }
        setForm((currentForm) => ({
            ...currentForm,
            summary: resultPayload
        }));
        setSummaryModalOpen(false);
        messageApi.success("摘要已写入基础信息");
    };
    const changeCategory = (categoryId: number | null) => {
        setForm((currentForm) => {
            const currentVolume = volumes.find((volume) => volume.id === currentForm.volumeId);
            const volumeStillMatches = currentVolume?.categoryId === categoryId;
            return {
                ...currentForm,
                categoryId,
                volumeId: volumeStillMatches ? currentForm.volumeId : null
            };
        });
    };
    const submitForm = () => {
        if (!form.volumeId) {
            messageApi.warning("请选择卷");
            return;
        }
        onSubmit(form);
    };
    const uploadImageMutation = useMutation({
        mutationFn: (file: File) => {
            if (!entryId) {
                throw new Error("请先保存条目后再上传图片");
            }
            return entryService.uploadImage({
                currentUsed: true,
                entryId,
                file,
                imageType: "ORIGINAL",
                replaceImageId: currentImage?.id,
                title: file.name
            });
        },
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "entries", "images", entryId]
                }),
                queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] })
            ]);
            messageApi.success("三才图会图片已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "图片上传失败");
        }
    });

    if (!entry && mode !== "create") {
        return null;
    }

    const summaryModalProps: SancaiEntrySummaryModalProps = {
        aiTextDraft: summaryDraft,
        form,
        hasRunningAiTextTask: hasRunningSummaryTask,
        isAiTextApplyDisabled: isSummaryApplyDisabled,
        isAiTextCandidateFetching: summaryCandidatesQuery.isFetching,
        isAiTextCandidateLoadError: summaryCandidatesQuery.isError,
        isApplyingAiText: applyAiTextCandidateMutation.isPending,
        isCreatingAiTextTask: isCreatingSummaryTask,
        latestAiTextTask: latestSummaryTask,
        open: summaryModalOpen,
        onApply: () => applySummaryDraft(summaryDraft, loadedSummaryCandidate),
        onCancel: closeSummaryModal,
        onFetchTask: (taskId) => aiRefinementTaskService.getTask({ taskId: Number(taskId) }),
        onRequestTask: requestSummaryTask,
        onTaskChange: syncSummaryTask,
        onTextDraftChange: setSummaryDraft
    };

    const basicContent = (
        <SancaiEntryBasicSection
            categoryOptions={categoryOptions}
            currentImage={currentImage}
            downloadUrl={downloadUrl}
            entryId={entryId}
            form={form}
            imageContent={imageContent}
            isCreatingTranslationTask={isCreatingTranslationTask}
            isUploadingImage={uploadImageMutation.isPending}
            mode={mode}
            previewUrl={previewUrl}
            setForm={setForm}
            summaryModalProps={summaryModalProps}
            translationTasks={translationTasks}
            volumeOptions={volumeOptions}
            onChangeCategory={changeCategory}
            onOpenSummaryModal={openSummaryModal}
            onRequestTranslationTask={onCreateTranslationTask}
            onUploadImage={(file) => uploadImageMutation.mutate(file)}
        />
    );

    const visualAssetContent = entryId ? (
        <SancaiEntryVisualSection
            creatingVisualAssetCapability={creatingVisualAssetCapability}
            currentVisualAsset={currentVisualAsset}
            defaultSourceImage={defaultSourceImage}
            entryImages={entryImages}
            generatedPreviewUrl={generatedPreviewUrl}
            isUpdatingVisualAsset={isUpdatingVisualAsset}
            selectedSourceImage={selectedSourceImage}
            selectedSourceStorageObjectId={selectedSourceStorageObjectId}
            selectedVisualAsset={selectedVisualAsset}
            sourcePreviewUrl={sourcePreviewUrl}
            visualAssetFormValue={visualAssetFormValue}
            visualAssetsForSelectedSource={visualAssetsForSelectedSource}
            onCreateVisualAssetTask={onCreateVisualAssetTask ? createVisualAssetTask : undefined}
            onSaveVisualAsset={saveVisualAsset}
            onSelectVisualAsset={selectVisualAsset}
            onSelectVisualSourceImageBySelectValue={selectVisualSourceImageBySelectValue}
            onUpdateVisualAssetForm={updateVisualAssetForm}
            onUseVisualAsset={onUseVisualAsset}
        />
    ) : null;

    const openPreviewWindow = () => {
        const imageUrl =
            previewUrl && typeof window !== "undefined"
                ? new URL(previewUrl, window.location.origin).toString()
                : "";
        const visualUrl =
            generatedPreviewUrl && typeof window !== "undefined"
                ? new URL(generatedPreviewUrl, window.location.origin).toString()
                : "";
        const html = `<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>${escapeHtml(form.title || "三才图会条目预览")}</title>
<style>
body{margin:0;background:#f7f1e6;color:#2f2418;font:16px/1.75 "Songti SC","STSong","Noto Serif CJK SC",serif;}
main{max-width:960px;margin:0 auto;padding:48px 28px 64px;}
h1{margin:0 0 10px;font-size:30px;line-height:1.3;font-weight:800;}
h2{margin:32px 0 10px;font-size:18px;border-bottom:1px solid rgba(124,93,59,.28);padding-bottom:8px;}
.meta{display:flex;gap:12px;flex-wrap:wrap;color:#7c5d3b;font-size:14px;}
.paper{margin-top:24px;padding:28px;background:#fffaf0;border:1px solid rgba(124,93,59,.26);box-shadow:0 18px 48px rgba(72,48,24,.08);}
p{white-space:pre-wrap;margin:0;}
img{display:block;max-width:100%;height:auto;border:1px solid rgba(124,93,59,.24);background:#fffaf0;}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:18px;}
</style>
</head>
<body>
<main>
<h1>${escapeHtml(form.title || "未命名条目")}</h1>
<div class="meta">
<span>可见性：${escapeHtml(form.visibility)}</span>
<span>当前视觉处理记录：${escapeHtml(readVisualAssetTitle(currentVisualAsset))}</span>
</div>
<section class="paper">
<h2>原文</h2><p>${escapeHtml(form.originalText || "-")}</p>
<h2>译文</h2><p>${escapeHtml(form.translationText || "-")}</p>
<h2>摘要</h2><p>${escapeHtml(form.summary || "-")}</p>
${imageUrl || visualUrl ? `<h2>图像</h2><div class="grid">${imageUrl ? `<img src="${escapeHtml(imageUrl)}" alt="条目图片" />` : ""}${visualUrl ? `<img src="${escapeHtml(visualUrl)}" alt="视觉处理生成图" />` : ""}</div>` : ""}
${visualAssetFormValue?.visualDescription ? `<h2>视觉描述</h2><p>${escapeHtml(visualAssetFormValue.visualDescription)}</p>` : ""}
</section>
</main>
</body>
</html>`;
        const blob = new Blob([html], { type: "text/html;charset=utf-8" });
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank", "noopener,noreferrer");
        window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
    };

    const formProps = {
        className: "sancai-detail-card sancai-entry-edit-drawer-form",
        colon: false,
        component: "div" as const
    };
    const sections = [
        {
            label: "基础信息",
            value: "basic",
            content: <KuzhambuForm {...formProps}>{basicContent}</KuzhambuForm>
        },
        {
            label: "视觉处理",
            value: "visual",
            content: (
                <>
                    {visualAssetContent}
                    {visualRefinementContent}
                </>
            ),
            visible: mode === "edit"
        },
        {
            label: "标签",
            value: "tags",
            content: tagContent || (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
            ),
            visible: mode === "edit"
        },
        {
            label: "问答",
            value: "qa",
            content: qaContent || (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无问答" />
            ),
            visible: mode === "edit"
        },
        {
            label: "版本",
            value: "versions",
            content: versionContent || (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无版本" />
            ),
            visible: mode === "edit"
        }
    ] satisfies Array<{
        content: ReactNode;
        label: string;
        value: SancaiEntryEditDrawerSection;
        visible?: boolean;
    }>;

    return (
        <KuzhambuSegmentedDrawer
            activeSection={activeSection}
            extraClassName="sancai-entry-edit-drawer-header-actions"
            headerExtra={
                mode === "edit" ? (
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-preview-sancai-entry-button"
                        onClick={openPreviewWindow}
                    >
                        预览
                    </KuzhambuButton>
                ) : undefined
            }
            sectionClassName="sancai-entry-edit-drawer-section"
            sections={sections}
            segmentedClassName="sancai-entry-edit-drawer-header-sections"
            showSegmented={mode === "edit"}
            testId="classics-sancai-sancai-entry-editor-drawer"
            title={mode === "create" ? "新增条目" : "编辑条目"}
            open={open}
            size="large"
            destroyOnHidden
            footerActions={[
                {
                    testId: "classics-sancai-sancai-entry-cancel-button",
                    title: "取消",
                    action: onCancel
                },
                {
                    testId: "classics-sancai-sancai-entry-create-button",
                    title: "保存",
                    type: "primary",
                    loading: isSubmitting,
                    action: submitForm
                }
            ]}
            onClose={onCancel}
            onSectionChange={setActiveSection}
        />
    );
};
