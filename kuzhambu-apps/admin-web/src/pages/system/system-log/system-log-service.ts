import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { LogRecord } from "./system-log-types";

export interface LogPageQuery {
    pageNo?: number;
    pageSize?: number;
    title?: string | null;
    userLoginName?: string | null;
    userName?: string | null;
    remoteAddr?: string | null;
    requestUri?: string | null;
    beginDate?: string | null;
    endDate?: string | null;
}

export const pageEvents = (request: LogPageQuery = {}) => {
    return postJson<Page<LogRecord>, LogPageQuery>("/sys/log/page", {
        body: request
    });
};
