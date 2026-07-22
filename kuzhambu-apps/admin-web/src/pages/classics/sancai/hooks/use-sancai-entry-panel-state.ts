import { useMutation } from "@tanstack/react-query";
import { useCallback, useEffect, useRef, useState } from "react";
import type { MessageInstance } from "antd/es/message/interface";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementStreamEventRecord,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import type { QueryClient } from "@tanstack/react-query";
import type { SancaiVisualAssetRefinementCapability } from "../services/sancai-entry-service";
import type { SancaiEntryRecord, SancaiVisualAssetRecord } from "../sancai-types";

type RefinementCapability = "translate" | "summary" | SancaiVisualAssetRefinementCapability;
type TextRefinementEntryDraft = Pick<
    SancaiEntryRecord,
    "originalText" | "summary" | "title" | "translationText"
>;

const isStreamRefinementCapability = (capability: string) => {
    return capability === "image_analysis" || capability === "image_gen";
};

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
    streamingRefinementTask: AiRefinementTaskRecord | null;
    streamEvents: AiRefinementStreamEventRecord[];
    isStreamingRefinementTask: boolean;
    streamErrorText: string | null;
    creatingRefinementCapability: RefinementCapability | null;
    retryingRefinementTaskId: number | null;
    invalidateSancaiContentGovernance: () => Promise<void>;
    invalidateSancaiContentCandidates: (visualAssetId: number | null) => Promise<void>;
    refreshSancaiEntryDetail: () => Promise<void>;
    createRefinementTask: (
        capability: RefinementCapability,
        imageAnalysisAsset?: SancaiVisualAssetRecord | null,
        entryDraft?: TextRefinementEntryDraft | null
    ) => void;
    retryRefinementTask: (task: AiRefinementTaskRecord) => void;
    closeStreamingRefinementTask: () => void;
    refreshAfterVisualAssetCandidateHandled: () => Promise<void>;
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
    const [retryingRefinementTaskId, setRetryingRefinementTaskId] = useState<number | null>(null);
    const [selectedVisualAsset, setSelectedVisualAsset] = useState<SancaiVisualAssetRecord | null>(
        null
    );
    const [streamingRefinementTask, setStreamingRefinementTask] =
        useState<AiRefinementTaskRecord | null>(null);
    const [streamEvents, setStreamEvents] = useState<AiRefinementStreamEventRecord[]>([]);
    const [isStreamingRefinementTask, setIsStreamingRefinementTask] = useState(false);
    const [streamErrorText, setStreamErrorText] = useState<string | null>(null);
    const streamAbortControllerRef = useRef<AbortController | null>(null);
    const selectedVisualAssetId =
        selectedVisualAsset?.visualAssetId ?? selectedVisualAsset?.id ?? null;

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
                queryKey: ["classics", "sancai", "entries", "detail", selectedEntryId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "sancai", "entries"]
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

    const closeStreamingRefinementTask = useCallback(() => {
        streamAbortControllerRef.current?.abort();
        streamAbortControllerRef.current = null;
        setStreamingRefinementTask(null);
        setStreamEvents([]);
        setStreamErrorText(null);
        setIsStreamingRefinementTask(false);
    }, []);

    const refreshStreamingTaskDetail = useCallback(
        async (taskId: number) => {
            const task = await aiRefinementTaskService.getTask({ taskId });
            setStreamingRefinementTask(task);
            await invalidateRefinementTasks();
            if (task.status === "SUCCEEDED" && task.candidateId) {
                await Promise.all([
                    invalidateSancaiContentCandidates(task.objectId ?? selectedVisualAssetId),
                    refreshSancaiVisualAssets()
                ]);
            }
            return task;
        },
        [
            invalidateRefinementTasks,
            invalidateSancaiContentCandidates,
            refreshSancaiVisualAssets,
            selectedVisualAssetId
        ]
    );

    const openStreamingRefinementTask = useCallback((task: AiRefinementTaskRecord) => {
        streamAbortControllerRef.current?.abort();
        setStreamingRefinementTask(task);
        setStreamEvents([]);
        setStreamErrorText(null);
        setIsStreamingRefinementTask(true);
    }, []);

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
        onSuccess: async (acceptedTask, command) => {
            await invalidateRefinementTasks();
            if (acceptedTask?.taskId && isStreamRefinementCapability(command.capability)) {
                const task = await aiRefinementTaskService.getTask({
                    taskId: acceptedTask.taskId
                });
                if (task.streamEnabled === true) {
                    openStreamingRefinementTask(task);
                }
            }
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
        if (!streamingRefinementTask?.taskId || streamingRefinementTask.streamEnabled !== true) {
            return undefined;
        }
        const taskId = streamingRefinementTask.taskId;
        const controller = new AbortController();
        streamAbortControllerRef.current = controller;
        void aiRefinementTaskService
            .requestTaskStream({
                taskId,
                signal: controller.signal,
                onEvent: (event) => {
                    setStreamEvents((currentEvents) => [...currentEvents, event]);
                    if (event.eventType === "completed" || event.eventType === "error") {
                        void refreshStreamingTaskDetail(taskId);
                    }
                }
            })
            .catch((error) => {
                if (controller.signal.aborted) {
                    return;
                }
                setStreamErrorText(error instanceof Error ? error.message : "AI 流式过程订阅失败");
            })
            .finally(() => {
                if (streamAbortControllerRef.current === controller) {
                    streamAbortControllerRef.current = null;
                }
                setIsStreamingRefinementTask(false);
                if (!controller.signal.aborted) {
                    void refreshStreamingTaskDetail(taskId);
                }
            });
        return () => {
            controller.abort();
        };
    }, [
        refreshStreamingTaskDetail,
        streamingRefinementTask?.streamEnabled,
        streamingRefinementTask?.taskId
    ]);

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

    const submitRefinementTask = useCallback(
        (
            capability: RefinementCapability,
            imageAnalysisAsset: SancaiVisualAssetRecord | null = null,
            sourceTaskId: number | null = null,
            entryDraft: TextRefinementEntryDraft | null = null
        ) => {
            if (!selectedEntry?.id) {
                return;
            }
            if (!currentUserId) {
                messageApi.warning("当前用户信息尚未加载完成");
                return;
            }
            const taskEntry = entryDraft ? { ...selectedEntry, ...entryDraft } : selectedEntry;
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
                messageApi.warning("当前视觉处理缺少原图，无法创建图片相关任务");
                return;
            }
            if (
                capability === "fusion" &&
                (imageAnalysisObjectId == null ||
                    sourceImageStorageObjectId == null ||
                    !imageAnalysisMarkdown?.trim())
            ) {
                messageApi.warning("当前视觉处理缺少图片理解结果，无法创建信息融合任务");
                return;
            }
            if (
                capability === "fusion" &&
                (!Number.isInteger(textWeight) || !Number.isInteger(imageWeight))
            ) {
                messageApi.warning("当前视觉处理权重未正确设置，无法创建信息融合任务");
                return;
            }
            if (
                capability === "visual" &&
                (!imageAnalysisMarkdown?.trim() ||
                    !Number.isInteger(textWeight) ||
                    !Number.isInteger(imageWeight))
            ) {
                messageApi.warning("当前视觉处理缺少图片理解结果或权重，无法创建视觉描述任务");
                return;
            }
            if (capability === "image_gen" && !visualDescription?.trim()) {
                messageApi.warning("当前视觉处理缺少视觉描述结果，无法创建生图任务");
                return;
            }
            if (
                capability !== "image_analysis" &&
                capability !== "visual" &&
                capability !== "fusion" &&
                capability !== "image_gen" &&
                !taskEntry.originalText?.trim()
            ) {
                messageApi.warning("当前条目缺少原文，无法创建 AI 精修任务");
                return;
            }
            if (sourceTaskId) {
                setRetryingRefinementTaskId(sourceTaskId);
            }
            createRefinementTaskMutation.mutate(
                {
                    capability,
                    scope: "classics",
                    contentType: "SANCAI_ENTRY",
                    contentId: selectedEntry.id,
                    objectId:
                        capability === "image_analysis" ||
                        capability === "visual" ||
                        capability === "fusion" ||
                        capability === "image_gen"
                            ? imageAnalysisObjectId
                            : null,
                    requestedBy: Number(currentUserId),
                    requestId: createEventId("sancai-task"),
                    traceId: createEventId("sancai-trace"),
                    promptMessagesJson: buildPromptMessagesJson(
                        capability,
                        taskEntry,
                        resolvedVisualAsset
                    ),
                    promptVariablesJson: JSON.stringify({
                        title: taskEntry.title
                    }),
                    inputPayloadJson: buildInputPayloadJson(
                        capability,
                        taskEntry,
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
                },
                {
                    onSettled: () => {
                        if (sourceTaskId) {
                            setRetryingRefinementTaskId(null);
                        }
                    }
                }
            );
        },
        [
            createRefinementTaskMutation,
            currentUserId,
            messageApi,
            selectedEntry,
            selectedVisualAsset
        ]
    );

    const createRefinementTask = (
        capability: RefinementCapability,
        imageAnalysisAsset: SancaiVisualAssetRecord | null = null,
        entryDraft: TextRefinementEntryDraft | null = null
    ) => {
        submitRefinementTask(capability, imageAnalysisAsset, null, entryDraft);
    };

    const retryRefinementTask = (task: AiRefinementTaskRecord) => {
        if (!selectedEntry?.id) {
            return;
        }
        const capability = task.capability as RefinementCapability;
        if (
            capability !== "translate" &&
            capability !== "summary" &&
            capability !== "image_analysis" &&
            capability !== "fusion" &&
            capability !== "visual" &&
            capability !== "image_gen"
        ) {
            messageApi.warning("当前任务能力暂不支持页面内重试");
            return;
        }
        const visualAssets =
            queryClient.getQueryData<SancaiVisualAssetRecord[]>([
                "classics",
                "sancai",
                "entries",
                "visual-assets",
                selectedEntry.id
            ]) || [];
        const taskAsset =
            visualAssets.find(
                (asset) => (asset.visualAssetId ?? asset.id ?? null) === (task.objectId ?? null)
            ) ||
            (selectedVisualAsset &&
            (selectedVisualAsset.visualAssetId ?? selectedVisualAsset.id ?? null) ===
                (task.objectId ?? null)
                ? selectedVisualAsset
                : null);
        submitRefinementTask(capability, taskAsset, task.taskId);
    };

    const refreshAfterVisualAssetCandidateHandled = useCallback(async () => {
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
        streamingRefinementTask,
        streamEvents,
        isStreamingRefinementTask,
        streamErrorText,
        creatingRefinementCapability,
        retryingRefinementTaskId,
        invalidateSancaiContentGovernance,
        invalidateSancaiContentCandidates,
        refreshSancaiEntryDetail,
        createRefinementTask,
        retryRefinementTask,
        closeStreamingRefinementTask,
        refreshAfterVisualAssetCandidateHandled,
        resetHandledSucceededTaskIds
    };
};
