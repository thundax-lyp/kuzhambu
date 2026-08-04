import type { SancaiVolumeRecord } from "@/pages/classics/sancai/sancai-types";

export interface SancaiVolumeFormValues {
    categoryId: string | null;
    title: string;
    volumeType: string;
}

export const toVolumeFormValues = (
    volume?: SancaiVolumeRecord,
    fallbackCategoryId?: string | null
): SancaiVolumeFormValues => {
    return {
        categoryId: volume?.categoryId ?? fallbackCategoryId ?? null,
        title: volume?.title || "",
        volumeType: volume?.volumeType || "MAIN"
    };
};
