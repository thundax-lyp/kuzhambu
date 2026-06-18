import { postJson } from "@/api/http";
import type { SancaiContentRecord } from "../sancai-types";

export interface SancaiContentCommand {
    id?: number | null;
    entryId?: number | null;
    question?: string | null;
    answer?: string | null;
    source?: string | null;
}

export interface SancaiContentSortCommand {
    entryId: number;
    orderedIds: number[];
    sortDirection?: "ASC" | "DESC" | null;
}

export const listByEntry = (entryId: number) => {
    return postJson<SancaiContentRecord[], { entryId: number }>(
        "/classics/sancai/contents/list",
        {
            body: { entryId }
        }
    );
};

export const add = (request: SancaiContentCommand) => {
    return postJson<SancaiContentRecord, SancaiContentCommand>("/classics/sancai/contents/add", {
        body: request
    });
};

export const update = (request: SancaiContentCommand) => {
    return postJson<SancaiContentRecord, SancaiContentCommand>(
        "/classics/sancai/contents/update",
        {
            body: request
        }
    );
};

export const deleteById = (id: number) => {
    return postJson<boolean, { id: number }>("/classics/sancai/contents/delete", {
        body: { id }
    });
};

export const sort = (request: SancaiContentSortCommand) => {
    return postJson<boolean, SancaiContentSortCommand>("/classics/sancai/contents/sort", {
        body: request
    });
};
