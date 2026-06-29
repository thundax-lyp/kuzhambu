import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as shareService from "@/pages/classics/common/classics-share-service";

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
        const method = init?.method || "GET";
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
            method,
            path: url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "")
        });
        let data: unknown = {
            id: 9001,
            shareToken: "abc123_-",
            shareUrl: "http://localhost:5174/share/abc123_-",
            status: "ACTIVE",
            targets: [
                {
                    contentId: 3001,
                    contentType: "SANCAI_ENTRY",
                    contentVersionId: 8101,
                    contentVersionNo: 1,
                    id: 7001,
                    titleSnapshot: "天地"
                }
            ],
            title: "三才分享",
            visibility: "PUBLIC"
        };
        if (url.includes("/classics/shares/page")) {
            data = {
                pageNo: 1,
                pageSize: 10,
                count: 1,
                totalCount: 1,
                totalPage: 1,
                records: [data]
            };
        }
        if (url.includes("/classics/shares/access-records/page")) {
            data = {
                pageNo: 1,
                pageSize: 10,
                count: 1,
                totalCount: 1,
                totalPage: 1,
                records: [
                    {
                        id: 5001,
                        shareLinkId: 9001,
                        accessResult: "ALLOWED"
                    }
                ]
            };
        }

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                data,
                message: "success"
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

describe("classics share service request contracts", () => {
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

    it("creates a share with target references and reads the portal url", async () => {
        const response = await shareService.create({
            targets: [
                {
                    contentId: 3001,
                    contentType: "SANCAI_ENTRY"
                }
            ],
            title: "三才分享",
            visibility: "PUBLIC"
        });

        expect(capturedCalls.at(-1)).toEqual({
            body: {
                targets: [
                    {
                        contentId: 3001,
                        contentType: "SANCAI_ENTRY"
                    }
                ],
                title: "三才分享",
                visibility: "PUBLIC"
            },
            method: "POST",
            path: "/classics/shares/create"
        });
        expect(response.shareUrl).toBe("http://localhost:5174/share/abc123_-");
    });

    it("loads shares by page request", async () => {
        await shareService.page({
            pageNo: 1,
            pageSize: 10,
            status: "ACTIVE",
            visibility: "PUBLIC"
        });
        expect(capturedCalls.at(-1)).toEqual({
            body: {
                pageNo: 1,
                pageSize: 10,
                status: "ACTIVE",
                visibility: "PUBLIC"
            },
            method: "POST",
            path: "/classics/shares/page"
        });
    });

    it("gets a share by id", async () => {
        await shareService.get(9001);
        expect(capturedCalls.at(-1)).toEqual({
            body: undefined,
            method: "GET",
            path: "/classics/shares/9001"
        });
    });

    it("updates share status by id", async () => {
        await shareService.updateStatus({
            id: 9001,
            status: "REVOKED"
        });
        expect(capturedCalls.at(-1)).toEqual({
            body: {
                id: 9001,
                status: "REVOKED"
            },
            method: "POST",
            path: "/classics/shares/status/update"
        });
    });

    it("loads access records for a share", async () => {
        await shareService.pageAccessRecords({
            pageNo: 1,
            pageSize: 10,
            shareLinkId: 9001,
            shareTargetId: 7001
        });
        expect(capturedCalls.at(-1)).toEqual({
            body: {
                pageNo: 1,
                pageSize: 10,
                shareLinkId: 9001,
                shareTargetId: 7001
            },
            method: "POST",
            path: "/classics/shares/access-records/page"
        });
    });
});
