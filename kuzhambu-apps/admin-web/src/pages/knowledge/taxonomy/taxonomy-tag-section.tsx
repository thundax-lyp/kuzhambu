import type { Key } from "react";
import { KuzhambuButton } from "@/components";
import { TagGovernanceMetricsPanel } from "./tag-governance-metrics-panel";
import { TagMergePanel } from "./tag-merge-panel";
import { TagTable } from "./tag-table";
import type { TagMergeCommand, TagPageQuery, TagStatusCommand } from "./taxonomy-service";
import type {
    TagGovernanceMetricsRecord,
    TagMergePreviewRecord,
    TagRecord
} from "./taxonomy-types";

interface TaxonomyTagSectionProps {
    applyingMerge: boolean;
    canEditTaxonomy: boolean;
    loading: boolean;
    metrics?: TagGovernanceMetricsRecord | null;
    metricsLoading: boolean;
    preview?: TagMergePreviewRecord | null;
    previewingMerge: boolean;
    query: TagPageQuery;
    selectedRowKeys: Key[];
    tags: TagRecord[];
    totalCount: number;
    onAdd: () => void;
    onBatchDeprecate: () => void;
    onBatchMerge: () => void;
    onChange: (values: TagPageQuery) => void;
    onEdit: (tag: TagRecord) => void;
    onExtract: () => void;
    onOpenDetail: (tag: TagRecord) => void;
    onRefresh: () => void;
    onRefreshMetrics: () => void;
    onSelectedRowKeysChange: (keys: Key[]) => void;
    onStatusChange: (request: TagStatusCommand) => void;
    onApplyMerge: (request: TagMergeCommand) => void;
    onPreviewMerge: (request: TagMergeCommand) => void;
}

export const TaxonomyTagSection = ({
    applyingMerge,
    canEditTaxonomy,
    loading,
    metrics,
    metricsLoading,
    preview,
    previewingMerge,
    query,
    selectedRowKeys,
    tags,
    totalCount,
    onAdd,
    onBatchDeprecate,
    onBatchMerge,
    onChange,
    onEdit,
    onExtract,
    onOpenDetail,
    onRefresh,
    onRefreshMetrics,
    onSelectedRowKeysChange,
    onStatusChange,
    onApplyMerge,
    onPreviewMerge
}: TaxonomyTagSectionProps) => (
    <div className="knowledge-taxonomy-tag-governance">
        <TagMergePanel
            applying={applyingMerge}
            canEditTag={canEditTaxonomy}
            preview={preview}
            previewing={previewingMerge}
            tags={tags}
            onApply={onApplyMerge}
            onPreview={onPreviewMerge}
        />
        <TagGovernanceMetricsPanel
            loading={metricsLoading}
            metrics={metrics}
            onRefresh={onRefreshMetrics}
        />
        <TagTable
            canEditTag={canEditTaxonomy}
            loading={loading}
            query={query}
            selectedRowKeys={selectedRowKeys}
            tags={tags}
            totalCount={totalCount}
            onAdd={onAdd}
            onBatchDeprecate={onBatchDeprecate}
            onBatchMerge={onBatchMerge}
            onChange={onChange}
            onEdit={onEdit}
            onOpenDetail={onOpenDetail}
            onRefresh={onRefresh}
            onSelectedRowKeysChange={onSelectedRowKeysChange}
            pageActions={
                canEditTaxonomy ? (
                    <KuzhambuButton
                        testId="knowledge-taxonomy-taxonomy-ai-button"
                        type="primary"
                        onClick={onExtract}
                    >
                        AI 抽取标签
                    </KuzhambuButton>
                ) : null
            }
            onStatusChange={onStatusChange}
        />
    </div>
);
