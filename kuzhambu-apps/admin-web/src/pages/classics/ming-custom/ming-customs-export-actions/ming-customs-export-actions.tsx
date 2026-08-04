import { KuzhambuAlert, KuzhambuDrawer } from "@/components";
import { ClassicsExportJobSection } from "@/pages/classics/common/classics-export-job-section";
import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";

interface MingCustomsExportActionsProps {
    canExportEntries: boolean;
    exportJobs: ClassicsExportJobRecord[];
    hasExportJobsError: boolean;
    loading: boolean;
    onBatchDeleteExportJobs: (jobs: ClassicsExportJobRecord[]) => void;
    onCloseExportJobs: () => void;
    onDeleteExportJob: (job: ClassicsExportJobRecord) => void;
    onRefreshExportJobs: () => void;
    openExportJobs: boolean;
}

export const MingCustomsExportActions = ({
    canExportEntries,
    exportJobs,
    hasExportJobsError,
    loading,
    onBatchDeleteExportJobs,
    onCloseExportJobs,
    onDeleteExportJob,
    onRefreshExportJobs,
    openExportJobs
}: MingCustomsExportActionsProps) => {
    return (
        <KuzhambuDrawer
            testId="classics-ming-customs-ming-customs-export-jobs-drawer"
            aria-label="明代习俗导出任务"
            destroyOnHidden
            open={openExportJobs}
            size="large"
            title="导出任务"
            onClose={onCloseExportJobs}
        >
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
        </KuzhambuDrawer>
    );
};
