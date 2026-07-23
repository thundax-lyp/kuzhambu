import type { KuzhambuSyncTaskAdapter } from "@/components/kuzhambu-sync-task-modal";
import type { AiRefinementTaskRecord } from "@/pages/classics/common/ai-refinement-task-types";
import { readSancaiAiTextFieldConfig } from "./sancai-entry-ai-text-config";
import type { SancaiAiTextField } from "./sancai-entry-ai-text-config";

export const readRefinementTaskStatusLabel = (
    status?: string | null,
    capability?: string | null
) => {
    switch (status) {
        case "PENDING":
            return "等待中";
        case "RUNNING":
            if (capability === "translate") {
                return "翻译中";
            }
            if (capability === "summary") {
                return "摘要中";
            }
            return "处理中";
        case "SUCCEEDED":
            return "已完成";
        case "PARTIAL":
            return "部分完成";
        case "FAILED":
            return "失败";
        case "CANCELLED":
            return "已取消";
        default:
            return status || "-";
    }
};

export const readRefinementTaskAlertType = (status?: string | null) => {
    if (status === "SUCCEEDED" || status === "PARTIAL") {
        return "success";
    }
    if (status === "FAILED" || status === "CANCELLED") {
        return "warning";
    }
    return "info";
};

export const resolveAiTextField = (
    capability?: string | null,
    fallback: SancaiAiTextField = "translate"
) => {
    return capability === "summary" || capability === "translate" ? capability : fallback;
};

export const aiTextTaskAdapter: KuzhambuSyncTaskAdapter<AiRefinementTaskRecord> = {
    getId: (task) => task.taskId,
    getMessage: (task) => task.errorMessage || undefined,
    getPhase: (task) => {
        if (task.status === "PENDING" || task.status === "RUNNING") {
            return "tracking";
        }
        if (task.status === "SUCCEEDED" || task.status === "PARTIAL") {
            return task.candidateId ? "result_ready" : "waiting_result";
        }
        if (task.status === "CANCELLED") {
            return "cancelled";
        }
        if (task.status === "FAILED") {
            return "failed";
        }
        return "tracking";
    },
    getResultKey: (task) => task.candidateId,
    getStatusLabel: (task) =>
        `${readSancaiAiTextFieldConfig(resolveAiTextField(task.capability)).taskLabel}任务：${readRefinementTaskStatusLabel(
            task.status,
            task.capability
        )}`
};
