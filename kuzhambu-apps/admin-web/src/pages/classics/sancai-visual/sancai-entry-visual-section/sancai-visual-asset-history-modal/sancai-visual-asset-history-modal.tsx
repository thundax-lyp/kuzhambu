import { CheckOutlined } from "@ant-design/icons";
import { Image, Tag, Typography } from "antd";
import {
    KuzhambuButton,
    KuzhambuModal,
    KuzhambuTable,
    type KuzhambuTableProps
} from "@/components";
import type { SancaiVisualAssetRecord } from "@/pages/classics/sancai-visual/sancai-visual-types";
import {
    readVisualAssetId,
    readVisualAssetStatusLabel,
    readVisualAssetStatusTagColor,
    readVisualAssetTitle,
    resolveStorageUrl
} from "../sancai-entry-visual-formatters";

const { Text } = Typography;

interface SancaiVisualAssetHistoryModalProps {
    onCancel: () => void;
    onSelectHistoryVisualAsset: (asset: SancaiVisualAssetRecord) => void;
    onUseHistoryVisualAsset: (asset: SancaiVisualAssetRecord) => void;
    open: boolean;
    selectedSourceStorageObjectId?: string | null;
    selectedVisualAsset: SancaiVisualAssetRecord;
    visualAssetsForSelectedSource: SancaiVisualAssetRecord[];
}

export const SancaiVisualAssetHistoryModal = ({
    onCancel,
    onSelectHistoryVisualAsset,
    onUseHistoryVisualAsset,
    open,
    selectedSourceStorageObjectId,
    selectedVisualAsset,
    visualAssetsForSelectedSource
}: SancaiVisualAssetHistoryModalProps) => {
    return (
        <KuzhambuModal
            testId="classics-sancai-visual-history-modal"
            title="选择视觉处理历史"
            open={open}
            width={760}
            footer={
                <KuzhambuButton
                    testId="classics-sancai-visual-history-close-button"
                    onClick={onCancel}
                >
                    关闭
                </KuzhambuButton>
            }
            onCancel={onCancel}
        >
            <KuzhambuTable
                className="sancai-visual-asset-version-table"
                ariaLabel="三才图会视觉处理历史记录列表"
                columns={
                    [
                        {
                            title: "历史记录",
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
                                    asset.generatedPreviewUrl ?? asset.generatedDownloadUrl
                                );
                                const fullImageUrl = resolveStorageUrl(
                                    asset.generatedDownloadUrl ?? asset.generatedPreviewUrl
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
                                const selectedAssetId = readVisualAssetId(selectedVisualAsset);
                                const isSelected = Boolean(assetId) && assetId === selectedAssetId;
                                return [
                                    {
                                        key: "select",
                                        text: "选择",
                                        ariaLabel: `选择${readVisualAssetTitle(asset)}`,
                                        testId: `sancai-visual-asset-${assetId}-select-button`,
                                        disabled: !selectedSourceStorageObjectId || isSelected,
                                        onClick: onSelectHistoryVisualAsset
                                    },
                                    {
                                        key: "use",
                                        text: "当前",
                                        ariaLabel: `设为当前视觉处理 ${readVisualAssetTitle(asset)}`,
                                        testId: `sancai-visual-asset-${assetId}-use-button`,
                                        disabled:
                                            !selectedSourceStorageObjectId ||
                                            Boolean(asset.currentUsed),
                                        onClick: onUseHistoryVisualAsset
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
        </KuzhambuModal>
    );
};
