import { DashboardOutlined, SettingOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Input, Spin, Typography } from "antd";
import { useState } from "react";
import { Link } from "react-router-dom";
import { hasPermission } from "@/auth/permission-storage";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuDrawer,
    KuzhambuPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTag,
    KuzhambuCard,
    KuzhambuDescriptions
} from "@/components";
import * as service from "./task-service";
import type { OperationsTaskPageQuery } from "./task-service";
import type { OperationsTaskRecord } from "./task-types";

import "./task-page.css";

const { Text, Title } = Typography;

const EMPTY_TASK_QUERY: OperationsTaskPageQuery = {};
const TASK_STATUS_OPTIONS = ["ALL", "QUEUED", "RUNNING", "SUCCEEDED", "FAILED", "CANCELLED"];
const TASK_STATUS_SELECT_OPTIONS = TASK_STATUS_OPTIONS.map((status) => ({
    label: status,
    value: status
}));
const TASK_PAGE_SIZE_OPTIONS = [10, 20, 50].map((size) => ({
    label: String(size),
    value: size
}));

const normalizeSearch = (value?: string | null) => {
    const trimmed = value?.trim();
    return trimmed || undefined;
};

const toStatusTone = (status?: string | null) => {
    const normalizedStatus = (status || "").toUpperCase();
    if (["OK", "UP", "ACTIVE", "SUCCEEDED"].includes(normalizedStatus)) {
        return "success";
    }
    if (["WARN", "DEGRADED", "RUNNING", "QUEUED"].includes(normalizedStatus)) {
        return "warning";
    }
    if (["ERROR", "FAILED", "FAIL"].includes(normalizedStatus)) {
        return "danger";
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

const formatNumber = (value?: number | null) => {
    if (typeof value !== "number" || !Number.isFinite(value)) {
        return "-";
    }
    return String(value);
};

const isFailedTask = (task?: OperationsTaskRecord | null) => {
    return task?.taskStatus === "FAILED";
};

const failureReasonText = (value?: string | null) => {
    return value || "未返回失败原因";
};

const buildTaskAlertPath = (snapshotId?: string | null) => {
    if (!snapshotId) {
        return "/operations/dashboard";
    }
    return `/operations/dashboard?sourceRefType=LONG_TASK&sourceRefId=${snapshotId}`;
};

const buildTaskQuery = (query: OperationsTaskPageQuery, pageNo: number, pageSize: number) => {
    return {
        sourceDomain: normalizeSearch(query.sourceDomain),
        taskType: normalizeSearch(query.taskType),
        taskStatus: query.taskStatus === "ALL" ? undefined : normalizeSearch(query.taskStatus),
        pageNo,
        pageSize
    };
};

const operationEntries = [
    {
        description: "返回运营看板，查看健康指标和趋势",
        icon: <DashboardOutlined />,
        title: "运营看板",
        to: "/operations/dashboard"
    },
    {
        description: "查看备份和恢复任务记录",
        icon: <SettingOutlined />,
        title: "备份恢复",
        to: "/operations/backup-restore"
    },
    {
        description: "查看清理任务和失败明细",
        icon: <SettingOutlined />,
        title: "清理维护",
        to: "/operations/cleanup"
    }
];

export const OperationsTaskPage = () => {
    const canViewTask = hasPermission("operations:task:view");
    const [filters, setFilters] = useState<OperationsTaskPageQuery>(EMPTY_TASK_QUERY);
    const [taskPageNo, setTaskPageNo] = useState(DEFAULT_PAGE_NO);
    const [taskPageSize, setTaskPageSize] = useState(DEFAULT_PAGE_SIZE);
    const [detailTaskSnapshotId, setDetailTaskSnapshotId] = useState<string | null>(null);

    const taskQuery = useQuery({
        queryKey: ["operations", "task", "page", filters, taskPageNo, taskPageSize],
        queryFn: () => service.pageTasks(buildTaskQuery(filters, taskPageNo, taskPageSize)),
        enabled: canViewTask,
        retry: false
    });

    const taskDetailQuery = useQuery({
        queryKey: ["operations", "task", "detail", detailTaskSnapshotId],
        queryFn: () => service.getTaskDetail({ snapshotId: detailTaskSnapshotId ?? "" }),
        enabled: detailTaskSnapshotId !== null,
        retry: false
    });

    const taskPage = taskQuery.data;
    const tasks: OperationsTaskRecord[] = taskPage?.records || [];
    const totalCount = taskPage?.totalCount ?? taskPage?.count ?? 0;
    const totalPage = taskPage?.totalPage || 1;
    const detailTaskRecord = taskDetailQuery.data;
    const taskDetailDrawerOpen = detailTaskSnapshotId !== null;

    const updateFilters = (patch: Partial<OperationsTaskPageQuery>) => {
        setFilters((currentFilters) => ({
            ...currentFilters,
            ...patch
        }));
        setTaskPageNo(DEFAULT_PAGE_NO);
    };

    const nextPage = () => {
        if (taskPageNo >= totalPage) {
            return;
        }
        setTaskPageNo(taskPageNo + 1);
    };

    const previousPage = () => {
        if (taskPageNo <= DEFAULT_PAGE_NO) {
            return;
        }
        setTaskPageNo(taskPageNo - 1);
    };

    const openTaskDetailDrawer = (snapshotId: string) => {
        setDetailTaskSnapshotId(snapshotId);
    };

    const closeTaskDetailDrawer = () => {
        setDetailTaskSnapshotId(null);
    };

    return (
        <KuzhambuPage
            className="task-page operations-task-page"
            description="集中查看长任务状态、筛选执行记录，并快速返回运营看板或相关运维入口。"
            title="运营任务台账"
        >
            <div>
                <section className="operations-tasks-body">
                    <KuzhambuCard
                        className="operations-tasks-section-card"
                        title="长任务列表"
                        size="small"
                    >
                        <KuzhambuSpace
                            orientation="vertical"
                            className="operations-tasks-filters"
                            size={12}
                        >
                            <KuzhambuSpace size={8} wrap>
                                <Input
                                    placeholder="来源域（如 operations）"
                                    value={filters.sourceDomain || ""}
                                    onChange={(event) =>
                                        updateFilters({ sourceDomain: event.target.value })
                                    }
                                    style={{ width: 220 }}
                                    allowClear
                                />
                                <Input
                                    placeholder="任务类型"
                                    value={filters.taskType || ""}
                                    onChange={(event) =>
                                        updateFilters({ taskType: event.target.value })
                                    }
                                    style={{ width: 180 }}
                                    allowClear
                                />
                                <KuzhambuSelect
                                    value={filters.taskStatus || "ALL"}
                                    options={TASK_STATUS_SELECT_OPTIONS}
                                    style={{ width: 140 }}
                                    onChange={(status) => updateFilters({ taskStatus: status })}
                                />
                            </KuzhambuSpace>
                            <KuzhambuSpace size={8} wrap>
                                <KuzhambuButton
                                    testId="operations-tasks-tasks-reset-filter-button"
                                    icon={<SettingOutlined />}
                                    type="default"
                                    onClick={() => setFilters({})}
                                >
                                    重置筛选
                                </KuzhambuButton>
                                <Text type="secondary">共 {totalCount} 条记录</Text>
                            </KuzhambuSpace>
                        </KuzhambuSpace>
                        <table className="operations-tasks-table">
                            <thead>
                                <tr>
                                    <th>快照 ID</th>
                                    <th>来源域</th>
                                    <th>任务类型</th>
                                    <th>任务状态</th>
                                    <th>成功/失败</th>
                                    <th>启动时间</th>
                                    <th>完成时间</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                {tasks.length ? (
                                    tasks.map((task) => {
                                        const snapshotId = task.snapshotId;
                                        const failedTask = isFailedTask(task);
                                        return (
                                            <tr key={snapshotId}>
                                                <td>{snapshotId}</td>
                                                <td>{task.sourceDomain || "-"}</td>
                                                <td>{task.taskType || "-"}</td>
                                                <td>
                                                    <KuzhambuSpace size={6} wrap>
                                                        <KuzhambuTag
                                                            type={toStatusTone(task.taskStatus)}
                                                        >
                                                            {task.taskStatus || "-"}
                                                        </KuzhambuTag>
                                                        {failedTask ? (
                                                            <KuzhambuTag type="danger">
                                                                失败
                                                            </KuzhambuTag>
                                                        ) : null}
                                                    </KuzhambuSpace>
                                                </td>
                                                <td>
                                                    {formatNumber(task.successCount)} /{" "}
                                                    {formatNumber(task.failedCount)}
                                                    {failedTask ? (
                                                        <Text type="danger">
                                                            {failureReasonText(task.failureReason)}
                                                        </Text>
                                                    ) : null}
                                                </td>
                                                <td>{formatDateTime(task.startedAt)}</td>
                                                <td>{formatDateTime(task.completedAt)}</td>
                                                <td>
                                                    <KuzhambuButton
                                                        testId="operations-tasks-tasks-detail-button"
                                                        size="small"
                                                        type="link"
                                                        onClick={() =>
                                                            openTaskDetailDrawer(task.snapshotId)
                                                        }
                                                        disabled={!canViewTask}
                                                    >
                                                        详情
                                                    </KuzhambuButton>
                                                </td>
                                            </tr>
                                        );
                                    })
                                ) : (
                                    <tr>
                                        <td className="operations-tasks-empty-cell" colSpan={8}>
                                            {taskQuery.isLoading ? "加载中..." : "暂无任务数据"}
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                        <div className="operations-tasks-pagination">
                            <KuzhambuButton
                                testId="operations-tasks-tasks-previous-page-button"
                                disabled={taskPageNo <= 1}
                                onClick={previousPage}
                            >
                                上一页
                            </KuzhambuButton>
                            <Text>
                                第 {taskPageNo} / {totalPage} 页
                            </Text>
                            <KuzhambuButton
                                testId="operations-tasks-tasks-next-page-button"
                                disabled={taskPageNo >= totalPage}
                                onClick={nextPage}
                            >
                                下一页
                            </KuzhambuButton>
                            <KuzhambuSpace size={8}>
                                <Text>每页</Text>
                                <KuzhambuSelect
                                    value={taskPageSize}
                                    options={TASK_PAGE_SIZE_OPTIONS}
                                    style={{ width: 86 }}
                                    onChange={(size) => {
                                        setTaskPageNo(DEFAULT_PAGE_NO);
                                        setTaskPageSize(size);
                                    }}
                                />
                                <Text>条</Text>
                            </KuzhambuSpace>
                        </div>
                    </KuzhambuCard>
                </section>

                <section className="operations-tasks-shortcuts">
                    <Title level={5}>运维入口</Title>
                    <div className="operations-tasks-shortcut-grid">
                        {operationEntries.map((entry) => (
                            <Link
                                to={entry.to}
                                className="operations-tasks-shortcut"
                                key={entry.to}
                            >
                                <KuzhambuCard
                                    className="operations-tasks-shortcut-card"
                                    size="small"
                                >
                                    <KuzhambuSpace
                                        size={8}
                                        className="operations-tasks-shortcut-header"
                                    >
                                        <span aria-hidden>{entry.icon}</span>
                                        <Title level={5}>{entry.title}</Title>
                                    </KuzhambuSpace>
                                    <Text type="secondary">{entry.description}</Text>
                                </KuzhambuCard>
                            </Link>
                        ))}
                    </div>
                </section>

                <KuzhambuDrawer
                    testId="operations-tasks-tasks-drawer"
                    open={taskDetailDrawerOpen}
                    size="middle"
                    title={
                        detailTaskRecord
                            ? `长任务详情 #${detailTaskRecord.snapshotId}`
                            : "长任务详情"
                    }
                    onClose={closeTaskDetailDrawer}
                >
                    <div className="operations-tasks-detail">
                        <KuzhambuSpace size={4} orientation="vertical">
                            <Text strong>任务快照</Text>
                        </KuzhambuSpace>
                        <KuzhambuDescriptions
                            bordered
                            size="small"
                            column={1}
                            items={[
                                {
                                    key: "sourceDomain",
                                    label: "来源域",
                                    children: <Text>{detailTaskRecord?.sourceDomain || "-"}</Text>
                                },
                                {
                                    key: "taskType",
                                    label: "任务类型",
                                    children: <Text>{detailTaskRecord?.taskType || "-"}</Text>
                                },
                                {
                                    key: "taskStatus",
                                    label: "状态",
                                    children: (
                                        <KuzhambuTag
                                            type={toStatusTone(detailTaskRecord?.taskStatus)}
                                        >
                                            {detailTaskRecord?.taskStatus || "-"}
                                        </KuzhambuTag>
                                    )
                                },
                                {
                                    key: "taskKey",
                                    label: "任务键值",
                                    children: <Text>{detailTaskRecord?.taskKey || "-"}</Text>
                                },
                                {
                                    key: "result",
                                    label: "执行结果",
                                    children: (
                                        <Text>{`${detailTaskRecord?.successCount || 0}/${detailTaskRecord?.failedCount || 0}`}</Text>
                                    )
                                },
                                {
                                    key: "failureReason",
                                    label: "失败原因",
                                    children: <Text>{detailTaskRecord?.failureReason || "-"}</Text>
                                },
                                {
                                    key: "requestedByUserId",
                                    label: "发起用户",
                                    children: (
                                        <Text>{detailTaskRecord?.requestedByUserId || "-"}</Text>
                                    )
                                },
                                {
                                    key: "startedAt",
                                    label: "开始时间",
                                    children: (
                                        <Text>{formatDateTime(detailTaskRecord?.startedAt)}</Text>
                                    )
                                },
                                {
                                    key: "completedAt",
                                    label: "完成时间",
                                    children: (
                                        <Text>{formatDateTime(detailTaskRecord?.completedAt)}</Text>
                                    )
                                }
                            ]}
                        />
                        {taskDetailQuery.isLoading ? <Spin size="large" /> : null}
                        {isFailedTask(detailTaskRecord) ? (
                            <KuzhambuAlert
                                action={
                                    <KuzhambuButton
                                        testId="operations-tasks-tasks-view-alerts-button"
                                        size="small"
                                    >
                                        <Link to={buildTaskAlertPath(detailTaskRecord?.snapshotId)}>
                                            查看告警
                                        </Link>
                                    </KuzhambuButton>
                                }
                                description={`${failureReasonText(detailTaskRecord?.failureReason)}。请查看来源域任务状态，必要时重新发起业务动作。`}
                                title="长任务执行失败"
                                showIcon
                                type="warning"
                            />
                        ) : null}
                    </div>
                </KuzhambuDrawer>
            </div>
        </KuzhambuPage>
    );
};
