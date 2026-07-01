import { useMutation } from "@tanstack/react-query";
import { useCallback, useEffect, useRef, useState } from "react";
import type { MessageInstance } from "antd/es/message/interface";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { QueryClient } from "@tanstack/react-query";
import type { SancaiEntryRecord, SancaiVisualAssetRecord } from "../sancai-types";

type RefinementCapability = "translate" | "summary" | "image_analysis";

const DEFAULT_REFINEMENT_MODEL_ID = 1;
const DEFAULT_REFINEMENT_MODEL_NAME = "gpt-5.5";
const DEFAULT_REFINEMENT_SERVICE_ROLE = "PRIMARY";

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const buildPromptMessagesJson = (capability: RefinementCapability, entry: SancaiEntryRecord) => {
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
    objectId?: number | null
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
    creatingRefinementCapability: RefinementCapability | null;
    invalidateSancaiContentGovernance: () => Promise<void>;
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
                queryKey: ["classics", "content", "tags", "SANCAI_ENTRY"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["classics", "content", "qa-pairs", "SANCAI_ENTRY"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["ai", "candidates", "SANCAI_ENTRY", selectedEntryId]
            })
        ]);
    }, [queryClient, selectedEntryId]);

    const createRefinementTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onMutate: (command) => {
            if (
                command.capability === "translate" ||
                command.capability === "summary" ||
                command.capability === "image_analysis"
            ) {
                setCreatingRefinementCapability(command.capability);
            }
        },
        onSuccess: async (_, command) => {
            await invalidateRefinementTasks();
            const createSuccessMessages: Record<RefinementCapability, string> = {
                translate: "译文任务已创建",
                summary: "摘要任务已创建",
                image_analysis: "图片理解任务已创建"
            };
            messageApi.success(createSuccessMessages[command.capability]);
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
        const imageAnalysisObjectId = imageAnalysisAsset
            ? (imageAnalysisAsset.visualAssetId ?? imageAnalysisAsset.id ?? null)
            : null;
        if (
            capability === "image_analysis" &&
            (imageAnalysisObjectId == null || imageAnalysisAsset.sourceImageStorageObjectId == null)
        ) {
            messageApi.warning("当前视觉资产缺少原图，无法创建图片理解任务");
            return;
        }
        if (capability !== "image_analysis" && !selectedEntry.originalText?.trim()) {
            messageApi.warning("当前条目缺少原文，无法创建 AI 精修任务");
            return;
        }
        createRefinementTaskMutation.mutate({
            capability,
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: selectedEntry.id,
            objectId: capability === "image_analysis" ? imageAnalysisObjectId : null,
            requestedBy: Number(currentUserId),
            serviceRole: DEFAULT_REFINEMENT_SERVICE_ROLE,
            modelId: DEFAULT_REFINEMENT_MODEL_ID,
            modelName: DEFAULT_REFINEMENT_MODEL_NAME,
            requestId: createEventId("sancai-task"),
            traceId: createEventId("sancai-trace"),
            promptMessagesJson: buildPromptMessagesJson(capability, selectedEntry),
            promptVariablesJson: JSON.stringify({
                title: selectedEntry.title
            }),
            inputPayloadJson: buildInputPayloadJson(
                capability,
                selectedEntry,
                capability === "image_analysis" ? imageAnalysisObjectId : null
            ),
            locale: "zh-CN"
        });
    };

    const refreshAfterImageAnalysisApplied = useCallback(async () => {
        await Promise.all([
            refreshSancaiEntryDetail(),
            refreshSancaiVisualAssets(),
            queryClient.invalidateQueries({
                queryKey: [
                    "ai",
                    "candidates",
                    "SANCAI_ENTRY",
                    selectedEntryId,
                    selectedVisualAssetId
                ]
            })
        ]);
    }, [
        queryClient,
        refreshSancaiEntryDetail,
        refreshSancaiVisualAssets,
        selectedEntryId,
        selectedVisualAssetId
    ]);

    return {
        setSelectedVisualAsset,
        selectedVisualAssetId,
        creatingRefinementCapability,
        invalidateSancaiContentGovernance,
        refreshSancaiEntryDetail,
        createRefinementTask,
        refreshAfterImageAnalysisApplied,
        resetHandledSucceededTaskIds
    };
};
