import { DownloadOutlined, EyeOutlined, PictureOutlined, UploadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Empty, Image, Input, Switch, Typography, Upload } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-form-values";
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
    entry: SancaiEntryRecord | undefined;
    isSubmitting: boolean;
    isSwitchingVisualAsset?: boolean;
    isUpdatingVisualAsset?: boolean;
    mode?: "create" | "edit";
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
    onUseVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onUpdateVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onCreateImageAnalysisTask?: (asset: SancaiVisualAssetRecord) => void;
    isCreatingImageAnalysisTask?: boolean;
    onSelectedVisualAssetChange?: (asset: SancaiVisualAssetRecord | null) => void;
}

export const SancaiEntryModel = ({
    afterForm,
    entry,
    isSubmitting,
    isSwitchingVisualAsset = false,
    isUpdatingVisualAsset = false,
    mode = "edit",
    open,
    onCancel,
    onSubmit,
    onUseVisualAsset,
    onUpdateVisualAsset,
    onCreateImageAnalysisTask,
    isCreatingImageAnalysisTask = false,
    onSelectedVisualAssetChange
}: SancaiEntryModelProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<SancaiEntryFormValues>(() => toEntryFormValues(entry));
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
    const sourcePreviewUrl = resolveStorageUrl(selectedVisualAsset?.sourcePreviewUrl);
    const sourceDownloadUrl = resolveStorageUrl(selectedVisualAsset?.sourceDownloadUrl);
    const generatedPreviewUrl = resolveStorageUrl(selectedVisualAsset?.generatedPreviewUrl);
    const generatedDownloadUrl = resolveStorageUrl(selectedVisualAsset?.generatedDownloadUrl);
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
                        onClick={() => onSubmit(form)}
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
                    <section className="sancai-form-field" aria-label="三才图会视觉资产面板">
                        <Text strong>视觉资产</Text>
                        {selectedVisualAsset ? (
                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: 12,
                                    width: "100%"
                                }}
                            >
                                <Text type="secondary">
                                    当前使用版本：
                                    {readVisualAssetTitle(currentVisualAsset)}
                                    {currentVisualAsset?.status
                                        ? ` · 状态 ${currentVisualAsset.status}`
                                        : ""}
                                </Text>
                                <KuzhambuSpace wrap>
                                    {visualAssets.map((asset) => {
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
                                    {onCreateImageAnalysisTask ? (
                                        <Button
                                            type="default"
                                            loading={isCreatingImageAnalysisTask}
                                            onClick={() => {
                                                if (!selectedVisualAsset) {
                                                    return;
                                                }
                                                onCreateImageAnalysisTask(selectedVisualAsset);
                                            }}
                                        >
                                            创建图片理解任务
                                        </Button>
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
                                <Text type="secondary">
                                    已选版本：
                                    {readVisualAssetTitle(selectedVisualAsset)}
                                    {selectedVisualAsset.status
                                        ? ` · 状态 ${selectedVisualAsset.status}`
                                        : ""}
                                </Text>
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
