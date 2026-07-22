import { postJson } from "@/api/http";
import type { SancaiCategoryRecord, SancaiCategoryTypeRecord } from "./sancai-types";

export interface SancaiCategoryCommand {
    categoryType?: string | null;
    id?: number | null;
    title?: string | null;
}

export interface SancaiCategorySortCommand {
    orderedIds: number[];
    sortDirection?: "ASC" | "DESC" | null;
}

export const listTypes = () => {
    return postJson<SancaiCategoryTypeRecord[]>("/classics/sancai/categories/types/list");
};

export const list = () => {
    return postJson<SancaiCategoryRecord[]>("/classics/sancai/categories/list");
};

export const add = (request: SancaiCategoryCommand) => {
    return postJson<SancaiCategoryRecord, SancaiCategoryCommand>(
        "/classics/sancai/categories/add",
        {
            body: request
        }
    );
};

export const update = (request: SancaiCategoryCommand) => {
    return postJson<SancaiCategoryRecord, SancaiCategoryCommand>(
        "/classics/sancai/categories/update",
        {
            body: request
        }
    );
};

export const deleteById = (id: number) => {
    return postJson<boolean, SancaiCategoryCommand>("/classics/sancai/categories/delete", {
        body: { id }
    });
};

export const sort = (request: SancaiCategorySortCommand) => {
    return postJson<boolean, SancaiCategorySortCommand>("/classics/sancai/categories/sort", {
        body: request
    });
};
