import { FileTextOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Input } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";
import { SancaiEntrySummaryModal } from "./sancai-entry-summary-modal";
import "./sancai-entry-summary-text-field.css";

const AI_TEXT_CANDIDATE_POLL_INTERVAL_MS = 3000;
const RUNNING_REFINEMENT_STATUSES = new Set(["PENDING", "RUNNING"]);

const sortRefinementTasksByNewest = (
    left: AiRefinementTaskRecord,
    right: AiRefinementTaskRecord
) => {
    if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
        return right.requestedAt.localeCompare(left.requestedAt);
    }
    return right.taskId - left.taskId;
};

const selectLatestSummaryCandidate = (candidates: AiCandidateRecord[] | undefined) => {
    return [...(candidates || [])]
        .filter(
            (candidate) =>
                candidate.capability === "summary" &&
                candidate.status === "PENDING" &&
                typeof candidate.resultPayload === "string" &&
                candidate.resultPayload.trim().length > 0
        )
        .sort((left, right) => {
            if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
                return right.requestedAt.localeCompare(left.requestedAt);
            }
            return right.candidateId - left.candidateId;
        })[0];
};

const isRunningRefinementTask = (task?: AiRefinementTaskRecord | null) => {
    return Boolean(task?.status) && RUNNING_REFINEMENT_STATUSES.has(task?.status ?? "");
};

interface SancaiEntrySummaryTextFieldProps {
    entryId?: number;
    form: SancaiEntryFormValues;
    isCreatingSummaryTask?: boolean;
    mode: "create" | "edit";
    summaryTasks?: AiRefinementTaskRecord[];
    value: string;
    onChange: (value: string) => void;
    onRequestSummaryTask?: (draft: SancaiEntryFormValues) => void;
}

export const SancaiEntrySummaryTextField = ({
    entryId,
    form,
    isCreatingSummaryTask = false,
    mode,
    summaryTasks = [],
    value,
    onChange,
    onRequestSummaryTask
}: SancaiEntrySummaryTextFieldProps) => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const [summaryModalOpen, setSummaryModalOpen] = useState(false);
    const [summaryDraft, setSummaryDraft] = useState("");
    const [loadedSummaryCandidateId, setLoadedSummaryCandidateId] = useState<number | null>(null);
    const latestSummaryTask = useMemo(
        () =>
            [...summaryTasks]
                .filter(
                    (task) =>
                        aiRefinementTaskService.getNormalizedTaskCapability(task.capability) ===
                        "summary"
                )
                .sort(sortRefinementTasksByNewest)[0] ?? null,
        [summaryTasks]
    );
    const hasRunningSummaryTask = isRunningRefinementTask(latestSummaryTask);
    const syncSummaryTask = useCallback(
        (task: AiRefinementTaskRecord | null) => {
            if (!task || !entryId) {
                return;
            }
            const normalizedTask = { ...task, capability: "summary" };
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
    const summaryCandidatesQuery = useQuery({
        queryKey: ["ai", "candidates", "SANCAI_ENTRY", entryId, "summary", "modal"],
        queryFn: () =>
            aiCandidateService.list({
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: "summary",
                status: "PENDING"
            }),
        enabled: summaryModalOpen && Boolean(entryId),
        retry: false,
        refetchInterval: () => {
            return isCreatingSummaryTask || hasRunningSummaryTask
                ? AI_TEXT_CANDIDATE_POLL_INTERVAL_MS
                : false;
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
            onChange(command.resultPayload);
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
        return (
            (summaryCandidatesQuery.data || []).find(
                (candidate) =>
                    candidate.candidateId === loadedSummaryCandidateId &&
                    candidate.capability === "summary"
            ) ?? null
        );
    }, [summaryCandidatesQuery.data, loadedSummaryCandidateId]);
    const isSummaryApplyDisabled =
        !summaryDraft.trim() ||
        isCreatingSummaryTask ||
        hasRunningSummaryTask ||
        summaryCandidatesQuery.isFetching;

    useEffect(() => {
        if (!summaryModalOpen || !latestSummaryCandidate) {
            return;
        }
        if (latestSummaryCandidate.candidateId === loadedSummaryCandidateId) {
            return;
        }
        const timer = window.setTimeout(() => {
            setLoadedSummaryCandidateId(latestSummaryCandidate.candidateId);
            setSummaryDraft(latestSummaryCandidate.resultPayload?.trim() || "");
        }, 0);
        return () => window.clearTimeout(timer);
    }, [latestSummaryCandidate, loadedSummaryCandidateId, summaryModalOpen]);

    useEffect(() => {
        if (!summaryModalOpen || !latestSummaryTask?.taskId) {
            return;
        }
        if (latestSummaryTask.status !== "SUCCEEDED" && latestSummaryTask.status !== "PARTIAL") {
            return;
        }
        void summaryCandidatesQuery.refetch();
    }, [
        latestSummaryTask?.status,
        latestSummaryTask?.taskId,
        summaryCandidatesQuery,
        summaryModalOpen
    ]);

    const openSummaryModal = () => {
        setSummaryDraft(value || "");
        setLoadedSummaryCandidateId(null);
        setSummaryModalOpen(true);
    };
    const closeSummaryModal = () => {
        setSummaryModalOpen(false);
    };
    const requestSummaryTask = () => {
        if (!entryId) {
            return false;
        }
        if (!onRequestSummaryTask) {
            messageApi.warning("请先保存条目后再使用 AI摘要");
            return false;
        }
        if (!form.originalText?.trim()) {
            messageApi.warning("请先填写原文");
            return false;
        }
        onRequestSummaryTask(form);
        return true;
    };
    const applySummaryDraft = () => {
        if (!entryId) {
            return;
        }
        const resultPayload = summaryDraft;
        if (loadedSummaryCandidate) {
            applySummaryCandidateMutation.mutate({
                candidateId: loadedSummaryCandidate.candidateId,
                contentId: entryId,
                contentType: "SANCAI_ENTRY",
                capability: "summary",
                objectId: loadedSummaryCandidate.objectId,
                resultFormat: loadedSummaryCandidate.resultFormat?.trim() || "TEXT",
                resultPayload,
                changeSummary: "AI 应用：摘要"
            });
            return;
        }
        onChange(resultPayload);
        setSummaryModalOpen(false);
        messageApi.success("摘要已写入基础信息");
    };

    return (
        <div className="sancai-entry-summary-text-field">
            <Input.TextArea
                aria-label="三才图会摘要"
                value={value}
                autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                onChange={(event) => onChange(event.target.value)}
            />
            {mode === "edit" ? (
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-ai-summary-button"
                        className="sancai-entry-summary-text-field-button"
                        icon={<FileTextOutlined />}
                        onClick={openSummaryModal}
                    >
                        AI摘要
                    </KuzhambuButton>
                </KuzhambuSpace>
            ) : null}
            <SancaiEntrySummaryModal
                aiTextDraft={summaryDraft}
                form={form}
                hasRunningAiTextTask={hasRunningSummaryTask}
                isAiTextApplyDisabled={isSummaryApplyDisabled}
                isAiTextCandidateFetching={summaryCandidatesQuery.isFetching}
                isAiTextCandidateLoadError={summaryCandidatesQuery.isError}
                isApplyingAiText={applySummaryCandidateMutation.isPending}
                isCreatingAiTextTask={isCreatingSummaryTask}
                latestAiTextTask={latestSummaryTask}
                open={summaryModalOpen}
                onApply={applySummaryDraft}
                onCancel={closeSummaryModal}
                onFetchTask={(taskId) =>
                    aiRefinementTaskService.getTask({ taskId: Number(taskId) })
                }
                onRequestTask={requestSummaryTask}
                onTaskChange={syncSummaryTask}
                onTextDraftChange={setSummaryDraft}
            />
        </div>
    );
};
