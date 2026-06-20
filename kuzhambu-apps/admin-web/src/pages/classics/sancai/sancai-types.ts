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

export type SancaiCatalogNodeType = "category" | "root" | "volume";

export interface SancaiCatalogTreeNode {
    children?: SancaiCatalogTreeNode[];
    key: string;
    nodeType: SancaiCatalogNodeType;
    title: string;
}
