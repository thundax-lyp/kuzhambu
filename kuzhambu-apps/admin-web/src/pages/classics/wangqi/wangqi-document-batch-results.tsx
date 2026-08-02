import { KuzhambuAlert } from "@/components";
import type { ClassicsBatchOperationRecord } from "@/pages/classics/common/classics-content-types";

interface WangqiDocumentBatchResultsProps {
    batchVisibilityResult?: ClassicsBatchOperationRecord | null;
}

const formatBatchFailures = (result: ClassicsBatchOperationRecord) => {
    return result.failures
        .map(
            (item) =>
                `${item.contentType}#${item.contentId}: ${
                    item.failureReason || item.failureCode || "未知失败"
                }`
        )
        .join("；");
};

export const WangqiDocumentBatchResults = ({
    batchVisibilityResult
}: WangqiDocumentBatchResultsProps) => {
    return (
        <>
            {batchVisibilityResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchVisibilityResult.failureCount > 0 ? "warning" : "success"}
                    className="wangqi-result-alert"
                    title={`可见性结果：成功 ${batchVisibilityResult.successCount}，失败 ${batchVisibilityResult.failureCount}`}
                    description={
                        batchVisibilityResult.failures.length
                            ? formatBatchFailures(batchVisibilityResult)
                            : "全部选中王圻文档已更新可见性。"
                    }
                />
            ) : null}
        </>
    );
};
