import { TranslationOutlined } from "@ant-design/icons";
import { Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert, KuzhambuSyncTaskModal, type KuzhambuSyncTaskAdapter } from "@/components";

import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";

const MODAL_TITLE = "AI翻译";
const TASK_LABEL = "翻译";

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

const translationTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
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

interface SancaiEntryTranslationModalProps {
    aiTextDraft: string;
    form: SancaiEntryFormValues;
    hasRunningAiTextTask: boolean;
    isAiTextApplyDisabled: boolean;
    isAiTextCandidateFetching: boolean;
    isAiTextCandidateLoadError: boolean;
    isApplyingAiText: boolean;
    isCreatingAiTextTask: boolean;
    latestAiTextTask: AiRefinementTaskRecord | null;
    onFetchTask: (taskId: number | string) => Promise<AiRefinementTaskRecord>;
    onApply: () => void;
    onCancel: () => void;
    onRequestTask: () => void;
    onTaskChange?: (task: AiRefinementTaskRecord | null) => void;
    onTextDraftChange: (draft: string) => void;
    open: boolean;
}

export const SancaiEntryTranslationModal = ({
    aiTextDraft,
    form,
    hasRunningAiTextTask,
    isAiTextApplyDisabled,
    isAiTextCandidateFetching,
    isAiTextCandidateLoadError,
    isApplyingAiText,
    isCreatingAiTextTask,
    latestAiTextTask,
    onFetchTask,
    onApply,
    onCancel,
    onRequestTask,
    onTaskChange,
    onTextDraftChange,
    open
}: SancaiEntryTranslationModalProps) => {
    return (
        <KuzhambuSyncTaskModal<AiRefinementTaskRecord, null>
            testId="classics-sancai-sancai-entry-ai-text-modal"
            title={MODAL_TITLE}
            open={open}
            width={960}
            applying={isApplyingAiText}
            applyDisabled={isAiTextApplyDisabled}
            applyTestId="classics-sancai-sancai-entry-apply-ai-text-button"
            cancelTestId="classics-sancai-sancai-entry-cancel-ai-text-button"
            createIcon={<TranslationOutlined />}
            createTestId="classics-sancai-sancai-entry-create-ai-text-task-button"
            createText="翻译"
            creating={isCreatingAiTextTask}
            onCancel={onCancel}
            workflow={{
                ...translationTaskAdapter,
                task: latestAiTextTask,
                createTask: onRequestTask,
                fetchTask: onFetchTask,
                applyResult: onApply,
                onTaskChange,
                trackTask: Boolean(latestAiTextTask?.taskId)
            }}
            renderStatus={({ creating, task }) =>
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
                            hasRunningAiTextTask
                                ? "任务完成后会自动刷新 AI译文。"
                                : readRefinementTaskFailureText(task)
                        }
                    />
                ) : null
            }
            renderBody={() => (
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
                                <label className="sancai-ai-text-modal-label">当前译文</label>
                                <Input.TextArea
                                    aria-label={`${MODAL_TITLE}当前译文`}
                                    value={form.translationText}
                                    readOnly
                                    autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 16 })}
                                />
                            </div>
                        </div>
                        <div className="sancai-detail-card sancai-entry-edit-drawer-form">
                            <div className="sancai-ai-text-modal-field">
                                <label className="sancai-ai-text-modal-label">AI译文</label>
                                <Input.TextArea
                                    aria-label={`${MODAL_TITLE}AI译文`}
                                    value={aiTextDraft}
                                    placeholder={
                                        isCreatingAiTextTask || isAiTextCandidateFetching
                                            ? "AI 翻译生成中..."
                                            : "暂无候选译文，可先保留当前译文或稍后重试"
                                    }
                                    disabled={
                                        isCreatingAiTextTask ||
                                        hasRunningAiTextTask ||
                                        isAiTextCandidateFetching
                                    }
                                    autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 16 })}
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
                </>
            )}
        />
    );
};
