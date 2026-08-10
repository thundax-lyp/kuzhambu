import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./taxonomy-service";

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";
const capturedCalls: CapturedCall[] = [];
let responseData: unknown = true;

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
                data: responseData
            }),
            {
                headers: { "Content-Type": "application/json" },
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

describe("taxonomy service merge contracts", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        responseData = true;
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

    it("sends merge preview, apply, deprecate and metrics requests", async () => {
        const request = {
            sourceTagId: "1001",
            targetTagId: "1002"
        };

        await service.previewTagMergeImpact(request);
        expectLastCall("POST", "/knowledge/taxonomy/tag/merge/preview", request);

        await service.applyTagMerge(request);
        expectLastCall("POST", "/knowledge/taxonomy/tag/merge/apply", request);

        await service.deprecateTag({ id: "1001" });
        expectLastCall("POST", "/knowledge/taxonomy/tag/deprecate", {
            id: "1001"
        });

        await service.getTagGovernanceMetrics({ topLimit: 10, recentMonths: 6 });
        expectLastCall("POST", "/knowledge/taxonomy/tag/metrics/list", {
            topLimit: 10,
            recentMonths: 6
        });
    });

    it("sends batch merge, deprecate and review requests", async () => {
        const batchMergeRequest = {
            sourceTagIds: ["1001", "1003"],
            targetTagId: "1002"
        };

        await service.previewTagBatchMergeImpact(batchMergeRequest);
        expectLastCall("POST", "/knowledge/taxonomy/tag/merge/list/preview", batchMergeRequest);

        await service.applyTagBatchMerge(batchMergeRequest);
        expectLastCall("POST", "/knowledge/taxonomy/tag/merge/list/apply", batchMergeRequest);

        await service.deprecateBatchTags({ tagIds: ["1001", "1003"] });
        expectLastCall("POST", "/knowledge/taxonomy/tag/deprecate/list", {
            tagIds: ["1001", "1003"]
        });

        const batchReviewRequest = {
            tagIds: ["1001", "1003"],
            decision: "APPROVE" as const,
            categoryId: "11",
            reviewNote: "批量通过"
        };
        await service.reviewBatchTags(batchReviewRequest);
        expectLastCall("POST", "/knowledge/taxonomy/tag/review/list", batchReviewRequest);
    });

    it("sends tag extraction and candidate apply requests", async () => {
        responseData = {
            aiCallId: "501",
            aiCandidateId: "601",
            status: "SUCCEEDED",
            resultFormat: "STRUCTURED",
            resultPayload:
                '{"tags":[{"name":"礼制","categoryId":"11","categoryName":"制度","confidence":0.91,"reason":"匹配既有标签","matchedExistingTagId":"21"}]}'
        };
        const extractionRequest = {
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001",
            contentTitle: "条目标题",
            contentText: "正文片段",
            modelId: "401",
            modelName: "gpt-5",
            promptVersionId: "301",
            maxTags: 8,
            allowNewTags: true
        };

        const result = await service.requestTagExtraction(extractionRequest);

        expectLastCall("POST", "/knowledge/taxonomy/tag/extract", extractionRequest);
        expect(result.aiCallId).toBe("501");
        expect(result.aiCandidateId).toBe("601");
        expect(result.candidates).toEqual([
            {
                name: "礼制",
                categoryId: "11",
                categoryName: "制度",
                confidence: 0.91,
                reason: "匹配既有标签",
                matchedExistingTagId: "21"
            }
        ]);

        responseData = true;
        const applyRequest = {
            aiCandidateId: "601",
            selectedTags: result.candidates ?? [],
            reviewNote: "AI 审核",
            reviewedBy: "201"
        };
        await service.applyExtractedTags(applyRequest);
        expectLastCall("POST", "/knowledge/taxonomy/tag/extract/apply", applyRequest);
    });

    it("normalizes numeric prompt template and version ids at the response boundary", async () => {
        const responseByPath = new Map<string, unknown>([
            ["/knowledge/taxonomy/tag/extract/apply", true],
            [
                "/ai/config/prompt/template/list",
                [
                    {
                        id: 201,
                        capability: "KNOWLEDGE_TAG_EXTRACT",
                        name: "知识标签提取",
                        currentVersionNo: 3
                    }
                ]
            ],
            [
                "/ai/config/prompt/version/list",
                [
                    {
                        id: 301,
                        templateId: 201,
                        versionNo: 3,
                        registeredAt: "2026-07-01T00:00:00.000Z"
                    }
                ]
            ]
        ]);
        vi.mocked(globalThis.fetch).mockImplementation(async (input, init) => {
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
                    data: responseByPath.get(path) ?? true
                }),
                {
                    headers: { "Content-Type": "application/json" },
                    status: 200
                }
            );
        });

        const versions = await service.listTagExtractionPromptVersions();

        expect(capturedCalls.at(-1)).toEqual({
            body: { id: "201" },
            method: "POST",
            path: "/ai/config/prompt/version/list"
        });
        expect(versions).toEqual([
            {
                id: "301",
                templateId: "201",
                templateName: "知识标签提取",
                capability: "KNOWLEDGE_TAG_EXTRACT",
                versionNo: 3,
                registeredAt: "2026-07-01T00:00:00.000Z"
            }
        ]);
    });
});
