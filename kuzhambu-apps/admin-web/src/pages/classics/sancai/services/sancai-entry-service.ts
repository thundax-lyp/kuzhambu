import { getJson, postJson } from "@/api/http";
import type { SancaiContentVersionRecord, SancaiEntryRecord } from "../sancai-types";

const ENTRIES_PATH = "/classics/sancai/entries";

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

export const resetVersion = (entryId: number, versionId: number) => {
    return postJson<SancaiContentVersionRecord, SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/reset`,
        {
            body: { id: entryId, versionId }
        }
    );
};
