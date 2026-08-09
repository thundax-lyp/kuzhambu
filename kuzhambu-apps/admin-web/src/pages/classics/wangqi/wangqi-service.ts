import { ADMIN_API_BASE_URL, postFormData, postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    WangqiContentVersionRecord,
    WangqiDocumentRecord,
    WangqiPublicationActionRecord,
    WangqiPublicationBatchRecord,
    WangqiSourceFileContentMode,
    WangqiSourceFileRecord
} from "./wangqi-types";

const DOCUMENTS_PATH = "/classics/wangqi/documents";
const PUBLICATION_DOCUMENTS_PATH = "/classics/publication/wangqi/documents";

export type WangqiDocumentQuery = PageQuery<{
    keyword?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}>;

export interface WangqiDocumentCommand {
    id?: string | null;
    title?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    documentTime?: string | null;
    storageObjectId?: string | null;
}

interface WangqiVersionCommand {
    id: string;
    versionId?: string | null;
}

export interface WangqiSourceFileContentUrlCommand {
    documentId: string;
    mode?: WangqiSourceFileContentMode;
}

export interface WangqiPublicationActionCommand {
    id: string;
}

export interface WangqiPublicationBatchCommand {
    ids: string[];
}

export const page = (request: WangqiDocumentQuery = {}) => {
    return postJson<Page<WangqiDocumentRecord>, WangqiDocumentQuery>(`${DOCUMENTS_PATH}/page`, {
        body: request
    });
};

export const get = (id: string) => {
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

export const deleteById = (id: string) => {
    return postJson<void, WangqiDocumentCommand>(`${DOCUMENTS_PATH}/delete`, {
        body: { id }
    });
};

export const publish = (command: WangqiPublicationActionCommand) => {
    return postJson<WangqiPublicationActionRecord, WangqiPublicationActionCommand>(
        `${PUBLICATION_DOCUMENTS_PATH}/publish`,
        { body: command }
    );
};

export const submitOffline = (command: WangqiPublicationActionCommand) => {
    return postJson<WangqiPublicationActionRecord, WangqiPublicationActionCommand>(
        `${PUBLICATION_DOCUMENTS_PATH}/offline`,
        { body: command }
    );
};

export const publishBatch = (command: WangqiPublicationBatchCommand) => {
    return postJson<WangqiPublicationBatchRecord, WangqiPublicationBatchCommand>(
        `${PUBLICATION_DOCUMENTS_PATH}/batch/publish`,
        { body: command }
    );
};

export const submitOfflineBatch = (command: WangqiPublicationBatchCommand) => {
    return postJson<WangqiPublicationBatchRecord, WangqiPublicationBatchCommand>(
        `${PUBLICATION_DOCUMENTS_PATH}/batch/offline`,
        { body: command }
    );
};

export const uploadSourceFile = (documentId: string, file: File) => {
    const body = new FormData();
    body.append("file", file);
    return postFormData<WangqiSourceFileRecord>(
        `${DOCUMENTS_PATH}/${documentId}/source-file/upload`,
        body
    );
};

export const getSourceFile = (documentId: string) => {
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

export const listVersions = (documentId: string) => {
    return postJson<WangqiContentVersionRecord[], WangqiVersionCommand>(
        `${DOCUMENTS_PATH}/versions/list`,
        {
            body: { id: documentId }
        }
    );
};

export const getVersion = (documentId: string, versionId: string) => {
    return postJson<WangqiContentVersionRecord, WangqiVersionCommand>(
        `${DOCUMENTS_PATH}/versions/get`,
        {
            body: { id: documentId, versionId }
        }
    );
};

export const resetVersion = (documentId: string, versionId: string) => {
    return postJson<WangqiContentVersionRecord, WangqiVersionCommand>(
        `${DOCUMENTS_PATH}/versions/reset`,
        {
            body: { id: documentId, versionId }
        }
    );
};
