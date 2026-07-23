import { FileTextOutlined } from "@ant-design/icons";
import { Form, Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuSyncTaskModal } from "@/components/kuzhambu-sync-task-modal";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import { readSancaiAiTextFieldConfig } from "./sancai-entry-ai-text-config";
import {
    aiTextTaskAdapter,
    readRefinementTaskAlertType,
    readRefinementTaskStatusLabel,
    resolveAiTextField
} from "./sancai-entry-ai-text-workflow";
import type { SancaiEntryFormValues } from "./sancai-form-values";

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
    const aiTextConfig = readSancaiAiTextFieldConfig("summary");

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
            createIcon={<FileTextOutlined />}
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
                                      resolveAiTextField(task?.capability, "summary")
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
