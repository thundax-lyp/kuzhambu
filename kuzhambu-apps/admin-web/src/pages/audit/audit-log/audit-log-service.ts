import { postJson } from "@/api/http";
import type { OptionsRecord } from "@/types/options";
import type { Page } from "@/types/page";
import type { AuditLogDetailRecord, AuditLogRecord } from "./audit-log-types";

export interface AuditLogPageQuery {
    pageNo?: number;
    pageSize?: number;
    objectType?: string | null;
    objectId?: string | null;
    action?: string | null;
    operatorType?: string | null;
    operatorId?: string | null;
    source?: string | null;
    requestId?: string | null;
    beginDate?: string | null;
    endDate?: string | null;
}

export type AuditOptionKeys = "objectTypes" | "actions" | "operatorTypes";

export const pageAuditLogs = (request: AuditLogPageQuery = {}) => {
    return postJson<Page<AuditLogRecord>, AuditLogPageQuery>("/audit/log/page", {
        body: request
    });
};

export const getAuditLogDetail = (id: string) => {
    return postJson<AuditLogDetailRecord, { id: string }>("/audit/log/detail", {
        body: { id }
    });
};

export const getAuditOptions = () => {
    return postJson<Partial<OptionsRecord<AuditOptionKeys>>, Record<string, never>>(
        "/audit/log/options",
        {
            body: {}
        }
    );
};
