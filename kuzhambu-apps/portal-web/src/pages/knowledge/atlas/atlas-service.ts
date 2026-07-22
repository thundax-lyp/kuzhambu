import { postJson } from "@/api/http";
import { buildKnowledgeAtlasFallback, KNOWLEDGE_ATLAS_FALLBACK } from "./atlas-fallback";
import type { KnowledgeAtlasQuery, KnowledgeAtlasResponse } from "./atlas-types";

export { KNOWLEDGE_ATLAS_FALLBACK };

export const getKnowledgeAtlas = async (query?: KnowledgeAtlasQuery) => {
    try {
        return await postJson<KnowledgeAtlasResponse>("/portal/knowledge/atlas/get", {
            categoryCode: query?.categoryCode,
            entityId: query?.entityId,
            keyword: query?.keyword,
            knowledgeBase: query?.knowledgeBase,
            level: query?.level,
            tag: query?.tag,
            timeRange: query?.timeRange
        });
    } catch {
        return buildKnowledgeAtlasFallback(query);
    }
};
