import {
    SancaiEntryAiTextWorkflowModal,
    type SancaiEntryAiTextWorkflowModalProps
} from "./sancai-entry-ai-text-modal";

type SancaiEntrySummaryModalProps = Omit<SancaiEntryAiTextWorkflowModalProps, "field">;

export const SancaiEntrySummaryModal = (props: SancaiEntrySummaryModalProps) => (
    <SancaiEntryAiTextWorkflowModal {...props} field="summary" />
);
