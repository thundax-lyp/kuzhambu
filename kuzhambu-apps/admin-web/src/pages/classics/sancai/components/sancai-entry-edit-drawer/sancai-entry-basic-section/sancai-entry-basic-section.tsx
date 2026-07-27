import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Form, Input, Switch } from "antd";
import { useEffect, useMemo } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect,
    type KuzhambuTableSortPosition
} from "@/components";
import { SancaiEntryImageField } from "./sancai-entry-image-field";
import { SancaiEntrySummaryTextField } from "./sancai-entry-summary-text-field";
import { SancaiEntryTranslationTextField } from "./sancai-entry-translation-text-field";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";

const selectCurrentImage = (images: SancaiEntryImageRecord[]) => {
    return [...images]
        .filter((image) => image.currentUsed !== false)
        .sort((left, right) => (left.priority ?? 0) - (right.priority ?? 0))[0];
};

const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
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

interface SancaiEntryBasicPreviewState {
    imageUrl?: string;
}

interface SancaiEntryBasicSectionProps {
    categoryOptions: Array<{ label: string; value: number }>;
    entryId?: number;
    isCreatingSummaryTask: boolean;
    isCreatingTranslationTask: boolean;
    mode: "create" | "edit";
    summaryTasks: AiRefinementTaskRecord[];
    translationTasks: AiRefinementTaskRecord[];
    value: SancaiEntryFormValues;
    volumes: SancaiVolumeRecord[];
    onChange: (value: SancaiEntryFormValues) => void;
    onPreviewStateChange: (state: SancaiEntryBasicPreviewState) => void;
    onRequestSummaryTask?: (draft: SancaiEntryFormValues) => void;
    onRequestTranslationTask?: (draft: SancaiEntryFormValues) => void;
}

export const SancaiEntryBasicSection = ({
    categoryOptions,
    entryId,
    isCreatingSummaryTask,
    isCreatingTranslationTask,
    mode,
    summaryTasks,
    translationTasks,
    value,
    volumes,
    onChange,
    onPreviewStateChange,
    onRequestSummaryTask,
    onRequestTranslationTask
}: SancaiEntryBasicSectionProps) => {
    const { message: messageApi } = App.useApp();
    const [form] = Form.useForm<SancaiEntryFormValues>();
    const selectedCategoryId = Form.useWatch("categoryId", form) ?? value.categoryId;
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const imagesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "images", entryId],
        queryFn: () => entryService.listImages(entryId ?? 0),
        enabled: Boolean(entryId),
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
    const volumeOptions = useMemo(
        () =>
            volumes
                .filter((volume) => volume.categoryId === selectedCategoryId)
                .map((volume) => ({
                    label: volume.title?.trim() || `卷 ${volume.id}`,
                    value: volume.id
                })),
        [selectedCategoryId, volumes]
    );

    useEffect(() => {
        form.setFieldsValue(value);
    }, [form, value]);

    const invalidateEntryImages = async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "images", entryId]
            }),
            queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "entries"] })
        ]);
    };
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
            if (!entryId) {
                throw new Error("请先保存条目后再上传图片");
            }
            return entryService.uploadImage({
                currentUsed: false,
                entryId,
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

    const downloadImage = (image: SancaiEntryImageRecord) => {
        const downloadUrl = resolveImageUrl(entryId, image, "download");
        if (!downloadUrl) {
            return;
        }
        window.open(downloadUrl, "_blank", "noopener,noreferrer");
    };
    const changeCurrentImage = (image: SancaiEntryImageRecord) => {
        if (!entryId || image.currentUsed) {
            return;
        }
        changeCurrentImageMutation.mutate({
            entryId,
            imageId: image.id
        });
    };
    const deleteImage = (image: SancaiEntryImageRecord) => {
        if (!entryId) {
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
                    entryId,
                    imageId: image.id
                })
        });
    };
    const sortImage = (
        sourceImage: SancaiEntryImageRecord,
        targetImage: SancaiEntryImageRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (!entryId || sourceImage.id === targetImage.id) {
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
            entryId,
            orderedIds: nextImages.map((image) => image.id),
            sortDirection: "ASC"
        });
    };

    useEffect(() => {
        onPreviewStateChange({ imageUrl: previewUrl });
    }, [onPreviewStateChange, previewUrl]);

    const readFormValues = () => form.getFieldsValue(true) as SancaiEntryFormValues;
    const changeFormValues = (
        changedValues: Partial<SancaiEntryFormValues>,
        values: SancaiEntryFormValues
    ) => {
        if (Object.prototype.hasOwnProperty.call(changedValues, "categoryId")) {
            const currentVolume = volumes.find((volume) => volume.id === values.volumeId);
            if (currentVolume?.categoryId !== values.categoryId) {
                const nextValues = {
                    ...values,
                    volumeId: null
                };
                form.setFieldsValue({ volumeId: null });
                onChange(nextValues);
                return;
            }
        }
        onChange(values);
    };

    return (
        <KuzhambuForm
            form={form}
            className="sancai-detail-card sancai-entry-edit-drawer-form"
            colon={false}
            component="div"
            initialValues={value}
            onValuesChange={changeFormValues}
        >
            <KuzhambuFormItem name="categoryId" label="门类">
                <KuzhambuSelect
                    aria-label="三才图会条目门类"
                    placeholder="选择门类"
                    options={categoryOptions}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="volumeId" label="卷">
                <KuzhambuSelect
                    aria-label="三才图会条目卷"
                    disabled={!selectedCategoryId}
                    placeholder="选择卷"
                    options={volumeOptions}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="title" label="标题" layoutSize="large">
                <Input aria-label="三才图会条目标题" />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="originalText" label="原文" layoutSize="large">
                <Input.TextArea
                    aria-label="三才图会原文"
                    autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="translationText" label="译文" layoutSize="large">
                <SancaiEntryTranslationTextField
                    entryId={entryId}
                    getFormValues={readFormValues}
                    isCreatingTranslationTask={isCreatingTranslationTask}
                    mode={mode}
                    translationTasks={translationTasks}
                    onRequestTranslationTask={onRequestTranslationTask}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="summary" label="摘要" layoutSize="large">
                <SancaiEntrySummaryTextField
                    entryId={entryId}
                    getFormValues={readFormValues}
                    isCreatingSummaryTask={isCreatingSummaryTask}
                    mode={mode}
                    summaryTasks={summaryTasks}
                    onRequestSummaryTask={onRequestSummaryTask}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem
                name="visibility"
                label="可见性"
                layoutSize="large"
                valuePropName="checked"
                getValueProps={(visibility) => ({ checked: visibility === "PUBLIC" })}
                normalize={(checked) => (checked ? "PUBLIC" : "PRIVATE")}
            >
                <Switch
                    checkedChildren="公开"
                    unCheckedChildren="私有"
                    aria-label="三才图会公开状态"
                />
            </KuzhambuFormItem>
            {entryId ? (
                <KuzhambuFormItem label="图片" layoutSize="large">
                    <SancaiEntryImageField
                        deleteImageLoading={deleteImageMutation.isPending}
                        entryId={entryId}
                        images={entryImages}
                        isLoading={imagesQuery.isLoading}
                        isUploadingImage={uploadImageMutation.isPending}
                        onDeleteImage={deleteImage}
                        onDownloadImage={downloadImage}
                        onSortImage={sortImage}
                        onUploadImage={(file) => uploadImageMutation.mutate(file)}
                        onUseImage={changeCurrentImage}
                    />
                </KuzhambuFormItem>
            ) : null}
        </KuzhambuForm>
    );
};
