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
        const command: ClassicsExportCreateCommand = {
            contentType: "SANCAI_ENTRY",
            exportKind: "SELECTED_ENTRIES",
            exportFormat: "ZIP",
            scopeType: "SEARCH_RESULT",
            scopeJson: JSON.stringify({ ids: [3001, 3002] })
        };

        await exportService.create(command);
        expectLastCall("POST", "/classics/content/exports/create", command);

        await exportService.page({
            pageNo: 1,
            pageSize: 20,
            contentType: "SANCAI_ENTRY",
            exportKind: "SELECTED_ENTRIES",
            status: "COMPLETED"
        });
        expectLastCall("POST", "/classics/content/exports/page", {
            pageNo: 1,
            pageSize: 20,
            contentType: "SANCAI_ENTRY",
            exportKind: "SELECTED_ENTRIES",
            status: "COMPLETED"
        });

        expect(
            exportService.getContentUrl({
                jobId: 9001
            })
        ).toBe(`${DEV_PROXY_PREFIX}/classics/content/exports/9001/content`);
        expect(
            exportService.getContentUrl({
                jobId: 9001,
                mode: "download"
            })
        ).toBe(`${DEV_PROXY_PREFIX}/classics/content/exports/9001/content?download=true`);
    });
});
