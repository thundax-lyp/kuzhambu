import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./graph-extraction-service";

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

const installFetchRecorder = (data: unknown = { id: "7001" }) => {
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
                data,
                message: "success"
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
        vi.spyOn(crypto, "randomUUID").mockReturnValue("00000000-0000-4000-8000-000000000002");
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("sends task page requests to Knowledge graph endpoints", async () => {
        installFetchRecorder({
            pageNo: "1",
            pageSize: "20",
            records: [],
            totalCount: "0",
            totalPage: "1"
        });

        await service.pageTasks({
            batchId: "batch-001",
            contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }],
            executionStatus: "RUNNING",
            groupBy: "MATERIAL",
            pageNo: 1,
            pageSize: 20
        });
        expectLastCall("POST", "/knowledge/graph/task/page", {
            batchId: "batch-001",
            contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }],
            executionStatus: "RUNNING",
            groupBy: "MATERIAL",
            pageNo: 1,
            pageSize: 20
        });
    });

    it("adds idempotency key, lock version and expected state to task retry", async () => {
        installFetchRecorder({
            attemptNo: "1",
            currentStage: "CANDIDATE_READY",
            disposition: "PENDING",
            executionStatus: "SUCCEEDED",
            id: "7001",
            lockVersion: "5",
            materialRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" },
            progress: 100
        });

        await service.retryTask({
            expectedExecutionStatus: "FAILED",
            taskId: "7003",
            taskLockVersion: "3"
        });
        expectLastCall("POST", "/knowledge/graph/task/retry", {
            expectedExecutionStatus: "FAILED",
            idempotencyKey: "00000000-0000-4000-8000-000000000002",
            taskId: "7003",
            taskLockVersion: "3"
        });
    });
});
