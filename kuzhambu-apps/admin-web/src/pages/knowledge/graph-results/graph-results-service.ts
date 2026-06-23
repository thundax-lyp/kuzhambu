import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { GraphVersionPageQuery, GraphVersionRecord } from "./graph-results-types";

const API_PREFIX = "/knowledge/graph-extraction";

interface GraphVersionDetailCommand {
    versionId: number;
}

export const pageVersions = (request: GraphVersionPageQuery = {}) => {
    return postJson<Page<GraphVersionRecord>, GraphVersionPageQuery>(`${API_PREFIX}/version/page`, {
        body: request
    });
};

export const getVersionDetail = (request: GraphVersionDetailCommand) => {
    return postJson<GraphVersionRecord, GraphVersionDetailCommand>(`${API_PREFIX}/version/get`, {
        body: request
    });
};
