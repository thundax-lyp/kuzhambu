import { EyeOutlined, PlayCircleOutlined, SyncOutlined } from "@ant-design/icons";
import { Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuButton, KuzhambuSpace, KuzhambuTag } from "@/components";

import { DEFAULT_PAGE_SIZE } from "@/types/page";
import type {
    OperationsBackupRecord,
    OperationsBackupType
} from "@/pages/operations/backup-restore/backup-restore-types";

const { Text } = Typography;

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

const backupTypeLabel = (value?: OperationsBackupType | null) => {
    if (value === "AUTO") {
        return "自动备份";
    }
    if (value === "MANUAL") {
        return "手动备份";
    }
    if (value === "PRE_RESTORE") {
        return "恢复前快照";
    }
    return "-";
};

const requesterLabel = (value?: number | null) => {
    return value == null ? "系统自动" : String(value);
};

const failureReasonText = (value?: string | null) => {
    return value || "未返回失败原因";
};

const buildAlertPath = (sourceRefType: string, sourceRefId?: string | null) => {
    if (!sourceRefId) {
        return "/operations/dashboard";
    }
    return `/operations/dashboard?sourceRefType=${sourceRefType}&sourceRefId=${sourceRefId}`;
};

interface BackupLedgerTableProps {
    canExecuteRestore: boolean;
    currentPage: number;
    items: OperationsBackupRecord[];
    loading: boolean;
    pageSize: number;
    total: number;
    onPageChange: (pageNo: number) => void;
    onRestoreDrill: (record: OperationsBackupRecord) => void;
    onRestoreReal: (record: OperationsBackupRecord) => void;
    onView: (record: OperationsBackupRecord) => void;
}

export const BackupLedgerTable = ({
    canExecuteRestore,
    currentPage,
    items,
    loading,
    pageSize,
    total,
    onPageChange,
    onRestoreDrill,
    onRestoreReal,
    onView
}: BackupLedgerTableProps) => {
    const columns: ColumnsType<OperationsBackupRecord> = [
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
            render: (value?: OperationsBackupType | null) => backupTypeLabel(value)
        },
        {
            title: "发起人",
            dataIndex: "requesterUserId",
            key: "requesterUserId",
            width: 120,
            render: (value?: number | null) => requesterLabel(value)
        },
        {
            title: "状态",
            dataIndex: "backupStatus",
            key: "backupStatus",
            width: 120,
            render: (value?: string | null, record?: OperationsBackupRecord) => (
                <KuzhambuSpace orientation="vertical" size={4}>
                    <KuzhambuTag type={backupStatusTone(value)}>{value || "UNKNOWN"}</KuzhambuTag>
                    {value === "FAILED" ? (
                        <>
                            <Text type="danger">{failureReasonText(record?.failureReason)}</Text>
                            <KuzhambuButton
                                testId="operations-backup-restore-backup-restore-view-alerts-button"
                                href={buildAlertPath("BACKUP", record?.backupId)}
                                size="small"
                            >
                                查看告警
                            </KuzhambuButton>
                        </>
                    ) : null}
                </KuzhambuSpace>
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
                    <KuzhambuButton
                        testId="operations-backup-restore-backup-restore-view-button"
                        icon={<EyeOutlined />}
                        onClick={() => onView(record)}
                        size="small"
                    >
                        查看
                    </KuzhambuButton>
                    {canExecuteRestore && record.backupStatus === "SUCCEEDED" ? (
                        <KuzhambuButton
                            testId="operations-backup-restore-backup-restore-drill-button"
                            icon={<PlayCircleOutlined />}
                            onClick={() => onRestoreDrill(record)}
                            size="small"
                        >
                            演练
                        </KuzhambuButton>
                    ) : null}
                    {canExecuteRestore && record.backupStatus === "SUCCEEDED" ? (
                        <KuzhambuButton
                            testId="operations-backup-restore-backup-restore-restore-button"
                            danger
                            icon={<SyncOutlined />}
                            onClick={() => onRestoreReal(record)}
                            size="small"
                        >
                            恢复
                        </KuzhambuButton>
                    ) : null}
                </KuzhambuSpace>
            )
        }
    ];

    return (
        <Table
            aria-label="备份台账表格"
            columns={columns}
            dataSource={items}
            loading={loading}
            pagination={{
                current: currentPage,
                pageSize: pageSize || DEFAULT_PAGE_SIZE,
                total,
                onChange: onPageChange
            }}
            rowKey={(record) => record.backupId}
            scroll={{ x: 1440 }}
            size="small"
        />
    );
};
