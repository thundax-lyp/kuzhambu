import { FileTextOutlined } from "@ant-design/icons";
import { Form, Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./wangqi-document-summary-field.css";

const { TextArea } = Input;

interface WangqiDocumentSummaryFieldProps {
    mode: "create" | "edit";
    summaryLocked?: boolean;
    onOpenSummaryModal: () => void;
}

export const WangqiDocumentSummaryField = ({
    mode,
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
            <Form.Item name="summary" noStyle>
                <TextArea
                    aria-label="王圻文档摘要"
                    autoSize={resolveTextAreaAutoSize({
                        minRows: 4,
                        maxRows: 8
                    })}
                    disabled={summaryLocked}
                    maxLength={500}
                    showCount
                />
            </Form.Item>
        </div>
    );
};
