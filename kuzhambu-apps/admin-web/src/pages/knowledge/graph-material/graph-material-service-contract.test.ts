import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./graph-material-service";

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

describe("knowledge graph material service request contracts", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        vi.spyOn(crypto, "randomUUID").mockReturnValue("idem-material-001");
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("sends material page and detail requests to Knowledge graph endpoints", async () => {
        installFetchRecorder({
            pageNo: "1",
            pageSize: "20",
            records: [],
            totalCount: "0",
            totalPage: "1"
        });

        await service.pageMaterials({
            categoryCode: "astronomy",
            contentType: "SANCAI_ENTRY",
            keyword: "天文",
            pageNo: 1,
            pageSize: 20,
            status: "DRAFT",
            taskDisposition: "PENDING",
            taskExecutionStatus: "SUCCEEDED",
            volumeCode: "vol-001"
        });
        expectLastCall("POST", "/knowledge/graph/material/page", {
            categoryCode: "astronomy",
            contentType: "SANCAI_ENTRY",
            keyword: "天文",
            pageNo: 1,
            pageSize: 20,
            status: "DRAFT",
            taskDisposition: "PENDING",
            taskExecutionStatus: "SUCCEEDED",
            volumeCode: "vol-001"
        });

        await service.getMaterial({
            contentRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" }
        });
        expectLastCall("POST", "/knowledge/graph/material/get", {
            contentRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" }
        });
    });

    it("adds idempotency key to extraction requests", async () => {
        installFetchRecorder({
            attemptNo: "1",
            currentStage: "PENDING",
            disposition: null,
            executionStatus: "PENDING",
            id: "7001",
            lockVersion: "1",
            materialRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" },
            progress: 0
        });

        await service.createExtraction({
            contentRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" }
        });
        expectLastCall("POST", "/knowledge/graph/material/extraction/create", {
            contentRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" },
            idempotencyKey: "idem-material-001"
        });

        await service.createBatchExtraction({
            contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }]
        });
        expectLastCall("POST", "/knowledge/graph/task/batch/create", {
            idempotencyKey: "idem-material-001",
            selection: {
                contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }]
            }
        });
    });

    it("sends batch withdrawal preview and withdraw requests", async () => {
        installFetchRecorder({
            materials: []
        });

        await service.previewBatchWithdrawal({
            contentRefs: [{ contentRefId: "1002", contentType: "SANCAI_ENTRY" }]
        });
        expectLastCall("POST", "/knowledge/graph/publication/batch/withdrawal/preview", {
            contentRefs: [{ contentRefId: "1002", contentType: "SANCAI_ENTRY" }]
        });

        await service.withdrawBatch({
            materials: [
                {
                    contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                    materialLockVersion: "4"
                }
            ]
        });
        expectLastCall("POST", "/knowledge/graph/publication/batch/withdrawal/withdraw", {
            idempotencyKey: "idem-material-001",
            materials: [
                {
                    contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                    materialLockVersion: "4"
                }
            ]
        });
    });
});
