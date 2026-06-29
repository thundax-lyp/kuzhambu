import { Button, Empty, Tag, Typography } from "antd";
import { KuzhambuList, KuzhambuListItem, KuzhambuListMeta } from "@/components/kuzhambu-list";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import "./classics-export-job-section.css";
import type { ClassicsExportJobRecord } from "../classics-export-types";

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

const exportStatusTagType = (status?: string | null) => {
    switch (status) {
        case "COMPLETED":
            return "success";
        case "RUNNING":
        case "REQUESTED":
            return "processing";
        case "FAILED":
            return "error";
        case "EXPIRED":
            return "warning";
        default:
            return "default";
    }
};

const isExpired = (expiresAt?: string | null) => {
    if (!expiresAt) {
        return false;
    }
    return Number.isNaN(Date.parse(expiresAt)) ? false : Date.parse(expiresAt) <= Date.now();
};

const isDownloadableExport = (job: ClassicsExportJobRecord) => {
    return job.status === "COMPLETED" && Boolean(job.downloadUrl) && !isExpired(job.expiresAt);
};

const renderExportStatus = (status?: string | null, expiresAt?: string | null) => {
    const normalized = status || "UNKNOWN";
    if (isExpired(expiresAt)) {
        return <Tag color="warning">已过期</Tag>;
    }
    return (
        <Tag color={exportStatusTagType(normalized)}>
            {{
                COMPLETED: "已完成",
                REQUESTED: "排队中",
                RUNNING: "进行中",
                FAILED: "失败",
                EXPIRED: "已过期"
            }[normalized] || normalized}
        </Tag>
    );
};

const renderExportDescription = (job: ClassicsExportJobRecord) => {
    return `条目数：${job.itemCount ?? 0} | 资产数：${job.assetCount ?? 0}`;
};

export interface ClassicsExportJobSectionProps {
    loading?: boolean;
    onDownload: (job: ClassicsExportJobRecord) => void;
    onRefresh?: () => void;
    sectionTitle?: string;
    sectionDescription?: string;
    items: ClassicsExportJobRecord[];
}

export const ClassicsExportJobSection = ({
    items,
    loading = false,
    onDownload,
    onRefresh,
    sectionTitle = "导出任务",
    sectionDescription = "暂无导出任务"
}: ClassicsExportJobSectionProps) => {
    return (
        <section className="classics-export-job-section">
            <KuzhambuSpace
                align="center"
                className="classics-export-job-section-head"
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
                    const expired = isExpired(job.expiresAt);
                    const downloadable = isDownloadableExport(job);
                    const statusText = expired ? "已过期" : formatDateTime(job.requestedAt);
                    return (
                        <KuzhambuListItem
                            key={job.id ?? `classics-export-job-${job.requestedAt}`}
                            extra={
                                <KuzhambuSpace size={8} wrap>
                                    {renderExportStatus(job.status, job.expiresAt)}
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
                                description={`${statusText} | ${renderExportDescription(job)}`}
                            />
                        </KuzhambuListItem>
                    );
                }}
            />
        </section>
    );
};
