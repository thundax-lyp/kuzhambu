import { postJson } from "@/api/http";
import type { SancaiEntryRecord } from "../sancai-types";

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

export const list = (request: SancaiEntryQuery = {}) => {
    return postJson<SancaiEntryRecord[], SancaiEntryQuery>("/classics/sancai/entries/list", {
        body: request
    });
};

export const add = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>("/classics/sancai/entries/add", {
        body: request
    });
};

export const update = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>("/classics/sancai/entries/update", {
        body: request
    });
};

export const deleteById = (id: number) => {
    return postJson<boolean, SancaiEntryCommand>("/classics/sancai/entries/delete", {
        body: { id }
    });
};

export const sort = (request: SancaiEntrySortCommand) => {
    return postJson<boolean, SancaiEntrySortCommand>("/classics/sancai/entries/sort", {
        body: request
    });
};
