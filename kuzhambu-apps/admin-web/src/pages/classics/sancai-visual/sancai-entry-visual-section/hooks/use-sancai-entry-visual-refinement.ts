import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementStreamEventRecord,
    AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiVisualAssetRefinementCapability } from "@/pages/classics/sancai-visual/sancai-visual-service";
import type {
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";

const TASK_POLL_INTERVAL_MS = 3000;

const isStreamRefinementCapability = (capability: string) => {
    const normalizedCapability = aiRefinementTaskService.getNormalizedTaskCapability(capability);
    return normalizedCapability === "image_analysis" || normalizedCapability === "image_gen";
};

const isActiveRefinementTask = (task: AiRefinementTaskRecord) => {
    return task.status === "PENDING" || task.status === "RUNNING";
};

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const buildEntryContextText = (entry: SancaiEntryRecord) =>
    [
        entry.originalText ? `原文：${entry.originalText}` : null,
        entry.translationText ? `译文：${entry.translationText}` : null,
        entry.summary ? `摘要：${entry.summary}` : null
    ]
        .filter(Boolean)
        .join("\n\n");

export const buildInputPayloadJson = (
    capability: SancaiVisualAssetRefinementCapability,
    capabilityCode: string,
    entry: SancaiEntryRecord,
    objectId: string | null,
    asset: SancaiVisualAssetRecord
) => {
    const payload = {
        capability: capabilityCode,
        contentId: entry.id,
        contentType: "SANCAI_ENTRY",
        objectId,
        originalText: entry.originalText ?? null,
        sourceText: entry.originalText ?? null,
        summary: entry.summary ?? null,
        title: entry.title ?? null,
        translationText: entry.translationText ?? null
    };
    if (capability === "image_analysis") {
        return JSON.stringify({
            ...payload,
            contextText: buildEntryContextText(entry) || null,
            imageDescription: null,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId ?? null
        });
    }
    if (capability === "fusion") {
        return JSON.stringify({
            ...payload,
            imageAnalysis: asset.imageAnalysisMarkdown ?? null,
            imageAnalysisMarkdown: asset.imageAnalysisMarkdown ?? null,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId ?? null,
            textWeight: asset.textWeight,
            imageWeight: asset.imageWeight
        });
    }
    if (capability === "visual") {
        return JSON.stringify({
            ...payload,
            fusionDescription: asset.fusionDescription ?? null,
            fusionText: asset.fusionDescription ?? null,
            imageAnalysis: asset.imageAnalysisMarkdown ?? null,
            imageAnalysisMarkdown: asset.imageAnalysisMarkdown ?? null,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId ?? null,
            styleGuide: asset.generationParamsJson ?? null,
            textWeight: asset.textWeight,
            imageWeight: asset.imageWeight
        });
    }
    if (capability === "image_gen") {
        return JSON.stringify({
            ...payload,
            sourceImageStorageObjectId: asset.sourceImageStorageObjectId ?? null,
            fusionDescription: asset.fusionDescription ?? null,
            sourceText: asset.visualDescription ?? entry.originalText ?? null,
            styleGuide: asset.generationParamsJson ?? null,
            visualDescription: asset.visualDescription ?? null,
            generationParamsJson: asset.generationParamsJson ?? null
        });
    }
    return JSON.stringify(payload);
};

interface UseSancaiEntryVisualRefinementParams {
    entry: SancaiEntryRecord;
    selectedVisualAsset: SancaiVisualAssetRecord | null | undefined;
    selectedVisualAssetId: string | null;
    visualAssetFormValue: SancaiVisualAssetRecord | null;
    onRefinementChanged: () => Promise<void> | void;
}

export const useSancaiEntryVisualRefinement = ({
    entry,
    selectedVisualAsset,
    selectedVisualAssetId,
    visualAssetFormValue,
    onRefinementChanged
}: UseSancaiEntryVisualRefinementParams) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [creatingVisualAssetCapability, setCreatingVisualAssetCapability] =
        useState<SancaiVisualAssetRefinementCapability | null>(null);
    const [retryingRefinementTaskId, setRetryingRefinementTaskId] = useState<string | null>(null);
    const [streamingRefinementTask, setStreamingRefinementTask] =
        useState<AiRefinementTaskRecord | null>(null);
    const [streamEvents, setStreamEvents] = useState<AiRefinementStreamEventRecord[]>([]);
    const [isStreamingRefinementTask, setIsStreamingRefinementTask] = useState(false);
    const [streamErrorText, setStreamErrorText] = useState<string | null>(null);
    const streamAbortControllerRef = useRef<AbortController | null>(null);
    const dismissedStreamingTaskIdsRef = useRef<Set<string>>(new Set());
    const streamingRefinementTaskId = streamingRefinementTask?.taskId ?? null;

    const refinementTasksQuery = useQuery({
        queryKey: ["classics", "sancai", "refinement", "tasks", entry.id],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                contentId: entry.id,
                contentType: "SANCAI_ENTRY",
                pageNo: 1,
                pageSize: 20
            }),
        enabled: Boolean(entry.id),
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some(isActiveRefinementTask) ? TASK_POLL_INTERVAL_MS : false;
        },
        retry: false
    });
    const refinementTasks = useMemo(
        () => refinementTasksQuery.data?.items || [],
        [refinementTasksQuery.data?.items]
    );

    const invalidateRefinementTasks = useCallback(async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "refinement", "tasks", entry.id]
        });
    }, [entry.id, queryClient]);

    const refreshVisualAssets = useCallback(async () => {
        await queryClient.invalidateQueries({
            queryKey: ["classics", "sancai", "entries", "visual-assets", entry.id]
        });
    }, [entry.id, queryClient]);

    const invalidateVisualAssetCandidates = useCallback(
        (visualAssetId: string | null) =>
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "SANCAI_ENTRY", entry.id, visualAssetId]
            }),
        [entry.id, queryClient]
    );

    const closeStreamingRefinementTask = useCallback(() => {
        if (streamingRefinementTaskId) {
            dismissedStreamingTaskIdsRef.current.add(streamingRefinementTaskId);
        }
        streamAbortControllerRef.current?.abort();
        streamAbortControllerRef.current = null;
        setStreamingRefinementTask(null);
        setStreamEvents([]);
        setStreamErrorText(null);
        setIsStreamingRefinementTask(false);
    }, [streamingRefinementTaskId]);

    const refreshStreamingTaskDetail = useCallback(
        async (taskId: string) => {
            const task = await aiRefinementTaskService.getTask({ taskId });
            setStreamingRefinementTask(task);
            await invalidateRefinementTasks();
            if (task.status === "SUCCEEDED" && task.candidateId) {
                await Promise.all([
                    invalidateVisualAssetCandidates(task.objectId ?? selectedVisualAssetId),
                    refreshVisualAssets()
                ]);
            }
            return task;
        },
        [
            invalidateRefinementTasks,
            invalidateVisualAssetCandidates,
            refreshVisualAssets,
            selectedVisualAssetId
        ]
    );

    const openStreamingRefinementTask = useCallback((task: AiRefinementTaskRecord) => {
        streamAbortControllerRef.current?.abort();
        if (task.taskId) {
            dismissedStreamingTaskIdsRef.current.delete(task.taskId);
        }
        setStreamingRefinementTask(task);
        setStreamEvents([]);
        setStreamErrorText(null);
        setIsStreamingRefinementTask(true);
    }, []);

    useEffect(() => {
        const resumableTask = refinementTasks.find(
            (task) =>
                task.taskId &&
                task.streamEnabled === true &&
                isActiveRefinementTask(task) &&
                isStreamRefinementCapability(task.capability) &&
                !dismissedStreamingTaskIdsRef.current.has(task.taskId)
        );
        if (!resumableTask || streamingRefinementTaskId === resumableTask.taskId) {
            return;
        }
        openStreamingRefinementTask(resumableTask);
    }, [openStreamingRefinementTask, refinementTasks, streamingRefinementTaskId]);

    const createRefinementTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onMutate: (command) => {
            setCreatingVisualAssetCapability(
                aiRefinementTaskService.getNormalizedTaskCapability(
                    command.capability
                ) as SancaiVisualAssetRefinementCapability
            );
        },
        onSuccess: async (acceptedTask, command) => {
            const normalizedCapability = aiRefinementTaskService.getNormalizedTaskCapability(
                command.capability
            );
            await invalidateRefinementTasks();
            if (acceptedTask?.taskId && isStreamRefinementCapability(command.capability)) {
                const task = await aiRefinementTaskService.getTask({
                    taskId: acceptedTask.taskId
                });
                if (task.streamEnabled === true) {
                    openStreamingRefinementTask(task);
                }
            }
            if (normalizedCapability === "visual") {
                messageApi.success("视觉描述任务已创建");
            } else if (normalizedCapability === "fusion") {
                messageApi.success("信息融合任务已创建");
            } else if (normalizedCapability === "image_gen") {
                messageApi.success("生图任务已创建");
            } else {
                messageApi.success("图片理解任务已创建");
            }
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 精修任务创建失败");
        },
        onSettled: () => {
            setCreatingVisualAssetCapability(null);
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

    const submitRefinementTask = useCallback(
        (
            capability: SancaiVisualAssetRefinementCapability,
            asset: SancaiVisualAssetRecord | null,
            sourceTaskId: string | null = null
        ) => {
            const objectId = asset?.visualAssetId ?? asset?.id ?? null;
            const sourceImageStorageObjectId = asset?.sourceImageStorageObjectId;
            const imageAnalysisMarkdown = asset?.imageAnalysisMarkdown;
            const fusionDescription = asset?.fusionDescription;
            const textWeight = asset?.textWeight;
            const imageWeight = asset?.imageWeight;
            const visualDescription = asset?.visualDescription;
            if (!asset || objectId == null || sourceImageStorageObjectId == null) {
                messageApi.warning("当前视觉处理缺少原图，无法创建图片相关任务");
                return;
            }
            if (
                capability === "fusion" &&
                (!imageAnalysisMarkdown?.trim() ||
                    !Number.isInteger(textWeight) ||
                    !Number.isInteger(imageWeight))
            ) {
                messageApi.warning("当前视觉处理缺少图片理解结果或权重，无法创建信息融合任务");
                return;
            }
            if (
                capability === "visual" &&
                (!fusionDescription?.trim() ||
                    !imageAnalysisMarkdown?.trim() ||
                    !Number.isInteger(textWeight) ||
                    !Number.isInteger(imageWeight))
            ) {
                messageApi.warning(
                    "当前视觉处理缺少图文融合结果、图片理解结果或权重，无法创建视觉描述任务"
                );
                return;
            }
            if (capability === "image_gen" && !visualDescription?.trim()) {
                messageApi.warning("当前视觉处理缺少视觉描述结果，无法创建生图任务");
                return;
            }
            if (sourceTaskId) {
                setRetryingRefinementTaskId(sourceTaskId);
            }
            const capabilityCode = aiRefinementTaskService.getBusinessCapabilityCode(capability);
            createRefinementTaskMutation.mutate(
                {
                    capability: capabilityCode,
                    scope: "classics",
                    contentType: "SANCAI_ENTRY",
                    contentId: entry.id,
                    objectId,
                    requestId: createEventId("sancai-task"),
                    traceId: createEventId("sancai-trace"),
                    inputPayloadJson: buildInputPayloadJson(
                        capability,
                        capabilityCode,
                        entry,
                        objectId,
                        asset
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
        [createRefinementTaskMutation, entry, messageApi]
    );

    const createVisualAssetTask = (capability: SancaiVisualAssetRefinementCapability) => {
        submitRefinementTask(capability, visualAssetFormValue);
    };

    const retryRefinementTask = (task: AiRefinementTaskRecord) => {
        const capability = aiRefinementTaskService.getNormalizedTaskCapability(task.capability);
        if (
            capability !== "image_analysis" &&
            capability !== "fusion" &&
            capability !== "visual" &&
            capability !== "image_gen"
        ) {
            messageApi.warning("当前任务能力暂不支持视觉区重试");
            return;
        }
        const visualAssets =
            queryClient.getQueryData<SancaiVisualAssetRecord[]>([
                "classics",
                "sancai",
                "entries",
                "visual-assets",
                entry.id
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
        submitRefinementTask(
            capability as SancaiVisualAssetRefinementCapability,
            taskAsset,
            task.taskId
        );
    };

    const refreshAfterVisualAssetCandidateHandled = useCallback(async () => {
        await Promise.all([
            onRefinementChanged(),
            refreshVisualAssets(),
            invalidateVisualAssetCandidates(selectedVisualAssetId)
        ]);
    }, [
        invalidateVisualAssetCandidates,
        onRefinementChanged,
        refreshVisualAssets,
        selectedVisualAssetId
    ]);

    const refreshVisualAssetCandidates = (visualAssetId?: string | null) => {
        void invalidateVisualAssetCandidates(visualAssetId ?? selectedVisualAssetId);
    };

    return {
        closeStreamingRefinementTask,
        createVisualAssetTask,
        creatingVisualAssetCapability,
        isStreamingRefinementTask,
        refinementTasks,
        refreshAfterVisualAssetCandidateHandled,
        refreshVisualAssetCandidates,
        retryingRefinementTaskId,
        retryRefinementTask,
        streamErrorText,
        streamEvents,
        streamingRefinementTask
    };
};
