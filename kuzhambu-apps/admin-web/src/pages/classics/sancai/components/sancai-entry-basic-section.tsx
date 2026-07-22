import {
    DownloadOutlined,
    FileTextOutlined,
    TranslationOutlined,
    UploadOutlined
} from "@ant-design/icons";
import { Form, Image, Input, Select, Switch, Typography, Upload } from "antd";
import type { Dispatch, ReactNode, SetStateAction } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { SancaiAiTextField } from "./sancai-entry-ai-text-config";
import type { SancaiEntryFormValues } from "./sancai-form-values";
import type { SancaiEntryImageRecord } from "../sancai-types";

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

interface SancaiEntryBasicSectionProps {
    categoryOptions: Array<{ label: string; value: number }>;
    currentImage?: SancaiEntryImageRecord;
    downloadUrl?: string;
    entryId?: number;
    form: SancaiEntryFormValues;
    imageContent?: ReactNode;
    isUploadingImage: boolean;
    mode: "create" | "edit";
    previewUrl?: string;
    setForm: Dispatch<SetStateAction<SancaiEntryFormValues>>;
    volumeOptions: Array<{ label: string; value: number }>;
    onChangeCategory: (categoryId: number | null) => void;
    onOpenAiTextModal: (field: SancaiAiTextField) => void;
    onUploadImage: (file: File) => void;
}

export const SancaiEntryBasicSection = ({
    categoryOptions,
    currentImage,
    downloadUrl,
    entryId,
    form,
    imageContent,
    isUploadingImage,
    mode,
    previewUrl,
    setForm,
    volumeOptions,
    onChangeCategory,
    onOpenAiTextModal,
    onUploadImage
}: SancaiEntryBasicSectionProps) => {
    return (
        <>
            <div className="sancai-entry-edit-drawer-catalog-row">
                <Form.Item label="门类">
                    <Select
                        aria-label="三才图会条目门类"
                        placeholder="选择门类"
                        options={categoryOptions}
                        value={form.categoryId ?? undefined}
                        onChange={(value) => onChangeCategory(value ?? null)}
                    />
                </Form.Item>
                <Form.Item label="卷">
                    <Select
                        aria-label="三才图会条目卷"
                        disabled={!form.categoryId}
                        placeholder="选择卷"
                        options={volumeOptions}
                        value={form.volumeId ?? undefined}
                        onChange={(value) =>
                            setForm((currentForm) => ({
                                ...currentForm,
                                volumeId: value ?? null
                            }))
                        }
                    />
                </Form.Item>
            </div>
            <Form.Item label="标题">
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
            </Form.Item>
            <Form.Item label="原文" className="sancai-entry-edit-drawer-form-item-top">
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
            </Form.Item>
            <Form.Item label="译文" className="sancai-entry-edit-drawer-form-item-top">
                <div className="sancai-entry-ai-text-field">
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
                    {mode === "edit" ? (
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                testId="classics-sancai-sancai-entry-ai-button"
                                className="sancai-entry-ai-text-button"
                                icon={<TranslationOutlined />}
                                onClick={() => onOpenAiTextModal("translate")}
                            >
                                AI翻译
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    ) : null}
                </div>
            </Form.Item>
            <Form.Item label="摘要" className="sancai-entry-edit-drawer-form-item-top">
                <div className="sancai-entry-ai-text-field">
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
                    {mode === "edit" ? (
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                testId="classics-sancai-sancai-entry-ai-summary-button"
                                className="sancai-entry-ai-text-button"
                                icon={<FileTextOutlined />}
                                onClick={() => onOpenAiTextModal("summary")}
                            >
                                AI摘要
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    ) : null}
                </div>
            </Form.Item>
            <Form.Item label="可见性">
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
            </Form.Item>
            {entryId ? (
                <Form.Item label="图片">
                    {imageContent || (
                        <div className="sancai-entry-image-field">
                            {currentImage && previewUrl ? (
                                <div className="sancai-entry-image-frame">
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
                                        testId="classics-sancai-sancai-entry-action-button"
                                        icon={<UploadOutlined />}
                                        loading={isUploadingImage}
                                    >
                                        上传
                                    </KuzhambuButton>
                                </Upload>
                                <KuzhambuButton
                                    testId="classics-sancai-sancai-entry-action-button-2"
                                    icon={<DownloadOutlined />}
                                    href={downloadUrl}
                                    target="_blank"
                                    disabled={!downloadUrl}
                                >
                                    下载
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </div>
                    )}
                </Form.Item>
            ) : null}
        </>
    );
};
