import { getJson } from "@/api/http";
import type { KnowledgeLineageCanvasRecord } from "./knowledge-lineage-types";

export interface KnowledgeLineageCanvasQuery {
    versionId?: number | null;
    focusNodeId?: number | null;
    focusRelationId?: number | null;
    keyword?: string | null;
    nodeType?: string | null;
    relationType?: string | null;
    confirmationStatus?: string | null;
    depth?: number;
}

export const getKnowledgeLineage = (query: KnowledgeLineageCanvasQuery = {}) => {
    const params: Record<string, string | number | null | undefined> = { ...query };
    return getJson<KnowledgeLineageCanvasRecord>("/portal/knowledge/lineage", params);
};
