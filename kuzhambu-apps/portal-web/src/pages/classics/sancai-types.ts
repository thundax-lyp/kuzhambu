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
    contentUpdatedAt?: string | null;
}

export interface SancaiEntryPage {
    pageNo?: number | null;
    pageSize?: number | null;
    totalCount?: number | null;
    totalPage?: number | null;
    records?: SancaiEntryRecord[] | null;
}

export interface SancaiEntryQuery {
    categoryId?: number | null;
    volumeId?: number | null;
    keyword?: string | null;
    pageNo?: number | null;
    pageSize?: number | null;
}
