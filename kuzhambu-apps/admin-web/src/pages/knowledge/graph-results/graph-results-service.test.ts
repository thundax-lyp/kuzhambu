import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./graph-results-service";
import type { GraphVersionPageQuery } from "./graph-results-types";

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
                    versionId: 71,
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
            sourceContentId: 1001
        };

        await service.pageVersions(query);
        expect(capturedCalls.at(-1)).toEqual({
            body: query,
            method: "POST",
            path: "/knowledge/graph-extraction/version/page"
        });

        await service.getVersionDetail({ versionId: 71 });
        expect(capturedCalls.at(-1)).toEqual({
            body: { versionId: 71 },
            method: "POST",
            path: "/knowledge/graph-extraction/version/get"
        });
    });
});
