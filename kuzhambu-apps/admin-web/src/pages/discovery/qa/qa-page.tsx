import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useRef, useState } from "react";
import * as currentUserService from "@/service/current-user-service";
import { QaMessagePanel, type QaTimelineMessage } from "./qa-message-panel";
import { QaSessionDetailDrawer } from "./qa-session-detail-drawer";
import { QaSessionTable } from "./qa-session-table";
import * as service from "./qa-service";
import type {
    DiscoveryQaChatCompletionRecord,
    DiscoveryQaSessionRecord,
    DiscoveryQaSessionMessageRecord
} from "./qa-types";
import "./qa-page.css";

const FIXED_MODEL = "kuzhambu-qa";
const FULL_LIBRARY_CONTEXT_MODE = "GENERAL";
const FULL_LIBRARY_SESSION_TITLE = "新对话";
const SESSION_TITLE_MAX_LENGTH = 24;
interface QaFormState {
    question: string;
}

type QaTimeline = Record<string, QaTimelineMessage[]>;

const INITIAL_FORM_STATE: QaFormState = {
    question: ""
};

const toSessionId = (value?: string | null) => {
    return typeof value === "string" && value.trim().length ? value : null;
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

const toOpenSessionRequest = (ownerUserId: string | null, title = FULL_LIBRARY_SESSION_TITLE) => {
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

const toTimelineRole = (role?: string | null): QaTimelineMessage["role"] => {
    return role?.toUpperCase() === "USER" ? "user" : "assistant";
};

const toTimelineStatus = (
    message: DiscoveryQaSessionMessageRecord
): QaTimelineMessage["status"] => {
    const status = message.messageStatus?.toUpperCase();
    if (status === "FAILED") {
        return "failed";
    }
    if (status === "PROCESSING") {
        return "loading";
    }
    return "succeeded";
};

const toTimelineMessages = (session?: DiscoveryQaSessionRecord | null): QaTimelineMessage[] => {
    const sessionId = toSessionId(session?.id) ?? "session";
    return (session?.messages ?? []).map((message, index) => ({
        content: message.content ?? message.failureReason ?? "",
        id: message.id ?? `${sessionId}-history-${index}`,
        role: toTimelineRole(message.role),
        status: toTimelineStatus(message)
    }));
};

export const QaPage = () => {
    const queryClient = useQueryClient();
    const [form, setForm] = useState<QaFormState>(INITIAL_FORM_STATE);
    const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);
    const [sessionDetailDrawerOpen, setSessionDetailDrawerOpen] = useState(false);
    const [timelineBySession, setTimelineBySession] = useState<QaTimeline>({});
    const [operationMessage, setOperationMessage] = useState<string | null>(null);
    const streamAbortControllerRef = useRef<AbortController | null>(null);

    const currentUserQuery = useQuery({
        queryFn: currentUserService.getCurrentUserInfo,
        queryKey: ["current-user", "info"]
    });
    const ownerUserId = currentUserQuery.data?.id ?? null;
    const selectedSessionQuery = useQuery({
        enabled: ownerUserId !== null && selectedSessionId !== null,
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
    const messages = useMemo(() => {
        if (selectedSessionId === null) {
            return [];
        }
        const currentTimeline = timelineBySession[selectedSessionId] ?? [];
        return currentTimeline.length
            ? currentTimeline
            : toTimelineMessages(selectedSessionQuery.data);
    }, [selectedSessionId, selectedSessionQuery.data, timelineBySession]);
    const selectedSession = selectedSessionQuery.data;
    const updateField = (key: keyof QaFormState, value: string) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };

    const appendMessages = (
        sessionId: string,
        messagesToAppend: QaTimelineMessage[],
        initialMessages: QaTimelineMessage[] = []
    ) => {
        setTimelineBySession((current) => ({
            ...current,
            [sessionId]: [...(current[sessionId] ?? initialMessages), ...messagesToAppend]
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
        const nextSessionId = toSessionId(session.id);
        if (nextSessionId === null) {
            throw new Error("会话未返回会话号");
        }

        setSelectedSessionId(nextSessionId);
        setTimelineBySession((current) => ({
            ...current,
            [nextSessionId]: current[nextSessionId] ?? []
        }));
        void queryClient.invalidateQueries({ queryKey: ["discovery-qa", "session-page"] });
        return nextSessionId;
    };

    const cancelActiveStream = () => {
        streamAbortControllerRef.current?.abort();
        streamAbortControllerRef.current = null;
    };

    useEffect(() => {
        return cancelActiveStream;
    }, []);

    const createNewSession = () => {
        cancelActiveStream();
        setOperationMessage(null);
        setForm(INITIAL_FORM_STATE);
        setSelectedSessionId(null);
    };

    const removeDeletedSession = (sessionId: string) => {
        setTimelineBySession((current) => {
            const next = { ...current };
            delete next[sessionId];
            return next;
        });
        if (selectedSessionId === sessionId) {
            setSelectedSessionId(null);
        }
    };

    const selectSession = (sessionId: string) => {
        cancelActiveStream();
        setSelectedSessionId(sessionId);
    };

    const submitQuestion = async (questionValue = form.question) => {
        const question = questionValue.trim();
        if (!question) {
            return;
        }
        if (ownerUserId === null) {
            setOperationMessage("当前用户信息尚未加载完成");
            return;
        }

        cancelActiveStream();
        const streamAbortController = new AbortController();
        streamAbortControllerRef.current = streamAbortController;
        setOperationMessage(null);
        let nextSessionId: string | null = null;
        let assistantMessageId: string | null = null;
        try {
            nextSessionId = await ensureSessionId(question);
            const activeSessionId = nextSessionId;
            assistantMessageId = createMessageId();
            const activeAssistantMessageId = assistantMessageId;
            const initialMessages =
                selectedSessionId === activeSessionId
                    ? toTimelineMessages(selectedSessionQuery.data)
                    : [];
            appendMessages(
                activeSessionId,
                [
                    {
                        content: question,
                        id: createMessageId(),
                        role: "user",
                        status: "succeeded"
                    },
                    {
                        content: "",
                        id: activeAssistantMessageId,
                        role: "assistant",
                        status: "loading"
                    }
                ],
                initialMessages
            );
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
                },
                signal: streamAbortController.signal
            });
            const answerText = extractAnswerText(response) || streamedAnswer;
            updateMessage(activeSessionId, activeAssistantMessageId, {
                content: answerText || response.failureReason || "未返回回答内容",
                sources: response.sources ?? [],
                status: response.answerStatus === "FAILED" ? "failed" : "succeeded"
            });
        } catch (error) {
            if (streamAbortController.signal.aborted) {
                return;
            }
            const message = error instanceof Error ? error.message : "回答生成失败";
            if (nextSessionId !== null && assistantMessageId !== null) {
                updateMessage(nextSessionId, assistantMessageId, {
                    content: message,
                    status: "failed"
                });
                return;
            }
            setOperationMessage(message);
        } finally {
            if (streamAbortControllerRef.current === streamAbortController) {
                streamAbortControllerRef.current = null;
            }
        }
    };

    return (
        <main className="kuzhambu-page qa-page discovery-qa-page">
            <QaSessionTable
                onCreate={createNewSession}
                onDeleted={removeDeletedSession}
                onOperationMessage={setOperationMessage}
                onSelect={selectSession}
                ownerUserId={ownerUserId}
                opening={openSessionMutation.isPending}
                selectedSessionId={selectedSessionId}
            />
            <QaMessagePanel
                inputValue={form.question}
                loading={openSessionMutation.isPending || chatCompletionMutation.isPending}
                messages={messages}
                onDetailOpen={() => setSessionDetailDrawerOpen(true)}
                onInputChange={(value) => updateField("question", value)}
                onSubmit={(message) => void submitQuestion(message)}
                operationMessage={
                    operationMessage ?? (currentUserQuery.isError ? "当前用户信息加载失败" : null)
                }
                selectedSession={selectedSession}
            />
            <QaSessionDetailDrawer
                onClose={() => setSessionDetailDrawerOpen(false)}
                open={sessionDetailDrawerOpen}
                session={selectedSession}
            />
        </main>
    );
};
