import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import type { ClassicsBatchOperationRecord } from "@/pages/classics/common/classics-content-types";
import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";
import { KuzhambuAlert, KuzhambuButton } from "@/components";

interface MingCustomsExportActionsProps {
    batchShareResult: ClassicsBatchOperationRecord | null;
    batchVisibilityResult: ClassicsBatchOperationRecord | null;
    canChangeEntryVisibility: boolean;
    canExportEntries: boolean;
    canShareEntries: boolean;
    exportJobs: ClassicsExportJobRecord[];
    hasExportJobsError: boolean;
    loading: boolean;
    onBatchCandidate: () => void;
    onBatchDeleteExportJobs: (jobs: ClassicsExportJobRecord[]) => void;
    onChangeSelectedVisibility: (visibility: "PRIVATE" | "PUBLIC") => void;
    onDeleteExportJob: (job: ClassicsExportJobRecord) => void;
    onRefreshExportJobs: () => void;
    onShareSelectedEntries: () => void;
    selectedEntriesCount: number;
    sharing: boolean;
    visibilityChanging: boolean;
}

const renderBatchResultDescription = (result: ClassicsBatchOperationRecord) => {
    if (!result.failures.length) {
        return "全部选中明代习俗已处理完成。";
    }

    return result.failures
        .map(
            (item) =>
                `${item.contentType}#${item.contentId}: ${item.failureReason || item.failureCode || "未知失败"}`
        )
        .join("；");
};

export const MingCustomsExportActions = ({
    batchShareResult,
    batchVisibilityResult,
    canChangeEntryVisibility,
    canExportEntries,
    canShareEntries,
    exportJobs,
    hasExportJobsError,
    loading,
    onBatchCandidate,
    onBatchDeleteExportJobs,
    onChangeSelectedVisibility,
    onDeleteExportJob,
    onRefreshExportJobs,
    onShareSelectedEntries,
    selectedEntriesCount,
    sharing,
    visibilityChanging
}: MingCustomsExportActionsProps) => {
    return (
        <>
            {hasExportJobsError ? (
                <KuzhambuAlert
                    type="warning"
                    showIcon
                    title="导出任务列表加载失败"
                    description="请确认后台导出任务接口可用后重试。"
                />
            ) : null}
            <ClassicsExportJobSection
                items={exportJobs}
                loading={loading}
                onDownload={(job) => {
                    if (job.downloadUrl) {
                        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                    }
                }}
                onDelete={canExportEntries ? onDeleteExportJob : undefined}
                onBatchDelete={canExportEntries ? onBatchDeleteExportJobs : undefined}
                onRefresh={onRefreshExportJobs}
            />
            <div style={{ marginBottom: 12 }}>
                <KuzhambuButton
                    testId="classics-ming-customs-ming-customs-batch-share-button"
                    disabled={!selectedEntriesCount || !canShareEntries}
                    loading={sharing}
                    onClick={onShareSelectedEntries}
                >
                    批量分享
                </KuzhambuButton>
                <KuzhambuButton
                    testId="classics-ming-customs-ming-customs-action-button-2"
                    disabled={!selectedEntriesCount || !canChangeEntryVisibility}
                    style={{ marginLeft: 8 }}
                    onClick={onBatchCandidate}
                >
                    批量候选治理
                </KuzhambuButton>
                <KuzhambuButton
                    testId="classics-ming-customs-ming-customs-batch-public-button"
                    disabled={!selectedEntriesCount || !canChangeEntryVisibility}
                    loading={visibilityChanging}
                    style={{ marginLeft: 8 }}
                    onClick={() => onChangeSelectedVisibility("PUBLIC")}
                >
                    批量公开
                </KuzhambuButton>
                <KuzhambuButton
                    testId="classics-ming-customs-ming-customs-batch-private-button"
                    disabled={!selectedEntriesCount || !canChangeEntryVisibility}
                    loading={visibilityChanging}
                    style={{ marginLeft: 8 }}
                    onClick={() => onChangeSelectedVisibility("PRIVATE")}
                >
                    批量私有
                </KuzhambuButton>
            </div>
            {batchShareResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchShareResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    title={`批量分享结果：成功 ${batchShareResult.successCount}，失败 ${batchShareResult.failureCount}`}
                    description={
                        batchShareResult.failures.length
                            ? renderBatchResultDescription(batchShareResult)
                            : "全部选中明代习俗已创建分享记录。"
                    }
                />
            ) : null}
            {batchVisibilityResult ? (
                <KuzhambuAlert
                    showIcon
                    type={batchVisibilityResult.failureCount > 0 ? "warning" : "success"}
                    style={{ marginBottom: 12 }}
                    title={`批量可见性结果：成功 ${batchVisibilityResult.successCount}，失败 ${batchVisibilityResult.failureCount}`}
                    description={
                        batchVisibilityResult.failures.length
                            ? renderBatchResultDescription(batchVisibilityResult)
                            : "全部选中明代习俗已更新可见性。"
                    }
                />
            ) : null}
        </>
    );
};
