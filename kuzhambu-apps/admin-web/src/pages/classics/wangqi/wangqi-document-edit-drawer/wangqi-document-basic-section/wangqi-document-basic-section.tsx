import { DatePicker, Input } from "antd";
import type { DatePickerProps, FormInstance } from "antd";
import {
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuMarkdownEditor,
    KuzhambuSelect,
    type KuzhambuSelectProps
} from "@/components";

import { WangqiDocumentSummaryField } from "../wangqi-document-summary-field";
import type { WangqiDocumentFormValues } from "../wangqi-document-edit-drawer-form-values";
import "./wangqi-document-basic-section.css";

const WangqiDocumentContentFormatSelect = (props: KuzhambuSelectProps<string>) => {
    return (
        <KuzhambuSelect
            {...props}
            aria-label="王圻文档正文格式"
            options={[
                { label: "Markdown", value: "MARKDOWN" },
                { label: "HTML", value: "HTML" }
            ]}
        />
    );
};

const WangqiDocumentTimePicker = (props: DatePickerProps) => {
    return (
        <DatePicker
            {...props}
            aria-label="王圻文档时间"
            picker="month"
            format="YYYY-MM"
            className="wangqi-document-basic-section-date-picker"
        />
    );
};

interface WangqiDocumentBasicSectionProps {
    form: FormInstance<WangqiDocumentFormValues>;
    mode: "create" | "edit";
    summaryLocked: boolean;
    onOpenSummaryModal: () => void;
}

export const WangqiDocumentBasicSection = ({
    form,
    mode,
    summaryLocked,
    onOpenSummaryModal
}: WangqiDocumentBasicSectionProps) => {
    return (
        <KuzhambuForm<WangqiDocumentFormValues>
            form={form}
            colon={false}
            className="wangqi-document-basic-section-form"
        >
            <KuzhambuFormItem
                name="title"
                label="标题"
                layoutSize="large"
                rules={[{ required: true, message: "请输入标题" }]}
            >
                <Input aria-label="王圻文档标题" maxLength={120} showCount />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="contentFormat" label="格式">
                <WangqiDocumentContentFormatSelect />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="documentTime" label="文档时间">
                <WangqiDocumentTimePicker />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="summary" label="摘要" layoutSize="large">
                <WangqiDocumentSummaryField
                    mode={mode}
                    summaryLocked={summaryLocked}
                    onOpenSummaryModal={onOpenSummaryModal}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem name="content" label="正文" layoutSize="large">
                <KuzhambuMarkdownEditor
                    ariaLabel="王圻文档正文"
                    minHeight={360}
                    testIdPrefix="classics-wangqi-markdown"
                />
            </KuzhambuFormItem>
        </KuzhambuForm>
    );
};
