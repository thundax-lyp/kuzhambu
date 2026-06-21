import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./ming-customs-service";

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

        return new Response(JSON.stringify(toSuccessResponse()), {
            headers: {
                "Content-Type": "application/json"
            },
            status: 200
        });
    });
};

const toSuccessResponse = () => {
    const path = capturedCalls.at(-1)?.path;
    if (path === "/sys/dict/page") {
        return {
            code: "COMMON-00000",
            message: "success",
            data: {
                records: [
                    {
                        type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                        value: "RITUAL",
                        label: "礼制",
                        remarks: "明代习俗分类"
                    }
                ]
            }
        };
    }
    return {
        code: "COMMON-00000",
        message: "success",
        data: true
    };
};

const expectLastCall = (method: string, path: string, body: unknown) => {
    expect(capturedCalls.at(-1)).toEqual({
        body,
        method,
        path
    });
};

describe("ming customs service request contracts", () => {
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

    it("sends ming customs page and detail requests", async () => {
        await service.page({
            pageNo: 1,
            pageSize: 20,
            keyword: "元旦",
            category: "RITUAL",
            visibility: "PUBLIC",
            tagName: "礼制",
            sortDirection: "DESC"
        });
        expectLastCall("POST", "/classics/ming-customs/page", {
            pageNo: 1,
            pageSize: 20,
            keyword: "元旦",
            category: "RITUAL",
            visibility: "PUBLIC",
            tagName: "礼制",
            sortDirection: "DESC"
        });

        await service.get(500000000001);
        expectLastCall("GET", "/classics/ming-customs/500000000001", undefined);
    });

    it("sends ming customs write requests", async () => {
        const command: service.MingCustomsCommand = {
            title: "岁时礼仪：元旦朝贺",
            category: "RITUAL",
            chapter: "岁时礼仪",
            section: "正旦",
            summary: "记录明代正旦朝贺与家族拜礼。",
            contentFormat: "MARKDOWN",
            content: "## 元旦朝贺",
            originalExcerpts: "正旦，百官朝贺。",
            visibility: "PUBLIC"
        };

        await service.add(command);
        expectLastCall("POST", "/classics/ming-customs/add", command);

        await service.update({
            id: 500000000001,
            ...command
        });
        expectLastCall("POST", "/classics/ming-customs/update", {
            id: 500000000001,
            ...command
        });

        await service.deleteById(500000000001);
        expectLastCall("POST", "/classics/ming-customs/delete", {
            id: 500000000001
        });
    });

    it("sends ming customs option and keyword cloud requests", async () => {
        await service.listKeywordCloud("PUBLIC");
        expectLastCall("GET", "/classics/ming-customs/keyword-cloud?visibility=PUBLIC", undefined);

        await service.listKeywordCloud();
        expectLastCall("GET", "/classics/ming-customs/keyword-cloud", undefined);

        const options = await service.listCategoryOptions();
        expectLastCall("POST", "/sys/dict/page", {
            pageNo: 1,
            pageSize: 100,
            type: "CLASSICS_MING_CUSTOMS_CATEGORY"
        });
        expect(options).toEqual([
            {
                type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                value: "RITUAL",
                label: "礼制"
            }
        ]);
    });
});
