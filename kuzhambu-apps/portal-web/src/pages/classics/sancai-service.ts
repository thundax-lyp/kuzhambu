import { getJson } from "@/api/http";
import type {
    SancaiCategoryRecord,
    SancaiEntryPage,
    SancaiEntryQuery,
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "./sancai-types";

const SANCAI_PATH = "/portal/classics/sancai";

export const listCategories = () => {
    return getJson<SancaiCategoryRecord[]>(`${SANCAI_PATH}/categories`);
};

export const listVolumes = (categoryId?: number | null) => {
    return getJson<SancaiVolumeRecord[]>(`${SANCAI_PATH}/volumes`, { categoryId });
};

export const pageEntries = (query: SancaiEntryQuery = {}) => {
    return getJson<SancaiEntryPage>(`${SANCAI_PATH}/entries`, {
        categoryId: query.categoryId,
        keyword: query.keyword,
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        volumeId: query.volumeId
    });
};

export const getEntry = (id: number) => {
    return getJson<SancaiEntryRecord>(`${SANCAI_PATH}/entries/${id}`);
};
