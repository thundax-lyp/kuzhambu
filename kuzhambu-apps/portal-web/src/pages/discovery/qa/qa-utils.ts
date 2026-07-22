import type {
    DiscoveryQaChatCompletionResponse,
    DiscoveryQaChatCompletionSource,
    DiscoveryQaOpenSessionRequest,
    DiscoveryQaOpenSessionResponse,
    QaChatCompletionChoice
} from "./qa-types";

export interface QaFormState {
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

export interface QaTimelineMessage {
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

export interface SelectedQaSessionState {
    session: DiscoveryQaOpenSessionResponse | null;
    source: "opened" | "stored" | null;
}

export const FIXED_MODEL = "kuzhambu-qa";
export const SINGLE_DOCUMENT_MODE = "SINGLE_DOCUMENT";
export const WANGQI_DOCUMENT_TYPE = "WANGQI_DOCUMENT";

const SESSION_STORAGE_KEY = "kuzhambu.portal.discovery.qa.session";
const SESSION_TTL_MS = 30 * 60 * 1000;

export const INITIAL_FORM_STATE: QaFormState = {
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

export const toInitialFormState = (searchParams: URLSearchParams): QaFormState => {
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

export const parseNumber = (value: string) => {
    if (!value.trim()) {
        return null;
    }

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

export const parseString = (value: string) => {
    const trimmed = value.trim();
    return trimmed.length ? trimmed : null;
};

export const toOpenSessionRequest = (form: QaFormState): DiscoveryQaOpenSessionRequest => {
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

export const toSessionId = (value?: string | null): string | null => {
    return typeof value === "string" && value.trim().length ? value : null;
};

export const extractCompletionMessage = (response?: DiscoveryQaChatCompletionResponse | null) => {
    const choices: QaChatCompletionChoice[] = response?.choices ?? [];
    const firstChoice = choices[0];
    const message = firstChoice?.message;
    return message?.content?.trim() ?? "";
};

export const isUnavailableSource = (source: DiscoveryQaChatCompletionSource) => {
    return source.sourceStatus?.toUpperCase() === "UNAVAILABLE" || !source.sourceId;
};

export const createTimelineMessageId = () => {
    const randomSuffix = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
    return `qa-message-${randomSuffix}`;
};

export const toChatCompletionSourceKey = (
    source: DiscoveryQaChatCompletionSource,
    index: number
) => {
    return source.sourceId ?? `${source.contentType}-${source.contentId}-${index}`;
};

export const readStoredSession = (form: QaFormState): DiscoveryQaOpenSessionResponse | null => {
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

export const clearStoredSession = () => {
    try {
        globalThis.localStorage?.removeItem(SESSION_STORAGE_KEY);
    } catch {
        return;
    }
};

export const writeStoredSession = (form: QaFormState, session: DiscoveryQaOpenSessionResponse) => {
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

export const isUnavailableSessionError = (error: unknown) => {
    const message = error instanceof Error ? error.message : String(error);
    return /404|not[\s_-]*found|removed|deleted|invalid[\s_-]*session|NOT_FOUND|会话.*(不存在|已删除|删除|失效|不可用)|不存在|已删除/iu.test(
        message
    );
};

export const formatContextLabel = (
    contextContentType?: string | null,
    contextContentId?: number | string | null
) => {
    if (!contextContentType) {
        return null;
    }

    return contextContentId ? `${contextContentType} #${contextContentId}` : contextContentType;
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
