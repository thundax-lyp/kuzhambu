import { Input } from "antd";
import { KuzhambuSpace, KuzhambuButton } from "@/components";
import { isPositiveDecimalId } from "@/types/id";

interface QualityReportGenerateFormProps {
    disabled?: boolean;
    graphVersionId?: string | null;
    loading?: boolean;
    submitLabel?: string;
    onChange: (graphVersionId: string | null) => void;
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
    const normalizedGraphVersionId = graphVersionId?.trim() ?? "";
    const isGraphVersionIdValid = isPositiveDecimalId(normalizedGraphVersionId);

    return (
        <KuzhambuSpace wrap size={12}>
            <Input
                placeholder="graphVersionId"
                value={graphVersionId ?? ""}
                status={normalizedGraphVersionId && !isGraphVersionIdValid ? "error" : undefined}
                onChange={(event) => onChange(event.target.value.trim() || null)}
            />
            <KuzhambuButton
                testId="knowledge-quality-report-quality-report-generate-action-button"
                disabled={disabled || !isGraphVersionIdValid}
                loading={loading}
                type="primary"
                onClick={onGenerate}
            >
                {submitLabel}
            </KuzhambuButton>
        </KuzhambuSpace>
    );
};
