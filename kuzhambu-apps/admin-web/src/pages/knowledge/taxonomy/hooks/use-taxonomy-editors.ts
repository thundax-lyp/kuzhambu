import type { Key } from "react";
import { useState } from "react";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type {
    SynonymPageQuery,
    SynonymRecord,
    TagBatchMergePreviewRecord,
    TagCategoryPageQuery,
    TagCategoryRecord,
    TagExtractionResultRecord,
    TagMergePreviewRecord,
    TagPageQuery,
    TagRecord,
    TagReviewPageQuery
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
    const [categoryEditorOpen, setCategoryEditDrawerOpen] = useState(false);
    const [tagEditorOpen, setTagEditorOpen] = useState(false);
    const [synonymEditorOpen, setSynonymEditDrawerOpen] = useState(false);
    const [tagDetailOpen, setTagDetailOpen] = useState(false);
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
        categoryEditorOpen,
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
        setTagDetailOpen,
        setTagDetailReviewMode,
        setTagEditorOpen,
        setTagExtractionOpen,
        setTagExtractionResult,
        setTagMergePreview,
        setTagQuery,
        synonymEditorOpen,
        synonymQuery,
        tagBatchMergeOpen,
        tagBatchMergePreview,
        tagBatchReviewDecision,
        tagBatchReviewOpen,
        tagDetailOpen,
        tagDetailReviewMode,
        tagEditorOpen,
        tagExtractionOpen,
        tagExtractionResult,
        tagMergePreview,
        tagQuery
    };
};
