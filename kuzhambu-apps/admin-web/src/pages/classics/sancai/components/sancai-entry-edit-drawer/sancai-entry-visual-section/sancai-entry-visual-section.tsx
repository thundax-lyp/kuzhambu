import {
    BranchesOutlined,
    CheckOutlined,
    FileSearchOutlined,
    FileTextOutlined,
    PictureOutlined
} from "@ant-design/icons";
import { Empty, Image, Input, Tag, Typography } from "antd";
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

import type { SancaiVisualAssetRefinementCapability } from "@/pages/classics/sancai/sancai-entry-service";
import type {
    SancaiEntryImageRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-visual-section.css";

const { Text } = Typography;

const readVisualAssetTitle = (asset: SancaiVisualAssetRecord | undefined | null) => {
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

const readVisualSourceImageSelectValue = (image: SancaiEntryImageRecord | undefined) => {
    return image?.storageObjectId != null ? `storage:${image.storageObjectId}` : undefined;
};

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

interface SancaiEntryVisualSectionProps {
    creatingVisualAssetCapability: SancaiVisualAssetRefinementCapability | null;
    currentVisualAsset: SancaiVisualAssetRecord | null | undefined;
    defaultSourceImage?: SancaiEntryImageRecord;
    entryImages: SancaiEntryImageRecord[];
    generatedPreviewUrl?: string;
    isUpdatingVisualAsset: boolean;
    selectedSourceImage?: SancaiEntryImageRecord;
    selectedSourceStorageObjectId: number | string | null;
    selectedVisualAsset: SancaiVisualAssetRecord | null | undefined;
    sourcePreviewUrl?: string;
    visualAssetFormValue: SancaiVisualAssetRecord | null;
    visualAssetsForSelectedSource: SancaiVisualAssetRecord[];
    onCreateVisualAssetTask?: (capability: SancaiVisualAssetRefinementCapability) => void;
    onSaveVisualAsset: () => void;
    onSelectVisualAsset: (asset: SancaiVisualAssetRecord) => void;
    onSelectVisualSourceImageBySelectValue: (selectValue: string) => void;
    onUpdateVisualAssetForm: (patch: Partial<SancaiVisualAssetRecord>) => void;
    onUseVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
}

export const SancaiEntryVisualSection = ({
    creatingVisualAssetCapability,
    currentVisualAsset,
    defaultSourceImage,
    entryImages,
    generatedPreviewUrl,
    isUpdatingVisualAsset,
    selectedSourceImage,
    selectedSourceStorageObjectId,
    selectedVisualAsset,
    sourcePreviewUrl,
    visualAssetFormValue,
    visualAssetsForSelectedSource,
    onCreateVisualAssetTask,
    onSaveVisualAsset,
    onSelectVisualAsset,
    onSelectVisualSourceImageBySelectValue,
    onUpdateVisualAssetForm,
    onUseVisualAsset
}: SancaiEntryVisualSectionProps) => {
    const selectedSourceImageSelectValue = readVisualSourceImageSelectValue(selectedSourceImage);

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
                        className="sancai-entry-edit-drawer-form"
                        colon={false}
                        component="div"
                    >
                        <KuzhambuFormItem label="来源图片" layoutSize="large">
                            <KuzhambuSelect
                                aria-label="三才图会视觉处理来源图片"
                                disabled={!defaultSourceImage}
                                placeholder="选择来源图片"
                                value={selectedSourceImageSelectValue}
                                options={entryImages.map((image) => ({
                                    disabled: !image.storageObjectId,
                                    label: readImageTitle(image),
                                    value:
                                        readVisualSourceImageSelectValue(image) ??
                                        `image:${image.id}`
                                }))}
                                onChange={(value) => onSelectVisualSourceImageBySelectValue(value)}
                            />
                        </KuzhambuFormItem>
                    </KuzhambuForm>
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
                                            const selectedAssetId = selectedVisualAsset
                                                ? readVisualAssetId(selectedVisualAsset)
                                                : 0;
                                            const isSelected =
                                                assetId > 0 && assetId === selectedAssetId;
                                            return [
                                                {
                                                    key: "select",
                                                    text: "选择",
                                                    ariaLabel: `选择${readVisualAssetTitle(asset)}`,
                                                    testId: `sancai-visual-asset-${assetId}-select-button`,
                                                    disabled:
                                                        !selectedSourceStorageObjectId ||
                                                        isSelected,
                                                    onClick: (record) => onSelectVisualAsset(record)
                                                },
                                                {
                                                    key: "use",
                                                    text: "当前",
                                                    ariaLabel: `设为当前视觉处理 ${readVisualAssetTitle(asset)}`,
                                                    testId: `sancai-visual-asset-${assetId}-use-button`,
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
                                        testId="classics-sancai-sancai-entry-action-button-3"
                                        icon={<FileSearchOutlined />}
                                        loading={creatingVisualAssetCapability === "image_analysis"}
                                        onClick={() => {
                                            onCreateVisualAssetTask("image_analysis");
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
                                            onCreateVisualAssetTask("fusion");
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
                                            onCreateVisualAssetTask("visual");
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
                                            onCreateVisualAssetTask("image_gen");
                                        }}
                                    >
                                        生图
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                            </div>
                        ) : null}
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                testId="classics-sancai-sancai-entry-action-button-7"
                                icon={<CheckOutlined />}
                                type="primary"
                                loading={isUpdatingVisualAsset}
                                onClick={onSaveVisualAsset}
                            >
                                采纳
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    </div>
                    <KuzhambuForm
                        className="sancai-entry-edit-drawer-form"
                        colon={false}
                        component="div"
                    >
                        <KuzhambuFormItem label="文本权重" layoutSize="middle">
                            <Input
                                aria-label="三才图会视觉处理文本权重"
                                value={visualAssetFormValue?.textWeight ?? ""}
                                onChange={(event) =>
                                    onUpdateVisualAssetForm({
                                        textWeight: event.target.value
                                            ? Number(event.target.value)
                                            : null
                                    })
                                }
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem label="图片权重" layoutSize="middle">
                            <Input
                                aria-label="三才图会视觉处理图片权重"
                                value={visualAssetFormValue?.imageWeight ?? ""}
                                onChange={(event) =>
                                    onUpdateVisualAssetForm({
                                        imageWeight: event.target.value
                                            ? Number(event.target.value)
                                            : null
                                    })
                                }
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="图片理解"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理图片理解"
                                value={visualAssetFormValue?.imageAnalysisMarkdown ?? ""}
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 3,
                                    maxRows: 6
                                })}
                                onChange={(event) =>
                                    onUpdateVisualAssetForm({
                                        imageAnalysisMarkdown: event.target.value
                                    })
                                }
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="融合描述"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理融合描述"
                                value={visualAssetFormValue?.fusionDescription ?? ""}
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 2,
                                    maxRows: 5
                                })}
                                onChange={(event) =>
                                    onUpdateVisualAssetForm({
                                        fusionDescription: event.target.value
                                    })
                                }
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="视觉描述"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理视觉描述"
                                value={visualAssetFormValue?.visualDescription ?? ""}
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 2,
                                    maxRows: 5
                                })}
                                onChange={(event) =>
                                    onUpdateVisualAssetForm({
                                        visualDescription: event.target.value
                                    })
                                }
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="生成参数"
                            layoutSize="large"
                            className="sancai-entry-edit-drawer-form-item-top"
                        >
                            <Input.TextArea
                                aria-label="三才图会视觉处理生成参数"
                                value={visualAssetFormValue?.generationParamsJson ?? ""}
                                autoSize={resolveTextAreaAutoSize({
                                    minRows: 2,
                                    maxRows: 5
                                })}
                                onChange={(event) =>
                                    onUpdateVisualAssetForm({
                                        generationParamsJson: event.target.value
                                    })
                                }
                            />
                        </KuzhambuFormItem>
                    </KuzhambuForm>
                </>
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无视觉处理记录" />
            )}
        </section>
    );
};
