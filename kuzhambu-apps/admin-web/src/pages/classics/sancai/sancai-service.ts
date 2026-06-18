import { getJson, postJson } from "@/api/http";
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

export interface SancaiEntrySaveCommand {
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

export const listCategories = () => {
    return postJson<SancaiCategoryRecord[]>("/classics/sancai/categories/list");
};

export const listVolumes = (request: SancaiVolumeQuery = {}) => {
    return postJson<SancaiVolumeRecord[], SancaiVolumeQuery>("/classics/sancai/volumes/list", {
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

export const saveEntry = (request: SancaiEntrySaveCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntrySaveCommand>("/classics/sancai/entries/save", {
        body: request
    });
};
