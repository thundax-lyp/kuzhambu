import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as exportService from "@/pages/classics/common/classics-export-service";
import type { ClassicsExportCreateCommand } from "@/pages/classics/common/classics-export-service";

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
                data: true
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

describe("classics export service request contracts", () => {
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

    it("sends classics export requests and builds content urls", async () => {
        const scopePayload = {
            title: "王圻文档 导出",
            contentType: "WANGQI_DOCUMENT",
            scopeType: "SELECTED_ITEMS",
            items: [
                {
                    id: "400000000001",
                    title: "王圻文档",
                    text: "## 王圻",
                    summary: "记录王圻古籍条目。",
                    visibility: "PUBLIC",
                    documentTime: "2026-01-01T00:00:00.000+00:00",
                    sourceFileStorageObjectId: "7001"
                }
            ]
        };
        const command: ClassicsExportCreateCommand = {
            contentType: "WANGQI_DOCUMENT",
            exportKind: "CONTENT_DATASET",
            exportFormat: "HTML",
            scopeType: "SELECTED_ITEMS",
            scopeJson: JSON.stringify(scopePayload)
        };

        await exportService.create(command);
        expectLastCall("POST", "/classics/content/exports/create", command);

        await exportService.page({
            pageNo: 1,
            pageSize: 20,
            contentType: "WANGQI_DOCUMENT",
            exportKind: "CONTENT_DATASET",
            status: "COMPLETED"
        });
        expectLastCall("POST", "/classics/content/exports/page", {
            pageNo: 1,
            pageSize: 20,
            contentType: "WANGQI_DOCUMENT",
            exportKind: "CONTENT_DATASET",
            status: "COMPLETED"
        });

        await exportService.deleteById("9001");
        expectLastCall("POST", "/classics/content/exports/delete", {
            id: "9001"
        });

        expect(
            exportService.getContentUrl({
                jobId: "9001"
            })
        ).toBe(`${DEV_PROXY_PREFIX}/classics/content/exports/9001/content`);
        expect(
            exportService.getContentUrl({
                jobId: "9001",
                mode: "download"
            })
        ).toBe(`${DEV_PROXY_PREFIX}/classics/content/exports/9001/content?download=true`);
    });
});
