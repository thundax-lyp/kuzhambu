import { buildApiUrl, postJson } from "@/api/http";
import type { SancaiEntryRecord } from "@/pages/classics/sancai-types";

export type DiscoverySearchItemType = "SANCAI_ENTRY";

export interface DiscoverySearchItemModel {
    entry: SancaiEntryRecord;
    type: DiscoverySearchItemType;
}

export const isDiscoverySearchItemType = (value: string | null): value is DiscoverySearchItemType =>
    value === "SANCAI_ENTRY";

export const getSearchItem = async (
    type: DiscoverySearchItemType,
    id: number
): Promise<DiscoverySearchItemModel> => {
    switch (type) {
        case "SANCAI_ENTRY":
            return {
                entry: await getSancaiEntry(id),
                type
            };
    }
};

const getSancaiEntry = (id: number) => {
    return postJson<SancaiEntryRecord>("/portal/classics/sancai/entries/get", { id }).then(
        (entry) => ({
            ...entry,
            currentVisualAsset: entry.currentVisualAsset
                ? {
                      ...entry.currentVisualAsset,
                      generatedPreviewUrl: normalizeMediaUrl(
                          entry.currentVisualAsset.generatedPreviewUrl
                      ),
                      sourcePreviewUrl: normalizeMediaUrl(entry.currentVisualAsset.sourcePreviewUrl)
                  }
                : entry.currentVisualAsset,
            images: entry.images?.map((image) => ({
                ...image,
                downloadUrl: normalizeMediaUrl(image.downloadUrl),
                previewUrl: normalizeMediaUrl(image.previewUrl)
            }))
        })
    );
};

const normalizeMediaUrl = (url?: string | null) => {
    const trimmedUrl = url?.trim();
    if (!trimmedUrl) {
        return trimmedUrl;
    }
    if (/^(https?:|data:|blob:)/iu.test(trimmedUrl)) {
        return trimmedUrl;
    }
    const portalPath = trimmedUrl.startsWith("/api/") ? trimmedUrl.slice(4) : trimmedUrl;
    return buildApiUrl(portalPath);
};
