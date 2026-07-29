import { ADMIN_API_BASE_URL, postJson } from "@/api/http";
import type {
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryRecord,
    SancaiVisualAssetRecord
} from "./sancai-visual-types";

const ENTRIES_PATH = "/classics/sancai/entries";
const ASSET_IMAGES_PATH = "/classics/sancai/assets/images";
const ASSET_VISUAL_ASSETS_PATH = "/classics/sancai/assets/visual-assets";

export interface SancaiEntryQuery {
    categoryId?: string | null;
    volumeId?: string | null;
    keyword?: string | null;
    lifecycleStatus?: string | null;
    visibility?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface SancaiEntryCommand {
    id?: string | null;
}

export interface SancaiEntryImageContentUrlCommand {
    entryId: string;
    imageId: string;
    mode?: SancaiEntryImageContentMode;
}

export interface SancaiVisualAssetCommand {
    id?: string | null;
    visualAssetId?: string | null;
    entryId?: string | null;
    versionNo?: number | null;
    status?: string | null;
    sourceImageStorageObjectId?: string | null;
    generatedImageStorageObjectId?: string | null;
    currentUsed?: boolean | null;
    textWeight?: number | null;
    imageWeight?: number | null;
    imageAnalysisMarkdown?: string | null;
    fusionDescription?: string | null;
    visualDescription?: string | null;
    generationParamsJson?: string | null;
}

export interface SancaiVisualAssetContentUrlCommand {
    entryId: string;
    mode?: SancaiEntryImageContentMode;
    visualAssetId: string;
    variant: "source" | "generated";
}

export interface SancaiVisualAssetUseCommand {
    entryId: string;
    visualAssetId: string;
}

export type SancaiVisualAssetRefinementCapability =
    "image_analysis" | "fusion" | "visual" | "image_gen";

export const list = (request: SancaiEntryQuery = {}) => {
    return postJson<SancaiEntryRecord[], SancaiEntryQuery>(`${ENTRIES_PATH}/list`, {
        body: request
    });
};

export const get = (id: string) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>(`${ENTRIES_PATH}/get`, {
        body: { id }
    });
};

export const listImages = (entryId: string) => {
    return postJson<SancaiEntryImageRecord[], { entryId: string }>(`${ASSET_IMAGES_PATH}/list`, {
        body: { entryId }
    });
};

export const listVisualAssets = (entryId: string) => {
    return postJson<SancaiVisualAssetRecord[], { entryId: string }>(
        `${ASSET_VISUAL_ASSETS_PATH}/list`,
        {
            body: { entryId }
        }
    );
};

export const getImageContentUrl = (request: SancaiEntryImageContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    return `${ADMIN_API_BASE_URL}${ASSET_IMAGES_PATH}/${request.entryId}/${request.imageId}/content${search}`;
};

export const getVisualAssetContentUrl = (request: SancaiVisualAssetContentUrlCommand) => {
    const mode = request.mode || "preview";
    const search = mode === "download" ? "?download=true" : "";
    const suffix = request.variant === "source" ? "source-content" : "generated-content";
    return `${ADMIN_API_BASE_URL}${ASSET_VISUAL_ASSETS_PATH}/${request.entryId}/${request.visualAssetId}/${suffix}${search}`;
};

export const updateVisualAsset = (command: SancaiVisualAssetCommand) => {
    return postJson<SancaiVisualAssetRecord, SancaiVisualAssetCommand>(
        `${ASSET_VISUAL_ASSETS_PATH}/update`,
        {
            body: command
        }
    );
};

export const changeCurrentVisualAsset = (command: SancaiVisualAssetUseCommand) => {
    return postJson<boolean, SancaiVisualAssetUseCommand>(
        `${ASSET_VISUAL_ASSETS_PATH}/current/change`,
        {
            body: command
        }
    );
};
