import { KuzhambuDrawer } from "@/components";
import { ClassicsExportJobSection } from "@/pages/classics/common/components/classics-export-job-section";
import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";

interface WangqiExportActionsProps {
    canExportDocuments: boolean;
    exportJobs: ClassicsExportJobRecord[];
    loading: boolean;
    open: boolean;
    onBatchDelete: (jobs: ClassicsExportJobRecord[]) => void;
    onClose: () => void;
    onDelete: (job: ClassicsExportJobRecord) => void;
    onRefresh: () => void;
}

export const WangqiExportActions = ({
    canExportDocuments,
    exportJobs,
    loading,
    open,
    onBatchDelete,
    onClose,
    onDelete,
    onRefresh
}: WangqiExportActionsProps) => {
    return (
        <KuzhambuDrawer
            testId="classics-wangqi-wangqi-drawer"
            aria-label="王圻导出任务"
            destroyOnHidden
            open={open}
            size="large"
            title="导出任务"
            onClose={onClose}
        >
            <ClassicsExportJobSection
                items={exportJobs}
                loading={loading}
                onDownload={(job) => {
                    if (job.downloadUrl) {
                        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                    }
                }}
                onDelete={canExportDocuments ? onDelete : undefined}
                onBatchDelete={canExportDocuments ? onBatchDelete : undefined}
                onRefresh={onRefresh}
            />
        </KuzhambuDrawer>
    );
};
