import { CategoryTable } from "./category-table";
import type { TagCategoryPageQuery, TagCategoryStatusCommand } from "../taxonomy-service";
import type { TagCategoryRecord } from "../taxonomy-types";

interface TaxonomyCategorySectionProps {
    canEditTaxonomy: boolean;
    categories: TagCategoryRecord[];
    loading: boolean;
    query: TagCategoryPageQuery;
    totalCount: number;
    onAdd: () => void;
    onChange: (values: TagCategoryPageQuery) => void;
    onEdit: (category: TagCategoryRecord) => void;
    onRefresh: () => void;
    onStatusChange: (request: TagCategoryStatusCommand) => void;
}

export const TaxonomyCategorySection = ({
    canEditTaxonomy,
    categories,
    loading,
    query,
    totalCount,
    onAdd,
    onChange,
    onEdit,
    onRefresh,
    onStatusChange
}: TaxonomyCategorySectionProps) => (
    <CategoryTable
        canEditCategory={canEditTaxonomy}
        categories={categories}
        loading={loading}
        totalCount={totalCount}
        query={query}
        onAdd={onAdd}
        onChange={onChange}
        onEdit={onEdit}
        onRefresh={onRefresh}
        onStatusChange={onStatusChange}
    />
);
