import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as dictionaryService from "@/pages/system/dictionary/dictionary-service";

interface CapturedCall {
    path: string;
    body: unknown;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";

const capturedRequests: CapturedCall[] = [];

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
        const path = url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "");
        capturedRequests.push({
            path,
            body: init?.body ? JSON.parse(String(init.body)) : undefined
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

const expectLastRequest = (path: string, body: unknown) => {
    expect(capturedRequests.at(-1)).toEqual({
        path,
        body
    });
};

describe("dictionary service request contracts", () => {
    beforeEach(() => {
        capturedRequests.length = 0;
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

    it("sends dictionary write requests with Dict request fields", async () => {
        const saveRequest: dictionaryService.DictSaveCommand = {
            id: "dict-1",
            type: "system_status",
            label: "启用",
            value: "ENABLED",
            remarks: "默认启用状态"
        };

        await dictionaryService.addDictionary(saveRequest);
        expectLastRequest("/sys/dict/create", saveRequest);

        await dictionaryService.changeDictionaryInfo(saveRequest);
        expectLastRequest("/sys/dict/update", saveRequest);

        await dictionaryService.removeDictionaries(["dict-1"]);
        expectLastRequest("/sys/dict/delete", [{ id: "dict-1" }]);
    });
});
