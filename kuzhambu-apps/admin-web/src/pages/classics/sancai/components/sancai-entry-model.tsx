import {
    DownloadOutlined,
    EditOutlined,
    EyeOutlined,
    PictureOutlined,
    UploadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Empty, Image, Input, Select, Switch, Tag, Typography, Upload } from "antd";
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
    entryTags?: string[];
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
    onEditTags?: () => void;
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
    entryTags,
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
    onEditTags,
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
    const activeTags = useMemo(
        () => [...new Set((entryTags || []).map((tag) => tag.trim()).filter(Boolean))],
        [entryTags]
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
            size="middle"
            destroyOnHidden
            footer={
                <div className="sancai-drawer-footer">
                    <Button onClick={onCancel}>取消</Button>
                    <Button
                        aria-label={mode === "create" ? "保存新增三才图会条目" : "保存三才图会条目"}
                        type="primary"
                        loading={isSubmitting}
                        onClick={submitForm}
                    >
                        保存
                    </Button>
                </div>
            }
            onClose={onCancel}
        >
            <div className="sancai-detail-card">
                <label className="sancai-form-field">
                    <Text strong>标题</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>门类</Text>
                    <Select
                        aria-label="三才图会条目门类"
                        placeholder="选择门类"
                        options={categoryOptions}
                        value={form.categoryId ?? undefined}
                        onChange={(value) => changeCategory(value ?? null)}
                    />
                </label>
                <label className="sancai-form-field">
                    <Text strong>卷</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>原文</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>译文</Text>
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
                </label>
                <label className="sancai-form-field">
                    <Text strong>摘要</Text>
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
                </label>
                <div className="sancai-form-switch-field">
                    <Text strong>可见性</Text>
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
                </div>
                {entryId ? (
                    <section className="sancai-form-field" aria-label="三才图会图片面板">
                        <Text strong>当前图片</Text>
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
                                <Button
                                    icon={<UploadOutlined />}
                                    loading={uploadImageMutation.isPending}
                                >
                                    {currentImage ? "替换当前图片" : "上传图片"}
                                </Button>
                            </Upload>
                            <Button
                                aria-label="预览三才图会图片"
                                icon={<EyeOutlined />}
                                href={previewUrl}
                                target="_blank"
                                disabled={!previewUrl}
                            >
                                预览
                            </Button>
                            <Button
                                aria-label="下载三才图会图片"
                                icon={<DownloadOutlined />}
                                href={downloadUrl}
                                target="_blank"
                                disabled={!downloadUrl}
                            >
                                下载
                            </Button>
                        </KuzhambuSpace>
                        {currentImage && previewUrl ? (
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
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description="未关联当前图片"
                            />
                        )}
                    </section>
                ) : null}
                {entryId ? (
                    <section className="sancai-form-field" aria-label="三才图会内容上下文">
                        <Text strong>条目上下文</Text>
                        <KuzhambuSpace wrap>
                            <Text type="secondary">
                                翻译状态：{entry?.translationStatus || "待处理"}
                            </Text>
                            <Text type="secondary">图片状态：{entry?.imageStatus || "待处理"}</Text>
                            <Text type="secondary">
                                视觉状态：{entry?.visualAssetStatus || "待处理"}
                            </Text>
                            <Text type="secondary">
                                精修状态：{entry?.refinementStatus || "待处理"}
                            </Text>
                        </KuzhambuSpace>
                        <Text type="secondary">原文：{entry?.originalText || "未填写"}</Text>
                        <Text type="secondary">译文：{entry?.translationText || "未生成"}</Text>
                        <div>
                            <KuzhambuSpace wrap>
                                <Text type="secondary">标签：</Text>
                                {activeTags.length ? (
                                    <>
                                        {activeTags.map((tag) => (
                                            <Tag key={tag}>{tag}</Tag>
                                        ))}
                                    </>
                                ) : (
                                    <Text type="secondary">未标注标签</Text>
                                )}
                                {onEditTags ? (
                                    <Button
                                        aria-label="编辑三才图会条目标签"
                                        icon={<EditOutlined />}
                                        size="small"
                                        onClick={onEditTags}
                                    >
                                        编辑标签
                                    </Button>
                                ) : null}
                            </KuzhambuSpace>
                        </div>
                    </section>
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
                                            <Button
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
                                            </Button>
                                        );
                                    })}
                                </KuzhambuSpace>
                                <KuzhambuSpace wrap>
                                    {onCreateVisualAssetTask ? (
                                        <>
                                            <Button
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
                                            </Button>
                                            <Button
                                                loading={creatingVisualAssetCapability === "fusion"}
                                                onClick={() => {
                                                    createVisualAssetTask("fusion");
                                                }}
                                            >
                                                创建信息融合任务
                                            </Button>
                                            <Button
                                                loading={creatingVisualAssetCapability === "visual"}
                                                onClick={() => {
                                                    createVisualAssetTask("visual");
                                                }}
                                            >
                                                创建视觉描述任务
                                            </Button>
                                            <Button
                                                loading={
                                                    creatingVisualAssetCapability === "image_gen"
                                                }
                                                onClick={() => {
                                                    createVisualAssetTask("image_gen");
                                                }}
                                            >
                                                创建生图任务
                                            </Button>
                                        </>
                                    ) : null}
                                    <Button
                                        type="primary"
                                        loading={isUpdatingVisualAsset}
                                        onClick={saveVisualAsset}
                                    >
                                        保存视觉资产字段
                                    </Button>
                                    <Button
                                        loading={isSwitchingVisualAsset}
                                        disabled={!canSwitchVisualAsset}
                                        onClick={activateVisualAsset}
                                    >
                                        设为当前使用版本
                                    </Button>
                                </KuzhambuSpace>
                                <KuzhambuSpace wrap>
                                    <Button
                                        aria-label="预览视觉资产原图"
                                        icon={<EyeOutlined />}
                                        href={sourcePreviewUrl}
                                        target="_blank"
                                        disabled={!sourcePreviewUrl}
                                    >
                                        预览原图
                                    </Button>
                                    <Button
                                        aria-label="下载视觉资产原图"
                                        icon={<DownloadOutlined />}
                                        href={sourceDownloadUrl}
                                        target="_blank"
                                        disabled={!sourceDownloadUrl}
                                    >
                                        下载原图
                                    </Button>
                                    <Button
                                        aria-label="预览视觉资产生成图"
                                        icon={<PictureOutlined />}
                                        href={generatedPreviewUrl}
                                        target="_blank"
                                        disabled={!generatedPreviewUrl}
                                    >
                                        预览生成图
                                    </Button>
                                    <Button
                                        aria-label="下载视觉资产生成图"
                                        icon={<DownloadOutlined />}
                                        href={generatedDownloadUrl}
                                        target="_blank"
                                        disabled={!generatedDownloadUrl}
                                    >
                                        下载生成图
                                    </Button>
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
                                <label className="sancai-form-field">
                                    <Text strong>文本权重</Text>
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
                                </label>
                                <label className="sancai-form-field">
                                    <Text strong>图片权重</Text>
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
                                </label>
                                <label className="sancai-form-field">
                                    <Text strong>图片理解</Text>
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
                                </label>
                                <label className="sancai-form-field">
                                    <Text strong>融合描述</Text>
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
                                </label>
                                <label className="sancai-form-field">
                                    <Text strong>视觉描述</Text>
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
                                </label>
                                <label className="sancai-form-field">
                                    <Text strong>生成参数</Text>
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
                                </label>
                            </div>
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description="暂无视觉资产版本"
                            />
                        )}
                    </section>
                ) : null}
            </div>
            {afterForm}
        </KuzhambuDrawer>
    );
};
