import { Checkbox, Empty, Input, Select, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import {
    KuzhambuTable,
    type KuzhambuTableColumn,
    type KuzhambuTableRowActionOption
} from "@/components/kuzhambu-table";
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

interface ClassicsExportJobSectionProps {
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
    const columns = useMemo<KuzhambuTableColumn<ClassicsExportJobRecord>[]>(
        () => [
            {
                title: "任务",
                dataIndex: "id",
                key: "id",
                width: 104,
                render: (id?: number | null) => `#${id ?? "草稿"}`
            },
            {
                title: "状态",
                dataIndex: "status",
                key: "status",
                width: 104,
                render: (_status, job: ClassicsExportJobRecord) =>
                    renderExportStatus(job.status, job.expiresAt)
            },
            {
                title: "类型",
                dataIndex: "exportKind",
                key: "exportKind",
                width: 168,
                render: (_exportKind, job: ClassicsExportJobRecord) => (
                    <div className="classics-export-job-section-cell-stack">
                        <Text>{job.exportKind || "未设置"}</Text>
                        <Text type="secondary">{job.exportFormat || "未设置"}</Text>
                    </div>
                )
            },
            {
                title: "范围",
                dataIndex: "scopeType",
                key: "scopeType",
                width: 168,
                render: (_scopeType, job: ClassicsExportJobRecord) => (
                    <div className="classics-export-job-section-cell-stack">
                        <Text>{job.scopeType || "未设置"}</Text>
                        <Text type="secondary">
                            条目 {job.itemCount ?? 0} / 资产 {job.assetCount ?? 0}
                        </Text>
                    </div>
                )
            },
            {
                title: "请求时间",
                dataIndex: "requestedAt",
                key: "requestedAt",
                width: 168,
                render: (requestedAt?: string | null) => formatDateTime(requestedAt)
            },
            {
                title: "过期时间",
                dataIndex: "expiresAt",
                key: "expiresAt",
                width: 168,
                render: (expiresAt?: string | null) => formatDateTime(expiresAt)
            },
            {
                key: "actions",
                options: (job: ClassicsExportJobRecord) => {
                    const actions: KuzhambuTableRowActionOption<ClassicsExportJobRecord>[] = [
                        {
                            key: "download",
                            text: "下载",
                            ariaLabel: `下载导出任务 ${job.id ?? "草稿"}`,
                            testId: `classics-export-job-${job.id ?? "draft"}-download-button`,
                            disabled: !isDownloadableExport(job),
                            onClick: () => onDownload(job)
                        }
                    ];

                    if (onDelete) {
                        actions.push({
                            key: "delete",
                            text: "删除",
                            type: "danger",
                            ariaLabel: `删除导出任务 ${job.id ?? "草稿"}`,
                            testId: `classics-export-job-${job.id ?? "draft"}-delete-button`,
                            onClick: () => onDelete(job)
                        });
                    }

                    return actions;
                }
            }
        ],
        [onDelete, onDownload]
    );

    return (
        <section className="classics-export-job-section">
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
            </KuzhambuSpace>
            <KuzhambuTable<ClassicsExportJobRecord>
                ariaLabel={`${sectionTitle}表格`}
                rowKey={(job) => job.id ?? `classics-export-job-${job.requestedAt ?? "draft"}`}
                className="classics-export-job-section-table"
                columns={columns}
                dataSource={visibleItems}
                loading={loading}
                toolbar={{
                    leading: onBatchDelete ? (
                        <Text type="secondary">已选任务 {selectedJobs.length} 个</Text>
                    ) : undefined,
                    actions: [
                        ...(onRefresh
                            ? [
                                  {
                                      testId: "classics-common-classics-export-job-refresh-button",
                                      title: "刷新",
                                      action: () => {
                                          onRefresh();
                                      }
                                  }
                              ]
                            : []),
                        ...(onBatchDelete
                            ? [
                                  {
                                      testId: "classics-common-classics-export-job-delete-button",
                                      title: "删除",
                                      danger: true,
                                      disabled: !selectedJobs.length,
                                      action: () => onBatchDelete(selectedJobs)
                                  }
                              ]
                            : [])
                    ]
                }}
                locale={{
                    emptyText: (
                        <Empty
                            description={sectionDescription}
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    )
                }}
                pagination={false}
                rowSelection={
                    onBatchDelete
                        ? {
                              selectedRowKeys: selectedIds,
                              onChange: (keys) => setSelectedIds(keys.map((key) => Number(key))),
                              getCheckboxProps: (job) => ({
                                  disabled: job.id == null
                              })
                          }
                        : undefined
                }
            />
        </section>
    );
};
