import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as publicationJobsService from "@/pages/classics/publication-jobs/publication-jobs-service";

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

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = readFetchUrl(input);
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
            method: init?.method,
            path: url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "")
        });
        return new Response(
            JSON.stringify({ code: "COMMON-00000", message: "success", data: {} }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200
            }
        );
    });
};

describe("publication jobs service request contracts", () => {
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

    it("sends page and detail requests to the read-only endpoints", async () => {
        const query = {
            pageNo: 2,
            pageSize: 20,
            jobType: "PUBLISH" as const,
            jobResultStatus: "FAILED" as const,
            jobStatus: "FASTGPT_PREPARED" as const,
            contentType: "SANCAI_ENTRY" as const,
            keyword: "本草"
        };

        await publicationJobsService.page(query);
        expect(capturedCalls.at(-1)).toEqual({
            body: query,
            method: "POST",
            path: "/classics/publication-jobs/page"
        });

        await publicationJobsService.get({ id: "9007199254740993" });
        expect(capturedCalls.at(-1)).toEqual({
            body: { id: "9007199254740993" },
            method: "POST",
            path: "/classics/publication-jobs/get"
        });
    });
});
