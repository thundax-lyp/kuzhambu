import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import type {
    AiCandidateApplyCommand,
    AiCandidateRejectCommand
} from "@/pages/classics/common/ai-candidate-service";

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
    const candidate = {
        candidateId: 7001,
        contentType: "SANCAI_ENTRY",
        contentId: 3001,
        objectId: 5001,
        candidateIdList: [7001],
        capability: "CLASSICS_SUMMARY",
        resultFormat: "TEXT",
        status: "PENDING",
        resultPayload: "ok"
    };
    if (path.endsWith("/list")) {
        return [candidate];
    }
    if (path.endsWith("/change")) {
        return {
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            versionId: 5002,
            versionNo: 2
        };
    }
    return candidate;
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

describe("AI candidate service request contracts", () => {
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

    it("lists candidates by content type and id", async () => {
        const candidates = await aiCandidateService.list({
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            objectId: "5001",
            capability: "CLASSICS_SUMMARY",
            status: "PENDING"
        });

        expect(candidates[0]).toEqual(
            expect.objectContaining({
                candidateId: "7001",
                contentId: "3001",
                objectId: "5001"
            })
        );
        expect(capturedCalls.at(-1)).toEqual({
            body: {
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: "5001",
                capability: "CLASSICS_SUMMARY",
                status: "PENDING"
            },
            method: "POST",
            path: "/ai/invocation/candidate/list"
        });
    });

    it("gets candidate by stable id text", async () => {
        await aiCandidateService.get({
            candidateId: "869897501442834432"
        });

        expect(capturedCalls.at(-1)).toEqual({
            body: {
                candidateId: "869897501442834432"
            },
            method: "POST",
            path: "/ai/invocation/candidate/get"
        });
    });

    it("applies candidate by invoking classics content apply api", async () => {
        const command: AiCandidateApplyCommand = {
            candidateId: "869897501442834432",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            capability: "CLASSICS_SUMMARY",
            objectId: "5001",
            resultFormat: "TEXT",
            resultPayload: "new summary",
            changeSummary: "AI 应用：摘要"
        };

        const result = await aiCandidateService.apply(command);

        expect(result).toEqual(
            expect.objectContaining({
                contentId: "3001",
                versionId: "5002"
            })
        );
        expect(capturedCalls.at(-1)).toEqual({
            body: command,
            method: "POST",
            path: "/classics/content/ai-candidates/change"
        });
    });

    it("rejects candidate with error info", async () => {
        const request: AiCandidateRejectCommand = {
            candidateId: "869897501442834432",
            errorType: "INVALID",
            errorMessage: "invalid"
        };

        await aiCandidateService.reject(request);

        expect(capturedCalls.at(-1)).toEqual({
            body: request,
            method: "POST",
            path: "/ai/invocation/candidate/reject"
        });
    });
});
