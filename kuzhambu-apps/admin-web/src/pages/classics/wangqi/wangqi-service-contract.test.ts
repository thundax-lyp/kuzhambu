import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./wangqi-service";

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
    if (body instanceof FormData) {
        return Object.fromEntries(
            Array.from(body.entries()).map(([key, value]) => [
                key,
                value instanceof File ? value.name : String(value)
            ])
        );
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

describe("wangqi service request contracts", () => {
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

    it("sends wangqi document read requests", async () => {
        await service.page({
            pageNo: 1,
            pageSize: 20,
            keyword: "王圻",
            visibility: "PUBLIC",
            sortDirection: "DESC"
        });
        expectLastCall("POST", "/classics/wangqi/documents/page", {
            pageNo: 1,
            pageSize: 20,
            keyword: "王圻",
            visibility: "PUBLIC",
            sortDirection: "DESC"
        });

        await service.get(400000000001);
        expectLastCall("POST", "/classics/wangqi/documents/400000000001/get", {
            id: 400000000001
        });

        await service.listTimeline({
            keyword: "万历",
            visibility: "PUBLIC",
            sortDirection: "ASC"
        });
        expectLastCall("POST", "/classics/wangqi/documents/timeline/list", {
            keyword: "万历",
            visibility: "PUBLIC",
            sortDirection: "ASC"
        });
    });

    it("sends wangqi document write requests", async () => {
        const command: service.WangqiDocumentCommand = {
            title: "王圻文档",
            summary: "摘要",
            contentFormat: "MARKDOWN",
            content: "正文",
            documentTime: "2026-01-01T00:00:00.000+00:00",
            storageObjectId: 7001,
            visibility: "PUBLIC"
        };

        await service.add(command);
        expectLastCall("POST", "/classics/wangqi/documents/add", command);

        await service.update({
            id: 400000000001,
            ...command
        });
        expectLastCall("POST", "/classics/wangqi/documents/update", {
            id: 400000000001,
            ...command
        });

        await service.deleteById(400000000001);
        expectLastCall("POST", "/classics/wangqi/documents/delete", {
            id: 400000000001
        });
    });

    it("sends wangqi source file requests without storage api coupling", async () => {
        await service.uploadSourceFile(
            400000000001,
            new File(["source-bin"], "wangqi.pdf", { type: "application/pdf" })
        );
        expectLastCall("POST", "/classics/wangqi/documents/400000000001/source-file/upload", {
            file: "wangqi.pdf"
        });

        await service.getSourceFile(400000000001);
        expectLastCall("POST", "/classics/wangqi/documents/400000000001/source-file/get", {
            id: 400000000001
        });

        expect(service.getSourceFileContentUrl({ documentId: 400000000001 })).toBe(
            `${DEV_PROXY_PREFIX}/classics/wangqi/documents/400000000001/source-file/content`
        );
        expect(
            service.getSourceFileContentUrl({ documentId: 400000000001, mode: "download" })
        ).toBe(
            `${DEV_PROXY_PREFIX}/classics/wangqi/documents/400000000001/source-file/content?download=true`
        );
    });

    it("sends wangqi version requests", async () => {
        await service.listVersions(400000000001);
        expectLastCall("POST", "/classics/wangqi/documents/versions/list", {
            id: 400000000001
        });

        await service.getVersion(400000000001, 9001);
        expectLastCall("POST", "/classics/wangqi/documents/versions/get", {
            id: 400000000001,
            versionId: 9001
        });

        await service.resetVersion(400000000001, 9001);
        expectLastCall("POST", "/classics/wangqi/documents/versions/reset", {
            id: 400000000001,
            versionId: 9001
        });
    });
});
