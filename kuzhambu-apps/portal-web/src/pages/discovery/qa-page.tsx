import { useState, type FormEvent, type KeyboardEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { SendHorizontal } from "lucide-react";
import { useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import ancientReaderAvatar from "@/assets/discovery/ancient-reader-avatar-face.jpg";
import ancientScholarAvatar from "@/assets/discovery/ancient-scholar-avatar-face.jpg";
import * as qaService from "./qa-service";
import type {
    DiscoveryQaChatCompletionResponse,
    DiscoveryQaChatCompletionSource,
    DiscoveryQaOpenSessionRequest,
    DiscoveryQaOpenSessionResponse,
    QaChatCompletionChoice
} from "./qa-types";

interface QaFormState {
    contextContentId: string;
    contextContentType: string;
    contextMode: string;
    ownerUserId: string;
    requestId: string;
    scope: string;
    sessionTitle: string;
    traceId: string;
    question: string;
}

interface QaTimelineMessage {
    content: string;
    failureReason?: string | null;
    id: string;
    retryQuestion?: string;
    role: "assistant" | "user";
    sources?: DiscoveryQaChatCompletionSource[];
    status: "failed" | "loading" | "succeeded";
}

interface StoredQaSession {
    contextKey: string;
    expiresAt: number;
    session: DiscoveryQaOpenSessionResponse;
}

const FIXED_MODEL = "kuzhambu-qa";
const SESSION_STORAGE_KEY = "kuzhambu.portal.discovery.qa.session";
const SESSION_TTL_MS = 30 * 60 * 1000;
const SINGLE_DOCUMENT_MODE = "SINGLE_DOCUMENT";
const WANGQI_DOCUMENT_TYPE = "WANGQI_DOCUMENT";

const INITIAL_FORM_STATE: QaFormState = {
    contextContentId: "",
    contextContentType: "",
    contextMode: "GENERAL",
    ownerUserId: "1001",
    requestId: "",
    scope: "PORTAL",
    sessionTitle: "知识中心问答",
    traceId: "",
    question: ""
};

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
            ...INITIAL_FORM_STATE,
            contextContentId,
            contextContentType,
            contextMode,
            sessionTitle: title?.trim() || "王圻文档问答"
        };
    }

    return INITIAL_FORM_STATE;
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
        ownerUserId: parseNumber(form.ownerUserId),
        requestId: parseString(form.requestId),
        scope: parseString(form.scope),
        title: parseString(form.sessionTitle),
        traceId: parseString(form.traceId)
    };
};

const toSessionId = (value?: string | null): string | null => {
    return typeof value === "string" && value.trim().length ? value : null;
};

const extractCompletionMessage = (response?: DiscoveryQaChatCompletionResponse | null) => {
    const choices: QaChatCompletionChoice[] = response?.choices ?? [];
    const firstChoice = choices[0];
    const message = firstChoice?.message;
    return message?.content?.trim() ?? "";
};

const isUnavailableSource = (source: DiscoveryQaChatCompletionSource) => {
    return source.sourceStatus?.toUpperCase() === "UNAVAILABLE" || !source.sourceId;
};

const createTimelineMessageId = () => {
    const randomSuffix = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
    return `qa-message-${randomSuffix}`;
};

const toChatCompletionSourceKey = (source: DiscoveryQaChatCompletionSource, index: number) => {
    return source.sourceId ?? `${source.contentType}-${source.contentId}-${index}`;
};

const getSessionContextKey = (form: QaFormState) => {
    return [
        form.scope,
        form.ownerUserId,
        form.contextMode,
        form.contextContentType,
        form.contextContentId
    ].join("|");
};

const readStoredSession = (form: QaFormState): DiscoveryQaOpenSessionResponse | null => {
    try {
        const value = globalThis.localStorage?.getItem(SESSION_STORAGE_KEY);
        if (!value) {
            return null;
        }

        const stored = JSON.parse(value) as StoredQaSession;
        if (stored.expiresAt <= Date.now()) {
            globalThis.localStorage?.removeItem(SESSION_STORAGE_KEY);
            return null;
        }

        return stored.contextKey === getSessionContextKey(form) ? stored.session : null;
    } catch {
        return null;
    }
};

const writeStoredSession = (form: QaFormState, session: DiscoveryQaOpenSessionResponse) => {
    try {
        globalThis.localStorage?.setItem(
            SESSION_STORAGE_KEY,
            JSON.stringify({
                contextKey: getSessionContextKey(form),
                expiresAt: Date.now() + SESSION_TTL_MS,
                session
            } satisfies StoredQaSession)
        );
    } catch {
        return;
    }
};

const formatContextLabel = (
    contextContentType?: string | null,
    contextContentId?: number | string | null
) => {
    if (!contextContentType) {
        return null;
    }

    return contextContentId ? `${contextContentType} #${contextContentId}` : contextContentType;
};

export const DiscoveryQaPage = () => {
    const [searchParams] = useSearchParams();
    const initialForm = toInitialFormState(searchParams);
    const [form, setForm] = useState<QaFormState>(() => initialForm);
    const [selectedSession, setSelectedSession] = useState<DiscoveryQaOpenSessionResponse | null>(
        () => readStoredSession(initialForm)
    );
    const [messages, setMessages] = useState<QaTimelineMessage[]>([]);

    const openSessionMutation = useMutation({ mutationFn: qaService.openQaSession });
    const chatCompletionMutation = useMutation({
        mutationFn: qaService.createQaChatCompletionStream
    });

    const selectedSessionId = toSessionId(selectedSession?.sessionId);
    const hasWangqiSingleDocumentContext =
        form.contextMode === SINGLE_DOCUMENT_MODE &&
        form.contextContentType === WANGQI_DOCUMENT_TYPE &&
        Boolean(form.contextContentId);
    const contextLabel = formatContextLabel(form.contextContentType, form.contextContentId);

    const updateQuestion = (value: string) => {
        setForm((current) => ({
            ...current,
            question: value
        }));
    };

    const updateMessage = (messageId: string, patch: Partial<QaTimelineMessage>) => {
        setMessages((current) =>
            current.map((message) =>
                message.id === messageId ? { ...message, ...patch } : message
            )
        );
    };

    const ensureSession = async () => {
        if (selectedSessionId) {
            if (selectedSession) {
                writeStoredSession(form, selectedSession);
            }
            return {
                session: selectedSession,
                sessionId: selectedSessionId
            };
        }

        const openResponse = await openSessionMutation.mutateAsync(toOpenSessionRequest(form));
        const openedSessionId = toSessionId(openResponse.sessionId);
        if (openedSessionId === null) {
            throw new Error("会话未返回会话号");
        }

        setSelectedSession(openResponse);
        writeStoredSession(form, openResponse);
        return {
            session: openResponse,
            sessionId: openedSessionId
        };
    };

    const sendQuestion = async (
        questionText: string,
        sessionId: string,
        session: DiscoveryQaOpenSessionResponse | null,
        existingMessageId?: string
    ) => {
        const assistantMessageId = existingMessageId ?? createTimelineMessageId();
        if (!existingMessageId) {
            setMessages((current) => [
                ...current,
                {
                    content: questionText,
                    id: createTimelineMessageId(),
                    role: "user",
                    status: "succeeded"
                },
                {
                    content: "",
                    id: assistantMessageId,
                    retryQuestion: questionText,
                    role: "assistant",
                    status: "loading"
                }
            ]);
        } else {
            updateMessage(assistantMessageId, {
                content: "",
                failureReason: null,
                retryQuestion: questionText,
                sources: [],
                status: "loading"
            });
        }

        let streamedAnswer = "";
        let response: DiscoveryQaChatCompletionResponse;
        try {
            response = await chatCompletionMutation.mutateAsync({
                onDelta: (content) => {
                    streamedAnswer += content;
                    updateMessage(assistantMessageId, {
                        content: streamedAnswer,
                        status: "loading"
                    });
                },
                request: {
                    messages: [
                        {
                            content: questionText,
                            role: "user"
                        }
                    ],
                    metadata: {
                        contextContentId:
                            parseNumber(String(session?.contextContentId ?? "")) ??
                            parseNumber(form.contextContentId),
                        contextContentType:
                            parseString(session?.contextContentType ?? "") ??
                            parseString(form.contextContentType),
                        contextMode:
                            parseString(session?.contextMode ?? "") ??
                            parseString(form.contextMode),
                        sessionId
                    },
                    model: FIXED_MODEL,
                    options: {},
                    requestId: parseString(form.requestId),
                    sessionId,
                    stream: true,
                    traceId: parseString(form.traceId)
                }
            });
        } catch (error) {
            updateMessage(assistantMessageId, {
                content: "发送失败，请重试。",
                failureReason: error instanceof Error ? error.message : "发送失败",
                retryQuestion: questionText,
                sources: [],
                status: "failed"
            });
            throw error;
        }

        const answerText = extractCompletionMessage(response) || streamedAnswer;
        const hasAnswer = Boolean(answerText);
        const isSucceeded = hasAnswer && response.answerStatus !== "FAILED";

        updateMessage(assistantMessageId, {
            content: answerText || "暂时没有生成回答，请重试。",
            failureReason: isSucceeded
                ? null
                : (response.failureReason ?? "回答失败，请稍后重试。"),
            retryQuestion: isSucceeded ? undefined : questionText,
            sources: response.sources ?? [],
            status: isSucceeded ? "succeeded" : "failed"
        });
        if (session) {
            writeStoredSession(form, session);
        }
        setForm((current) => ({
            ...current,
            question: ""
        }));
    };

    const submitQuestion = async () => {
        const questionText = parseString(form.question);
        if (!questionText) {
            return;
        }

        try {
            const currentSession = await ensureSession();
            await sendQuestion(questionText, currentSession.sessionId, currentSession.session);
        } catch {
            return;
        }
    };

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        await submitQuestion();
    };

    const handleQuestionKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
        if (event.key !== "Enter" || event.shiftKey || event.nativeEvent.isComposing) {
            return;
        }

        event.preventDefault();
        if (!chatCompletionMutation.isPending) {
            void submitQuestion();
        }
    };

    const handleRetry = async (messageId: string) => {
        const targetMessage = messages.find((message) => message.id === messageId);
        if (!targetMessage?.retryQuestion || !selectedSessionId) {
            return;
        }

        await sendQuestion(
            targetMessage.retryQuestion,
            selectedSessionId,
            selectedSession,
            messageId
        );
    };

    return (
        <main className="portal-shell portal-qa-only">
            <section className="portal-qa-chat-shell" aria-label="Discovery 问答">
                {hasWangqiSingleDocumentContext && contextLabel ? (
                    <p className="portal-qa-context">当前文档：{contextLabel}</p>
                ) : null}

                <Card className="portal-qa-chat-card">
                    {messages.length ? (
                        <div className="portal-qa-timeline">
                            {messages.map((message) => (
                                <article
                                    className={`portal-qa-message portal-qa-message-${message.role}`}
                                    key={message.id}
                                >
                                    <span
                                        aria-label={message.role === "user" ? "用户" : "古籍助手"}
                                        className="portal-qa-avatar"
                                        role="img"
                                    >
                                        <img
                                            alt=""
                                            aria-hidden="true"
                                            src={
                                                message.role === "user"
                                                    ? ancientReaderAvatar
                                                    : ancientScholarAvatar
                                            }
                                        />
                                    </span>
                                    <div className="portal-qa-bubble">
                                        {message.role === "assistant" ? (
                                            <div>
                                                <p>{message.content}</p>
                                                {message.status === "loading" ? (
                                                    <p>正在生成回答…</p>
                                                ) : null}
                                                {message.status === "failed" ? (
                                                    <p>
                                                        {message.failureReason ??
                                                            "回答失败，请稍后重试。"}
                                                    </p>
                                                ) : null}
                                                {message.sources?.length ? (
                                                    <div className="portal-qa-source-list">
                                                        {message.sources.map(
                                                            (source, sourceIndex) => {
                                                                const key =
                                                                    toChatCompletionSourceKey(
                                                                        source,
                                                                        sourceIndex
                                                                    );
                                                                const hasSourcePath =
                                                                    source.sourcePath !== null &&
                                                                    source.sourcePath !== undefined;
                                                                const sourcePath =
                                                                    source.sourcePath ?? "";
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
                                                                                <a
                                                                                    href={
                                                                                        sourcePath
                                                                                    }
                                                                                >
                                                                                    {source.titleSnapshot ??
                                                                                        source.sourceId}
                                                                                </a>
                                                                            )}
                                                                        </p>
                                                                        <p>
                                                                            来源状态：
                                                                            {source.sourceStatus ??
                                                                                "-"}
                                                                        </p>
                                                                        <p>
                                                                            相关度：
                                                                            {source.score ?? "-"}
                                                                        </p>
                                                                    </div>
                                                                );
                                                            }
                                                        )}
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
                                    </div>
                                </article>
                            ))}
                        </div>
                    ) : (
                        <div className="portal-empty">输入问题后，回答会显示在这里。</div>
                    )}

                    <form className="portal-qa-form" onSubmit={handleSubmit}>
                        <Label className="portal-filter-field portal-qa-question">
                            <Textarea
                                aria-label="问题"
                                name="question"
                                placeholder="请输入问题"
                                rows={4}
                                value={form.question}
                                onKeyDown={handleQuestionKeyDown}
                                onChange={(event) => updateQuestion(event.target.value)}
                            />
                        </Label>
                        <div className="portal-qa-actions">
                            <Button disabled={chatCompletionMutation.isPending} type="submit">
                                <SendHorizontal aria-hidden="true" size={16} />
                                {chatCompletionMutation.isPending ? "回答中..." : "发送"}
                            </Button>
                        </div>
                    </form>
                </Card>
            </section>
        </main>
    );
};
