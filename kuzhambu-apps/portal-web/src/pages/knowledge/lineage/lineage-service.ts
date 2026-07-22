import { postJson } from "@/api/http";
import type { KnowledgeLineageCanvasRecord } from "./lineage-types";

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
    return postJson<KnowledgeLineageCanvasRecord>("/portal/knowledge/lineage/get", { ...query });
};
