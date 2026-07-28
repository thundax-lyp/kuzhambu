import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./lineage-service";
import type { LineageCanvasQuery } from "./lineage-service";

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
                    version: {
                        versionId: "71",
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
                }
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200
            }
        );
    });
};

describe("knowledge lineage service request contracts", () => {
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

    it("gets lineage canvas through the admin lineage api", async () => {
        const query: LineageCanvasQuery = {
            versionId: "71",
            focusNodeId: "301",
            focusRelationId: null,
            keyword: "贾宝玉",
            nodeType: "PERSON",
            relationType: "PARENT_CHILD",
            confirmationStatus: "CONFIRMED",
            depth: 2
        };

        const result = await service.getLineageCanvas(query);

        expect(result.version?.versionId).toBe("71");
        expect(capturedCalls.at(-1)).toEqual({
            body: query,
            method: "POST",
            path: "/knowledge/lineage/canvas"
        });
    });
});
