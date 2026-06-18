import type { SancaiCategoryRecord, SancaiEntryRecord, SancaiVolumeRecord } from "../sancai-types";

export interface SancaiCategoryFormValues {
    categoryType: string;
    title: string;
}

export interface SancaiVolumeFormValues {
    categoryId: number | null;
    title: string;
    volumeType: string;
}

export interface SancaiEntryFormValues {
    originalText: string;
    summary: string;
    title: string;
    translationText: string;
    visibility: string;
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

export const toEntryFormValues = (entry?: SancaiEntryRecord): SancaiEntryFormValues => {
    return {
        originalText: entry?.originalText || "",
        summary: entry?.summary || "",
        title: entry?.title || "",
        translationText: entry?.translationText || "",
        visibility: entry?.visibility || "PUBLIC"
    };
};
