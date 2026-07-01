export interface SancaiCategoryRecord {
    id: number;
    title?: string | null;
    categoryType?: string | null;
    priority?: number | null;
}

export interface SancaiVolumeRecord {
    id: number;
    categoryId?: number | null;
    title?: string | null;
    volumeType?: string | null;
    priority?: number | null;
}

export interface SancaiEntryRecord {
    id: number;
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
    priority?: number | null;
    currentVersionId?: number | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    versionDirty?: boolean;
}

export interface SancaiEntryImageRecord {
    id: number;
    entryId?: number | null;
    storageObjectId?: number | null;
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

export interface SancaiVisualAssetRecord {
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
    sourcePreviewUrl?: string | null;
    sourceDownloadUrl?: string | null;
    generatedPreviewUrl?: string | null;
    generatedDownloadUrl?: string | null;
}

export type SancaiEntryImageContentMode = "preview" | "download";

export interface SancaiContentVersionRecord {
    id: number;
    contentType?: string | null;
    contentId?: number | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export type SancaiShowcaseStatus = "REQUESTED" | "PROCESSING" | "COMPLETED" | "FAILED" | "EXPIRED";

export interface SancaiShowcaseRecord {
    id?: number | null;
    requestedAt?: string | null;
    status?: SancaiShowcaseStatus | null;
    scopeJson?: string | null;
    storageObjectId?: number | null;
    entryCount?: number | null;
    visibilityRiskStatus?: string | null;
    contentUrl?: string | null;
    downloadUrl?: string | null;
}

export interface SancaiVersionSnapshot {
    contentType?: string | null;
    contentId?: number | null;
    contentUpdatedAt?: string | null;
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
    priority?: number | null;
}

export type SancaiExportStatus = "REQUESTED" | "RUNNING" | "COMPLETED" | "FAILED" | "EXPIRED";

export interface SancaiExportJobRecord {
    id?: number | null;
    contentType?: string | null;
    exportKind?: string | null;
    exportFormat?: string | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    requestedAt?: string | null;
    expiresAt?: string | null;
    status?: SancaiExportStatus | null;
    storageObjectId?: number | null;
    itemCount?: number | null;
    assetCount?: number | null;
    visibilityRiskStatus?: string | null;
    contentChanged?: boolean | null;
    contentUrl?: string | null;
    downloadUrl?: string | null;
}

export type SancaiCatalogNodeType = "category" | "root" | "volume";

export interface SancaiCatalogTreeNode {
    children?: SancaiCatalogTreeNode[];
    key: string;
    nodeType: SancaiCatalogNodeType;
    title: string;
}
