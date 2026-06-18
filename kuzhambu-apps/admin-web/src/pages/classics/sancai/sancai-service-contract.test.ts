import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as sancaiService from "./sancai-service";

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/admin-api/api";

const capturedCalls: CapturedCall[] = [];

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = typeof input === "string" ? input : input.url;
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
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

describe("sancai service request contracts", () => {
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

    it("sends Sancai list and entry requests with backend contract fields", async () => {
        await sancaiService.listCategories();
        expectLastCall("POST", "/classics/sancai/categories/list", undefined);

        await sancaiService.listVolumes({
            categoryId: 2
        });
        expectLastCall("POST", "/classics/sancai/volumes/list", {
            categoryId: 2
        });

        await sancaiService.pageEntries({
            categoryId: 2,
            volumeId: 101,
            keyword: "天地",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "TRANSLATED",
            imageStatus: "HAS_IMAGE",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE",
            sortDirection: "ASC",
            pageNo: 1,
            pageSize: 50
        });
        expectLastCall("POST", "/classics/sancai/entries/page", {
            categoryId: 2,
            volumeId: 101,
            keyword: "天地",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "TRANSLATED",
            imageStatus: "HAS_IMAGE",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE",
            sortDirection: "ASC",
            pageNo: 1,
            pageSize: 50
        });

        await sancaiService.getEntry(3001);
        expectLastCall("GET", "/classics/sancai/entries/3001", undefined);

        await sancaiService.saveEntry({
            id: 3001,
            volumeId: 101,
            title: "天地",
            originalText: "原文",
            translationText: "译文",
            summary: "摘要",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "TRANSLATED",
            imageStatus: "HAS_IMAGE",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE"
        });
        expectLastCall("POST", "/classics/sancai/entries/save", {
            id: 3001,
            volumeId: 101,
            title: "天地",
            originalText: "原文",
            translationText: "译文",
            summary: "摘要",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "TRANSLATED",
            imageStatus: "HAS_IMAGE",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE"
        });
    });
});
