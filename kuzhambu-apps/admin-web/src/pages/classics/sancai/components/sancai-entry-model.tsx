import {
    BranchesOutlined,
    CheckOutlined,
    DownloadOutlined,
    FileSearchOutlined,
    FileTextOutlined,
    PictureOutlined,
    TranslationOutlined,
    UploadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    App,
    Alert,
    Empty,
    Form,
    Image,
    Input,
    Segmented,
    Select,
    Switch,
    Tag,
    Typography,
    Upload
} from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-form-values";
import type { SancaiVisualAssetRefinementCapability } from "../services/sancai-entry-service";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "../sancai-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;
const imageAccept = ".jpg,.jpeg,.png,.gif,.webp";
type SancaiEntryModelSection = "basic" | "content" | "visual" | "tags" | "qa" | "versions";
const TRANSLATION_CANDIDATE_POLL_INTERVAL_MS = 3000;
const RUNNING_REFINEMENT_STATUSES = new Set(["PENDING", "RUNNING"]);

const formatSize = (size?: number | null) => {
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

const readVisualAssetId = (asset: SancaiVisualAssetRecord) => {
    return asset.visualAssetId ?? asset.id ?? 0;
};

const readVisualAssetStatusLabel = (status?: string | null) => {
    switch (status) {
        case "READY":
            return "已完成";
        case "QUEUED":
        case "PENDING":
            return "排队中";
        case "PROCESSING":
        case "RUNNING":
            return "正在处理";
        case "DRAFT":
            return "草稿";
        case "FAILED":
            return "失败";
        default:
            return status || "-";
    }
};

const readVisualAssetStatusTagColor = (status?: string | null) => {
    switch (status) {
        case "READY":
            return "success";
        case "QUEUED":
        case "PENDING":
        case "PROCESSING":
        case "RUNNING":
            return "warning";
        case "DRAFT":
            return "default";
        case "ERROR":
        case "FAILED":
            return "error";
        default:
            return "default";
    }
};

const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
};

const isSameStorageObjectId = (
    left: number | string | null | undefined,
    right: number | string | null | undefined
) => {
    return left != null && right != null && String(left) === String(right);
};

const readVisualSourceImageSelectValue = (image: SancaiEntryImageRecord | undefined) => {
    return image?.storageObjectId != null ? `storage:${image.storageObjectId}` : undefined;
};

const readRefinementTaskStatusLabel = (status?: string | null) => {
    switch (status) {
        case "PENDING":
            return "等待中";
        case "RUNNING":
            return "翻译中";
        case "SUCCEEDED":
            return "已完成";
        case "PARTIAL":
            return "部分完成";
        case "FAILED":
            return "失败";
        case "CANCELLED":
            return "已取消";
        default:
            return status || "-";
    }
};

const readRefinementTaskAlertType = (status?: string | null) => {
    if (status === "SUCCEEDED" || status === "PARTIAL") {
        return "success";
    }
    if (status === "FAILED" || status === "CANCELLED") {
        return "warning";
    }
    return "info";
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

interface SancaiEntryModelProps {
    imageContent?: ReactNode;
    qaContent?: ReactNode;
    refinementContent?: ReactNode;
    tagContent?: ReactNode;
    versionContent?: ReactNode;
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
    onCreateTranslationTask?: () => void;
    creatingVisualAssetCapability?: SancaiVisualAssetRefinementCapability | null;
    isCreatingTranslationTask?: boolean;
    translationTasks?: AiRefinementTaskRecord[];
    onSelectedVisualAssetChange?: (asset: SancaiVisualAssetRecord | null) => void;
    volumes?: Array<{ categoryId?: number | null; id: number; title?: string | null }>;
}

export const SancaiEntryModel = ({
    imageContent,
    qaContent,
    refinementContent,
    tagContent,
    versionContent,
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
    creatingVisualAssetCapability = null,
    isCreatingTranslationTask = false,
    translationTasks = [],
    onSelectedVisualAssetChange,
    volumes = []
}: SancaiEntryModelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<SancaiEntryFormValues>(() =>
        toEntryFormValues(entry, volumes, initialCategoryId, initialVolumeId)
    );
    const [activeSection, setActiveSection] = useState<SancaiEntryModelSection>("basic");
    const [isTranslationModalOpen, setIsTranslationModalOpen] = useState(false);
    const [translationDraft, setTranslationDraft] = useState("");
    const [loadedTranslationCandidateId, setLoadedTranslationCandidateId] = useState<number | null>(
        null
    );
    const entryId = mode === "edit" ? entry?.id : undefined;
    const latestTranslationTask = useMemo(
        () =>
            [...translationTasks]
                .filter((task) => task.capability === "translate")
                .sort((left, right) => {
                    if (
                        left.requestedAt &&
                        right.requestedAt &&
                        left.requestedAt !== right.requestedAt
                    ) {
                        return right.requestedAt.localeCompare(left.requestedAt);
                    }
                    return right.taskId - left.taskId;
                })[0] ?? null,
        [translationTasks]
    );
    const hasRunningTranslationTask =
        Boolean(latestTranslationTask?.status) &&
        RUNNING_REFINEMENT_STATUSES.has(latestTranslationTask?.status ?? "");
    const translationCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "translate", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: "translate",
                status: "PENDING"
            }),
        enabled: isTranslationModalOpen && Boolean(entryId),
        retry: false,
        refetchInterval: () => {
            return isCreatingTranslationTask || hasRunningTranslationTask
                ? TRANSLATION_CANDIDATE_POLL_INTERVAL_MS
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
    const latestTranslationCandidate = useMemo(() => {
        const candidates = translationCandidatesQuery.data || [];
        return [...candidates]
            .filter(
                (candidate) =>
                    candidate.capability === "translate" &&
                    candidate.status === "PENDING" &&
                    typeof candidate.resultPayload === "string" &&
                    candidate.resultPayload.trim().length > 0
            )
            .sort((left, right) => {
                if (
                    left.requestedAt &&
                    right.requestedAt &&
                    left.requestedAt !== right.requestedAt
                ) {
                    return right.requestedAt.localeCompare(left.requestedAt);
                }
                return right.candidateId - left.candidateId;
            })[0];
    }, [translationCandidatesQuery.data]);

    useEffect(() => {
        if (!isTranslationModalOpen || !latestTranslationCandidate) {
            return;
        }
        if (latestTranslationCandidate.candidateId === loadedTranslationCandidateId) {
            return;
        }
        const timer = window.setTimeout(() => {
            setLoadedTranslationCandidateId(latestTranslationCandidate.candidateId);
            setTranslationDraft(latestTranslationCandidate.resultPayload?.trim() || "");
        }, 0);
        return () => window.clearTimeout(timer);
    }, [isTranslationModalOpen, latestTranslationCandidate, loadedTranslationCandidateId]);

    useEffect(() => {
        if (!isTranslationModalOpen || !latestTranslationTask?.taskId) {
            return;
        }
        if (
            latestTranslationTask.status !== "SUCCEEDED" &&
            latestTranslationTask.status !== "PARTIAL"
        ) {
            return;
        }
        void translationCandidatesQuery.refetch();
    }, [
        isTranslationModalOpen,
        latestTranslationTask?.status,
        latestTranslationTask?.taskId,
        translationCandidatesQuery
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
    const selectedSourceImageSelectValue = readVisualSourceImageSelectValue(selectedSourceImage);
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
    const requestTranslationTask = () => {
        if (!entryId || !onCreateTranslationTask) {
            messageApi.warning("请先保存条目后再使用 AI 翻译");
            return false;
        }
        if (!form.originalText?.trim()) {
            messageApi.warning("请先填写原文");
            return false;
        }
        onCreateTranslationTask();
        return true;
    };
    const openTranslationModal = () => {
        setTranslationDraft(form.translationText || "");
        setLoadedTranslationCandidateId(null);
        setIsTranslationModalOpen(true);
    };
    const closeTranslationModal = () => {
        setIsTranslationModalOpen(false);
    };
    const applyTranslationDraft = () => {
        setForm((currentForm) => ({
            ...currentForm,
            translationText: translationDraft
        }));
        setIsTranslationModalOpen(false);
        messageApi.success("译文已写入基础信息");
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

    const basicContent = (
        <>
            <div className="sancai-entry-model-catalog-row">
                <Form.Item label="门类">
                    <Select
                        aria-label="三才图会条目门类"
                        placeholder="选择门类"
                        options={categoryOptions}
                        value={form.categoryId ?? undefined}
                        onChange={(value) => changeCategory(value ?? null)}
                    />
                </Form.Item>
                <Form.Item label="卷">
                    <Select
                        aria-label="三才图会条目卷"
                        disabled={!form.categoryId}
                        placeholder="选择卷"
                        options={volumeOptions}
                        value={form.volumeId ?? undefined}
                        onChange={(value) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                volumeId: value ?? null
                            }))
                        }
                    />
                </Form.Item>
            </div>
            <Form.Item label="标题">
                <Input
                    aria-label="三才图会条目标题"
                    value={form.title}
                    onChange={(event) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            title: event.target.value
                        }))
                    }
                />
            </Form.Item>
            <Form.Item label="原文" className="sancai-entry-model-form-item-top">
                <Input.TextArea
                    aria-label="三才图会原文"
                    value={form.originalText}
                    autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                    onChange={(event) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            originalText: event.target.value
                        }))
                    }
                />
            </Form.Item>
            <Form.Item label="译文" className="sancai-entry-model-form-item-top">
                <div className="sancai-entry-translation-field">
                    <Input.TextArea
                        aria-label="三才图会译文"
                        value={form.translationText}
                        autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                        onChange={(event) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                translationText: event.target.value
                            }))
                        }
                    />
                    {mode === "edit" ? (
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                name="AI翻译"
                                className="sancai-entry-ai-translation-button"
                                icon={<TranslationOutlined />}
                                onClick={openTranslationModal}
                            >
                                AI翻译
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    ) : null}
                </div>
            </Form.Item>
            <Form.Item label="摘要" className="sancai-entry-model-form-item-top">
                <Input.TextArea
                    aria-label="三才图会摘要"
                    value={form.summary}
                    autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                    onChange={(event) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            summary: event.target.value
                        }))
                    }
                />
            </Form.Item>
            <Form.Item label="可见性">
                <Switch
                    checked={form.visibility === "PUBLIC"}
                    checkedChildren="公开"
                    unCheckedChildren="私有"
                    aria-label="三才图会公开状态"
                    onChange={(checked) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            visibility: checked ? "PUBLIC" : "PRIVATE"
                        }))
                    }
                />
            </Form.Item>
            {entryId ? (
                <Form.Item label="图片">
                    {imageContent || (
                        <div className="sancai-entry-image-field">
                            {currentImage && previewUrl ? (
                                <div className="sancai-entry-image-frame">
                                    <>
                                        <Image
                                            width={180}
                                            src={previewUrl}
                                            alt={
                                                currentImage.title ||
                                                currentImage.originalFilename ||
                                                "三才图会图片"
                                            }
                                        />
                                        <Text type="secondary">
                                            {currentImage.originalFilename ||
                                                currentImage.title ||
                                                `图片 ${currentImage.id}`}{" "}
                                            - {formatSize(currentImage.size)}
                                        </Text>
                                    </>
                                </div>
                            ) : null}
                            <KuzhambuSpace wrap>
                                <Upload
                                    aria-label="上传三才图会图片"
                                    accept={imageAccept}
                                    showUploadList={false}
                                    beforeUpload={(file) => {
                                        uploadImageMutation.mutate(file);
                                        return Upload.LIST_IGNORE;
                                    }}
                                >
                                    <KuzhambuButton
                                        name="上传三才图会图片"
                                        icon={<UploadOutlined />}
                                        loading={uploadImageMutation.isPending}
                                    >
                                        上传
                                    </KuzhambuButton>
                                </Upload>
                                <KuzhambuButton
                                    name="下载三才图会图片"
                                    icon={<DownloadOutlined />}
                                    href={downloadUrl}
                                    target="_blank"
                                    disabled={!downloadUrl}
                                >
                                    下载
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </div>
                    )}
                </Form.Item>
            ) : null}
        </>
    );

    const visualAssetContent = entryId ? (
        <section className="sancai-visual-asset-field" aria-label="三才图会视觉处理面板">
            <div className="sancai-visual-asset-summary">
                <Text type="secondary">
                    来源图片：{selectedSourceImage ? readImageTitle(selectedSourceImage) : "未选择"}
                </Text>
                <Text type="secondary">当前处理：{readVisualAssetTitle(currentVisualAsset)}</Text>
                <Text type="secondary">已选处理：{readVisualAssetTitle(selectedVisualAsset)}</Text>
                {selectedVisualAsset ? (
                    <Text type="secondary">
                        状态：{readVisualAssetStatusLabel(selectedVisualAsset.status)}
                    </Text>
                ) : null}
            </div>
            {selectedVisualAsset ? (
                <>
                    <Form.Item label="来源图片">
                        <Select
                            aria-label="三才图会视觉处理来源图片"
                            disabled={!defaultSourceImage}
                            placeholder="选择来源图片"
                            value={selectedSourceImageSelectValue}
                            options={entryImages.map((image) => ({
                                disabled: !image.storageObjectId,
                                label: readImageTitle(image),
                                value:
                                    readVisualSourceImageSelectValue(image) ?? `image:${image.id}`
                            }))}
                            onChange={(value) => selectVisualSourceImageBySelectValue(value)}
                        />
                    </Form.Item>
                    <div className="sancai-visual-asset-image-list">
                        <div className="sancai-entry-image-frame">
                            {sourcePreviewUrl ? (
                                <Image
                                    width={180}
                                    src={sourcePreviewUrl}
                                    alt="三才图会视觉处理来源图片"
                                />
                            ) : (
                                <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description="未选择来源图片"
                                />
                            )}
                            <div className="sancai-visual-asset-image-actions">
                                <Text type="secondary">来源图片</Text>
                            </div>
                        </div>
                        <div className="sancai-entry-image-frame">
                            {generatedPreviewUrl ? (
                                <Image
                                    width={180}
                                    src={generatedPreviewUrl}
                                    alt="三才图会视觉处理生成图"
                                />
                            ) : (
                                <div
                                    className="sancai-visual-generated-placeholder"
                                    role="img"
                                    aria-label="三才图会视觉处理生成图占位"
                                >
                                    <PictureOutlined />
                                    <Text type="secondary">未生成图片</Text>
                                </div>
                            )}
                            <div className="sancai-visual-asset-image-actions">
                                <Text type="secondary">生成图</Text>
                            </div>
                        </div>
                    </div>
                    {visualAssetsForSelectedSource.length > 0 ? (
                        <KuzhambuTable
                            className="sancai-visual-asset-version-table"
                            ariaLabel="三才图会视觉处理记录列表"
                            columns={
                                [
                                    {
                                        title: "处理记录",
                                        key: "version",
                                        width: 120,
                                        render: (_, asset) => readVisualAssetTitle(asset)
                                    },
                                    {
                                        title: "图片",
                                        key: "preview",
                                        width: 84,
                                        render: (_, asset) => {
                                            const previewUrl = resolveStorageUrl(
                                                asset.generatedPreviewUrl ??
                                                    asset.generatedDownloadUrl
                                            );
                                            const fullImageUrl = resolveStorageUrl(
                                                asset.generatedDownloadUrl ??
                                                    asset.generatedPreviewUrl
                                            );
                                            if (asset.status !== "READY" || !previewUrl) {
                                                return <Text type="secondary">-</Text>;
                                            }
                                            return (
                                                <Image
                                                    width={56}
                                                    height={56}
                                                    className="sancai-visual-asset-table-preview"
                                                    src={previewUrl}
                                                    alt={`${readVisualAssetTitle(asset)}生成图预览`}
                                                    preview={{ src: fullImageUrl ?? previewUrl }}
                                                />
                                            );
                                        }
                                    },
                                    {
                                        title: "状态",
                                        dataIndex: "status",
                                        key: "status",
                                        width: 96,
                                        render: (status?: string | null) =>
                                            status ? (
                                                <Tag color={readVisualAssetStatusTagColor(status)}>
                                                    {readVisualAssetStatusLabel(status)}
                                                </Tag>
                                            ) : (
                                                <Text type="secondary">-</Text>
                                            )
                                    },
                                    {
                                        title: "当前",
                                        dataIndex: "currentUsed",
                                        key: "currentUsed",
                                        width: 72,
                                        render: (currentUsed?: boolean | null) =>
                                            currentUsed ? (
                                                <CheckOutlined
                                                    aria-label="当前使用"
                                                    className="sancai-image-current-icon"
                                                />
                                            ) : (
                                                <Text type="secondary">-</Text>
                                            )
                                    },
                                    {
                                        inlineLimit: 2,
                                        key: "actions",
                                        options: (asset) => {
                                            const assetId = readVisualAssetId(asset);
                                            const isSelected =
                                                assetId > 0 &&
                                                assetId === readVisualAssetId(selectedVisualAsset);
                                            return [
                                                {
                                                    key: "select",
                                                    text: "选择",
                                                    ariaLabel: `选择${readVisualAssetTitle(asset)}`,
                                                    disabled:
                                                        !selectedSourceStorageObjectId ||
                                                        isSelected,
                                                    onClick: (record) => selectVisualAsset(record)
                                                },
                                                {
                                                    key: "use",
                                                    text: "当前",
                                                    ariaLabel: `设为当前视觉处理 ${readVisualAssetTitle(asset)}`,
                                                    disabled:
                                                        !selectedSourceStorageObjectId ||
                                                        Boolean(asset.currentUsed),
                                                    onClick: (record) => onUseVisualAsset?.(record)
                                                }
                                            ];
                                        }
                                    }
                                ] satisfies KuzhambuTableProps<SancaiVisualAssetRecord>["columns"]
                            }
                            dataSource={visualAssetsForSelectedSource}
                            pagination={false}
                            rowKey={(asset) => readVisualAssetId(asset)}
                            size="small"
                        />
                    ) : (
                        <div className="sancai-visual-asset-empty-records">
                            <Text type="secondary">
                                {selectedSourceStorageObjectId
                                    ? "当前来源图片暂无处理记录"
                                    : "请先选择来源图片"}
                            </Text>
                        </div>
                    )}
                    <div className="sancai-visual-asset-toolbar">
                        {onCreateVisualAssetTask ? (
                            <div className="sancai-visual-workflow" aria-label="图文生图工作流">
                                <KuzhambuSpace wrap>
                                    <KuzhambuButton
                                        name="图片理解"
                                        icon={<FileSearchOutlined />}
                                        loading={creatingVisualAssetCapability === "image_analysis"}
                                        onClick={() => {
                                            createVisualAssetTask("image_analysis");
                                        }}
                                    >
                                        图片理解
                                    </KuzhambuButton>
                                    <span className="sancai-visual-workflow-arrow">›</span>
                                    <KuzhambuButton
                                        name="信息融合"
                                        icon={<BranchesOutlined />}
                                        loading={creatingVisualAssetCapability === "fusion"}
                                        onClick={() => {
                                            createVisualAssetTask("fusion");
                                        }}
                                    >
                                        信息融合
                                    </KuzhambuButton>
                                    <span className="sancai-visual-workflow-arrow">›</span>
                                    <KuzhambuButton
                                        name="视觉描述"
                                        icon={<FileTextOutlined />}
                                        loading={creatingVisualAssetCapability === "visual"}
                                        onClick={() => {
                                            createVisualAssetTask("visual");
                                        }}
                                    >
                                        视觉描述
                                    </KuzhambuButton>
                                    <span className="sancai-visual-workflow-arrow">›</span>
                                    <KuzhambuButton
                                        name="生图"
                                        icon={<PictureOutlined />}
                                        loading={creatingVisualAssetCapability === "image_gen"}
                                        onClick={() => {
                                            createVisualAssetTask("image_gen");
                                        }}
                                    >
                                        生图
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                            </div>
                        ) : null}
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                name="采纳视觉处理"
                                icon={<CheckOutlined />}
                                type="primary"
                                loading={isUpdatingVisualAsset}
                                onClick={saveVisualAsset}
                            >
                                采纳
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    </div>
                    <div className="sancai-entry-model-catalog-row">
                        <Form.Item label="文本权重">
                            <Input
                                aria-label="三才图会视觉处理文本权重"
                                value={visualAssetFormValue?.textWeight ?? ""}
                                onChange={(event) =>
                                    updateVisualAssetForm({
                                        textWeight: event.target.value
                                            ? Number(event.target.value)
                                            : null
                                    })
                                }
                            />
                        </Form.Item>
                        <Form.Item label="图片权重">
                            <Input
                                aria-label="三才图会视觉处理图片权重"
                                value={visualAssetFormValue?.imageWeight ?? ""}
                                onChange={(event) =>
                                    updateVisualAssetForm({
                                        imageWeight: event.target.value
                                            ? Number(event.target.value)
                                            : null
                                    })
                                }
                            />
                        </Form.Item>
                    </div>
                    <Form.Item label="图片理解" className="sancai-entry-model-form-item-top">
                        <Input.TextArea
                            aria-label="三才图会视觉处理图片理解"
                            value={visualAssetFormValue?.imageAnalysisMarkdown ?? ""}
                            autoSize={resolveTextAreaAutoSize({
                                minRows: 3,
                                maxRows: 6
                            })}
                            onChange={(event) =>
                                updateVisualAssetForm({
                                    imageAnalysisMarkdown: event.target.value
                                })
                            }
                        />
                    </Form.Item>
                    <Form.Item label="融合描述" className="sancai-entry-model-form-item-top">
                        <Input.TextArea
                            aria-label="三才图会视觉处理融合描述"
                            value={visualAssetFormValue?.fusionDescription ?? ""}
                            autoSize={resolveTextAreaAutoSize({
                                minRows: 2,
                                maxRows: 5
                            })}
                            onChange={(event) =>
                                updateVisualAssetForm({
                                    fusionDescription: event.target.value
                                })
                            }
                        />
                    </Form.Item>
                    <Form.Item label="视觉描述" className="sancai-entry-model-form-item-top">
                        <Input.TextArea
                            aria-label="三才图会视觉处理视觉描述"
                            value={visualAssetFormValue?.visualDescription ?? ""}
                            autoSize={resolveTextAreaAutoSize({
                                minRows: 2,
                                maxRows: 5
                            })}
                            onChange={(event) =>
                                updateVisualAssetForm({
                                    visualDescription: event.target.value
                                })
                            }
                        />
                    </Form.Item>
                    <Form.Item label="生成参数" className="sancai-entry-model-form-item-top">
                        <Input.TextArea
                            aria-label="三才图会视觉处理生成参数"
                            value={visualAssetFormValue?.generationParamsJson ?? ""}
                            autoSize={resolveTextAreaAutoSize({
                                minRows: 2,
                                maxRows: 5
                            })}
                            onChange={(event) =>
                                updateVisualAssetForm({
                                    generationParamsJson: event.target.value
                                })
                            }
                        />
                    </Form.Item>
                </>
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无视觉处理记录" />
            )}
        </section>
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
        className: "sancai-detail-card sancai-entry-model-form",
        colon: false,
        component: "div" as const,
        labelCol: { flex: "88px" },
        layout: "horizontal" as const
    };
    const sectionOptions = [
        { label: "基础信息", value: "basic" },
        { label: "内容处理", value: "content" },
        { label: "视觉处理", value: "visual" },
        { label: "标签", value: "tags" },
        { label: "问答", value: "qa" },
        { label: "版本", value: "versions" }
    ];

    return (
        <KuzhambuDrawer
            title={mode === "create" ? "新增条目" : "编辑条目"}
            open={open}
            size="large"
            destroyOnHidden
            extra={
                mode === "edit" ? (
                    <KuzhambuSpace className="sancai-entry-model-header-actions" wrap>
                        <Segmented
                            className="sancai-entry-model-header-sections"
                            options={sectionOptions}
                            value={activeSection}
                            onChange={(value) => setActiveSection(value as SancaiEntryModelSection)}
                        />
                        <KuzhambuButton name="预览三才图会条目" onClick={openPreviewWindow}>
                            预览
                        </KuzhambuButton>
                    </KuzhambuSpace>
                ) : undefined
            }
            footer={
                <div className="sancai-drawer-footer">
                    <KuzhambuButton name="取消" onClick={onCancel}>
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        name={String(
                            mode === "create" ? "保存新增三才图会条目" : "保存三才图会条目"
                        )}
                        type="primary"
                        loading={isSubmitting}
                        onClick={submitForm}
                    >
                        保存
                    </KuzhambuButton>
                </div>
            }
            onClose={onCancel}
        >
            <KuzhambuModal
                title="AI翻译"
                open={isTranslationModalOpen}
                width={960}
                destroyOnHidden
                footer={
                    <div className="sancai-modal-footer">
                        <KuzhambuButton name="取消AI翻译" onClick={closeTranslationModal}>
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            name="采用AI译文"
                            type="primary"
                            disabled={!translationDraft.trim()}
                            onClick={applyTranslationDraft}
                        >
                            采用
                        </KuzhambuButton>
                    </div>
                }
                onCancel={closeTranslationModal}
            >
                <div className="sancai-translation-modal-toolbar">
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            name="翻译"
                            icon={<TranslationOutlined />}
                            type="primary"
                            loading={isCreatingTranslationTask}
                            onClick={requestTranslationTask}
                        >
                            翻译
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </div>
                {isCreatingTranslationTask || latestTranslationTask ? (
                    <Alert
                        showIcon
                        className="sancai-translation-task-alert"
                        type={
                            isCreatingTranslationTask
                                ? "info"
                                : readRefinementTaskAlertType(latestTranslationTask?.status)
                        }
                        message={
                            isCreatingTranslationTask
                                ? "正在创建翻译任务"
                                : `翻译任务：${readRefinementTaskStatusLabel(
                                      latestTranslationTask?.status
                                  )}`
                        }
                        description={
                            hasRunningTranslationTask
                                ? "任务完成后会自动刷新 AI 译文。"
                                : latestTranslationTask?.errorMessage || undefined
                        }
                    />
                ) : null}
                <Form
                    className="sancai-detail-card sancai-entry-model-form sancai-translation-modal-original"
                    colon={false}
                    component="div"
                    layout="vertical"
                >
                    <Form.Item label="原文">
                        <Input.TextArea
                            aria-label="AI翻译原文"
                            value={form.originalText}
                            readOnly
                            autoSize={resolveTextAreaAutoSize({ minRows: 5, maxRows: 8 })}
                        />
                    </Form.Item>
                </Form>
                <div className="sancai-translation-modal-compare-grid">
                    <Form
                        className="sancai-detail-card sancai-entry-model-form"
                        colon={false}
                        component="div"
                        layout="vertical"
                    >
                        <Form.Item label="当前译文">
                            <Input.TextArea
                                aria-label="AI翻译当前译文"
                                value={form.translationText}
                                readOnly
                                autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 16 })}
                            />
                        </Form.Item>
                    </Form>
                    <Form
                        className="sancai-detail-card sancai-entry-model-form"
                        colon={false}
                        component="div"
                        layout="vertical"
                    >
                        <Form.Item label="AI译文">
                            <Input.TextArea
                                aria-label="AI翻译AI译文"
                                value={translationDraft}
                                placeholder={
                                    isCreatingTranslationTask ||
                                    translationCandidatesQuery.isFetching
                                        ? "AI 翻译生成中..."
                                        : "暂无候选译文，可先保留当前译文或稍后重试"
                                }
                                autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 16 })}
                                onChange={(event) => setTranslationDraft(event.target.value)}
                            />
                        </Form.Item>
                        {translationCandidatesQuery.isError ? (
                            <Alert
                                showIcon
                                type="warning"
                                message="候选译文加载失败"
                                description="AI 任务可能仍在执行，请稍后重新打开。"
                            />
                        ) : null}
                    </Form>
                </div>
            </KuzhambuModal>
            {mode === "create" ? (
                <Form {...formProps}>{basicContent}</Form>
            ) : (
                <div className="sancai-entry-model-section">
                    {activeSection === "basic" ? <Form {...formProps}>{basicContent}</Form> : null}
                    {activeSection === "content"
                        ? refinementContent || (
                              <Empty
                                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                                  description="暂无内容处理任务"
                              />
                          )
                        : null}
                    {activeSection === "visual" ? (
                        <Form {...formProps}>{visualAssetContent}</Form>
                    ) : null}
                    {activeSection === "tags"
                        ? tagContent || (
                              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
                          )
                        : null}
                    {activeSection === "qa"
                        ? qaContent || (
                              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无问答" />
                          )
                        : null}
                    {activeSection === "versions"
                        ? versionContent || (
                              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无版本" />
                          )
                        : null}
                </div>
            )}
        </KuzhambuDrawer>
    );
};
