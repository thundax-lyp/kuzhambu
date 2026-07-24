import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { KuzhambuForm, KuzhambuSegmentedDrawer, KuzhambuButton } from "@/components";

import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";

import { SancaiEntryBasicSection } from "./sancai-entry-basic-section";
import { openSancaiEntryPreviewWindow } from "./sancai-entry-preview-window";
import { SancaiEntryVisualSection } from "./sancai-entry-visual-section";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-entry-form-values";
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

const isSameStorageObjectId = (
    left: number | string | null | undefined,
    right: number | string | null | undefined
) => {
    return left != null && right != null && String(left) === String(right);
};

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
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
    const entryId = mode === "edit" ? entry?.id : undefined;
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
        <SancaiEntryBasicSection
            categoryOptions={categoryOptions}
            currentImage={currentImage}
            downloadUrl={downloadUrl}
            entryId={entryId}
            form={form}
            imageContent={imageContent}
            isCreatingSummaryTask={isCreatingSummaryTask}
            isCreatingTranslationTask={isCreatingTranslationTask}
            isUploadingImage={uploadImageMutation.isPending}
            mode={mode}
            previewUrl={previewUrl}
            setForm={setForm}
            summaryTasks={summaryTasks}
            translationTasks={translationTasks}
            volumeOptions={volumeOptions}
            onChangeCategory={changeCategory}
            onRequestSummaryTask={onCreateSummaryTask}
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
        openSancaiEntryPreviewWindow({
            currentVisualAsset,
            form,
            imageUrl: previewUrl,
            visualDescription: visualAssetFormValue?.visualDescription,
            visualUrl: generatedPreviewUrl
        });
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
