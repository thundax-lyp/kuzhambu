import { KuzhambuAlert, KuzhambuButton, KuzhambuSpace } from "@/components";

import type { RefinementApplyRecord } from "./refinement-types";

interface RefinementApplyResultPanelProps {
    applyResult: RefinementApplyRecord;
}

const buildGraphResultsHref = (applyResult: RefinementApplyRecord) =>
    `/knowledge/graph-results?graphVersionId=${encodeURIComponent(String(applyResult.graphVersionId || ""))}`;

const buildGraphRegenerateHref = (applyResult: RefinementApplyRecord) => {
    const params = new URLSearchParams({
        regenerate: "1",
        taskType: applyResult.taskType || "",
        sourceTaskId: String(applyResult.sourceTaskId || ""),
        triggerSource: applyResult.triggerSource || "REFINEMENT_APPLIED",
        replaceUnconfirmedOnly: String(applyResult.replaceUnconfirmedOnly ?? true)
    });
    if (applyResult.selectionScopeJson) {
        params.set("selectionScopeJson", applyResult.selectionScopeJson);
    }
    return `/knowledge/graph-extraction?${params.toString()}`;
};

const buildQualityReportHref = (applyResult: RefinementApplyRecord) =>
    `/knowledge/quality-report?graphVersionId=${encodeURIComponent(
        String(applyResult.graphVersionId || "")
    )}&regenerate=1`;

export const RefinementApplyResultPanel = ({ applyResult }: RefinementApplyResultPanelProps) => (
    <KuzhambuAlert
        action={
            <KuzhambuSpace size={8}>
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-action-button"
                    href={buildGraphResultsHref(applyResult)}
                    size="small"
                >
                    查看图谱结果
                </KuzhambuButton>
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-action-button-2"
                    href={buildGraphRegenerateHref(applyResult)}
                    size="small"
                >
                    重生成图谱
                </KuzhambuButton>
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-action-button-3"
                    href={buildQualityReportHref(applyResult)}
                    size="small"
                >
                    重新生成质量报告
                </KuzhambuButton>
            </KuzhambuSpace>
        }
        title="精修已应用，图谱与质量报告需要继续联动处理"
        showIcon
        type="success"
    />
);
