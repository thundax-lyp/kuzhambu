import type { ClassicsPublicationJobStatus } from "./publication-jobs-types";

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
