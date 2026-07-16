import { ADMIN_API_BASE_URL, postFormData, postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    WangqiContentVersionRecord,
    WangqiDocumentRecord,
    WangqiSourceFileContentMode,
    WangqiSourceFileRecord
} from "./wangqi-types";

const DOCUMENTS_PATH = "/classics/wangqi/documents";

export type WangqiDocumentQuery = PageQuery<{
    keyword?: string | null;
    visibility?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}>;

export interface WangqiDocumentCommand {
    id?: number | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: number | null;
    visibility?: string | null;
}

interface WangqiVersionCommand {
    id: number;
    versionId?: number | null;
}

export interface WangqiSourceFileContentUrlCommand {
    documentId: number;
    mode?: WangqiSourceFileContentMode;
}

export const page = (request: WangqiDocumentQuery = {}) => {
    return postJson<Page<WangqiDocumentRecord>, WangqiDocumentQuery>(`${DOCUMENTS_PATH}/page`, {
        body: request
    });
};

export const get = (id: number) => {
    return postJson<WangqiDocumentRecord, WangqiDocumentCommand>(`${DOCUMENTS_PATH}/get`, {
        body: { id }
    });
};

export const listTimeline = (request: WangqiDocumentQuery = {}) => {
    return postJson<WangqiDocumentRecord[], WangqiDocumentQuery>(
        `${DOCUMENTS_PATH}/timeline/list`,
        {
            body: request
        }
    );
};

export const add = (request: WangqiDocumentCommand) => {
    return postJson<WangqiDocumentRecord, WangqiDocumentCommand>(`${DOCUMENTS_PATH}/add`, {
        body: request
    });
};

export const update = (request: WangqiDocumentCommand) => {
    return postJson<WangqiDocumentRecord, WangqiDocumentCommand>(`${DOCUMENTS_PATH}/update`, {
        body: request
    });
};

export const deleteById = (id: number) => {
    return postJson<void, WangqiDocumentCommand>(`${DOCUMENTS_PATH}/delete`, {
        body: { id }
    });
};

export const uploadSourceFile = (documentId: number, file: File) => {
    const body = new FormData();
    body.append("file", file);
    return postFormData<WangqiSourceFileRecord>(
        `${DOCUMENTS_PATH}/${documentId}/source-file/upload`,
        body
    );
};

export const getSourceFile = (documentId: number) => {
    return postJson<WangqiSourceFileRecord, WangqiDocumentCommand>(
        `${DOCUMENTS_PATH}/source-file/get`,
        {
            body: { id: documentId }
        }
    );
};

export const getSourceFileContentUrl = (request: WangqiSourceFileContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    return `${ADMIN_API_BASE_URL}${DOCUMENTS_PATH}/${request.documentId}/source-file/content${search}`;
};

export const listVersions = (documentId: number) => {
    return postJson<WangqiContentVersionRecord[], WangqiVersionCommand>(
        `${DOCUMENTS_PATH}/versions/list`,
        {
            body: { id: documentId }
        }
    );
};

export const getVersion = (documentId: number, versionId: number) => {
    return postJson<WangqiContentVersionRecord, WangqiVersionCommand>(
        `${DOCUMENTS_PATH}/versions/get`,
        {
            body: { id: documentId, versionId }
        }
    );
};

export const resetVersion = (documentId: number, versionId: number) => {
    return postJson<WangqiContentVersionRecord, WangqiVersionCommand>(
        `${DOCUMENTS_PATH}/versions/reset`,
        {
            body: { id: documentId, versionId }
        }
    );
};
