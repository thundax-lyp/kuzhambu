import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import * as visualPreviewService from "../sancai-visual-preview-service";
import type { SancaiVisualPreviewAssetRecord } from "../sancai-visual-preview-types";

export interface SancaiEntryVisualPreviewState {
    currentVisualAsset: SancaiVisualPreviewAssetRecord | null;
    generatedPreviewUrl?: string;
    visualDescription?: string;
}

const selectCurrentVisualAsset = (assets: SancaiVisualPreviewAssetRecord[]) => {
    return [...assets]
        .filter((asset) => asset.currentUsed !== false)
        .sort((left, right) => (right.versionNo ?? 0) - (left.versionNo ?? 0))[0];
};

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

export const useSancaiEntryVisualPreviewState = (
    entryId: string | undefined
): SancaiEntryVisualPreviewState => {
    const visualAssetsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "visual-assets", entryId],
        queryFn: () => visualPreviewService.listVisualAssets(entryId ?? ""),
        enabled: Boolean(entryId),
        retry: false
    });
    const currentVisualAsset = useMemo(
        () => selectCurrentVisualAsset(visualAssetsQuery.data || []) ?? null,
        [visualAssetsQuery.data]
    );

    return {
        currentVisualAsset,
        generatedPreviewUrl: resolveStorageUrl(currentVisualAsset?.generatedPreviewUrl),
        visualDescription: currentVisualAsset?.visualDescription?.trim() || undefined
    };
};
