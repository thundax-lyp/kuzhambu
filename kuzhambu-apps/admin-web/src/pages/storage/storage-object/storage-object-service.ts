import { postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { StorageRecord } from "./storage-object-types";

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
