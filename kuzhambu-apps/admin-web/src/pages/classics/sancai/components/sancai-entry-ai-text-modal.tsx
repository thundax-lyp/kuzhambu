import { FileTextOutlined, TranslationOutlined } from "@ant-design/icons";
import { Form, Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
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

interface SancaiEntryAiTextModalProps {
    activeAiTextField: SancaiAiTextField | null;
    aiTextDraft: string;
    form: SancaiEntryFormValues;
    hasRunningAiTextTask: boolean;
    isAiTextApplyDisabled: boolean;
    isAiTextCandidateFetching: boolean;
    isAiTextCandidateLoadError: boolean;
    isApplyingAiText: boolean;
    isCreatingAiTextTask: boolean;
    latestAiTextTask: AiRefinementTaskRecord | null;
    onApply: () => void;
    onCancel: () => void;
    onRequestTask: () => void;
    onTextDraftChange: (draft: string) => void;
}

export const SancaiEntryAiTextModal = ({
    activeAiTextField,
    aiTextDraft,
    form,
    hasRunningAiTextTask,
    isAiTextApplyDisabled,
    isAiTextCandidateFetching,
    isAiTextCandidateLoadError,
    isApplyingAiText,
    isCreatingAiTextTask,
    latestAiTextTask,
    onApply,
    onCancel,
    onRequestTask,
    onTextDraftChange
}: SancaiEntryAiTextModalProps) => {
    const aiTextConfig = activeAiTextField
        ? readSancaiAiTextFieldConfig(activeAiTextField)
        : readSancaiAiTextFieldConfig("translate");

    return (
        <KuzhambuModal
            testId="classics-sancai-sancai-entry-ai-text-modal"
            title={aiTextConfig.modalTitle}
            open={Boolean(activeAiTextField)}
            width={960}
            destroyOnHidden
            footer={
                <div className="sancai-modal-footer">
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-cancel-ai-text-button"
                        onClick={onCancel}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-apply-ai-text-button"
                        type="primary"
                        loading={isApplyingAiText}
                        disabled={isAiTextApplyDisabled}
                        onClick={onApply}
                    >
                        采用
                    </KuzhambuButton>
                </div>
            }
            onCancel={onCancel}
        >
            <div className="sancai-ai-text-modal-toolbar">
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-create-ai-text-task-button"
                        icon={
                            activeAiTextField === "summary" ? (
                                <FileTextOutlined />
                            ) : (
                                <TranslationOutlined />
                            )
                        }
                        type="primary"
                        loading={isCreatingAiTextTask}
                        onClick={onRequestTask}
                    >
                        {aiTextConfig.actionLabel}
                    </KuzhambuButton>
                </KuzhambuSpace>
            </div>
            {isCreatingAiTextTask || latestAiTextTask ? (
                <KuzhambuAlert
                    showIcon
                    className="sancai-ai-text-task-alert"
                    type={
                        isCreatingAiTextTask
                            ? "info"
                            : readRefinementTaskAlertType(latestAiTextTask?.status)
                    }
                    title={
                        isCreatingAiTextTask
                            ? `正在创建${aiTextConfig.taskLabel}任务`
                            : `${aiTextConfig.taskLabel}任务：${readRefinementTaskStatusLabel(
                                  latestAiTextTask?.status,
                                  latestAiTextTask?.capability
                              )}`
                    }
                    description={
                        hasRunningAiTextTask
                            ? `任务完成后会自动刷新 ${aiTextConfig.aiLabel}。`
                            : latestAiTextTask?.errorMessage || undefined
                    }
                />
            ) : null}
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
                                activeAiTextField === "summary"
                                    ? form.summary
                                    : form.translationText
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
        </KuzhambuModal>
    );
};
