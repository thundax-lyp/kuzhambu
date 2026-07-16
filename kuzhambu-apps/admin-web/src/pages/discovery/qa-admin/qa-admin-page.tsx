import { useMutation, useQuery } from "@tanstack/react-query";
import { Card, Descriptions, Input, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useState } from "react";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as service from "./qa-admin-service";
import type {
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSourceRecord,
    KnowledgeSyncItemRecord,
    ProviderTraceRecord
} from "./qa-admin-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./qa-admin-page.css";

const { Text, Title } = Typography;

const DEFAULT_PAGE_SIZE = 10;

const parseNumber = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) {
        return null;
    }

    const parsed = Number.parseInt(trimmed, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const parseString = (value: string) => {
    const trimmed = value.trim();
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

const formatRawJson = (value?: string | object | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    if (typeof value === "string") {
        try {
            return JSON.stringify(JSON.parse(value), null, 2);
        } catch {
            return value;
        }
    }
    return JSON.stringify(value, null, 2);
};

const formatSyncItemKey = (record: KnowledgeSyncItemRecord) => {
    return record.sourceId ?? `${record.contentType ?? "UNKNOWN"}-${record.contentId ?? "0"}`;
};

export const QaAdminPage = () => {
    const [sessionId, setSessionId] = useState("2001");
    const [requesterUserId, setRequesterUserId] = useState("1001");
    const [messageId, setMessageId] = useState("4001");
    const [traceId, setTraceId] = useState("9001");
    const [contentType, setContentType] = useState("SANCAI_ENTRY");
    const [contentId, setContentId] = useState("1001");
    const [currentVersionNo, setCurrentVersionNo] = useState("1");
    const [syncStatus, setSyncStatus] = useState("");
    const [sessionDetail, setSessionDetail] = useState<DiscoveryQaSessionDetailRecord | null>(null);
    const [sources, setSources] = useState<DiscoveryQaSourceRecord[]>([]);
    const [trace, setTrace] = useState<ProviderTraceRecord | null>(null);
    const [lastSyncResult, setLastSyncResult] = useState<KnowledgeSyncItemRecord | null>(null);
    const [sessionOperationText, setSessionOperationText] = useState<string | null>(null);

    const healthQuery = useQuery({
        queryFn: service.getKnowledgeHealth,
        queryKey: ["discovery-qa-admin-knowledge-health"]
    });

    const rebuildMutation = useMutation({
        mutationFn: service.rebuildKnowledge
    });
    const syncMutation = useMutation({
        mutationFn: service.createKnowledgeSync,
        onSuccess: (nextItem) => {
            setLastSyncResult(nextItem);
        }
    });
    const syncPageMutation = useMutation({
        mutationFn: service.pageKnowledgeSyncItems
    });
    const sessionMutation = useMutation({
        mutationFn: service.getQaSession,
        onSuccess: (nextDetail) => {
            setSessionDetail(nextDetail);
            setSessionOperationText(null);
        }
    });
    const deleteSessionMutation = useMutation({
        mutationFn: service.deleteQaSession,
        onSuccess: () => {
            const deletedSessionId = parseString(sessionId);
            setSessionDetail((current) => ({
                ...(current ?? {}),
                sessionId: current?.sessionId ?? deletedSessionId,
                status: "REMOVED"
            }));
            setSessionOperationText(`会话 ${deletedSessionId ?? sessionId} 已删除`);
        },
        onError: (error) => {
            setSessionOperationText(error instanceof Error ? error.message : "会话删除失败");
        }
    });
    const exportSessionMutation = useMutation({
        mutationFn: service.createQaSessionExport,
        onSuccess: (result) => {
            if (result.exportStatus === "FAILED") {
                setSessionOperationText(result.failureReason ?? "会话导出失败");
                return;
            }

            const filename = result.filename ?? `discovery-qa-session-${sessionId}.csv`;
            const storageObjectText = result.storageObjectId
                ? `，对象号 ${result.storageObjectId}`
                : "";
            setSessionOperationText(`导出成功：${filename}${storageObjectText}`);
        },
        onError: (error) => {
            setSessionOperationText(error instanceof Error ? error.message : "会话导出失败");
        }
    });
    const sourceMutation = useMutation({
        mutationFn: service.listQaSources,
        onSuccess: (nextSources) => {
            setSources(nextSources);
        }
    });
    const traceMutation = useMutation({
        mutationFn: service.getQaTrace,
        onSuccess: (nextTrace) => {
            setTrace(nextTrace);
        }
    });

    const syncItems = syncPageMutation.data?.records ?? [];
    const syncColumns: ColumnsType<KnowledgeSyncItemRecord> = [
        { title: "来源号", dataIndex: "sourceId", key: "sourceId", width: 180 },
        { title: "内容类型", dataIndex: "contentType", key: "contentType", width: 140 },
        { title: "内容号", dataIndex: "contentId", key: "contentId", width: 120 },
        {
            title: "知识库",
            dataIndex: "knowledgeBaseName",
            key: "knowledgeBaseName",
            width: 160
        },
        {
            title: "版本",
            key: "version",
            width: 160,
            render: (_, record) =>
                `${record.currentVersionNo ?? "-"} / ${record.knowledgeRevision ?? "-"}`
        },
        {
            title: "状态",
            dataIndex: "syncStatus",
            key: "syncStatus",
            width: 120,
            render: (value?: string | null) => <Tag>{value ?? "-"}</Tag>
        },
        {
            title: "同步时间",
            dataIndex: "syncedAt",
            key: "syncedAt",
            width: 180,
            render: (value?: number | null) => formatTime(value)
        },
        {
            title: "更新时间",
            dataIndex: "updatedAt",
            key: "updatedAt",
            width: 180,
            render: (value?: number | null) => formatTime(value)
        },
        {
            title: "操作",
            key: "action",
            width: 120,
            render: (_, record) => (
                <KuzhambuButton
                    testId="discovery-qa-admin-qa-admin-sync-button"
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

    const loadSyncItems = () => {
        syncPageMutation.mutate({
            contentType: parseString(contentType),
            pageNo: 1,
            pageSize: DEFAULT_PAGE_SIZE,
            syncStatus: parseString(syncStatus)
        });
    };

    const syncCurrentContent = () => {
        const nextContentId = parseNumber(contentId);
        if (nextContentId === null) {
            return;
        }

        syncMutation.mutate({
            contentId: nextContentId,
            contentType: parseString(contentType) ?? "",
            currentVersionNo: parseNumber(currentVersionNo)
        });
    };

    const deleteCurrentSession = () => {
        const nextSessionId = parseString(sessionId);
        if (nextSessionId === null) {
            return;
        }

        const confirmed = window.confirm(`确认删除会话 ${nextSessionId}？`);
        if (!confirmed) {
            return;
        }

        deleteSessionMutation.mutate({
            requesterUserId: parseNumber(requesterUserId),
            sessionId: nextSessionId
        });
    };

    const exportCurrentSession = () => {
        const nextSessionId = parseString(sessionId);
        if (nextSessionId === null) {
            return;
        }

        exportSessionMutation.mutate({
            format: "CSV",
            requesterUserId: parseNumber(requesterUserId),
            sessionId: nextSessionId
        });
    };

    const copyAiCallId = () => {
        if (!trace?.aiCallId || !navigator.clipboard) {
            return;
        }
        void navigator.clipboard.writeText(String(trace.aiCallId));
    };

    return (
        <main className="kuzhambu-page discovery-admin-page qa-admin-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Text className="kuzhambu-page-eyebrow">Discovery / QA Admin</Text>
                        <Title level={2}>问答运维台</Title>
                        <Text type="secondary">
                            查看知识库健康、同步状态、会话来源和 Provider 轨迹。
                        </Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Card title="知识库健康" size="small">
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <KuzhambuSpace wrap>
                                <KuzhambuButton
                                    testId="discovery-qa-admin-qa-admin-refresh-health-button"
                                    loading={healthQuery.isFetching}
                                    onClick={() => void healthQuery.refetch()}
                                    type="primary"
                                >
                                    刷新健康
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="discovery-qa-admin-qa-admin-rebuild-knowledge-base-button"
                                    danger
                                    loading={rebuildMutation.isPending}
                                    onClick={() => rebuildMutation.mutate({})}
                                >
                                    重建知识库
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
                                    },
                                    {
                                        key: "rebuildResult",
                                        label: "重建结果",
                                        children: rebuildMutation.data ?? "-"
                                    }
                                ]}
                                size="small"
                            />
                        </KuzhambuSpace>
                    </Card>

                    <Card title="知识同步" size="small">
                        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                            <KuzhambuSpace align="end" wrap>
                                <label>
                                    <Text type="secondary">内容类型</Text>
                                    <Input
                                        aria-label="内容类型"
                                        value={contentType}
                                        onChange={(event) => setContentType(event.target.value)}
                                        style={{ width: 180 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">同步状态</Text>
                                    <Input
                                        aria-label="同步状态"
                                        value={syncStatus}
                                        onChange={(event) => setSyncStatus(event.target.value)}
                                        style={{ width: 160 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">内容号</Text>
                                    <Input
                                        aria-label="内容号"
                                        value={contentId}
                                        onChange={(event) => setContentId(event.target.value)}
                                        style={{ width: 140 }}
                                    />
                                </label>
                                <label>
                                    <Text type="secondary">版本号</Text>
                                    <Input
                                        aria-label="版本号"
                                        value={currentVersionNo}
                                        onChange={(event) =>
                                            setCurrentVersionNo(event.target.value)
                                        }
                                        style={{ width: 120 }}
                                    />
                                </label>
                                <KuzhambuButton
                                    testId="discovery-qa-admin-qa-admin-query-sync-button"
                                    loading={syncPageMutation.isPending}
                                    onClick={loadSyncItems}
                                    type="primary"
                                >
                                    查询同步
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="discovery-qa-admin-qa-admin-sync-content-button"
                                    loading={syncMutation.isPending}
                                    onClick={syncCurrentContent}
                                >
                                    同步内容
                                </KuzhambuButton>
                            </KuzhambuSpace>
                            <Table
                                aria-label="知识同步表格"
                                columns={syncColumns}
                                dataSource={syncItems}
                                pagination={false}
                                rowKey={formatSyncItemKey}
                                scroll={{ x: 1240 }}
                                size="small"
                            />
                            <Text type="secondary">
                                {syncPageMutation.data
                                    ? `共 ${syncPageMutation.data.count ?? syncPageMutation.data.totalCount ?? 0} 条同步记录`
                                    : "暂无同步记录。"}
                            </Text>
                            <Text type="secondary">
                                最近同步：
                                {lastSyncResult
                                    ? `${lastSyncResult.sourceId ?? "-"} / ${lastSyncResult.syncStatus ?? "-"}`
                                    : "-"}
                            </Text>
                        </KuzhambuSpace>
                    </Card>

                    <Card title="会话详情" size="small">
                        <KuzhambuSpace align="end" style={{ flexWrap: "wrap", width: "100%" }}>
                            <label>
                                <Text type="secondary">会话号</Text>
                                <Input
                                    aria-label="会话号"
                                    value={sessionId}
                                    onChange={(event) => setSessionId(event.target.value)}
                                    style={{ width: 220 }}
                                />
                            </label>
                            <label>
                                <Text type="secondary">操作者用户号</Text>
                                <Input
                                    aria-label="操作者用户号"
                                    value={requesterUserId}
                                    onChange={(event) => setRequesterUserId(event.target.value)}
                                    style={{ width: 180 }}
                                />
                            </label>
                            <KuzhambuButton
                                testId="discovery-qa-admin-qa-admin-load-session-button"
                                loading={sessionMutation.isPending}
                                onClick={() => {
                                    const nextSessionId = parseString(sessionId);
                                    if (nextSessionId) {
                                        sessionMutation.mutate({
                                            sessionId: nextSessionId
                                        });
                                    }
                                }}
                                type="primary"
                            >
                                加载会话
                            </KuzhambuButton>
                            <KuzhambuButton
                                testId="discovery-qa-admin-qa-admin-delete-session-button"
                                danger
                                loading={deleteSessionMutation.isPending}
                                onClick={deleteCurrentSession}
                            >
                                删除会话
                            </KuzhambuButton>
                            <KuzhambuButton
                                testId="discovery-qa-admin-qa-admin-export-csv-button"
                                loading={exportSessionMutation.isPending}
                                onClick={exportCurrentSession}
                            >
                                导出 CSV
                            </KuzhambuButton>
                        </KuzhambuSpace>

                        {sessionOperationText ? (
                            <Text type="secondary">{sessionOperationText}</Text>
                        ) : null}

                        <Descriptions
                            bordered
                            column={2}
                            items={[
                                {
                                    key: "title",
                                    label: "标题",
                                    children: sessionDetail?.title ?? "-"
                                },
                                {
                                    key: "status",
                                    label: "状态",
                                    children: sessionDetail?.status ? (
                                        <Tag
                                            color={
                                                sessionDetail.status === "REMOVED"
                                                    ? "red"
                                                    : undefined
                                            }
                                        >
                                            {sessionDetail.status}
                                        </Tag>
                                    ) : (
                                        "-"
                                    )
                                },
                                {
                                    key: "ownerUserId",
                                    label: "拥有者用户号",
                                    children: sessionDetail?.ownerUserId ?? "-"
                                },
                                {
                                    key: "scope",
                                    label: "作用域",
                                    children: sessionDetail?.scope ?? "-"
                                },
                                {
                                    key: "contextMode",
                                    label: "上下文模式",
                                    children: sessionDetail?.contextMode ?? "-"
                                },
                                {
                                    key: "contextContentType",
                                    label: "上下文内容类型",
                                    children: sessionDetail?.contextContentType ?? "-"
                                },
                                {
                                    key: "openedAt",
                                    label: "创建时间",
                                    children: formatTime(sessionDetail?.openedAt)
                                }
                            ]}
                            size="small"
                            style={{ marginTop: 16 }}
                        />

                        <KuzhambuSpace
                            orientation="vertical"
                            size={8}
                            style={{ marginTop: 16, width: "100%" }}
                        >
                            <Text strong>消息列表</Text>
                            {sessionDetail?.messages?.length ? (
                                sessionDetail.messages.map((message) => (
                                    <div
                                        className="qa-admin-message"
                                        key={message.messageId ?? message.content}
                                    >
                                        <Text strong>
                                            {message.role ?? "-"} · {message.messageStatus ?? "-"}
                                        </Text>
                                        <Text>{message.content ?? "-"}</Text>
                                        <Text type="secondary">
                                            轮次 {message.contextTurnCount ?? "-"} · 发送
                                            {formatTime(message.sentAt)} · 回答
                                            {formatTime(message.answeredAt)}
                                        </Text>
                                    </div>
                                ))
                            ) : (
                                <Text type="secondary">暂无消息。</Text>
                            )}
                        </KuzhambuSpace>
                    </Card>

                    <Card title="来源列表" size="small">
                        <KuzhambuSpace align="end" style={{ flexWrap: "wrap", width: "100%" }}>
                            <label>
                                <Text type="secondary">消息号</Text>
                                <Input
                                    aria-label="消息号"
                                    value={messageId}
                                    onChange={(event) => setMessageId(event.target.value)}
                                    style={{ width: 220 }}
                                />
                            </label>
                            <KuzhambuButton
                                testId="discovery-qa-admin-qa-admin-load-sources-button"
                                loading={sourceMutation.isPending}
                                onClick={() => {
                                    const nextMessageId = parseString(messageId);
                                    if (nextMessageId) {
                                        sourceMutation.mutate({
                                            messageId: nextMessageId
                                        });
                                    }
                                }}
                                type="primary"
                            >
                                加载来源
                            </KuzhambuButton>
                        </KuzhambuSpace>

                        <KuzhambuSpace
                            orientation="vertical"
                            size={8}
                            style={{ marginTop: 16, width: "100%" }}
                        >
                            {sources.length ? (
                                sources.map((source) => (
                                    <div
                                        className="qa-admin-source"
                                        key={
                                            source.sourceId ??
                                            `${source.contentType}-${source.contentId}`
                                        }
                                    >
                                        <Text strong>{source.titleSnapshot ?? "-"}</Text>
                                        <Text type="secondary">
                                            {source.knowledgeBase ?? "-"} ·{" "}
                                            {source.contentType ?? "-"} · 排序
                                            {source.sourceRank ?? "-"} · 得分 {source.score ?? "-"}{" "}
                                            · {source.sourceStatus ?? "-"}
                                        </Text>
                                        <Text>{source.snippet ?? "-"}</Text>
                                    </div>
                                ))
                            ) : (
                                <Text type="secondary">暂无来源。</Text>
                            )}
                        </KuzhambuSpace>
                    </Card>

                    <Card title="Provider Trace" size="small">
                        <KuzhambuSpace align="end" style={{ flexWrap: "wrap", width: "100%" }}>
                            <label>
                                <Text type="secondary">轨迹号</Text>
                                <Input
                                    aria-label="轨迹号"
                                    value={traceId}
                                    onChange={(event) => setTraceId(event.target.value)}
                                    style={{ width: 220 }}
                                />
                            </label>
                            <KuzhambuButton
                                testId="discovery-qa-admin-qa-admin-load-traces-button"
                                loading={traceMutation.isPending}
                                onClick={() => {
                                    const nextTraceId = parseString(traceId);
                                    if (nextTraceId) {
                                        traceMutation.mutate({
                                            traceId: nextTraceId
                                        });
                                    }
                                }}
                                type="primary"
                            >
                                加载轨迹
                            </KuzhambuButton>
                        </KuzhambuSpace>

                        <Descriptions
                            bordered
                            column={2}
                            items={[
                                {
                                    key: "provider",
                                    label: "Provider",
                                    children: trace?.provider ?? "-"
                                },
                                {
                                    key: "knowledgeBase",
                                    label: "外部知识库",
                                    children: trace?.externalKnowledgeBaseId ?? "-"
                                },
                                {
                                    key: "itemIds",
                                    label: "外部条目",
                                    children: trace?.externalKnowledgeItemIds ?? "-"
                                },
                                {
                                    key: "externalChatId",
                                    label: "外部会话",
                                    children: trace?.externalChatId ?? "-"
                                },
                                {
                                    key: "providerRequestId",
                                    label: "Provider 请求号",
                                    children: trace?.providerRequestId ?? "-"
                                },
                                {
                                    key: "aiCallId",
                                    label: "AI 调用 ID",
                                    children: (
                                        <KuzhambuSpace size={8}>
                                            <Text>{trace?.aiCallId ?? "-"}</Text>
                                            <KuzhambuButton
                                                testId="discovery-qa-admin-qa-admin-copy-button"
                                                disabled={!trace?.aiCallId}
                                                onClick={copyAiCallId}
                                                size="small"
                                            >
                                                复制
                                            </KuzhambuButton>
                                        </KuzhambuSpace>
                                    )
                                },
                                {
                                    key: "aiStatus",
                                    label: "AI 状态",
                                    children: trace?.aiStatus ?? "-"
                                },
                                {
                                    key: "aiErrorType",
                                    label: "AI 错误类型",
                                    children: trace?.aiErrorType ?? "-"
                                },
                                {
                                    key: "aiErrorMessage",
                                    label: "AI 错误信息",
                                    children: trace?.aiErrorMessage ?? "-"
                                },
                                {
                                    key: "latency",
                                    label: "耗时",
                                    children: trace?.latencyMs ?? "-"
                                },
                                {
                                    key: "failure",
                                    label: "失败原因",
                                    children: trace?.failureReason ?? "-"
                                },
                                {
                                    key: "retrievedAt",
                                    label: "检索时间",
                                    children: formatTime(trace?.retrievedAt)
                                }
                            ]}
                            size="small"
                            style={{ marginTop: 16 }}
                        />
                        <Text code className="qa-admin-raw-json">
                            {formatRawJson(trace?.raw)}
                        </Text>
                    </Card>
                </KuzhambuSpace>
            </section>
        </main>
    );
};
