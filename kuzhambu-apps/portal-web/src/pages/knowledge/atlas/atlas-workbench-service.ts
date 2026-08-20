import { postJson } from "@/api/http";
import type {
    AtlasGraphRecord,
    AtlasOneHopEdgesRecord,
    AtlasOverviewRecord
} from "./atlas-workbench-types";

const ATLAS_PATH = "/portal/knowledge/graph/atlas";

export const getOverview = () => postJson<AtlasOverviewRecord>(`${ATLAS_PATH}/overview/get`, {});

export const listRecentEdges = () =>
    postJson<AtlasGraphRecord>(`${ATLAS_PATH}/recent-edges/list`, {});

export const listOneHopEdges = (nodeIds: string[], afterEdgeId: string | null) =>
    postJson<AtlasOneHopEdgesRecord>(`${ATLAS_PATH}/one-hop-edges/list`, {
        afterEdgeId,
        nodeIds
    });
