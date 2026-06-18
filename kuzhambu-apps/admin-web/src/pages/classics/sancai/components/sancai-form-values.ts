import type { SancaiCategoryRecord, SancaiVolumeRecord } from "../sancai-types";

export interface SancaiCategoryFormValues {
    categoryType: string;
    title: string;
}

export interface SancaiVolumeFormValues {
    categoryId: number | null;
    title: string;
    volumeType: string;
}

export const toCategoryFormValues = (category?: SancaiCategoryRecord): SancaiCategoryFormValues => {
    return {
        categoryType: category?.categoryType || "FORMAL",
        title: category?.title || ""
    };
};

export const toVolumeFormValues = (
    volume?: SancaiVolumeRecord,
    fallbackCategoryId?: number | null
): SancaiVolumeFormValues => {
    return {
        categoryId: volume?.categoryId ?? fallbackCategoryId ?? null,
        title: volume?.title || "",
        volumeType: volume?.volumeType || "MAIN"
    };
};
