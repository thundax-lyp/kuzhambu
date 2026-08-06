import type {
    ClassicsPublicationCleanupStatus,
    ClassicsPublicationContentType,
    ClassicsPublicationJobResultStatus,
    ClassicsPublicationJobStatus,
    ClassicsPublicationJobType,
    ClassicsPublicationLifecycleStatus
} from "./publication-job-types";

const PUBLICATION_JOB_TYPE_LABELS: Record<ClassicsPublicationJobType, string> = {
    PUBLISH: "发布",
    OFFLINE: "下线"
};

const PUBLICATION_JOB_RESULT_LABELS: Record<ClassicsPublicationJobResultStatus, string> = {
    RUNNING: "执行中",
    FAILED: "失败",
    SUCCEEDED: "成功"
};

export const PUBLICATION_JOB_RESULT_TAG_TYPES = {
    RUNNING: "info",
    FAILED: "danger",
    SUCCEEDED: "success"
} as const;

const PUBLICATION_CONTENT_TYPE_LABELS: Record<ClassicsPublicationContentType, string> = {
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王琪",
    MING_CUSTOMS: "明代风俗"
};

const PUBLICATION_LIFECYCLE_STATUS_LABELS: Record<ClassicsPublicationLifecycleStatus, string> = {
    DRAFT: "草稿",
    PUBLISHED: "已发布",
    OFFLINE: "已下线",
    ERROR: "异常"
};

const PUBLICATION_CLEANUP_STATUS_LABELS: Record<ClassicsPublicationCleanupStatus, string> = {
    NONE: "无需清理",
    PENDING: "等待清理",
    RUNNING: "清理中",
    FAILED: "清理失败",
    SUCCEEDED: "清理完成"
};

const PUBLICATION_JOB_STATUS_LABELS: Record<ClassicsPublicationJobStatus, string> = {
    QUEUED: "待处理",
    SNAPSHOT_READY: "内容已生成",
    ES_PREPARED: "搜索索引已写入",
    FASTGPT_PREPARED: "知识库已写入",
    ES_READY: "搜索索引已发布",
    FASTGPT_READY: "知识库已发布",
    ES_DISABLED: "搜索索引已下线",
    FASTGPT_DISABLED: "知识库已下线",
    CONTENT_COMMITTED: "已完成"
};

export const readPublicationJobStatusLabel = (status: ClassicsPublicationJobStatus) => {
    return PUBLICATION_JOB_STATUS_LABELS[status];
};

export const readPublicationJobTypeLabel = (type: ClassicsPublicationJobType) => {
    return PUBLICATION_JOB_TYPE_LABELS[type];
};

export const readPublicationJobResultLabel = (status: ClassicsPublicationJobResultStatus) => {
    return PUBLICATION_JOB_RESULT_LABELS[status];
};

export const readPublicationContentTypeLabel = (type: ClassicsPublicationContentType) => {
    return PUBLICATION_CONTENT_TYPE_LABELS[type];
};

export const readPublicationLifecycleStatusLabel = (status: ClassicsPublicationLifecycleStatus) => {
    return PUBLICATION_LIFECYCLE_STATUS_LABELS[status];
};

export const readPublicationCleanupStatusLabel = (status: ClassicsPublicationCleanupStatus) => {
    return PUBLICATION_CLEANUP_STATUS_LABELS[status];
};
