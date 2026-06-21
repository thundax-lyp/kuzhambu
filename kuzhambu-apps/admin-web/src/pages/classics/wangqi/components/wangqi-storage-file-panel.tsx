import { DownloadOutlined, EyeOutlined, ReloadOutlined, UploadOutlined } from "@ant-design/icons";
import { Button, Descriptions, Empty, Space, Typography, Upload } from "antd";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import * as wangqiService from "../wangqi-service";
import type {
    WangqiDocumentRecord,
    WangqiSourceFileContentMode,
    WangqiSourceFileRecord
} from "../wangqi-types";

const { Text } = Typography;

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

const resolveContentUrl = (
    document?: WangqiDocumentRecord | null,
    mode: WangqiSourceFileContentMode = "preview"
) => {
    if (!document?.id) {
        return undefined;
    }
    return toAuthenticatedResourceUrl(wangqiService.getSourceFileContentUrl(document.id, mode));
};

export interface WangqiStorageFilePanelProps {
    document?: WangqiDocumentRecord | null;
    loading?: boolean;
    sourceFile?: WangqiSourceFileRecord | null;
    uploading?: boolean;
    onRefresh: () => void;
    onUpload: (file: File) => Promise<unknown> | void;
}

export const WangqiStorageFilePanel = ({
    document,
    loading = false,
    sourceFile,
    uploading = false,
    onRefresh,
    onUpload
}: WangqiStorageFilePanelProps) => {
    const previewUrl = resolveContentUrl(document, "preview");
    const downloadUrl = resolveContentUrl(document, "download");
    const hasSourceFile = Boolean(sourceFile?.storageObjectId || document?.storageObjectId);

    return (
        <section className="wangqi-storage-file-panel" aria-label="王圻原始文件面板">
            <Space className="wangqi-storage-file-panel-actions" wrap>
                <Button
                    aria-label="刷新王圻原始文件元数据"
                    icon={<ReloadOutlined />}
                    loading={loading}
                    onClick={onRefresh}
                >
                    刷新
                </Button>
                <Upload
                    aria-label="上传王圻原始文件"
                    showUploadList={false}
                    beforeUpload={(file) => {
                        onUpload(file);
                        return Upload.LIST_IGNORE;
                    }}
                >
                    <Button
                        aria-label={hasSourceFile ? "替换王圻原始文件" : "上传王圻原始文件"}
                        icon={<UploadOutlined />}
                        loading={uploading}
                    >
                        {hasSourceFile ? "替换原始文件" : "上传原始文件"}
                    </Button>
                </Upload>
                <Button
                    aria-label="预览王圻原始文件"
                    icon={<EyeOutlined />}
                    href={previewUrl}
                    target="_blank"
                    disabled={!previewUrl || !hasSourceFile}
                >
                    预览
                </Button>
                <Button
                    aria-label="下载王圻原始文件"
                    icon={<DownloadOutlined />}
                    href={downloadUrl}
                    target="_blank"
                    disabled={!downloadUrl || !hasSourceFile}
                >
                    下载
                </Button>
            </Space>
            {hasSourceFile ? (
                <Descriptions
                    className="wangqi-storage-file-panel-meta"
                    column={1}
                    size="small"
                    bordered
                >
                    <Descriptions.Item label="文档 ID">
                        {document?.id ?? sourceFile?.documentId}
                    </Descriptions.Item>
                    <Descriptions.Item label="对象 ID">
                        {sourceFile?.storageObjectId ?? document?.storageObjectId ?? "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="文件名">
                        {sourceFile?.originalFilename || "未读取元数据"}
                    </Descriptions.Item>
                    <Descriptions.Item label="内容类型">
                        {sourceFile?.contentType || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="文件大小">
                        {formatSize(sourceFile?.size)}
                    </Descriptions.Item>
                </Descriptions>
            ) : (
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="未关联原始文件" />
            )}
            <Text type="secondary" className="wangqi-storage-file-panel-note">
                上传或替换会更新当前王圻文档关联，不会删除旧 Storage 对象。
            </Text>
        </section>
    );
};
