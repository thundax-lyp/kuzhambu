import { ApiError } from "@/api/http";
import type { GraphExtractionService } from "@/pages/knowledge/graph-extraction/graph-extraction-service";
import {
    graphBatchExtractionResult,
    graphExtractionMockTaskDetails,
    graphExtractionMockTasks,
    toMockPage
} from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";

const findTask = (taskId: string) => graphExtractionMockTasks.find((task) => task.id === taskId);

export const mockGraphExtractionService: GraphExtractionService = {
    pageTasks: async (query = {}) => {
        const records = query.batchId
            ? graphExtractionMockTasks.filter((task) => task.batchId === query.batchId)
            : graphExtractionMockTasks;
        return toMockPage(records, query.pageNo, query.pageSize);
    },
    getTask: async (command) => {
        const detail = graphExtractionMockTaskDetails.find(
            (record) => record.task.id === command.taskId
        );
        if (!detail) {
            throw new ApiError("GRAPH_TASK_NOT_FOUND", "任务不存在");
        }
        return detail;
    },
    retryTask: async (command) => {
        const task = findTask(command.taskId ?? "");
        if (!task || task.lockVersion !== command.taskLockVersion) {
            return {
                conflict: {
                    code: "GRAPH_TASK_LOCK_CONFLICT",
                    message: "任务版本已变化，请刷新后重试。"
                }
            };
        }
        return {
            task: {
                ...task,
                executionStatus: "PENDING",
                progress: 0
            }
        };
    },
    cancelTask: async (command) => {
        const task = findTask(command.taskId ?? "");
        if (!task) {
            return {
                conflict: {
                    code: "GRAPH_TASK_STATE_CONFLICT",
                    message: "任务状态已变化，请刷新后重试。"
                }
            };
        }
        return {
            task: {
                ...task,
                executionStatus: "CANCELLED",
                progress: task.progress
            }
        };
    },
    applyCandidate: async (command) => {
        const task = findTask(command.taskId ?? "");
        if (!task || task.disposition !== "PENDING") {
            return {
                conflict: {
                    code: "GRAPH_TASK_STATE_CONFLICT",
                    message: "候选已不可处置。"
                }
            };
        }
        return {
            task: {
                ...task,
                disposition: command.applyMode === "MERGE" ? "ADOPTED_MERGE" : "ADOPTED_REPLACE",
                disposedAt: "1723852860000"
            }
        };
    },
    discardCandidate: async (command) => {
        const task = findTask(command.taskId ?? "");
        if (!task) {
            return {
                conflict: {
                    code: "GRAPH_CANDIDATE_UNAVAILABLE",
                    message: "候选已清理或不可见。"
                }
            };
        }
        return {
            task: {
                ...task,
                disposition: "DISCARDED",
                disposedAt: "1723852860000"
            }
        };
    },
    regenerateTask: async (command) => {
        const task = findTask(command.taskId ?? "");
        if (!task) {
            return {
                conflict: {
                    code: "GRAPH_TASK_STATE_CONFLICT",
                    message: "任务状态已变化，请刷新后重试。"
                }
            };
        }
        return {
            task: {
                ...task,
                id: "7006",
                progress: 0,
                regeneratedFromTaskId: task.id
            }
        };
    },
    createBatchExtraction: async () => graphBatchExtractionResult
};
