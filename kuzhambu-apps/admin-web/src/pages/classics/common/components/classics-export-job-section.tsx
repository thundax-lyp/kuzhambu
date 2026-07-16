import { Checkbox, Empty, Input, Select, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuList, KuzhambuListItem, KuzhambuListMeta } from "@/components/kuzhambu-list";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuButton } from "@/components/kuzhambu-button";
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

const matchesKeyword = (job: ClassicsExportJobRecord, keyword: string) => {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) {
        return true;
    }
    return [
        job.id == null ? "" : String(job.id),
        job.contentType,
        job.exportKind,
        job.exportFormat,
        job.scopeType,
        job.visibilityRiskStatus
    ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(normalized));
};

export interface ClassicsExportJobSectionProps {
    loading?: boolean;
    onDownload: (job: ClassicsExportJobRecord) => void;
    onDelete?: (job: ClassicsExportJobRecord) => void;
    onBatchDelete?: (jobs: ClassicsExportJobRecord[]) => void;
    onRefresh?: () => void;
    sectionTitle?: string;
    sectionDescription?: string;
    items: ClassicsExportJobRecord[];
}

export const ClassicsExportJobSection = ({
    items,
    loading = false,
    onDownload,
    onDelete,
    onBatchDelete,
    onRefresh,
    sectionTitle = "导出任务",
    sectionDescription = "暂无导出任务"
}: ClassicsExportJobSectionProps) => {
    const [keyword, setKeyword] = useState("");
    const [status, setStatus] = useState<string>("ALL");
    const [expiredOnly, setExpiredOnly] = useState(false);
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
                if (expiredOnly && !isExpired(job.expiresAt)) {
                    return false;
                }
                return true;
            }),
        [expiredOnly, items, keyword, status]
    );
    const selectedJobs = visibleItems.filter(
        (job) => job.id != null && selectedIds.includes(job.id)
    );
    const selectableIds = visibleItems
        .map((job) => job.id)
        .filter((id): id is number => id != null);
    const allSelected = selectableIds.length > 0 && selectedJobs.length === selectableIds.length;

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
                    <KuzhambuButton
                        name="刷新"
                        size="small"
                        type="link"
                        onClick={() => {
                            onRefresh();
                        }}
                    >
                        刷新
                    </KuzhambuButton>
                ) : null}
            </KuzhambuSpace>
            <KuzhambuSpace className="classics-export-job-section-filters" size={8} wrap>
                <Input.Search
                    allowClear
                    aria-label="搜索导出任务"
                    placeholder="搜索任务编号、类型或范围"
                    value={keyword}
                    onChange={(event) => setKeyword(event.target.value)}
                    onSearch={setKeyword}
                    style={{ width: 240 }}
                />
                <Select
                    aria-label="筛选导出任务状态"
                    value={status}
                    options={[
                        { label: "全部状态", value: "ALL" },
                        { label: "已完成", value: "COMPLETED" },
                        { label: "排队中", value: "REQUESTED" },
                        { label: "进行中", value: "RUNNING" },
                        { label: "失败", value: "FAILED" },
                        { label: "已过期", value: "EXPIRED" }
                    ]}
                    onChange={setStatus}
                    style={{ width: 136 }}
                />
                <Checkbox
                    checked={expiredOnly}
                    onChange={(event) => setExpiredOnly(event.target.checked)}
                >
                    仅过期
                </Checkbox>
                {onBatchDelete ? (
                    <KuzhambuButton
                        name="删除选中"
                        danger
                        disabled={!selectedJobs.length}
                        size="small"
                        onClick={() => onBatchDelete(selectedJobs)}
                    >
                        删除选中
                    </KuzhambuButton>
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
                    const expired = isExpired(job.expiresAt);
                    const downloadable = isDownloadableExport(job);
                    const statusText = expired ? "已过期" : formatDateTime(job.requestedAt);
                    return (
                        <KuzhambuListItem
                            key={job.id ?? `classics-export-job-${job.requestedAt}`}
                            extra={
                                <KuzhambuSpace size={8} wrap>
                                    {onBatchDelete && job.id != null ? (
                                        <Checkbox
                                            aria-label={`选择导出任务 ${job.id}`}
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
                                    {renderExportStatus(job.status, job.expiresAt)}
                                    {downloadable ? (
                                        <KuzhambuButton
                                            name="下载"
                                            size="small"
                                            type="primary"
                                            onClick={() => onDownload(job)}
                                        >
                                            下载
                                        </KuzhambuButton>
                                    ) : (
                                        <KuzhambuButton name="下载" size="small" disabled>
                                            下载
                                        </KuzhambuButton>
                                    )}
                                    {onDelete ? (
                                        <KuzhambuButton
                                            name="删除"
                                            danger
                                            size="small"
                                            onClick={() => onDelete(job)}
                                        >
                                            删除
                                        </KuzhambuButton>
                                    ) : null}
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
            {onBatchDelete && selectableIds.length ? (
                <div className="classics-export-job-section-select-all">
                    <Checkbox
                        aria-label="选择全部可见导出任务"
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
