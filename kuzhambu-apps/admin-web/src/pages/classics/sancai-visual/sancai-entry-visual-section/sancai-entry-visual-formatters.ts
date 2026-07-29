import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import type {
    SancaiEntryImageRecord,
    SancaiVisualAssetRecord
} from "@/pages/classics/sancai-visual/sancai-visual-types";
import { normalizeId } from "@/types/id";

export const readVisualAssetTitle = (asset: SancaiVisualAssetRecord | undefined | null) => {
    if (!asset) {
        return "未选择视觉处理";
    }
    if (!asset.versionNo && !asset.visualAssetId && !asset.id) {
        return "待写入历史";
    }
    return `历史记录 ${asset.versionNo ?? asset.visualAssetId ?? asset.id ?? "-"}`;
};

export const readVisualAssetId = (asset: SancaiVisualAssetRecord) => {
    return normalizeId(asset.visualAssetId ?? asset.id);
};

export const readVisualAssetStatusLabel = (status?: string | null) => {
    switch (status) {
        case "READY":
            return "已完成";
        case "QUEUED":
        case "PENDING":
            return "排队中";
        case "PROCESSING":
        case "RUNNING":
            return "正在处理";
        case "DRAFT":
            return "草稿";
        case "FAILED":
            return "失败";
        default:
            return status || "-";
    }
};

export const readVisualAssetStatusTagColor = (status?: string | null) => {
    switch (status) {
        case "READY":
            return "success";
        case "QUEUED":
        case "PENDING":
        case "PROCESSING":
        case "RUNNING":
            return "warning";
        case "DRAFT":
            return "default";
        case "ERROR":
        case "FAILED":
            return "error";
        default:
            return "default";
    }
};

export const readImageTitle = (image: SancaiEntryImageRecord) => {
    return image.title?.trim() || image.originalFilename?.trim() || `图片 ${image.id}`;
};

export const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};
