import type { ClassicsContentTagRecord } from "@/pages/classics/common/classics-content-types";
import type { DictItem } from "@/types/dict";

export interface SancaiCategoryRecord {
    id: string;
    title?: string | null;
    categoryType?: string | null;
    priority?: number | null;
}

export type SancaiCategoryTypeRecord = DictItem;

export interface SancaiVolumeRecord {
    id: string;
    categoryId?: string | null;
    title?: string | null;
    volumeType?: string | null;
    priority?: number | null;
}

export type SancaiVolumeTypeRecord = DictItem;

export type SancaiEntryLifecycleStatus = "DRAFT" | "PUBLISHED" | "OFFLINE" | "ERROR";
export type SancaiEntryTransitionStatus = "NONE" | "PUBLISHING" | "OFFLINING";

export interface SancaiEntryRecord {
    id: string;
    volumeId?: string | null;
    title?: string | null;
    originalText?: string | null;
    translationText?: string | null;
    summary?: string | null;
    lifecycleStatus?: SancaiEntryLifecycleStatus | string | null;
    transitionStatus?: SancaiEntryTransitionStatus | string | null;
    currentPublicationJobId?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
    priority?: number | null;
    currentVersionId?: string | null;
    currentVersionNo?: number | null;
    currentVersionedAt?: string | null;
    contentUpdatedAt?: string | null;
    versionDirty?: boolean;
    tags?: ClassicsContentTagRecord[];
}

export interface SancaiPublicationActionRecord {
    jobId: string;
    contentType: "SANCAI_ENTRY";
    contentId: string;
    lifecycleStatus: SancaiEntryLifecycleStatus;
    transitionStatus: SancaiEntryTransitionStatus;
}

export interface SancaiPublicationBatchItemRecord {
    contentId: string;
    accepted: boolean;
    jobId?: string | null;
    reason?: string | null;
}

export interface SancaiPublicationBatchRecord {
    acceptedCount: number;
    rejectedCount: number;
    items: SancaiPublicationBatchItemRecord[];
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

export interface SancaiRefinementBatchRecord {
    batchId: string;
    scope?: string | null;
    capability?: string | null;
    contentType?: string | null;
    status?: string | null;
    totalCount?: number | null;
    successCount?: number | null;
    failedCount?: number | null;
    cancelledCount?: number | null;
    failureSummaryJson?: string | null;
    requestedAt?: string | null;
    cancelledAt?: string | null;
    completedAt?: string | null;
}

export type SancaiEntryImageContentMode = "preview" | "download";

export interface SancaiContentVersionRecord {
    id: string;
    contentType?: string | null;
    contentId?: string | null;
    versionNo?: number | null;
    versionedAt?: string | null;
    snapshotJson?: string | null;
    changeType?: string | null;
    changeSummary?: string | null;
}

export interface SancaiVersionSnapshot {
    contentType?: string | null;
    contentId?: string | null;
    contentUpdatedAt?: string | null;
    volumeId?: string | null;
    title?: string | null;
    originalText?: string | null;
    translationText?: string | null;
    summary?: string | null;
    lifecycleStatus?: string | null;
    translationStatus?: string | null;
    imageStatus?: string | null;
    visualAssetStatus?: string | null;
    refinementStatus?: string | null;
    priority?: number | null;
}

export type SancaiExportStatus = "REQUESTED" | "RUNNING" | "COMPLETED" | "FAILED" | "EXPIRED";

export interface SancaiExportJobRecord {
    id?: string | null;
    contentType?: string | null;
    exportKind?: string | null;
    exportFormat?: string | null;
    scopeType?: string | null;
    scopeJson?: string | null;
    requestedAt?: string | null;
    expiresAt?: string | null;
    status?: SancaiExportStatus | null;
    storageObjectId?: string | null;
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
