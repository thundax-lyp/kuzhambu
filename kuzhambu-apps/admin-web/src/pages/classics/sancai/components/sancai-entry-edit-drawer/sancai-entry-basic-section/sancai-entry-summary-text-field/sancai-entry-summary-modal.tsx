import { FileTextOutlined } from "@ant-design/icons";
import { Form, Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert, KuzhambuSyncTaskModal, type KuzhambuSyncTaskAdapter } from "@/components";

import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import type { SancaiEntryFormValues } from "@/pages/classics/sancai/components/sancai-entry-edit-drawer/sancai-entry-form-values";

const MODAL_TITLE = "AI摘要";
const TASK_LABEL = "摘要";

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

const summaryTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => task.taskId,
    getMessage: (task) => task.errorMessage || undefined,
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

export const SancaiEntrySummaryModal = ({
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
}: SancaiEntrySummaryModalProps) => {
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
            createIcon={<FileTextOutlined />}
            createTestId="classics-sancai-sancai-entry-create-ai-text-task-button"
            createText="摘要"
            creating={isCreatingAiTextTask}
            onCancel={onCancel}
            workflow={{
                ...summaryTaskAdapter,
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
                                ? "任务完成后会自动刷新 AI摘要。"
                                : task?.errorMessage || undefined
                        }
                    />
                ) : null
            }
            renderBody={() => (
                <>
                    <Form
                        className="sancai-detail-card sancai-entry-edit-drawer-form sancai-ai-text-modal-original"
                        colon={false}
                        component="div"
                        layout="vertical"
                    >
                        <Form.Item label="原文">
                            <Input.TextArea
                                aria-label={`${MODAL_TITLE}原文`}
                                value={form.originalText}
                                readOnly
                                autoSize={resolveTextAreaAutoSize({ minRows: 5, maxRows: 8 })}
                            />
                        </Form.Item>
                    </Form>
                    <div className="sancai-ai-text-modal-compare-grid">
                        <Form
                            className="sancai-detail-card sancai-entry-edit-drawer-form"
                            colon={false}
                            component="div"
                            layout="vertical"
                        >
                            <Form.Item label="当前摘要">
                                <Input.TextArea
                                    aria-label={`${MODAL_TITLE}当前摘要`}
                                    value={form.summary}
                                    readOnly
                                    autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 16 })}
                                />
                            </Form.Item>
                        </Form>
                        <Form
                            className="sancai-detail-card sancai-entry-edit-drawer-form"
                            colon={false}
                            component="div"
                            layout="vertical"
                        >
                            <Form.Item label="AI摘要">
                                <Input.TextArea
                                    aria-label={`${MODAL_TITLE}AI摘要`}
                                    value={aiTextDraft}
                                    placeholder={
                                        isCreatingAiTextTask || isAiTextCandidateFetching
                                            ? "AI 摘要生成中..."
                                            : "暂无候选摘要，可先保留当前摘要或稍后重试"
                                    }
                                    disabled={
                                        isCreatingAiTextTask ||
                                        hasRunningAiTextTask ||
                                        isAiTextCandidateFetching
                                    }
                                    autoSize={resolveTextAreaAutoSize({ minRows: 10, maxRows: 16 })}
                                    onChange={(event) => onTextDraftChange(event.target.value)}
                                />
                            </Form.Item>
                            {isAiTextCandidateLoadError ? (
                                <KuzhambuAlert
                                    showIcon
                                    type="warning"
                                    title={`候选${TASK_LABEL}加载失败`}
                                    description="AI 任务可能仍在执行，请稍后重新打开。"
                                />
                            ) : null}
                        </Form>
                    </div>
                </>
            )}
        />
    );
};
