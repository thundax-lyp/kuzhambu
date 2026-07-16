import { DownloadOutlined, EyeOutlined, PictureOutlined, UploadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Empty, Form, Image, Input, Select, Switch, Typography, Upload } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
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
        return "未选择视觉资产";
    }
    return `版本 ${asset.versionNo ?? asset.visualAssetId ?? asset.id ?? "-"}`;
};

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

interface SancaiEntryModelProps {
    afterForm?: ReactNode;
    categoryOptions?: Array<{ label: string; value: number }>;
    entry: SancaiEntryRecord | undefined;
    initialCategoryId?: number | null;
    initialVolumeId?: number | null;
    isSubmitting: boolean;
    isSwitchingVisualAsset?: boolean;
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
    creatingVisualAssetCapability?: SancaiVisualAssetRefinementCapability | null;
    onSelectedVisualAssetChange?: (asset: SancaiVisualAssetRecord | null) => void;
    volumes?: Array<{ categoryId?: number | null; id: number; title?: string | null }>;
}

export const SancaiEntryModel = ({
    afterForm,
    categoryOptions = [],
    entry,
    initialCategoryId = null,
    initialVolumeId = null,
    isSubmitting,
    isSwitchingVisualAsset = false,
    isUpdatingVisualAsset = false,
    mode = "edit",
    open,
    onCancel,
    onSubmit,
    onUseVisualAsset,
    onUpdateVisualAsset,
    onCreateVisualAssetTask,
    creatingVisualAssetCapability = null,
    onSelectedVisualAssetChange,
    volumes = []
}: SancaiEntryModelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<SancaiEntryFormValues>(() =>
        toEntryFormValues(entry, volumes, initialCategoryId, initialVolumeId)
    );
    const entryId = mode === "edit" ? entry?.id : undefined;
    const imagesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "images", entryId],
        queryFn: () => entryService.listImages(entryId ?? 0),
        enabled: open && Boolean(entryId),
        retry: false
    });
    const currentImage = selectCurrentImage(imagesQuery.data || []);
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
    const visualAssetFormValue = useMemo(() => {
        if (!selectedVisualAsset) {
            return null;
        }
        const selectedId = selectedVisualAsset.visualAssetId ?? selectedVisualAsset.id ?? null;
        const formId = visualAssetForm?.visualAssetId ?? visualAssetForm?.id ?? null;
        return formId === selectedId ? visualAssetForm : { ...selectedVisualAsset };
    }, [selectedVisualAsset, visualAssetForm]);
    const selectedVisualAssetResourceId =
        selectedVisualAsset?.visualAssetId ?? selectedVisualAsset?.id;
    const sourcePreviewUrl = resolveStorageUrl(
        selectedVisualAsset?.sourcePreviewUrl ??
            (entryId && selectedVisualAssetResourceId
                ? entryService.getVisualAssetContentUrl({
                      entryId,
                      visualAssetId: selectedVisualAssetResourceId,
                      variant: "source"
                  })
                : undefined)
    );
    const sourceDownloadUrl = resolveStorageUrl(
        selectedVisualAsset?.sourceDownloadUrl ??
            (entryId && selectedVisualAssetResourceId
                ? entryService.getVisualAssetContentUrl({
                      entryId,
                      visualAssetId: selectedVisualAssetResourceId,
                      variant: "source",
                      mode: "download"
                  })
                : undefined)
    );
    const generatedPreviewUrl = resolveStorageUrl(
        selectedVisualAsset?.generatedPreviewUrl ??
            (entryId && selectedVisualAssetResourceId
                ? entryService.getVisualAssetContentUrl({
                      entryId,
                      visualAssetId: selectedVisualAssetResourceId,
                      variant: "generated"
                  })
                : undefined)
    );
    const generatedDownloadUrl = resolveStorageUrl(
        selectedVisualAsset?.generatedDownloadUrl ??
            (entryId && selectedVisualAssetResourceId
                ? entryService.getVisualAssetContentUrl({
                      entryId,
                      visualAssetId: selectedVisualAssetResourceId,
                      variant: "generated",
                      mode: "download"
                  })
                : undefined)
    );
    const canSwitchVisualAsset =
        Boolean(selectedVisualAsset) &&
        (selectedVisualAsset?.visualAssetId ?? selectedVisualAsset?.id) !==
            (currentVisualAsset?.visualAssetId ?? currentVisualAsset?.id);
    const saveVisualAsset = () => {
        if (!visualAssetFormValue || !onUpdateVisualAsset) {
            return;
        }
        onUpdateVisualAsset(visualAssetFormValue);
    };
    const activateVisualAsset = () => {
        if (!selectedVisualAsset || !onUseVisualAsset) {
            return;
        }
        onUseVisualAsset(selectedVisualAsset);
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
    const createVisualAssetTask = (capability: SancaiVisualAssetRefinementCapability) => {
        if (!selectedVisualAsset || !onCreateVisualAssetTask) {
            return;
        }
        onCreateVisualAssetTask(capability, selectedVisualAsset);
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

    return (
        <KuzhambuDrawer
            title={mode === "create" ? "新增条目" : "编辑条目"}
            open={open}
            size="large"
            destroyOnHidden
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
            <Form
                className="sancai-detail-card sancai-entry-model-form"
                colon={false}
                component="div"
                labelCol={{ flex: "88px" }}
                layout="horizontal"
            >
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
                    </Form.Item>
                ) : null}
                {entryId ? (
                    <section className="sancai-form-field" aria-label="三才图会视觉资产面板">
                        <Text strong>视觉资产</Text>
                        <Text type="secondary">
                            当前版本：{readVisualAssetTitle(currentVisualAsset)}
                            {currentVisualAsset?.status
                                ? ` · 状态 ${currentVisualAsset.status}`
                                : ""}
                        </Text>
                        <Text type="secondary">
                            已选版本：{readVisualAssetTitle(selectedVisualAsset)}
                            {selectedVisualAsset?.status
                                ? ` · 状态 ${selectedVisualAsset.status}`
                                : ""}
                        </Text>
                        {selectedVisualAsset ? (
                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: 12,
                                    width: "100%"
                                }}
                            >
                                <KuzhambuSpace wrap>
                                    {orderedVisualAssets.map((asset) => {
                                        const assetId = asset.visualAssetId ?? asset.id;
                                        if (!assetId) {
                                            return null;
                                        }
                                        return (
                                            <KuzhambuButton
                                                name={String(
                                                    `版本 ${asset.versionNo ?? assetId}${
                                                        asset.currentUsed ? " · 当前使用" : ""
                                                    }`
                                                )}
                                                key={assetId}
                                                type={
                                                    assetId ===
                                                    (selectedVisualAsset?.visualAssetId ??
                                                        selectedVisualAsset?.id)
                                                        ? "primary"
                                                        : "default"
                                                }
                                                ghost={
                                                    assetId !==
                                                    (selectedVisualAsset?.visualAssetId ??
                                                        selectedVisualAsset?.id)
                                                }
                                                onClick={() => selectVisualAsset(asset)}
                                            >
                                                {`版本 ${asset.versionNo ?? assetId}${
                                                    asset.currentUsed ? " · 当前使用" : ""
                                                }`}
                                            </KuzhambuButton>
                                        );
                                    })}
                                </KuzhambuSpace>
                                <KuzhambuSpace wrap>
                                    {onCreateVisualAssetTask ? (
                                        <>
                                            <KuzhambuButton
                                                name="创建图片理解任务"
                                                type="default"
                                                loading={
                                                    creatingVisualAssetCapability ===
                                                    "image_analysis"
                                                }
                                                onClick={() => {
                                                    createVisualAssetTask("image_analysis");
                                                }}
                                            >
                                                创建图片理解任务
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                name="创建信息融合任务"
                                                loading={creatingVisualAssetCapability === "fusion"}
                                                onClick={() => {
                                                    createVisualAssetTask("fusion");
                                                }}
                                            >
                                                创建信息融合任务
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                name="创建视觉描述任务"
                                                loading={creatingVisualAssetCapability === "visual"}
                                                onClick={() => {
                                                    createVisualAssetTask("visual");
                                                }}
                                            >
                                                创建视觉描述任务
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                name="创建生图任务"
                                                loading={
                                                    creatingVisualAssetCapability === "image_gen"
                                                }
                                                onClick={() => {
                                                    createVisualAssetTask("image_gen");
                                                }}
                                            >
                                                创建生图任务
                                            </KuzhambuButton>
                                        </>
                                    ) : null}
                                    <KuzhambuButton
                                        name="保存视觉资产字段"
                                        type="primary"
                                        loading={isUpdatingVisualAsset}
                                        onClick={saveVisualAsset}
                                    >
                                        保存视觉资产字段
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        name="设为当前使用版本"
                                        loading={isSwitchingVisualAsset}
                                        disabled={!canSwitchVisualAsset}
                                        onClick={activateVisualAsset}
                                    >
                                        设为当前使用版本
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                                <KuzhambuSpace wrap>
                                    <KuzhambuButton
                                        name="预览视觉资产原图"
                                        icon={<EyeOutlined />}
                                        href={sourcePreviewUrl}
                                        target="_blank"
                                        disabled={!sourcePreviewUrl}
                                    >
                                        预览原图
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        name="下载视觉资产原图"
                                        icon={<DownloadOutlined />}
                                        href={sourceDownloadUrl}
                                        target="_blank"
                                        disabled={!sourceDownloadUrl}
                                    >
                                        下载原图
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        name="预览视觉资产生成图"
                                        icon={<PictureOutlined />}
                                        href={generatedPreviewUrl}
                                        target="_blank"
                                        disabled={!generatedPreviewUrl}
                                    >
                                        预览生成图
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        name="下载视觉资产生成图"
                                        icon={<DownloadOutlined />}
                                        href={generatedDownloadUrl}
                                        target="_blank"
                                        disabled={!generatedDownloadUrl}
                                    >
                                        下载生成图
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                                <KuzhambuSpace wrap align="start">
                                    {sourcePreviewUrl ? (
                                        <Image
                                            width={180}
                                            src={sourcePreviewUrl}
                                            alt="三才图会视觉资产原图"
                                        />
                                    ) : (
                                        <Empty
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                            description="未关联原图"
                                        />
                                    )}
                                    {generatedPreviewUrl ? (
                                        <Image
                                            width={180}
                                            src={generatedPreviewUrl}
                                            alt="三才图会视觉资产生成图"
                                        />
                                    ) : (
                                        <Empty
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                            description="未关联生成图"
                                        />
                                    )}
                                </KuzhambuSpace>
                                <KuzhambuSpace wrap>
                                    <Text type="secondary">
                                        当前使用：
                                        {selectedVisualAsset.currentUsed ? "是" : "否"}
                                    </Text>
                                </KuzhambuSpace>
                                <Form.Item label="文本权重">
                                    <Input
                                        aria-label="三才图会视觉资产文本权重"
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
                                        aria-label="三才图会视觉资产图片权重"
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
                                <Form.Item
                                    label="图片理解"
                                    className="sancai-entry-model-form-item-top"
                                >
                                    <Input.TextArea
                                        aria-label="三才图会视觉资产图片理解"
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
                                <Form.Item
                                    label="融合描述"
                                    className="sancai-entry-model-form-item-top"
                                >
                                    <Input.TextArea
                                        aria-label="三才图会视觉资产融合描述"
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
                                <Form.Item
                                    label="视觉描述"
                                    className="sancai-entry-model-form-item-top"
                                >
                                    <Input.TextArea
                                        aria-label="三才图会视觉资产视觉描述"
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
                                <Form.Item
                                    label="生成参数"
                                    className="sancai-entry-model-form-item-top"
                                >
                                    <Input.TextArea
                                        aria-label="三才图会视觉资产生成参数"
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
                            </div>
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description="暂无视觉资产版本"
                            />
                        )}
                    </section>
                ) : null}
            </Form>
            {afterForm}
        </KuzhambuDrawer>
    );
};
