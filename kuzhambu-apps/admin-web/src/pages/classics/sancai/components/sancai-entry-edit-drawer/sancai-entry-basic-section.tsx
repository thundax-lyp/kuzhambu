import { DownloadOutlined, UploadOutlined } from "@ant-design/icons";
import { Image, Input, Select, Switch, Typography, Upload } from "antd";
import type { Dispatch, ReactNode, SetStateAction } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuFormItem } from "@/components/kuzhambu-form";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { SancaiEntrySummaryTextField } from "./sancai-entry-summary-text-field";
import type { SancaiEntrySummaryModalProps } from "./sancai-entry-summary-text-field";
import { SancaiEntryTranslationTextField } from "./sancai-entry-translation-text-field";
import type { SancaiEntryTranslationModalProps } from "./sancai-entry-translation-text-field";
import type { SancaiEntryFormValues } from "../sancai-form-values";
import type { SancaiEntryImageRecord } from "@/pages/classics/sancai/sancai-types";

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
    summaryModalProps: SancaiEntrySummaryModalProps;
    translationModalProps: SancaiEntryTranslationModalProps;
    volumeOptions: Array<{ label: string; value: number }>;
    onChangeCategory: (categoryId: number | null) => void;
    onOpenSummaryModal: () => void;
    onOpenTranslationModal: () => void;
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
    summaryModalProps,
    translationModalProps,
    volumeOptions,
    onChangeCategory,
    onOpenSummaryModal,
    onOpenTranslationModal,
    onUploadImage
}: SancaiEntryBasicSectionProps) => {
    return (
        <>
            <KuzhambuFormItem label="门类">
                <Select
                    aria-label="三才图会条目门类"
                    placeholder="选择门类"
                    options={categoryOptions}
                    value={form.categoryId ?? undefined}
                    onChange={(value) => onChangeCategory(value ?? null)}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="卷">
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
            </KuzhambuFormItem>
            <KuzhambuFormItem label="标题" layoutSize="large">
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
            </KuzhambuFormItem>
            <KuzhambuFormItem label="原文" layoutSize="large">
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
            </KuzhambuFormItem>
            <KuzhambuFormItem label="译文" layoutSize="large">
                <SancaiEntryTranslationTextField
                    mode={mode}
                    value={form.translationText}
                    translationModalProps={translationModalProps}
                    onChange={(translationText) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            translationText
                        }))
                    }
                    onOpenTranslationModal={onOpenTranslationModal}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="摘要" layoutSize="large">
                <SancaiEntrySummaryTextField
                    mode={mode}
                    value={form.summary}
                    summaryModalProps={summaryModalProps}
                    onChange={(summary) =>
                        setForm((currentForm) => ({
                            ...currentForm,
                            summary
                        }))
                    }
                    onOpenSummaryModal={onOpenSummaryModal}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="可见性" layoutSize="large">
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
            </KuzhambuFormItem>
            {entryId ? (
                <KuzhambuFormItem label="图片" layoutSize="large">
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
                </KuzhambuFormItem>
            ) : null}
        </>
    );
};
