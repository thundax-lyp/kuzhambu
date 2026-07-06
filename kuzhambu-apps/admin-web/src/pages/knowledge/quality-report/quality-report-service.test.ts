import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./quality-report-service";
import type { GenerateQualityReportCommand, QualityReportPageQuery } from "./quality-report-types";

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
                data: {
                    report: {
                        reportId: 1001,
                        graphVersionId: 71,
                        reportStatus: "PUBLISHED"
                    },
                    records: []
                }
            }),
            { headers: { "Content-Type": "application/json" }, status: 200 }
        );
    });
};

const expectLastCall = (method: string, path: string, body: unknown) => {
    expect(capturedCalls.at(-1)).toEqual({ body, method, path });
};

describe("knowledge quality report service request contracts", () => {
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

    it("sends generate request", async () => {
        const command: GenerateQualityReportCommand = {
            graphVersionId: 71,
            generatedBy: 1
        };

        await service.generateReport(command);

        expectLastCall("POST", "/knowledge/quality/report/generate", command);
    });

    it("sends page request", async () => {
        const query: QualityReportPageQuery = {
            pageNo: 1,
            pageSize: 20,
            graphVersionId: 71,
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001,
            reportStatus: "PUBLISHED"
        };

        await service.pageReports(query);

        expectLastCall("POST", "/knowledge/quality/report/page", query);
    });

    it("sends detail and latest requests", async () => {
        await service.getReportDetail({ reportId: 1001 });
        expectLastCall("POST", "/knowledge/quality/report/detail", { reportId: 1001 });

        await service.getLatestReport({ graphVersionId: 71 });
        expectLastCall("POST", "/knowledge/quality/report/latest", { graphVersionId: 71 });
    });
});
