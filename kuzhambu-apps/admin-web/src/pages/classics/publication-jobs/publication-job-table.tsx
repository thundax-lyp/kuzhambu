import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { KuzhambuButton, KuzhambuTag } from "@/components";
import { readPublicationJobStatusLabel } from "@/pages/classics/publication-jobs/publication-job-labels";
import type { ClassicsPublicationJobRecord } from "@/pages/classics/publication-jobs/publication-jobs-types";

const JOB_TYPE_LABELS = { PUBLISH: "发布", OFFLINE: "下线" } as const;
const RESULT_LABELS = { RUNNING: "执行中", FAILED: "失败", SUCCEEDED: "成功" } as const;
const RESULT_TYPES = { RUNNING: "info", FAILED: "danger", SUCCEEDED: "success" } as const;

const formatTime = (value?: string | null) =>
    value ? dayjs(value).format("YYYY-MM-DD HH:mm:ss") : "-";

interface PublicationJobTableOptions {
    onView: (job: ClassicsPublicationJobRecord) => void;
}

export const createPublicationJobTableColumns = ({
    onView
}: PublicationJobTableOptions): ColumnsType<ClassicsPublicationJobRecord> => [
    { title: "任务 ID", dataIndex: "id", width: 170 },
    {
        title: "稿件",
        key: "content",
        width: 220,
        render: (_, job) => job.contentTitleSnapshot || `${job.contentType} #${job.contentId}`
    },
    {
        title: "动作",
        dataIndex: "jobType",
        width: 80,
        render: (value: ClassicsPublicationJobRecord["jobType"]) => JOB_TYPE_LABELS[value]
    },
    {
        title: "结果",
        dataIndex: "jobResultStatus",
        width: 100,
        render: (value: ClassicsPublicationJobRecord["jobResultStatus"]) => (
            <KuzhambuTag type={RESULT_TYPES[value]}>{RESULT_LABELS[value]}</KuzhambuTag>
        )
    },
    {
        title: "里程碑",
        dataIndex: "jobStatus",
        width: 160,
        render: (value: ClassicsPublicationJobRecord["jobStatus"]) => (
            <KuzhambuTag type="info">{readPublicationJobStatusLabel(value)}</KuzhambuTag>
        )
    },
    {
        title: "尝试次数",
        key: "attempts",
        width: 100,
        render: (_, job) => `${job.attemptCount}/${job.maxAttempts}`
    },
    {
        title: "请求时间",
        dataIndex: "requestedAt",
        width: 170,
        render: formatTime
    },
    {
        title: "操作",
        key: "actions",
        fixed: "right",
        width: 90,
        render: (_, job) => (
            <KuzhambuButton
                testId="classics-publication-jobs-view-button"
                type="link"
                onClick={() => onView(job)}
            >
                查看
            </KuzhambuButton>
        )
    }
];
