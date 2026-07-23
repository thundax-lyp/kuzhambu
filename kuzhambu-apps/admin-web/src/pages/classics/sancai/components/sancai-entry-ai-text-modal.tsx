import { FileTextOutlined, TranslationOutlined } from "@ant-design/icons";
import { Form, Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuSyncTaskModal } from "@/components/kuzhambu-sync-task-modal";
import type { KuzhambuSyncTaskAdapter } from "@/components/kuzhambu-sync-task-modal";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import { readSancaiAiTextFieldConfig } from "./sancai-entry-ai-text-config";
import type { SancaiAiTextField } from "./sancai-entry-ai-text-config";
import type { SancaiEntryFormValues } from "./sancai-form-values";

const readRefinementTaskStatusLabel = (status?: string | null, capability?: string | null) => {
    switch (status) {
        case "PENDING":
            return "等待中";
        case "RUNNING":
            if (capability === "translate") {
                return "翻译中";
            }
            if (capability === "summary") {
                return "摘要中";
            }
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

const resolveAiTextField = (
    capability?: string | null,
    fallback: SancaiAiTextField = "translate"
) => {
    return capability === "summary" || capability === "translate" ? capability : fallback;
};

const aiTextTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
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
    getStatusLabel: (task) =>
        `${readSancaiAiTextFieldConfig(resolveAiTextField(task.capability)).taskLabel}任务：${readRefinementTaskStatusLabel(
            task.status,
            task.capability
        )}`
};

export interface SancaiEntryAiTextWorkflowModalProps {
    aiTextDraft: string;
    field: SancaiAiTextField;
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

export const SancaiEntryAiTextWorkflowModal = ({
    aiTextDraft,
    field,
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
}: SancaiEntryAiTextWorkflowModalProps) => {
    const aiTextConfig = readSancaiAiTextFieldConfig(field);
    const createIcon = field === "summary" ? <FileTextOutlined /> : <TranslationOutlined />;

    return (
        <KuzhambuSyncTaskModal<AiRefinementTaskRecord, null>
            testId="classics-sancai-sancai-entry-ai-text-modal"
            title={aiTextConfig.modalTitle}
            open={open}
            width={960}
            applying={isApplyingAiText}
            applyDisabled={isAiTextApplyDisabled}
            applyTestId="classics-sancai-sancai-entry-apply-ai-text-button"
            cancelTestId="classics-sancai-sancai-entry-cancel-ai-text-button"
            createIcon={createIcon}
            createTestId="classics-sancai-sancai-entry-create-ai-text-task-button"
            createText={aiTextConfig.actionLabel}
            creating={isCreatingAiTextTask}
            onCancel={onCancel}
            workflow={{
                ...aiTextTaskAdapter,
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
                                ? `正在创建${aiTextConfig.taskLabel}任务`
                                : `${aiTextConfig.taskLabel}任务：${readRefinementTaskStatusLabel(
                                      task?.status,
                                      resolveAiTextField(task?.capability, field)
                                  )}`
                        }
                        description={
                            hasRunningAiTextTask
                                ? `任务完成后会自动刷新 ${aiTextConfig.aiLabel}。`
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
                        <Form.Item label={aiTextConfig.sourceLabel}>
                            <Input.TextArea
                                aria-label={`${aiTextConfig.modalTitle}${aiTextConfig.sourceLabel}`}
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
                            <Form.Item label={aiTextConfig.currentLabel}>
                                <Input.TextArea
                                    aria-label={`${aiTextConfig.modalTitle}${aiTextConfig.currentLabel}`}
                                    value={
                                        field === "summary" ? form.summary : form.translationText
                                    }
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
                            <Form.Item label={aiTextConfig.aiLabel}>
                                <Input.TextArea
                                    aria-label={`${aiTextConfig.modalTitle}${aiTextConfig.aiLabel}`}
                                    value={aiTextDraft}
                                    placeholder={
                                        isCreatingAiTextTask || isAiTextCandidateFetching
                                            ? aiTextConfig.loadingText
                                            : aiTextConfig.emptyText
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
                                    title={`候选${aiTextConfig.taskLabel}加载失败`}
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
