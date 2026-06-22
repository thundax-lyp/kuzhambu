/* eslint-disable local/service-input-type-location */

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

export interface TagCategoryPageQuery {
    pageNo?: number;
    pageSize?: number;
    name?: string | null;
    status?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface TagPageQuery {
    pageNo?: number;
    pageSize?: number;
    name?: string | null;
    categoryId?: string | null;
    status?: string | null;
    source?: string | null;
    reviewStatus?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface TagReviewPageQuery {
    pageNo?: number;
    pageSize?: number;
    name?: string | null;
    source?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}

export interface SynonymPageQuery {
    pageNo?: number;
    pageSize?: number;
    term?: string | null;
    synonym?: string | null;
    status?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}
