import { getEventStream, postJson } from "@/api/http";
import type {
    AiRefinementStreamEventRecord,
    AiRefinementTaskAcceptedRecord,
    AiRefinementTaskCancelPayload,
    AiRefinementTaskCreatePayload,
    AiRefinementTaskGetPayload,
    AiRefinementTaskPagePayload,
    AiRefinementTaskPageRecord,
    AiRefinementTaskRecord
} from "./ai-refinement-task-types";

const AI_REFINEMENT_TASK_PATH = "/ai/refinement/task";

export type AiRefinementTaskCreateCommand = AiRefinementTaskCreatePayload;
export type AiRefinementTaskGetCommand = AiRefinementTaskGetPayload;
export type AiRefinementTaskCancelCommand = AiRefinementTaskCancelPayload;
export type AiRefinementTaskPageQuery = AiRefinementTaskPagePayload;

interface AiRefinementTaskStreamCommand {
    onEvent: (event: AiRefinementStreamEventRecord) => void;
    signal?: AbortSignal;
    taskId: number;
}

const RETRYABLE_STATUSES = new Set(["FAILED", "PARTIAL", "CANCELLED"]);

const CAPABILITY_LABELS: Record<string, string> = {
    translate: "译文",
    summary: "摘要",
    image_analysis: "图片理解",
    fusion: "信息融合",
    visual: "视觉描述",
    image_gen: "生图"
};

export const getTaskCapabilityLabel = (capability: string) => {
    return CAPABILITY_LABELS[capability] ?? capability;
};

export const getTaskFailureText = (
    failureStage?: string | null,
    errorType?: string | null,
    errorMessage?: string | null
) => {
    const parts = [failureStage, errorType, errorMessage]
        .map((value) => value?.trim())
        .filter((value): value is string => Boolean(value));
    return parts.length ? parts.join(" / ") : null;
};

export const getTaskRetryable = (status: string, capability: string) => {
    return RETRYABLE_STATUSES.has(status) && capability in CAPABILITY_LABELS;
};

export const createTask = (command: AiRefinementTaskCreateCommand) => {
    return postJson<AiRefinementTaskAcceptedRecord, AiRefinementTaskCreateCommand>(
        `${AI_REFINEMENT_TASK_PATH}/add`,
        {
            body: command
        }
    );
};

export const getTask = (command: AiRefinementTaskGetCommand) => {
    return postJson<AiRefinementTaskRecord, AiRefinementTaskGetCommand>(
        `${AI_REFINEMENT_TASK_PATH}/get`,
        {
            body: command
        }
    );
};

export const pageTasks = (query: AiRefinementTaskPageQuery = {}) => {
    return postJson<AiRefinementTaskPageRecord, AiRefinementTaskPageQuery>(
        `${AI_REFINEMENT_TASK_PATH}/page`,
        {
            body: query
        }
    );
};

export const cancelTask = (command: AiRefinementTaskCancelCommand) => {
    return postJson<AiRefinementTaskRecord, AiRefinementTaskCancelCommand>(
        `${AI_REFINEMENT_TASK_PATH}/cancel`,
        {
            body: command
        }
    );
};

export const requestTaskStream = async ({
    onEvent,
    signal,
    taskId
}: AiRefinementTaskStreamCommand) => {
    let buffer = "";
    await getEventStream(
        `${AI_REFINEMENT_TASK_PATH}/stream?taskId=${encodeURIComponent(String(taskId))}`,
        {
            signal,
            onChunk: (chunk) => {
                buffer += chunk;
                buffer = flushSseBuffer(buffer, onEvent);
            }
        }
    );
    flushSseBuffer(`${buffer}\n\n`, onEvent);
};

const flushSseBuffer = (
    buffer: string,
    onEvent: (event: AiRefinementStreamEventRecord) => void
) => {
    const normalizedBuffer = buffer.replace(/\r\n/g, "\n");
    const blocks = normalizedBuffer.split("\n\n");
    const rest = blocks.pop() ?? "";
    blocks.forEach((block) => dispatchSseBlock(block, onEvent));
    return rest;
};

const dispatchSseBlock = (
    block: string,
    onEvent: (event: AiRefinementStreamEventRecord) => void
) => {
    const lines = block.split("\n");
    let eventType = "message";
    let eventId: string | null = null;
    const dataLines: string[] = [];

    lines.forEach((line) => {
        if (line.startsWith("event:")) {
            eventType = line.slice("event:".length).trim();
            return;
        }
        if (line.startsWith("id:")) {
            eventId = line.slice("id:".length).trim();
            return;
        }
        if (line.startsWith("data:")) {
            dataLines.push(line.slice("data:".length).trimStart());
        }
    });

    if (!dataLines.length) {
        return;
    }

    const rawData = dataLines.join("\n");
    const payload = JSON.parse(rawData) as Partial<AiRefinementStreamEventRecord>;
    onEvent({
        ...payload,
        eventId: payload.eventId ?? eventId,
        eventType: payload.eventType ?? eventType
    });
};
