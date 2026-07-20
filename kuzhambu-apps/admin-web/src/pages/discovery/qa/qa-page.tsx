import { useMutation, useQuery } from "@tanstack/react-query";
import { Card, Empty, Input, Select, Tag, Typography } from "antd";
import { useMemo, useState, type FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as service from "./qa-service";
import type {
    DiscoveryQaChatCompletionRecord,
    DiscoveryQaSessionRecord,
    DiscoveryQaSourceRecord
} from "./qa-types";
import "./qa-page.css";

const { Text, Title } = Typography;
const { TextArea } = Input;

const DEFAULT_OWNER_USER_ID = "1001";
const DEFAULT_PAGE_SIZE = 20;
const FIXED_MODEL = "kuzhambu-qa";
const SINGLE_DOCUMENT_MODE = "SINGLE_DOCUMENT";
const WANGQI_DOCUMENT_TYPE = "WANGQI_DOCUMENT";

const CONTEXT_MODE_OPTIONS = [
    { label: "通用问答", value: "GENERAL" },
    { label: "单文档追问", value: SINGLE_DOCUMENT_MODE }
];

const CONTEXT_TYPE_OPTIONS = [
    { label: "未限定", value: "" },
    { label: "王圻文档", value: WANGQI_DOCUMENT_TYPE },
    { label: "三才图会", value: "SANCAI_ENTRY" },
    { label: "明代习俗", value: "MING_CUSTOMS" }
];

interface QaFormState {
    contextContentId: string;
    contextContentType: string;
    contextMode: string;
    ownerUserId: string;
    question: string;
    requestId: string;
    sessionTitle: string;
    traceId: string;
}

interface QaTimelineMessage {
    content: string;
    id: string;
    role: "assistant" | "user";
    sources?: DiscoveryQaSourceRecord[];
    status: "failed" | "loading" | "succeeded";
}

type QaTimeline = Record<string, QaTimelineMessage[]>;

const toInitialFormState = (searchParams: URLSearchParams): QaFormState => {
    const contextMode = searchParams.get("contextMode");
    const contextContentType = searchParams.get("contextContentType");
    const contextContentId = searchParams.get("contextContentId");
    const title = searchParams.get("title");
    if (
        contextMode === SINGLE_DOCUMENT_MODE &&
        contextContentType === WANGQI_DOCUMENT_TYPE &&
        contextContentId
    ) {
        return {
            contextContentId,
            contextContentType,
            contextMode,
            ownerUserId: DEFAULT_OWNER_USER_ID,
            question: "",
            requestId: "",
            sessionTitle: title?.trim() || "王圻文档问答",
            traceId: ""
        };
    }

    return {
        contextContentId: "",
        contextContentType: "",
        contextMode: "GENERAL",
        ownerUserId: DEFAULT_OWNER_USER_ID,
        question: "",
        requestId: "",
        sessionTitle: "知识中心问答",
        traceId: ""
    };
};

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

const toSessionId = (value?: string | null) => {
    return typeof value === "string" && value.trim().length ? value : null;
};

const formatTime = (value?: number | null) => {
    if (!value) {
        return "-";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
};

const sessionTitle = (session?: DiscoveryQaSessionRecord) => {
    if (session?.title) {
        return session.title;
    }
    return session?.sessionId ? `会话 ${session.sessionId}` : "未命名会话";
};

const createMessageId = () => {
    const suffix = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
    return `discovery-qa-message-${suffix}`;
};

const extractAnswerText = (response?: DiscoveryQaChatCompletionRecord | null) => {
    return response?.choices?.[0]?.message?.content?.trim() || "";
};

const toOpenSessionRequest = (form: QaFormState) => {
    return {
        contextContentId: parseNumber(form.contextContentId),
        contextContentType: parseString(form.contextContentType),
        contextMode: parseString(form.contextMode),
        ownerUserId: parseNumber(form.ownerUserId),
        requestId: parseString(form.requestId),
        scope: "PORTAL",
        title: parseString(form.sessionTitle),
        traceId: parseString(form.traceId)
    };
};

const toSourceKey = (source: DiscoveryQaSourceRecord, index: number) => {
    return source.sourceId ?? `${source.contentType ?? "SOURCE"}-${source.contentId ?? index}`;
};

export const QaPage = () => {
    const [searchParams] = useSearchParams();
    const [form, setForm] = useState<QaFormState>(() => toInitialFormState(searchParams));
    const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);
    const [timelineBySession, setTimelineBySession] = useState<QaTimeline>({});
    const [operationMessage, setOperationMessage] = useState<string | null>(null);
    const ownerUserId = parseNumber(form.ownerUserId);

    const sessionsQuery = useQuery({
        queryFn: () =>
            service.pageQaSessions({
                ownerUserId,
                pageNo: 1,
                pageSize: DEFAULT_PAGE_SIZE,
                scope: "PORTAL"
            }),
        queryKey: ["discovery-qa", "session-page", ownerUserId]
    });
    const selectedSessionQuery = useQuery({
        enabled: selectedSessionId !== null,
        queryFn: () => {
            if (selectedSessionId === null) {
                throw new Error("会话尚未选中");
            }
            return service.getQaSession({ ownerUserId, sessionId: selectedSessionId });
        },
        queryKey: ["discovery-qa", "session", selectedSessionId, ownerUserId]
    });

    const openSessionMutation = useMutation({ mutationFn: service.createQaSession });
    const chatCompletionMutation = useMutation({ mutationFn: service.createQaChatCompletion });
    const exportSessionMutation = useMutation({
        mutationFn: service.createQaSessionExport,
        onSuccess: (result) => {
            if (result.exportStatus === "FAILED") {
                setOperationMessage(result.failureReason ?? "导出失败");
                return;
            }
            setOperationMessage(`导出成功：${result.filename ?? "问答会话.csv"}`);
        },
        onError: (error) => {
            setOperationMessage(error instanceof Error ? error.message : "导出失败");
        }
    });

    const sessions = useMemo(() => {
        const data = sessionsQuery.data;
        return data?.items ?? data?.records ?? [];
    }, [sessionsQuery.data]);
    const messages = selectedSessionId ? (timelineBySession[selectedSessionId] ?? []) : [];
    const selectedSession = selectedSessionQuery.data;
    const latestAssistantMessage = [...messages]
        .reverse()
        .find((message) => message.role === "assistant" && message.sources?.length);
    const hasFixedContext =
        form.contextMode === SINGLE_DOCUMENT_MODE &&
        form.contextContentType === WANGQI_DOCUMENT_TYPE &&
        Boolean(form.contextContentId);

    const updateField = (key: keyof QaFormState, value: string) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };

    const appendMessage = (sessionId: string, message: QaTimelineMessage) => {
        setTimelineBySession((current) => ({
            ...current,
            [sessionId]: [...(current[sessionId] ?? []), message]
        }));
    };

    const updateMessage = (
        sessionId: string,
        messageId: string,
        patch: Partial<QaTimelineMessage>
    ) => {
        setTimelineBySession((current) => ({
            ...current,
            [sessionId]: (current[sessionId] ?? []).map((message) =>
                message.id === messageId ? { ...message, ...patch } : message
            )
        }));
    };

    const ensureSessionId = async () => {
        if (selectedSessionId !== null) {
            return selectedSessionId;
        }

        const session = await openSessionMutation.mutateAsync(toOpenSessionRequest(form));
        const nextSessionId = toSessionId(session.sessionId);
        if (nextSessionId === null) {
            throw new Error("会话未返回会话号");
        }

        setSelectedSessionId(nextSessionId);
        setTimelineBySession((current) => ({
            ...current,
            [nextSessionId]: current[nextSessionId] ?? []
        }));
        void sessionsQuery.refetch();
        return nextSessionId;
    };

    const submitQuestion = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const question = form.question.trim();
        if (!question) {
            return;
        }

        setOperationMessage(null);
        const nextSessionId = await ensureSessionId();
        const assistantMessageId = createMessageId();
        appendMessage(nextSessionId, {
            content: question,
            id: createMessageId(),
            role: "user",
            status: "succeeded"
        });
        appendMessage(nextSessionId, {
            content: "正在生成回答...",
            id: assistantMessageId,
            role: "assistant",
            status: "loading"
        });
        updateField("question", "");

        try {
            const response = await chatCompletionMutation.mutateAsync({
                messages: [{ content: question, role: "user" }],
                metadata: {
                    contextContentId: parseNumber(form.contextContentId),
                    contextContentType: parseString(form.contextContentType),
                    contextMode: parseString(form.contextMode),
                    sessionId: nextSessionId
                },
                model: FIXED_MODEL,
                requestId: parseString(form.requestId),
                sessionId: nextSessionId,
                stream: false,
                traceId: parseString(form.traceId)
            });
            const answerText = extractAnswerText(response);
            updateMessage(nextSessionId, assistantMessageId, {
                content: answerText || response.failureReason || "未返回回答内容",
                sources: response.sources ?? [],
                status: response.answerStatus === "FAILED" ? "failed" : "succeeded"
            });
        } catch (error) {
            updateMessage(nextSessionId, assistantMessageId, {
                content: error instanceof Error ? error.message : "回答生成失败",
                status: "failed"
            });
        }
    };

    const exportCurrentSession = () => {
        if (selectedSessionId === null) {
            return;
        }
        exportSessionMutation.mutate({
            format: "CSV",
            ownerUserId,
            sessionId: selectedSessionId
        });
    };

    return (
        <main className="kuzhambu-page qa-page discovery-qa-page">
            <header className="discovery-qa-page__header">
                <div>
                    <Title level={2}>智能问答</Title>
                    <Text type="secondary">跨知识库提问，保留会话、回答和来源引用。</Text>
                </div>
                <KuzhambuButton
                    testId="discovery-qa-export-session-button"
                    disabled={selectedSessionId === null}
                    loading={exportSessionMutation.isPending}
                    onClick={exportCurrentSession}
                >
                    导出 CSV
                </KuzhambuButton>
            </header>

            {operationMessage ? <Text type="secondary">{operationMessage}</Text> : null}

            <section className="discovery-qa-page__body">
                <Card title="会话" size="small">
                    <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                        <label>
                            <Text type="secondary">用户 ID</Text>
                            <Input
                                aria-label="用户 ID"
                                value={form.ownerUserId}
                                onChange={(event) => updateField("ownerUserId", event.target.value)}
                            />
                        </label>
                        <div className="discovery-qa-page__session-list" aria-label="问答会话">
                            {sessions.length ? (
                                sessions.map((session) => {
                                    const sessionId = toSessionId(session.sessionId);
                                    if (sessionId === null) {
                                        return null;
                                    }

                                    return (
                                        <KuzhambuButton
                                            key={sessionId}
                                            testId="discovery-qa-select-session-button"
                                            type={
                                                sessionId === selectedSessionId
                                                    ? "primary"
                                                    : "default"
                                            }
                                            onClick={() => setSelectedSessionId(sessionId)}
                                        >
                                            {sessionTitle(session)}
                                        </KuzhambuButton>
                                    );
                                })
                            ) : (
                                <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description={
                                        sessionsQuery.isPending
                                            ? "会话加载中"
                                            : "暂无会话，首次提问将自动创建"
                                    }
                                />
                            )}
                        </div>
                    </KuzhambuSpace>
                </Card>

                <Card title="问答" size="small">
                    <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                        <div className="discovery-qa-page__context-grid">
                            <label>
                                <Text type="secondary">会话标题</Text>
                                <Input
                                    aria-label="会话标题"
                                    value={form.sessionTitle}
                                    onChange={(event) =>
                                        updateField("sessionTitle", event.target.value)
                                    }
                                />
                            </label>
                            <label>
                                <Text type="secondary">上下文模式</Text>
                                <Select
                                    aria-label="上下文模式"
                                    disabled={hasFixedContext}
                                    options={CONTEXT_MODE_OPTIONS}
                                    value={form.contextMode}
                                    onChange={(value) => updateField("contextMode", value)}
                                />
                            </label>
                            <label>
                                <Text type="secondary">上下文类型</Text>
                                <Select
                                    aria-label="上下文类型"
                                    disabled={hasFixedContext}
                                    options={CONTEXT_TYPE_OPTIONS}
                                    value={form.contextContentType}
                                    onChange={(value) => updateField("contextContentType", value)}
                                />
                            </label>
                            <label>
                                <Text type="secondary">上下文 ID</Text>
                                <Input
                                    aria-label="上下文 ID"
                                    disabled={hasFixedContext}
                                    value={form.contextContentId}
                                    onChange={(event) =>
                                        updateField("contextContentId", event.target.value)
                                    }
                                />
                            </label>
                            <label>
                                <Text type="secondary">请求 ID</Text>
                                <Input
                                    aria-label="请求 ID"
                                    value={form.requestId}
                                    onChange={(event) =>
                                        updateField("requestId", event.target.value)
                                    }
                                />
                            </label>
                            <label>
                                <Text type="secondary">Trace ID</Text>
                                <Input
                                    aria-label="Trace ID"
                                    value={form.traceId}
                                    onChange={(event) => updateField("traceId", event.target.value)}
                                />
                            </label>
                        </div>

                        <div className="discovery-qa-page__messages" aria-label="问答消息">
                            {messages.length ? (
                                messages.map((message) => (
                                    <article
                                        key={message.id}
                                        className={`discovery-qa-page__message discovery-qa-page__message--${message.role}`}
                                    >
                                        <div className="discovery-qa-page__message-header">
                                            <Text strong>
                                                {message.role === "user" ? "提问" : "回答"}
                                            </Text>
                                            <Tag
                                                color={message.status === "failed" ? "red" : "blue"}
                                            >
                                                {message.status}
                                            </Tag>
                                        </div>
                                        <Text>{message.content}</Text>
                                    </article>
                                ))
                            ) : (
                                <Empty
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                    description="尚未提问"
                                />
                            )}
                        </div>

                        <form className="discovery-qa-page__composer" onSubmit={submitQuestion}>
                            <KuzhambuSpace
                                orientation="vertical"
                                size={12}
                                style={{ width: "100%" }}
                            >
                                <TextArea
                                    aria-label="问题"
                                    autoSize={{ minRows: 3, maxRows: 6 }}
                                    value={form.question}
                                    onChange={(event) =>
                                        updateField("question", event.target.value)
                                    }
                                    placeholder="输入要追问的内容"
                                />
                                <KuzhambuButton
                                    testId="discovery-qa-send-question-button"
                                    htmlType="submit"
                                    loading={
                                        openSessionMutation.isPending ||
                                        chatCompletionMutation.isPending
                                    }
                                    type="primary"
                                >
                                    发送问题
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        </form>
                    </KuzhambuSpace>
                </Card>
            </section>

            <Card title="最近来源" size="small">
                <div className="discovery-qa-page__sources" aria-label="回答来源">
                    {latestAssistantMessage?.sources?.length ? (
                        latestAssistantMessage.sources.map((source, index) => (
                            <article
                                key={toSourceKey(source, index)}
                                className="discovery-qa-page__source"
                            >
                                <div className="discovery-qa-page__source-header">
                                    <Text strong>{source.titleSnapshot ?? source.sourceId}</Text>
                                    <Tag>{source.knowledgeBase ?? source.contentType ?? "-"}</Tag>
                                </div>
                                <Text type="secondary">
                                    {source.snippet ??
                                        source.locationLabel ??
                                        source.sourcePath ??
                                        "-"}
                                </Text>
                            </article>
                        ))
                    ) : (
                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无来源" />
                    )}
                </div>
            </Card>

            {selectedSession ? (
                <Text type="secondary">
                    当前会话：{sessionTitle(selectedSession)}，打开时间：
                    {formatTime(selectedSession.openedAt)}
                </Text>
            ) : null}
        </main>
    );
};
