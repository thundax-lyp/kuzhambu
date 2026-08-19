import { postJson } from "@/api/http";
import type {
    GraphWorkbenchGraphRecord,
    GraphWorkbenchOneHopEdgesRecord,
    GraphWorkbenchOverviewRecord
} from "./graph-workbench-types";

const API_PREFIX = "/knowledge/graph";

export interface GraphWorkbenchRequestQuery {
    signal?: AbortSignal;
}
export interface GraphOneHopEdgesQuery extends GraphWorkbenchRequestQuery {
    nodeIds: string[];
    afterEdgeId?: string | null;
}

interface GraphOneHopEdgesRequestQuery {
    afterEdgeId?: string | null;
    nodeIds: string[];
}

export const getWorkbenchOverview = (query: GraphWorkbenchRequestQuery = {}) =>
    postJson<GraphWorkbenchOverviewRecord>(`${API_PREFIX}/workbench/overview/get`, {
        body: {},
        signal: query.signal
    });

export const listRecentEdges = (query: GraphWorkbenchRequestQuery = {}) =>
    postJson<GraphWorkbenchGraphRecord>(`${API_PREFIX}/workbench/recent-edges/list`, {
        body: {},
        signal: query.signal
    });

export const listOneHopEdges = (query: GraphOneHopEdgesQuery) =>
    postJson<GraphWorkbenchOneHopEdgesRecord, GraphOneHopEdgesRequestQuery>(
        `${API_PREFIX}/workbench/one-hop-edges/list`,
        {
            body: { afterEdgeId: query.afterEdgeId, nodeIds: query.nodeIds },
            signal: query.signal
        }
    );
