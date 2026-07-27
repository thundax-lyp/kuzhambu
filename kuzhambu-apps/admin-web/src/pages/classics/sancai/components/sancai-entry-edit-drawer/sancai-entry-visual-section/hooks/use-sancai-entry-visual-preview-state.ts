import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";
import type { SancaiVisualAssetRecord } from "@/pages/classics/sancai/sancai-types";
import type { SancaiEntryVisualPreviewState } from "../sancai-entry-visual-section";

const selectCurrentVisualAsset = (assets: SancaiVisualAssetRecord[]) => {
    return [...assets]
        .filter((asset) => asset.currentUsed !== false)
        .sort((left, right) => (right.versionNo ?? 0) - (left.versionNo ?? 0))[0];
};

const resolveStorageUrl = (url?: string | null) => {
    return url ? toAuthenticatedResourceUrl(url) : undefined;
};

export const useSancaiEntryVisualPreviewState = (
    entryId: number | undefined
): SancaiEntryVisualPreviewState => {
    const visualAssetsQuery = useQuery({
        queryKey: ["classics", "sancai", "entries", "visual-assets", entryId],
        queryFn: () => entryService.listVisualAssets(entryId ?? 0),
        enabled: Boolean(entryId),
        retry: false
    });

    return useMemo(() => {
        const currentVisualAsset = selectCurrentVisualAsset(visualAssetsQuery.data || []) ?? null;
        const visualAssetId =
            currentVisualAsset?.visualAssetId ?? currentVisualAsset?.id ?? undefined;
        const hasGeneratedVisualImage = Boolean(
            currentVisualAsset?.generatedImageStorageObjectId ||
            currentVisualAsset?.generatedPreviewUrl
        );
        const generatedPreviewUrl = resolveStorageUrl(
            hasGeneratedVisualImage
                ? (currentVisualAsset?.generatedPreviewUrl ??
                      (entryId && visualAssetId
                          ? entryService.getVisualAssetContentUrl({
                                entryId,
                                visualAssetId,
                                variant: "generated"
                            })
                          : undefined))
                : undefined
        );

        return {
            currentVisualAsset,
            generatedPreviewUrl,
            visualDescription: currentVisualAsset?.visualDescription
        };
    }, [entryId, visualAssetsQuery.data]);
};
