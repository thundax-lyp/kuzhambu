import { FileTextOutlined } from "@ant-design/icons";
import { Input } from "antd";
import type { ChangeEvent } from "react";
import { KuzhambuButton } from "@/components";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import "./classics-summary-form-control.css";

interface ClassicsSummaryFormControlProps {
    aiButtonTestId: string;
    ariaLabel: string;
    mode: "create" | "edit";
    disabled?: boolean;
    maxLength?: number;
    showCount?: boolean;
    value?: string;
    onChange?: (event: ChangeEvent<HTMLTextAreaElement>) => void;
    onOpenAiSummary: () => void;
}

export const ClassicsSummaryFormControl = ({
    aiButtonTestId,
    ariaLabel,
    mode,
    disabled = false,
    maxLength,
    showCount = false,
    value,
    onChange,
    onOpenAiSummary
}: ClassicsSummaryFormControlProps) => (
    <div className="classics-summary-form-control">
        <Input.TextArea
            aria-label={ariaLabel}
            autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
            disabled={disabled}
            maxLength={maxLength}
            showCount={showCount}
            value={value}
            onChange={onChange}
        />
        {mode === "edit" ? (
            <KuzhambuButton
                testId={aiButtonTestId}
                className="classics-summary-form-control-ai-button"
                ariaLabel="AI 摘要"
                icon={<FileTextOutlined />}
                onClick={onOpenAiSummary}
            >
                AI摘要
            </KuzhambuButton>
        ) : null}
    </div>
);
