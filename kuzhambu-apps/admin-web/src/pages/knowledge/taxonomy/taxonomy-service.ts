import { postJson } from "@/api/http";
import { normalizeId } from "@/types/id";
import type { Page } from "@/types/page";
import type {
    TagAliasRecord,
    TagBatchMergePreviewRecord,
    TagCategoryRecord,
    TagDetailRecord,
    TagExtractionCandidateRecord,
    TagExtractionPromptTemplateRecord,
    TagExtractionPromptVersionRecord,
    TagExtractionResultRecord,
    TagGovernanceMetricsRecord,
    TagMergePreviewRecord,
    TagRecord
} from "./taxonomy-types";

const API_PREFIX = "/knowledge/taxonomy";
const TAG_EXTRACTION_CAPABILITY = "KNOWLEDGE_TAG_EXTRACT";

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
    modelId: string;
    modelName: string;
    promptVersionId?: string | null;
    maxTags?: number | null;
    allowNewTags?: boolean | null;
}

export interface TagCandidateApplyCommand {
    aiCandidateId: string;
    selectedTags: TagExtractionCandidateRecord[];
    reviewNote?: string | null;
    reviewedBy?: string | null;
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

export const listTagExtractionPromptVersions = async () => {
    const templates = await postJson<
        TagExtractionPromptTemplateRecord[],
        { capability: string; enabled: boolean }
    >("/ai/config/prompt/template/list", {
        body: { capability: TAG_EXTRACTION_CAPABILITY, enabled: true }
    });
    const versionGroups = await Promise.all(
        (templates || [])
            .map((template) => ({
                ...template,
                id: normalizeId(template.id)
            }))
            .filter((template) => template.id)
            .map(async (template) => {
                const templateId = normalizeId(template.id);
                const versions = await postJson<TagExtractionPromptVersionRecord[], { id: string }>(
                    "/ai/config/prompt/version/list",
                    {
                        body: { id: templateId }
                    }
                );
                return (versions || []).map((version) => ({
                    ...version,
                    id: normalizeId(version.id),
                    templateId: normalizeId(version.templateId || templateId),
                    capability: template.capability || TAG_EXTRACTION_CAPABILITY,
                    templateName: template.name || template.capability || templateId
                }));
            })
    );
    return versionGroups
        .flat()
        .filter((version) => version.id)
        .sort((left, right) => {
            const templateCompare = String(left.templateName || "").localeCompare(
                String(right.templateName || "")
            );
            if (templateCompare !== 0) {
                return templateCompare;
            }
            return (right.versionNo || 0) - (left.versionNo || 0);
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
