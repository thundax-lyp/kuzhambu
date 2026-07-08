import { Button, Checkbox, Empty, Input, Select, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
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

const matchesKeyword = (job: ClassicsShowcaseJobRecord, keyword: string) => {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) {
        return true;
    }
    return [job.id == null ? "" : String(job.id), job.status, job.visibilityRiskStatus]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(normalized));
};

export interface ClassicsShowcaseJobSectionProps {
    items: ClassicsShowcaseJobRecord[];
    loading?: boolean;
    onDownload: (job: ClassicsShowcaseJobRecord) => void;
    onDelete?: (job: ClassicsShowcaseJobRecord) => void;
    onBatchDelete?: (jobs: ClassicsShowcaseJobRecord[]) => void;
    onRefresh?: () => void;
    sectionTitle?: string;
    sectionDescription?: string;
}

export const ClassicsShowcaseJobSection = ({
    items,
    loading = false,
    onDownload,
    onDelete,
    onBatchDelete,
    onRefresh,
    sectionTitle = "静态展示任务",
    sectionDescription = "暂无静态展示任务"
}: ClassicsShowcaseJobSectionProps) => {
    const [keyword, setKeyword] = useState("");
    const [status, setStatus] = useState<string>("ALL");
    const [selectedIds, setSelectedIds] = useState<number[]>([]);
    const visibleItems = useMemo(
        () =>
            items.filter((job) => {
                if (!matchesKeyword(job, keyword)) {
                    return false;
                }
                if (status !== "ALL" && job.status !== status) {
                    return false;
                }
                return true;
            }),
        [items, keyword, status]
    );
    const selectedJobs = visibleItems.filter(
        (job) => job.id != null && selectedIds.includes(job.id)
    );
    const selectableIds = visibleItems
        .map((job) => job.id)
        .filter((id): id is number => id != null);
    const allSelected = selectableIds.length > 0 && selectedJobs.length === selectableIds.length;

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
            <KuzhambuSpace className="classics-showcase-job-section-filters" size={8} wrap>
                <Input.Search
                    allowClear
                    aria-label="搜索静态展示任务"
                    placeholder="搜索任务编号或风险状态"
                    value={keyword}
                    onChange={(event) => setKeyword(event.target.value)}
                    onSearch={setKeyword}
                    style={{ width: 240 }}
                />
                <Select
                    aria-label="筛选静态展示任务状态"
                    value={status}
                    options={[
                        { label: "全部状态", value: "ALL" },
                        { label: "已完成", value: "COMPLETED" },
                        { label: "排队中", value: "REQUESTED" },
                        { label: "进行中", value: "PROCESSING" },
                        { label: "失败", value: "FAILED" },
                        { label: "已过期", value: "EXPIRED" }
                    ]}
                    onChange={setStatus}
                    style={{ width: 136 }}
                />
                {onBatchDelete ? (
                    <Button
                        danger
                        disabled={!selectedJobs.length}
                        size="small"
                        onClick={() => onBatchDelete(selectedJobs)}
                    >
                        删除选中
                    </Button>
                ) : null}
            </KuzhambuSpace>
            <KuzhambuList
                size="small"
                dataSource={visibleItems}
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
                                    {onBatchDelete && job.id != null ? (
                                        <Checkbox
                                            aria-label={`选择静态展示任务 ${job.id}`}
                                            checked={selectedIds.includes(job.id)}
                                            onChange={(event) => {
                                                setSelectedIds((current) =>
                                                    event.target.checked
                                                        ? Array.from(
                                                              new Set([
                                                                  ...current,
                                                                  job.id as number
                                                              ])
                                                          )
                                                        : current.filter((id) => id !== job.id)
                                                );
                                            }}
                                        />
                                    ) : null}
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
                                    {onDelete ? (
                                        <Button danger size="small" onClick={() => onDelete(job)}>
                                            删除
                                        </Button>
                                    ) : null}
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
            {onBatchDelete && selectableIds.length ? (
                <div className="classics-showcase-job-section-select-all">
                    <Checkbox
                        aria-label="选择全部可见静态展示任务"
                        checked={allSelected}
                        indeterminate={selectedJobs.length > 0 && !allSelected}
                        onChange={(event) => {
                            setSelectedIds(event.target.checked ? selectableIds : []);
                        }}
                    >
                        已选 {selectedJobs.length} / {selectableIds.length}
                    </Checkbox>
                </div>
            ) : null}
        </section>
    );
};
