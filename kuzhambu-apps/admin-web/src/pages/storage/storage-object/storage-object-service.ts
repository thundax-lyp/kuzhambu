import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { StorageRecord } from "./storage-object-types";

export interface StoragePageQuery {
    pageNo?: number;
    pageSize?: number;
    contentType?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    originalFilename?: string | null;
    remarks?: string | null;
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
    return postJson<boolean, Array<{ id: string }>>("/storage/object/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const sortStorageObjects = (request: StorageSortCommand) => {
    return postJson<boolean, StorageSortCommand>("/storage/object/sort", {
        body: request
    });
};
