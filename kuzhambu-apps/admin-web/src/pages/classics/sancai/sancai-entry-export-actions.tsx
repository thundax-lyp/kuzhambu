import { KuzhambuAlert, KuzhambuDrawer } from "@/components";

import type { ClassicsExportJobRecord } from "@/pages/classics/common/classics-export-types";
import { ClassicsExportJobSection } from "@/pages/classics/common/classics-export-job-section";

interface SancaiEntryExportActionsProps {
    canManageGeneratedArtifacts: boolean;
    isError: boolean;
    loading: boolean;
    open: boolean;
    exportJobs: ClassicsExportJobRecord[];
    onBatchDelete: (jobs: ClassicsExportJobRecord[]) => void;
    onClose: () => void;
    onDelete: (job: ClassicsExportJobRecord) => void;
    onRefresh: () => void;
}

export const SancaiEntryExportActions = ({
    canManageGeneratedArtifacts,
    isError,
    loading,
    open,
    exportJobs,
    onBatchDelete,
    onClose,
    onDelete,
    onRefresh
}: SancaiEntryExportActionsProps) => {
    return (
        <KuzhambuDrawer
            testId="classics-sancai-sancai-entry-panel-drawer"
            destroyOnClose={false}
            open={open}
            size="large"
            title="导出任务"
            footerActions={[
                {
                    testId: "classics-sancai-sancai-entry-close-button",
                    title: "关闭",
                    type: "primary",
                    action: onClose
                }
            ]}
            onClose={onClose}
        >
            {isError ? (
                <KuzhambuAlert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="导出任务列表加载失败"
                    description="请确认后台导出任务接口可用后刷新页面。"
                />
            ) : null}
            <ClassicsExportJobSection
                items={exportJobs}
                loading={loading}
                sectionTitle="任务列表"
                onDownload={(job) => {
                    if (job.downloadUrl) {
                        window.open(job.downloadUrl, "_blank", "noopener,noreferrer");
                    }
                }}
                onDelete={canManageGeneratedArtifacts ? onDelete : undefined}
                onBatchDelete={canManageGeneratedArtifacts ? onBatchDelete : undefined}
                onRefresh={onRefresh}
            />
        </KuzhambuDrawer>
    );
};
