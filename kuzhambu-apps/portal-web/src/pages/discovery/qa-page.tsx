import { useMemo, useState, type FormEvent } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { CircleSlash2, RefreshCw, Sparkles, Workflow } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import * as qaService from "./qa-service";
import type {
    QaChatCompletionChoice,
    DiscoveryQaChatCompletionResponse,
    DiscoveryQaChatCompletionSource,
    DiscoveryQaGetSessionRequest,
    DiscoveryQaOpenSessionRequest,
    DiscoveryQaOpenSessionResponse,
    DiscoveryQaSessionPageResponse
} from "./qa-types";

interface QaFormState {
    contextContentId: string;
    contextContentType: string;
    contextMode: string;
    requestId: string;
    scope: string;
    sessionId: string;
    sessionTitle: string;
    traceId: string;
    question: string;
}

interface QaTimelineMessage {
    content: string;
    id: string;
    sources?: DiscoveryQaChatCompletionSource[];
    status: "failed" | "loading" | "succeeded";
    failureReason?: string | null;
    role: "assistant" | "user";
    retryQuestion?: string;
}

type QaTimeline = Record<number, QaTimelineMessage[]>;

const DEFAULT_SESSION_SIZE = 20;
const FIXED_MODEL = "kuzhambu-qa";

const INITIAL_FORM_STATE: QaFormState = {
    contextContentId: "",
    contextContentType: "",
    contextMode: "GENERAL",
    requestId: "",
    scope: "PORTAL",
    sessionId: "",
    sessionTitle: "知识中心问答",
    traceId: "",
    question: ""
};

const parseNumber = (value: string) => {
    if (!value.trim()) {
        return null;
    }

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const parseString = (value: string) => {
    const trimmed = value.trim();
    return trimmed.length ? trimmed : null;
};

const toOpenSessionRequest = (form: QaFormState): DiscoveryQaOpenSessionRequest => {
    return {
        contextContentId: parseNumber(form.contextContentId),
        contextContentType: parseString(form.contextContentType),
        contextMode: parseString(form.contextMode),
        requestId: parseString(form.requestId),
        scope: parseString(form.scope),
        title: parseString(form.sessionTitle),
        traceId: parseString(form.traceId)
    };
};

const getSessionList = () => {
    return qaService.pageQaSessions({
        pageNo: 1,
        pageSize: DEFAULT_SESSION_SIZE,
        scope: "PORTAL"
    });
};

const toSessionId = (value?: number | null): number | null => {
    return typeof value === "number" ? value : null;
};

const sessionTitle = (session?: DiscoveryQaOpenSessionResponse) => {
    if (session?.title) {
        return session.title;
    }

    return session?.sessionId ? `会话 ${session.sessionId}` : "未命名会话";
};

const extractCompletionMessage = (response?: DiscoveryQaChatCompletionResponse | null) => {
    const choices: QaChatCompletionChoice[] = response?.choices ?? [];
    const firstChoice = choices[0];
    const message = firstChoice?.message;
    return message?.content?.trim() ?? "";
};

const formatTime = (value?: number | null) => {
    if (!value) {
        return "未设置";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
};

const isUnavailableSource = (source: DiscoveryQaChatCompletionSource) => {
    return source.sourceStatus?.toUpperCase() === "UNAVAILABLE" || !source.sourceId;
};

const toSessionQuery = (sessionId: number): DiscoveryQaGetSessionRequest => {
    return { sessionId };
};

const createTimelineMessageId = () => {
    const randomSuffix = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
    return `qa-message-${randomSuffix}`;
};

const toChatCompletionSourceKey = (source: DiscoveryQaChatCompletionSource, index: number) => {
    return source.sourceId ?? `${source.contentType}-${source.contentId}-${index}`;
};

export const DiscoveryQaPage = () => {
    const [form, setForm] = useState<QaFormState>(INITIAL_FORM_STATE);
    const [selectedSessionId, setSelectedSessionId] = useState<number | null>(null);
    const [timelineBySession, setTimelineBySession] = useState<QaTimeline>({});

    const sessionsQuery = useQuery<DiscoveryQaSessionPageResponse>({
        queryFn: getSessionList,
        queryKey: ["portal-qa-session-page"]
    });
    const selectedSessionQuery = useQuery<DiscoveryQaOpenSessionResponse>({
        enabled: selectedSessionId !== null,
        queryFn: () => {
            if (selectedSessionId === null) {
                throw new Error("会话尚未选中");
            }
            return qaService.getQaSession(toSessionQuery(selectedSessionId));
        },
        queryKey: ["portal-qa-session", selectedSessionId]
    });

    const openSessionMutation = useMutation({ mutationFn: qaService.openQaSession });
    const chatCompletionMutation = useMutation({ mutationFn: qaService.createQaChatCompletion });

    const sessions = sessionsQuery.data?.items ?? [];
    const selectedSession = selectedSessionQuery.data;
    const hasSessions = sessions.length > 0;
    const messages = useMemo(() => {
        return selectedSessionId ? (timelineBySession[selectedSessionId] ?? []) : [];
    }, [selectedSessionId, timelineBySession]);
    const selectedSessionForComposer =
        toSessionId(selectedSession?.sessionId) ??
        toSessionId(
            sessions.find((session) => session.sessionId === selectedSessionId)?.sessionId
        ) ??
        null;

    const hasNoSession = selectedSessionId === null;

    const summaryText = useMemo(() => {
        if (chatCompletionMutation.isPending) {
            return "正在生成回答";
        }

        if (!messages.length) {
            return hasNoSession ? "先输入问题，系统将自动创建会话" : "会话已就绪，继续追问";
        }

        const latest = messages[messages.length - 1];
        if (latest.role === "assistant" && latest.status === "failed") {
            return "最近一次回答失败，可尝试重试";
        }

        return `已展示 ${messages.length} 条消息`;
    }, [chatCompletionMutation.isPending, hasNoSession, messages]);

    const sessionListContent = (() => {
        if (sessionsQuery.isPending) {
            return <div className="portal-empty">会话列表加载中...</div>;
        }

        if (!hasSessions) {
            return <div className="portal-empty">暂无历史会话。首次提问将自动创建新会话。</div>;
        }

        return (
            <div className="portal-qa-session-list">
                {sessions.map((session) => {
                    const sessionId = toSessionId(session.sessionId);
                    if (sessionId === null) {
                        return null;
                    }

                    return (
                        <Button
                            key={sessionId}
                            type="button"
                            variant={sessionId === selectedSessionId ? "default" : "outline"}
                            onClick={() => handleSelectSession(sessionId)}
                        >
                            {sessionTitle(session)}
                        </Button>
                    );
                })}
            </div>
        );
    })();

    const updateField = (key: keyof QaFormState, value: string) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };

    const updateTimeline = (
        sessionId: number,
        messageId: string,
        patch: Partial<QaTimelineMessage>
    ) => {
        setTimelineBySession((current) => {
            const timeline = current[sessionId] ?? [];
            const nextTimeline = timeline.map((item) => {
                if (item.id !== messageId) {
                    return item;
                }

                return { ...item, ...patch };
            });

            return {
                ...current,
                [sessionId]: nextTimeline
            };
        });
    };

    const appendMessage = (sessionId: number, message: QaTimelineMessage) => {
        setTimelineBySession((current) => ({
            ...current,
            [sessionId]: [...(current[sessionId] ?? []), message]
        }));
    };

    const ensureSessionId = async () => {
        if (selectedSessionId !== null) {
            return selectedSessionId;
        }

        const openResponse = await openSessionMutation.mutateAsync(toOpenSessionRequest(form));
        const openedSessionId = toSessionId(openResponse.sessionId);
        if (openedSessionId === null) {
            throw new Error("会话未返回会话号");
        }

        setTimelineBySession((current) => ({
            ...current,
            [openedSessionId]: current[openedSessionId] ?? []
        }));
        setSelectedSessionId(openedSessionId);
        void sessionsQuery.refetch();
        return openedSessionId;
    };

    const sendQuestion = async (
        questionText: string,
        sessionId: number,
        existingMessageId?: string
    ) => {
        const assistantMessageId = existingMessageId ?? createTimelineMessageId();
        if (!existingMessageId) {
            appendMessage(sessionId, {
                content: questionText,
                id: createTimelineMessageId(),
                role: "user",
                status: "succeeded"
            });
            appendMessage(sessionId, {
                content: "",
                id: assistantMessageId,
                role: "assistant",
                status: "loading",
                retryQuestion: questionText
            });
        } else {
            updateTimeline(sessionId, assistantMessageId, {
                content: "",
                retryQuestion: questionText,
                sources: [],
                status: "loading",
                failureReason: null
            });
        }

        const selectedContext = selectedSession ?? null;
        let response: DiscoveryQaChatCompletionResponse;
        try {
            response = await chatCompletionMutation.mutateAsync({
                messages: [
                    {
                        content: questionText,
                        role: "user"
                    }
                ],
                metadata: {
                    contextContentId:
                        parseNumber(form.contextContentId) ??
                        parseNumber(String(selectedContext?.contextContentId ?? "")),
                    contextContentType:
                        parseString(form.contextContentType) ??
                        parseString(selectedContext?.contextContentType ?? ""),
                    sessionId
                },
                model: FIXED_MODEL,
                options: {},
                requestId: parseString(form.requestId),
                stream: false,
                traceId: parseString(form.traceId)
            });
        } catch (error) {
            updateTimeline(sessionId, assistantMessageId, {
                content: "发送失败，请重试。",
                failureReason: error instanceof Error ? error.message : "发送失败",
                status: "failed",
                sources: [],
                retryQuestion: questionText
            });
            throw error;
        }

        const answerText = extractCompletionMessage(response);
        const hasAnswer = Boolean(answerText);
        const isSucceeded = hasAnswer && response.answerStatus !== "FAILED";

        updateTimeline(sessionId, assistantMessageId, {
            content: answerText || "未返回回答内容。",
            failureReason: isSucceeded ? null : (response.failureReason ?? "回答失败"),
            sources: response.sources ?? [],
            status: isSucceeded ? "succeeded" : "failed",
            retryQuestion: isSucceeded ? undefined : questionText
        });
        setForm((current) => ({
            ...current,
            question: ""
        }));
    };

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const questionText = parseString(form.question);
        if (!questionText) {
            return;
        }

        try {
            const currentSessionId = await ensureSessionId();
            await sendQuestion(questionText, currentSessionId);
        } catch {
            return;
        }
    };

    const handleRetry = async (messageId: string) => {
        if (!selectedSessionId) {
            return;
        }

        const timeline = timelineBySession[selectedSessionId] ?? [];
        const targetMessage = timeline.find((message) => message.id === messageId);
        if (!targetMessage?.retryQuestion) {
            return;
        }

        await sendQuestion(targetMessage.retryQuestion, selectedSessionId, messageId);
    };

    const handleSelectSession = (sessionId: number) => {
        setSelectedSessionId(sessionId);
    };

    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">知识中心 · Discovery</p>
                    <h1>问答工作台</h1>
                </div>
                <Button asChild className="portal-action" size="lg" variant="outline">
                    <Link to="/">返回首页</Link>
                </Button>
            </header>

            <section className="portal-qa-hero">
                <div className="portal-qa-copy">
                    <p className="portal-qa-tag">
                        <Sparkles aria-hidden="true" size={16} />
                        对话式问答闭环，先建会话再追问
                    </p>
                    <h2>固定模型、无 Provider 直连，一律走 Discovery</h2>
                    <p>首问自动创建会话，后续消息复用会话，来源展示在每条回答下方。</p>
                </div>
                <div className="portal-qa-stat">
                    <span>当前状态</span>
                    <strong>{summaryText}</strong>
                    <small>
                        已选会话 {selectedSessionId ?? "未选择"}
                        {selectedSession?.status ? ` · ${selectedSession.status}` : ""}
                    </small>
                </div>
            </section>

            <section className="portal-qa-grid">
                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">会话列表</p>
                            <h2>选择会话继续追问</h2>
                        </div>
                        <Workflow aria-hidden="true" size={18} />
                    </div>

                    {sessionListContent}

                    <dl className="portal-qa-session-meta">
                        <div>
                            <dt>会话号</dt>
                            <dd>{selectedSession?.sessionId ?? "-"}</dd>
                        </div>
                        <div>
                            <dt>状态</dt>
                            <dd>{selectedSession?.status ?? "-"}</dd>
                        </div>
                        <div>
                            <dt>最近消息</dt>
                            <dd>{formatTime(selectedSession?.lastMessageAt)}</dd>
                        </div>
                        <div>
                            <dt>上下文</dt>
                            <dd>{selectedSession?.contextContentType ?? "-"}</dd>
                        </div>
                    </dl>
                </Card>

                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">会话元数据</p>
                            <h2>上下文与追踪</h2>
                        </div>
                    </div>

                    <div className="portal-qa-form-grid">
                        <Label className="portal-filter-field">
                            <span>会话标题</span>
                            <Input
                                name="sessionTitle"
                                value={form.sessionTitle}
                                onChange={(event) =>
                                    updateField("sessionTitle", event.target.value)
                                }
                            />
                        </Label>
                        <Label className="portal-filter-field">
                            <span>上下文模式</span>
                            <Input
                                name="contextMode"
                                value={form.contextMode}
                                onChange={(event) => updateField("contextMode", event.target.value)}
                            />
                        </Label>
                        <Label className="portal-filter-field">
                            <span>上下文类型</span>
                            <Input
                                name="contextContentType"
                                value={form.contextContentType}
                                onChange={(event) =>
                                    updateField("contextContentType", event.target.value)
                                }
                            />
                        </Label>
                        <Label className="portal-filter-field">
                            <span>上下文 ID</span>
                            <Input
                                name="contextContentId"
                                type="number"
                                value={form.contextContentId}
                                onChange={(event) =>
                                    updateField("contextContentId", event.target.value)
                                }
                            />
                        </Label>
                        <Label className="portal-filter-field">
                            <span>请求号</span>
                            <Input
                                name="requestId"
                                value={form.requestId}
                                onChange={(event) => updateField("requestId", event.target.value)}
                            />
                        </Label>
                        <Label className="portal-filter-field">
                            <span>链路号</span>
                            <Input
                                name="traceId"
                                value={form.traceId}
                                onChange={(event) => updateField("traceId", event.target.value)}
                            />
                        </Label>
                        <Label className="portal-filter-field">
                            <span>会话号</span>
                            <Input
                                name="sessionId"
                                type="number"
                                value={form.sessionId}
                                onChange={(event) => updateField("sessionId", event.target.value)}
                            />
                            <em>当前仅展示，不强制使用</em>
                        </Label>
                    </div>
                </Card>
            </section>

            <section className="portal-qa-grid">
                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">问题输入</p>
                            <h2>Composer</h2>
                        </div>
                        <CircleSlash2 aria-hidden="true" size={18} />
                    </div>

                    <form className="portal-qa-form" onSubmit={handleSubmit}>
                        <Label className="portal-filter-field portal-qa-question">
                            <span>问题</span>
                            <Textarea
                                name="question"
                                placeholder="例如：礼器常见于哪类篇章？"
                                rows={6}
                                value={form.question}
                                onChange={(event) => updateField("question", event.target.value)}
                            />
                        </Label>
                        <div className="portal-qa-actions">
                            <Button disabled={chatCompletionMutation.isPending} type="submit">
                                {chatCompletionMutation.isPending ? "回答中..." : "发送问题"}
                            </Button>
                        </div>
                    </form>

                    <dl className="portal-qa-trace">
                        <div>
                            <dt>当前会话</dt>
                            <dd>{selectedSessionForComposer ?? "未选择"}</dd>
                        </div>
                        <div>
                            <dt>模型</dt>
                            <dd>{FIXED_MODEL}</dd>
                        </div>
                        <div>
                            <dt>请求状态</dt>
                            <dd>{chatCompletionMutation.isPending ? "进行中" : "空闲"}</dd>
                        </div>
                        <div>
                            <dt>列表加载</dt>
                            <dd>{sessionsQuery.isPending ? "进行中" : "完成"}</dd>
                        </div>
                    </dl>
                </Card>

                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">消息流</p>
                            <h2>消息时间线</h2>
                        </div>
                        <Workflow aria-hidden="true" size={18} />
                    </div>

                    {messages.length ? (
                        <div className="portal-qa-timeline">
                            {messages.map((message, index) => (
                                <article className="portal-qa-message" key={message.id}>
                                    <header>
                                        <strong>{message.role}</strong>
                                        <small>
                                            {index + 1} · {message.status}
                                        </small>
                                    </header>
                                    {message.role === "assistant" ? (
                                        <div>
                                            <p>{message.content}</p>
                                            {message.status === "loading" ? (
                                                <p>正在从 Discovery 取回回答...</p>
                                            ) : null}
                                            {message.status === "failed" ? (
                                                <p>
                                                    回答失败：{message.failureReason ?? "未知错误"}
                                                </p>
                                            ) : null}
                                            {message.sources && message.sources.length ? (
                                                <div className="portal-qa-source-list">
                                                    {message.sources.map((source, sourceIndex) => {
                                                        const key = toChatCompletionSourceKey(
                                                            source,
                                                            sourceIndex
                                                        );
                                                        const hasSourcePath =
                                                            source.sourcePath !== null &&
                                                            source.sourcePath !== undefined;
                                                        const sourcePath = source.sourcePath ?? "";
                                                        const unavailable =
                                                            isUnavailableSource(source) ||
                                                            !hasSourcePath;
                                                        return (
                                                            <div key={key}>
                                                                <p>
                                                                    {unavailable ? (
                                                                        <span>
                                                                            {source.titleSnapshot ??
                                                                                source.sourceId}
                                                                        </span>
                                                                    ) : (
                                                                        <a href={sourcePath}>
                                                                            {source.titleSnapshot ??
                                                                                source.sourceId}
                                                                        </a>
                                                                    )}
                                                                </p>
                                                                <p>
                                                                    来源状态：
                                                                    {source.sourceStatus ?? "-"}
                                                                </p>
                                                                <p>
                                                                    置信来源：{source.score ?? "-"}
                                                                </p>
                                                            </div>
                                                        );
                                                    })}
                                                </div>
                                            ) : null}
                                            {message.status === "failed" ? (
                                                <Button
                                                    size="sm"
                                                    type="button"
                                                    variant="outline"
                                                    onClick={() => handleRetry(message.id)}
                                                >
                                                    重试
                                                </Button>
                                            ) : null}
                                        </div>
                                    ) : (
                                        <p>{message.content}</p>
                                    )}
                                    {message.role === "user" && selectedSessionId ? (
                                        <p className="portal-empty">
                                            会话 {selectedSessionId} 的用户提问
                                        </p>
                                    ) : null}
                                </article>
                            ))}
                        </div>
                    ) : (
                        <div className="portal-empty">发送问题后会出现在这里。</div>
                    )}
                </Card>
            </section>

            <section className="portal-qa-results">
                <Card className="portal-qa-panel">
                    <div className="portal-qa-panel-header">
                        <div>
                            <p className="portal-kicker">操作提示</p>
                            <h2>会话与追问规则</h2>
                        </div>
                        <RefreshCw aria-hidden="true" size={18} />
                    </div>
                    <p>当前实现默认使用 OpenAI-compatible 的 chat/completions。</p>
                    <p>第一条问题会先创建会话，再在同会话下追加后续问题。</p>
                    <p>回答可见即展示，失败会展示重试入口。</p>
                </Card>
            </section>
        </main>
    );
};
