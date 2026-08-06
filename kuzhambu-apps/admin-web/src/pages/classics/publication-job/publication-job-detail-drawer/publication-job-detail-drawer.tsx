import { Descriptions } from "antd";
import { useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import { KuzhambuAlert, KuzhambuButton, KuzhambuDrawer, KuzhambuTag } from "@/components";
import {
    PUBLICATION_JOB_RESULT_TAG_TYPES,
    readPublicationCleanupStatusLabel,
    readPublicationContentTypeLabel,
    readPublicationJobResultLabel,
    readPublicationJobStatusLabel,
    readPublicationJobTypeLabel,
    readPublicationLifecycleStatusLabel
} from "@/pages/classics/publication-job/publication-job-constants";
import * as service from "@/pages/classics/publication-job/publication-job-service";

import "./publication-job-detail-drawer.css";

const formatTime = (value?: string | null) =>
    value ? dayjs(value).format("YYYY-MM-DD HH:mm:ss") : "-";

interface PublicationJobDetailDrawerProps {
    jobId: string | null;
    open: boolean;
    onClose: () => void;
}

export const PublicationJobDetailDrawer = ({
    jobId,
    open,
    onClose
}: PublicationJobDetailDrawerProps) => {
    const detailQuery = useQuery({
        queryKey: ["classics", "publication-jobs", "detail", jobId],
        queryFn: () => service.get({ id: jobId || "" }),
        enabled: open && jobId !== null,
        retry: false
    });
    const job = detailQuery.data;

    return (
        <KuzhambuDrawer
            testId="classics-publication-jobs-detail-drawer"
            aria-label="发布任务详情"
            destroyOnHidden
            loading={detailQuery.isLoading}
            open={open}
            size="large"
            title="发布任务详情"
            onClose={onClose}
        >
            {detailQuery.isError ? (
                <KuzhambuAlert
                    showIcon
                    type="error"
                    title="发布任务详情加载失败"
                    description={
                        detailQuery.error instanceof Error
                            ? detailQuery.error.message
                            : "请稍后重试"
                    }
                    action={
                        <KuzhambuButton
                            ariaLabel="重试加载发布任务详情"
                            testId="classics-publication-job-detail-retry-button"
                            onClick={() => void detailQuery.refetch()}
                        >
                            重试
                        </KuzhambuButton>
                    }
                />
            ) : null}
            {job ? (
                <Descriptions column={2} size="small">
                    <Descriptions.Item label="任务 ID">{job.id}</Descriptions.Item>
                    <Descriptions.Item label="稿件 ID">{job.contentId}</Descriptions.Item>
                    <Descriptions.Item label="稿件类型">
                        {readPublicationContentTypeLabel(job.contentType)}
                    </Descriptions.Item>
                    <Descriptions.Item label="稿件标题">
                        {job.contentTitleSnapshot || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="任务动作">
                        {readPublicationJobTypeLabel(job.jobType)}
                    </Descriptions.Item>
                    <Descriptions.Item label="任务结果">
                        <KuzhambuTag type={PUBLICATION_JOB_RESULT_TAG_TYPES[job.jobResultStatus]}>
                            {readPublicationJobResultLabel(job.jobResultStatus)}
                        </KuzhambuTag>
                    </Descriptions.Item>
                    <Descriptions.Item label="当前里程碑">
                        <KuzhambuTag type="info">
                            {readPublicationJobStatusLabel(job.jobStatus)}
                        </KuzhambuTag>
                    </Descriptions.Item>
                    <Descriptions.Item label="失败步骤">
                        {job.failureStep ? readPublicationJobStatusLabel(job.failureStep) : "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="生命周期">
                        {readPublicationLifecycleStatusLabel(job.sourceLifecycleStatus)} →{" "}
                        {readPublicationLifecycleStatusLabel(job.targetLifecycleStatus)}
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
                    <Descriptions.Item label="租约到期">
                        {formatTime(job.expiresAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="正文删除时间">
                        {formatTime(job.contentDeletedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="ES 文档">{job.esDocumentId || "-"}</Descriptions.Item>
                    <Descriptions.Item label="ES 清理">
                        {readPublicationCleanupStatusLabel(job.esCleanupStatus)}
                    </Descriptions.Item>
                    <Descriptions.Item label="FastGPT 集合">
                        {job.fastgptCollectionId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="FastGPT 清理">
                        {readPublicationCleanupStatusLabel(job.fastgptCleanupStatus)}
                    </Descriptions.Item>
                    <Descriptions.Item label="请求时间">
                        {formatTime(job.requestedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="开始时间">
                        {formatTime(job.startedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="完成时间">
                        {formatTime(job.finishedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="失败原因">
                        {job.failureReason || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="诊断摘要" span={2}>
                        <pre className="publication-jobs-detail-summary">
                            {job.detailJsonSummary || "-"}
                        </pre>
                    </Descriptions.Item>
                </Descriptions>
            ) : null}
        </KuzhambuDrawer>
    );
};
