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
import { AI_BUSINESS_CAPABILITY } from "./ai-refinement-task-types";

const AI_REFINEMENT_TASK_PATH = "/ai/refinement/task";

export type AiRefinementTaskCreateCommand = AiRefinementTaskCreatePayload;
export type AiRefinementTaskGetCommand = AiRefinementTaskGetPayload;
export type AiRefinementTaskCancelCommand = AiRefinementTaskCancelPayload;
export type AiRefinementTaskPageQuery = AiRefinementTaskPagePayload;

export interface SortNewestByRequestedAtThenIdCommand {
    left: { id?: string | null; requestedAt?: string | null };
    right: { id?: string | null; requestedAt?: string | null };
}

interface AiRefinementTaskStreamCommand {
    onEvent: (event: AiRefinementStreamEventRecord) => void;
    signal?: AbortSignal;
    taskId: string;
}

const RETRYABLE_STATUSES = new Set(["FAILED", "PARTIAL", "CANCELLED"]);

const TASK_CAPABILITY_ALIASES: Record<string, string> = {
    [AI_BUSINESS_CAPABILITY.CLASSICS_TRANSLATE]: "translate",
    [AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY]: "summary",
    [AI_BUSINESS_CAPABILITY.CLASSICS_TAG_EXTRACT]: "tags",
    [AI_BUSINESS_CAPABILITY.CLASSICS_QA]: "qa",
    [AI_BUSINESS_CAPABILITY.CLASSICS_IMAGE_DESCRIBE]: "image_analysis",
    [AI_BUSINESS_CAPABILITY.CLASSICS_IMAGE_PROMPT_FUSION]: "fusion",
    [AI_BUSINESS_CAPABILITY.CLASSICS_VISUAL_DESCRIBE]: "visual",
    [AI_BUSINESS_CAPABILITY.CLASSICS_IMAGE_GENERATE]: "image_gen"
};

const BUSINESS_CAPABILITY_CODES: Record<string, string> = {
    translate: AI_BUSINESS_CAPABILITY.CLASSICS_TRANSLATE,
    summary: AI_BUSINESS_CAPABILITY.CLASSICS_SUMMARY,
    tags: AI_BUSINESS_CAPABILITY.CLASSICS_TAG_EXTRACT,
    qa: AI_BUSINESS_CAPABILITY.CLASSICS_QA,
    image_analysis: AI_BUSINESS_CAPABILITY.CLASSICS_IMAGE_DESCRIBE,
    fusion: AI_BUSINESS_CAPABILITY.CLASSICS_IMAGE_PROMPT_FUSION,
    visual: AI_BUSINESS_CAPABILITY.CLASSICS_VISUAL_DESCRIBE,
    image_gen: AI_BUSINESS_CAPABILITY.CLASSICS_IMAGE_GENERATE
};

const CAPABILITY_LABELS: Record<string, string> = {
    translate: "译文",
    summary: "摘要",
    tags: "标签",
    qa: "问答",
    image_analysis: "图片理解",
    fusion: "信息融合",
    visual: "视觉描述",
    image_gen: "生图"
};

const GENERIC_ERROR_MESSAGES = new Set(["业务处理失败"]);
const DECIMAL_ID_PATTERN = /^\d+$/;

const FAILURE_STAGE_LABELS: Record<string, string> = {
    REQUEST_VALIDATE: "任务请求参数校验失败",
    WORKER_REQUEST: "AI Worker 调用失败",
    WORKER_STREAM: "AI Worker 流式应答异常",
    WORKER_RESULT: "AI Worker 结果处理失败",
    INTERNAL_EXECUTION: "系统内部处理失败"
};

const ERROR_TYPE_LABELS: Record<string, string> = {
    INTERNAL_FAILURE: "系统内部处理失败",
    MODEL_CONFIG_INVALID: "模型配置错误，请检查模型、服务来源或鉴权配置",
    MODEL_OUTPUT_EMPTY: "模型没有返回有效内容，请稍后重试或切换模型",
    MODEL_OUTPUT_INVALID_JSON: "模型应答格式错误，请检查提示词模板、输出 Schema 或模型能力",
    MODEL_PROVIDER_UNAVAILABLE: "模型服务暂时不可用，请稍后重试或切换模型",
    MODEL_RATE_LIMITED: "模型服务触发限流，请稍后重试",
    MODEL_REQUEST_REJECTED: "模型拒绝了本次请求，请检查输入内容、提示词或模型策略",
    MODEL_SEMANTIC_FAILURE: "模型未按预期完成任务，请检查提示词模板或模型能力",
    MODEL_STREAM_CHUNK_INVALID: "模型流式应答格式错误，请检查提示词模板或模型能力",
    MODEL_TIMEOUT: "模型服务响应超时，请稍后重试或切换模型",
    MODEL_TRANSPORT_ERROR: "模型服务连接失败，请检查服务可用性或网络配置",
    MODEL_TRANSPORT_FAILURE: "模型服务连接失败，请检查服务可用性或网络配置",
    PATH_FORBIDDEN: "Worker 访问路径被拒绝，请检查服务授权配置",
    SERVICE_NOT_ALLOWED: "Worker 服务未被允许，请检查服务授权配置",
    UNSUPPORTED_MODEL_API_SOURCE: "模型服务来源不支持，请切换模型或修正模型配置",
    WORKER_PROTOCOL_FAILURE: "AI Worker 协议应答异常，请检查 Worker 版本或接口契约",
    WORKER_RESULT_INVALID:
        "AI Worker 返回结果格式错误，请检查提示词模板、输出 Schema 或 Worker 实现",
    WORKER_RESULT_UNEXPECTED: "AI Worker 返回了非预期结果，请检查任务配置或 Worker 实现",
    WORKER_TIMEOUT: "AI Worker 处理超时，请稍后重试"
};

const readTaskFailureSummary = (failureStage?: string, errorType?: string) => {
    if (errorType) {
        const errorTypeLabel = ERROR_TYPE_LABELS[errorType];
        if (errorTypeLabel) {
            return errorTypeLabel;
        }
    }
    if (failureStage) {
        return FAILURE_STAGE_LABELS[failureStage] ?? null;
    }
    return null;
};

const readWorkerProtocolFailureSummary = (
    errorType: string | null,
    errorMessage: string | null
) => {
    if (errorType === "WORKER_PROTOCOL_FAILURE" && errorMessage?.includes("OpenAI-compatible")) {
        return ERROR_TYPE_LABELS.UNSUPPORTED_MODEL_API_SOURCE;
    }
    return null;
};

const trimFailurePart = (value?: string | null) => {
    return value?.trim() || null;
};

export const getNormalizedTaskCapability = (capability: string) => {
    return TASK_CAPABILITY_ALIASES[capability] ?? capability;
};

export const getBusinessCapabilityCode = (capability: string) => {
    return BUSINESS_CAPABILITY_CODES[capability] ?? capability;
};

export const getTaskCapabilityLabel = (capability: string) => {
    const normalizedCapability = getNormalizedTaskCapability(capability);
    return CAPABILITY_LABELS[normalizedCapability] ?? capability;
};

export const getTaskStableId = (taskId: string, taskIdText?: string | null) => {
    return taskIdText || taskId;
};

export const sortDecimalIdAsc = (leftId?: string | null, rightId?: string | null) => {
    const left = String(leftId ?? "").trim();
    const right = String(rightId ?? "").trim();
    if (DECIMAL_ID_PATTERN.test(left) && DECIMAL_ID_PATTERN.test(right)) {
        if (left.length !== right.length) {
            return left.length - right.length;
        }
    }
    return left.localeCompare(right);
};

export const sortDecimalIdDesc = (leftId?: string | null, rightId?: string | null) =>
    sortDecimalIdAsc(rightId, leftId);

export const sortNewestByRequestedAtThenId = (command: SortNewestByRequestedAtThenIdCommand) => {
    const { left, right } = command;
    if (left.requestedAt && right.requestedAt && left.requestedAt !== right.requestedAt) {
        return right.requestedAt.localeCompare(left.requestedAt);
    }
    return sortDecimalIdDesc(left.id, right.id);
};

export const getTaskFailureText = (
    failureStage?: string | null,
    errorType?: string | null,
    errorMessage?: string | null
) => {
    const normalizedFailureStage = trimFailurePart(failureStage);
    const normalizedErrorType = trimFailurePart(errorType);
    const normalizedErrorMessage = trimFailurePart(errorMessage);
    const summary =
        readWorkerProtocolFailureSummary(normalizedErrorType, normalizedErrorMessage) ||
        readTaskFailureSummary(
            normalizedFailureStage ?? undefined,
            normalizedErrorType ?? undefined
        );
    const detailParts = [
        normalizedFailureStage ? `阶段：${normalizedFailureStage}` : null,
        normalizedErrorType ? `类型：${normalizedErrorType}` : null,
        normalizedErrorMessage && !GENERIC_ERROR_MESSAGES.has(normalizedErrorMessage)
            ? `详情：${normalizedErrorMessage}`
            : null
    ].filter((value): value is string => Boolean(value));

    if (summary && detailParts.length) {
        return `${summary}（${detailParts.join("；")}）`;
    }
    if (summary) {
        return summary;
    }
    return normalizedErrorMessage;
};

export const getTaskRetryable = (status: string, capability: string) => {
    return (
        RETRYABLE_STATUSES.has(status) &&
        getNormalizedTaskCapability(capability) in CAPABILITY_LABELS
    );
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
