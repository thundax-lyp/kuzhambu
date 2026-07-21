import { useMutation, useQuery } from "@tanstack/react-query";
import {
    Card,
    DatePicker,
    Descriptions,
    Input,
    Segmented,
    Select,
    Table,
    Tag,
    Typography
} from "antd";
import type { ColumnsType } from "antd/es/table";
import type { Dayjs } from "dayjs";
import { useCallback, useEffect, useState } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as service from "./qa-console-service";
import type { DiscoveryQaSessionDetailRecord, KnowledgeSyncItemRecord } from "./qa-console-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./qa-console-page.css";

const { Text, Title } = Typography;
const { RangePicker } = DatePicker;

const DEFAULT_PAGE_SIZE = 10;

type QaConsolePanel = "health" | "sync" | "sessions" | "diagnostics";

const CONTENT_TYPE_OPTIONS = [{ label: "三才图会", value: "SANCAI_ENTRY" }];

const SYNC_STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "已删除", value: "DELETED" },
    { label: "同步中", value: "SYNCING" },
    { label: "待同步", value: "PENDING" }
];

const formatContentType = (value?: string | null) => {
    return CONTENT_TYPE_OPTIONS.find((option) => option.value === value)?.label ?? value ?? "-";
};

const formatSyncStatus = (value?: string | null) => {
    return SYNC_STATUS_OPTIONS.find((option) => option.value === value)?.label ?? value ?? "-";
};

const formatSyncStatusColor = (value?: string | null) => {
    if (value === "SUCCEEDED") {
        return "success";
    }
    if (value === "SYNCING") {
        return "processing";
    }
    if (value === "FAILED") {
        return "error";
    }
    return "default";
};

const formatSyncTitle = (record: KnowledgeSyncItemRecord) => {
    return record.title ?? "-";
};

const parseNumber = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) {
        return null;
    }

    const parsed = Number.parseInt(trimmed, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const parseString = (value?: string | null) => {
    const trimmed = value?.trim() ?? "";
    return trimmed.length ? trimmed : null;
};

const formatTime = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const timestamp = typeof value === "number" ? value : Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return String(value);
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

const formatDate = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const date = typeof value === "number" ? new Date(value) : new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const formatSyncItemKey = (record: KnowledgeSyncItemRecord) => {
    return record.sourceId ?? `${record.contentType ?? "UNKNOWN"}-${record.contentId ?? "0"}`;
};

export const QaConsolePage = () => {
    const [activePanel, setActivePanel] = useState<QaConsolePanel>("health");
    const requesterUserId = "1001";
    const fastGptConsoleUrl = parseString(import.meta.env.VITE_FASTGPT_CONSOLE_URL);
    const [sessionTitle, setSessionTitle] = useState("");
    const [sessionOpenedRange, setSessionOpenedRange] = useState<
        [Dayjs | null, Dayjs | null] | null
    >(null);
    const [sessionPageNo, setSessionPageNo] = useState(1);
    const [contentType, setContentType] = useState<string | undefined>("SANCAI_ENTRY");
    const [syncStatus, setSyncStatus] = useState<string | undefined>();
    const [syncPageNo, setSyncPageNo] = useState(1);
    const [sessionDrawerOpen, setSessionDrawerOpen] = useState(false);
    const [sessionDetail, setSessionDetail] = useState<DiscoveryQaSessionDetailRecord | null>(null);
    const [sessionOperationText, setSessionOperationText] = useState<string | null>(null);

    const healthQuery = useQuery({
        queryFn: service.getKnowledgeHealth,
        queryKey: ["discovery-qa-console-knowledge-health"]
    });

    const rebuildMutation = useMutation({
        mutationFn: service.rebuildKnowledge
    });
    const syncMutation = useMutation({
        mutationFn: service.createKnowledgeSync
    });
    const syncPageMutation = useMutation({
        mutationFn: service.pageKnowledgeSyncItems
    });
    const {
        data: syncPageData,
        isPending: isSyncPagePending,
        mutate: mutateSyncPage
    } = syncPageMutation;
    const sessionMutation = useMutation({
        mutationFn: service.getQaSession,
        onSuccess: (nextDetail) => {
            setSessionDetail(nextDetail);
            setSessionDrawerOpen(true);
            setSessionOperationText(null);
        }
    });
    const sessionPageMutation = useMutation({
        mutationFn: service.pageQaSessions
    });
    const {
        data: sessionPageData,
        isPending: isSessionPagePending,
        mutate: mutateSessionPage
    } = sessionPageMutation;
    const syncItems = syncPageData?.records ?? [];
    const syncColumns: ColumnsType<KnowledgeSyncItemRecord> = [
        {
            title: "内容类型",
            dataIndex: "contentType",
            key: "contentType",
            width: 140,
            render: (value?: string | null) => <Tag>{formatContentType(value)}</Tag>
        },
        {
            title: "标题",
            key: "title",
            width: 260,
            render: (_, record) => formatSyncTitle(record)
        },
        {
            title: "状态",
            dataIndex: "syncStatus",
            key: "syncStatus",
            width: 120,
            render: (value?: string | null) => (
                <Tag color={formatSyncStatusColor(value)}>{formatSyncStatus(value)}</Tag>
            )
        },
        {
            title: "同步时间",
            dataIndex: "syncedAt",
            key: "syncedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            title: "更新时间",
            dataIndex: "updatedAt",
            key: "updatedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            fixed: "right",
            key: "actions",
            render: (_, record) => (
                <KuzhambuButton
                    testId="discovery-qa-console-qa-console-sync-button"
                    loading={syncMutation.isPending}
                    onClick={() =>
                        syncMutation.mutate({
                            contentId: record.contentId ?? 0,
                            contentType: record.contentType ?? "",
                            currentVersionNo: record.currentVersionNo ?? null
                        })
                    }
                    size="small"
                >
                    同步
                </KuzhambuButton>
            )
        }
    ];

    const loadSyncItems = useCallback(
        (pageNo = syncPageNo) => {
            mutateSyncPage({
                contentType: parseString(contentType),
                pageNo,
                pageSize: DEFAULT_PAGE_SIZE,
                syncStatus: parseString(syncStatus)
            });
        },
        [contentType, mutateSyncPage, syncPageNo, syncStatus]
    );

    useEffect(() => {
        if (activePanel === "sync" && !syncPageData && !isSyncPagePending) {
            loadSyncItems();
        }
    }, [activePanel, isSyncPagePending, loadSyncItems, syncPageData]);

    const buildSessionPageQuery = useCallback(
        (pageNo = sessionPageNo) => ({
            openedAtEnd: sessionOpenedRange?.[1]?.endOf("day").toISOString() ?? null,
            openedAtStart: sessionOpenedRange?.[0]?.startOf("day").toISOString() ?? null,
            pageNo,
            pageSize: DEFAULT_PAGE_SIZE,
            title: parseString(sessionTitle)
        }),
        [sessionOpenedRange, sessionPageNo, sessionTitle]
    );

    const loadSessions = useCallback(
        (pageNo = sessionPageNo) => {
            mutateSessionPage(buildSessionPageQuery(pageNo));
            setSessionDrawerOpen(false);
        },
        [buildSessionPageQuery, mutateSessionPage, sessionPageNo]
    );

    const deleteSessionMutation = useMutation({
        mutationFn: service.deleteQaSession,
        onSuccess: (_, variables) => {
            const deletedSessionId = parseString(variables.sessionId);
            setSessionDetail((current) => ({
                ...(current ?? {}),
                sessionId: current?.sessionId ?? deletedSessionId,
                status: "REMOVED"
            }));
            setSessionDrawerOpen(false);
            setSessionOperationText(`会话 ${deletedSessionId ?? "-"} 已删除`);
            mutateSessionPage(buildSessionPageQuery());
        },
        onError: (error) => {
            setSessionOperationText(error instanceof Error ? error.message : "会话删除失败");
        }
    });

    const deleteCurrentSession = (targetSessionId: string) => {
        const nextSessionId = parseString(targetSessionId);
        if (nextSessionId === null) {
            return;
        }

        deleteSessionMutation.mutate({
            requesterUserId: parseNumber(requesterUserId),
            sessionId: nextSessionId
        });
    };

    useEffect(() => {
        if (activePanel === "sessions" && !sessionPageData && !isSessionPagePending) {
            mutateSessionPage(buildSessionPageQuery());
        }
    }, [
        activePanel,
        buildSessionPageQuery,
        isSessionPagePending,
        mutateSessionPage,
        sessionPageData
    ]);

    const sessionRows = sessionPageData?.records ?? [];
    const sessionColumns: ColumnsType<DiscoveryQaSessionDetailRecord> = [
        {
            title: "标题",
            dataIndex: "title",
            key: "title",
            width: 220,
            render: (value?: string | null) => value ?? "-"
        },
        {
            title: "拥有者",
            dataIndex: "ownerUserId",
            key: "ownerUserId",
            width: 120,
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "创建时间",
            dataIndex: "openedAt",
            key: "openedAt",
            width: 140,
            render: (value?: number | null) => formatDate(value)
        },
        {
            fixed: "right",
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpace size={8}>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-view-session-button"
                        loading={sessionMutation.isPending}
                        onClick={(event) => {
                            event.stopPropagation();
                            const nextSessionId = parseString(record.sessionId);
                            if (nextSessionId) {
                                sessionMutation.mutate({ sessionId: nextSessionId });
                            }
                        }}
                        size="small"
                    >
                        查看
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="discovery-qa-console-qa-console-delete-session-button"
                        danger
                        loading={deleteSessionMutation.isPending}
                        onClick={(event) => {
                            event.stopPropagation();
                            deleteCurrentSession(String(record.sessionId ?? ""));
                        }}
                        size="small"
                    >
                        删除
                    </KuzhambuButton>
                </KuzhambuSpace>
            )
        }
    ];

    const renderSessionDetail = (record: DiscoveryQaSessionDetailRecord) => (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <Descriptions
                bordered
                column={2}
                items={[
                    {
                        key: "scope",
                        label: "作用域",
                        children: record.scope ?? "-"
                    },
                    {
                        key: "contextMode",
                        label: "上下文模式",
                        children: record.contextMode ?? "-"
                    },
                    {
                        key: "contextContentType",
                        label: "上下文内容类型",
                        children: formatContentType(record.contextContentType)
                    },
                    {
                        key: "lastMessageAt",
                        label: "最后消息",
                        children: formatTime(record.lastMessageAt)
                    }
                ]}
                size="small"
            />

            <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                <Text strong>消息</Text>
                {record.messages?.length ? (
                    record.messages.map((message) => (
                        <div
                            className="qa-console-message"
                            key={message.messageId ?? message.content}
                        >
                            <Text strong>
                                {message.role ?? "-"} · {message.messageStatus ?? "-"}
                            </Text>
                            <Text>{message.content ?? "-"}</Text>
                            <Text type="secondary">
                                发送 {formatTime(message.sentAt)} · 回答{" "}
                                {formatTime(message.answeredAt)}
                            </Text>
                        </div>
                    ))
                ) : (
                    <Text type="secondary">暂无消息</Text>
                )}
            </KuzhambuSpace>
        </KuzhambuSpace>
    );

    return (
        <main className="kuzhambu-page discovery-admin-page qa-console-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Title level={2}>问答运维</Title>
                        <Text type="secondary">查看知识库健康、知识文档和问答会话。</Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Segmented
                        className="qa-console-segmented"
                        options={[
                            { label: "健康状态", value: "health" },
                            { label: "知识文档", value: "sync" },
                            { label: "会话管理", value: "sessions" },
                            { label: "问答诊断", value: "diagnostics" }
                        ]}
                        value={activePanel}
                        onChange={(value) => setActivePanel(value as QaConsolePanel)}
                    />

                    {activePanel === "health" ? (
                        <Card title="知识库健康" size="small">
                            <KuzhambuSpace
                                orientation="vertical"
                                size={12}
                                style={{ width: "100%" }}
                            >
                                <KuzhambuSpace wrap>
                                    <KuzhambuButton
                                        testId="discovery-qa-console-qa-console-refresh-health-button"
                                        loading={healthQuery.isFetching}
                                        onClick={() => void healthQuery.refetch()}
                                        type="primary"
                                    >
                                        刷新健康
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                                <Descriptions
                                    bordered
                                    column={2}
                                    items={[
                                        {
                                            key: "knowledgeBaseName",
                                            label: "知识库",
                                            children: healthQuery.data?.knowledgeBaseName ?? "-"
                                        },
                                        {
                                            key: "status",
                                            label: "状态",
                                            children: healthQuery.data?.status ?? "-"
                                        },
                                        {
                                            key: "provider",
                                            label: "Provider",
                                            children: healthQuery.data?.provider ?? "-"
                                        },
                                        {
                                            key: "checkedAt",
                                            label: "检查时间",
                                            children: formatTime(healthQuery.data?.checkedAt)
                                        },
                                        {
                                            key: "failureReason",
                                            label: "失败原因",
                                            children: healthQuery.data?.failureReason ?? "-"
                                        }
                                    ]}
                                    size="small"
                                />
                            </KuzhambuSpace>
                        </Card>
                    ) : null}

                    {activePanel === "sync" ? (
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <Card title="知识文档" size="small">
                                <KuzhambuSpace
                                    orientation="vertical"
                                    size={12}
                                    style={{ width: "100%" }}
                                >
                                    <Text type="secondary">
                                        查询同步记录，处理单条失败或过期同步。知识条目、分段和召回配置去
                                        FastGPT；批量异常用重建。
                                    </Text>
                                    <KuzhambuSpace align="end" wrap>
                                        <label className="qa-console-form-item">
                                            <Text type="secondary">内容类型</Text>
                                            <Select
                                                allowClear
                                                aria-label="内容类型"
                                                options={CONTENT_TYPE_OPTIONS}
                                                placeholder="全部类型"
                                                value={contentType}
                                                onChange={setContentType}
                                                style={{ width: 180 }}
                                            />
                                        </label>
                                        <label className="qa-console-form-item">
                                            <Text type="secondary">同步状态</Text>
                                            <Select
                                                allowClear
                                                aria-label="同步状态"
                                                options={SYNC_STATUS_OPTIONS}
                                                placeholder="全部状态"
                                                value={syncStatus}
                                                onChange={setSyncStatus}
                                                style={{ width: 160 }}
                                            />
                                        </label>
                                        <KuzhambuButton
                                            testId="discovery-qa-console-qa-console-query-sync-button"
                                            loading={isSyncPagePending}
                                            onClick={() => {
                                                setSyncPageNo(1);
                                                loadSyncItems(1);
                                            }}
                                            type="primary"
                                        >
                                            查询
                                        </KuzhambuButton>
                                        <KuzhambuButton
                                            testId="discovery-qa-console-qa-console-rebuild-knowledge-base-button"
                                            danger
                                            loading={rebuildMutation.isPending}
                                            onClick={() => rebuildMutation.mutate({})}
                                        >
                                            全部同步
                                        </KuzhambuButton>
                                    </KuzhambuSpace>
                                </KuzhambuSpace>
                            </Card>
                            <Card title="同步记录" size="small">
                                <Table
                                    aria-label="知识同步表格"
                                    columns={syncColumns}
                                    dataSource={syncItems}
                                    pagination={{
                                        current: syncPageData?.pageNo ?? syncPageNo,
                                        onChange: (nextPageNo) => {
                                            setSyncPageNo(nextPageNo);
                                            loadSyncItems(nextPageNo);
                                        },
                                        pageSize: DEFAULT_PAGE_SIZE,
                                        showTotal: (total) => `共 ${total} 条`,
                                        showSizeChanger: false,
                                        total: syncPageData?.totalCount ?? syncPageData?.count ?? 0
                                    }}
                                    rowKey={formatSyncItemKey}
                                    scroll={{ x: 900 }}
                                    size="small"
                                />
                            </Card>
                        </KuzhambuSpace>
                    ) : null}

                    {activePanel === "sessions" ? (
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <Card title="会话管理" size="small">
                                <KuzhambuSpace align="end" wrap>
                                    <label className="qa-console-form-item">
                                        <Text type="secondary">标题</Text>
                                        <Input
                                            allowClear
                                            aria-label="标题"
                                            value={sessionTitle}
                                            onChange={(event) =>
                                                setSessionTitle(event.target.value)
                                            }
                                            style={{ width: 220 }}
                                        />
                                    </label>
                                    <label className="qa-console-form-item">
                                        <Text type="secondary">创建时间</Text>
                                        <RangePicker
                                            aria-label="创建时间"
                                            value={sessionOpenedRange}
                                            onChange={(value) => setSessionOpenedRange(value)}
                                        />
                                    </label>
                                    <KuzhambuButton
                                        testId="discovery-qa-console-qa-console-load-session-button"
                                        loading={isSessionPagePending}
                                        onClick={() => {
                                            setSessionPageNo(1);
                                            loadSessions(1);
                                        }}
                                        type="primary"
                                    >
                                        查询
                                    </KuzhambuButton>
                                </KuzhambuSpace>
                            </Card>

                            <Card title="会话记录" size="small">
                                <KuzhambuSpace
                                    orientation="vertical"
                                    size={12}
                                    style={{ width: "100%" }}
                                >
                                    {sessionOperationText ? (
                                        <Text type="secondary">{sessionOperationText}</Text>
                                    ) : null}
                                    <Table
                                        aria-label="问答会话表格"
                                        columns={sessionColumns}
                                        dataSource={sessionRows}
                                        pagination={{
                                            current: sessionPageData?.pageNo ?? sessionPageNo,
                                            onChange: (nextPageNo) => {
                                                setSessionPageNo(nextPageNo);
                                                loadSessions(nextPageNo);
                                            },
                                            pageSize: DEFAULT_PAGE_SIZE,
                                            showTotal: (total) => `共 ${total} 条`,
                                            showSizeChanger: false,
                                            total:
                                                sessionPageData?.totalCount ??
                                                sessionPageData?.count ??
                                                0
                                        }}
                                        rowKey={(record) => record.sessionId ?? "-"}
                                        scroll={{ x: 780 }}
                                        size="small"
                                    />
                                </KuzhambuSpace>
                            </Card>
                            <KuzhambuDrawer
                                destroyOnClose
                                onClose={() => setSessionDrawerOpen(false)}
                                open={sessionDrawerOpen}
                                size="large"
                                testId="discovery-qa-console-session-detail-drawer"
                                title={sessionDetail?.title ?? "会话详情"}
                            >
                                {sessionDetail ? renderSessionDetail(sessionDetail) : null}
                            </KuzhambuDrawer>
                        </KuzhambuSpace>
                    ) : null}

                    {activePanel === "diagnostics" ? (
                        <Card title="问答诊断" size="small">
                            <KuzhambuSpace orientation="vertical" size={12}>
                                <Text type="secondary">
                                    知识条目、分段、召回配置以 FastGPT 为准。
                                </Text>
                                <KuzhambuButton
                                    disabled={!fastGptConsoleUrl}
                                    href={fastGptConsoleUrl ?? undefined}
                                    target="_blank"
                                    testId="discovery-qa-console-fastgpt-console-link"
                                    type="primary"
                                >
                                    FastGPT 控制台
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </Card>
                    ) : null}
                </KuzhambuSpace>
            </section>
        </main>
    );
};
