import { ADMIN_API_BASE_URL, getJson, postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    SancaiContentVersionRecord,
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiShowcaseRecord
} from "../sancai-types";

const ENTRIES_PATH = "/classics/sancai/entries";
const ASSET_IMAGES_PATH = "/classics/sancai/assets/images";
const ASSET_SHOWCASES_PATH = "/classics/sancai/assets/showcases";

export interface SancaiEntryQuery {
    categoryId?: number | null;
    volumeId?: number | null;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    visibility?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface SancaiEntryCommand {
    id?: number | null;
    volumeId?: number | null;
    title?: string | null;
    originalText?: string | null;
    translationText?: string | null;
    summary?: string | null;
    lifecycleStatus?: string | null;
    visibility?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
}

export interface SancaiEntrySortCommand {
    orderedIds: number[];
    sortDirection?: "ASC" | "DESC" | null;
}

interface SancaiVersionCommand {
    id: number;
    versionId?: number | null;
}

export interface SancaiEntryImageUploadCommand {
    currentUsed?: boolean;
    entryId: number;
    file: File;
    imageType?: string | null;
    replaceImageId?: number | null;
    title?: string | null;
}

export interface SancaiEntryImageContentUrlCommand {
    entryId: number;
    imageId: number;
    mode?: SancaiEntryImageContentMode;
}

export type SancaiShowcaseStatus =
    | "REQUESTED"
    | "PROCESSING"
    | "COMPLETED"
    | "FAILED"
    | "EXPIRED";

export interface SancaiShowcaseCreateCommand {
    status?: string | null;
    scopeJson?: string | null;
    storageObjectId?: number | null;
    entryCount?: number | null;
    visibilityRiskStatus?: string | null;
}

export interface SancaiShowcasePageQuery {
    pageNo?: number | null;
    pageSize?: number | null;
    status?: SancaiShowcaseStatus | null;
}

export const list = (request: SancaiEntryQuery = {}) => {
    return postJson<SancaiEntryRecord[], SancaiEntryQuery>(`${ENTRIES_PATH}/list`, {
        body: request
    });
};

export const get = (id: number) => {
    return getJson<SancaiEntryRecord>(`${ENTRIES_PATH}/${id}`);
};

export const add = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>(`${ENTRIES_PATH}/add`, {
        body: request
    });
};

export const update = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>(`${ENTRIES_PATH}/update`, {
        body: request
    });
};

export const deleteById = (id: number) => {
    return postJson<boolean, SancaiEntryCommand>(`${ENTRIES_PATH}/delete`, {
        body: { id }
    });
};

export const sort = (request: SancaiEntrySortCommand) => {
    return postJson<boolean, SancaiEntrySortCommand>(`${ENTRIES_PATH}/sort`, {
        body: request
    });
};

export const listImages = (entryId: number) => {
    return getJson<SancaiEntryImageRecord[]>(`${ASSET_IMAGES_PATH}/${entryId}`);
};

export const uploadImage = (command: SancaiEntryImageUploadCommand) => {
    const body = new FormData();
    body.append("file", command.file);
    if (command.title) {
        body.append("title", command.title);
    }
    if (command.imageType) {
        body.append("imageType", command.imageType);
    }
    if (typeof command.currentUsed === "boolean") {
        body.append("currentUsed", String(command.currentUsed));
    }
    if (command.replaceImageId) {
        body.append("replaceImageId", String(command.replaceImageId));
    }
    return postFormData<SancaiEntryImageRecord>(
        `${ASSET_IMAGES_PATH}/${command.entryId}/upload`,
        body
    );
};

export const getImageContentUrl = (request: SancaiEntryImageContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    return `${ADMIN_API_BASE_URL}${ASSET_IMAGES_PATH}/${request.entryId}/${request.imageId}/content${search}`;
};

export const listVersions = (entryId: number) => {
    return postJson<SancaiContentVersionRecord[], SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/list`,
        {
            body: { id: entryId }
        }
    );
};

export const getVersion = (entryId: number, versionId: number) => {
    return postJson<SancaiContentVersionRecord, SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/get`,
        {
            body: { id: entryId, versionId }
        }
    );
};

export const requestShowcase = (command: SancaiShowcaseCreateCommand) => {
    return postJson<SancaiShowcaseRecord, SancaiShowcaseCreateCommand>(
        `${ASSET_SHOWCASES_PATH}/request`,
        {
            body: command
        }
    );
};

export const pageShowcases = (query: SancaiShowcasePageQuery = {}) => {
    return postJson<Page<SancaiShowcaseRecord>, SancaiShowcasePageQuery>(
        `${ASSET_SHOWCASES_PATH}/page`,
        {
            body: query
        }
    );
};

export const resetVersion = (entryId: number, versionId: number) => {
    return postJson<SancaiContentVersionRecord, SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/reset`,
        {
            body: { id: entryId, versionId }
        }
    );
};
