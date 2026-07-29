import { useMutation, useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
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

const DEFAULT_OWNER_USER_ID = "1001";
const DEFAULT_PAGE_SIZE = 20;
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
    const sessionId = toSessionId(session?.sessionId) ?? "session";
    return (session?.messages ?? []).map((message, index) => ({
        content: message.content ?? message.failureReason ?? "",
        id: message.messageId ?? `${sessionId}-history-${index}`,
        role: toTimelineRole(message.role),
        status: toTimelineStatus(message)
    }));
};

export const QaPage = () => {
    const [form, setForm] = useState<QaFormState>(INITIAL_FORM_STATE);
    const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);
    const [sessionDetailDrawerOpen, setSessionDetailDrawerOpen] = useState(false);
    const [timelineBySession, setTimelineBySession] = useState<QaTimeline>({});
    const [operationMessage, setOperationMessage] = useState<string | null>(null);
    const ownerUserId = DEFAULT_OWNER_USER_ID;

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

    const createNewSession = () => {
        setOperationMessage(null);
        setForm(INITIAL_FORM_STATE);
        setSelectedSessionId(null);
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
                content: "",
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
            <QaSessionTable
                deleting={deleteSessionMutation.isPending}
                exportDisabled={selectedSessionId === null}
                exporting={exportSessionMutation.isPending}
                loading={sessionsQuery.isPending}
                onCreate={createNewSession}
                onDelete={(sessionId) => void deleteSession(sessionId)}
                onExport={exportCurrentSession}
                onSelect={setSelectedSessionId}
                opening={openSessionMutation.isPending}
                selectedSessionId={selectedSessionId}
                sessions={sessions}
            />
            <QaMessagePanel
                inputValue={form.question}
                loading={openSessionMutation.isPending || chatCompletionMutation.isPending}
                messages={messages}
                onDetailOpen={() => setSessionDetailDrawerOpen(true)}
                onInputChange={(value) => updateField("question", value)}
                onSubmit={(message) => void submitQuestion(message)}
                operationMessage={operationMessage}
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
