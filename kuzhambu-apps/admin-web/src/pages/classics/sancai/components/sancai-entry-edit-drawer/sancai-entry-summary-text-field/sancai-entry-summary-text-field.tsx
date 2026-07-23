import { FileTextOutlined } from "@ant-design/icons";
import { Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { SancaiEntrySummaryModal } from "./sancai-entry-summary-modal";
import type { SancaiEntrySummaryModalProps } from "./sancai-entry-summary-modal";
import "./sancai-entry-summary-text-field.css";

interface SancaiEntrySummaryTextFieldProps {
    mode: "create" | "edit";
    summaryModalProps: SancaiEntrySummaryModalProps;
    value: string;
    onChange: (value: string) => void;
    onOpenSummaryModal: () => void;
}

export const SancaiEntrySummaryTextField = ({
    mode,
    summaryModalProps,
    value,
    onChange,
    onOpenSummaryModal
}: SancaiEntrySummaryTextFieldProps) => {
    return (
        <div className="sancai-entry-summary-text-field">
            <Input.TextArea
                aria-label="三才图会摘要"
                value={value}
                autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                onChange={(event) => onChange(event.target.value)}
            />
            {mode === "edit" ? (
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-ai-summary-button"
                        className="sancai-entry-summary-text-field-button"
                        icon={<FileTextOutlined />}
                        onClick={onOpenSummaryModal}
                    >
                        AI摘要
                    </KuzhambuButton>
                </KuzhambuSpace>
            ) : null}
            <SancaiEntrySummaryModal {...summaryModalProps} />
        </div>
    );
};
