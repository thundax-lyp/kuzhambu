import { Typography } from "antd";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { ClassicsBatchOperationRecord } from "@/pages/classics/common/classics-content-types";

const { Text } = Typography;

interface WangqiDocumentToolbarProps {
    batchShareResult?: ClassicsBatchOperationRecord | null;
    batchVisibilityResult?: ClassicsBatchOperationRecord | null;
    canChangeDocumentVisibility: boolean;
    canShareDocuments: boolean;
    isBatchSharing: boolean;
    isBatchVisibilityChanging: boolean;
    recordCount: number;
    selectedCount: number;
    onChangeSelectedVisibility: (visibility: "PRIVATE" | "PUBLIC") => void;
    onOpenBatchCandidateDrawer: () => void;
    onShareSelectedDocuments: () => void;
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

export const WangqiDocumentToolbar = ({
    batchShareResult,
    batchVisibilityResult,
    canChangeDocumentVisibility,
    canShareDocuments,
    isBatchSharing,
    isBatchVisibilityChanging,
    recordCount,
    selectedCount,
    onChangeSelectedVisibility,
    onOpenBatchCandidateDrawer,
    onShareSelectedDocuments
}: WangqiDocumentToolbarProps) => {
    return (
        <>
            <div className="wangqi-document-toolbar">
                <Text type="secondary">
                    已选 {selectedCount} / 当前页 {recordCount}
                </Text>
                <KuzhambuSpace className="wangqi-document-toolbar-actions" wrap>
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-batch-share-button"
                        disabled={!selectedCount || !canShareDocuments}
                        loading={isBatchSharing}
                        onClick={onShareSelectedDocuments}
                    >
                        分享文档
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-action-button"
                        disabled={!selectedCount || !canChangeDocumentVisibility}
                        onClick={onOpenBatchCandidateDrawer}
                    >
                        候选治理
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-batch-public-button"
                        disabled={!selectedCount || !canChangeDocumentVisibility}
                        loading={isBatchVisibilityChanging}
                        onClick={() => onChangeSelectedVisibility("PUBLIC")}
                    >
                        设为公开
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-wangqi-wangqi-batch-private-button"
                        disabled={!selectedCount || !canChangeDocumentVisibility}
                        loading={isBatchVisibilityChanging}
                        onClick={() => onChangeSelectedVisibility("PRIVATE")}
                    >
                        设为私有
                    </KuzhambuButton>
                </KuzhambuSpace>
            </div>
            {batchShareResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                    className="wangqi-result-alert"
                    title={`分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
                    description={
                        batchShareResult.failures.length
                            ? formatBatchFailures(batchShareResult)
                            : "全部选中王圻文档已创建分享记录。"
                    }
                />
            ) : null}
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
