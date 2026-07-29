import {
    ArrowLeftOutlined,
    ArrowRightOutlined,
    BranchesOutlined,
    CheckOutlined,
    FileSearchOutlined,
    FileTextOutlined,
    PictureOutlined,
    SwapOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Col, Empty, Form, Image, Input, Row, Tag, Typography } from "antd";
import { useEffect, useMemo, useRef, useState } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuModal,
    KuzhambuSpace,
    KuzhambuStep,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuSelect
} from "@/components";

import type { AiCandidateApplyRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as entryService from "@/pages/classics/sancai-visual/sancai-visual-service";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";
import { useSancaiEntryVisualRefinement } from "./hooks/use-sancai-entry-visual-refinement";
import { SancaiEntryVisualRefinementSection } from "./sancai-entry-visual-refinement-section";
import "./sancai-entry-visual-section.css";

const { Text } = Typography;

const readVisualAssetTitle = (asset: SancaiVisualAssetRecord | undefined | null) => {
    if (!asset) {
        return "未选择视觉处理";
    }
    if (!asset.versionNo && !asset.visualAssetId && !asset.id) {
        return "待写入历史";
    }
    return `历史记录 ${asset.versionNo ?? asset.visualAssetId ?? asset.id ?? "-"}`;
};

const readVisualAssetId = (asset: SancaiVisualAssetRecord) => {
    return asset.visualAssetId ?? asset.id ?? "";
};

const selectCurrentVisualAsset = (assets: SancaiVisualAssetRecord[]) => {
    return [...assets]
        .filter((asset) => asset.currentUsed !== false)
        .sort((left, right) => (right.versionNo ?? 0) - (left.versionNo ?? 0))[0];
};

const resolveImageUrl = (
    entryId: string | undefined,
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

const isSameStorageObjectId = (
    left: number | string | null | undefined,
    right: number | string | null | undefined
) => {
    return left != null && right != null && String(left) === String(right);
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

const hasVisualText = (value?: string | null) => Boolean(value?.trim());

const readStepStatus = (done: boolean, blocked: boolean) => {
    if (blocked) {
        return { color: "default", label: "待前置", stepStatus: "wait" as const };
    }
    if (done) {
        return { color: "success", label: "已完成", stepStatus: "finish" as const };
    }
    return { color: "processing", label: "可执行", stepStatus: "process" as const };
};

type VisualStepKey = "source" | "image_analysis" | "fusion" | "visual" | "image_gen" | "save";

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

const normalizeNumberField = (value: unknown) => {
    if (value === "" || value == null) {
        return null;
    }
    const numericValue = Number(value);
    return Number.isFinite(numericValue) ? numericValue : null;
};

const VISUAL_ASSET_FORM_FIELD_NAMES: Array<keyof SancaiVisualAssetRecord> = [
    "sourceImageStorageObjectId",
    "sourcePreviewUrl",
    "sourceDownloadUrl",
    "generatedImageStorageObjectId",
    "generatedPreviewUrl",
    "generatedDownloadUrl",
    "textWeight",
    "imageWeight",
    "imageAnalysisMarkdown",
    "fusionDescription",
    "visualDescription",
    "generationParamsJson"
];

const toVisualAssetFormValue = (
    asset: SancaiVisualAssetRecord | null,
    defaultSourceImage: SancaiEntryImageRecord | undefined,
    entryId: string | undefined
) => {
    if (!asset) {
        return null;
    }
    const formValue = { ...asset };
    if (!formValue.sourceImageStorageObjectId && defaultSourceImage?.storageObjectId) {
        formValue.sourceImageStorageObjectId = defaultSourceImage.storageObjectId;
        formValue.sourcePreviewUrl = resolveImageUrl(entryId, defaultSourceImage, "preview");
        formValue.sourceDownloadUrl = resolveImageUrl(entryId, defaultSourceImage, "download");
    }
    return formValue;
};

interface SancaiEntryVisualSectionProps {
    entry: SancaiEntryRecord;
    isUpdatingVisualAsset: boolean;
    onRefinementChanged: () => Promise<void> | void;
    onPreviewStateChange: (state: SancaiEntryVisualPreviewState) => void;
    onUpdateVisualAsset?: (
        asset: SancaiVisualAssetRecord
    ) => Promise<SancaiVisualAssetRecord | void> | SancaiVisualAssetRecord | void;
    onUseVisualAsset?: (asset: SancaiVisualAssetRecord) => void;
}

export interface SancaiEntryVisualPreviewState {
    currentVisualAsset: SancaiVisualAssetRecord | null;
    generatedPreviewUrl?: string;
    visualDescription?: string | null;
}

export const SancaiEntryVisualSection = ({
    entry,
    isUpdatingVisualAsset,
    onRefinementChanged,
    onPreviewStateChange,
    onUpdateVisualAsset,
    onUseVisualAsset
}: SancaiEntryVisualSectionProps) => {
    const entryId = entry.id;
    const imagesQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "images", entryId],
        queryFn: () => entryService.listImages(entryId),
        enabled: Boolean(entryId),
        refetchOnMount: false,
        retry: false
    });
    const entryImages = useMemo(() => imagesQuery.data || [], [imagesQuery.data]);
    const visualAssetsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "visual-assets", entryId],
        queryFn: () => entryService.listVisualAssets(entryId),
        enabled: Boolean(entryId),
        refetchOnMount: false,
        retry: false
    });
    const visualAssets = useMemo(() => visualAssetsQuery.data || [], [visualAssetsQuery.data]);
    const orderedVisualAssets = useMemo(
        () =>
            [...visualAssets].sort((left, right) => {
                if ((left.versionNo ?? 0) !== (right.versionNo ?? 0)) {
                    return (right.versionNo ?? 0) - (left.versionNo ?? 0);
                }
                return aiRefinementTaskService.sortDecimalIdDesc(
                    left.visualAssetId ?? left.id,
                    right.visualAssetId ?? right.id
                );
            }),
        [visualAssets]
    );
    const currentVisualAsset = useMemo(
        () => selectCurrentVisualAsset(visualAssets),
        [visualAssets]
    );
    const [selectedVisualAssetId, setSelectedVisualAssetId] = useState<string | null>(null);
    const [manualWorkflowStep, setManualWorkflowStep] = useState<{
        assetId: string | null;
        stepKey: VisualStepKey;
    } | null>(null);
    const [optimisticVisualAsset, setOptimisticVisualAsset] =
        useState<SancaiVisualAssetRecord | null>(null);
    const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);
    const [visualAssetForm] = Form.useForm<SancaiVisualAssetRecord>();
    const loadedVisualAssetIdRef = useRef<string | null>(null);
    const defaultSourceImage = entryImages.find((image) => image.storageObjectId);
    const activeVisualAssetId =
        selectedVisualAssetId ??
        currentVisualAsset?.visualAssetId ??
        currentVisualAsset?.id ??
        null;
    const draftVisualAsset = useMemo<SancaiVisualAssetRecord | null>(() => {
        if (activeVisualAssetId || !defaultSourceImage?.storageObjectId) {
            return null;
        }
        return {
            entryId,
            status: "DRAFT",
            sourceImageStorageObjectId: defaultSourceImage.storageObjectId,
            sourcePreviewUrl: resolveImageUrl(entryId, defaultSourceImage, "preview"),
            sourceDownloadUrl: resolveImageUrl(entryId, defaultSourceImage, "download"),
            textWeight: 60,
            imageWeight: 40
        };
    }, [activeVisualAssetId, defaultSourceImage, entryId]);
    const selectedVisualAsset =
        visualAssets.find(
            (asset) => (asset.visualAssetId ?? asset.id ?? null) === activeVisualAssetId
        ) ||
        (optimisticVisualAsset &&
        (optimisticVisualAsset.visualAssetId ?? optimisticVisualAsset.id ?? null) ===
            activeVisualAssetId
            ? optimisticVisualAsset
            : null) ||
        currentVisualAsset ||
        draftVisualAsset;
    const initialVisualAssetFormValue = useMemo(
        () => toVisualAssetFormValue(selectedVisualAsset, defaultSourceImage, entryId),
        [defaultSourceImage, entryId, selectedVisualAsset]
    );
    useEffect(() => {
        const nextVisualAssetId = initialVisualAssetFormValue
            ? readVisualAssetId(initialVisualAssetFormValue)
            : null;
        if (!initialVisualAssetFormValue) {
            loadedVisualAssetIdRef.current = null;
            visualAssetForm.resetFields();
            return;
        }
        if (loadedVisualAssetIdRef.current !== nextVisualAssetId) {
            loadedVisualAssetIdRef.current = nextVisualAssetId;
            visualAssetForm.resetFields();
            visualAssetForm.setFieldsValue(initialVisualAssetFormValue);
            return;
        }
        const untouchedServerValues = VISUAL_ASSET_FORM_FIELD_NAMES.reduce<
            Partial<SancaiVisualAssetRecord>
        >((values, fieldName) => {
            if (!visualAssetForm.isFieldTouched(fieldName)) {
                values[fieldName] = initialVisualAssetFormValue[fieldName] as never;
            }
            return values;
        }, {});
        if (Object.keys(untouchedServerValues).length > 0) {
            visualAssetForm.setFieldsValue(untouchedServerValues);
        }
    }, [initialVisualAssetFormValue, visualAssetForm]);
    const sourceImageStorageObjectId = Form.useWatch("sourceImageStorageObjectId", visualAssetForm);
    const textWeight = Form.useWatch("textWeight", visualAssetForm);
    const imageWeight = Form.useWatch("imageWeight", visualAssetForm);
    const imageAnalysisMarkdown = Form.useWatch("imageAnalysisMarkdown", visualAssetForm);
    const fusionDescription = Form.useWatch("fusionDescription", visualAssetForm);
    const visualDescription = Form.useWatch("visualDescription", visualAssetForm);
    const generationParamsJson = Form.useWatch("generationParamsJson", visualAssetForm);
    const sourcePreviewUrlField = Form.useWatch("sourcePreviewUrl", visualAssetForm);
    const sourceDownloadUrl = Form.useWatch("sourceDownloadUrl", visualAssetForm);
    const generatedPreviewUrlField = Form.useWatch("generatedPreviewUrl", visualAssetForm);
    const generatedImageStorageObjectId = Form.useWatch(
        "generatedImageStorageObjectId",
        visualAssetForm
    );
    const visualAssetFormValue = useMemo(() => {
        if (!selectedVisualAsset) {
            return null;
        }
        return {
            ...selectedVisualAsset,
            sourceImageStorageObjectId:
                sourceImageStorageObjectId ?? selectedVisualAsset.sourceImageStorageObjectId,
            sourcePreviewUrl: sourcePreviewUrlField ?? selectedVisualAsset.sourcePreviewUrl,
            sourceDownloadUrl: sourceDownloadUrl ?? selectedVisualAsset.sourceDownloadUrl,
            generatedImageStorageObjectId:
                generatedImageStorageObjectId ?? selectedVisualAsset.generatedImageStorageObjectId,
            generatedPreviewUrl:
                generatedPreviewUrlField ?? selectedVisualAsset.generatedPreviewUrl,
            textWeight: normalizeNumberField(textWeight ?? selectedVisualAsset.textWeight),
            imageWeight: normalizeNumberField(imageWeight ?? selectedVisualAsset.imageWeight),
            imageAnalysisMarkdown:
                imageAnalysisMarkdown ?? selectedVisualAsset.imageAnalysisMarkdown,
            fusionDescription: fusionDescription ?? selectedVisualAsset.fusionDescription,
            visualDescription: visualDescription ?? selectedVisualAsset.visualDescription,
            generationParamsJson: generationParamsJson ?? selectedVisualAsset.generationParamsJson
        };
    }, [
        fusionDescription,
        generatedImageStorageObjectId,
        generatedPreviewUrlField,
        generationParamsJson,
        imageAnalysisMarkdown,
        imageWeight,
        selectedVisualAsset,
        sourceDownloadUrl,
        sourceImageStorageObjectId,
        sourcePreviewUrlField,
        textWeight,
        visualDescription
    ]);
    const selectedVisualAssetResourceId = selectedVisualAsset
        ? readVisualAssetId(selectedVisualAsset) || null
        : null;
    const isDraftVisualAsset = Boolean(selectedVisualAsset && !selectedVisualAssetResourceId);
    const selectedSourceStorageObjectId =
        visualAssetFormValue?.sourceImageStorageObjectId ??
        defaultSourceImage?.storageObjectId ??
        null;
    const visualAssetsForSelectedSource = useMemo(
        () =>
            selectedSourceStorageObjectId == null
                ? []
                : orderedVisualAssets.filter((asset) =>
                      isSameStorageObjectId(
                          asset.sourceImageStorageObjectId,
                          selectedSourceStorageObjectId
                      )
                  ),
        [orderedVisualAssets, selectedSourceStorageObjectId]
    );
    const selectedSourceImage = entryImages.find(
        (image) =>
            image.storageObjectId != null &&
            isSameStorageObjectId(image.storageObjectId, selectedSourceStorageObjectId)
    );
    const selectedSourcePreviewUrl = selectedSourceImage
        ? resolveImageUrl(entryId, selectedSourceImage, "preview")
        : undefined;
    const hasSourceVisualImage = Boolean(
        selectedSourceImage ||
        visualAssetFormValue?.sourceImageStorageObjectId ||
        selectedVisualAsset?.sourceImageStorageObjectId
    );
    const hasGeneratedVisualImage = Boolean(
        visualAssetFormValue?.generatedImageStorageObjectId ||
        selectedVisualAsset?.generatedImageStorageObjectId
    );
    const sourcePreviewUrl =
        selectedSourcePreviewUrl ??
        resolveStorageUrl(
            hasSourceVisualImage
                ? (visualAssetFormValue?.sourcePreviewUrl ??
                      selectedVisualAsset?.sourcePreviewUrl ??
                      (selectedVisualAssetResourceId
                          ? entryService.getVisualAssetContentUrl({
                                entryId,
                                visualAssetId: selectedVisualAssetResourceId,
                                variant: "source"
                            })
                          : undefined))
                : undefined
        );
    const generatedPreviewUrl = resolveStorageUrl(
        hasGeneratedVisualImage
            ? (visualAssetFormValue?.generatedPreviewUrl ??
                  selectedVisualAsset?.generatedPreviewUrl ??
                  (selectedVisualAssetResourceId
                      ? entryService.getVisualAssetContentUrl({
                            entryId,
                            visualAssetId: selectedVisualAssetResourceId,
                            variant: "generated"
                        })
                      : undefined))
            : undefined
    );
    const saveVisualAsset = async () => {
        if (!selectedVisualAsset || !onUpdateVisualAsset) {
            return null;
        }
        await visualAssetForm.validateFields();
        const values = visualAssetForm.getFieldsValue(true);
        const savedAsset = await onUpdateVisualAsset({
            ...selectedVisualAsset,
            ...values,
            textWeight: normalizeNumberField(values.textWeight),
            imageWeight: normalizeNumberField(values.imageWeight)
        });
        if (savedAsset) {
            const savedAssetId = readVisualAssetId(savedAsset);
            if (savedAssetId) {
                setOptimisticVisualAsset(savedAsset);
                setSelectedVisualAssetId(savedAssetId);
            }
            visualAssetForm.setFieldsValue(
                toVisualAssetFormValue(savedAsset, defaultSourceImage, entryId) ?? savedAsset
            );
        }
        return savedAsset ?? null;
    };
    const selectVisualAsset = (asset: SancaiVisualAssetRecord) => {
        const assetId = asset.visualAssetId ?? asset.id ?? null;
        setOptimisticVisualAsset(null);
        setSelectedVisualAssetId(assetId);
        const nextFormValue = toVisualAssetFormValue(asset, defaultSourceImage, entryId);
        visualAssetForm.resetFields();
        if (nextFormValue) {
            visualAssetForm.setFieldsValue(nextFormValue);
        }
    };
    const selectHistoryVisualAsset = (asset: SancaiVisualAssetRecord) => {
        selectVisualAsset(asset);
        setIsHistoryModalOpen(false);
    };
    const handleUseHistoryVisualAsset = (asset: SancaiVisualAssetRecord) => {
        onUseVisualAsset?.(asset);
        setIsHistoryModalOpen(false);
    };
    const selectVisualSourceImage = (image: SancaiEntryImageRecord) => {
        if (!image.storageObjectId) {
            return;
        }
        visualAssetForm.setFieldsValue({
            sourceImageStorageObjectId: image.storageObjectId,
            sourcePreviewUrl: resolveImageUrl(entryId, image, "preview"),
            sourceDownloadUrl: resolveImageUrl(entryId, image, "download")
        });
    };
    const selectVisualSourceImageBySelectValue = (storageObjectId: string) => {
        const image = entryImages.find((entryImage) =>
            isSameStorageObjectId(entryImage.storageObjectId, storageObjectId)
        );
        if (image) {
            selectVisualSourceImage(image);
        }
    };
    const {
        closeStreamingRefinementTask,
        createVisualAssetTask,
        creatingVisualAssetCapability,
        isStreamingRefinementTask,
        refreshAfterVisualAssetCandidateHandled,
        refreshVisualAssetCandidates,
        retryRefinementTask,
        streamErrorText,
        streamEvents,
        streamingRefinementTask
    } = useSancaiEntryVisualRefinement({
        entry,
        selectedVisualAsset,
        selectedVisualAssetId: selectedVisualAssetResourceId,
        visualAssetFormValue,
        onRefinementChanged
    });
    const handleVisualAssetCandidateChanged = async (result?: AiCandidateApplyRecord) => {
        await refreshAfterVisualAssetCandidateHandled();
        if (result?.versionId) {
            setOptimisticVisualAsset(null);
            setSelectedVisualAssetId(String(result.versionId));
        }
    };
    const imageAnalysisDone = hasVisualText(visualAssetFormValue?.imageAnalysisMarkdown);
    const fusionDone = hasVisualText(visualAssetFormValue?.fusionDescription);
    const visualDescriptionDone = hasVisualText(visualAssetFormValue?.visualDescription);
    const imageGenerationDone = Boolean(generatedPreviewUrl || generatedImageStorageObjectId);
    const saveStepDone = Boolean(selectedVisualAssetResourceId && imageGenerationDone);
    let saveBlockedReason: string | null = null;
    if (!selectedVisualAssetResourceId) {
        saveBlockedReason = "请先确认来源图片。";
    } else if (!imageGenerationDone) {
        saveBlockedReason = "请先完成生图。";
    }
    let imageAnalysisBlockedReason: string | null = null;
    if (!hasSourceVisualImage) {
        imageAnalysisBlockedReason = "请选择来源图片，这张图会作为图片理解、图文融合和生图参考。";
    } else if (!selectedVisualAssetResourceId) {
        imageAnalysisBlockedReason = "请先保存当前来源图片和权重，后续 AI 结果会追加到这条历史。";
    }
    const sourceStepDone = Boolean(hasSourceVisualImage && selectedVisualAssetResourceId);
    const visualWorkflowSteps = [
        {
            key: "source" as const,
            title: "选择图片",
            summary: selectedVisualAssetResourceId
                ? "当前来源图片已确认，可继续进行图片理解。"
                : "选择本次视觉处理的来源图片。",
            status: readStepStatus(sourceStepDone, !hasSourceVisualImage),
            blockedReason: !hasSourceVisualImage ? "请选择来源图片。" : null,
            buttonText: selectedVisualAssetResourceId ? "更新来源" : "确认来源",
            icon: <SwapOutlined />,
            loading: isUpdatingVisualAsset,
            testId: "classics-sancai-sancai-entry-action-button-7",
            onClick: saveVisualAsset
        },
        {
            key: "image_analysis" as const,
            title: "图片理解",
            summary: "识别来源图片里的画面元素、构图和可校对细节。",
            status: readStepStatus(
                imageAnalysisDone,
                !hasSourceVisualImage || !selectedVisualAssetResourceId
            ),
            blockedReason: imageAnalysisBlockedReason,
            buttonText: imageAnalysisDone ? "重新理解" : "开始理解",
            icon: <FileSearchOutlined />,
            loading: creatingVisualAssetCapability === "image_analysis",
            testId: "classics-sancai-sancai-entry-action-button-3",
            onClick: () => {
                createVisualAssetTask("image_analysis");
            }
        },
        {
            key: "fusion" as const,
            title: "图文融合",
            summary: "设置文本和图片权重，结合条目文本、译文和图片理解结果。",
            status: readStepStatus(fusionDone, !imageAnalysisDone),
            blockedReason: imageAnalysisDone ? null : "请先完成图片理解",
            buttonText: fusionDone ? "重新融合" : "生成融合",
            icon: <BranchesOutlined />,
            loading: creatingVisualAssetCapability === "fusion",
            testId: "classics-sancai-sancai-entry-action-button-4",
            onClick: () => {
                createVisualAssetTask("fusion");
            }
        },
        {
            key: "visual" as const,
            title: "视觉描述",
            summary: "把融合结果整理成展示和生图可复用的说明。",
            status: readStepStatus(visualDescriptionDone, !fusionDone),
            blockedReason: fusionDone ? null : "请先完成图文融合",
            buttonText: visualDescriptionDone ? "重新描述" : "生成描述",
            icon: <FileTextOutlined />,
            loading: creatingVisualAssetCapability === "visual",
            testId: "classics-sancai-sancai-entry-action-button-5",
            onClick: () => {
                createVisualAssetTask("visual");
            }
        },
        {
            key: "image_gen" as const,
            title: "生图",
            summary: "根据视觉描述和生成参数生成图片结果。",
            status: readStepStatus(imageGenerationDone, !visualDescriptionDone),
            blockedReason: visualDescriptionDone ? null : "请先完成视觉描述",
            buttonText: imageGenerationDone ? "重新生图" : "生成图片",
            icon: <PictureOutlined />,
            loading: creatingVisualAssetCapability === "image_gen",
            testId: "classics-sancai-sancai-entry-action-button-6",
            onClick: () => {
                createVisualAssetTask("image_gen");
            }
        },
        {
            key: "save" as const,
            title: "保存",
            summary: "确认生成图、说明和参数，保存到视觉处理历史。",
            status: readStepStatus(saveStepDone, Boolean(saveBlockedReason)),
            blockedReason: saveBlockedReason,
            buttonText: "保存历史",
            icon: <CheckOutlined />,
            loading: isUpdatingVisualAsset,
            testId: "classics-sancai-sancai-entry-action-button-7",
            onClick: saveVisualAsset
        }
    ];
    const suggestedWorkflowStepIndex = (() => {
        const unfinishedStepIndex = visualWorkflowSteps.findIndex(
            (step) => step.status.stepStatus !== "finish"
        );
        if (unfinishedStepIndex >= 0) {
            return unfinishedStepIndex;
        }
        return visualWorkflowSteps.findIndex((step) => step.key === "save");
    })();
    const activeWorkflowStepKey =
        manualWorkflowStep?.assetId === selectedVisualAssetResourceId
            ? manualWorkflowStep.stepKey
            : visualWorkflowSteps[suggestedWorkflowStepIndex]?.key;
    const activeWorkflowStepIndex = Math.max(
        0,
        visualWorkflowSteps.findIndex((step) => step.key === activeWorkflowStepKey)
    );
    const currentWorkflowStepIndex = activeWorkflowStepIndex;
    const currentWorkflowStep = visualWorkflowSteps[currentWorkflowStepIndex];
    const canEnterWorkflowStep = (stepIndex: number) => {
        const step = visualWorkflowSteps[stepIndex];
        return Boolean(step && (stepIndex === 0 || !step.blockedReason));
    };
    const canGoToPreviousWorkflowStep =
        currentWorkflowStepIndex > 0 && canEnterWorkflowStep(currentWorkflowStepIndex - 1);
    const canGoToNextWorkflowStep =
        currentWorkflowStepIndex < visualWorkflowSteps.length - 1 &&
        (currentWorkflowStep.key === "source"
            ? hasSourceVisualImage
            : canEnterWorkflowStep(currentWorkflowStepIndex + 1));
    const goToWorkflowStep = (stepIndex: number) => {
        const nextStep = visualWorkflowSteps[stepIndex];
        if (nextStep && canEnterWorkflowStep(stepIndex)) {
            setManualWorkflowStep({
                assetId: selectedVisualAssetResourceId,
                stepKey: nextStep.key
            });
        }
    };
    const goToNextWorkflowStep = async () => {
        const nextStep = visualWorkflowSteps[currentWorkflowStepIndex + 1];
        if (!nextStep || !canGoToNextWorkflowStep) {
            return;
        }
        if (currentWorkflowStep.key === "source" && !selectedVisualAssetResourceId) {
            const savedAsset = await saveVisualAsset();
            const savedAssetId = savedAsset ? readVisualAssetId(savedAsset) : null;
            if (savedAssetId) {
                setManualWorkflowStep({
                    assetId: savedAssetId,
                    stepKey: nextStep.key
                });
            }
            return;
        }
        goToWorkflowStep(currentWorkflowStepIndex + 1);
    };
    useEffect(() => {
        onPreviewStateChange({
            currentVisualAsset: currentVisualAsset ?? null,
            generatedPreviewUrl,
            visualDescription
        });
    }, [currentVisualAsset, generatedPreviewUrl, onPreviewStateChange, visualDescription]);

    return (
        <section
            className="sancai-detail-card sancai-visual-asset-field"
            aria-label="三才图会视觉处理面板"
        >
            {selectedVisualAsset ? (
                <>
                    <KuzhambuForm
                        form={visualAssetForm}
                        className="sancai-entry-edit-drawer-form"
                        colon={false}
                        component="div"
                    >
                        <Row gutter={[12, 12]} align="top">
                            <Col xs={24} xl={15}>
                                <div className="sancai-visual-workflow" aria-label="图文生图工作流">
                                    <Row gutter={[12, 8]} align="top">
                                        <Col xs={24} lg={9} xl={8}>
                                            <div className="sancai-visual-stepper-panel">
                                                <KuzhambuStep
                                                    testId="classics-sancai-visual-workflow-step"
                                                    className="sancai-visual-workflow-stepper"
                                                    current={currentWorkflowStepIndex}
                                                    orientation="vertical"
                                                    responsive
                                                    onChange={goToWorkflowStep}
                                                    items={visualWorkflowSteps.map((step) => ({
                                                        status: step.status.stepStatus,
                                                        title: step.title,
                                                        content: step.status.label
                                                    }))}
                                                />
                                                <div className="sancai-visual-stepper-actions">
                                                    <KuzhambuButton
                                                        testId="classics-sancai-visual-workflow-prev-button"
                                                        ariaLabel="上一步"
                                                        icon={<ArrowLeftOutlined />}
                                                        disabled={!canGoToPreviousWorkflowStep}
                                                        onClick={() =>
                                                            goToWorkflowStep(
                                                                currentWorkflowStepIndex - 1
                                                            )
                                                        }
                                                    />
                                                    <KuzhambuButton
                                                        testId="classics-sancai-visual-workflow-next-button"
                                                        ariaLabel="下一步"
                                                        icon={<ArrowRightOutlined />}
                                                        type="primary"
                                                        disabled={!canGoToNextWorkflowStep}
                                                        loading={
                                                            currentWorkflowStep.key === "source" &&
                                                            isUpdatingVisualAsset
                                                        }
                                                        onClick={goToNextWorkflowStep}
                                                    />
                                                </div>
                                            </div>
                                        </Col>
                                        <Col xs={24} lg={15} xl={16}>
                                            <KuzhambuSpace
                                                orientation="vertical"
                                                size={8}
                                                style={{ width: "100%" }}
                                            >
                                                <KuzhambuAlert
                                                    className="sancai-visual-workflow-alert"
                                                    showIcon
                                                    title={currentWorkflowStep.title}
                                                    description={
                                                        currentWorkflowStep.blockedReason ??
                                                        currentWorkflowStep.summary
                                                    }
                                                    type={
                                                        currentWorkflowStep.blockedReason
                                                            ? "warning"
                                                            : currentWorkflowStep.status
                                                                    .stepStatus === "finish"
                                                              ? "success"
                                                              : "info"
                                                    }
                                                />
                                                <div className="sancai-visual-workflow-step-form">
                                                    {currentWorkflowStep.key === "source" ? (
                                                        <div className="sancai-visual-asset-picker">
                                                            <KuzhambuFormItem
                                                                name="sourceImageStorageObjectId"
                                                                layoutSize="large"
                                                            >
                                                                <KuzhambuSelect
                                                                    aria-label="三才图会视觉处理来源图片"
                                                                    disabled={!defaultSourceImage}
                                                                    placeholder="选择来源图片"
                                                                    options={entryImages.map(
                                                                        (image) => ({
                                                                            disabled:
                                                                                !image.storageObjectId,
                                                                            label: readImageTitle(
                                                                                image
                                                                            ),
                                                                            value:
                                                                                image.storageObjectId ??
                                                                                `image:${image.id}`
                                                                        })
                                                                    )}
                                                                    onChange={(value) =>
                                                                        selectVisualSourceImageBySelectValue(
                                                                            value
                                                                        )
                                                                    }
                                                                />
                                                            </KuzhambuFormItem>
                                                        </div>
                                                    ) : null}
                                                    {currentWorkflowStep.key ===
                                                    "image_analysis" ? (
                                                        <KuzhambuFormItem
                                                            name="imageAnalysisMarkdown"
                                                            layoutSize="large"
                                                            className="sancai-entry-edit-drawer-form-item-top"
                                                        >
                                                            <Input.TextArea
                                                                aria-label="三才图会视觉处理图片理解"
                                                                autoSize={resolveTextAreaAutoSize({
                                                                    minRows: 2,
                                                                    maxRows: 4
                                                                })}
                                                            />
                                                        </KuzhambuFormItem>
                                                    ) : null}
                                                    {currentWorkflowStep.key === "fusion" ? (
                                                        <>
                                                            <Row gutter={[10, 10]}>
                                                                <Col xs={24} sm={12}>
                                                                    <KuzhambuFormItem
                                                                        name="textWeight"
                                                                        label="文本权重"
                                                                        layoutSize="middle"
                                                                    >
                                                                        <Input aria-label="三才图会视觉处理文本权重" />
                                                                    </KuzhambuFormItem>
                                                                </Col>
                                                                <Col xs={24} sm={12}>
                                                                    <KuzhambuFormItem
                                                                        name="imageWeight"
                                                                        label="图片权重"
                                                                        layoutSize="middle"
                                                                    >
                                                                        <Input aria-label="三才图会视觉处理图片权重" />
                                                                    </KuzhambuFormItem>
                                                                </Col>
                                                            </Row>
                                                            <KuzhambuFormItem
                                                                name="fusionDescription"
                                                                label="图文融合"
                                                                layoutSize="large"
                                                                className="sancai-entry-edit-drawer-form-item-top"
                                                            >
                                                                <Input.TextArea
                                                                    aria-label="三才图会视觉处理融合描述"
                                                                    autoSize={resolveTextAreaAutoSize(
                                                                        {
                                                                            minRows: 2,
                                                                            maxRows: 4
                                                                        }
                                                                    )}
                                                                />
                                                            </KuzhambuFormItem>
                                                        </>
                                                    ) : null}
                                                    {currentWorkflowStep.key === "visual" ? (
                                                        <KuzhambuFormItem
                                                            name="visualDescription"
                                                            label="视觉描述"
                                                            layoutSize="large"
                                                            className="sancai-entry-edit-drawer-form-item-top"
                                                        >
                                                            <Input.TextArea
                                                                aria-label="三才图会视觉处理视觉描述"
                                                                autoSize={resolveTextAreaAutoSize({
                                                                    minRows: 2,
                                                                    maxRows: 4
                                                                })}
                                                            />
                                                        </KuzhambuFormItem>
                                                    ) : null}
                                                    {currentWorkflowStep.key === "image_gen" ? (
                                                        <KuzhambuFormItem
                                                            name="generationParamsJson"
                                                            label="生成参数"
                                                            layoutSize="large"
                                                            className="sancai-entry-edit-drawer-form-item-top"
                                                        >
                                                            <Input.TextArea
                                                                aria-label="三才图会视觉处理生成参数"
                                                                autoSize={resolveTextAreaAutoSize({
                                                                    minRows: 2,
                                                                    maxRows: 4
                                                                })}
                                                            />
                                                        </KuzhambuFormItem>
                                                    ) : null}
                                                    {currentWorkflowStep.key === "save" ? (
                                                        <div className="sancai-visual-save-review">
                                                            <KuzhambuSpace wrap>
                                                                <Tag
                                                                    color={readVisualAssetStatusTagColor(
                                                                        selectedVisualAsset.status
                                                                    )}
                                                                >
                                                                    {readVisualAssetStatusLabel(
                                                                        selectedVisualAsset.status
                                                                    )}
                                                                </Tag>
                                                                <Text type="secondary">
                                                                    {readVisualAssetTitle(
                                                                        selectedVisualAsset
                                                                    )}
                                                                </Text>
                                                            </KuzhambuSpace>
                                                            <Text type="secondary">
                                                                保存当前来源图片、图片理解、融合描述、视觉描述、生成参数和生成图状态。
                                                            </Text>
                                                        </div>
                                                    ) : null}
                                                    {currentWorkflowStep.key !== "source" ? (
                                                        <div className="sancai-visual-workflow-card-actions">
                                                            <KuzhambuButton
                                                                testId={currentWorkflowStep.testId}
                                                                icon={currentWorkflowStep.icon}
                                                                type="primary"
                                                                loading={
                                                                    currentWorkflowStep.loading
                                                                }
                                                                disabled={Boolean(
                                                                    currentWorkflowStep.blockedReason
                                                                )}
                                                                onClick={
                                                                    currentWorkflowStep.onClick
                                                                }
                                                            >
                                                                {currentWorkflowStep.buttonText}
                                                            </KuzhambuButton>
                                                        </div>
                                                    ) : null}
                                                </div>
                                            </KuzhambuSpace>
                                        </Col>
                                    </Row>
                                </div>
                            </Col>
                            <Col xs={24} xl={9}>
                                <div
                                    className="sancai-visual-asset-media"
                                    aria-label="视觉处理图片"
                                >
                                    <div className="sancai-visual-asset-image-list">
                                        <div className="sancai-visual-asset-image-frame">
                                            <div className="sancai-visual-asset-image-stage">
                                                {sourcePreviewUrl ? (
                                                    <Image
                                                        src={sourcePreviewUrl}
                                                        alt="三才图会视觉处理来源图片"
                                                        style={{
                                                            width: "100%",
                                                            height: "100%",
                                                            objectFit: "contain"
                                                        }}
                                                    />
                                                ) : (
                                                    <Empty
                                                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                                                        description="未选择来源图片"
                                                    />
                                                )}
                                            </div>
                                            <div className="sancai-visual-asset-image-actions">
                                                <Text type="secondary">
                                                    {selectedSourceImage
                                                        ? readImageTitle(selectedSourceImage)
                                                        : "来源图片"}
                                                </Text>
                                            </div>
                                        </div>
                                        <div className="sancai-visual-asset-image-frame">
                                            <div className="sancai-visual-asset-image-stage">
                                                {generatedPreviewUrl ? (
                                                    <Image
                                                        src={generatedPreviewUrl}
                                                        alt="三才图会视觉处理生成图"
                                                        style={{
                                                            width: "100%",
                                                            height: "100%",
                                                            objectFit: "contain"
                                                        }}
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
                                            </div>
                                            <div className="sancai-visual-asset-image-actions">
                                                <KuzhambuSpace wrap>
                                                    <Text type="secondary">生成图</Text>
                                                    <Tag
                                                        color={readVisualAssetStatusTagColor(
                                                            selectedVisualAsset.status
                                                        )}
                                                    >
                                                        {readVisualAssetStatusLabel(
                                                            selectedVisualAsset.status
                                                        )}
                                                    </Tag>
                                                </KuzhambuSpace>
                                                {!isDraftVisualAsset ? (
                                                    <KuzhambuButton
                                                        testId="classics-sancai-visual-asset-adopt-button"
                                                        icon={<CheckOutlined />}
                                                        type="primary"
                                                        loading={isUpdatingVisualAsset}
                                                        onClick={saveVisualAsset}
                                                    >
                                                        采纳
                                                    </KuzhambuButton>
                                                ) : null}
                                            </div>
                                        </div>
                                    </div>
                                    <KuzhambuButton
                                        block
                                        testId="classics-sancai-visual-history-button"
                                        disabled={
                                            !selectedSourceStorageObjectId ||
                                            visualAssetsForSelectedSource.length === 0
                                        }
                                        onClick={() => setIsHistoryModalOpen(true)}
                                    >
                                        {visualAssetsForSelectedSource.length > 0
                                            ? "切换历史版本"
                                            : "暂无历史版本"}
                                    </KuzhambuButton>
                                    <KuzhambuModal
                                        testId="classics-sancai-visual-history-modal"
                                        title="选择视觉处理历史"
                                        open={isHistoryModalOpen}
                                        width={760}
                                        footer={
                                            <KuzhambuButton
                                                testId="classics-sancai-visual-history-close-button"
                                                onClick={() => setIsHistoryModalOpen(false)}
                                            >
                                                关闭
                                            </KuzhambuButton>
                                        }
                                        onCancel={() => setIsHistoryModalOpen(false)}
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
                                                        render: (_, asset) =>
                                                            readVisualAssetTitle(asset)
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
                                                            if (
                                                                asset.status !== "READY" ||
                                                                !previewUrl
                                                            ) {
                                                                return (
                                                                    <Text type="secondary">-</Text>
                                                                );
                                                            }
                                                            return (
                                                                <Image
                                                                    width={56}
                                                                    height={56}
                                                                    className="sancai-visual-asset-table-preview"
                                                                    src={previewUrl}
                                                                    alt={`${readVisualAssetTitle(asset)}生成图预览`}
                                                                    preview={{
                                                                        src:
                                                                            fullImageUrl ??
                                                                            previewUrl
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
                                                                <Tag
                                                                    color={readVisualAssetStatusTagColor(
                                                                        status
                                                                    )}
                                                                >
                                                                    {readVisualAssetStatusLabel(
                                                                        status
                                                                    )}
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
                                                            const assetId =
                                                                readVisualAssetId(asset);
                                                            const selectedAssetId =
                                                                selectedVisualAsset
                                                                    ? readVisualAssetId(
                                                                          selectedVisualAsset
                                                                      )
                                                                    : "";
                                                            const isSelected =
                                                                Boolean(assetId) &&
                                                                assetId === selectedAssetId;
                                                            return [
                                                                {
                                                                    key: "select",
                                                                    text: "选择",
                                                                    ariaLabel: `选择${readVisualAssetTitle(asset)}`,
                                                                    testId: `sancai-visual-asset-${assetId}-select-button`,
                                                                    disabled:
                                                                        !selectedSourceStorageObjectId ||
                                                                        isSelected,
                                                                    onClick: (record) =>
                                                                        selectHistoryVisualAsset(
                                                                            record
                                                                        )
                                                                },
                                                                {
                                                                    key: "use",
                                                                    text: "当前",
                                                                    ariaLabel: `设为当前视觉处理 ${readVisualAssetTitle(asset)}`,
                                                                    testId: `sancai-visual-asset-${assetId}-use-button`,
                                                                    disabled:
                                                                        !selectedSourceStorageObjectId ||
                                                                        Boolean(asset.currentUsed),
                                                                    onClick: (record) =>
                                                                        handleUseHistoryVisualAsset(
                                                                            record
                                                                        )
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
                                </div>
                            </Col>
                        </Row>
                    </KuzhambuForm>
                </>
            ) : (
                <div className="sancai-visual-asset-start-empty">
                    <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description={
                            imagesQuery.isLoading
                                ? "正在加载来源图片"
                                : entryImages.length
                                  ? "请选择来源图片开始视觉处理"
                                  : "当前稿件暂无来源图片"
                        }
                    >
                        {!imagesQuery.isLoading && entryImages.length === 0 ? (
                            <Text type="secondary">
                                请先在稿件基础信息中维护图片，再进行视觉处理。
                            </Text>
                        ) : null}
                    </Empty>
                </div>
            )}
            <SancaiEntryVisualRefinementSection
                entryId={entry.id}
                isStreamingRefinementTask={isStreamingRefinementTask}
                selectedVisualAssetId={selectedVisualAssetResourceId}
                streamErrorText={streamErrorText}
                streamEvents={streamEvents}
                streamingRefinementTask={streamingRefinementTask}
                onCloseStreamingRefinementTask={closeStreamingRefinementTask}
                onRefreshVisualAssetCandidates={refreshVisualAssetCandidates}
                onRetryRefinementTask={retryRefinementTask}
                onVisualAssetCandidateChanged={handleVisualAssetCandidateChanged}
            />
        </section>
    );
};
