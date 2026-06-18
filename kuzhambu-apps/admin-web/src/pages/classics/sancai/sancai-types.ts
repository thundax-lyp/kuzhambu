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
}

export interface SancaiContentRecord {
    id: number;
    entryId?: number | null;
    question?: string | null;
    answer?: string | null;
}

export type SancaiCatalogNodeType = "category" | "entry" | "volume";

export interface SancaiCatalogTreeNode {
    children?: SancaiCatalogTreeNode[];
    key: string;
    nodeType: SancaiCatalogNodeType;
    title: string;
}
