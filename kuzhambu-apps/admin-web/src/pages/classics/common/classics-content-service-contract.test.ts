import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as contentService from "@/pages/classics/common/classics-content-service";
import type {
    ClassicsAiCandidateBatchApplyCommand,
    ClassicsAiCandidateBatchRejectCommand,
    ClassicsBatchVisibilityCommand,
    ClassicsContentQaPairCommand,
    ClassicsContentQaPairDeleteCommand,
    ClassicsContentQaPairSortCommand,
    ClassicsContentTagCommand,
    ClassicsContentTagDeleteCommand,
    ClassicsContentTagSortCommand
} from "@/pages/classics/common/classics-content-service";

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
        const path = url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "");
        capturedCalls.push({
            body: readFetchBody(init?.body),
            method: init?.method,
            path
        });

        const data = (() => {
            if (path === "/classics/content/visibility/change") {
                return {
                    failureCount: 1,
                    failures: [
                        {
                            contentId: 4002,
                            contentType: "WANGQI_DOCUMENT",
                            failureCode: "BATCH_VISIBILITY_FAILED",
                            failureReason: "内容不存在",
                            resultId: null,
                            status: null
                        }
                    ],
                    successCount: 1,
                    successes: [
                        {
                            contentId: 4001,
                            contentType: "WANGQI_DOCUMENT",
                            failureCode: null,
                            failureReason: null,
                            resultId: 4001,
                            status: "PUBLIC"
                        }
                    ]
                };
            }

            if (path === "/classics/content/ai-candidates/batch/apply") {
                return {
                    failureCount: 1,
                    failures: [
                        {
                            candidateId: 7002,
                            contentId: 4002,
                            contentType: "WANGQI_DOCUMENT",
                            capability: "summary",
                            failureCode: "INVALID_FORMAT",
                            failureReason: "Payload 不符合结构化摘要约束"
                        }
                    ],
                    successCount: 1,
                    successes: [
                        {
                            candidateId: 7001,
                            contentId: 4001,
                            contentType: "WANGQI_DOCUMENT",
                            capability: "summary",
                            objectId: null,
                            resultId: 9001,
                            status: "APPLIED"
                        }
                    ]
                };
            }

            if (path === "/classics/content/ai-candidates/batch/reject") {
                return {
                    failureCount: 0,
                    failures: [],
                    successCount: 2,
                    successes: [
                        {
                            candidateId: 8001,
                            contentId: 5001,
                            contentType: "MING_CUSTOMS",
                            capability: "tags",
                            objectId: null,
                            resultId: 8001,
                            status: "REJECTED"
                        },
                        {
                            candidateId: 8002,
                            contentId: 5002,
                            contentType: "MING_CUSTOMS",
                            capability: "qa",
                            objectId: 9002,
                            resultId: 8002,
                            status: "REJECTED"
                        }
                    ]
                };
            }

            return true;
        })();

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data
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

describe("classics content service request contracts", () => {
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

    it("sends POST list requests for tags and qa pairs", async () => {
        const tagQuery = {
            contentType: "SANCAI_ENTRY",
            contentId: 3001
        };
        const qaPairQuery = {
            contentType: "WANGQI_DOCUMENT",
            contentId: 4001
        };

        await contentService.listTags(tagQuery);
        expectLastCall("POST", "/classics/content/tags/list", tagQuery);

        await contentService.listQaPairs(qaPairQuery);
        expectLastCall("POST", "/classics/content/qa-pairs/list", qaPairQuery);
    });

    it("sends tag add/update/sort commands", async () => {
        const addTagCommand: ClassicsContentTagCommand = {
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            tagNameSnapshot: "礼制",
            source: "MANUAL",
            status: "ACTIVE"
        };

        await contentService.addTag(addTagCommand);
        expectLastCall("POST", "/classics/content/tags/add", addTagCommand);

        const updateTagCommand: ClassicsContentTagCommand = {
            ...addTagCommand,
            id: 7001,
            tagId: 2001,
            tagNameSnapshot: "祭祀"
        };

        await contentService.updateTag(updateTagCommand);
        expectLastCall("POST", "/classics/content/tags/update", updateTagCommand);

        const deleteTagCommand: ClassicsContentTagDeleteCommand = {
            id: 7001
        };

        await contentService.deleteTag(deleteTagCommand);
        expectLastCall("POST", "/classics/content/tags/delete", deleteTagCommand);

        const sortTagCommand: ClassicsContentTagSortCommand = {
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            orderedIds: [7001, 7002],
            sortDirection: "ASC"
        };

        await contentService.sortTags(sortTagCommand);
        expectLastCall("POST", "/classics/content/tags/sort", sortTagCommand);
    });

    it("sends qa pair add/update/sort commands", async () => {
        const addQaCommand: ClassicsContentQaPairCommand = {
            contentType: "MING_CUSTOMS",
            contentId: 5001,
            question: "太极生两仪？",
            answer: "此言文献常见于道家语境。",
            source: "MANUAL"
        };

        await contentService.addQaPair(addQaCommand);
        expectLastCall("POST", "/classics/content/qa-pairs/add", addQaCommand);

        const updateQaCommand: ClassicsContentQaPairCommand = {
            ...addQaCommand,
            id: 8001,
            answer: "修订后的回答。"
        };

        await contentService.updateQaPair(updateQaCommand);
        expectLastCall("POST", "/classics/content/qa-pairs/update", updateQaCommand);

        const sortQaCommand: ClassicsContentQaPairSortCommand = {
            orderedIds: [8001, 8002],
            sortDirection: "DESC"
        };

        await contentService.sortQaPairs(sortQaCommand);
        expectLastCall("POST", "/classics/content/qa-pairs/sort", sortQaCommand);
    });

    it("sends qa pair delete commands", async () => {
        const deleteQaCommand: ClassicsContentQaPairDeleteCommand = {
            id: 8001
        };

        await contentService.deleteQaPair(deleteQaCommand);
        expectLastCall("POST", "/classics/content/qa-pairs/delete", deleteQaCommand);
    });

    it("sends batch visibility commands and preserves operation result fields", async () => {
        const command: ClassicsBatchVisibilityCommand = {
            contentIds: [4001, 4002],
            contentType: "WANGQI_DOCUMENT",
            visibility: "PUBLIC"
        };

        const response = await contentService.changeVisibilityBatch(command);

        expectLastCall("POST", "/classics/content/visibility/change", command);
        expect(response.successCount).toBe(1);
        expect(response.successes[0]).toEqual({
            contentId: 4001,
            contentType: "WANGQI_DOCUMENT",
            failureCode: null,
            failureReason: null,
            resultId: 4001,
            status: "PUBLIC"
        });
        expect(response.failureCount).toBe(1);
        expect(response.failures[0]).toEqual({
            contentId: 4002,
            contentType: "WANGQI_DOCUMENT",
            failureCode: "BATCH_VISIBILITY_FAILED",
            failureReason: "内容不存在",
            resultId: null,
            status: null
        });
    });

    it("sends ai candidate batch apply commands and preserves operation result fields", async () => {
        const command: ClassicsAiCandidateBatchApplyCommand = {
            items: [
                {
                    candidateId: 7001,
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 4001,
                    capability: "summary",
                    objectId: null,
                    resultFormat: "TEXT",
                    resultPayload: "new summary"
                },
                {
                    candidateId: 7002,
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 4002,
                    capability: "summary",
                    resultFormat: "TEXT",
                    resultPayload: "bad payload"
                }
            ]
        };

        const response = await contentService.applyAiCandidatesBatch(command);

        expectLastCall("POST", "/classics/content/ai-candidates/batch/apply", command);
        expect(response.successCount).toBe(1);
        expect(response.successes[0]).toEqual({
            candidateId: 7001,
            contentId: 4001,
            contentType: "WANGQI_DOCUMENT",
            capability: "summary",
            objectId: null,
            resultId: 9001,
            status: "APPLIED"
        });
        expect(response.failureCount).toBe(1);
        expect(response.failures[0]).toEqual({
            candidateId: 7002,
            contentType: "WANGQI_DOCUMENT",
            contentId: 4002,
            capability: "summary",
            failureCode: "INVALID_FORMAT",
            failureReason: "Payload 不符合结构化摘要约束"
        });
    });

    it("sends ai candidate batch reject commands and preserves operation result fields", async () => {
        const command: ClassicsAiCandidateBatchRejectCommand = {
            errorType: "USER_REJECTED",
            errorMessage: "用户已批量拒绝该 AI 候选",
            items: [
                {
                    candidateId: 8001,
                    contentType: "MING_CUSTOMS",
                    contentId: 5001,
                    capability: "tags"
                },
                {
                    candidateId: 8002,
                    contentType: "MING_CUSTOMS",
                    contentId: 5002,
                    capability: "qa",
                    objectId: 9002
                }
            ]
        };

        const response = await contentService.rejectAiCandidatesBatch(command);

        expectLastCall("POST", "/classics/content/ai-candidates/batch/reject", command);
        expect(response.successCount).toBe(2);
        expect(response.successes[0]).toEqual({
            candidateId: 8001,
            contentId: 5001,
            contentType: "MING_CUSTOMS",
            capability: "tags",
            objectId: null,
            resultId: 8001,
            status: "REJECTED"
        });
        expect(response.successes[1]).toEqual({
            candidateId: 8002,
            contentId: 5002,
            contentType: "MING_CUSTOMS",
            capability: "qa",
            objectId: 9002,
            resultId: 8002,
            status: "REJECTED"
        });
    });
});
