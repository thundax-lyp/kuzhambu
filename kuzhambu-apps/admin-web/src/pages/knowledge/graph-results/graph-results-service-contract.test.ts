import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./graph-results-service";
import type {
    GraphEntityPageQuery,
    GraphLineageNodePageQuery,
    GraphLineageRelationPageQuery,
    GraphRelationPageQuery,
    GraphVersionPageQuery
} from "./graph-results-service";

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";

const capturedCalls: CapturedCall[] = [];

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const readFetchBody = (body: BodyInit | null | undefined) => {
    if (!body) {
        return undefined;
    }
    return JSON.parse(String(body));
};

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = readFetchUrl(input);
        capturedCalls.push({
            body: readFetchBody(init?.body),
            method: init?.method,
            path: url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "")
        });

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    versionId: "71",
                    taskId: "31",
                    taskType: "GRAPH",
                    status: "APPLIED"
                }
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200
            }
        );
    });
};

describe("knowledge graph results service request contracts", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        installFetchRecorder();
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("sends version page and detail requests", async () => {
        const query: GraphVersionPageQuery = {
            pageNo: 1,
            pageSize: 20,
            taskType: "GRAPH",
            status: "APPLIED",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001"
        };

        await service.pageVersions(query);
        expect(capturedCalls.at(-1)).toEqual({
            body: query,
            method: "POST",
            path: "/knowledge/graph-extraction/version/page"
        });

        await service.getVersionDetail({ versionId: "71" });
        expect(capturedCalls.at(-1)).toEqual({
            body: { versionId: "71" },
            method: "POST",
            path: "/knowledge/graph-extraction/version/get"
        });
    });

    it("sends entity and relation requests", async () => {
        const entityQuery: GraphEntityPageQuery = {
            pageNo: 1,
            pageSize: 20,
            versionId: "71",
            keyword: "孙悟空",
            entityType: "PERSON",
            confirmationStatus: "CONFIRMED"
        };
        const relationQuery: GraphRelationPageQuery = {
            pageNo: 1,
            pageSize: 20,
            versionId: "71",
            keyword: "师徒",
            relationType: "MASTER_DISCIPLE",
            confirmationStatus: "CONFIRMED"
        };

        await service.pageEntities(entityQuery);
        expect(capturedCalls.at(-1)).toEqual({
            body: entityQuery,
            method: "POST",
            path: "/knowledge/graph-extraction/entity/page"
        });

        await service.getEntityDetail({ entityId: "101" });
        expect(capturedCalls.at(-1)).toEqual({
            body: { entityId: "101" },
            method: "POST",
            path: "/knowledge/graph-extraction/entity/get"
        });

        await service.pageRelations(relationQuery);
        expect(capturedCalls.at(-1)).toEqual({
            body: relationQuery,
            method: "POST",
            path: "/knowledge/graph-extraction/relation/page"
        });

        await service.getRelationDetail({ relationId: "202" });
        expect(capturedCalls.at(-1)).toEqual({
            body: { relationId: "202" },
            method: "POST",
            path: "/knowledge/graph-extraction/relation/get"
        });
    });

    it("sends lineage node and relation requests", async () => {
        const nodeQuery: GraphLineageNodePageQuery = {
            pageNo: 1,
            pageSize: 20,
            versionId: "71",
            keyword: "贾宝玉",
            nodeType: "PERSON",
            confirmationStatus: "CONFIRMED"
        };
        const relationQuery: GraphLineageRelationPageQuery = {
            pageNo: 1,
            pageSize: 20,
            versionId: "71",
            keyword: "父子",
            relationType: "PARENT_CHILD",
            confirmationStatus: "CONFIRMED"
        };

        await service.pageLineageNodes(nodeQuery);
        expect(capturedCalls.at(-1)).toEqual({
            body: nodeQuery,
            method: "POST",
            path: "/knowledge/graph-extraction/lineage/node/page"
        });

        await service.getLineageNodeDetail({ nodeId: "301" });
        expect(capturedCalls.at(-1)).toEqual({
            body: { nodeId: "301" },
            method: "POST",
            path: "/knowledge/graph-extraction/lineage/node/get"
        });

        await service.pageLineageRelations(relationQuery);
        expect(capturedCalls.at(-1)).toEqual({
            body: relationQuery,
            method: "POST",
            path: "/knowledge/graph-extraction/lineage/relation/page"
        });

        await service.getLineageRelationDetail({ relationId: "302" });
        expect(capturedCalls.at(-1)).toEqual({
            body: { relationId: "302" },
            method: "POST",
            path: "/knowledge/graph-extraction/lineage/relation/get"
        });
    });
});
