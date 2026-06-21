import { ADMIN_API_BASE_URL, postJson } from "@/api/http";
import type {
    ClassicsExportJobRecord,
    ClassicsExportStatus
} from "@/pages/classics/export/export-types";
import type { Page, PageQuery } from "@/types/page";

export interface ClassicsExportCreateCommand {
    contentType?: string | null;
    exportKind?: string | null;
    exportFormat?: string | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    expiresAt?: string | null;
    contentChanged?: boolean | null;
}

export type ClassicsExportContentMode = "preview" | "download";

export interface ClassicsExportContentUrlCommand {
    jobId: number;
    mode?: ClassicsExportContentMode;
}

export type ClassicsExportQuery = PageQuery<{
    contentType?: string | null;
    exportKind?: string | null;
    status?: ClassicsExportStatus | null;
}> | PageQuery;

const EXPORT_PATH = "/classics/content/exports";

export const create = (request: ClassicsExportCreateCommand) => {
    return postJson<ClassicsExportJobRecord, ClassicsExportCreateCommand>(`${EXPORT_PATH}/create`, {
        body: request
    });
};

export const page = (request: ClassicsExportQuery = {}) => {
    return postJson<Page<ClassicsExportJobRecord>, ClassicsExportQuery>(`${EXPORT_PATH}/page`, {
        body: request
    });
};

export const getContentUrl = (request: ClassicsExportContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    return `${ADMIN_API_BASE_URL}${EXPORT_PATH}/${request.jobId}/content${search}`;
};
