import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type { OperationsTaskRecord } from "./tasks-types";

export interface OperationsTaskPageQuery {
    sourceDomain?: string | null;
    taskType?: string | null;
    taskStatus?: string | null;
}

export interface OperationsTaskDetailCommand {
    snapshotId: number;
}

export const pageTasks = (query: PageQuery<OperationsTaskPageQuery> = {}) => {
    return postJson<Page<OperationsTaskRecord>, PageQuery<OperationsTaskPageQuery>>(
        "/operations/task/page",
        {
            body: query
        }
    );
};

export const getTaskDetail = (command: OperationsTaskDetailCommand) => {
    return postJson<OperationsTaskRecord, OperationsTaskDetailCommand>("/operations/task/detail", {
        body: command
    });
};
