import {
    BranchesOutlined,
    CheckOutlined,
    FileSearchOutlined,
    FileTextOutlined,
    PictureOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Empty, Form, Image, Input, Tag, Typography } from "antd";
import { useEffect, useMemo, useRef, useState } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuSelect
} from "@/components";

import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai/sancai-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import { useSancaiEntryVisualRefinement } from "./hooks/use-sancai-entry-visual-refinement";
import { SancaiEntryVisualRefinementSection } from "./sancai-entry-visual-refinement-section";
import "./sancai-entry-visual-section.css";

const { Text } = Typography;

const readVisualAssetTitle = (asset: SancaiVisualAssetRecord | undefined | null) => {
    if (!asset) {
        return "未选择视觉处理";
    }
    return `处理记录 ${asset.versionNo ?? asset.visualAssetId ?? asset.id ?? "-"}`;
};

const readVisualAssetId = (asset: SancaiVisualAssetRecord) => {
    return asset.visualAssetId ?? asset.id ?? "";
};

const selectCurrentVisualAsset = (assets: SancaiVisualAssetRecord[]) => {
    return [...assets]
        .filter((asset) => asset.currentUsed !== false)
        .sort((left, right) => (right.versionNo ?? 0) - (left.versionNo ?? 0))[0];
};

const resolveImageUrl = (
    entryId: string | undefined,
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

const isSameStorageObjectId = (
    left: number | string | null | undefined,
    right: number | string | null | undefined
) => {
    return left != null && right != null && String(left) === String(right);
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

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

const normalizeNumberField = (value: unknown) => {
    if (value === "" || value == null) {
        return null;
    }
    const numericValue = Number(value);
    return Number.isFinite(numericValue) ? numericValue : null;
};

const VISUAL_ASSET_FORM_FIELD_NAMES: Array<keyof SancaiVisualAssetRecord> = [
    "sourceImageStorageObjectId",
    "sourcePreviewUrl",
    "sourceDownloadUrl",
    "generatedImageStorageObjectId",
    "generatedPreviewUrl",
    "generatedDownloadUrl",
    "textWeight",
    "imageWeight",
    "imageAnalysisMarkdown",
    "fusionDescription",
    "visualDescription",
    "generationParamsJson"
];

const toVisualAssetFormValue = (
    asset: SancaiVisualAssetRecord | null,
    defaultSourceImage: SancaiEntryImageRecord | undefined,
    entryId: string | undefined
) => {
    if (!asset) {
        return null;
    }
    const formValue = { ...asset };
    if (!formValue.sourceImageStorageObjectId && defaultSourceImage?.storageObjectId) {
        formValue.sourceImageStorageObjectId = defaultSourceImage.storageObjectId;
        formValue.sourcePreviewUrl = resolveImageUrl(entryId, defaultSourceImage, "preview");
        formValue.sourceDownloadUrl = resolveImageUrl(entryId, defaultSourceImage, "download");
    }
    return formValue;
};

interface SancaiEntryVisualSectionProps {
    entry: SancaiEntryRecord;
    isUpdatingVisualAsset: boolean;
    onRefinementChanged: () => Promise<void> | void;
    onPreviewStateChange: (state: SancaiEntryVisualPreviewState) => void;
    onUpdateVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
    onUseVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
}

export interface SancaiEntryVisualPreviewState {
    currentVisualAsset: SancaiVisualAssetRecord | null;
    generatedPreviewUrl?: string;
    visualDescription?: string | null;
}

export const SancaiEntryVisualSection = ({
    entry,
    isUpdatingVisualAsset,
    onRefinementChanged,
    onPreviewStateChange,
    onUpdateVisualAsset,
    onUseVisualAsset
}: SancaiEntryVisualSectionProps) => {
    const entryId = entry.id;
    const imagesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "images", entryId],
        queryFn: () => entryService.listImages(entryId),
        enabled: Boolean(entryId),
        refetchOnMount: false,
        retry: false
    });
    const entryImages = useMemo(() => imagesQuery.data || [], [imagesQuery.data]);
    const visualAssetsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "visual-assets", entryId],
        queryFn: () => entryService.listVisualAssets(entryId),
        enabled: Boolean(entryId),
        refetchOnMount: false,
        retry: false
    });
    const visualAssets = useMemo(() => visualAssetsQuery.data || [], [visualAssetsQuery.data]);
    const orderedVisualAssets = useMemo(
        () =>
            [...visualAssets].sort((left, right) => {
                if ((left.versionNo ?? 0) !== (right.versionNo ?? 0)) {
                    return (right.versionNo ?? 0) - (left.versionNo ?? 0);
                }
                return aiRefinementTaskService.sortDecimalIdDesc(
                    left.visualAssetId ?? left.id,
                    right.visualAssetId ?? right.id
                );
            }),
        [visualAssets]
    );
    const currentVisualAsset = useMemo(
        () => selectCurrentVisualAsset(visualAssets),
        [visualAssets]
    );
    const [selectedVisualAssetId, setSelectedVisualAssetId] = useState<string | null>(null);
    const [visualAssetForm] = Form.useForm<SancaiVisualAssetRecord>();
    const loadedVisualAssetIdRef = useRef<string | null>(null);
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
    const defaultSourceImage = entryImages.find((image) => image.storageObjectId);
    const initialVisualAssetFormValue = useMemo(
        () => toVisualAssetFormValue(selectedVisualAsset, defaultSourceImage, entryId),
        [defaultSourceImage, entryId, selectedVisualAsset]
    );
    useEffect(() => {
        const nextVisualAssetId = initialVisualAssetFormValue
            ? readVisualAssetId(initialVisualAssetFormValue)
            : null;
        if (!initialVisualAssetFormValue) {
            loadedVisualAssetIdRef.current = null;
            visualAssetForm.resetFields();
            return;
        }
        if (loadedVisualAssetIdRef.current !== nextVisualAssetId) {
            loadedVisualAssetIdRef.current = nextVisualAssetId;
            visualAssetForm.resetFields();
            visualAssetForm.setFieldsValue(initialVisualAssetFormValue);
            return;
        }
        const untouchedServerValues = VISUAL_ASSET_FORM_FIELD_NAMES.reduce<
            Partial<SancaiVisualAssetRecord>
        >((values, fieldName) => {
            if (!visualAssetForm.isFieldTouched(fieldName)) {
                values[fieldName] = initialVisualAssetFormValue[fieldName] as never;
            }
            return values;
        }, {});
        if (Object.keys(untouchedServerValues).length > 0) {
            visualAssetForm.setFieldsValue(untouchedServerValues);
        }
    }, [initialVisualAssetFormValue, visualAssetForm]);
    const sourceImageStorageObjectId = Form.useWatch("sourceImageStorageObjectId", visualAssetForm);
    const textWeight = Form.useWatch("textWeight", visualAssetForm);
    const imageWeight = Form.useWatch("imageWeight", visualAssetForm);
    const imageAnalysisMarkdown = Form.useWatch("imageAnalysisMarkdown", visualAssetForm);
    const fusionDescription = Form.useWatch("fusionDescription", visualAssetForm);
    const visualDescription = Form.useWatch("visualDescription", visualAssetForm);
    const generationParamsJson = Form.useWatch("generationParamsJson", visualAssetForm);
    const sourcePreviewUrlField = Form.useWatch("sourcePreviewUrl", visualAssetForm);
    const sourceDownloadUrl = Form.useWatch("sourceDownloadUrl", visualAssetForm);
    const generatedPreviewUrlField = Form.useWatch("generatedPreviewUrl", visualAssetForm);
    const generatedImageStorageObjectId = Form.useWatch(
        "generatedImageStorageObjectId",
        visualAssetForm
    );
    const visualAssetFormValue = useMemo(() => {
        if (!selectedVisualAsset) {
            return null;
        }
        return {
            ...selectedVisualAsset,
            sourceImageStorageObjectId:
                sourceImageStorageObjectId ?? selectedVisualAsset.sourceImageStorageObjectId,
            sourcePreviewUrl: sourcePreviewUrlField ?? selectedVisualAsset.sourcePreviewUrl,
            sourceDownloadUrl: sourceDownloadUrl ?? selectedVisualAsset.sourceDownloadUrl,
            generatedImageStorageObjectId:
                generatedImageStorageObjectId ?? selectedVisualAsset.generatedImageStorageObjectId,
            generatedPreviewUrl:
                generatedPreviewUrlField ?? selectedVisualAsset.generatedPreviewUrl,
            textWeight: normalizeNumberField(textWeight ?? selectedVisualAsset.textWeight),
            imageWeight: normalizeNumberField(imageWeight ?? selectedVisualAsset.imageWeight),
            imageAnalysisMarkdown:
                imageAnalysisMarkdown ?? selectedVisualAsset.imageAnalysisMarkdown,
            fusionDescription: fusionDescription ?? selectedVisualAsset.fusionDescription,
            visualDescription: visualDescription ?? selectedVisualAsset.visualDescription,
            generationParamsJson: generationParamsJson ?? selectedVisualAsset.generationParamsJson
        };
    }, [
        fusionDescription,
        generatedImageStorageObjectId,
        generatedPreviewUrlField,
        generationParamsJson,
        imageAnalysisMarkdown,
        imageWeight,
        selectedVisualAsset,
        sourceDownloadUrl,
        sourceImageStorageObjectId,
        sourcePreviewUrlField,
        textWeight,
        visualDescription
    ]);
    const selectedVisualAssetResourceId = selectedVisualAsset
        ? readVisualAssetId(selectedVisualAsset)
        : null;
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
                      (selectedVisualAssetResourceId
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
                  (selectedVisualAssetResourceId
                      ? entryService.getVisualAssetContentUrl({
                            entryId,
                            visualAssetId: selectedVisualAssetResourceId,
                            variant: "generated"
                        })
                      : undefined))
            : undefined
    );
    const saveVisualAsset = async () => {
        if (!selectedVisualAsset || !onUpdateVisualAsset) {
            return;
        }
        const values = await visualAssetForm.validateFields();
        onUpdateVisualAsset({
            ...selectedVisualAsset,
            ...values,
            textWeight: normalizeNumberField(values.textWeight),
            imageWeight: normalizeNumberField(values.imageWeight)
        });
    };
    const selectVisualAsset = (asset: SancaiVisualAssetRecord) => {
        const assetId = asset.visualAssetId ?? asset.id ?? null;
        setSelectedVisualAssetId(assetId);
        const nextFormValue = toVisualAssetFormValue(asset, defaultSourceImage, entryId);
        visualAssetForm.resetFields();
        if (nextFormValue) {
            visualAssetForm.setFieldsValue(nextFormValue);
        }
    };
    const selectVisualSourceImage = (image: SancaiEntryImageRecord) => {
        if (!image.storageObjectId) {
            return;
        }
        visualAssetForm.setFieldsValue({
            sourceImageStorageObjectId: image.storageObjectId,
            sourcePreviewUrl: resolveImageUrl(entryId, image, "preview"),
            sourceDownloadUrl: resolveImageUrl(entryId, image, "download")
        });
    };
    const selectVisualSourceImageBySelectValue = (storageObjectId: string) => {
        const image = entryImages.find((entryImage) =>
            isSameStorageObjectId(entryImage.storageObjectId, storageObjectId)
        );
        if (image) {
            selectVisualSourceImage(image);
        }
    };
    const {
        closeStreamingRefinementTask,
        createVisualAssetTask,
        creatingVisualAssetCapability,
        isStreamingRefinementTask,
        refinementTasks,
        refreshAfterVisualAssetCandidateHandled,
        refreshVisualAssetCandidates,
        retryingRefinementTaskId,
        retryRefinementTask,
        streamErrorText,
        streamEvents,
        streamingRefinementTask
    } = useSancaiEntryVisualRefinement({
        entry,
        selectedVisualAsset,
        selectedVisualAssetId: selectedVisualAssetResourceId,
        visualAssetFormValue,
        onRefinementChanged
    });
    useEffect(() => {
        onPreviewStateChange({
            currentVisualAsset: currentVisualAsset ?? null,
            generatedPreviewUrl,
            visualDescription
        });
    }, [currentVisualAsset, generatedPreviewUrl, onPreviewStateChange, visualDescription]);

    return (
        <section
            className="sancai-detail-card sancai-visual-asset-field"
            aria-label="三才图会视觉处理面板"
        >
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
                    <KuzhambuForm
                        form={visualAssetForm}
                        className="sancai-entry-edit-drawer-form"
                        colon={false}
                        component="div"
                    >
                        <KuzhambuFormItem
                            name="sourceImageStorageObjectId"
                            label="来源图片"
                            layoutSize="large"
                        >
                            <KuzhambuSelect
                                aria-label="三才图会视觉处理来源图片"
                                disabled={!defaultSourceImage}
                                placeholder="选择来源图片"
                                options={entryImages.map((image) => ({
                                    disabled: !image.storageObjectId,
                                    label: readImageTitle(image),
                                    value: image.storageObjectId ?? `image:${image.id}`
                                }))}
                                onChange={(value) => selectVisualSourceImageBySelectValue(value)}
                            />
                        </KuzhambuFormItem>
                        <div className="sancai-visual-asset-image-list">
                            <div className="sancai-visual-asset-image-frame">
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
                            <div className="sancai-visual-asset-image-frame">
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
                                                        preview={{
                                                            src: fullImageUrl ?? previewUrl
                                                        }}
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
                                                    <Tag
                                                        color={readVisualAssetStatusTagColor(
                                                            status
                                                        )}
                                                    >
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
                                                const selectedAssetId = selectedVisualAsset
                                                    ? readVisualAssetId(selectedVisualAsset)
                                                    : "";
                                                const isSelected =
                                                    Boolean(assetId) && assetId === selectedAssetId;
                                                return [
                                                    {
                                                        key: "select",
                                                        text: "选择",
                                                        ariaLabel: `选择${readVisualAssetTitle(asset)}`,
                                                        testId: `sancai-visual-asset-${assetId}-select-button`,
                                                        disabled:
                                                            !selectedSourceStorageObjectId ||
                                                            isSelected,
                                                        onClick: (record) =>
                                                            selectVisualAsset(record)
                                                    },
                                                    {
                                                        key: "use",
                                                        text: "当前",
                                                        ariaLabel: `设为当前视觉处理 ${readVisualAssetTitle(asset)}`,
                                                        testId: `sancai-visual-asset-${assetId}-use-button`,
                                                        disabled:
                                                            !selectedSourceStorageObjectId ||
                                                            Boolean(asset.currentUsed),
                                                        onClick: (record) =>
                                                            onUseVisualAsset?.(record)
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
                            <div className="sancai-visual-workflow" aria-label="图文生图工作流">
                                <KuzhambuSpace wrap>
                                    <KuzhambuButton
                                        testId="classics-sancai-sancai-entry-action-button-3"
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
                                        testId="classics-sancai-sancai-entry-action-button-4"
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
                                        testId="classics-sancai-sancai-entry-action-button-5"
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
                                        testId="classics-sancai-sancai-entry-action-button-6"
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
                            <KuzhambuSpace wrap>
                                <KuzhambuButton
                                    testId="classics-sancai-sancai-entry-action-button-7"
                                    icon={<CheckOutlined />}
                                    type="primary"
                                    loading={isUpdatingVisualAsset}
                                    onClick={saveVisualAsset}
                                >
                                    采纳
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </div>
                        <KuzhambuFormItem name="textWeight" label="文本权重" layoutSize="middle">
                            <Input aria-label="三才图会视觉处理文本权重" />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem name="imageWeight" label="图片权重" layoutSize="middle">
                            <Input aria-label="三才图会视觉处理图片权重" />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            name="imageAnalysisMarkdown"
                            label="图片理解"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理图片理解"
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 3,
                                    maxRows: 6
                                })}
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            name="fusionDescription"
                            label="融合描述"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理融合描述"
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 2,
                                    maxRows: 5
                                })}
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            name="visualDescription"
                            label="视觉描述"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理视觉描述"
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 2,
                                    maxRows: 5
                                })}
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            name="generationParamsJson"
                            label="生成参数"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理生成参数"
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 2,
                                    maxRows: 5
                                })}
                            />
                        </KuzhambuFormItem>
                    </KuzhambuForm>
                </>
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无视觉处理记录" />
            )}
            <SancaiEntryVisualRefinementSection
                entryId={entry.id}
                isStreamingRefinementTask={isStreamingRefinementTask}
                refinementTasks={refinementTasks}
                retryingRefinementTaskId={retryingRefinementTaskId}
                selectedVisualAssetId={selectedVisualAssetResourceId}
                streamErrorText={streamErrorText}
                streamEvents={streamEvents}
                streamingRefinementTask={streamingRefinementTask}
                onCloseStreamingRefinementTask={closeStreamingRefinementTask}
                onRefreshVisualAssetCandidates={refreshVisualAssetCandidates}
                onRetryRefinementTask={retryRefinementTask}
                onVisualAssetCandidateChanged={refreshAfterVisualAssetCandidateHandled}
            />
        </section>
    );
};
