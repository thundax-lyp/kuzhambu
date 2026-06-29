import {
    CloseCircleOutlined,
    DeleteOutlined,
    LoadingOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Card, Descriptions, Select, Statistic, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./cleanup-service";
import type { CleanupExecuteCommand, CleanupPageQuery } from "./cleanup-service";
import type { OperationsCleanupRecord } from "./cleanup-types";
import "./cleanup-page.css";

const { Text, Title } = Typography;

const statusTone = (status?: string | null) => {
    if (status === "SUCCEEDED") {
        return "success";
    }
    if (status === "FAILED") {
        return "danger";
    }
    if (status === "RUNNING") {
        return "warning";
    }
    return "neutral";
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

const cleanupTypeOptions = [
    { label: "全部类型", value: "ALL" },
    { label: "过期备份", value: "EXPIRED_BACKUP" },
    { label: "过期分享", value: "EXPIRED_SHARE" },
    { label: "过期草稿", value: "EXPIRED_DRAFT" },
    { label: "过期导出产物", value: "EXPIRED_EXPORT" }
];

const cleanupStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "进行中", value: "RUNNING" },
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" }
];

const normalizeFilterValue = (value: string) => {
    return value === "ALL" ? undefined : value;
};

const countByStatus = (records: OperationsCleanupRecord[], status: string) => {
    return records.filter((record) => record.cleanupStatus === status).length;
};

export const CleanupPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canExecuteCleanup = hasPermission("operations:cleanup:execute");
    const canViewCleanup = hasPermission("operations:cleanup:view");
    const [filter, setFilter] = useState<CleanupPageQuery>({
        cleanupType: undefined,
        cleanupStatus: undefined
    });
    const [pageNo, setPageNo] = useState(DEFAULT_PAGE_NO);
    const [selectedCleanupId, setSelectedCleanupId] = useState<number | null>(null);
    const [showFailureItems, setShowFailureItems] = useState(false);

    const cleanupPageQuery = useQuery({
        queryKey: ["operations", "cleanup", "page", filter, pageNo],
        queryFn: () =>
            service.pageCleanups({
                ...filter,
                pageNo,
                pageSize: DEFAULT_PAGE_SIZE
            }),
        enabled: canViewCleanup,
        retry: false
    });

    const cleanupDetailQuery = useQuery({
        queryKey: ["operations", "cleanup", "detail", selectedCleanupId],
        queryFn: () => service.getCleanupDetail({ cleanupId: selectedCleanupId as number }),
        enabled: selectedCleanupId !== null,
        retry: false
    });

    const refreshPage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["operations", "cleanup", "page"] });
    };

    const executeMutation = useMutation({
        mutationFn: (command: CleanupExecuteCommand) => service.requestCleanup(command),
        onSuccess: async (result) => {
            await refreshPage();
            if (result.cleanupStatus === "FAILED") {
                messageApi.error(result.failureReason || "清理任务执行失败");
                return;
            }
            messageApi.success(`清理任务 ${result.cleanupType || ""} 已完成`);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "清理任务执行失败");
        }
    });

    const cleanupPage = cleanupPageQuery.data;
    const cleanupRecords: OperationsCleanupRecord[] = cleanupPage?.records || [];
    const totalCount = cleanupPage?.count || cleanupPage?.totalCount || 0;
    const totalPage = cleanupPage?.totalPage || 1;
    const latestRecord = cleanupRecords[0] ?? null;
    const detailRecord = cleanupDetailQuery.data;
    const canOpenDrawer = selectedCleanupId !== null;

    const openCleanupDetail = (cleanupId: number, shouldShowFailureItems: boolean) => {
        setSelectedCleanupId(cleanupId);
        setShowFailureItems(shouldShowFailureItems);
    };

    const closeDrawer = () => {
        setSelectedCleanupId(null);
        setShowFailureItems(false);
    };

    const executeCleanup = (cleanupType: string) => {
        confirm.danger({
            title: "执行清理任务",
            message: `确认执行 ${cleanupType} 清理吗？`,
            description: "清理任务会遍历目标数据并标记清理结果，执行中请勿重复提交。",
            okText: "立即执行",
            onConfirm: () => executeMutation.mutateAsync({ cleanupType })
        });
    };

    return (
        <KuzhambuPage
            className="cleanup-page operations-cleanup-page"
            description="支持手动触发清理任务，并查看清理台账、详情与失败项。"
            eyebrow="Operations"
            title="清理任务台账"
        >
            <section>
                <div className="operations-cleanup-summary">
                    <Card className="operations-cleanup-summary-card">
                        <Statistic
                            title="清理任务总数"
                            prefix={<ReloadOutlined />}
                            value={totalCount}
                        />
                        <Text className="operations-cleanup-description" type="secondary">
                            当前查询页共 {totalCount} 条清理记录。
                        </Text>
                    </Card>
                    <Card className="operations-cleanup-summary-card">
                        <Statistic
                            title="进行中/失败"
                            value={`${countByStatus(cleanupRecords, "RUNNING")} / ${countByStatus(
                                cleanupRecords,
                                "FAILED"
                            )}`}
                        />
                        <Text className="operations-cleanup-description" type="secondary">
                            成功 {countByStatus(cleanupRecords, "SUCCEEDED")} 次，失败{" "}
                            {countByStatus(cleanupRecords, "FAILED")} 次（当前页）。
                        </Text>
                    </Card>
                    <Card className="operations-cleanup-summary-card">
                        <Statistic
                            title="最近一次任务"
                            value={latestRecord?.cleanupId || "-"}
                            prefix={<CloseCircleOutlined />}
                        />
                        <Text className="operations-cleanup-description" type="secondary">
                            最近一次状态：
                            <KuzhambuTag tone={statusTone(latestRecord?.cleanupStatus)}>
                                {latestRecord?.cleanupStatus || "UNKNOWN"}
                            </KuzhambuTag>
                        </Text>
                    </Card>
                </div>
            </section>

            <section>
                <Card className="operations-cleanup-section-card" title="清理任务查询" size="small">
                    <KuzhambuSpace className="operations-cleanup-filters" size={12} wrap>
                        <label className="operations-cleanup-filter">
                            <Text type="secondary">清理类型</Text>
                            <Select
                                aria-label="清理类型"
                                style={{ width: 200 }}
                                options={cleanupTypeOptions}
                                value={filter.cleanupType || "ALL"}
                                onChange={(value) => {
                                    setPageNo(DEFAULT_PAGE_NO);
                                    setFilter((currentFilter) => ({
                                        ...currentFilter,
                                        cleanupType: normalizeFilterValue(value)
                                    }));
                                }}
                            />
                        </label>
                        <label className="operations-cleanup-filter">
                            <Text type="secondary">清理状态</Text>
                            <Select
                                aria-label="清理状态"
                                style={{ width: 140 }}
                                options={cleanupStatusOptions}
                                value={filter.cleanupStatus || "ALL"}
                                onChange={(value) => {
                                    setPageNo(DEFAULT_PAGE_NO);
                                    setFilter((currentFilter) => ({
                                        ...currentFilter,
                                        cleanupStatus: normalizeFilterValue(value)
                                    }));
                                }}
                            />
                        </label>
                        <div className="operations-cleanup-toolbar-actions">
                            <Button icon={<ReloadOutlined />} onClick={() => void refreshPage()}>
                                刷新
                            </Button>
                            {canExecuteCleanup ? (
                                <Select
                                    aria-label="执行清理类型"
                                    style={{ width: 220 }}
                                    onSelect={(value) => executeCleanup(value as string)}
                                    options={cleanupTypeOptions.filter(
                                        (option) => option.value !== "ALL"
                                    )}
                                    placeholder="选择清理类型并执行"
                                    allowClear
                                />
                            ) : null}
                        </div>
                    </KuzhambuSpace>

                    <table className="operations-cleanup-table">
                        <thead>
                            <tr>
                                <th>清理 ID</th>
                                <th>类型</th>
                                <th>状态</th>
                                <th>总量</th>
                                <th>成功</th>
                                <th>失败</th>
                                <th>开始时间</th>
                                <th>完成时间</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            {cleanupRecords.length ? (
                                cleanupRecords.map((record) => {
                                    const hasFailure = (record.failedCount ?? 0) > 0;
                                    return (
                                        <tr key={record.cleanupId}>
                                            <td>{record.cleanupId}</td>
                                            <td>{record.cleanupType || "-"}</td>
                                            <td>
                                                <KuzhambuTag
                                                    tone={statusTone(record.cleanupStatus)}
                                                >
                                                    {record.cleanupStatus || "UNKNOWN"}
                                                </KuzhambuTag>
                                            </td>
                                            <td>{record.totalCount ?? "-"}</td>
                                            <td>{record.successCount ?? "-"}</td>
                                            <td>{record.failedCount ?? "-"}</td>
                                            <td>{formatDateTime(record.startedAt)}</td>
                                            <td>{formatDateTime(record.completedAt)}</td>
                                            <td>
                                                <KuzhambuSpace size={8} wrap>
                                                    <Button
                                                        aria-label="详情"
                                                        icon={<LoadingOutlined />}
                                                        onClick={() =>
                                                            openCleanupDetail(
                                                                record.cleanupId,
                                                                false
                                                            )
                                                        }
                                                        size="small"
                                                    >
                                                        详情
                                                    </Button>
                                                    {hasFailure ? (
                                                        <Button
                                                            danger
                                                            aria-label="失败项"
                                                            icon={<DeleteOutlined />}
                                                            onClick={() =>
                                                                openCleanupDetail(
                                                                    record.cleanupId,
                                                                    true
                                                                )
                                                            }
                                                            size="small"
                                                        >
                                                            失败项
                                                        </Button>
                                                    ) : null}
                                                </KuzhambuSpace>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td className="operations-cleanup-empty-cell" colSpan={9}>
                                        暂无清理任务
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>

                    <div className="operations-cleanup-pagination">
                        <Button
                            disabled={pageNo <= DEFAULT_PAGE_NO}
                            onClick={() => setPageNo((currentPage) => currentPage - 1)}
                        >
                            上一页
                        </Button>
                        <Text type="secondary">
                            第 {pageNo} / {totalPage} 页，共 {totalCount} 条
                        </Text>
                        <Button
                            disabled={pageNo >= totalPage}
                            onClick={() => setPageNo((currentPage) => currentPage + 1)}
                        >
                            下一页
                        </Button>
                    </div>
                </Card>
            </section>

            <KuzhambuDrawer
                loading={cleanupDetailQuery.isLoading}
                onClose={closeDrawer}
                open={canOpenDrawer}
                size="middle"
                title={showFailureItems ? "清理失败项" : "清理任务详情"}
            >
                {detailRecord ? (
                    <div className="operations-cleanup-detail">
                        <Descriptions
                            bordered
                            column={1}
                            items={[
                                {
                                    key: "cleanupId",
                                    label: "清理 ID",
                                    children: detailRecord.cleanupId
                                },
                                {
                                    key: "cleanupType",
                                    label: "清理类型",
                                    children: detailRecord.cleanupType || "-"
                                },
                                {
                                    key: "cleanupStatus",
                                    label: "任务状态",
                                    children: (
                                        <KuzhambuTag tone={statusTone(detailRecord.cleanupStatus)}>
                                            {detailRecord.cleanupStatus || "UNKNOWN"}
                                        </KuzhambuTag>
                                    )
                                },
                                {
                                    key: "totalCount",
                                    label: "处理总量",
                                    children: detailRecord.totalCount ?? "-"
                                },
                                {
                                    key: "successCount",
                                    label: "成功数量",
                                    children: detailRecord.successCount ?? "-"
                                },
                                {
                                    key: "failedCount",
                                    label: "失败数量",
                                    children: detailRecord.failedCount ?? "-"
                                },
                                {
                                    key: "startedAt",
                                    label: "开始时间",
                                    children: formatDateTime(detailRecord.startedAt)
                                },
                                {
                                    key: "completedAt",
                                    label: "完成时间",
                                    children: formatDateTime(detailRecord.completedAt)
                                }
                            ]}
                        />

                        {showFailureItems ? (
                            <div className="operations-cleanup-failure">
                                <Title level={5}>
                                    失败项
                                    <Text type="secondary">（后端当前仅聚合失败原因）</Text>
                                </Title>
                                <Text type="secondary">
                                    {detailRecord.failedCount || 0} 条失败项
                                </Text>
                                <pre>{detailRecord.failureReason || "暂无失败原因"}</pre>
                            </div>
                        ) : null}
                    </div>
                ) : null}
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
