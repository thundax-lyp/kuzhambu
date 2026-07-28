import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./graph-workbench-service";

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
                data: {}
            }),
            {
                headers: {
                    "Content-Type": "application/json"
                },
                status: 200
            }
        );
    });
};

const expectLastCall = (method: string, path: string, body: unknown) => {
    expect(capturedCalls.at(-1)).toEqual({
        body,
        method,
        path
    });
};

describe("knowledge graph workbench service request contracts", () => {
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

    it("sends manuscript tree and detail requests", async () => {
        await service.listManuscriptTree({
            sourceContentType: "SANCAI_ENTRY",
            parentKey: "SOURCE_ROOT:SANCAI_ENTRY",
            keyword: "黄帝",
            graphStatus: "CANDIDATE_READY"
        });
        expectLastCall("POST", "/knowledge/graph-workbench/manuscript-tree", {
            sourceContentType: "SANCAI_ENTRY",
            parentKey: "SOURCE_ROOT:SANCAI_ENTRY",
            keyword: "黄帝",
            graphStatus: "CANDIDATE_READY"
        });

        await service.getManuscript({
            sourceContentType: "WANGQI_DOCUMENT",
            sourceContentId: "2001"
        });
        expectLastCall("POST", "/knowledge/graph-workbench/manuscript/get", {
            sourceContentType: "WANGQI_DOCUMENT",
            sourceContentId: "2001"
        });
    });

    it("sends automatic extraction payload without manual JSON fields", async () => {
        await service.extractManuscript({
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001",
            taskType: "GRAPH"
        });

        const body = capturedCalls.at(-1)?.body;
        expectLastCall("POST", "/knowledge/graph-workbench/manuscript/extract", {
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001",
            taskType: "GRAPH"
        });
        expect(body).not.toHaveProperty("scopeJson");
        expect(body).not.toHaveProperty("selectionScopeJson");
        expect(body).not.toHaveProperty("promptMessagesJson");
        expect(body).not.toHaveProperty("inputPayloadJson");
        expect(body).not.toHaveProperty("outputSchemaJson");
        expect(body).not.toHaveProperty("modelName");
    });

    it("sends candidate read and apply requests", async () => {
        await service.getLatestCandidate({
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001",
            taskType: "GRAPH"
        });
        expectLastCall("POST", "/knowledge/graph-workbench/candidate/get", {
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001",
            taskType: "GRAPH"
        });

        await service.applyCandidate({ taskId: "9001" });
        expectLastCall("POST", "/knowledge/graph-workbench/candidate/apply", {
            taskId: "9001"
        });
    });
});
