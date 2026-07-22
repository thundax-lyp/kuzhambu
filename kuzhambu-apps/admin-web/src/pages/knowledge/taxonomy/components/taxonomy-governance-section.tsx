import type { Key } from "react";
import { TagReviewTable } from "./tag-review-table";
import type { TagRecord, TagReviewPageQuery } from "../taxonomy-types";

interface TaxonomyGovernanceSectionProps {
    loading: boolean;
    query: TagReviewPageQuery;
    selectedRowKeys: Key[];
    tags: TagRecord[];
    totalCount: number;
    onBatchApprove: () => void;
    onBatchReject: () => void;
    onChange: (values: TagReviewPageQuery) => void;
    onOpenReview: (tag: TagRecord) => void;
    onRefresh: () => void;
    onSelectedRowKeysChange: (keys: Key[]) => void;
}

export const TaxonomyGovernanceSection = ({
    loading,
    query,
    selectedRowKeys,
    tags,
    totalCount,
    onBatchApprove,
    onBatchReject,
    onChange,
    onOpenReview,
    onRefresh,
    onSelectedRowKeysChange
}: TaxonomyGovernanceSectionProps) => (
    <TagReviewTable
        loading={loading}
        query={query}
        selectedRowKeys={selectedRowKeys}
        tags={tags}
        totalCount={totalCount}
        onBatchApprove={onBatchApprove}
        onBatchReject={onBatchReject}
        onChange={onChange}
        onOpenReview={onOpenReview}
        onRefresh={onRefresh}
        onSelectedRowKeysChange={onSelectedRowKeysChange}
    />
);
