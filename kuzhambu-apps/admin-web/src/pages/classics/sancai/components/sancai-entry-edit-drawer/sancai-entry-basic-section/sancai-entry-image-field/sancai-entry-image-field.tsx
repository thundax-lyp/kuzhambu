import { DownloadOutlined, UploadOutlined } from "@ant-design/icons";
import { Image, Typography, Upload } from "antd";
import type { ReactNode } from "react";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { SancaiEntryImageRecord } from "@/pages/classics/sancai/sancai-types";
import "./sancai-entry-image-field.css";

const { Text } = Typography;
const IMAGE_ACCEPT = ".jpg,.jpeg,.png,.gif,.webp";

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

interface SancaiEntryImageFieldProps {
    content?: ReactNode;
    currentImage?: SancaiEntryImageRecord;
    downloadUrl?: string;
    isUploadingImage: boolean;
    previewUrl?: string;
    onUploadImage: (file: File) => void;
}

export const SancaiEntryImageField = ({
    content,
    currentImage,
    downloadUrl,
    isUploadingImage,
    previewUrl,
    onUploadImage
}: SancaiEntryImageFieldProps) => {
    if (content) {
        return <>{content}</>;
    }

    return (
        <div className="sancai-entry-image-field">
            {currentImage && previewUrl ? (
                <div className="sancai-entry-image-frame">
                    <Image
                        width={180}
                        src={previewUrl}
                        alt={currentImage.title || currentImage.originalFilename || "三才图会图片"}
                    />
                    <Text type="secondary">
                        {currentImage.originalFilename ||
                            currentImage.title ||
                            `图片 ${currentImage.id}`}{" "}
                        - {formatSize(currentImage.size)}
                    </Text>
                </div>
            ) : null}
            <KuzhambuSpace wrap>
                <Upload
                    aria-label="上传三才图会图片"
                    accept={IMAGE_ACCEPT}
                    showUploadList={false}
                    beforeUpload={(file) => {
                        onUploadImage(file);
                        return Upload.LIST_IGNORE;
                    }}
                >
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-image-upload-button"
                        icon={<UploadOutlined />}
                        loading={isUploadingImage}
                    >
                        上传
                    </KuzhambuButton>
                </Upload>
                <KuzhambuButton
                    testId="classics-sancai-sancai-entry-image-download-button"
                    icon={<DownloadOutlined />}
                    href={downloadUrl}
                    target="_blank"
                    disabled={!downloadUrl}
                >
                    下载
                </KuzhambuButton>
            </KuzhambuSpace>
        </div>
    );
};
