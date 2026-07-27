import { DownloadOutlined, UploadOutlined } from "@ant-design/icons";
import { Badge, Empty, Image, Typography, Upload } from "antd";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import {
    KuzhambuButton,
    KuzhambuTable,
    type KuzhambuTableProps,
    type KuzhambuTableSortPosition
} from "@/components";

import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type { SancaiEntryImageRecord } from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-image-field.css";

const { Text } = Typography;
const IMAGE_ACCEPT = ".jpg,.jpeg,.png,.gif,.webp";

const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
};

const formatImageSize = (size?: number | null) => {
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

const resolveImagePreviewUrl = (entryId: number, image: SancaiEntryImageRecord) => {
    return toAuthenticatedResourceUrl(
        entryService.getImageContentUrl({
            entryId,
            imageId: image.id,
            mode: "preview"
        })
    );
};

interface SancaiEntryImageFieldProps {
    deleteImageLoading: boolean;
    entryId: number;
    images: SancaiEntryImageRecord[];
    isLoading: boolean;
    isUploadingImage: boolean;
    onDeleteImage: (image: SancaiEntryImageRecord) => void;
    onDownloadImage: (image: SancaiEntryImageRecord) => void;
    onSortImage: (
        sourceImage: SancaiEntryImageRecord,
        targetImage: SancaiEntryImageRecord,
        position: KuzhambuTableSortPosition
    ) => void;
    onUploadImage: (file: File) => void;
    onUseImage: (image: SancaiEntryImageRecord) => void;
}

export const SancaiEntryImageField = ({
    deleteImageLoading,
    entryId,
    images,
    isLoading,
    isUploadingImage,
    onDeleteImage,
    onDownloadImage,
    onSortImage,
    onUploadImage,
    onUseImage
}: SancaiEntryImageFieldProps) => {
    return (
        <div
            className="sancai-entry-image-manager"
            aria-label="三才图会图片管理"
            aria-busy={isLoading}
        >
            <div className="sancai-entry-image-toolbar">
                <Upload
                    aria-label="上传图片"
                    accept={IMAGE_ACCEPT}
                    showUploadList={false}
                    beforeUpload={(file) => {
                        onUploadImage(file);
                        return Upload.LIST_IGNORE;
                    }}
                >
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-action-button"
                        icon={<UploadOutlined />}
                        loading={isUploadingImage}
                        type="primary"
                    >
                        上传图片
                    </KuzhambuButton>
                </Upload>
            </div>
            {images.length > 0 ? (
                <Image.PreviewGroup>
                    <KuzhambuTable
                        className="sancai-image-table"
                        ariaLabel="三才图会图片列表"
                        columns={
                            [
                                {
                                    title: "图片",
                                    key: "image",
                                    render: (_, image) => {
                                        const imageTitle = readImageTitle(image);
                                        const thumbnail = (
                                            <Image
                                                className={
                                                    image.currentUsed
                                                        ? "sancai-entry-image-cover-thumbnail"
                                                        : undefined
                                                }
                                                width={132}
                                                height={88}
                                                src={resolveImagePreviewUrl(entryId, image)}
                                                alt={imageTitle}
                                            />
                                        );
                                        return (
                                            <div className="sancai-entry-image-cell">
                                                {image.currentUsed ? (
                                                    <span className="sancai-entry-image-cover">
                                                        <Badge.Ribbon text="封面">
                                                            {thumbnail}
                                                        </Badge.Ribbon>
                                                    </span>
                                                ) : (
                                                    thumbnail
                                                )}
                                                <span className="sancai-entry-image-meta">
                                                    <Text strong>{imageTitle}</Text>
                                                    <Text type="secondary">
                                                        {formatImageSize(image.size)}
                                                    </Text>
                                                </span>
                                            </div>
                                        );
                                    }
                                },
                                {
                                    inlineLimit: 4,
                                    key: "actions",
                                    title: "操作",
                                    width: 220,
                                    options: (image) => [
                                        {
                                            key: "download",
                                            text: "下载",
                                            icon: <DownloadOutlined />,
                                            ariaLabel: `下载 ${readImageTitle(image)}`,
                                            onClick: () => onDownloadImage(image)
                                        },
                                        {
                                            key: "cover",
                                            text: "封面",
                                            ariaLabel: `设为封面 ${readImageTitle(image)}`,
                                            disabled: Boolean(image.currentUsed),
                                            onClick: () => onUseImage(image)
                                        },
                                        {
                                            type: "divider"
                                        },
                                        {
                                            key: "delete",
                                            text: "删除",
                                            type: "danger",
                                            ariaLabel: `删除 ${readImageTitle(image)}`,
                                            disabled: deleteImageLoading,
                                            onClick: () => onDeleteImage(image)
                                        }
                                    ]
                                }
                            ] satisfies KuzhambuTableProps<SancaiEntryImageRecord>["columns"]
                        }
                        dataSource={images}
                        pagination={false}
                        rowKey="id"
                        size="small"
                        scroll={{ x: 640 }}
                        sortable
                        onSort={onSortImage}
                    />
                </Image.PreviewGroup>
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无图片" />
            )}
        </div>
    );
};
