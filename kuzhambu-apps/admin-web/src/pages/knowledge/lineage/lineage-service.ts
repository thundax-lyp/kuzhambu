import { postJson } from "@/api/http";
import type { LineageCanvasRecord } from "./lineage-types";

const API_PREFIX = "/knowledge/lineage";

export interface LineageCanvasQuery {
    versionId?: number | null;
    focusNodeId?: number | null;
    focusRelationId?: number | null;
    keyword?: string | null;
    nodeType?: string | null;
    relationType?: string | null;
    confirmationStatus?: string | null;
    depth?: number;
}

export const getLineageCanvas = (request: LineageCanvasQuery = {}) => {
    return postJson<LineageCanvasRecord, LineageCanvasQuery>(`${API_PREFIX}/canvas`, {
        body: request
    });
};
