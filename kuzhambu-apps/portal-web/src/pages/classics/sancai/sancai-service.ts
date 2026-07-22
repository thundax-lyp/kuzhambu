import { buildApiUrl, postJson } from "@/api/http";
import type {
    SancaiCategoryRecord,
    SancaiEntryPage,
    SancaiEntryImageRecord,
    SancaiEntryQuery,
    SancaiEntryRecord,
    SancaiVisualAssetRecord,
    SancaiVolumeRecord
} from "./sancai-types";

const SANCAI_PATH = "/portal/classics/sancai";

export const listCategories = () => {
    return postJson<SancaiCategoryRecord[]>(`${SANCAI_PATH}/categories/list`, {}).then(
        (categories) =>
            categories.map((category) => ({
                ...category,
                thumbnailUrl: normalizeMediaUrl(category.thumbnailUrl)
            }))
    );
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
    return postJson<SancaiEntryRecord>(`${SANCAI_PATH}/entries/get`, { id }).then((entry) => ({
        ...entry,
        currentVisualAsset: normalizeVisualAsset(entry.currentVisualAsset),
        images: entry.images?.map(normalizeImage)
    }));
};

const normalizeImage = (image: SancaiEntryImageRecord): SancaiEntryImageRecord => ({
    ...image,
    downloadUrl: normalizeMediaUrl(image.downloadUrl),
    previewUrl: normalizeMediaUrl(image.previewUrl)
});

const normalizeVisualAsset = (
    visualAsset?: SancaiVisualAssetRecord | null
): SancaiVisualAssetRecord | null | undefined => {
    if (!visualAsset) {
        return visualAsset;
    }
    return {
        ...visualAsset,
        generatedPreviewUrl: normalizeMediaUrl(visualAsset.generatedPreviewUrl),
        sourcePreviewUrl: normalizeMediaUrl(visualAsset.sourcePreviewUrl)
    };
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
