import { postJson } from "@/api/http";
import type {
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
