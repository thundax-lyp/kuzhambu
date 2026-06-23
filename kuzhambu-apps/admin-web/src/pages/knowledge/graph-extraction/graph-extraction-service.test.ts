import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./graph-extraction-service";
import type { GraphExtractionCreateCommand } from "./graph-extraction-types";

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
                    taskId: "9001",
                    taskType: "GRAPH",
                    status: "PENDING"
                }
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

describe("knowledge graph extraction service request contracts", () => {
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

    it("sends create requests for all graph extraction task types", async () => {
        const command: GraphExtractionCreateCommand = {
            scopeType: "CLASSICS_ENTRY",
            scopeJson: '{"entryId":1001}',
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001,
            requestedBy: 2001,
            serviceId: 3001,
            serviceRole: "KNOWLEDGE_GRAPH",
            modelId: 4001,
            modelName: "gpt-5.5",
            promptVersionId: 5001,
            requestId: "req-graph-001",
            traceId: "trace-graph-001",
            promptMessagesJson: '[{"role":"system","content":"extract"}]',
            promptVariablesJson: '{"locale":"zh-CN"}',
            promptHash: "prompt-hash",
            inputPayloadJson: '{"content":"test"}',
            outputSchemaJson: '{"type":"object"}',
            forceJson: true,
            locale: "zh-CN"
        };

        await service.requestRelationExtraction(command);
        expectLastCall("POST", "/knowledge/graph-extraction/relation/request", command);

        await service.requestGraphExtraction(command);
        expectLastCall("POST", "/knowledge/graph-extraction/graph/request", command);

        await service.requestLineageExtraction(command);
        expectLastCall("POST", "/knowledge/graph-extraction/lineage/request", command);
    });

    it("sends task query and task action requests", async () => {
        await service.pageTasks({
            pageNo: 1,
            pageSize: 20,
            taskType: "GRAPH",
            status: "PENDING",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001
        });
        expectLastCall("POST", "/knowledge/graph-extraction/task/page", {
            pageNo: 1,
            pageSize: 20,
            taskType: "GRAPH",
            status: "PENDING",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001
        });

        await service.getTaskDetail({ taskId: 9001 });
        expectLastCall("POST", "/knowledge/graph-extraction/task/detail", {
            taskId: 9001
        });

        await service.applyTaskCandidate({ taskId: 9001 });
        expectLastCall("POST", "/knowledge/graph-extraction/task/apply", {
            taskId: 9001
        });
    });
});
