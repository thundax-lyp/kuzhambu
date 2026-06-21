import { DownloadOutlined, EyeOutlined, UploadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Empty, Image, Input, Space, Switch, Typography, Upload } from "antd";
import { useState } from "react";
import type { ReactNode } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { toEntryFormValues, type SancaiEntryFormValues } from "./sancai-form-values";
import * as entryService from "../services/sancai-entry-service";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord
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

interface SancaiEntryModelProps {
    afterForm?: ReactNode;
    entry: SancaiEntryRecord | undefined;
    isSubmitting: boolean;
    mode?: "create" | "edit";
    open: boolean;
    onCancel: () => void;
    onSubmit: (values: SancaiEntryFormValues) => void;
}

export const SancaiEntryModel = ({
    afterForm,
    entry,
    isSubmitting,
    mode = "edit",
    open,
    onCancel,
    onSubmit
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
                        autoSize={{ minRows: 4, maxRows: 8 }}
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
                        autoSize={{ minRows: 4, maxRows: 8 }}
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
                        autoSize={{ minRows: 3, maxRows: 6 }}
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
                        <Space wrap>
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
                        </Space>
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
            </div>
            {afterForm}
        </KuzhambuDrawer>
    );
};
