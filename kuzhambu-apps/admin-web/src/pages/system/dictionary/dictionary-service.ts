import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { DictRecord } from "./dictionary-types";

export interface DictPageQuery {
    pageNo?: number;
    pageSize?: number;
    type?: string | null;
    label?: string | null;
    remarks?: string | null;
}

export interface DictSaveCommand {
    id?: string | null;
    type?: string | null;
    label?: string | null;
    value?: string | null;
    remarks?: string | null;
}

export const page = (request: DictPageQuery = {}) => {
    return postJson<Page<DictRecord>, DictPageQuery>("/sys/dict/page", {
        body: request
    });
};

export const addDictionary = (request: DictSaveCommand) => {
    return postJson<DictRecord, DictSaveCommand>("/sys/dict/create", {
        body: request
    });
};

export const changeDictionaryInfo = (request: DictSaveCommand) => {
    return postJson<DictRecord, DictSaveCommand>("/sys/dict/update", {
        body: request
    });
};

export const removeDictionaries = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/dict/delete", {
        body: ids.map((id) => ({ id }))
    });
};
