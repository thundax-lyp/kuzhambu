import { getJson, postJson } from "@/api/http";
import type { DictItem } from "@/types/dict";
import type { Page } from "@/types/page";
import type { SancaiCategoryRecord, SancaiEntryRecord, SancaiVolumeRecord } from "./sancai-types";

export interface SancaiVolumeQuery {
    categoryId?: number | null;
}

export interface SancaiEntryPageQuery {
    pageNo?: number;
    pageSize?: number;
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

export interface SancaiCategoryCommand {
    id?: number | null;
    title?: string | null;
    categoryType?: string | null;
}

export interface SancaiVolumeCommand {
    id?: number | null;
    categoryId?: number | null;
    title?: string | null;
    volumeType?: string | null;
}

export interface SancaiCategorySortCommand {
    orderedIds: number[];
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

export const listCategoryTypes = () => {
    return getJson<DictItem[]>("/classics/sancai/categories/types");
};

export const listVolumeTypes = () => {
    return getJson<DictItem[]>("/classics/sancai/volumes/types");
};

export const listCategories = () => {
    return postJson<SancaiCategoryRecord[]>("/classics/sancai/categories/list");
};

export const getCategory = (id: number) => {
    return getJson<SancaiCategoryRecord>(`/classics/sancai/categories/${id}`);
};

export const addCategory = (request: SancaiCategoryCommand) => {
    return postJson<SancaiCategoryRecord, SancaiCategoryCommand>("/classics/sancai/categories/add", {
        body: request
    });
};

export const updateCategory = (request: SancaiCategoryCommand) => {
    return postJson<SancaiCategoryRecord, SancaiCategoryCommand>(
        "/classics/sancai/categories/update",
        {
            body: request
        }
    );
};

export const removeCategory = (request: SancaiCategoryCommand) => {
    return postJson<boolean, SancaiCategoryCommand>("/classics/sancai/categories/delete", {
        body: request
    });
};

export const sortCategories = (request: SancaiCategorySortCommand) => {
    return postJson<boolean, SancaiCategorySortCommand>("/classics/sancai/categories/sort", {
        body: request
    });
};

export const sortVolumes = (request: SancaiCategorySortCommand) => {
    return postJson<boolean, SancaiCategorySortCommand>("/classics/sancai/volumes/sort", {
        body: request
    });
};

export const listVolumes = (request: SancaiVolumeQuery = {}) => {
    return postJson<SancaiVolumeRecord[], SancaiVolumeQuery>("/classics/sancai/volumes/list", {
        body: request
    });
};

export const getVolume = (id: number) => {
    return getJson<SancaiVolumeRecord>(`/classics/sancai/volumes/${id}`);
};

export const addVolume = (request: SancaiVolumeCommand) => {
    return postJson<SancaiVolumeRecord, SancaiVolumeCommand>("/classics/sancai/volumes/add", {
        body: request
    });
};

export const updateVolume = (request: SancaiVolumeCommand) => {
    return postJson<SancaiVolumeRecord, SancaiVolumeCommand>("/classics/sancai/volumes/update", {
        body: request
    });
};

export const removeVolume = (request: SancaiVolumeCommand) => {
    return postJson<boolean, SancaiVolumeCommand>("/classics/sancai/volumes/delete", {
        body: request
    });
};

export const pageEntries = (request: SancaiEntryPageQuery = {}) => {
    return postJson<Page<SancaiEntryRecord>, SancaiEntryPageQuery>(
        "/classics/sancai/entries/page",
        {
            body: request
        }
    );
};

export const getEntry = (id: number) => {
    return getJson<SancaiEntryRecord>(`/classics/sancai/entries/${id}`);
};

export const addEntry = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>("/classics/sancai/entries/add", {
        body: request
    });
};

export const updateEntry = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>("/classics/sancai/entries/update", {
        body: request
    });
};
