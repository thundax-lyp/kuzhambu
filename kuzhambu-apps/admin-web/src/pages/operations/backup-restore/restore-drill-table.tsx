import { EyeOutlined } from "@ant-design/icons";
import { Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuButton, KuzhambuSpace, KuzhambuTag } from "@/components";

import { DEFAULT_PAGE_SIZE } from "@/types/page";
import type { OperationsRestoreMode, OperationsRestoreRecord } from "./backup-restore-types";

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

const restoreModeLabel = (value?: OperationsRestoreMode | null) => {
    if (value === "REAL") {
        return "真实恢复";
    }
    if (value === "DRILL") {
        return "恢复演练";
    }
    return "-";
};

const failureReasonText = (value?: string | null) => {
    return value || "未返回失败原因";
};

const buildAlertPath = (sourceRefType: string, sourceRefId?: number | null) => {
    if (!sourceRefId) {
        return "/operations/dashboard";
    }
    return `/operations/dashboard?sourceRefType=${sourceRefType}&sourceRefId=${sourceRefId}`;
};

const writeBlockLabel = (record: OperationsRestoreRecord) => {
    if (record.restoreStatus === "RUNNING" && record.writeBlockEnabled) {
        return <KuzhambuTag type="warning">阻断中</KuzhambuTag>;
    }
    if (record.writeBlockReleasedAt) {
        return <KuzhambuTag type="success">已释放</KuzhambuTag>;
    }
    return <Text type="secondary">未启用</Text>;
};

interface RestoreDrillTableProps {
    currentPage: number;
    items: OperationsRestoreRecord[];
    loading: boolean;
    pageSize: number;
    total: number;
    onPageChange: (pageNo: number) => void;
    onView: (record: OperationsRestoreRecord) => void;
}

export const RestoreDrillTable = ({
    currentPage,
    items,
    loading,
    pageSize,
    total,
    onPageChange,
    onView
}: RestoreDrillTableProps) => {
    const columns: ColumnsType<OperationsRestoreRecord> = [
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
            title: "模式",
            dataIndex: "restoreMode",
            key: "restoreMode",
            width: 120,
            render: (value?: OperationsRestoreMode | null) => restoreModeLabel(value)
        },
        {
            title: "状态",
            dataIndex: "restoreStatus",
            key: "restoreStatus",
            width: 120,
            render: (value?: string | null, record?: OperationsRestoreRecord) => (
                <KuzhambuSpace orientation="vertical" size={4}>
                    <KuzhambuTag type={restoreStatusTone(value)}>{value || "UNKNOWN"}</KuzhambuTag>
                    {value === "FAILED" ? (
                        <>
                            <Text type="danger">{failureReasonText(record?.failureReason)}</Text>
                            <KuzhambuButton
                                testId="operations-backup-restore-backup-restore-view-alerts-button-2"
                                href={buildAlertPath("RESTORE", record?.restoreId)}
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
            title: "写阻断",
            dataIndex: "writeBlockEnabled",
            key: "writeBlockEnabled",
            width: 120,
            render: (_, record) => writeBlockLabel(record)
        },
        {
            title: "阻断开启",
            dataIndex: "writeBlockStartedAt",
            key: "writeBlockStartedAt",
            width: 180,
            render: (value?: string | null) => formatDateTime(value)
        },
        {
            title: "阻断释放",
            dataIndex: "writeBlockReleasedAt",
            key: "writeBlockReleasedAt",
            width: 180,
            render: (value?: string | null) => formatDateTime(value)
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
                <KuzhambuButton
                    testId="operations-backup-restore-backup-restore-view-button-2"
                    icon={<EyeOutlined />}
                    onClick={() => onView(record)}
                    size="small"
                >
                    查看
                </KuzhambuButton>
            )
        }
    ];

    return (
        <Table
            aria-label="恢复台账表格"
            columns={columns}
            dataSource={items}
            loading={loading}
            pagination={{
                current: currentPage,
                pageSize: pageSize || DEFAULT_PAGE_SIZE,
                total,
                onChange: onPageChange
            }}
            rowKey={(record) => record.restoreId}
            scroll={{ x: 1560 }}
            size="small"
        />
    );
};
