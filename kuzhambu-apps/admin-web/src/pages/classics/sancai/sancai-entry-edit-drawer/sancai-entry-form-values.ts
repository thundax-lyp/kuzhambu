import type { SancaiEntryRecord, SancaiVolumeRecord } from "@/pages/classics/sancai/sancai-types";
import { isSameId } from "@/types/id";

export interface SancaiEntryFormValues {
    categoryId: string | null;
    originalText: string;
    summary: string;
    title: string;
    translationText: string;
    volumeId: string | null;
    visibility: string;
}

export const toEntryFormValues = (
    entry?: SancaiEntryRecord,
    volumes: SancaiVolumeRecord[] = [],
    fallbackCategoryId?: string | null,
    fallbackVolumeId?: string | null
): SancaiEntryFormValues => {
    const volumeId = entry?.volumeId ?? fallbackVolumeId ?? null;
    const currentVolume = volumes.find((volume) => isSameId(volume.id, volumeId));
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
