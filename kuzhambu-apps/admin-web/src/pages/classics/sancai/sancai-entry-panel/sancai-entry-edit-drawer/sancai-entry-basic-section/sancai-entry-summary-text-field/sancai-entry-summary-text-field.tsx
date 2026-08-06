import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";

import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import {
    AI_BUSINESS_CAPABILITY,
    type AiRefinementTaskRecord
} from "@/pages/classics/common/ai-refinement-task-types";
import { ClassicsSummaryFormControl } from "@/pages/classics/common/classics-summary-form-control";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/sancai-entry-panel/sancai-entry-edit-drawer/sancai-entry-edit-drawer-form-values";
import { SancaiEntrySummaryModal } from "./sancai-entry-summary-modal";

const AI_TEXT_CANDIDATE_POLL_INTERVAL_MS = 3000;
const SUMMARY_CANDIDATE_CAPABILITY = AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY;
const SUMMARY_TASK_POLL_INTERVAL_MS = 3000;

type SummaryTaskPage = {
    items?: AiRefinementTaskRecord[];
    pageNo?: number;
    pageSize?: number;
    total?: number;
};

const getCandidateStableId = (candidate: AiCandidateRecord) => {
    return candidate.candidateIdText || String(candidate.candidateId);
};

const isSameTaskRecord = (
    left: AiRefinementTaskRecord | undefined,
    right: AiRefinementTaskRecord
) => {
    if (!left) {
        return false;
    }
    const leftRecord = left as unknown as Record<string, unknown>;
    const rightRecord = right as unknown as Record<string, unknown>;
    const keys = new Set([...Object.keys(leftRecord), ...Object.keys(rightRecord)]);
    return [...keys].every((key) => leftRecord[key] === rightRecord[key]);
};

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const buildSummaryInputPayloadJson = (entryId: string, formValues: SancaiEntryFormValues) => {
    const document = [formValues.originalText, formValues.translationText, formValues.summary]
        .filter((value): value is string => Boolean(value?.trim()))
        .join("\n\n");
    return JSON.stringify({
        capability: AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY,
        contentId: entryId,
        contentType: "SANCAI_ENTRY",
        document,
        bodyText: formValues.originalText,
        existingSummary: formValues.summary,
        objectId: null,
        originalText: formValues.originalText,
        sourceText: formValues.originalText,
        summary: formValues.summary,
        title: formValues.title,
        translationText: formValues.translationText
    });
};

const isUsableSummaryCandidate = (candidate?: AiCandidateRecord | null) => {
    return (
        candidate?.capability &&
        aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) === "summary" &&
        candidate.status === "PENDING" &&
        typeof candidate.resultPayload === "string" &&
        candidate.resultPayload.trim().length > 0
    );
};

const selectLatestSummaryCandidate = (candidates: AiCandidateRecord[] | undefined) => {
    return [...(candidates || [])].filter(isUsableSummaryCandidate).sort((left, right) => {
        return aiRefinementTaskService.sortNewestByRequestedAtThenId({
            left: { id: getCandidateStableId(left), requestedAt: left.requestedAt },
            right: { id: getCandidateStableId(right), requestedAt: right.requestedAt }
        });
    })[0];
};

interface SancaiEntrySummaryTextFieldProps {
    entryId?: string;
    getFormValues: () => SancaiEntryFormValues;
    mode: "create" | "edit";
    value?: string;
    onChange?: (value: string) => void;
}

export const SancaiEntrySummaryTextField = ({
    entryId,
    getFormValues,
    mode,
    value = "",
    onChange
}: SancaiEntrySummaryTextFieldProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [summaryModalOpen, setSummaryModalOpen] = useState(false);
    const [summaryDraft, setSummaryDraft] = useState("");
    const [loadedSummaryCandidateId, setLoadedSummaryCandidateId] = useState<string | null>(null);
    const [loadedSummaryCandidateSnapshot, setLoadedSummaryCandidateSnapshot] =
        useState<AiCandidateRecord | null>(null);
    const summaryTasksQuery = useQuery({
        queryKey: ["classics", "sancai", "refinement", "tasks", entryId],
        queryFn: () =>
            aiRefinementTaskService.pageTasks({
                contentType: "SANCAI_ENTRY",
                contentId: entryId ?? "",
                pageNo: 1,
                pageSize: 20
            }),
        enabled: mode === "edit" && Boolean(entryId),
        retry: false,
        refetchInterval: (query) => {
            const tasks = query.state.data?.items || [];
            return tasks.some((task) => task.status === "PENDING" || task.status === "RUNNING")
                ? SUMMARY_TASK_POLL_INTERVAL_MS
                : false;
        }
    });
    const summaryTasks = useMemo(
        () =>
            (summaryTasksQuery.data?.items || []).filter(
                (task) =>
                    aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                    "summary"
            ),
        [summaryTasksQuery.data?.items]
    );
    const syncSummaryTask = useCallback(
        (task: AiRefinementTaskRecord | null) => {
            if (!task || !entryId) {
                return;
            }
            const normalizedTask = { ...task, capability: "summary" };
            queryClient.setQueryData<SummaryTaskPage>(
                ["classics", "sancai", "refinement", "tasks", entryId],
                (currentPage) => {
                    const items = currentPage?.items || [];
                    const existingTask = items.find(
                        (item) => item.taskId === normalizedTask.taskId
                    );
                    if (isSameTaskRecord(existingTask, normalizedTask)) {
                        return currentPage;
                    }
                    return {
                        pageNo: currentPage?.pageNo ?? 1,
                        pageSize: currentPage?.pageSize ?? Math.max(items.length + 1, 20),
                        total: existingTask
                            ? currentPage?.total
                            : (currentPage?.total ?? items.length) + 1,
                        items: existingTask
                            ? items.map((item) =>
                                  item.taskId === normalizedTask.taskId
                                      ? { ...item, ...normalizedTask }
                                      : item
                              )
                            : [normalizedTask, ...items]
                    };
                }
            );
        },
        [entryId, queryClient]
    );
    const createSummaryTaskMutation = useMutation({
        mutationFn: aiRefinementTaskService.createTask,
        onSuccess: async (acceptedTask) => {
            syncSummaryTask(acceptedTask);
            await summaryTasksQuery.refetch();
            messageApi.success("摘要任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 精修任务创建失败");
        }
    });
    const summaryCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "summary", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: SUMMARY_CANDIDATE_CAPABILITY,
                status: "PENDING"
            }),
        enabled: summaryModalOpen && Boolean(entryId),
        retry: false,
        refetchInterval: () => {
            return createSummaryTaskMutation.isPending ? AI_TEXT_CANDIDATE_POLL_INTERVAL_MS : false;
        }
    });
    const applySummaryCandidateMutation = useMutation({
        mutationFn: aiCandidateService.apply,
        onSuccess: async (_, command) => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "summary", "modal"]
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
            setSummaryModalOpen(false);
            messageApi.success("摘要已写入基础信息");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "AI 候选应用失败");
        }
    });
    const latestSummaryCandidate = useMemo(
        () => selectLatestSummaryCandidate(summaryCandidatesQuery.data),
        [summaryCandidatesQuery.data]
    );
    const loadedSummaryCandidate = useMemo(() => {
        if (!loadedSummaryCandidateId) {
            return null;
        }
        const queryCandidate =
            (summaryCandidatesQuery.data || []).find(
                (candidate) =>
                    getCandidateStableId(candidate) === loadedSummaryCandidateId &&
                    aiRefinementTaskService.getNormalizedTaskCapability(candidate.capability) ===
                        "summary"
            ) ?? null;
        if (queryCandidate) {
            return queryCandidate;
        }
        return loadedSummaryCandidateSnapshot &&
            getCandidateStableId(loadedSummaryCandidateSnapshot) === loadedSummaryCandidateId &&
            isUsableSummaryCandidate(loadedSummaryCandidateSnapshot)
            ? loadedSummaryCandidateSnapshot
            : null;
    }, [loadedSummaryCandidateId, loadedSummaryCandidateSnapshot, summaryCandidatesQuery.data]);
    const isSummaryApplyDisabled = !summaryDraft.trim() || createSummaryTaskMutation.isPending;
    const refetchSummaryCandidates = summaryCandidatesQuery.refetch;
    const loadSummaryCandidate = useCallback(
        async (task: AiRefinementTaskRecord | null) => {
            const candidateId =
                task?.candidateIdText || (task?.candidateId ? String(task.candidateId) : null);
            if (candidateId) {
                const candidate = await aiCandidateService.get({ candidateId });
                return isUsableSummaryCandidate(candidate) ? candidate : null;
            }
            const candidates = await aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: SUMMARY_CANDIDATE_CAPABILITY,
                status: "PENDING"
            });
            return selectLatestSummaryCandidate(candidates) ?? null;
        },
        [entryId]
    );
    const updateSummaryDraftFromCandidate = useCallback(
        (candidate: AiCandidateRecord | null) => {
            if (!candidate || !isUsableSummaryCandidate(candidate)) {
                return;
            }
            const candidateId = getCandidateStableId(candidate);
            if (candidateId === loadedSummaryCandidateId) {
                return;
            }
            setLoadedSummaryCandidateId(candidateId);
            setLoadedSummaryCandidateSnapshot(candidate);
            setSummaryDraft(candidate.resultPayload?.trim() || "");
        },
        [loadedSummaryCandidateId]
    );

    const handleSummaryTaskChange = useCallback(
        (task: AiRefinementTaskRecord | null) => {
            syncSummaryTask(task);
            if (task?.status === "SUCCEEDED" || task?.status === "PARTIAL") {
                void refetchSummaryCandidates();
            }
        },
        [refetchSummaryCandidates, syncSummaryTask]
    );

    useEffect(() => {
        if (!summaryModalOpen || !latestSummaryCandidate) {
            return;
        }
        if (getCandidateStableId(latestSummaryCandidate) === loadedSummaryCandidateId) {
            return;
        }
        const timer = window.setTimeout(() => {
            setLoadedSummaryCandidateId(getCandidateStableId(latestSummaryCandidate));
            setLoadedSummaryCandidateSnapshot(latestSummaryCandidate);
            setSummaryDraft(latestSummaryCandidate.resultPayload?.trim() || "");
        }, 0);
        return () => window.clearTimeout(timer);
    }, [latestSummaryCandidate, loadedSummaryCandidateId, summaryModalOpen]);

    const openSummaryModal = () => {
        setSummaryDraft(value || "");
        setLoadedSummaryCandidateId(null);
        setLoadedSummaryCandidateSnapshot(null);
        setSummaryModalOpen(true);
    };
    const closeSummaryModal = () => {
        setSummaryModalOpen(false);
    };
    const requestSummaryTask = () => {
        if (!entryId) {
            return false;
        }
        const formValues = getFormValues();
        if (!formValues.originalText?.trim()) {
            messageApi.warning("请先填写原文");
            return false;
        }
        createSummaryTaskMutation.mutate({
            capability: AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY,
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: entryId,
            objectId: null,
            requestId: createEventId("sancai-summary-task"),
            traceId: createEventId("sancai-summary-trace"),
            inputPayloadJson: buildSummaryInputPayloadJson(entryId, formValues),
            locale: "zh-CN"
        });
        return true;
    };
    const applySummaryDraft = () => {
        if (!entryId) {
            return;
        }
        const resultPayload = summaryDraft;
        if (loadedSummaryCandidate) {
            applySummaryCandidateMutation.mutate({
                candidateId: getCandidateStableId(loadedSummaryCandidate),
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY,
                objectId: loadedSummaryCandidate.objectId,
                resultFormat: loadedSummaryCandidate.resultFormat?.trim() || "TEXT",
                resultPayload,
                changeSummary: "AI 应用：摘要"
            });
            return;
        }
        onChange?.(resultPayload);
        setSummaryModalOpen(false);
        messageApi.success("摘要已写入基础信息");
    };

    const formValues = getFormValues();

    return (
        <div>
            <ClassicsSummaryFormControl
                aiButtonTestId="classics-sancai-sancai-entry-ai-summary-button"
                ariaLabel="三才图会摘要"
                mode={mode}
                value={value}
                onChange={(event) => onChange?.(event.target.value)}
                onOpenAiSummary={openSummaryModal}
            />
            <SancaiEntrySummaryModal
                aiTextDraft={summaryDraft}
                form={formValues}
                isAiTextApplyDisabled={isSummaryApplyDisabled}
                isAiTextCandidateFetching={summaryCandidatesQuery.isFetching}
                isAiTextCandidateLoadError={summaryCandidatesQuery.isError}
                isApplyingAiText={applySummaryCandidateMutation.isPending}
                isCreatingAiTextTask={createSummaryTaskMutation.isPending}
                open={summaryModalOpen}
                summaryTasks={summaryTasks}
                onApply={applySummaryDraft}
                onCancel={closeSummaryModal}
                onFetchResult={loadSummaryCandidate}
                onFetchTask={(taskId) => aiRefinementTaskService.getTask({ taskId })}
                onRequestTask={requestSummaryTask}
                onResultChange={updateSummaryDraftFromCandidate}
                onTaskChange={handleSummaryTaskChange}
                onTextDraftChange={setSummaryDraft}
            />
        </div>
    );
};
