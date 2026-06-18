import { getJson, postJson } from "@/api/http";
import type { DictItem } from "@/types/dict";
import type { SancaiVolumeRecord } from "../sancai-types";

export interface SancaiVolumeQuery {
    categoryId?: number | null;
}

export interface SancaiVolumeCommand {
    categoryId?: number | null;
    id?: number | null;
    title?: string | null;
    volumeType?: string | null;
}

export interface SancaiVolumeSortCommand {
    orderedIds: number[];
    sortDirection?: "ASC" | "DESC" | null;
}

export const listTypes = () => {
    return getJson<DictItem[]>("/classics/sancai/volumes/types");
};

export const list = (request: SancaiVolumeQuery = {}) => {
    return postJson<SancaiVolumeRecord[], SancaiVolumeQuery>("/classics/sancai/volumes/list", {
        body: request
    });
};

export const add = (request: SancaiVolumeCommand) => {
    return postJson<SancaiVolumeRecord, SancaiVolumeCommand>("/classics/sancai/volumes/add", {
        body: request
    });
};

export const update = (request: SancaiVolumeCommand) => {
    return postJson<SancaiVolumeRecord, SancaiVolumeCommand>("/classics/sancai/volumes/update", {
        body: request
    });
};

export const deleteById = (id: number) => {
    return postJson<boolean, SancaiVolumeCommand>("/classics/sancai/volumes/delete", {
        body: { id }
    });
};

export const sort = (request: SancaiVolumeSortCommand) => {
    return postJson<boolean, SancaiVolumeSortCommand>("/classics/sancai/volumes/sort", {
        body: request
    });
};
