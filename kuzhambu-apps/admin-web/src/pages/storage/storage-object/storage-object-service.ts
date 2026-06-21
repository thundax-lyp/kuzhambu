import { ADMIN_API_BASE_URL, postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { StorageContentMode, StorageRecord } from "./storage-object-types";

export interface StoragePageQuery {
    pageNo?: number;
    pageSize?: number;
    contentType?: string | null;
    ownerId?: string | null;
    ownerType?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    referenceOwnerId?: string | null;
    referenceOwnerType?: string | null;
    originalFilename?: string | null;
    remarks?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface StorageSortCommand {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export interface StorageObjectContentUrlCommand {
    mode?: StorageContentMode;
    storageObjectId: string;
}

export const pageStorageObjects = (request: StoragePageQuery = {}) => {
    return postJson<Page<StorageRecord>, StoragePageQuery>("/storage/object/page", {
        body: request
    });
};

export const removeStorageObjects = (ids: string[]) => {
    return postJson<boolean, { ids: string[] }>("/storage/object/delete", {
        body: { ids }
    });
};

export const uploadStorageObject = (file: File) => {
    const body = new FormData();
    body.append("file", file);
    return postFormData<StorageRecord>("/storage/object/upload", body);
};

export const sortStorageObjects = (request: StorageSortCommand) => {
    return postJson<boolean, StorageSortCommand>("/storage/object/sort", {
        body: request
    });
};

export const getStorageObjectContentUrl = (request: StorageObjectContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    return `${ADMIN_API_BASE_URL}/storage/object/${encodeURIComponent(request.storageObjectId)}/content${search}`;
};
