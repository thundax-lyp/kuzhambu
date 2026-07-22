export interface SancaiCategoryRecord {
    id: number;
    title?: string | null;
    categoryType?: string | null;
    priority?: number | null;
    publicEntryCount?: number | null;
    illustratedEntryCount?: number | null;
    thumbnailUrl?: string | null;
    thumbnailTitle?: string | null;
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
    author?: string | null;
    authorName?: string | null;
    sourceTitle?: string | null;
    bodyHtml?: string | null;
    bodyMarkdown?: string | null;
    summaryHtml?: string | null;
    summaryMarkdown?: string | null;
    originalHtml?: string | null;
    originalMarkdown?: string | null;
    translationHtml?: string | null;
    translationMarkdown?: string | null;
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
    tags?: SancaiEntryTagRecord[] | null;
    images?: SancaiEntryImageRecord[] | null;
    currentVisualAsset?: SancaiVisualAssetRecord | null;
}

export interface SancaiEntryTagRecord {
    id?: number | null;
    tagId?: number | null;
    tagName?: string | null;
    source?: string | null;
    priority?: number | null;
}

export interface SancaiEntryImageRecord {
    id?: number | null;
    title?: string | null;
    imageType?: string | null;
    currentUsed?: boolean | null;
    priority?: number | null;
    previewUrl?: string | null;
    downloadUrl?: string | null;
}

export interface SancaiVisualAssetRecord {
    visualAssetId?: number | null;
    versionNo?: number | null;
    status?: string | null;
    imageAnalysisMarkdown?: string | null;
    fusionDescription?: string | null;
    visualDescription?: string | null;
    sourcePreviewUrl?: string | null;
    generatedPreviewUrl?: string | null;
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
