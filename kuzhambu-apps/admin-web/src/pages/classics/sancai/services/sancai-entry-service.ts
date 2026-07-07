import { ADMIN_API_BASE_URL, getJson, postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    SancaiContentVersionRecord,
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryLifecycleStatus,
    SancaiEntryRecord,
    SancaiRefinementBatchRecord,
    SancaiShowcaseRecord,
    SancaiVisualAssetRecord
} from "../sancai-types";

const ENTRIES_PATH = "/classics/sancai/entries";
const ASSET_IMAGES_PATH = "/classics/sancai/assets/images";
const ASSET_SHOWCASES_PATH = "/classics/sancai/assets/showcases";
const ASSET_VISUAL_ASSETS_PATH = "/classics/sancai/assets/visual-assets";

export interface SancaiEntryQuery {
    categoryId?: number | null;
    volumeId?: number | null;
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
    id?: number | null;
    volumeId?: number | null;
    title?: string | null;
    originalText?: string | null;
    translationText?: string | null;
    summary?: string | null;
    lifecycleStatus?: string | null;
    visibility?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
}

export interface SancaiEntryLifecycleCommand {
    id: number;
    lifecycleStatus: SancaiEntryLifecycleStatus;
}

export interface SancaiEntrySortCommand {
    orderedIds: number[];
    sortDirection?: "ASC" | "DESC" | null;
}

interface SancaiVersionCommand {
    id: number;
    versionId?: number | null;
}

export interface SancaiEntryImageUploadCommand {
    currentUsed?: boolean;
    entryId: number;
    file: File;
    imageType?: string | null;
    replaceImageId?: number | null;
    title?: string | null;
}

export interface SancaiEntryImageContentUrlCommand {
    entryId: number;
    imageId: number;
    mode?: SancaiEntryImageContentMode;
}

export interface SancaiEntryImageMutationCommand {
    entryId: number;
    imageId: number;
}

export interface SancaiEntryImageSortCommand {
    entryId: number;
    orderedIds: number[];
    sortDirection?: "ASC" | "DESC" | null;
}

export interface SancaiVisualAssetContentUrlCommand {
    entryId: number;
    mode?: SancaiEntryImageContentMode;
    visualAssetId: number;
    variant: "source" | "generated";
}

export interface SancaiVisualAssetCommand {
    id?: number | null;
    visualAssetId?: number | null;
    entryId?: number | null;
    versionNo?: number | null;
    status?: string | null;
    sourceImageStorageObjectId?: number | null;
    generatedImageStorageObjectId?: number | null;
    currentUsed?: boolean | null;
    textWeight?: number | null;
    imageWeight?: number | null;
    imageAnalysisMarkdown?: string | null;
    fusionDescription?: string | null;
    visualDescription?: string | null;
    generationParamsJson?: string | null;
}

export interface SancaiVisualAssetUseCommand {
    entryId: number;
    visualAssetId: number;
}

export type SancaiVisualAssetRefinementCapability =
    "image_analysis" | "fusion" | "visual" | "image_gen";

export interface SancaiRefinementBatchCreateCommand {
    capability: "image_analysis" | "visual";
    contentType: "SANCAI_ENTRY";
    failureSummaryJson?: string | null;
    scope: "classics";
    totalCount: number;
}

// prettier-ignore
export type SancaiShowcaseStatus =
    | "REQUESTED"
    | "PROCESSING"
    | "COMPLETED"
    | "FAILED"
    | "EXPIRED";

export interface SancaiShowcaseCreateCommand {
    status?: string | null;
    scopeJson?: string | null;
    storageObjectId?: number | null;
    entryCount?: number | null;
    visibilityRiskStatus?: string | null;
}

export interface SancaiShowcasePageQuery {
    pageNo?: number | null;
    pageSize?: number | null;
    status?: SancaiShowcaseStatus | null;
}

export const list = (request: SancaiEntryQuery = {}) => {
    return postJson<SancaiEntryRecord[], SancaiEntryQuery>(`${ENTRIES_PATH}/list`, {
        body: request
    });
};

export const get = (id: number) => {
    return getJson<SancaiEntryRecord>(`${ENTRIES_PATH}/${id}`);
};

export const add = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>(`${ENTRIES_PATH}/add`, {
        body: request
    });
};

export const update = (request: SancaiEntryCommand) => {
    return postJson<SancaiEntryRecord, SancaiEntryCommand>(`${ENTRIES_PATH}/update`, {
        body: request
    });
};

export const changeLifecycleStatus = (request: SancaiEntryLifecycleCommand) => {
    return postJson<boolean, SancaiEntryLifecycleCommand>(`${ENTRIES_PATH}/lifecycle/change`, {
        body: request
    });
};

export const deleteById = (id: number) => {
    return postJson<boolean, SancaiEntryCommand>(`${ENTRIES_PATH}/delete`, {
        body: { id }
    });
};

export const sort = (request: SancaiEntrySortCommand) => {
    return postJson<boolean, SancaiEntrySortCommand>(`${ENTRIES_PATH}/sort`, {
        body: request
    });
};

export const listImages = (entryId: number) => {
    return getJson<SancaiEntryImageRecord[]>(`${ASSET_IMAGES_PATH}/${entryId}`);
};

export const deleteImage = (command: SancaiEntryImageMutationCommand) => {
    return postJson<boolean, { entryId: number; id: number }>(`${ASSET_IMAGES_PATH}/delete`, {
        body: {
            entryId: command.entryId,
            id: command.imageId
        }
    });
};

export const changeCurrentImage = (command: SancaiEntryImageMutationCommand) => {
    return postJson<boolean, { entryId: number; id: number }>(
        `${ASSET_IMAGES_PATH}/current/change`,
        {
            body: {
                entryId: command.entryId,
                id: command.imageId
            }
        }
    );
};

export const sortImages = (command: SancaiEntryImageSortCommand) => {
    return postJson<boolean, SancaiEntryImageSortCommand>(`${ASSET_IMAGES_PATH}/sort`, {
        body: command
    });
};

export const listVisualAssets = (entryId: number) => {
    return getJson<SancaiVisualAssetRecord[]>(`${ASSET_VISUAL_ASSETS_PATH}/${entryId}`);
};

export const uploadImage = (command: SancaiEntryImageUploadCommand) => {
    const body = new FormData();
    body.append("file", command.file);
    if (command.title) {
        body.append("title", command.title);
    }
    if (command.imageType) {
        body.append("imageType", command.imageType);
    }
    if (typeof command.currentUsed === "boolean") {
        body.append("currentUsed", String(command.currentUsed));
    }
    if (command.replaceImageId) {
        body.append("replaceImageId", String(command.replaceImageId));
    }
    return postFormData<SancaiEntryImageRecord>(
        `${ASSET_IMAGES_PATH}/${command.entryId}/upload`,
        body
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
        "/classics/sancai/assets/visual-assets/current/change",
        {
            body: command
        }
    );
};

export const listVersions = (entryId: number) => {
    return postJson<SancaiContentVersionRecord[], SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/list`,
        {
            body: { id: entryId }
        }
    );
};

export const getVersion = (entryId: number, versionId: number) => {
    return postJson<SancaiContentVersionRecord, SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/get`,
        {
            body: { id: entryId, versionId }
        }
    );
};

export const requestShowcase = (command: SancaiShowcaseCreateCommand) => {
    return postJson<SancaiShowcaseRecord, SancaiShowcaseCreateCommand>(
        `${ASSET_SHOWCASES_PATH}/request`,
        {
            body: command
        }
    );
};

export const pageShowcases = (query: SancaiShowcasePageQuery = {}) => {
    return postJson<Page<SancaiShowcaseRecord>, SancaiShowcasePageQuery>(
        `${ASSET_SHOWCASES_PATH}/page`,
        {
            body: query
        }
    );
};

export const resetVersion = (entryId: number, versionId: number) => {
    return postJson<SancaiContentVersionRecord, SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/reset`,
        {
            body: { id: entryId, versionId }
        }
    );
};

export const createRefinementBatch = (command: SancaiRefinementBatchCreateCommand) => {
    return postJson<SancaiRefinementBatchRecord, SancaiRefinementBatchCreateCommand>(
        "/ai/refinement/task/batch/create",
        {
            body: command
        }
    );
};

export const getRefinementBatch = (batchId: number) => {
    return postJson<SancaiRefinementBatchRecord, { batchId: number }>(
        "/ai/refinement/task/batch/get",
        {
            body: { batchId }
        }
    );
};

export const cancelRefinementBatch = (batchId: number) => {
    return postJson<SancaiRefinementBatchRecord, { batchId: number }>(
        "/ai/refinement/task/batch/cancel",
        {
            body: { batchId }
        }
    );
};
