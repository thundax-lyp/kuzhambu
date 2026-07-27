import { FileTextOutlined } from "@ant-design/icons";
import { Input } from "antd";
import type { ChangeEvent } from "react";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components";
import "./wangqi-document-summary-field.css";

const { TextArea } = Input;

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
        <div className="wangqi-document-summary-field">
            {mode === "edit" ? (
                <div className="wangqi-document-summary-field-action">
                    <KuzhambuButton
                        testId="classics-wangqi-document-summary-ai-button"
                        type="primary"
                        ariaLabel="AI 摘要"
                        icon={<FileTextOutlined />}
                        onClick={onOpenSummaryModal}
                    >
                        AI 摘要
                    </KuzhambuButton>
                </div>
            ) : null}
            <TextArea
                aria-label="王圻文档摘要"
                autoSize={resolveTextAreaAutoSize({
                    minRows: 4,
                    maxRows: 8
                })}
                disabled={summaryLocked}
                maxLength={500}
                showCount
                value={value}
                onChange={onChange}
            />
        </div>
    );
};
