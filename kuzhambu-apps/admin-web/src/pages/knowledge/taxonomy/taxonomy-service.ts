import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    SynonymRecord,
    TagAliasRecord,
    TagBatchMergePreviewRecord,
    TagCategoryRecord,
    TagDetailRecord,
    TagExtractionCandidateRecord,
    TagExtractionResultRecord,
    TagGovernanceMetricsRecord,
    TagMergePreviewRecord,
    TagRecord
} from "./taxonomy-types";

const API_PREFIX = "/knowledge/taxonomy";

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

export interface TagCategoryCreateCommand {
    id: string;
    name: string;
    description?: string | null;
    priority: number;
    status?: string | null;
}

export interface TagCategoryUpdateCommand {
    id: string;
    name: string;
    description?: string | null;
    priority: number;
}

export interface TagCategoryStatusCommand {
    id: string;
    status: string;
}

export interface TagCreateCommand {
    id: string;
    name: string;
    categoryId?: string | null;
    description?: string | null;
    reviewStatus?: string | null;
    reviewNote?: string | null;
    reviewedAt?: number | null;
}

export interface TagUpdateCommand {
    id: string;
    name: string;
    categoryId?: string | null;
    description?: string | null;
}

export interface TagStatusCommand {
    id: string;
    status: string;
}

export interface TagMergeCommand {
    sourceTagId: string;
    targetTagId: string;
}

export interface TagBatchMergeCommand {
    sourceTagIds: string[];
    targetTagId: string;
}

export interface TagDeprecateCommand {
    id: string;
}

export interface TagBatchDeprecateCommand {
    tagIds: string[];
}

export interface TagIdCommand {
    tagId: string;
}

export interface TagGovernanceMetricsQuery {
    topLimit?: number;
    recentMonths?: number;
}

export interface TagReviewCommand {
    id: string;
    decision: "APPROVE" | "REJECT";
    reviewNote?: string | null;
}

export interface TagBatchReviewCommand {
    tagIds: string[];
    decision: "APPROVE" | "REJECT";
    categoryId?: string | null;
    reviewNote?: string | null;
}

export interface TagExtractionCommand {
    sourceContentType: string;
    sourceContentId: string;
    contentTitle?: string | null;
    contentText: string;
    modelId: number;
    modelName: string;
    promptVersionId?: number | null;
    maxTags?: number | null;
    allowNewTags?: boolean | null;
}

export interface TagCandidateApplyCommand {
    aiCandidateId: number;
    selectedTags: TagExtractionCandidateRecord[];
    reviewNote?: string | null;
    reviewedBy?: number | null;
}

export interface TagAliasCreateCommand {
    id: string;
    tagId: string;
    name: string;
    source?: string | null;
}

export interface TagAliasRemoveCommand {
    id: string;
}

export interface TagAliasListCommand {
    tagId: string;
}

export interface SynonymCreateCommand {
    id: string;
    term: string;
    synonym: string;
    status?: string | null;
}

export interface SynonymUpdateCommand {
    id: string;
    term: string;
    synonym: string;
}

export interface SynonymStatusCommand {
    id: string;
    status: string;
}

export interface SynonymRemoveCommand {
    id: string;
}

export const pageCategories = (request: TagCategoryPageQuery = {}) => {
    return postJson<Page<TagCategoryRecord>, TagCategoryPageQuery>(`${API_PREFIX}/category/page`, {
        body: request
    });
};

export const createCategory = (request: TagCategoryCreateCommand) => {
    return postJson<boolean, TagCategoryCreateCommand>(`${API_PREFIX}/category/create`, {
        body: request
    });
};

export const updateCategory = (request: TagCategoryUpdateCommand) => {
    return postJson<boolean, TagCategoryUpdateCommand>(`${API_PREFIX}/category/update`, {
        body: request
    });
};

export const changeCategoryStatus = (request: TagCategoryStatusCommand) => {
    return postJson<boolean, TagCategoryStatusCommand>(`${API_PREFIX}/category/status`, {
        body: request
    });
};

export const pageTags = (request: TagPageQuery = {}) => {
    return postJson<Page<TagRecord>, TagPageQuery>(`${API_PREFIX}/tag/page`, {
        body: request
    });
};

export const getTagDetail = (request: TagIdCommand) => {
    return postJson<TagDetailRecord, TagIdCommand>(`${API_PREFIX}/tag/detail`, {
        body: request
    });
};

export const createTag = (request: TagCreateCommand) => {
    return postJson<boolean, TagCreateCommand>(`${API_PREFIX}/tag/create`, {
        body: request
    });
};

export const updateTag = (request: TagUpdateCommand) => {
    return postJson<boolean, TagUpdateCommand>(`${API_PREFIX}/tag/update`, {
        body: request
    });
};

export const changeTagStatus = (request: TagStatusCommand) => {
    return postJson<boolean, TagStatusCommand>(`${API_PREFIX}/tag/status`, {
        body: request
    });
};

export const previewTagMergeImpact = (request: TagMergeCommand) => {
    return postJson<TagMergePreviewRecord, TagMergeCommand>(`${API_PREFIX}/tag/merge/preview`, {
        body: request
    });
};

export const applyTagMerge = (request: TagMergeCommand) => {
    return postJson<boolean, TagMergeCommand>(`${API_PREFIX}/tag/merge/apply`, {
        body: request
    });
};

export const previewTagBatchMergeImpact = (request: TagBatchMergeCommand) => {
    return postJson<TagBatchMergePreviewRecord, TagBatchMergeCommand>(
        `${API_PREFIX}/tag/merge/batch-preview`,
        {
            body: request
        }
    );
};

export const applyTagBatchMerge = (request: TagBatchMergeCommand) => {
    return postJson<boolean, TagBatchMergeCommand>(`${API_PREFIX}/tag/merge/batch-apply`, {
        body: request
    });
};

export const deprecateTag = (request: TagDeprecateCommand) => {
    return postJson<boolean, TagDeprecateCommand>(`${API_PREFIX}/tag/deprecate`, {
        body: request
    });
};

export const deprecateBatchTags = (request: TagBatchDeprecateCommand) => {
    return postJson<boolean, TagBatchDeprecateCommand>(`${API_PREFIX}/tag/deprecate/batch`, {
        body: request
    });
};

export const pagePendingTags = (request: TagReviewPageQuery = {}) => {
    return postJson<Page<TagRecord>, TagReviewPageQuery>(`${API_PREFIX}/tag/review/page`, {
        body: request
    });
};

export const reviewTag = (request: TagReviewCommand) => {
    return postJson<boolean, TagReviewCommand>(`${API_PREFIX}/tag/review`, {
        body: request
    });
};

export const reviewBatchTags = (request: TagBatchReviewCommand) => {
    return postJson<boolean, TagBatchReviewCommand>(`${API_PREFIX}/tag/review/batch`, {
        body: request
    });
};

export const requestTagExtraction = async (request: TagExtractionCommand) => {
    const result = await postJson<TagExtractionResultRecord, TagExtractionCommand>(
        `${API_PREFIX}/tag/extract`,
        {
            body: request
        }
    );
    return parseTagExtractionResult(result);
};

export const applyExtractedTags = (request: TagCandidateApplyCommand) => {
    return postJson<boolean, TagCandidateApplyCommand>(`${API_PREFIX}/tag/extract/apply`, {
        body: request
    });
};

export const getTagGovernanceMetrics = (request: TagGovernanceMetricsQuery = {}) => {
    return postJson<TagGovernanceMetricsRecord, TagGovernanceMetricsQuery>(
        `${API_PREFIX}/tag/metrics`,
        {
            body: request
        }
    );
};

export const listTagAliases = (request: TagAliasListCommand) => {
    return postJson<TagAliasRecord[], TagAliasListCommand>(`${API_PREFIX}/tag/alias/list`, {
        body: request
    });
};

export const createTagAlias = (request: TagAliasCreateCommand) => {
    return postJson<boolean, TagAliasCreateCommand>(`${API_PREFIX}/tag/alias/create`, {
        body: request
    });
};

export const removeTagAlias = (request: TagAliasRemoveCommand) => {
    return postJson<boolean, TagAliasRemoveCommand>(`${API_PREFIX}/tag/alias/remove`, {
        body: request
    });
};

export const pageSynonyms = (request: SynonymPageQuery = {}) => {
    return postJson<Page<SynonymRecord>, SynonymPageQuery>(`${API_PREFIX}/synonym/page`, {
        body: request
    });
};

export const createSynonym = (request: SynonymCreateCommand) => {
    return postJson<boolean, SynonymCreateCommand>(`${API_PREFIX}/synonym/create`, {
        body: request
    });
};

export const updateSynonym = (request: SynonymUpdateCommand) => {
    return postJson<boolean, SynonymUpdateCommand>(`${API_PREFIX}/synonym/update`, {
        body: request
    });
};

export const changeSynonymStatus = (request: SynonymStatusCommand) => {
    return postJson<boolean, SynonymStatusCommand>(`${API_PREFIX}/synonym/status`, {
        body: request
    });
};

export const removeSynonym = (request: SynonymRemoveCommand) => {
    return postJson<boolean, SynonymRemoveCommand>(`${API_PREFIX}/synonym/remove`, {
        body: request
    });
};

const parseTagExtractionResult = (result: TagExtractionResultRecord): TagExtractionResultRecord => {
    if (!result?.resultPayload) {
        return result;
    }
    try {
        const payload = JSON.parse(result.resultPayload) as {
            tags?: TagExtractionCandidateRecord[];
        };
        return {
            ...result,
            candidates: Array.isArray(payload.tags) ? payload.tags : []
        };
    } catch {
        return {
            ...result,
            candidates: [],
            errorMessage: result.errorMessage ?? "AI 标签候选解析失败"
        };
    }
};
