import {
    DatabaseOutlined,
    EyeOutlined,
    HistoryOutlined,
    PlayCircleOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    SyncOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Card, Descriptions, Select, Statistic, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./backup-restore-service";
import type { BackupLedgerQuery, RestoreLedgerQuery } from "./backup-restore-service";
import type { OperationsBackupRecord, OperationsRestoreRecord } from "./backup-restore-types";
import "./backup-restore-page.css";

const { Text, Title } = Typography;

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

const formatFileSize = (value?: number | null) => {
    if (typeof value !== "number" || !Number.isFinite(value) || value < 0) {
        return "-";
    }
    if (value < 1024) {
        return `${value} B`;
    }
    const units = ["KB", "MB", "GB", "TB"];
    let normalizedValue = value / 1024;
    let unitIndex = 0;
    while (normalizedValue >= 1024 && unitIndex < units.length - 1) {
        normalizedValue /= 1024;
        unitIndex += 1;
    }
    return `${normalizedValue.toFixed(normalizedValue >= 10 ? 1 : 2)} ${units[unitIndex]}`;
};

const backupStatusTone = (status?: string | null) => {
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

const restoreStatusTone = (status?: string | null) => {
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

const backupTypeOptions = [
    { label: "全部备份", value: "ALL" },
    { label: "手动备份", value: "MANUAL" },
    { label: "恢复前快照", value: "PRE_RESTORE" }
];

const ledgerStatusOptions = [
    { label: "全部状态", value: "ALL" },
    { label: "进行中", value: "RUNNING" },
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" }
];

const normalizeFilterValue = (value: string) => {
    return value === "ALL" ? undefined : value;
};

const countByStatus = (
    records: { backupStatus?: string | null; restoreStatus?: string | null }[],
    status: string
) => {
    return records.filter((record) => {
        const currentStatus = "backupStatus" in record ? record.backupStatus : record.restoreStatus;
        return currentStatus === status;
    }).length;
};

export const BackupRestorePage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canExecuteBackup = hasPermission("operations:backup:execute");
    const canExecuteRestore = hasPermission("operations:restore:execute");
    const [backupFilter, setBackupFilter] = useState<BackupLedgerQuery>({
        backupType: undefined,
        backupStatus: undefined
    });
    const [restoreFilter, setRestoreFilter] = useState<RestoreLedgerQuery>({
        restoreStatus: undefined
    });
    const [backupPageNo, setBackupPageNo] = useState(DEFAULT_PAGE_NO);
    const [restorePageNo, setRestorePageNo] = useState(DEFAULT_PAGE_NO);
    const [backupDetailId, setBackupDetailId] = useState<number | null>(null);
    const [restoreDetailId, setRestoreDetailId] = useState<number | null>(null);

    const backupQuery = useQuery({
        queryKey: ["operations", "backup", "page", backupFilter, backupPageNo],
        queryFn: () =>
            service.pageBackups({
                ...backupFilter,
                pageNo: backupPageNo,
                pageSize: DEFAULT_PAGE_SIZE
            }),
        retry: false
    });
    const restoreQuery = useQuery({
        queryKey: ["operations", "restore", "page", restoreFilter, restorePageNo],
        queryFn: () =>
            service.pageRestores({
                ...restoreFilter,
                pageNo: restorePageNo,
                pageSize: DEFAULT_PAGE_SIZE
            }),
        retry: false
    });
    const backupDetailQuery = useQuery({
        queryKey: ["operations", "backup", "detail", backupDetailId],
        queryFn: () => service.getBackupDetail({ backupId: backupDetailId as number }),
        enabled: backupDetailId !== null,
        retry: false
    });
    const restoreDetailQuery = useQuery({
        queryKey: ["operations", "restore", "detail", restoreDetailId],
        queryFn: () => service.getRestoreDetail({ restoreId: restoreDetailId as number }),
        enabled: restoreDetailId !== null,
        retry: false
    });

    const refreshLedgers = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ["operations", "backup", "page"] }),
            queryClient.invalidateQueries({ queryKey: ["operations", "restore", "page"] })
        ]);
    };

    const manualBackupMutation = useMutation({
        mutationFn: service.createManualBackup,
        onSuccess: async (result) => {
            await refreshLedgers();
            if (result.backupStatus === "FAILED") {
                messageApi.error(result.failureReason || "手动备份失败");
                return;
            }
            messageApi.success("手动备份已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "手动备份失败");
        }
    });

    const restoreMutation = useMutation({
        mutationFn: service.recoverBackup,
        onSuccess: async (result) => {
            await refreshLedgers();
            if (result.restoreStatus === "FAILED") {
                messageApi.error(result.failureReason || "恢复失败");
                return;
            }
            messageApi.success("恢复已完成");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "恢复失败");
        }
    });

    const backupRecords = backupQuery.data?.records || [];
    const restoreRecords = restoreQuery.data?.records || [];
    const latestBackup = backupQuery.data?.records?.[0] || null;

    const backupColumns: ColumnsType<OperationsBackupRecord> = [
        {
            title: "备份 ID",
            dataIndex: "backupId",
            key: "backupId",
            width: 110
        },
        {
            title: "文件",
            dataIndex: "fileName",
            key: "fileName",
            width: 260,
            render: (value?: string | null) => value || "-"
        },
        {
            title: "类型",
            dataIndex: "backupType",
            key: "backupType",
            width: 120,
            render: (value?: string | null) => value || "-"
        },
        {
            title: "状态",
            dataIndex: "backupStatus",
            key: "backupStatus",
            width: 120,
            render: (value?: string | null) => (
                <KuzhambuTag type={backupStatusTone(value)}>{value || "UNKNOWN"}</KuzhambuTag>
            )
        },
        {
            title: "大小",
            dataIndex: "fileSizeBytes",
            key: "fileSizeBytes",
            width: 120,
            render: (value?: number | null) => formatFileSize(value)
        },
        {
            title: "发起时间",
            dataIndex: "startedAt",
            key: "startedAt",
            width: 180,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            title: "完成时间",
            dataIndex: "completedAt",
            key: "completedAt",
            width: 180,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpace wrap>
                    <Button
                        icon={<EyeOutlined />}
                        onClick={() => setBackupDetailId(record.backupId)}
                        size="small"
                    >
                        查看
                    </Button>
                    {canExecuteRestore && record.backupStatus === "SUCCEEDED" ? (
                        <Button
                            danger
                            icon={<SyncOutlined />}
                            onClick={() =>
                                confirm.danger({
                                    title: "执行恢复",
                                    message: `确认从备份 #${record.backupId} 执行恢复吗？`,
                                    description:
                                        "恢复会先创建 PRE_RESTORE 快照，并覆盖业务恢复集中的当前数据。",
                                    okText: "执行恢复",
                                    onConfirm: () =>
                                        restoreMutation.mutateAsync({
                                            backupId: record.backupId
                                        })
                                })
                            }
                            size="small"
                        >
                            恢复
                        </Button>
                    ) : null}
                </KuzhambuSpace>
            )
        }
    ];

    const restoreColumns: ColumnsType<OperationsRestoreRecord> = [
        {
            title: "恢复 ID",
            dataIndex: "restoreId",
            key: "restoreId",
            width: 110
        },
        {
            title: "来源备份",
            dataIndex: "backupId",
            key: "backupId",
            width: 120
        },
        {
            title: "PRE_RESTORE",
            dataIndex: "preRestoreBackupId",
            key: "preRestoreBackupId",
            width: 140,
            render: (value?: number | null) => value || "-"
        },
        {
            title: "状态",
            dataIndex: "restoreStatus",
            key: "restoreStatus",
            width: 120,
            render: (value?: string | null) => (
                <KuzhambuTag type={restoreStatusTone(value)}>{value || "UNKNOWN"}</KuzhambuTag>
            )
        },
        {
            title: "写阻断",
            dataIndex: "writeBlockEnabled",
            key: "writeBlockEnabled",
            width: 120,
            render: (value?: boolean | null) =>
                value ? (
                    <KuzhambuTag type="warning">已启用</KuzhambuTag>
                ) : (
                    <Text type="secondary">未启用</Text>
                )
        },
        {
            title: "开始时间",
            dataIndex: "startedAt",
            key: "startedAt",
            width: 180,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            title: "完成时间",
            dataIndex: "completedAt",
            key: "completedAt",
            width: 180,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            key: "actions",
            render: (_, record) => (
                <Button
                    icon={<EyeOutlined />}
                    onClick={() => setRestoreDetailId(record.restoreId)}
                    size="small"
                >
                    查看
                </Button>
            )
        }
    ];

    return (
        <main className="kuzhambu-page backup-restore-page">
            <section className="kuzhambu-page-panel">
                <header className="kuzhambu-page-header">
                    <div>
                        <Text className="kuzhambu-page-eyebrow">Operations / Backup Restore</Text>
                        <Title level={2}>备份与恢复</Title>
                        <Text type="secondary">
                            统一管理手动备份、恢复执行和 PRE_RESTORE 快照台账。
                        </Text>
                    </div>
                </header>

                <div className="backup-restore-page-summary">
                    <Card className="backup-restore-page-summary-card">
                        <Statistic
                            prefix={<DatabaseOutlined />}
                            title="最近一次成功备份"
                            value={latestBackup?.fileName || "暂无"}
                        />
                        <Text className="backup-restore-page-empty-copy" type="secondary">
                            {latestBackup?.completedAt
                                ? `完成于 ${formatDateTime(latestBackup.completedAt)}`
                                : "当前还没有可用备份记录。"}
                        </Text>
                    </Card>
                    <Card className="backup-restore-page-summary-card">
                        <Statistic
                            prefix={<HistoryOutlined />}
                            title="恢复记录总数"
                            value={restoreQuery.data?.count ?? restoreQuery.data?.totalCount ?? 0}
                        />
                        <Text className="backup-restore-page-empty-copy" type="secondary">
                            成功 {countByStatus(restoreRecords, "SUCCEEDED")} 次，失败{" "}
                            {countByStatus(restoreRecords, "FAILED")} 次。
                        </Text>
                    </Card>
                    <Card className="backup-restore-page-summary-card">
                        <Statistic
                            prefix={<SafetyCertificateOutlined />}
                            title="PRE_RESTORE 快照"
                            value={countByStatus(
                                backupRecords.filter(
                                    (record) => record.backupType === "PRE_RESTORE"
                                ),
                                "SUCCEEDED"
                            )}
                        />
                        <Text className="backup-restore-page-empty-copy" type="secondary">
                            恢复失败时也会保留 PRE_RESTORE 快照记录，便于回退排查。
                        </Text>
                    </Card>
                </div>

                <div className="backup-restore-page-toolbar">
                    <div className="backup-restore-page-toolbar-fields">
                        <label className="backup-restore-page-toolbar-field">
                            <Text type="secondary">备份类型</Text>
                            <Select
                                aria-label="备份类型"
                                options={backupTypeOptions}
                                value={backupFilter.backupType || "ALL"}
                                onChange={(value) => {
                                    setBackupPageNo(DEFAULT_PAGE_NO);
                                    setBackupFilter((currentFilter) => ({
                                        ...currentFilter,
                                        backupType: normalizeFilterValue(value)
                                    }));
                                }}
                            />
                        </label>
                        <label className="backup-restore-page-toolbar-field">
                            <Text type="secondary">备份状态</Text>
                            <Select
                                aria-label="备份状态"
                                options={ledgerStatusOptions}
                                value={backupFilter.backupStatus || "ALL"}
                                onChange={(value) => {
                                    setBackupPageNo(DEFAULT_PAGE_NO);
                                    setBackupFilter((currentFilter) => ({
                                        ...currentFilter,
                                        backupStatus: normalizeFilterValue(value)
                                    }));
                                }}
                            />
                        </label>
                        <label className="backup-restore-page-toolbar-field">
                            <Text type="secondary">恢复状态</Text>
                            <Select
                                aria-label="恢复状态"
                                options={ledgerStatusOptions}
                                value={restoreFilter.restoreStatus || "ALL"}
                                onChange={(value) => {
                                    setRestorePageNo(DEFAULT_PAGE_NO);
                                    setRestoreFilter((currentFilter) => ({
                                        ...currentFilter,
                                        restoreStatus: normalizeFilterValue(value)
                                    }));
                                }}
                            />
                        </label>
                    </div>
                    <div className="backup-restore-page-toolbar-actions">
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={() => {
                                void refreshLedgers();
                            }}
                        >
                            刷新台账
                        </Button>
                        {canExecuteBackup ? (
                            <Button
                                icon={<PlayCircleOutlined />}
                                loading={manualBackupMutation.isPending}
                                onClick={() => {
                                    void manualBackupMutation.mutateAsync();
                                }}
                                type="primary"
                            >
                                执行手动备份
                            </Button>
                        ) : null}
                    </div>
                </div>

                <div className="backup-restore-page-ledgers">
                    <Card className="backup-restore-page-card">
                        <div className="backup-restore-page-card-head">
                            <div className="backup-restore-page-card-title">
                                <Title level={4}>备份台账</Title>
                                <Text type="secondary">
                                    展示手动备份与 PRE_RESTORE 快照的执行结果、失败原因和校验信息。
                                </Text>
                            </div>
                        </div>
                        <Table
                            aria-label="备份台账表格"
                            columns={backupColumns}
                            dataSource={backupRecords}
                            loading={backupQuery.isLoading}
                            pagination={{
                                current: backupQuery.data?.pageNo || backupPageNo,
                                pageSize: backupQuery.data?.pageSize || DEFAULT_PAGE_SIZE,
                                total: backupQuery.data?.count ?? backupQuery.data?.totalCount ?? 0,
                                onChange: (pageNo) => setBackupPageNo(pageNo)
                            }}
                            rowKey={(record) => record.backupId}
                            scroll={{ x: 1440 }}
                            size="small"
                        />
                    </Card>

                    <Card className="backup-restore-page-card">
                        <div className="backup-restore-page-card-head">
                            <div className="backup-restore-page-card-title">
                                <Title level={4}>恢复台账</Title>
                                <Text type="secondary">
                                    展示每次恢复对应的来源备份、PRE_RESTORE 快照和写阻断状态。
                                </Text>
                            </div>
                        </div>
                        <Table
                            aria-label="恢复台账表格"
                            columns={restoreColumns}
                            dataSource={restoreRecords}
                            loading={restoreQuery.isLoading}
                            pagination={{
                                current: restoreQuery.data?.pageNo || restorePageNo,
                                pageSize: restoreQuery.data?.pageSize || DEFAULT_PAGE_SIZE,
                                total:
                                    restoreQuery.data?.count ?? restoreQuery.data?.totalCount ?? 0,
                                onChange: (pageNo) => setRestorePageNo(pageNo)
                            }}
                            rowKey={(record) => record.restoreId}
                            scroll={{ x: 1200 }}
                            size="small"
                        />
                    </Card>
                </div>
            </section>

            <KuzhambuDrawer
                loading={backupDetailQuery.isLoading}
                onClose={() => setBackupDetailId(null)}
                open={backupDetailId !== null}
                size="middle"
                title="备份详情"
            >
                {backupDetailQuery.data ? (
                    <div className="backup-restore-page-detail">
                        <Descriptions
                            bordered
                            column={1}
                            items={[
                                {
                                    key: "backupId",
                                    label: "备份 ID",
                                    children: backupDetailQuery.data.backupId
                                },
                                {
                                    key: "backupType",
                                    label: "备份类型",
                                    children: backupDetailQuery.data.backupType || "-"
                                },
                                {
                                    key: "backupStatus",
                                    label: "备份状态",
                                    children: (
                                        <KuzhambuTag
                                            type={backupStatusTone(
                                                backupDetailQuery.data.backupStatus
                                            )}
                                        >
                                            {backupDetailQuery.data.backupStatus || "UNKNOWN"}
                                        </KuzhambuTag>
                                    )
                                },
                                {
                                    key: "fileName",
                                    label: "文件名",
                                    children: backupDetailQuery.data.fileName || "-"
                                },
                                {
                                    key: "fileSizeBytes",
                                    label: "文件大小",
                                    children: formatFileSize(backupDetailQuery.data.fileSizeBytes)
                                },
                                {
                                    key: "checksum",
                                    label: "Checksum",
                                    children: backupDetailQuery.data.checksum || "-"
                                },
                                {
                                    key: "failureReason",
                                    label: "失败原因",
                                    children: backupDetailQuery.data.failureReason || "-"
                                },
                                {
                                    key: "startedAt",
                                    label: "开始时间",
                                    children: formatDateTime(backupDetailQuery.data.startedAt)
                                },
                                {
                                    key: "completedAt",
                                    label: "完成时间",
                                    children: formatDateTime(backupDetailQuery.data.completedAt)
                                },
                                {
                                    key: "expiresAt",
                                    label: "有效期至",
                                    children: formatDateTime(backupDetailQuery.data.expiresAt)
                                }
                            ]}
                        />
                    </div>
                ) : null}
            </KuzhambuDrawer>

            <KuzhambuDrawer
                loading={restoreDetailQuery.isLoading}
                onClose={() => setRestoreDetailId(null)}
                open={restoreDetailId !== null}
                size="middle"
                title="恢复详情"
            >
                {restoreDetailQuery.data ? (
                    <div className="backup-restore-page-detail">
                        <Descriptions
                            bordered
                            column={1}
                            items={[
                                {
                                    key: "restoreId",
                                    label: "恢复 ID",
                                    children: restoreDetailQuery.data.restoreId
                                },
                                {
                                    key: "backupId",
                                    label: "来源备份",
                                    children: restoreDetailQuery.data.backupId || "-"
                                },
                                {
                                    key: "preRestoreBackupId",
                                    label: "PRE_RESTORE 备份",
                                    children: restoreDetailQuery.data.preRestoreBackupId || "-"
                                },
                                {
                                    key: "restoreStatus",
                                    label: "恢复状态",
                                    children: (
                                        <KuzhambuTag
                                            type={restoreStatusTone(
                                                restoreDetailQuery.data.restoreStatus
                                            )}
                                        >
                                            {restoreDetailQuery.data.restoreStatus || "UNKNOWN"}
                                        </KuzhambuTag>
                                    )
                                },
                                {
                                    key: "writeBlockEnabled",
                                    label: "写阻断",
                                    children: restoreDetailQuery.data.writeBlockEnabled
                                        ? "已启用"
                                        : "未启用"
                                },
                                {
                                    key: "failureReason",
                                    label: "失败原因",
                                    children: restoreDetailQuery.data.failureReason || "-"
                                },
                                {
                                    key: "startedAt",
                                    label: "开始时间",
                                    children: formatDateTime(restoreDetailQuery.data.startedAt)
                                },
                                {
                                    key: "completedAt",
                                    label: "完成时间",
                                    children: formatDateTime(restoreDetailQuery.data.completedAt)
                                }
                            ]}
                        />
                    </div>
                ) : null}
            </KuzhambuDrawer>
        </main>
    );
};
