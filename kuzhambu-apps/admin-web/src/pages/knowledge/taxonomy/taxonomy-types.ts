export interface TagCategoryRecord {
    id: string;
    name: string;
    description?: string | null;
    priority?: number | null;
    status?: string | null;
}

export interface TagRecord {
    id: string;
    name: string;
    categoryId?: string | null;
    categoryName?: string | null;
    description?: string | null;
    status?: string | null;
    source?: string | null;
    reviewStatus?: string | null;
    contentRefCount?: number | null;
    createdAt?: number | null;
    reviewedAt?: number | null;
}

export interface TagDetailRecord {
    tag?: TagRecord | null;
    aliases?: TagAliasRecord[] | null;
    contentRefs?: TagContentRefRecord[] | null;
}

export interface TagMergePreviewRecord {
    sourceTag?: TagRecord | null;
    targetTag?: TagRecord | null;
    aliasesToMerge?: TagAliasRecord[] | null;
    impactedContentRefs?: TagContentRefRecord[] | null;
    pendingReviewCount?: number | null;
    governedRecordCount?: number | null;
}

export interface TagBatchMergePreviewRecord {
    sourceTags?: TagRecord[] | null;
    targetTag?: TagRecord | null;
    aliasesToMerge?: TagAliasRecord[] | null;
    impactedContentRefs?: TagContentRefRecord[] | null;
    pendingReviewCount?: number | null;
    governedRecordCount?: number | null;
}

export interface TagGovernanceMetricsRecord {
    topTags?: TagUsageMetricRecord[] | null;
    categoryDistributions?: CategoryDistributionMetricRecord[] | null;
    sourceRatios?: SourceRatioMetricRecord[] | null;
    monthlyNewTags?: MonthlyNewTagMetricRecord[] | null;
}

export interface TagExtractionCandidateRecord {
    name: string;
    categoryId?: string | null;
    categoryName?: string | null;
    confidence?: number | null;
    reason?: string | null;
    matchedExistingTagId?: string | null;
}

export interface TagExtractionResultRecord {
    aiCallId?: number | null;
    aiCandidateId?: number | null;
    status?: string | null;
    resultFormat?: string | null;
    resultPayload?: string | null;
    errorType?: string | null;
    errorMessage?: string | null;
    candidates?: TagExtractionCandidateRecord[] | null;
}

export interface TagUsageMetricRecord {
    tagName?: string | null;
    contentRefCount?: number | null;
}

export interface CategoryDistributionMetricRecord {
    categoryName?: string | null;
    tagCount?: number | null;
}

export interface SourceRatioMetricRecord {
    source?: string | null;
    tagCount?: number | null;
}

export interface MonthlyNewTagMetricRecord {
    month?: string | null;
    tagCount?: number | null;
}

export interface TagAliasRecord {
    id: string;
    name: string;
    source?: string | null;
}

export interface TagContentRefRecord {
    id: string;
    contentType?: string | null;
    contentId?: string | null;
    contentTitle?: string | null;
    source?: string | null;
}

export interface SynonymRecord {
    id: string;
    term?: string | null;
    synonym?: string | null;
    status?: string | null;
}
