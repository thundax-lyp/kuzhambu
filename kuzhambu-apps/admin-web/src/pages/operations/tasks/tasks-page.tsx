import { DashboardOutlined, SettingOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Button, Card, Descriptions, Input, Select, Spin, Typography } from "antd";
import { useState } from "react";
import { Link } from "react-router-dom";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./tasks-service";
import type { OperationsTaskPageQuery } from "./tasks-service";
import type { OperationsTaskRecord } from "./tasks-types";
import "./tasks-page.css";

const { Text, Title } = Typography;
const { Option } = Select;

const EMPTY_TASK_QUERY: OperationsTaskPageQuery = {};
const TASK_STATUS_OPTIONS = ["ALL", "QUEUED", "RUNNING", "SUCCEEDED", "FAILED", "CANCELLED"];

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

export const OperationsTasksPage = () => {
    const canViewTask = hasPermission("operations:task:view");
    const [filters, setFilters] = useState<OperationsTaskPageQuery>(EMPTY_TASK_QUERY);
    const [taskPageNo, setTaskPageNo] = useState(DEFAULT_PAGE_NO);
    const [taskPageSize, setTaskPageSize] = useState(DEFAULT_PAGE_SIZE);
    const [detailSnapshotId, setDetailSnapshotId] = useState<number | null>(null);

    const taskQuery = useQuery({
        queryKey: ["operations", "task", "page", filters, taskPageNo, taskPageSize],
        queryFn: () => service.pageTasks(buildTaskQuery(filters, taskPageNo, taskPageSize)),
        enabled: canViewTask,
        retry: false
    });

    const taskDetailQuery = useQuery({
        queryKey: ["operations", "task", "detail", detailSnapshotId],
        queryFn: () => service.getTaskDetail({ snapshotId: detailSnapshotId as number }),
        enabled: detailSnapshotId !== null,
        retry: false
    });

    const taskPage = taskQuery.data;
    const tasks: OperationsTaskRecord[] = taskPage?.records || [];
    const totalCount = taskPage?.totalCount ?? taskPage?.count ?? 0;
    const totalPage = taskPage?.totalPage || 1;
    const detailRecord = taskDetailQuery.data;
    const isTaskDetailOpen = detailSnapshotId !== null;

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

    const openTaskDetail = (snapshotId: number) => {
        setDetailSnapshotId(snapshotId);
    };

    const closeTaskDetail = () => {
        setDetailSnapshotId(null);
    };

    return (
        <KuzhambuPage
            className="tasks-page operations-tasks-page"
            description="集中查看长任务状态、筛选执行记录，并快速返回运营看板或相关运维入口。"
            title="运营任务台账"
            eyebrow="Operations"
        >
            <div>
                <section className="operations-tasks-body">
                    <Card className="operations-tasks-section-card" title="长任务列表" size="small">
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
                                <Select
                                    value={filters.taskStatus || "ALL"}
                                    style={{ width: 140 }}
                                    onChange={(status) => updateFilters({ taskStatus: status })}
                                >
                                    {TASK_STATUS_OPTIONS.map((status) => (
                                        <Option value={status} key={status}>
                                            {status}
                                        </Option>
                                    ))}
                                </Select>
                            </KuzhambuSpace>
                            <KuzhambuSpace size={8} wrap>
                                <Button
                                    icon={<SettingOutlined />}
                                    type="default"
                                    onClick={() => setFilters({})}
                                >
                                    重置筛选
                                </Button>
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
                                        return (
                                            <tr key={snapshotId}>
                                                <td>{snapshotId}</td>
                                                <td>{task.sourceDomain || "-"}</td>
                                                <td>{task.taskType || "-"}</td>
                                                <td>
                                                    <KuzhambuTag
                                                        type={toStatusTone(task.taskStatus)}
                                                    >
                                                        {task.taskStatus || "-"}
                                                    </KuzhambuTag>
                                                </td>
                                                <td>
                                                    {formatNumber(task.successCount)} /{" "}
                                                    {formatNumber(task.failedCount)}
                                                </td>
                                                <td>{formatDateTime(task.startedAt)}</td>
                                                <td>{formatDateTime(task.completedAt)}</td>
                                                <td>
                                                    <Button
                                                        size="small"
                                                        type="link"
                                                        onClick={() =>
                                                            openTaskDetail(task.snapshotId)
                                                        }
                                                        disabled={!canViewTask}
                                                    >
                                                        详情
                                                    </Button>
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
                            <Button disabled={taskPageNo <= 1} onClick={previousPage}>
                                上一页
                            </Button>
                            <Text>
                                第 {taskPageNo} / {totalPage} 页
                            </Text>
                            <Button disabled={taskPageNo >= totalPage} onClick={nextPage}>
                                下一页
                            </Button>
                            <KuzhambuSpace size={8}>
                                <Text>每页</Text>
                                <Select
                                    value={taskPageSize}
                                    style={{ width: 86 }}
                                    onChange={(size) => {
                                        setTaskPageNo(DEFAULT_PAGE_NO);
                                        setTaskPageSize(size);
                                    }}
                                >
                                    {[10, 20, 50].map((size) => (
                                        <Option value={size} key={size}>
                                            {size}
                                        </Option>
                                    ))}
                                </Select>
                                <Text>条</Text>
                            </KuzhambuSpace>
                        </div>
                    </Card>
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
                                <Card size="small">
                                    <KuzhambuSpace
                                        size={8}
                                        className="operations-tasks-shortcut-header"
                                    >
                                        <span aria-hidden>{entry.icon}</span>
                                        <Title level={5}>{entry.title}</Title>
                                    </KuzhambuSpace>
                                    <Text type="secondary">{entry.description}</Text>
                                </Card>
                            </Link>
                        ))}
                    </div>
                </section>

                <KuzhambuDrawer
                    open={isTaskDetailOpen}
                    size="middle"
                    title={detailRecord ? `长任务详情 #${detailRecord.snapshotId}` : "长任务详情"}
                    onClose={closeTaskDetail}
                >
                    <div className="operations-tasks-detail">
                        <KuzhambuSpace size={4} orientation="vertical">
                            <Text strong>任务快照</Text>
                        </KuzhambuSpace>
                        <Descriptions bordered size="small" column={1}>
                            <Descriptions.Item label="来源域">
                                <Text>{detailRecord?.sourceDomain || "-"}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="任务类型">
                                <Text>{detailRecord?.taskType || "-"}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="状态">
                                <KuzhambuTag type={toStatusTone(detailRecord?.taskStatus)}>
                                    {detailRecord?.taskStatus || "-"}
                                </KuzhambuTag>
                            </Descriptions.Item>
                            <Descriptions.Item label="任务键值">
                                <Text>{detailRecord?.taskKey || "-"}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="执行结果">
                                <Text>{`${detailRecord?.successCount || 0}/${detailRecord?.failedCount || 0}`}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="失败原因">
                                <Text>{detailRecord?.failureReason || "-"}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="发起用户">
                                <Text>{detailRecord?.requestedByUserId || "-"}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="开始时间">
                                <Text>{formatDateTime(detailRecord?.startedAt)}</Text>
                            </Descriptions.Item>
                            <Descriptions.Item label="完成时间">
                                <Text>{formatDateTime(detailRecord?.completedAt)}</Text>
                            </Descriptions.Item>
                        </Descriptions>
                        {taskDetailQuery.isLoading ? <Spin size="large" /> : null}
                    </div>
                </KuzhambuDrawer>
            </div>
        </KuzhambuPage>
    );
};
