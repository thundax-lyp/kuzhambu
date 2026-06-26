import { useMutation } from "@tanstack/react-query";
import { Button, Card, Descriptions, Input, Typography } from "antd";
import { useState } from "react";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as service from "./qa-admin-service";
import type {
    DiscoveryQaSessionDetailRecord,
    DiscoveryQaSourceRecord,
    DiscoveryQaTraceRecord
} from "./qa-admin-types";
import "./qa-admin-page.css";

const { Text, Title } = Typography;

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

export const QaAdminPage = () => {
    const [sessionId, setSessionId] = useState("2001");
    const [messageId, setMessageId] = useState("4001");
    const [traceId, setTraceId] = useState("9001");
    const [sessionDetail, setSessionDetail] = useState<DiscoveryQaSessionDetailRecord | null>(null);
    const [sources, setSources] = useState<DiscoveryQaSourceRecord[]>([]);
    const [trace, setTrace] = useState<DiscoveryQaTraceRecord | null>(null);

    const sessionMutation = useMutation({
        mutationFn: service.getQaSessionDetail,
        onSuccess: (nextDetail) => {
            setSessionDetail(nextDetail);
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

    return (
        <main className="kuzhambu-page discovery-admin-page qa-admin-page">
            <section className="kuzhambu-page-panel">
                <header className="kuzhambu-page-header">
                    <div>
                        <Text className="kuzhambu-page-eyebrow">Discovery / QA Admin</Text>
                        <Title level={2}>问答调试台</Title>
                        <Text type="secondary">
                            查看会话、来源和检索轨迹，验证知识中心的问答链路。
                        </Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
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
                            <Button
                                loading={sessionMutation.isPending}
                                onClick={() =>
                                    sessionMutation.mutate({
                                        sessionId: Number.parseInt(sessionId, 10)
                                    })
                                }
                                type="primary"
                            >
                                加载会话
                            </Button>
                        </KuzhambuSpace>

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
                                    children: sessionDetail?.status ?? "-"
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
                                    <Card
                                        key={
                                            message.messageId ??
                                            `${message.role}-${message.content}`
                                        }
                                        size="small"
                                    >
                                        <KuzhambuSpace
                                            orientation="vertical"
                                            size={4}
                                            style={{ width: "100%" }}
                                        >
                                            <Text strong>
                                                {message.role ?? "-"} ·{" "}
                                                {message.messageStatus ?? "-"}
                                            </Text>
                                            <Text>{message.content ?? "-"}</Text>
                                            <Text type="secondary">
                                                轮次 {message.contextTurnCount ?? "-"} · 发送
                                                {formatTime(message.sentAt)} · 回答
                                                {formatTime(message.answeredAt)}
                                            </Text>
                                        </KuzhambuSpace>
                                    </Card>
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
                            <Button
                                loading={sourceMutation.isPending}
                                onClick={() =>
                                    sourceMutation.mutate({
                                        messageId: Number.parseInt(messageId, 10)
                                    })
                                }
                                type="primary"
                            >
                                加载来源
                            </Button>
                        </KuzhambuSpace>

                        <KuzhambuSpace
                            orientation="vertical"
                            size={8}
                            style={{ marginTop: 16, width: "100%" }}
                        >
                            {sources.length ? (
                                sources.map((source) => (
                                    <Card
                                        key={
                                            source.sourceId ??
                                            `${source.contentType}-${source.contentId}`
                                        }
                                        size="small"
                                    >
                                        <KuzhambuSpace
                                            orientation="vertical"
                                            size={4}
                                            style={{ width: "100%" }}
                                        >
                                            <Text strong>{source.titleSnapshot ?? "-"}</Text>
                                            <Text type="secondary">
                                                {source.knowledgeBase ?? "-"} ·{" "}
                                                {source.contentType ?? "-"} · 排序{" "}
                                                {source.sourceRank ?? "-"} · 得分{" "}
                                                {source.score ?? "-"}
                                            </Text>
                                            <Text>{source.snippet ?? "-"}</Text>
                                        </KuzhambuSpace>
                                    </Card>
                                ))
                            ) : (
                                <Text type="secondary">暂无来源。</Text>
                            )}
                        </KuzhambuSpace>
                    </Card>

                    <Card title="检索轨迹" size="small">
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
                            <Button
                                loading={traceMutation.isPending}
                                onClick={() =>
                                    traceMutation.mutate({
                                        traceId: Number.parseInt(traceId, 10)
                                    })
                                }
                                type="primary"
                            >
                                加载轨迹
                            </Button>
                        </KuzhambuSpace>

                        <Descriptions
                            bordered
                            column={2}
                            items={[
                                {
                                    key: "rawQuestion",
                                    label: "原始问题",
                                    children: trace?.rawQuestion ?? "-"
                                },
                                {
                                    key: "rewrittenQuestion",
                                    label: "改写问题",
                                    children: trace?.rewrittenQuestion ?? "-"
                                },
                                {
                                    key: "candidateCount",
                                    label: "候选数",
                                    children: trace?.candidateCount ?? "-"
                                },
                                { key: "scope", label: "作用域", children: trace?.scope ?? "-" },
                                {
                                    key: "retrievedAt",
                                    label: "检索时间",
                                    children: formatTime(trace?.retrievedAt)
                                }
                            ]}
                            size="small"
                            style={{ marginTop: 16 }}
                        />

                        <Descriptions
                            bordered
                            column={1}
                            items={[
                                {
                                    key: "filtersJson",
                                    label: "过滤条件 JSON",
                                    children: trace?.filtersJson ?? "-"
                                },
                                {
                                    key: "expandedTermsJson",
                                    label: "扩展词 JSON",
                                    children: trace?.expandedTermsJson ?? "-"
                                },
                                {
                                    key: "linkedEntitiesJson",
                                    label: "关联实体 JSON",
                                    children: trace?.linkedEntitiesJson ?? "-"
                                },
                                {
                                    key: "contextSnapshot",
                                    label: "上下文快照",
                                    children: trace?.contextSnapshot ?? "-"
                                }
                            ]}
                            size="small"
                            style={{ marginTop: 16 }}
                        />
                    </Card>
                </KuzhambuSpace>
            </section>
        </main>
    );
};
