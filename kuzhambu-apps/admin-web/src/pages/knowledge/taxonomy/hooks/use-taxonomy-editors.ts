import type { Key } from "react";
import { useState } from "react";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type {
    SynonymPageQuery,
    TagCategoryPageQuery,
    TagPageQuery,
    TagReviewPageQuery
} from "../taxonomy-service";
import type {
    SynonymRecord,
    TagBatchMergePreviewRecord,
    TagCategoryRecord,
    TagExtractionResultRecord,
    TagMergePreviewRecord,
    TagRecord
} from "../taxonomy-types";

export const useTaxonomyEditors = () => {
    const [activeTabKey, setActiveTabKey] = useState("categories");
    const [categoryQuery, setCategoryQuery] = useState<TagCategoryPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [tagQuery, setTagQuery] = useState<TagPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [reviewQuery, setReviewQuery] = useState<TagReviewPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [synonymQuery, setSynonymQuery] = useState<SynonymPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [editingCategory, setEditingCategory] = useState<TagCategoryRecord | null>(null);
    const [editingTag, setEditingTag] = useState<TagRecord | null>(null);
    const [editingSynonym, setEditingSynonym] = useState<SynonymRecord | null>(null);
    const [selectedTag, setSelectedTag] = useState<TagRecord | null>(null);
    const [categoryEditDrawerOpen, setCategoryEditDrawerOpen] = useState(false);
    const [tagEditDrawerOpen, setTagEditDrawerOpen] = useState(false);
    const [synonymEditDrawerOpen, setSynonymEditDrawerOpen] = useState(false);
    const [tagDetailDrawerOpen, setTagDetailDrawerOpen] = useState(false);
    const [tagDetailReviewMode, setTagDetailReviewMode] = useState(false);
    const [removingAliasId, setRemovingAliasId] = useState<string | null>(null);
    const [tagMergePreview, setTagMergePreview] = useState<TagMergePreviewRecord | null>(null);
    const [tagBatchMergeOpen, setTagBatchMergeOpen] = useState(false);
    const [selectedTagRowKeys, setSelectedTagRowKeys] = useState<Key[]>([]);
    const [tagBatchMergePreview, setTagBatchMergePreview] =
        useState<TagBatchMergePreviewRecord | null>(null);
    const [tagBatchReviewOpen, setTagBatchReviewOpen] = useState(false);
    const [tagBatchReviewDecision, setTagBatchReviewDecision] = useState<"APPROVE" | "REJECT">(
        "APPROVE"
    );
    const [selectedReviewRowKeys, setSelectedReviewRowKeys] = useState<Key[]>([]);
    const [tagExtractionOpen, setTagExtractionOpen] = useState(false);
    const [tagExtractionResult, setTagExtractionResult] =
        useState<TagExtractionResultRecord | null>(null);

    return {
        activeTabKey,
        categoryEditDrawerOpen,
        categoryQuery,
        editingCategory,
        editingSynonym,
        editingTag,
        removingAliasId,
        reviewQuery,
        selectedReviewRowKeys,
        selectedTag,
        selectedTagRowKeys,
        setActiveTabKey,
        setCategoryEditDrawerOpen,
        setCategoryQuery,
        setEditingCategory,
        setEditingSynonym,
        setEditingTag,
        setRemovingAliasId,
        setReviewQuery,
        setSelectedReviewRowKeys,
        setSelectedTag,
        setSelectedTagRowKeys,
        setSynonymEditDrawerOpen,
        setSynonymQuery,
        setTagBatchMergeOpen,
        setTagBatchMergePreview,
        setTagBatchReviewDecision,
        setTagBatchReviewOpen,
        setTagDetailDrawerOpen,
        setTagDetailReviewMode,
        setTagEditDrawerOpen,
        setTagExtractionOpen,
        setTagExtractionResult,
        setTagMergePreview,
        setTagQuery,
        synonymEditDrawerOpen,
        synonymQuery,
        tagBatchMergeOpen,
        tagBatchMergePreview,
        tagBatchReviewDecision,
        tagBatchReviewOpen,
        tagDetailDrawerOpen,
        tagDetailReviewMode,
        tagEditDrawerOpen,
        tagExtractionOpen,
        tagExtractionResult,
        tagMergePreview,
        tagQuery
    };
};
