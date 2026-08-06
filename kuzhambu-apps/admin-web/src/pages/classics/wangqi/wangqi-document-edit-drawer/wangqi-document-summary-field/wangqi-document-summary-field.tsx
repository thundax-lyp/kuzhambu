import type { ChangeEvent } from "react";
import { ClassicsSummaryFormControl } from "@/pages/classics/common/classics-summary-form-control";

interface WangqiDocumentSummaryFieldProps {
    mode: "create" | "edit";
    summaryLocked?: boolean;
    value?: string;
    onChange?: (event: ChangeEvent<HTMLTextAreaElement>) => void;
    onOpenSummaryModal: () => void;
}

export const WangqiDocumentSummaryField = ({
    mode,
    value,
    onChange,
    summaryLocked = false,
    onOpenSummaryModal
}: WangqiDocumentSummaryFieldProps) => {
    return (
        <ClassicsSummaryFormControl
            aiButtonTestId="classics-wangqi-document-summary-ai-button"
            ariaLabel="王圻文档摘要"
            disabled={summaryLocked}
            maxLength={500}
            mode={mode}
            showCount
            value={value}
            onChange={onChange}
            onOpenAiSummary={onOpenSummaryModal}
        />
    );
};
