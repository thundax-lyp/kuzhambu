import { useMutation, useQuery } from "@tanstack/react-query";
import { Bubble, Sender, type BubbleItemType } from "@ant-design/x";
import { Empty, Input, Tag, Typography } from "antd";
import { forwardRef, useMemo, useState, type ComponentProps, type ElementRef } from "react";
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

const DEFAULT_OWNER_USER_ID = "1001";
const DEFAULT_PAGE_SIZE = 20;
const FIXED_MODEL = "kuzhambu-qa";
const FULL_LIBRARY_CONTEXT_MODE = "GENERAL";
const FULL_LIBRARY_SESSION_TITLE = "新对话";
const SESSION_TITLE_MAX_LENGTH = 24;
const QaSenderInput = forwardRef<
    ElementRef<typeof Input.TextArea>,
    ComponentProps<typeof Input.TextArea>
>((props, ref) => <Input.TextArea {...props} ref={ref} aria-label="问题" />);
QaSenderInput.displayName = "QaSenderInput";
const QA_SENDER_COMPONENTS = { input: QaSenderInput };

interface QaFormState {
    question: string;
}

interface QaTimelineMessage {
    content: string;
    id: string;
    role: "assistant" | "user";
    sources?: DiscoveryQaSourceRecord[];
    status: "failed" | "loading" | "succeeded";
}

type QaTimeline = Record<string, QaTimelineMessage[]>;

const INITIAL_FORM_STATE: QaFormState = {
    question: ""
};

const parseNumber = (value: string) => {
    const trimmed = value.trim();
    if (!trimmed) {
        return null;
    }

    const parsed = Number.parseInt(trimmed, 10);
    return Number.isNaN(parsed) ? null : parsed;
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

const toConversationTitle = (question: string) => {
    const normalizedQuestion = question.replace(/\s+/g, " ").trim();
    if (!normalizedQuestion) {
        return FULL_LIBRARY_SESSION_TITLE;
    }
    if (normalizedQuestion.length <= SESSION_TITLE_MAX_LENGTH) {
        return normalizedQuestion;
    }
    return `${normalizedQuestion.slice(0, SESSION_TITLE_MAX_LENGTH)}...`;
};

const toOpenSessionRequest = (ownerUserId: number | null, title = FULL_LIBRARY_SESSION_TITLE) => {
    return {
        contextContentId: null,
        contextContentType: null,
        contextMode: FULL_LIBRARY_CONTEXT_MODE,
        ownerUserId,
        requestId: null,
        scope: "PORTAL",
        title,
        traceId: null
    };
};

const toSourceKey = (source: DiscoveryQaSourceRecord, index: number) => {
    return source.sourceId ?? `${source.contentType ?? "SOURCE"}-${source.contentId ?? index}`;
};

const toBubbleStatus = (status: QaTimelineMessage["status"]) => {
    if (status === "loading") {
        return "loading";
    }
    if (status === "failed") {
        return "error";
    }
    return "success";
};

const toBubbleTagColor = (status: QaTimelineMessage["status"]) => {
    if (status === "failed") {
        return "red";
    }
    if (status === "loading") {
        return "processing";
    }
    return "blue";
};

export const QaPage = () => {
    const [form, setForm] = useState<QaFormState>(INITIAL_FORM_STATE);
    const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);
    const [timelineBySession, setTimelineBySession] = useState<QaTimeline>({});
    const [operationMessage, setOperationMessage] = useState<string | null>(null);
    const ownerUserId = parseNumber(DEFAULT_OWNER_USER_ID);

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
    const chatCompletionMutation = useMutation({
        mutationFn: service.createQaChatCompletionStream
    });
    const deleteSessionMutation = useMutation({
        mutationFn: service.deleteQaSession,
        onError: (error) => {
            setOperationMessage(error instanceof Error ? error.message : "删除对话失败");
        }
    });
    const exportSessionMutation = useMutation({
        mutationFn: service.createQaSessionExport,
        onSuccess: (result) => {
            if (result.exportStatus === "FAILED") {
                setOperationMessage(result.failureReason ?? "导出失败");
                return;
            }
            setOperationMessage(`导出完成：${result.filename ?? "问答会话.csv"}`);
        },
        onError: (error) => {
            setOperationMessage(error instanceof Error ? error.message : "导出失败");
        }
    });

    const sessions = useMemo(() => {
        const data = sessionsQuery.data;
        return data?.items ?? data?.records ?? [];
    }, [sessionsQuery.data]);
    const messages = useMemo(() => {
        return selectedSessionId ? (timelineBySession[selectedSessionId] ?? []) : [];
    }, [selectedSessionId, timelineBySession]);
    const selectedSession = selectedSessionQuery.data;
    const latestAssistantMessage = [...messages]
        .reverse()
        .find((message) => message.role === "assistant" && message.sources?.length);
    const bubbleItems = useMemo<BubbleItemType[]>(() => {
        return messages.map((message) => ({
            content:
                message.content ||
                (message.status === "loading" ? "正在生成回答..." : "未返回回答内容"),
            footer: (
                <KuzhambuSpace size={8}>
                    <Tag color={toBubbleTagColor(message.status)}>{message.status}</Tag>
                    {message.sources?.length ? (
                        <Text type="secondary">{message.sources.length} 个来源</Text>
                    ) : null}
                </KuzhambuSpace>
            ),
            key: message.id,
            loading: message.status === "loading",
            role: message.role === "user" ? "user" : "ai",
            status: toBubbleStatus(message.status),
            variant: message.role === "user" ? "filled" : "outlined"
        }));
    }, [messages]);

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

    const ensureSessionId = async (question: string) => {
        if (selectedSessionId !== null) {
            return selectedSessionId;
        }

        const session = await openSessionMutation.mutateAsync(
            toOpenSessionRequest(ownerUserId, toConversationTitle(question))
        );
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

    const createNewSession = async () => {
        setOperationMessage(null);
        setForm(INITIAL_FORM_STATE);
        const session = await openSessionMutation.mutateAsync(toOpenSessionRequest(ownerUserId));
        const nextSessionId = toSessionId(session.sessionId);
        if (nextSessionId === null) {
            setOperationMessage("新建对话失败：会话未返回会话号");
            return;
        }

        setSelectedSessionId(nextSessionId);
        setTimelineBySession((current) => ({
            ...current,
            [nextSessionId]: []
        }));
        void sessionsQuery.refetch();
    };

    const deleteSession = async (sessionId: string) => {
        setOperationMessage(null);
        await deleteSessionMutation.mutateAsync({ ownerUserId, sessionId });
        setTimelineBySession((current) => {
            const next = { ...current };
            delete next[sessionId];
            return next;
        });
        if (selectedSessionId === sessionId) {
            setSelectedSessionId(null);
        }
        void sessionsQuery.refetch();
    };

    const submitQuestion = async (questionValue = form.question) => {
        const question = questionValue.trim();
        if (!question) {
            return;
        }

        setOperationMessage(null);
        let nextSessionId: string | null = null;
        let assistantMessageId: string | null = null;
        try {
            nextSessionId = await ensureSessionId(question);
            const activeSessionId = nextSessionId;
            assistantMessageId = createMessageId();
            const activeAssistantMessageId = assistantMessageId;
            appendMessage(activeSessionId, {
                content: question,
                id: createMessageId(),
                role: "user",
                status: "succeeded"
            });
            appendMessage(activeSessionId, {
                content: "正在生成回答...",
                id: activeAssistantMessageId,
                role: "assistant",
                status: "loading"
            });
            updateField("question", "");

            let streamedAnswer = "";
            const response = await chatCompletionMutation.mutateAsync({
                command: {
                    messages: [{ content: question, role: "user" }],
                    metadata: {
                        contextContentId: null,
                        contextContentType: null,
                        contextMode: FULL_LIBRARY_CONTEXT_MODE,
                        sessionId: activeSessionId
                    },
                    model: FIXED_MODEL,
                    requestId: null,
                    sessionId: activeSessionId,
                    stream: true,
                    traceId: null
                },
                onDelta: (content) => {
                    streamedAnswer += content;
                    updateMessage(activeSessionId, activeAssistantMessageId, {
                        content: streamedAnswer,
                        status: "loading"
                    });
                },
                onError: (message) => {
                    updateMessage(activeSessionId, activeAssistantMessageId, {
                        content: message,
                        status: "failed"
                    });
                }
            });
            const answerText = extractAnswerText(response) || streamedAnswer;
            updateMessage(activeSessionId, activeAssistantMessageId, {
                content: answerText || response.failureReason || "未返回回答内容",
                sources: response.sources ?? [],
                status: response.answerStatus === "FAILED" ? "failed" : "succeeded"
            });
        } catch (error) {
            const message = error instanceof Error ? error.message : "回答生成失败";
            if (nextSessionId !== null && assistantMessageId !== null) {
                updateMessage(nextSessionId, assistantMessageId, {
                    content: message,
                    status: "failed"
                });
                return;
            }
            setOperationMessage(message);
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
            <aside className="discovery-qa-page__sidebar">
                <KuzhambuButton
                    block
                    testId="discovery-qa-create-session-button"
                    loading={openSessionMutation.isPending}
                    type="primary"
                    onClick={() => void createNewSession()}
                >
                    新建对话
                </KuzhambuButton>
                <div className="discovery-qa-page__session-list" aria-label="问答会话">
                    {sessions.length ? (
                        sessions.map((session) => {
                            const sessionId = toSessionId(session.sessionId);
                            if (sessionId === null) {
                                return null;
                            }

                            return (
                                <div key={sessionId} className="discovery-qa-page__session-item">
                                    <KuzhambuButton
                                        block
                                        testId="discovery-qa-select-session-button"
                                        type={
                                            sessionId === selectedSessionId ? "primary" : "default"
                                        }
                                        onClick={() => setSelectedSessionId(sessionId)}
                                    >
                                        {sessionTitle(session)}
                                    </KuzhambuButton>
                                    <KuzhambuButton
                                        aria-label={`删除对话 ${sessionTitle(session)}`}
                                        className="discovery-qa-page__delete-session"
                                        disabled={deleteSessionMutation.isPending}
                                        testId="discovery-qa-delete-session-button"
                                        onClick={(event) => {
                                            event.stopPropagation();
                                            void deleteSession(sessionId);
                                        }}
                                    >
                                        删除
                                    </KuzhambuButton>
                                </div>
                            );
                        })
                    ) : (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description={sessionsQuery.isPending ? "正在加载对话" : "还没有对话"}
                        />
                    )}
                </div>
                <KuzhambuButton
                    testId="discovery-qa-export-session-button"
                    disabled={selectedSessionId === null}
                    loading={exportSessionMutation.isPending}
                    onClick={exportCurrentSession}
                >
                    导出对话
                </KuzhambuButton>
            </aside>

            <section className="discovery-qa-page__chat">
                <header className="discovery-qa-page__chat-header">
                    <div>
                        <Title level={2}>知识助手</Title>
                        <Text type="secondary">提问后，我会在知识库中查询并附上来源。</Text>
                    </div>
                    {selectedSession ? (
                        <Text type="secondary">
                            {sessionTitle(selectedSession)} · {formatTime(selectedSession.openedAt)}
                        </Text>
                    ) : null}
                </header>

                {operationMessage ? (
                    <Text className="discovery-qa-page__notice" type="secondary">
                        {operationMessage}
                    </Text>
                ) : null}

                <div className="discovery-qa-page__messages" aria-label="问答消息">
                    {bubbleItems.length ? (
                        <Bubble.List
                            autoScroll
                            items={bubbleItems}
                            role={{
                                ai: {
                                    placement: "start",
                                    shape: "corner"
                                },
                                user: {
                                    placement: "end",
                                    shape: "corner"
                                }
                            }}
                        />
                    ) : (
                        <div className="discovery-qa-page__empty">
                            <Title level={3}>我能帮你解答什么？</Title>
                            <Text type="secondary">问一个问题，开始新的对话。</Text>
                        </div>
                    )}
                </div>

                <div className="discovery-qa-page__sources" aria-label="回答来源">
                    {latestAssistantMessage?.sources?.length
                        ? latestAssistantMessage.sources.map((source, index) => (
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
                        : null}
                </div>

                <Sender
                    autoSize={false}
                    className="discovery-qa-page__composer"
                    components={QA_SENDER_COMPONENTS}
                    loading={openSessionMutation.isPending || chatCompletionMutation.isPending}
                    placeholder="发送消息"
                    submitType="enter"
                    value={form.question}
                    onChange={(value) => updateField("question", value)}
                    onSubmit={(message) => void submitQuestion(message)}
                    suffix={(_, { components }) => (
                        <components.SendButton
                            aria-label="发送问题"
                            data-testid="discovery-qa-send-question-button"
                        />
                    )}
                />
            </section>
        </main>
    );
};
