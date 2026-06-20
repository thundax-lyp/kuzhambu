import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as categoryService from "./services/sancai-category-service";
import * as entryService from "./services/sancai-entry-service";
import * as volumeService from "./services/sancai-volume-service";

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

    it("sends category service requests with domain function names", async () => {
        await categoryService.listTypes();
        expectLastCall("GET", "/classics/sancai/categories/types", undefined);

        await categoryService.list();
        expectLastCall("POST", "/classics/sancai/categories/list", undefined);

        await categoryService.add({
            title: "天文",
            categoryType: "FORMAL"
        });
        expectLastCall("POST", "/classics/sancai/categories/add", {
            title: "天文",
            categoryType: "FORMAL"
        });

        await categoryService.update({
            id: 2,
            title: "天文",
            categoryType: "FORMAL"
        });
        expectLastCall("POST", "/classics/sancai/categories/update", {
            id: 2,
            title: "天文",
            categoryType: "FORMAL"
        });

        await categoryService.deleteById(2);
        expectLastCall("POST", "/classics/sancai/categories/delete", {
            id: 2
        });

        await categoryService.sort({
            orderedIds: [2, 3, 4],
            sortDirection: "ASC"
        });
        expectLastCall("POST", "/classics/sancai/categories/sort", {
            orderedIds: [2, 3, 4],
            sortDirection: "ASC"
        });
    });

    it("sends volume service requests with domain function names", async () => {
        await volumeService.listTypes();
        expectLastCall("GET", "/classics/sancai/volumes/types", undefined);

        await volumeService.list({
            categoryId: 2
        });
        expectLastCall("POST", "/classics/sancai/volumes/list", {
            categoryId: 2
        });

        await volumeService.add({
            categoryId: 2,
            title: "天文卷一",
            volumeType: "MAIN"
        });
        expectLastCall("POST", "/classics/sancai/volumes/add", {
            categoryId: 2,
            title: "天文卷一",
            volumeType: "MAIN"
        });

        await volumeService.update({
            id: 101,
            categoryId: 2,
            title: "天文卷一",
            volumeType: "MAIN"
        });
        expectLastCall("POST", "/classics/sancai/volumes/update", {
            id: 101,
            categoryId: 2,
            title: "天文卷一",
            volumeType: "MAIN"
        });

        await volumeService.deleteById(101);
        expectLastCall("POST", "/classics/sancai/volumes/delete", {
            id: 101
        });

        await volumeService.sort({
            orderedIds: [101, 102],
            sortDirection: "ASC"
        });
        expectLastCall("POST", "/classics/sancai/volumes/sort", {
            orderedIds: [101, 102],
            sortDirection: "ASC"
        });
    });

    it("sends entry service requests with domain function names", async () => {
        const entryCommand = {
            volumeId: 101,
            title: "天地",
            originalText: "原文",
            translationText: "译文",
            summary: "摘要",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "READY",
            imageStatus: "READY",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE"
        };

        await entryService.list({
            categoryId: 2,
            volumeId: 101,
            keyword: "天地",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "READY",
            imageStatus: "READY",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE",
            sortDirection: "ASC"
        });
        expectLastCall("POST", "/classics/sancai/entries/list", {
            categoryId: 2,
            volumeId: 101,
            keyword: "天地",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "READY",
            imageStatus: "READY",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE",
            sortDirection: "ASC"
        });

        await entryService.get(3001);
        expectLastCall("GET", "/classics/sancai/entries/3001", undefined);

        await entryService.add(entryCommand);
        expectLastCall("POST", "/classics/sancai/entries/add", entryCommand);

        await entryService.update({
            id: 3001,
            ...entryCommand
        });
        expectLastCall("POST", "/classics/sancai/entries/update", {
            id: 3001,
            ...entryCommand
        });

        await entryService.deleteById(3001);
        expectLastCall("POST", "/classics/sancai/entries/delete", {
            id: 3001
        });

        await entryService.sort({
            orderedIds: [3001, 3002],
            sortDirection: "ASC"
        });
        expectLastCall("POST", "/classics/sancai/entries/sort", {
            orderedIds: [3001, 3002],
            sortDirection: "ASC"
        });

        await entryService.listVersions(3001);
        expectLastCall("POST", "/classics/sancai/entries/versions/list", {
            id: 3001
        });

        await entryService.getVersion(3001, 9001);
        expectLastCall("POST", "/classics/sancai/entries/versions/get", {
            id: 3001,
            versionId: 9001
        });

        await entryService.resetVersion(3001, 9001);
        expectLastCall("POST", "/classics/sancai/entries/versions/reset", {
            id: 3001,
            versionId: 9001
        });
    });
});
