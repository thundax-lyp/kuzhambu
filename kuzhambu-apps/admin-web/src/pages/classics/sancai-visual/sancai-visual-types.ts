export interface SancaiEntryRecord {
    id: string;
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
    priority?: number | null;
}

export interface SancaiEntryImageRecord {
    id: string;
    entryId?: string | null;
    storageObjectId?: string | null;
    imageType?: string | null;
    title?: string | null;
    currentUsed?: boolean | null;
    priority?: number | null;
    originalFilename?: string | null;
    contentType?: string | null;
    size?: number | null;
    previewUrl?: string | null;
    downloadUrl?: string | null;
}

export type SancaiEntryImageContentMode = "preview" | "download";

export interface SancaiVisualAssetRecord {
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
    sourcePreviewUrl?: string | null;
    sourceDownloadUrl?: string | null;
    generatedPreviewUrl?: string | null;
    generatedDownloadUrl?: string | null;
}
