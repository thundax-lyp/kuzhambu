import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./ming-custom-service";

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
                        value: "食（饮食生活）",
                        label: "食（饮食生活）",
                        remarks: "明代习俗分类"
                    }
                ]
            }
        };
    }
    if (path === "/classics/ming-customs/categories/list") {
        return {
            code: "COMMON-00000",
            message: "success",
            data: { categories: ["食（饮食生活）", "RITUAL"] }
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
            category: "食（饮食生活）",
            tagName: "礼制",
            sortDirection: "DESC"
        });
        expectLastCall("POST", "/classics/ming-customs/page", {
            pageNo: 1,
            pageSize: 20,
            keyword: "元旦",
            category: "食（饮食生活）",
            tagName: "礼制",
            sortDirection: "DESC"
        });

        await service.get("500000000001");
        expectLastCall("POST", "/classics/ming-customs/get", {
            id: "500000000001"
        });
    });

    it("sends ming customs write requests", async () => {
        const command: service.MingCustomsCommand = {
            title: "岁时礼仪：元旦朝贺",
            category: "食（饮食生活）",
            chapter: "岁时礼仪",
            section: "正旦",
            summary: "记录明代正旦朝贺与家族拜礼。",
            contentFormat: "MARKDOWN",
            content: "## 元旦朝贺",
            originalExcerpts: "正旦，百官朝贺。"
        };

        await service.add(command);
        expectLastCall("POST", "/classics/ming-customs/add", command);

        await service.update({
            id: "500000000001",
            ...command
        });
        expectLastCall("POST", "/classics/ming-customs/update", {
            id: "500000000001",
            ...command
        });

        await service.deleteById("500000000001");
        expectLastCall("POST", "/classics/ming-customs/delete", {
            id: "500000000001"
        });

        await service.publish({ id: "500000000001" });
        expectLastCall("POST", "/classics/ming-customs/publish", { id: "500000000001" });

        await service.submitOffline({ id: "500000000001" });
        expectLastCall("POST", "/classics/ming-customs/offline", { id: "500000000001" });

        await service.publishBatch({ ids: ["500000000001", "500000000002"] });
        expectLastCall("POST", "/classics/ming-customs/batch/publish", {
            ids: ["500000000001", "500000000002"]
        });

        await service.submitOfflineBatch({ ids: ["500000000001", "500000000002"] });
        expectLastCall("POST", "/classics/ming-customs/batch/offline", {
            ids: ["500000000001", "500000000002"]
        });
    });

    it("sends ming customs option and keyword cloud requests", async () => {
        await service.listKeywordCloud();
        expectLastCall("POST", "/classics/ming-customs/keyword-cloud/list", {});

        await service.listTagCloud({
            category: "食（饮食生活）",
            keyword: "元旦"
        });
        expectLastCall("POST", "/classics/ming-customs/tag-cloud/list", {
            category: "食（饮食生活）",
            keyword: "元旦"
        });

        await service.listTagCloud();
        expectLastCall("POST", "/classics/ming-customs/tag-cloud/list", {});

        const options = await service.listCategoryOptions();
        expectLastCall("POST", "/classics/ming-customs/categories/list", {});
        expect(options).toEqual([
            {
                type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                value: "食（饮食生活）",
                label: "食（饮食生活）"
            },
            {
                type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                value: "RITUAL",
                label: "历史分类：RITUAL"
            }
        ]);
    });

    it("sends ming customs version requests", async () => {
        await service.listVersions("600000000001");
        expectLastCall("POST", "/classics/ming-customs/versions/list", {
            id: "600000000001"
        });

        await service.getVersion("600000000001", "9001");
        expectLastCall("POST", "/classics/ming-customs/versions/get", {
            id: "600000000001",
            versionId: "9001"
        });

        await service.resetVersion("600000000001", "9001");
        expectLastCall("POST", "/classics/ming-customs/versions/reset", {
            id: "600000000001",
            versionId: "9001"
        });
    });
});
