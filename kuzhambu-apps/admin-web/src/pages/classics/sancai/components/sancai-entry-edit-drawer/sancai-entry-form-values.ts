import type {
    SancaiEntryRecord,
    SancaiVolumeRecord
} from "@/pages/classics/sancai/sancai-types";

export interface SancaiEntryFormValues {
    categoryId: number | null;
    originalText: string;
    summary: string;
    title: string;
    translationText: string;
    volumeId: number | null;
    visibility: string;
}

export const toEntryFormValues = (
    entry?: SancaiEntryRecord,
    volumes: SancaiVolumeRecord[] = [],
    fallbackCategoryId?: number | null,
    fallbackVolumeId?: number | null
): SancaiEntryFormValues => {
    const volumeId = entry?.volumeId ?? fallbackVolumeId ?? null;
    const currentVolume = volumes.find((volume) => volume.id === volumeId);
    return {
        categoryId: currentVolume?.categoryId ?? fallbackCategoryId ?? null,
        originalText: entry?.originalText || "",
        summary: entry?.summary || "",
        title: entry?.title || "",
        translationText: entry?.translationText || "",
        volumeId,
        visibility: entry?.visibility || "PUBLIC"
    };
};
