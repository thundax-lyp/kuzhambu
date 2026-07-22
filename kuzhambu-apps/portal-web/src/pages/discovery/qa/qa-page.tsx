import { useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { QaComposer } from "./components/qa-composer";
import { QaTimeline } from "./components/qa-timeline";
import * as qaService from "./qa-service";
import type { DiscoveryQaOpenSessionResponse } from "./qa-types";
import {
    FIXED_MODEL,
    SINGLE_DOCUMENT_MODE,
    WANGQI_DOCUMENT_TYPE,
    clearStoredSession,
    createTimelineMessageId,
    extractCompletionMessage,
    formatContextLabel,
    isUnavailableSessionError,
    parseNumber,
    parseString,
    readStoredSession,
    toInitialFormState,
    toOpenSessionRequest,
    toSessionId,
    writeStoredSession,
    type QaFormState,
    type QaTimelineMessage,
    type SelectedQaSessionState
} from "./qa-utils";

export const DiscoveryQaPage = () => {
    const [searchParams] = useSearchParams();
    const initialForm = toInitialFormState(searchParams);
    const [form, setForm] = useState<QaFormState>(() => initialForm);
    const [selectedSessionState, setSelectedSessionState] = useState<SelectedQaSessionState>(() => {
        const session = readStoredSession(initialForm);
        return {
            session,
            source: session ? "stored" : null
        };
    });
    const [messages, setMessages] = useState<QaTimelineMessage[]>([]);
    const isSubmittingRef = useRef(false);
    const [isSubmittingLocked, setIsSubmittingLocked] = useState(false);

    const openSessionMutation = useMutation({ mutationFn: qaService.openQaSession });
    const chatCompletionMutation = useMutation({
        mutationFn: qaService.createQaChatCompletionStream
    });
    const isSubmitting =
        isSubmittingLocked || openSessionMutation.isPending || chatCompletionMutation.isPending;

    const selectedSession = selectedSessionState.session;
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

    const startSubmitting = () => {
        if (isSubmittingRef.current) {
            return false;
        }

        isSubmittingRef.current = true;
        setIsSubmittingLocked(true);
        return true;
    };

    const finishSubmitting = () => {
        isSubmittingRef.current = false;
        setIsSubmittingLocked(false);
    };

    const ensureSession = async () => {
        if (selectedSessionId) {
            let reusableSession = selectedSession;
            let reusableSessionSource = selectedSessionState.source;
            if (selectedSessionState.source === "stored") {
                try {
                    reusableSession =
                        (await qaService.getQaSession({
                            ownerUserId: parseNumber(form.ownerUserId),
                            sessionId: selectedSessionId
                        })) ?? selectedSession;
                    reusableSessionSource = "opened";
                    setSelectedSessionState({
                        session: reusableSession,
                        source: "opened"
                    });
                } catch (error) {
                    if (isUnavailableSessionError(error)) {
                        clearStoredSession();
                        setSelectedSessionState({
                            session: null,
                            source: null
                        });
                        return openSession();
                    }

                    reusableSessionSource = "opened";
                }
            }
            if (reusableSession) {
                writeStoredSession(form, reusableSession);
            }
            const reusableSessionId = toSessionId(reusableSession?.sessionId) ?? selectedSessionId;
            return {
                session: reusableSession,
                sessionId: reusableSessionId,
                sessionSource: reusableSessionSource
            };
        }

        return openSession();
    };

    const openSession = async () => {
        const openResponse = await openSessionMutation.mutateAsync(toOpenSessionRequest(form));
        const openedSessionId = toSessionId(openResponse.sessionId);
        if (openedSessionId === null) {
            throw new Error("会话未返回会话号");
        }

        setSelectedSessionState({
            session: openResponse,
            source: "opened"
        });
        writeStoredSession(form, openResponse);
        return {
            session: openResponse,
            sessionId: openedSessionId,
            sessionSource: "opened" as const
        };
    };

    const sendQuestion = async (
        questionText: string,
        sessionId: string,
        session: DiscoveryQaOpenSessionResponse | null,
        existingMessageId?: string,
        sessionSource?: SelectedQaSessionState["source"]
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
            const shouldResetSession = isUnavailableSessionError(error);
            if (shouldResetSession) {
                clearStoredSession();
                setSelectedSessionState({
                    session: null,
                    source: null
                });
            }
            updateMessage(assistantMessageId, {
                content: shouldResetSession
                    ? "当前会话已失效，请重新发送问题。"
                    : "发送失败，请重试。",
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
            if (sessionSource === "stored") {
                setSelectedSessionState({
                    session,
                    source: "opened"
                });
            }
        }
        setForm((current) => ({
            ...current,
            question: ""
        }));
    };

    const submitQuestion = async () => {
        const questionText = parseString(form.question);
        if (!questionText || !startSubmitting()) {
            return;
        }

        try {
            const currentSession = await ensureSession();
            await sendQuestion(
                questionText,
                currentSession.sessionId,
                currentSession.session,
                undefined,
                currentSession.sessionSource
            );
        } catch {
            return;
        } finally {
            finishSubmitting();
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
        if (!isSubmitting) {
            void submitQuestion();
        }
    };

    const handleRetry = async (messageId: string) => {
        const targetMessage = messages.find((message) => message.id === messageId);
        if (!targetMessage?.retryQuestion || !startSubmitting()) {
            return;
        }

        try {
            const currentSession = selectedSessionId
                ? {
                      session: selectedSession,
                      sessionId: selectedSessionId,
                      sessionSource: selectedSessionState.source
                  }
                : await ensureSession();
            await sendQuestion(
                targetMessage.retryQuestion,
                currentSession.sessionId,
                currentSession.session,
                messageId,
                currentSession.sessionSource
            );
        } catch {
            return;
        } finally {
            finishSubmitting();
        }
    };

    return (
        <main className="portal-shell portal-qa-only">
            <section className="portal-qa-chat-shell" aria-label="Discovery 问答">
                {hasWangqiSingleDocumentContext && contextLabel ? (
                    <p className="portal-qa-context">当前文档：{contextLabel}</p>
                ) : null}

                <Card className="portal-qa-chat-card">
                    <QaTimeline messages={messages} onRetry={handleRetry} />
                    <QaComposer
                        disabled={isSubmitting}
                        question={form.question}
                        onQuestionChange={updateQuestion}
                        onQuestionKeyDown={handleQuestionKeyDown}
                        onSubmit={handleSubmit}
                    />
                </Card>
            </section>
        </main>
    );
};
