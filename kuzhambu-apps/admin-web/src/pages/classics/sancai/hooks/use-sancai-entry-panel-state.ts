import { useMutation, useQuery } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { MessageInstance } from "antd/es/message/interface";
import * as classicsContentService from "@/pages/classics/common/classics-content-service";
import type { ClassicsContentTagRecord } from "@/pages/classics/common/classics-content-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { QueryClient } from "@tanstack/react-query";
import type { SancaiVisualAssetRefinementCapability } from "../services/sancai-entry-service";
import type { SancaiEntryRecord, SancaiVisualAssetRecord } from "../sancai-types";

type RefinementCapability = "translate" | "summary" | SancaiVisualAssetRefinementCapability;

const DEFAULT_REFINEMENT_MODEL_ID = 1;
const DEFAULT_REFINEMENT_MODEL_NAME = "gpt-5.5";
const DEFAULT_REFINEMENT_SERVICE_ROLE = "PRIMARY";

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const buildPromptMessagesJson = (
    capability: RefinementCapability,
    entry: SancaiEntryRecord,
    asset?: SancaiVisualAssetRecord | null
) => {
    if (capability === "translate") {
        return JSON.stringify([
            {
                role: "system",
                content: "你是古籍翻译助手，请输出可直接展示的现代汉语译文。"
            },
            {
                role: "user",
                content: entry.originalText?.trim() || ""
            }
        ]);
    }
    if (capability === "image_analysis") {
        return JSON.stringify([
            {
                role: "system",
                content: "你是图片理解助手，请输出结构清晰、可展示的中文描述。"
            },
            {
                role: "user",
                content: JSON.stringify({
                    title: entry.title,
                    contentId: entry.id,
                    capability
                })
            }
        ]);
    }
    if (capability === "visual") {
        return JSON.stringify([
            {
                role: "system",
                content: "你是古籍视觉描述助手，请输出可直接展示的中文视觉说明。"
            },
            {
                role: "user",
                content: JSON.stringify({
                    title: entry.title,
                    contentId: entry.id,
                    capability
                })
            }
        ]);
    }
    if (capability === "fusion") {
        return JSON.stringify([
            {
                role: "system",
                content:
                    "你是古籍视觉信息融合助手，请结合文本语义和图片理解生成可用于视觉生成的融合说明。"
            },
            {
                role: "user",
                content: JSON.stringify({
                    title: entry.title,
                    contentId: entry.id,
                    capability
                })
            }
        ]);
    }
    if (capability === "image_gen") {
        return JSON.stringify([
            {
                role: "system",
                content: "你是古籍生图助手，请根据视觉描述输出稳定可执行的生图结果。"
            },
            {
                role: "user",
                content: JSON.stringify({
                    title: entry.title,
                    contentId: entry.id,
                    objectId: asset?.visualAssetId ?? asset?.id ?? null,
                    capability
                })
            }
        ]);
    }
    return JSON.stringify([
        {
            role: "system",
            content: "你是古籍摘要助手，请输出可直接展示的简明中文摘要。"
        },
        {
            role: "user",
            content: JSON.stringify({
                title: entry.title,
                originalText: entry.originalText,
                translationText: entry.translationText
            })
        }
    ]);
};

const buildInputPayloadJson = (
    capability: RefinementCapability,
    entry: SancaiEntryRecord,
    objectId?: number | null,
    textWeight?: number | null,
    imageWeight?: number | null,
    imageAnalysisMarkdown?: string | null,
    fusionDescription?: string | null,
    visualDescription?: string | null,
    generationParamsJson?: string | null,
    sourceImageStorageObjectId?: number | null
) => {
    const payload = {
        capability,
        contentId: entry.id,
        contentType: "SANCAI_ENTRY",
        objectId,
        originalText: entry.originalText,
        summary: entry.summary,
        title: entry.title,
        translationText: entry.translationText
    };
    if (capability === "fusion" || capability === "visual") {
        return JSON.stringify({
            ...payload,
            sourceImageStorageObjectId: sourceImageStorageObjectId ?? null,
            imageAnalysisMarkdown: imageAnalysisMarkdown ?? null,
            textWeight,
            imageWeight
        });
    }
    if (capability === "image_gen") {
        return JSON.stringify({
            ...payload,
            sourceImageStorageObjectId: sourceImageStorageObjectId ?? null,
            fusionDescription: fusionDescription ?? null,
            visualDescription: visualDescription ?? null,
            generationParamsJson: generationParamsJson ?? null
        });
    }
    return JSON.stringify(payload);
};

const readActiveTagNames = (tags: ClassicsContentTagRecord[] | undefined) => {
    const names = (tags || [])
        .map((tag) => tag.tagNameSnapshot?.trim())
        .filter((tagName): tagName is string => Boolean(tagName));
    return [...new Set(names)];
};

interface UseSancaiEntryPanelStateParams {
    queryClient: QueryClient;
    messageApi: MessageInstance;
    selectedEntry: SancaiEntryRecord | null | undefined;
    selectedEntryId: number | null;
    currentUserId?: number | string | null;
    refinementTasks: AiRefinementTaskRecord[];
    invalidateEntries: () => Promise<void>;
    invalidateRefinementTasks: () => Promise<void>;
}

interface UseSancaiEntryPanelStateResult {
    setSelectedVisualAsset: (asset: SancaiVisualAssetRecord | null) => void;
    selectedVisualAssetId: number | null;
    entryTagNames: string[];
    creatingRefinementCapability: RefinementCapability | null;
    invalidateSancaiContentGovernance: () => Promise<void>;
    invalidateSancaiContentCandidates: (visualAssetId: number | null) => Promise<void>;
    refreshSancaiEntryDetail: () => Promise<void>;
    createRefinementTask: (
        capability: RefinementCapability,
        imageAnalysisAsset?: SancaiVisualAssetRecord | null
    ) => void;
    refreshAfterImageAnalysisApplied: () => Promise<void>;
    resetHandledSucceededTaskIds: () => void;
}

export const useSancaiEntryPanelState = ({
    queryClient,
    messageApi,
    selectedEntry,
    selectedEntryId,
    currentUserId,
    refinementTasks,
    invalidateEntries,
    invalidateRefinementTasks
}: UseSancaiEntryPanelStateParams): UseSancaiEntryPanelStateResult => {
    const [creatingRefinementCapability, setCreatingRefinementCapability] =
        useState<RefinementCapability | null>(null);
    const [selectedVisualAsset, setSelectedVisualAsset] = useState<SancaiVisualAssetRecord | null>(
        null
    );
    const selectedVisualAssetId =
        selectedVisualAsset?.visualAssetId ?? selectedVisualAsset?.id ?? null;

    const entryTagsQuery = useQuery({
        queryKey: ["classics", "content", "tags", "SANCAI_ENTRY", selectedEntryId],
        queryFn: () =>
            classicsContentService.listTags({
                contentType: "SANCAI_ENTRY",
                contentId: selectedEntryId ?? 0
            }),
        enabled: Boolean(selectedEntryId),
        retry: false
    });

    const entryTagNames = useMemo(
        () => readActiveTagNames(entryTagsQuery.data),
        [entryTagsQuery.data]
    );
    const handledSucceededTaskIdsRef = useRef<Set<number>>(new Set());
    const resetHandledSucceededTaskIds = () => {
        handledSucceededTaskIdsRef.current.clear();
    };

    const refreshSancaiEntryDetail = useCallback(async () => {
        if (!selectedEntryId) {
            return;
        }
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "detail", selectedEntryId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries", "versions", selectedEntryId]
            })
        ]);
    }, [queryClient, selectedEntryId]);
    const refreshSancaiVisualAssets = useCallback(async () => {
        if (!selectedEntryId) {
            return;
        }
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "entries", "visual-assets", selectedEntryId]
        });
    }, [queryClient, selectedEntryId]);

    const invalidateSancaiContentGovernance = useCallback(async () => {
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "tags", "SANCAI_ENTRY", selectedEntryId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "SANCAI_ENTRY"]
            })
        ]);
    }, [queryClient, selectedEntryId]);

    const invalidateSancaiContentCandidates = useCallback(
        (visualAssetId: number | null) =>
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "SANCAI_ENTRY", selectedEntryId, visualAssetId]
            }),
        [queryClient, selectedEntryId]
    );

    const createRefinementTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onMutate: (command) => {
            if (
                command.capability === "translate" ||
                command.capability === "summary" ||
                command.capability === "image_analysis" ||
                command.capability === "visual" ||
                command.capability === "fusion"
            ) {
                setCreatingRefinementCapability(command.capability);
            }
        },
        onSuccess: async (_, command) => {
            await invalidateRefinementTasks();
            if (command.capability === "translate") {
                messageApi.success("译文任务已创建");
            } else if (command.capability === "summary") {
                messageApi.success("摘要任务已创建");
            } else if (command.capability === "visual") {
                messageApi.success("视觉描述任务已创建");
            } else if (command.capability === "fusion") {
                messageApi.success("信息融合任务已创建");
            } else if (command.capability === "image_gen") {
                messageApi.success("生图任务已创建");
            } else {
                messageApi.success("图片理解任务已创建");
            }
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 精修任务创建失败");
        },
        onSettled: () => {
            setCreatingRefinementCapability(null);
        }
    });

    useEffect(() => {
        const newlySucceededTaskIds = refinementTasks
            .filter(
                (task) =>
                    (task.status === "SUCCEEDED" || task.status === "PARTIAL") &&
                    typeof task.taskId === "number" &&
                    !handledSucceededTaskIdsRef.current.has(task.taskId)
            )
            .map((task) => task.taskId);
        if (!newlySucceededTaskIds.length) {
            return;
        }
        newlySucceededTaskIds.forEach((taskId) => handledSucceededTaskIdsRef.current.add(taskId));
        void Promise.all([
            refreshSancaiEntryDetail(),
            invalidateEntries(),
            invalidateSancaiContentGovernance()
        ]);
    }, [
        invalidateEntries,
        invalidateSancaiContentGovernance,
        refreshSancaiEntryDetail,
        refinementTasks
    ]);

    const createRefinementTask = (
        capability: RefinementCapability,
        imageAnalysisAsset: SancaiVisualAssetRecord | null = null
    ) => {
        if (!selectedEntry?.id) {
            return;
        }
        if (!currentUserId) {
            messageApi.warning("当前用户信息尚未加载完成");
            return;
        }
        const resolvedVisualAsset = imageAnalysisAsset ?? selectedVisualAsset;
        const imageAnalysisObjectId = resolvedVisualAsset
            ? (resolvedVisualAsset.visualAssetId ?? resolvedVisualAsset.id ?? null)
            : null;
        const sourceImageStorageObjectId = resolvedVisualAsset?.sourceImageStorageObjectId;
        const imageAnalysisMarkdown = resolvedVisualAsset?.imageAnalysisMarkdown;
        const fusionDescription = resolvedVisualAsset?.fusionDescription;
        const visualDescription = resolvedVisualAsset?.visualDescription;
        const generationParamsJson = resolvedVisualAsset?.generationParamsJson;
        const textWeight = resolvedVisualAsset?.textWeight;
        const imageWeight = resolvedVisualAsset?.imageWeight;
        if (
            (capability === "image_analysis" ||
                capability === "visual" ||
                capability === "fusion" ||
                capability === "image_gen") &&
            (imageAnalysisObjectId == null || sourceImageStorageObjectId == null)
        ) {
            messageApi.warning("当前视觉资产缺少原图，无法创建图片相关任务");
            return;
        }
        if (
            capability === "fusion" &&
            (imageAnalysisObjectId == null ||
                sourceImageStorageObjectId == null ||
                !imageAnalysisMarkdown?.trim())
        ) {
            messageApi.warning("当前视觉资产缺少图片理解结果，无法创建信息融合任务");
            return;
        }
        if (
            capability === "fusion" &&
            (!Number.isInteger(textWeight) || !Number.isInteger(imageWeight))
        ) {
            messageApi.warning("当前视觉资产权重未正确设置，无法创建信息融合任务");
            return;
        }
        if (
            capability === "visual" &&
            (!imageAnalysisMarkdown?.trim() ||
                !Number.isInteger(textWeight) ||
                !Number.isInteger(imageWeight))
        ) {
            messageApi.warning("当前视觉资产缺少图片理解结果或权重，无法创建视觉描述任务");
            return;
        }
        if (capability === "image_gen" && !visualDescription?.trim()) {
            messageApi.warning("当前视觉资产缺少视觉描述结果，无法创建生图任务");
            return;
        }
        if (
            capability !== "image_analysis" &&
            capability !== "visual" &&
            capability !== "fusion" &&
            capability !== "image_gen" &&
            !selectedEntry.originalText?.trim()
        ) {
            messageApi.warning("当前条目缺少原文，无法创建 AI 精修任务");
            return;
        }
        createRefinementTaskMutation.mutate({
            capability,
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: selectedEntry.id,
            objectId:
                capability === "image_analysis" ||
                capability === "visual" ||
                capability === "fusion"
                    ? imageAnalysisObjectId
                    : null,
            requestedBy: Number(currentUserId),
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId("sancai-task"),
            traceId: createEventId("sancai-trace"),
            promptMessagesJson: buildPromptMessagesJson(
                capability,
                selectedEntry,
                resolvedVisualAsset
            ),
            promptVariablesJson: JSON.stringify({
                title: selectedEntry.title
            }),
            inputPayloadJson: buildInputPayloadJson(
                capability,
                selectedEntry,
                capability === "image_analysis" ||
                    capability === "visual" ||
                    capability === "fusion" ||
                    capability === "image_gen"
                    ? imageAnalysisObjectId
                    : null,
                textWeight,
                imageWeight,
                imageAnalysisMarkdown,
                fusionDescription,
                visualDescription,
                generationParamsJson,
                sourceImageStorageObjectId
            ),
            locale: "zh-CN"
        });
    };

    const refreshAfterImageAnalysisApplied = useCallback(async () => {
        await Promise.all([
            refreshSancaiEntryDetail(),
            refreshSancaiVisualAssets(),
            invalidateSancaiContentCandidates(selectedVisualAssetId)
        ]);
    }, [
        refreshSancaiEntryDetail,
        refreshSancaiVisualAssets,
        selectedVisualAssetId,
        invalidateSancaiContentCandidates
    ]);

    return {
        setSelectedVisualAsset,
        selectedVisualAssetId,
        entryTagNames,
        creatingRefinementCapability,
        invalidateSancaiContentGovernance,
        invalidateSancaiContentCandidates,
        refreshSancaiEntryDetail,
        createRefinementTask,
        refreshAfterImageAnalysisApplied,
        resetHandledSucceededTaskIds
    };
};
