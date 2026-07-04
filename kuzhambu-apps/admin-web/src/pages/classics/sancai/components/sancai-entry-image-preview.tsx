import { Button, Empty, Image, Typography } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as entryService from "../services/sancai-entry-service";
import type { SancaiEntryImageRecord } from "../sancai-types";

const { Text } = Typography;

const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
};

interface SancaiEntryImagePreviewProps {
    entryId: number | null;
    images: SancaiEntryImageRecord[];
    openImageId: number | null;
    onClose: () => void;
}

export const SancaiEntryImagePreview = ({
    entryId,
    images,
    openImageId,
    onClose
}: SancaiEntryImagePreviewProps) => {
    const orderedImages = useMemo(
        () =>
            [...images].sort((left, right) => {
                if ((left.priority ?? 0) !== (right.priority ?? 0)) {
                    return (left.priority ?? 0) - (right.priority ?? 0);
                }
                return left.id - right.id;
            }),
        [images]
    );
    const [navigatedImageId, setNavigatedImageId] = useState<number | null>(null);
    const activeImageId = navigatedImageId ?? openImageId;

    const activeIndex = orderedImages.findIndex((image) => image.id === activeImageId);
    const resolvedIndex = activeIndex >= 0 ? activeIndex : 0;
    const activeImage = orderedImages[resolvedIndex];
    const previewUrl =
        entryId && activeImage
            ? entryService.getImageContentUrl({
                  entryId,
                  imageId: activeImage.id
              })
            : undefined;
    const downloadUrl =
        entryId && activeImage
            ? entryService.getImageContentUrl({
                  entryId,
                  imageId: activeImage.id,
                  mode: "download"
              })
            : undefined;
    const hasMultipleImages = orderedImages.length > 1;

    return (
        <KuzhambuDrawer
            title="配图预览"
            open={openImageId !== null}
            size="middle"
            destroyOnHidden
            onClose={onClose}
        >
            <section className="sancai-image-preview" aria-label="配图预览">
                {activeImage && previewUrl ? (
                    <>
                        <div className="sancai-image-preview-main">
                            <Image
                                src={previewUrl}
                                alt={readImageTitle(activeImage)}
                                width="100%"
                                preview={false}
                            />
                        </div>
                        <div className="sancai-image-preview-meta">
                            <Text strong>{readImageTitle(activeImage)}</Text>
                            <Text type="secondary">
                                {resolvedIndex + 1} / {orderedImages.length}
                            </Text>
                        </div>
                        <KuzhambuSpace wrap>
                            <Button
                                disabled={!hasMultipleImages || resolvedIndex === 0}
                                onClick={() =>
                                    setNavigatedImageId(
                                        orderedImages[resolvedIndex - 1]?.id ?? null
                                    )
                                }
                            >
                                上一张
                            </Button>
                            <Button
                                disabled={
                                    !hasMultipleImages || resolvedIndex === orderedImages.length - 1
                                }
                                onClick={() =>
                                    setNavigatedImageId(
                                        orderedImages[resolvedIndex + 1]?.id ?? null
                                    )
                                }
                            >
                                下一张
                            </Button>
                            <Button href={downloadUrl} target="_blank" disabled={!downloadUrl}>
                                下载当前图片
                            </Button>
                        </KuzhambuSpace>
                    </>
                ) : (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无可预览配图" />
                )}
            </section>
        </KuzhambuDrawer>
    );
};
