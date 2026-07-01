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
