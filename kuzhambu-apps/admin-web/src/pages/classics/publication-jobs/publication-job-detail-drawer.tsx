import { Descriptions } from "antd";
import dayjs from "dayjs";
import { KuzhambuDrawer } from "@/components";
import type { ClassicsPublicationJobRecord } from "@/pages/classics/publication-jobs/publication-jobs-types";

const formatTime = (value?: string | null) =>
    value ? dayjs(value).format("YYYY-MM-DD HH:mm:ss") : "-";

interface PublicationJobDetailDrawerProps {
    job?: ClassicsPublicationJobRecord | null;
    loading: boolean;
    open: boolean;
    onClose: () => void;
}

export const PublicationJobDetailDrawer = ({
    job,
    loading,
    open,
    onClose
}: PublicationJobDetailDrawerProps) => (
    <KuzhambuDrawer
        testId="classics-publication-jobs-detail-drawer"
        aria-label="发布任务详情"
        destroyOnHidden
        loading={loading}
        open={open}
        size="large"
        title="发布任务详情"
        onClose={onClose}
    >
        {job ? (
            <Descriptions column={2} size="small">
                <Descriptions.Item label="任务 ID">{job.id}</Descriptions.Item>
                <Descriptions.Item label="稿件 ID">{job.contentId}</Descriptions.Item>
                <Descriptions.Item label="稿件类型">{job.contentType}</Descriptions.Item>
                <Descriptions.Item label="稿件标题">
                    {job.contentTitleSnapshot || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="任务动作">{job.jobType}</Descriptions.Item>
                <Descriptions.Item label="任务结果">{job.jobResultStatus}</Descriptions.Item>
                <Descriptions.Item label="当前里程碑">{job.jobStatus}</Descriptions.Item>
                <Descriptions.Item label="失败步骤">{job.failureStep || "-"}</Descriptions.Item>
                <Descriptions.Item label="生命周期">
                    {job.sourceLifecycleStatus} → {job.targetLifecycleStatus}
                </Descriptions.Item>
                <Descriptions.Item label="内容版本">
                    {job.contentVersionNo ? `v${job.contentVersionNo}` : "-"}
                </Descriptions.Item>
                <Descriptions.Item label="尝试次数">
                    {job.attemptCount}/{job.maxAttempts}
                </Descriptions.Item>
                <Descriptions.Item label="下次重试">
                    {formatTime(job.nextRetryAt)}
                </Descriptions.Item>
                <Descriptions.Item label="租约到期">{formatTime(job.expiresAt)}</Descriptions.Item>
                <Descriptions.Item label="正文删除时间">
                    {formatTime(job.contentDeletedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="ES 文档">{job.esDocumentId || "-"}</Descriptions.Item>
                <Descriptions.Item label="ES 清理">{job.esCleanupStatus}</Descriptions.Item>
                <Descriptions.Item label="FastGPT 集合">
                    {job.fastgptCollectionId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="FastGPT 清理">
                    {job.fastgptCleanupStatus}
                </Descriptions.Item>
                <Descriptions.Item label="请求时间">
                    {formatTime(job.requestedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="开始时间">{formatTime(job.startedAt)}</Descriptions.Item>
                <Descriptions.Item label="完成时间">{formatTime(job.finishedAt)}</Descriptions.Item>
                <Descriptions.Item label="失败原因">{job.failureReason || "-"}</Descriptions.Item>
                <Descriptions.Item label="诊断摘要" span={2}>
                    <pre className="publication-jobs-detail-summary">
                        {job.detailJsonSummary || "-"}
                    </pre>
                </Descriptions.Item>
            </Descriptions>
        ) : null}
    </KuzhambuDrawer>
);
