import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Input, Switch } from "antd";
import type { Dispatch, ReactNode, SetStateAction } from "react";
import { useEffect, useMemo } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuForm, KuzhambuFormItem, KuzhambuSelect } from "@/components";
import { SancaiEntryImageField } from "./sancai-entry-image-field";
import { SancaiEntrySummaryTextField } from "./sancai-entry-summary-text-field";
import { SancaiEntryTranslationTextField } from "./sancai-entry-translation-text-field";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord
} from "@/pages/classics/sancai/sancai-types";

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

interface SancaiEntryBasicPreviewState {
    imageUrl?: string;
}

interface SancaiEntryBasicSectionProps {
    categoryOptions: Array<{ label: string; value: number }>;
    entryId?: number;
    form: SancaiEntryFormValues;
    imageContent?: ReactNode;
    isCreatingSummaryTask: boolean;
    isCreatingTranslationTask: boolean;
    mode: "create" | "edit";
    setForm: Dispatch<SetStateAction<SancaiEntryFormValues>>;
    summaryTasks: AiRefinementTaskRecord[];
    translationTasks: AiRefinementTaskRecord[];
    volumeOptions: Array<{ label: string; value: number }>;
    onChangeCategory: (categoryId: number | null) => void;
    onPreviewStateChange: (state: SancaiEntryBasicPreviewState) => void;
    onRequestSummaryTask?: (draft: SancaiEntryFormValues) => void;
    onRequestTranslationTask?: (draft: SancaiEntryFormValues) => void;
}

export const SancaiEntryBasicSection = ({
    categoryOptions,
    entryId,
    form,
    imageContent,
    isCreatingSummaryTask,
    isCreatingTranslationTask,
    mode,
    setForm,
    summaryTasks,
    translationTasks,
    volumeOptions,
    onChangeCategory,
    onPreviewStateChange,
    onRequestSummaryTask,
    onRequestTranslationTask
}: SancaiEntryBasicSectionProps) => {
    const { message: messageApi } = App.useApp();
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
    const downloadUrl = resolveImageUrl(entryId, currentImage, "download");
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

    useEffect(() => {
        onPreviewStateChange({ imageUrl: previewUrl });
    }, [onPreviewStateChange, previewUrl]);

    return (
        <KuzhambuForm
            className="sancai-detail-card sancai-entry-edit-drawer-form"
            colon={false}
            component="div"
        >
            <KuzhambuFormItem label="门类">
                <KuzhambuSelect
                    aria-label="三才图会条目门类"
                    placeholder="选择门类"
                    options={categoryOptions}
                    value={form.categoryId ?? undefined}
                    onChange={(value) => onChangeCategory(value ?? null)}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="卷">
                <KuzhambuSelect
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
            </KuzhambuFormItem>
            <KuzhambuFormItem label="标题" layoutSize="large">
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
            </KuzhambuFormItem>
            <KuzhambuFormItem label="原文" layoutSize="large">
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
            </KuzhambuFormItem>
            <KuzhambuFormItem label="译文" layoutSize="large">
                <SancaiEntryTranslationTextField
                    entryId={entryId}
                    form={form}
                    isCreatingTranslationTask={isCreatingTranslationTask}
                    mode={mode}
                    translationTasks={translationTasks}
                    value={form.translationText}
                    onChange={(translationText) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            translationText
                        }))
                    }
                    onRequestTranslationTask={onRequestTranslationTask}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="摘要" layoutSize="large">
                <SancaiEntrySummaryTextField
                    entryId={entryId}
                    form={form}
                    isCreatingSummaryTask={isCreatingSummaryTask}
                    mode={mode}
                    summaryTasks={summaryTasks}
                    value={form.summary}
                    onChange={(summary) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            summary
                        }))
                    }
                    onRequestSummaryTask={onRequestSummaryTask}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="可见性" layoutSize="large">
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
            </KuzhambuFormItem>
            {entryId ? (
                <KuzhambuFormItem label="图片" layoutSize="large">
                    <SancaiEntryImageField
                        content={imageContent}
                        currentImage={currentImage}
                        downloadUrl={downloadUrl}
                        isUploadingImage={uploadImageMutation.isPending}
                        previewUrl={previewUrl}
                        onUploadImage={(file) => uploadImageMutation.mutate(file)}
                    />
                </KuzhambuFormItem>
            ) : null}
        </KuzhambuForm>
    );
};
