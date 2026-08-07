import { CheckOutlined, PictureOutlined } from "@ant-design/icons";
import { Empty, Image, Tag, Typography } from "antd";
import { KuzhambuButton, KuzhambuSpace } from "@/components";
import type {
    SancaiEntryImageRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";
import {
    readImageTitle,
    readVisualAssetStatusLabel,
    readVisualAssetStatusTagColor
} from "./sancai-entry-visual-formatters";
import { SancaiVisualAssetHistoryModal } from "./sancai-visual-asset-history-modal";

const { Text } = Typography;

interface SancaiVisualAssetMediaPanelProps {
    generatedPreviewUrl?: string;
    isHistoryModalOpen: boolean;
    isUpdatingVisualAsset: boolean;
    onCloseHistoryModal: () => void;
    onOpenHistoryModal: () => void;
    onSaveVisualAsset: () => void;
    onSelectHistoryVisualAsset: (asset: SancaiVisualAssetRecord) => void;
    onUseHistoryVisualAsset: (asset: SancaiVisualAssetRecord) => void;
    selectedSourceImage?: SancaiEntryImageRecord;
    selectedSourceStorageObjectId?: string | null;
    selectedVisualAsset: SancaiVisualAssetRecord;
    sourcePreviewUrl?: string;
    visualAssetsForSelectedSource: SancaiVisualAssetRecord[];
}

export const SancaiVisualAssetMediaPanel = ({
    generatedPreviewUrl,
    isHistoryModalOpen,
    isUpdatingVisualAsset,
    onCloseHistoryModal,
    onOpenHistoryModal,
    onSaveVisualAsset,
    onSelectHistoryVisualAsset,
    onUseHistoryVisualAsset,
    selectedSourceImage,
    selectedSourceStorageObjectId,
    selectedVisualAsset,
    sourcePreviewUrl,
    visualAssetsForSelectedSource
}: SancaiVisualAssetMediaPanelProps) => {
    const isDraftVisualAsset = !selectedVisualAsset.visualAssetId && !selectedVisualAsset.id;
    return (
        <section className="sancai-visual-asset-media" aria-label="视觉处理图片">
            <section className="sancai-visual-asset-image-list">
                <section className="sancai-visual-asset-image-frame">
                    <section className="sancai-visual-asset-image-stage">
                        {sourcePreviewUrl ? (
                            <Image
                                className="sancai-visual-asset-image"
                                rootClassName="sancai-visual-asset-image-root"
                                src={sourcePreviewUrl}
                                alt="三才图会视觉处理来源图片"
                            />
                        ) : (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description="未选择来源图片"
                            />
                        )}
                    </section>
                    <KuzhambuSpace className="sancai-visual-asset-image-actions">
                        <Text type="secondary">
                            {selectedSourceImage ? readImageTitle(selectedSourceImage) : "来源图片"}
                        </Text>
                    </KuzhambuSpace>
                </section>
                <section className="sancai-visual-asset-image-frame">
                    <section className="sancai-visual-asset-image-stage">
                        {generatedPreviewUrl ? (
                            <Image
                                className="sancai-visual-asset-image"
                                rootClassName="sancai-visual-asset-image-root"
                                src={generatedPreviewUrl}
                                alt="三才图会视觉处理生成图"
                            />
                        ) : (
                            <section
                                className="sancai-visual-generated-placeholder"
                                role="img"
                                aria-label="三才图会视觉处理生成图占位"
                            >
                                <PictureOutlined />
                                <Text type="secondary">未生成图片</Text>
                            </section>
                        )}
                    </section>
                    <KuzhambuSpace className="sancai-visual-asset-image-actions">
                        <KuzhambuSpace wrap>
                            <Text type="secondary">生成图</Text>
                            <Tag color={readVisualAssetStatusTagColor(selectedVisualAsset.status)}>
                                {readVisualAssetStatusLabel(selectedVisualAsset.status)}
                            </Tag>
                        </KuzhambuSpace>
                        {!isDraftVisualAsset ? (
                            <KuzhambuButton
                                testId="classics-sancai-visual-asset-adopt-button"
                                icon={<CheckOutlined />}
                                type="primary"
                                loading={isUpdatingVisualAsset}
                                onClick={onSaveVisualAsset}
                            >
                                采纳
                            </KuzhambuButton>
                        ) : null}
                    </KuzhambuSpace>
                </section>
            </section>
            <KuzhambuButton
                block
                testId="classics-sancai-visual-history-button"
                disabled={
                    !selectedSourceStorageObjectId || visualAssetsForSelectedSource.length === 0
                }
                onClick={onOpenHistoryModal}
            >
                {visualAssetsForSelectedSource.length > 0 ? "切换历史版本" : "暂无历史版本"}
            </KuzhambuButton>
            <SancaiVisualAssetHistoryModal
                open={isHistoryModalOpen}
                selectedSourceStorageObjectId={selectedSourceStorageObjectId}
                selectedVisualAsset={selectedVisualAsset}
                visualAssetsForSelectedSource={visualAssetsForSelectedSource}
                onCancel={onCloseHistoryModal}
                onSelectHistoryVisualAsset={onSelectHistoryVisualAsset}
                onUseHistoryVisualAsset={onUseHistoryVisualAsset}
            />
        </section>
    );
};
