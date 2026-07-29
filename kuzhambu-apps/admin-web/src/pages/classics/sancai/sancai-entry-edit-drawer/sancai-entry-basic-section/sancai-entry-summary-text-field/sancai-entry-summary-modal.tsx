import { FileTextOutlined } from "@ant-design/icons";
import { Input } from "antd";
import { useMemo } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuAlert,
    KuzhambuSyncTaskModal,
    KuzhambuTextCompare,
    type KuzhambuSyncTaskAdapter
} from "@/components";

import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiCandidateRecord } from "@/pages/classics/common/ai-candidate-types";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/sancai-entry-edit-drawer/sancai-entry-form-values";

const MODAL_TITLE = "AI摘要";
const TASK_LABEL = "摘要";

const sortRefinementTasksByNewest = (
    left: AiRefinementTaskRecord,
    right: AiRefinementTaskRecord
) => {
    return aiRefinementTaskService.sortNewestByRequestedAtThenId({
        left: {
            id: aiRefinementTaskService.getTaskStableId(left.taskId, left.taskIdText),
            requestedAt: left.requestedAt
        },
        right: {
            id: aiRefinementTaskService.getTaskStableId(right.taskId, right.taskIdText),
            requestedAt: right.requestedAt
        }
    });
};

const readRefinementTaskStatusLabel = (status?: string | null) => {
    switch (status) {
        case "PENDING":
            return "等待中";
        case "RUNNING":
            return "处理中";
        case "SUCCEEDED":
            return "已完成";
        case "PARTIAL":
            return "部分完成";
        case "FAILED":
            return "失败";
        case "CANCELLED":
            return "已取消";
        default:
            return status || "-";
    }
};

const readRefinementTaskAlertType = (status?: string | null) => {
    if (status === "SUCCEEDED" || status === "PARTIAL") {
        return "success";
    }
    if (status === "FAILED" || status === "CANCELLED") {
        return "warning";
    }
    return "info";
};

const readRefinementTaskFailureText = (task?: AiRefinementTaskRecord | null) => {
    if (!task) {
        return undefined;
    }
    return (
        aiRefinementTaskService.getTaskFailureText(
            task.failureStage,
            task.errorType,
            task.errorMessage
        ) || undefined
    );
};

const summaryTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => aiRefinementTaskService.getTaskStableId(task.taskId, task.taskIdText),
    getMessage: (task) => readRefinementTaskFailureText(task),
    getPhase: (task) => {
        if (task.status === "PENDING" || task.status === "RUNNING") {
            return "tracking";
        }
        if (task.status === "SUCCEEDED" || task.status === "PARTIAL") {
            return task.candidateId ? "result_ready" : "waiting_result";
        }
        if (task.status === "CANCELLED") {
            return "cancelled";
        }
        if (task.status === "FAILED") {
            return "failed";
        }
        return "tracking";
    },
    getResultKey: (task) => task.candidateId,
    getStatusLabel: (task) => `${TASK_LABEL}任务：${readRefinementTaskStatusLabel(task.status)}`
};

interface SancaiEntrySummaryModalProps {
    aiTextDraft: string;
    form: SancaiEntryFormValues;
    isAiTextApplyDisabled: boolean;
    isAiTextCandidateFetching: boolean;
    isAiTextCandidateLoadError: boolean;
    isApplyingAiText: boolean;
    isCreatingAiTextTask: boolean;
    summaryTasks: AiRefinementTaskRecord[];
    onFetchResult: (task: AiRefinementTaskRecord | null) => Promise<AiCandidateRecord | null>;
    onFetchTask: (taskId: string) => Promise<AiRefinementTaskRecord>;
    onApply: () => void;
    onCancel: () => void;
    onRequestTask: () => void;
    onResultChange?: (candidate: AiCandidateRecord | null) => void;
    onTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    onTextDraftChange: (draft: string) => void;
    open: boolean;
}

export const SancaiEntrySummaryModal = ({
    aiTextDraft,
    form,
    isAiTextApplyDisabled,
    isAiTextCandidateFetching,
    isAiTextCandidateLoadError,
    isApplyingAiText,
    isCreatingAiTextTask,
    summaryTasks,
    onFetchResult,
    onFetchTask,
    onApply,
    onCancel,
    onRequestTask,
    onResultChange,
    onTaskChange,
    onTextDraftChange,
    open
}: SancaiEntrySummaryModalProps) => {
    const latestAiTextTask = useMemo(
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

    return (
        <KuzhambuSyncTaskModal<AiRefinementTaskRecord, AiCandidateRecord>
            testId="classics-sancai-sancai-entry-ai-text-modal"
            title={MODAL_TITLE}
            open={open}
            width={960}
            applying={isApplyingAiText}
            applyDisabled={(state) =>
                isAiTextApplyDisabled ||
                state.tracking ||
                state.taskLoading ||
                state.resultLoading ||
                isAiTextCandidateFetching
            }
            applyTestId="classics-sancai-sancai-entry-apply-ai-text-button"
            cancelTestId="classics-sancai-sancai-entry-cancel-ai-text-button"
            createIcon={<FileTextOutlined />}
            createTestId="classics-sancai-sancai-entry-create-ai-text-task-button"
            createText="摘要"
            creating={isCreatingAiTextTask}
            onCancel={onCancel}
            workflow={{
                ...summaryTaskAdapter,
                task: latestAiTextTask,
                createTask: onRequestTask,
                fetchResult: onFetchResult,
                fetchTask: onFetchTask,
                applyResult: onApply,
                onResultChange,
                onTaskChange,
                pollIntervalMs: 3000,
                resultQueryKey: ["SANCAI_ENTRY", latestAiTextTask?.contentId ?? null, "summary"],
                trackTask: Boolean(latestAiTextTask?.taskId)
            }}
            renderStatus={({ creating, task, tracking }) =>
                creating || task ? (
                    <KuzhambuAlert
                        showIcon
                        className="sancai-ai-text-task-alert"
                        type={creating ? "info" : readRefinementTaskAlertType(task?.status)}
                        title={
                            creating
                                ? `正在创建${TASK_LABEL}任务`
                                : `${TASK_LABEL}任务：${readRefinementTaskStatusLabel(task?.status)}`
                        }
                        description={
                            tracking
                                ? "任务完成后会自动刷新 AI摘要。"
                                : readRefinementTaskFailureText(task)
                        }
                    />
                ) : null
            }
            renderBody={({ taskLoading, tracking }) => {
                const isAiTextGenerating = isCreatingAiTextTask || tracking || taskLoading;
                const isAiTextLoading = isAiTextGenerating || isAiTextCandidateFetching;
                return (
                    <>
                        <div className="sancai-detail-card sancai-entry-edit-drawer-form sancai-ai-text-modal-original">
                            <div className="sancai-ai-text-modal-field">
                                <label className="sancai-ai-text-modal-label">原文</label>
                                <Input.TextArea
                                    aria-label={`${MODAL_TITLE}原文`}
                                    value={form.originalText}
                                    readOnly
                                    autoSize={resolveTextAreaAutoSize({ minRows: 5, maxRows: 8 })}
                                />
                            </div>
                        </div>
                        <div className="sancai-ai-text-modal-compare-grid">
                            <div className="sancai-detail-card sancai-entry-edit-drawer-form">
                                <div className="sancai-ai-text-modal-field">
                                    <label className="sancai-ai-text-modal-label">当前摘要</label>
                                    <Input.TextArea
                                        aria-label={`${MODAL_TITLE}当前摘要`}
                                        value={form.summary}
                                        readOnly
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 10,
                                            maxRows: 16
                                        })}
                                    />
                                </div>
                            </div>
                            <div className="sancai-detail-card sancai-entry-edit-drawer-form">
                                <div className="sancai-ai-text-modal-field">
                                    <label className="sancai-ai-text-modal-label">AI摘要</label>
                                    <Input.TextArea
                                        aria-label={`${MODAL_TITLE}AI摘要`}
                                        value={aiTextDraft}
                                        placeholder={
                                            isAiTextGenerating
                                                ? "AI 摘要生成中..."
                                                : isAiTextCandidateFetching
                                                  ? "AI 摘要加载中..."
                                                  : "暂无候选摘要，可先保留当前摘要或稍后重试"
                                        }
                                        disabled={isAiTextLoading}
                                        autoSize={resolveTextAreaAutoSize({
                                            minRows: 10,
                                            maxRows: 16
                                        })}
                                        onChange={(event) => onTextDraftChange(event.target.value)}
                                    />
                                </div>
                                {isAiTextCandidateLoadError ? (
                                    <KuzhambuAlert
                                        showIcon
                                        type="warning"
                                        title={`候选${TASK_LABEL}加载失败`}
                                        description="AI 任务可能仍在执行，请稍后重新打开。"
                                    />
                                ) : null}
                            </div>
                        </div>
                        <KuzhambuTextCompare
                            baseline={form.summary}
                            candidate={aiTextDraft}
                            className="sancai-ai-text-modal-diff"
                            emptyText="当前摘要与 AI 摘要暂无差异"
                            testId="classics-sancai-sancai-entry-ai-summary-compare"
                            title="摘要差异"
                        />
                    </>
                );
            }}
        />
    );
};
