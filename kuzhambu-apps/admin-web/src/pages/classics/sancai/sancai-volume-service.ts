import { postJson } from "@/api/http";
import type { SancaiVolumeRecord, SancaiVolumeTypeRecord } from "./sancai-types";

export interface SancaiVolumeQuery {
    categoryId?: string | null;
}

export interface SancaiVolumeCommand {
    categoryId?: string | null;
    id?: string | null;
    title?: string | null;
    volumeType?: string | null;
}

export interface SancaiVolumeSortCommand {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC" | null;
}

export const listTypes = () => {
    return postJson<SancaiVolumeTypeRecord[]>("/classics/sancai/volumes/types/list");
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

export const deleteById = (id: string) => {
    return postJson<boolean, SancaiVolumeCommand>("/classics/sancai/volumes/delete", {
        body: { id }
    });
};

export const sort = (request: SancaiVolumeSortCommand) => {
    return postJson<boolean, SancaiVolumeSortCommand>("/classics/sancai/volumes/sort", {
        body: request
    });
};
