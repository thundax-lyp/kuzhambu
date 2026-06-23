import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./taxonomy-service";

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
                headers: { "Content-Type": "application/json" },
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

describe("taxonomy service merge contracts", () => {
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

    it("sends merge preview, apply, deprecate and metrics requests", async () => {
        const request = {
            sourceTagId: "1001",
            targetTagId: "1002"
        };

        await service.previewTagMergeImpact(request);
        expectLastCall("POST", "/knowledge/taxonomy/tag/merge/preview", request);

        await service.applyTagMerge(request);
        expectLastCall("POST", "/knowledge/taxonomy/tag/merge/apply", request);

        await service.deprecateTag({ id: "1001" });
        expectLastCall("POST", "/knowledge/taxonomy/tag/deprecate", {
            id: "1001"
        });

        await service.getTagGovernanceMetrics({ topLimit: 10, recentMonths: 6 });
        expectLastCall("POST", "/knowledge/taxonomy/tag/metrics", {
            topLimit: 10,
            recentMonths: 6
        });
    });
});
