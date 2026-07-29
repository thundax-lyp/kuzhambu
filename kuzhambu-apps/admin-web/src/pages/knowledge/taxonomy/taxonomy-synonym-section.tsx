import { SynonymTable } from "./synonym-table";
import type {
    SynonymPageQuery,
    SynonymRemoveCommand,
    SynonymStatusCommand
} from "./taxonomy-service";
import type { SynonymRecord } from "./taxonomy-types";

interface TaxonomySynonymSectionProps {
    canEditTaxonomy: boolean;
    loading: boolean;
    query: SynonymPageQuery;
    removing: boolean;
    synonyms: SynonymRecord[];
    totalCount: number;
    onAdd: () => void;
    onChange: (values: SynonymPageQuery) => void;
    onEdit: (record: SynonymRecord) => void;
    onRefresh: () => void;
    onRemove: (request: SynonymRemoveCommand) => void;
    onStatusChange: (request: SynonymStatusCommand) => void;
}

export const TaxonomySynonymSection = ({
    canEditTaxonomy,
    loading,
    query,
    removing,
    synonyms,
    totalCount,
    onAdd,
    onChange,
    onEdit,
    onRefresh,
    onRemove,
    onStatusChange
}: TaxonomySynonymSectionProps) => (
    <SynonymTable
        canEditSynonym={canEditTaxonomy}
        loading={loading}
        query={query}
        removing={removing}
        synonyms={synonyms}
        totalCount={totalCount}
        onAdd={onAdd}
        onChange={onChange}
        onEdit={onEdit}
        onRefresh={onRefresh}
        onRemove={onRemove}
        onStatusChange={onStatusChange}
    />
);
