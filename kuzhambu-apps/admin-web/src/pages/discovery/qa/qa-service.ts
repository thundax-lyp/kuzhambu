import { postEventStream, postJson } from "@/api/http";
import type {
    DiscoveryQaChatCompletionRecord,
    DiscoveryQaChatMessage,
    DiscoveryQaChatMetadata,
    DiscoveryQaExportSessionRecord,
    DiscoveryQaSessionPageRecord,
    DiscoveryQaSessionRecord
} from "./qa-types";

export interface DiscoveryQaOpenSessionCommand {
    contextContentId?: number | null;
    contextContentType?: string | null;
    contextMode?: string | null;
    ownerUserId?: number | null;
    requestId?: string | null;
    scope?: string | null;
    title?: string | null;
    traceId?: string | null;
}

export interface DiscoveryQaSessionPageQuery {
    ownerUserId?: number | null;
    pageNo?: number | null;
    pageSize?: number | null;
    scope?: string | null;
}

export interface DiscoveryQaGetSessionQuery {
    ownerUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaDeleteSessionCommand {
    ownerUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaExportSessionCommand {
    format?: string | null;
    ownerUserId?: number | null;
    sessionId: string;
}

export interface DiscoveryQaChatCompletionCommand {
    messages: DiscoveryQaChatMessage[];
    metadata?: DiscoveryQaChatMetadata | null;
    model: string;
    options?: Record<string, unknown> | null;
    requestId?: string | null;
    sessionId: string;
    stream: boolean;
    traceId?: string | null;
}

export interface DiscoveryQaChatCompletionStreamEvent {
    content?: string | null;
    message?: string | null;
    response?: DiscoveryQaChatCompletionRecord | null;
    sessionId?: string | null;
}

export interface DiscoveryQaChatCompletionStreamCommand {
    command: DiscoveryQaChatCompletionCommand;
    onCompleted?: (response: DiscoveryQaChatCompletionRecord) => void;
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

export const createQaSession = (command: DiscoveryQaOpenSessionCommand) => {
    return postJson<DiscoveryQaSessionRecord, DiscoveryQaOpenSessionCommand>(
        "/discovery/qa/session/open",
        {
            body: command
        }
    );
};

export const pageQaSessions = (query: DiscoveryQaSessionPageQuery) => {
    return postJson<DiscoveryQaSessionPageRecord, DiscoveryQaSessionPageQuery>(
        "/discovery/qa/session/page",
        {
            body: query
        }
    );
};

export const getQaSession = (query: DiscoveryQaGetSessionQuery) => {
    return postJson<DiscoveryQaSessionRecord, DiscoveryQaGetSessionQuery>(
        "/discovery/qa/session/get",
        {
            body: query
        }
    );
};

export const deleteQaSession = (command: DiscoveryQaDeleteSessionCommand) => {
    return postJson<void, DiscoveryQaDeleteSessionCommand>("/discovery/qa/session/delete", {
        body: command
    });
};

export const createQaSessionExport = (command: DiscoveryQaExportSessionCommand) => {
    return postJson<DiscoveryQaExportSessionRecord, DiscoveryQaExportSessionCommand>(
        "/discovery/qa/session/export",
        {
            body: command
        }
    );
};

export const createQaChatCompletion = (command: DiscoveryQaChatCompletionCommand) => {
    return postJson<DiscoveryQaChatCompletionRecord, DiscoveryQaChatCompletionCommand>(
        "/discovery/qa/chat/completions",
        {
            body: command
        }
    );
};

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

export const createQaChatCompletionStream = async ({
    command,
    onCompleted,
    onDelta,
    onError,
    onStarted,
    signal
}: DiscoveryQaChatCompletionStreamCommand): Promise<DiscoveryQaChatCompletionRecord> => {
    let completedResponse: DiscoveryQaChatCompletionRecord | null = null;
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
                (message.data as unknown as DiscoveryQaChatCompletionRecord);
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

    await postEventStream<DiscoveryQaChatCompletionCommand>(
        "/discovery/qa/chat/completions/stream",
        {
            body: {
                ...command,
                stream: true
            },
            onChunk: (chunk) => {
                pending += chunk;
                const blocks = pending.split(/\r?\n\r?\n/);
                pending = blocks.pop() ?? "";
                blocks.forEach(handleBlock);
            },
            signal
        }
    );

    if (pending.trim()) {
        handleBlock(pending);
    }
    const finalResponse = completedResponse as DiscoveryQaChatCompletionRecord | null;
    if (!finalResponse) {
        throw new Error("流式响应未返回完成事件");
    }

    return finalResponse;
};
