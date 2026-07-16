import { InputNumber } from "antd";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface QualityReportGenerateFormProps {
    disabled?: boolean;
    graphVersionId?: number | null;
    loading?: boolean;
    submitLabel?: string;
    onChange: (graphVersionId: number | null) => void;
    onGenerate: () => void;
}

export const QualityReportGenerateForm = ({
    disabled = false,
    graphVersionId = null,
    loading = false,
    submitLabel = "生成报告",
    onChange,
    onGenerate
}: QualityReportGenerateFormProps) => {
    return (
        <KuzhambuSpace wrap size={12}>
            <InputNumber
                min={1}
                placeholder="graphVersionId"
                precision={0}
                value={graphVersionId}
                onChange={onChange}
            />
            <KuzhambuButton
                name={String(submitLabel)}
                disabled={disabled || !graphVersionId}
                loading={loading}
                type="primary"
                onClick={onGenerate}
            >
                {submitLabel}
            </KuzhambuButton>
        </KuzhambuSpace>
    );
};
