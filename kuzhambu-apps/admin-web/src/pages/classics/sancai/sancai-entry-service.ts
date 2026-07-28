import { ADMIN_API_BASE_URL, postFormData, postJson } from "@/api/http";
import type {
    SancaiContentVersionRecord,
    SancaiEntryImageContentMode,
    SancaiEntryImageRecord,
    SancaiEntryLifecycleStatus,
    SancaiEntryRecord,
    SancaiRefinementBatchRecord,
    SancaiVisualAssetRecord
} from "./sancai-types";

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
    volumeId?: string | null;
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
    id: string;
    lifecycleStatus: SancaiEntryLifecycleStatus;
}

export interface SancaiEntrySortCommand {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC" | null;
}

interface SancaiVersionCommand {
    id: string;
    versionId?: string | null;
}

export interface SancaiEntryImageUploadCommand {
    currentUsed?: boolean;
    entryId: string;
    file: File;
    imageType?: string | null;
    replaceImageId?: string | null;
    title?: string | null;
}

export interface SancaiEntryImageContentUrlCommand {
    entryId: string;
    imageId: string;
    mode?: SancaiEntryImageContentMode;
}

export interface SancaiEntryImageMutationCommand {
    entryId: string;
    imageId: string;
}

export interface SancaiEntryImageSortCommand {
    entryId: string;
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC" | null;
}

export interface SancaiVisualAssetContentUrlCommand {
    entryId: string;
    mode?: SancaiEntryImageContentMode;
    visualAssetId: string;
    variant: "source" | "generated";
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

export interface SancaiVisualAssetUseCommand {
    entryId: string;
    visualAssetId: string;
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

export const deleteById = (id: string) => {
    return postJson<boolean, SancaiEntryCommand>(`${ENTRIES_PATH}/delete`, {
        body: { id }
    });
};

export const sort = (request: SancaiEntrySortCommand) => {
    return postJson<boolean, SancaiEntrySortCommand>(`${ENTRIES_PATH}/sort`, {
        body: request
    });
};

export const listImages = (entryId: string) => {
    return postJson<SancaiEntryImageRecord[], { entryId: string }>(`${ASSET_IMAGES_PATH}/list`, {
        body: { entryId }
    });
};

export const deleteImage = (command: SancaiEntryImageMutationCommand) => {
    return postJson<boolean, { entryId: string; id: string }>(`${ASSET_IMAGES_PATH}/delete`, {
        body: {
            entryId: command.entryId,
            id: command.imageId
        }
    });
};

export const changeCurrentImage = (command: SancaiEntryImageMutationCommand) => {
    return postJson<boolean, { entryId: string; id: string }>(
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

export const listVisualAssets = (entryId: string) => {
    return postJson<SancaiVisualAssetRecord[], { entryId: string }>(
        `${ASSET_VISUAL_ASSETS_PATH}/list`,
        {
            body: { entryId }
        }
    );
};

export const uploadImage = (command: SancaiEntryImageUploadCommand) => {
    const body = new FormData();
    body.append("entryId", String(command.entryId));
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
    return postFormData<SancaiEntryImageRecord>(`${ASSET_IMAGES_PATH}/upload`, body);
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

export const listVersions = (entryId: string) => {
    return postJson<SancaiContentVersionRecord[], SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/list`,
        {
            body: { id: entryId }
        }
    );
};

export const getVersion = (entryId: string, versionId: string) => {
    return postJson<SancaiContentVersionRecord, SancaiVersionCommand>(
        `${ENTRIES_PATH}/versions/get`,
        {
            body: { id: entryId, versionId }
        }
    );
};

export const resetVersion = (entryId: string, versionId: string) => {
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

export const getRefinementBatch = (batchId: string) => {
    return postJson<SancaiRefinementBatchRecord, { batchId: string }>(
        "/ai/refinement/task/batch/get",
        {
            body: { batchId }
        }
    );
};

export const cancelRefinementBatch = (batchId: string) => {
    return postJson<SancaiRefinementBatchRecord, { batchId: string }>(
        "/ai/refinement/task/batch/cancel",
        {
            body: { batchId }
        }
    );
};
