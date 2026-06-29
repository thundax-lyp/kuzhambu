import { Button, Empty, Tag, Typography } from "antd";
import { KuzhambuList, KuzhambuListItem, KuzhambuListMeta } from "@/components/kuzhambu-list";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { ClassicsShowcaseJobRecord } from "../classics-export-types";
import "./classics-showcase-job-section.css";

const { Text } = Typography;

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");
    const second = String(date.getSeconds()).padStart(2, "0");
    return `${year}/${month}/${day} ${hour}:${minute}:${second}`;
};

const statusTagType = (status?: string | null) => {
    switch (status) {
        case "COMPLETED":
            return "success";
        case "REQUESTED":
        case "PROCESSING":
            return "processing";
        case "FAILED":
            return "error";
        case "EXPIRED":
            return "warning";
        default:
            return "default";
    }
};

const renderStatusText = (status?: string | null) => {
    const normalized = status || "UNKNOWN";
    return (
        {
            COMPLETED: "已完成",
            REQUESTED: "排队中",
            PROCESSING: "进行中",
            FAILED: "失败",
            EXPIRED: "已过期"
        }[normalized] || normalized
    );
};

const isDownloadableShowcase = (job: ClassicsShowcaseJobRecord) => {
    return job.status === "COMPLETED" && Boolean(job.downloadUrl);
};

export interface ClassicsShowcaseJobSectionProps {
    items: ClassicsShowcaseJobRecord[];
    loading?: boolean;
    onDownload: (job: ClassicsShowcaseJobRecord) => void;
    onRefresh?: () => void;
    sectionTitle?: string;
    sectionDescription?: string;
}

export const ClassicsShowcaseJobSection = ({
    items,
    loading = false,
    onDownload,
    onRefresh,
    sectionTitle = "静态展示任务",
    sectionDescription = "暂无静态展示任务"
}: ClassicsShowcaseJobSectionProps) => {
    return (
        <section className="classics-showcase-job-section">
            <KuzhambuSpace
                align="center"
                className="classics-showcase-job-section-head"
                size={12}
                wrap
            >
                <Text strong>{sectionTitle}</Text>
                {onRefresh ? (
                    <Button
                        size="small"
                        type="link"
                        onClick={() => {
                            onRefresh();
                        }}
                    >
                        刷新
                    </Button>
                ) : null}
            </KuzhambuSpace>
            <KuzhambuList
                size="small"
                dataSource={items}
                loading={loading}
                empty={
                    <Empty description={sectionDescription} image={Empty.PRESENTED_IMAGE_SIMPLE} />
                }
                renderItem={(job) => {
                    const downloadable = isDownloadableShowcase(job);
                    const statusText = formatDateTime(job.requestedAt);
                    return (
                        <KuzhambuListItem
                            key={job.id ?? `showcase-job-${job.requestedAt}`}
                            extra={
                                <KuzhambuSpace size={8} wrap>
                                    <Tag color={statusTagType(job.status)}>
                                        {renderStatusText(job.status)}
                                    </Tag>
                                    {downloadable ? (
                                        <Button
                                            size="small"
                                            type="primary"
                                            onClick={() => onDownload(job)}
                                        >
                                            下载
                                        </Button>
                                    ) : (
                                        <Button size="small" disabled>
                                            下载
                                        </Button>
                                    )}
                                </KuzhambuSpace>
                            }
                        >
                            <KuzhambuListMeta
                                title={`任务 #${job.id ?? "草稿"}`}
                                description={`${statusText} | 条目数：${job.entryCount ?? 0} | 风险：${job.visibilityRiskStatus || "未知"}`}
                            />
                        </KuzhambuListItem>
                    );
                }}
            />
        </section>
    );
};
