import { TranslationOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Input } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton, KuzhambuSpace } from "@/components";

import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";
import { SancaiEntryTranslationModal } from "./sancai-entry-translation-modal";
import "./sancai-entry-translation-text-field.css";

const AI_TEXT_CANDIDATE_POLL_INTERVAL_MS = 3000;
const TRANSLATION_CANDIDATE_CAPABILITY = "classics_translate";

const getCandidateStableId = (candidate: AiCandidateRecord) => {
    return candidate.candidateIdText || String(candidate.candidateId);
};

const isUsableTranslationCandidate = (candidate?: AiCandidateRecord | null) => {
    return (
        candidate?.capability &&
        aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) === "translate" &&
        candidate.status === "PENDING" &&
        typeof candidate.resultPayload === "string" &&
        candidate.resultPayload.trim().length > 0
    );
};

const selectLatestTranslationCandidate = (candidates: AiCandidateRecord[] | undefined) => {
    return [...(candidates || [])].filter(isUsableTranslationCandidate).sort((left, right) => {
        return aiRefinementTaskService.sortNewestByRequestedAtThenId({
            left: { id: getCandidateStableId(left), requestedAt: left.requestedAt },
            right: { id: getCandidateStableId(right), requestedAt: right.requestedAt }
        });
    })[0];
};

interface SancaiEntryTranslationTextFieldProps {
    entryId?: string;
    getFormValues: () => SancaiEntryFormValues;
    isCreatingTranslationTask?: boolean;
    mode: "create" | "edit";
    translationTasks?: AiRefinementTaskRecord[];
    value?: string;
    onChange?: (value: string) => void;
    onRequestTranslationTask?: (draft: SancaiEntryFormValues) => void;
}

export const SancaiEntryTranslationTextField = ({
    entryId,
    getFormValues,
    isCreatingTranslationTask = false,
    mode,
    translationTasks = [],
    value = "",
    onChange,
    onRequestTranslationTask
}: SancaiEntryTranslationTextFieldProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [translationModalOpen, setTranslationModalOpen] = useState(false);
    const [translationDraft, setTranslationDraft] = useState("");
    const [loadedTranslationCandidateId, setLoadedTranslationCandidateId] = useState<string | null>(
        null
    );
    const [loadedTranslationCandidateSnapshot, setLoadedTranslationCandidateSnapshot] =
        useState<AiCandidateRecord | null>(null);
    const syncTranslationTask = useCallback(
        (task: AiRefinementTaskRecord | null) => {
            if (!task || !entryId) {
                return;
            }
            const normalizedTask = { ...task, capability: "translate" };
            queryClient.setQueryData<{
                items?: AiRefinementTaskRecord[];
                [key: string]: unknown;
            }>(["classics", "sancai", "refinement", "tasks", entryId], (currentPage) => {
                if (!currentPage?.items) {
                    return currentPage;
                }
                const taskExists = currentPage.items.some(
                    (item) => item.taskId === normalizedTask.taskId
                );
                return {
                    ...currentPage,
                    items: taskExists
                        ? currentPage.items.map((item) =>
                              item.taskId === normalizedTask.taskId
                                  ? { ...item, ...normalizedTask }
                                  : item
                          )
                        : [normalizedTask, ...currentPage.items]
                };
            });
        },
        [entryId, queryClient]
    );
    const translationCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "translate", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: TRANSLATION_CANDIDATE_CAPABILITY,
                status: "PENDING"
            }),
        enabled: translationModalOpen && Boolean(entryId),
        retry: false,
        refetchInterval: () => {
            return isCreatingTranslationTask ? AI_TEXT_CANDIDATE_POLL_INTERVAL_MS : false;
        }
    });
    const applyTranslationCandidateMutation = useMutation({
        mutationFn: aiCandidateService.apply,
        onSuccess: async (_, command) => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "translate", "modal"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "entries"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "entries", "versions", entryId]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["classics", "sancai", "refinement", "tasks", entryId]
                })
            ]);
            onChange?.(command.resultPayload);
            setTranslationModalOpen(false);
            messageApi.success("译文已写入基础信息");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 候选应用失败");
        }
    });
    const latestTranslationCandidate = useMemo(
        () => selectLatestTranslationCandidate(translationCandidatesQuery.data),
        [translationCandidatesQuery.data]
    );
    const loadedTranslationCandidate = useMemo(() => {
        if (!loadedTranslationCandidateId) {
            return null;
        }
        const queryCandidate =
            (translationCandidatesQuery.data || []).find(
                (candidate) =>
                    getCandidateStableId(candidate) === loadedTranslationCandidateId &&
                    aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                        "translate"
            ) ?? null;
        if (queryCandidate) {
            return queryCandidate;
        }
        return loadedTranslationCandidateSnapshot &&
            getCandidateStableId(loadedTranslationCandidateSnapshot) ===
                loadedTranslationCandidateId &&
            isUsableTranslationCandidate(loadedTranslationCandidateSnapshot)
            ? loadedTranslationCandidateSnapshot
            : null;
    }, [
        loadedTranslationCandidateId,
        loadedTranslationCandidateSnapshot,
        translationCandidatesQuery.data
    ]);
    const isTranslationApplyDisabled = !translationDraft.trim() || isCreatingTranslationTask;
    const refetchTranslationCandidates = translationCandidatesQuery.refetch;
    const loadTranslationCandidate = useCallback(
        async (task: AiRefinementTaskRecord | null) => {
            const candidateId =
                task?.candidateIdText || (task?.candidateId ? String(task.candidateId) : null);
            if (candidateId) {
                const candidate = await aiCandidateService.get({ candidateId });
                return isUsableTranslationCandidate(candidate) ? candidate : null;
            }
            const candidates = await aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: TRANSLATION_CANDIDATE_CAPABILITY,
                status: "PENDING"
            });
            return selectLatestTranslationCandidate(candidates) ?? null;
        },
        [entryId]
    );
    const updateTranslationDraftFromCandidate = useCallback(
        (candidate: AiCandidateRecord | null) => {
            if (!candidate || !isUsableTranslationCandidate(candidate)) {
                return;
            }
            const candidateId = getCandidateStableId(candidate);
            if (candidateId === loadedTranslationCandidateId) {
                return;
            }
            setLoadedTranslationCandidateId(candidateId);
            setLoadedTranslationCandidateSnapshot(candidate);
            setTranslationDraft(candidate.resultPayload?.trim() || "");
        },
        [loadedTranslationCandidateId]
    );

    const handleTranslationTaskChange = useCallback(
        (task: AiRefinementTaskRecord | null) => {
            syncTranslationTask(task);
            if (task?.status === "SUCCEEDED" || task?.status === "PARTIAL") {
                void refetchTranslationCandidates();
            }
        },
        [refetchTranslationCandidates, syncTranslationTask]
    );

    useEffect(() => {
        if (!translationModalOpen || !latestTranslationCandidate) {
            return;
        }
        if (getCandidateStableId(latestTranslationCandidate) === loadedTranslationCandidateId) {
            return;
        }
        const timer = window.setTimeout(() => {
            setLoadedTranslationCandidateId(getCandidateStableId(latestTranslationCandidate));
            setLoadedTranslationCandidateSnapshot(latestTranslationCandidate);
            setTranslationDraft(latestTranslationCandidate.resultPayload?.trim() || "");
        }, 0);
        return () => window.clearTimeout(timer);
    }, [latestTranslationCandidate, loadedTranslationCandidateId, translationModalOpen]);

    const openTranslationModal = () => {
        setTranslationDraft(value || "");
        setLoadedTranslationCandidateId(null);
        setLoadedTranslationCandidateSnapshot(null);
        setTranslationModalOpen(true);
    };
    const closeTranslationModal = () => {
        setTranslationModalOpen(false);
    };
    const applyTranslationDraft = () => {
        if (!entryId) {
            return;
        }
        const resultPayload = translationDraft;
        if (loadedTranslationCandidate) {
            applyTranslationCandidateMutation.mutate({
                candidateId: getCandidateStableId(loadedTranslationCandidate),
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: "translate",
                objectId: loadedTranslationCandidate.objectId,
                resultFormat: loadedTranslationCandidate.resultFormat?.trim() || "TEXT",
                resultPayload,
                changeSummary: "AI 应用：译文"
            });
            return;
        }
        onChange?.(resultPayload);
        setTranslationModalOpen(false);
        messageApi.success("译文已写入基础信息");
    };

    const formValues = getFormValues();

    return (
        <div className="sancai-entry-translation-text-field">
            <Input.TextArea
                aria-label="三才图会译文"
                value={value}
                autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                onChange={(event) => onChange?.(event.target.value)}
            />
            {mode === "edit" ? (
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-ai-button"
                        className="sancai-entry-translation-text-field-button"
                        icon={<TranslationOutlined />}
                        onClick={openTranslationModal}
                    >
                        AI翻译
                    </KuzhambuButton>
                </KuzhambuSpace>
            ) : null}
            <SancaiEntryTranslationModal
                aiTextDraft={translationDraft}
                entryId={entryId}
                form={formValues}
                isAiTextApplyDisabled={isTranslationApplyDisabled}
                isAiTextCandidateFetching={translationCandidatesQuery.isFetching}
                isAiTextCandidateLoadError={translationCandidatesQuery.isError}
                isApplyingAiText={applyTranslationCandidateMutation.isPending}
                isCreatingAiTextTask={isCreatingTranslationTask}
                open={translationModalOpen}
                onApply={applyTranslationDraft}
                onCancel={closeTranslationModal}
                onFetchResult={loadTranslationCandidate}
                onFetchTask={(taskId) => aiRefinementTaskService.getTask({ taskId })}
                onResultChange={updateTranslationDraftFromCandidate}
                onRequestTranslationTask={onRequestTranslationTask}
                onTaskChange={handleTranslationTaskChange}
                onTextDraftChange={setTranslationDraft}
                translationTasks={translationTasks}
            />
        </div>
    );
};
