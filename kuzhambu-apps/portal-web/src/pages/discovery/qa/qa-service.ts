import { postEventStream, postJson } from "@/api/http";
import type {
    DiscoveryQaChatCompletionRequest,
    DiscoveryQaChatCompletionResponse,
    DiscoveryQaDeleteSessionRequest,
    DiscoveryQaExportSessionRequest,
    DiscoveryQaExportSessionResponse,
    DiscoveryQaGetSessionRequest,
    DiscoveryQaOpenSessionRequest,
    DiscoveryQaOpenSessionResponse,
    DiscoveryQaSessionPageQuery,
    DiscoveryQaSessionPageResponse
} from "./qa-types";

export const initQaSession = (request: DiscoveryQaOpenSessionRequest) => {
    return postJson<DiscoveryQaOpenSessionResponse, DiscoveryQaOpenSessionRequest>(
        "/portal/discovery/qa/session/init",
        request
    );
};

export const pageQaSessions = (query: DiscoveryQaSessionPageQuery) => {
    return postJson<DiscoveryQaSessionPageResponse, DiscoveryQaSessionPageQuery>(
        "/portal/discovery/qa/session/page",
        query
    );
};

export const getQaSession = (request: DiscoveryQaGetSessionRequest) => {
    return postJson<DiscoveryQaOpenSessionResponse, DiscoveryQaGetSessionRequest>(
        "/portal/discovery/qa/session/get",
        request
    );
};

export const deleteQaSession = (request: DiscoveryQaDeleteSessionRequest) => {
    return postJson<void, DiscoveryQaDeleteSessionRequest>(
        "/portal/discovery/qa/session/delete",
        request
    );
};

export const downloadQaSession = (request: DiscoveryQaExportSessionRequest) => {
    return postJson<DiscoveryQaExportSessionResponse, DiscoveryQaExportSessionRequest>(
        "/portal/discovery/qa/session/download",
        request
    );
};

export const createChatCompletion = (request: DiscoveryQaChatCompletionRequest) => {
    return postJson<DiscoveryQaChatCompletionResponse, DiscoveryQaChatCompletionRequest>(
        "/portal/discovery/qa/chat/create",
        request
    );
};

export interface DiscoveryQaChatCompletionStreamEvent {
    content?: string | null;
    message?: string | null;
    response?: DiscoveryQaChatCompletionResponse | null;
    sessionId?: string | null;
}

export interface DiscoveryQaChatCompletionStreamRequest {
    request: DiscoveryQaChatCompletionRequest;
    onCompleted?: (response: DiscoveryQaChatCompletionResponse) => void;
    onDelta?: (content: string) => void;
    onError?: (message: string) => void;
    onStarted?: (sessionId?: string | null) => void;
    signal?: AbortSignal;
}

class DiscoveryQaStreamError extends Error {
    constructor(message: string) {
        super(message);
        this.name = "DiscoveryQaStreamError";
    }
}

const parseSseBlock = (block: string) => {
    const lines = block.split(/\r?\n/);
    let event = "message";
    const dataLines: string[] = [];

    lines.forEach((line) => {
        if (line.startsWith("event:")) {
            event = line.slice("event:".length).trim();
            return;
        }
        if (line.startsWith("data:")) {
            dataLines.push(line.slice("data:".length).trimStart());
        }
    });

    if (!dataLines.length) {
        return null;
    }

    return {
        data: JSON.parse(dataLines.join("\n")) as DiscoveryQaChatCompletionStreamEvent,
        event
    };
};

export const submitChatCompletion = async ({
    onCompleted,
    onDelta,
    onError,
    onStarted,
    request,
    signal
}: DiscoveryQaChatCompletionStreamRequest): Promise<DiscoveryQaChatCompletionResponse> => {
    let completedResponse: DiscoveryQaChatCompletionResponse | null = null;
    let pending = "";

    const handleBlock = (block: string) => {
        const message = parseSseBlock(block);
        if (!message) {
            return;
        }

        if (message.event === "started") {
            onStarted?.(message.data.sessionId);
            return;
        }
        if (message.event === "delta" && message.data.content) {
            onDelta?.(message.data.content);
            return;
        }
        if (message.event === "completed") {
            const response =
                message.data.response ??
                (message.data as unknown as DiscoveryQaChatCompletionResponse);
            completedResponse = response;
            onCompleted?.(response);
            return;
        }
        if (message.event === "error") {
            const errorMessage = message.data.message ?? "回答生成失败";
            onError?.(errorMessage);
            throw new DiscoveryQaStreamError(errorMessage);
        }
    };

    await postEventStream<DiscoveryQaChatCompletionRequest>("/portal/discovery/qa/chat/submit", {
        body: {
            ...request,
            stream: true
        },
        onChunk: (chunk) => {
            pending += chunk;
            const blocks = pending.split(/\r?\n\r?\n/);
            pending = blocks.pop() ?? "";
            blocks.forEach(handleBlock);
        },
        signal
    });

    if (pending.trim()) {
        handleBlock(pending);
    }
    const finalResponse = completedResponse as DiscoveryQaChatCompletionResponse | null;
    if (!finalResponse) {
        throw new Error("流式响应未返回完成事件");
    }

    return finalResponse;
};
