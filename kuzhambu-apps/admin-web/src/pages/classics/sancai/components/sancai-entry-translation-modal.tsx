import {
    SancaiEntryAiTextWorkflowModal,
    type SancaiEntryAiTextWorkflowModalProps
} from "./sancai-entry-ai-text-modal";

type SancaiEntryTranslationModalProps = Omit<SancaiEntryAiTextWorkflowModalProps, "field">;

export const SancaiEntryTranslationModal = (props: SancaiEntryTranslationModalProps) => (
    <SancaiEntryAiTextWorkflowModal {...props} field="translate" />
);
