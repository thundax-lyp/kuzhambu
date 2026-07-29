import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as visualService from "./sancai-visual-service";

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

const readMockResponseData = (path: string) => {
    if (path.endsWith("/images/list")) {
        return [{ id: 8001, entryId: 3001, storageObjectId: 7001, title: "原图" }];
    }
    if (path.endsWith("/visual-assets/list")) {
        return [
            {
                id: 5001,
                visualAssetId: 5001,
                entryId: 3001,
                sourceImageStorageObjectId: 7001,
                generatedImageStorageObjectId: 7002
            }
        ];
    }
    if (path.endsWith("/visual-assets/update")) {
        return { id: 5002, visualAssetId: 5002 };
    }
    return { id: 3001, volumeId: 101, title: "天地" };
};

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = readFetchUrl(input);
        const path = url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "");
        capturedCalls.push({
            body: readFetchBody(init?.body),
            method: init?.method,
            path
        });

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: readMockResponseData(path)
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

describe("sancai visual service contracts", () => {
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

    it("normalizes numeric backend ids at the visual page boundary", async () => {
        const entry = await visualService.get("3001");
        const images = await visualService.listImages("3001");
        const assets = await visualService.listVisualAssets("3001");
        const savedAsset = await visualService.updateVisualAsset({
            entryId: "3001",
            sourceImageStorageObjectId: "7001",
            textWeight: 60,
            imageWeight: 40
        });

        expect(entry).toEqual(expect.objectContaining({ id: "3001", volumeId: "101" }));
        expect(images[0]).toEqual(
            expect.objectContaining({ id: "8001", entryId: "3001", storageObjectId: "7001" })
        );
        expect(assets[0]).toEqual(
            expect.objectContaining({
                id: "5001",
                visualAssetId: "5001",
                entryId: "3001",
                sourceImageStorageObjectId: "7001",
                generatedImageStorageObjectId: "7002"
            })
        );
        expect(savedAsset).toEqual(expect.objectContaining({ id: "5002", visualAssetId: "5002" }));
        expect(capturedCalls.at(-1)).toEqual({
            body: {
                entryId: "3001",
                sourceImageStorageObjectId: "7001",
                textWeight: 60,
                imageWeight: 40
            },
            method: "POST",
            path: "/classics/sancai/assets/visual-assets/update"
        });
    });
});
