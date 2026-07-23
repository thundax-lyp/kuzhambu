import { TranslationOutlined } from "@ant-design/icons";
import { Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { SancaiEntryTranslationModal } from "./sancai-entry-translation-modal";
import type { SancaiEntryTranslationModalProps } from "./sancai-entry-translation-modal";
import "./sancai-entry-translation-text-field.css";

interface SancaiEntryTranslationTextFieldProps {
    mode: "create" | "edit";
    translationModalProps: SancaiEntryTranslationModalProps;
    value: string;
    onChange: (value: string) => void;
    onOpenTranslationModal: () => void;
}

export const SancaiEntryTranslationTextField = ({
    mode,
    translationModalProps,
    value,
    onChange,
    onOpenTranslationModal
}: SancaiEntryTranslationTextFieldProps) => {
    return (
        <div className="sancai-entry-translation-text-field">
            <Input.TextArea
                aria-label="三才图会译文"
                value={value}
                autoSize={resolveTextAreaAutoSize({ minRows: 4, maxRows: 8 })}
                onChange={(event) => onChange(event.target.value)}
            />
            {mode === "edit" ? (
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        testId="classics-sancai-sancai-entry-ai-button"
                        className="sancai-entry-translation-text-field-button"
                        icon={<TranslationOutlined />}
                        onClick={onOpenTranslationModal}
                    >
                        AI翻译
                    </KuzhambuButton>
                </KuzhambuSpace>
            ) : null}
            <SancaiEntryTranslationModal {...translationModalProps} />
        </div>
    );
};
