import {
    CloseCircleOutlined,
    DeleteOutlined,
    LoadingOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Descriptions, Statistic, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuDrawer,
    KuzhambuPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTag,
    KuzhambuCard
} from "@/components";
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
    { label: "过期导出", value: "EXPIRED_EXPORT" },
    { label: "过期分享", value: "EXPIRED_SHARE" },
    { label: "过期草稿", value: "EXPIRED_DRAFT" },
    { label: "过期报表", value: "EXPIRED_REPORT" },
    { label: "过期健康检查", value: "EXPIRED_HEALTH_CHECK" },
    { label: "过期长任务快照", value: "EXPIRED_LONG_TASK" }
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

const failureReasonText = (value?: string | null) => {
    return value || "未返回失败原因";
};

const executorText = (requesterUserId?: string | null) => {
    return requesterUserId == null ? "系统自动" : requesterUserId;
};

const triggerSourceText = (requesterUserId?: string | null) => {
    return requesterUserId == null ? "系统自动" : `人工执行：${requesterUserId}`;
};

const buildAlertPath = (cleanupId?: string | null) => {
    if (!cleanupId) {
        return "/operations/dashboard";
    }
    return `/operations/dashboard?sourceRefType=CLEANUP&sourceRefId=${cleanupId}`;
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
    const [detailCleanupId, setDetailCleanupId] = useState<string | null>(null);
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
        queryKey: ["operations", "cleanup", "detail", detailCleanupId],
        queryFn: () => service.getCleanupDetail({ cleanupId: detailCleanupId ?? "" }),
        enabled: detailCleanupId !== null,
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
    const detailCleanupRecord = cleanupDetailQuery.data;
    const detailItems = detailCleanupRecord?.items || [];
    const visibleDetailItems = showFailureItems
        ? detailItems.filter((item) => item.itemStatus === "FAILED")
        : detailItems;
    const cleanupDetailDrawerOpen = detailCleanupId !== null;

    const openCleanupDetailDrawer = (cleanupId: string, shouldShowFailureItems: boolean) => {
        setDetailCleanupId(cleanupId);
        setShowFailureItems(shouldShowFailureItems);
    };

    const closeCleanupDetailDrawer = () => {
        setDetailCleanupId(null);
        setShowFailureItems(false);
    };

    const executeCleanup = (cleanupType: string) => {
        confirm.danger({
            title: "执行清理任务",
            message: `确认人工补偿执行 ${cleanupType} 清理吗？`,
            description: "自动调度是主路径，本操作仅用于人工补偿；执行中请勿重复提交。",
            okText: "确认执行",
            onConfirm: () => executeMutation.mutateAsync({ cleanupType })
        });
    };

    return (
        <KuzhambuPage
            className="cleanup-page operations-cleanup-page"
            description="支持手动触发清理任务，并查看清理台账、详情与失败项。"
            title="清理任务台账"
        >
            <section>
                <div className="operations-cleanup-summary">
                    <KuzhambuCard className="operations-cleanup-summary-card">
                        <Statistic
                            title="清理任务总数"
                            prefix={<ReloadOutlined />}
                            value={totalCount}
                        />
                        <Text className="operations-cleanup-description" type="secondary">
                            当前查询页共 {totalCount} 条清理记录。
                        </Text>
                    </KuzhambuCard>
                    <KuzhambuCard className="operations-cleanup-summary-card">
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
                    </KuzhambuCard>
                    <KuzhambuCard className="operations-cleanup-summary-card">
                        <Statistic
                            title="最近一次任务"
                            value={latestRecord?.cleanupId || "-"}
                            prefix={<CloseCircleOutlined />}
                        />
                        <Text className="operations-cleanup-description" type="secondary">
                            最近一次状态：
                            <KuzhambuTag type={statusTone(latestRecord?.cleanupStatus)}>
                                {latestRecord?.cleanupStatus || "UNKNOWN"}
                            </KuzhambuTag>
                        </Text>
                    </KuzhambuCard>
                </div>
            </section>

            <section>
                <KuzhambuCard
                    className="operations-cleanup-section-card"
                    title="清理任务查询"
                    size="small"
                >
                    <KuzhambuSpace className="operations-cleanup-filters" size={12} wrap>
                        <label className="operations-cleanup-filter">
                            <Text type="secondary">清理类型</Text>
                            <KuzhambuSelect
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
                            <KuzhambuSelect
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
                            <KuzhambuButton
                                testId="operations-cleanup-cleanup-refresh-button"
                                icon={<ReloadOutlined />}
                                onClick={() => void refreshPage()}
                            >
                                刷新
                            </KuzhambuButton>
                            {canExecuteCleanup ? (
                                <KuzhambuSelect
                                    aria-label="执行清理类型"
                                    style={{ width: 220 }}
                                    onSelect={(value) => executeCleanup(value as string)}
                                    options={cleanupTypeOptions.filter(
                                        (option) => option.value !== "ALL"
                                    )}
                                    placeholder="选择清理类型并立即执行一次"
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
                                <th>失败提示</th>
                                <th>执行人</th>
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
                                                    type={statusTone(record.cleanupStatus)}
                                                >
                                                    {record.cleanupStatus || "UNKNOWN"}
                                                </KuzhambuTag>
                                            </td>
                                            <td>{record.totalCount ?? "-"}</td>
                                            <td>{record.successCount ?? "-"}</td>
                                            <td>{record.failedCount ?? "-"}</td>
                                            <td>
                                                {record.cleanupStatus === "FAILED" ? (
                                                    <KuzhambuSpace orientation="vertical" size={4}>
                                                        <Text type="danger">
                                                            {failureReasonText(
                                                                record.failureReason
                                                            )}
                                                        </Text>
                                                        <KuzhambuButton
                                                            testId="operations-cleanup-cleanup-view-alerts-button"
                                                            href={buildAlertPath(record.cleanupId)}
                                                            size="small"
                                                        >
                                                            查看告警
                                                        </KuzhambuButton>
                                                    </KuzhambuSpace>
                                                ) : null}
                                            </td>
                                            <td>{executorText(record.requesterUserId)}</td>
                                            <td>{formatDateTime(record.startedAt)}</td>
                                            <td>{formatDateTime(record.completedAt)}</td>
                                            <td>
                                                <KuzhambuSpace size={8} wrap>
                                                    <KuzhambuButton
                                                        testId="operations-cleanup-cleanup-detail-button"
                                                        icon={<LoadingOutlined />}
                                                        onClick={() =>
                                                            openCleanupDetailDrawer(
                                                                record.cleanupId,
                                                                false
                                                            )
                                                        }
                                                        size="small"
                                                    >
                                                        详情
                                                    </KuzhambuButton>
                                                    {hasFailure ? (
                                                        <KuzhambuButton
                                                            testId="operations-cleanup-cleanup-action-button"
                                                            danger
                                                            icon={<DeleteOutlined />}
                                                            onClick={() =>
                                                                openCleanupDetailDrawer(
                                                                    record.cleanupId,
                                                                    true
                                                                )
                                                            }
                                                            size="small"
                                                        >
                                                            失败项
                                                        </KuzhambuButton>
                                                    ) : null}
                                                </KuzhambuSpace>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td className="operations-cleanup-empty-cell" colSpan={11}>
                                        暂无清理任务
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>

                    <div className="operations-cleanup-pagination">
                        <KuzhambuButton
                            testId="operations-cleanup-cleanup-previous-page-button"
                            disabled={pageNo <= DEFAULT_PAGE_NO}
                            onClick={() => setPageNo((currentPage) => currentPage - 1)}
                        >
                            上一页
                        </KuzhambuButton>
                        <Text type="secondary">
                            第 {pageNo} / {totalPage} 页，共 {totalCount} 条
                        </Text>
                        <KuzhambuButton
                            testId="operations-cleanup-cleanup-next-page-button"
                            disabled={pageNo >= totalPage}
                            onClick={() => setPageNo((currentPage) => currentPage + 1)}
                        >
                            下一页
                        </KuzhambuButton>
                    </div>
                </KuzhambuCard>
            </section>

            <KuzhambuDrawer
                testId="operations-cleanup-cleanup-drawer"
                loading={cleanupDetailQuery.isLoading}
                onClose={closeCleanupDetailDrawer}
                open={cleanupDetailDrawerOpen}
                size="middle"
                title={showFailureItems ? "清理失败项" : "清理任务详情"}
            >
                {detailCleanupRecord ? (
                    <div className="operations-cleanup-detail">
                        <Descriptions
                            bordered
                            column={1}
                            items={[
                                {
                                    key: "cleanupId",
                                    label: "清理 ID",
                                    children: detailCleanupRecord.cleanupId
                                },
                                {
                                    key: "cleanupType",
                                    label: "清理类型",
                                    children: detailCleanupRecord.cleanupType || "-"
                                },
                                {
                                    key: "cleanupStatus",
                                    label: "任务状态",
                                    children: (
                                        <KuzhambuTag
                                            type={statusTone(detailCleanupRecord.cleanupStatus)}
                                        >
                                            {detailCleanupRecord.cleanupStatus || "UNKNOWN"}
                                        </KuzhambuTag>
                                    )
                                },
                                {
                                    key: "triggerSource",
                                    label: "触发来源",
                                    children: triggerSourceText(detailCleanupRecord.requesterUserId)
                                },
                                {
                                    key: "totalCount",
                                    label: "处理总量",
                                    children: detailCleanupRecord.totalCount ?? "-"
                                },
                                {
                                    key: "successCount",
                                    label: "成功数量",
                                    children: detailCleanupRecord.successCount ?? "-"
                                },
                                {
                                    key: "failedCount",
                                    label: "失败数量",
                                    children: detailCleanupRecord.failedCount ?? "-"
                                },
                                {
                                    key: "startedAt",
                                    label: "开始时间",
                                    children: formatDateTime(detailCleanupRecord.startedAt)
                                },
                                {
                                    key: "completedAt",
                                    label: "完成时间",
                                    children: formatDateTime(detailCleanupRecord.completedAt)
                                }
                            ]}
                        />

                        {detailCleanupRecord.cleanupStatus === "FAILED" ? (
                            <KuzhambuAlert
                                action={
                                    <KuzhambuButton
                                        testId="operations-cleanup-cleanup-view-alerts-button-2"
                                        href={buildAlertPath(detailCleanupRecord.cleanupId)}
                                        size="small"
                                    >
                                        查看告警
                                    </KuzhambuButton>
                                }
                                description={`${failureReasonText(detailCleanupRecord.failureReason)}。请检查清理目标和失败项明细，必要时重新发起业务动作。`}
                                title="清理任务执行失败"
                                showIcon
                                type="warning"
                            />
                        ) : null}

                        {showFailureItems ? (
                            <div className="operations-cleanup-failure">
                                <Title level={5}>失败项</Title>
                                <Text type="secondary">
                                    当前任务共 {detailCleanupRecord.failedCount || 0} 条失败项。
                                </Text>
                            </div>
                        ) : null}

                        <div className="operations-cleanup-items">
                            <Title level={5}>
                                {showFailureItems ? "失败项明细" : "清理项明细"}
                            </Title>
                            <table className="operations-cleanup-table operations-cleanup-items-table">
                                <thead>
                                    <tr>
                                        <th>明细 ID</th>
                                        <th>目标类型</th>
                                        <th>目标 ID</th>
                                        <th>状态</th>
                                        <th>处理时间</th>
                                        <th>失败原因</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {visibleDetailItems.length ? (
                                        visibleDetailItems.map((item, index) => (
                                            <tr
                                                key={
                                                    item.cleanupItemId ||
                                                    `${item.targetType}-${item.targetId}-${index}`
                                                }
                                            >
                                                <td>{item.cleanupItemId || "-"}</td>
                                                <td>{item.targetType || "-"}</td>
                                                <td>{item.targetId ?? "-"}</td>
                                                <td>
                                                    <KuzhambuTag type={statusTone(item.itemStatus)}>
                                                        {item.itemStatus || "UNKNOWN"}
                                                    </KuzhambuTag>
                                                </td>
                                                <td>{formatDateTime(item.processedAt)}</td>
                                                <td>
                                                    {item.itemStatus === "FAILED" ? (
                                                        <KuzhambuAlert
                                                            description={failureReasonText(
                                                                item.failureReason
                                                            )}
                                                            title="清理项失败"
                                                            showIcon
                                                            type="warning"
                                                        />
                                                    ) : (
                                                        item.failureReason || "-"
                                                    )}
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td
                                                className="operations-cleanup-empty-cell"
                                                colSpan={6}
                                            >
                                                {showFailureItems ? "暂无失败项" : "暂无清理项"}
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>

                        {detailCleanupRecord.failureReason ? (
                            <div className="operations-cleanup-failure">
                                <Title level={5}>任务失败原因</Title>
                                <pre>{detailCleanupRecord.failureReason}</pre>
                            </div>
                        ) : null}
                    </div>
                ) : null}
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
