import type { ClassicsPublicationJobStatus } from "./publication-jobs-types";

const PUBLICATION_JOB_STATUS_LABELS: Record<ClassicsPublicationJobStatus, string> = {
    QUEUED: "待处理",
    SNAPSHOT_READY: "内容快照已生成",
    ES_PREPARED: "搜索索引已预备",
    FASTGPT_PREPARED: "知识库内容已写入",
    ES_READY: "搜索索引已发布",
    FASTGPT_READY: "知识库已启用",
    ES_DISABLED: "搜索索引已下线",
    FASTGPT_DISABLED: "知识库已禁用",
    CONTENT_COMMITTED: "稿件状态已回写"
};

export const readPublicationJobStatusLabel = (status: ClassicsPublicationJobStatus) => {
    return PUBLICATION_JOB_STATUS_LABELS[status];
};
