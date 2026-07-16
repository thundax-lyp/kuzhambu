import { postJson } from "@/api/http";
import type {
    SancaiCategoryRecord,
    SancaiEntryPage,
    SancaiEntryQuery,
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "./sancai-types";

const SANCAI_PATH = "/portal/classics/sancai";

export const listCategories = () => {
    return postJson<SancaiCategoryRecord[]>(`${SANCAI_PATH}/categories/list`, {});
};

export const listVolumes = (categoryId?: number | null) => {
    return postJson<SancaiVolumeRecord[]>(`${SANCAI_PATH}/volumes/list`, { categoryId });
};

export const pageEntries = (query: SancaiEntryQuery = {}) => {
    return postJson<SancaiEntryPage>(`${SANCAI_PATH}/entries/page`, {
        categoryId: query.categoryId,
        keyword: query.keyword,
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        volumeId: query.volumeId
    });
};

export const getEntry = (id: number) => {
    return postJson<SancaiEntryRecord>(`${SANCAI_PATH}/entries/get`, { id });
};
