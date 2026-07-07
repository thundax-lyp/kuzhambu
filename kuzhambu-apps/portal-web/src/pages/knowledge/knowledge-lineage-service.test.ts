import { afterEach, describe, expect, it, vi } from "vitest";
import * as http from "@/api/http";
import * as service from "./knowledge-lineage-service";
import type { KnowledgeLineageCanvasQuery } from "./knowledge-lineage-service";
import type { KnowledgeLineageCanvasRecord } from "./knowledge-lineage-types";

const canvas: KnowledgeLineageCanvasRecord = {
    version: {
        versionId: 71,
        versionNo: 3,
        taskType: "LINEAGE",
        status: "APPLIED"
    },
    summary: {
        nodeCount: 2,
        relationCount: 1,
        confirmedNodeCount: 1,
        confirmedRelationCount: 1
    },
    nodes: [],
    relations: [],
    availableFilters: {
        versions: [],
        nodeTypes: [],
        relationTypes: [],
        confirmationStatuses: []
    }
};

describe("knowledge lineage portal service", () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("forwards lineage query parameters to portal lineage api", async () => {
        const query: KnowledgeLineageCanvasQuery = {
            versionId: 71,
            focusNodeId: 301,
            focusRelationId: null,
            keyword: "贾宝玉",
            nodeType: "PERSON",
            relationType: "PARENT_CHILD",
            confirmationStatus: "CONFIRMED",
            depth: 2
        };
        const getJsonSpy = vi.spyOn(http, "getJson").mockResolvedValue(canvas);

        const result = await service.getKnowledgeLineage(query);

        expect(result.version?.versionId).toBe(71);
        expect(getJsonSpy).toHaveBeenCalledWith("/portal/knowledge/lineage", query);
    });
});
