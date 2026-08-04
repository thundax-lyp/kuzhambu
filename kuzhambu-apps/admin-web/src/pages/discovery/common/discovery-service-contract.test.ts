import { beforeEach, describe, expect, it, vi } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import * as qaConsumerService from "@/pages/discovery/qa/qa-service";
import * as qaService from "@/pages/discovery/qa-console/qa-console-service";
import * as searchConsumerService from "@/pages/discovery/search/search-service";
import * as searchService from "@/pages/discovery/search-statistic/search-statistic-service";

const postEventStream = vi.hoisted(() => vi.fn());
const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postEventStream,
    postJson
}));

describe("discovery admin service contracts", () => {
    beforeEach(() => {
        postEventStream.mockReset();
        postJson.mockReset();
    });

    it("maps qa consumer endpoints and request bodies", async () => {
        await qaConsumerService.createQaSession({
            ownerUserId: "1001",
            scope: "PORTAL",
            title: "知识中心问答"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/open", {
            body: {
                ownerUserId: "1001",
                scope: "PORTAL",
                title: "知识中心问答"
            }
        });

        await qaConsumerService.pageQaSessions({
            ownerUserId: "1001",
            pageNo: 1,
            pageSize: 20,
            scope: "PORTAL"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/page", {
            body: {
                ownerUserId: "1001",
                pageNo: 1,
                pageSize: 20,
                scope: "PORTAL"
            }
        });

        await qaConsumerService.getQaSession({ ownerUserId: "1001", sessionId: "7001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/get", {
            body: {
                ownerUserId: "1001",
                sessionId: "7001"
            }
        });

        await qaConsumerService.deleteQaSession({ ownerUserId: "1001", sessionId: "7001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/delete", {
            body: {
                ownerUserId: "1001",
                sessionId: "7001"
            }
        });

        await qaConsumerService.createQaSessionExport({
            format: "CSV",
            ownerUserId: "1001",
            sessionId: "7001"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/export", {
            body: {
                format: "CSV",
                ownerUserId: "1001",
                sessionId: "7001"
            }
        });

        await qaConsumerService.createQaChatCompletion({
            messages: [{ content: "礼学是什么？", role: "user" }],
            metadata: {
                contextMode: "GENERAL",
                sessionId: "7001"
            },
            model: "kuzhambu-qa",
            sessionId: "7001",
            stream: false
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/chat/completions", {
            body: {
                messages: [{ content: "礼学是什么？", role: "user" }],
                metadata: {
                    contextMode: "GENERAL",
                    sessionId: "7001"
                },
                model: "kuzhambu-qa",
                sessionId: "7001",
                stream: false
            }
        });
    });

    it("maps qa console endpoints and request bodies", async () => {
        await qaService.getKnowledgeHealth();
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/health");

        await qaService.rebuildKnowledge({ requestId: "REQ-1", traceId: "TRACE-1" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/rebuild", {
            body: {
                requestId: "REQ-1",
                traceId: "TRACE-1"
            }
        });

        await qaService.createKnowledgeSync({
            contentId: "1001",
            contentType: "SANCAI_ENTRY",
            currentVersionNo: 2,
            requestId: "REQ-2",
            traceId: "TRACE-2"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/sync", {
            body: {
                contentId: "1001",
                contentType: "SANCAI_ENTRY",
                currentVersionNo: 2,
                requestId: "REQ-2",
                traceId: "TRACE-2"
            }
        });

        await qaService.pageKnowledgeSyncItems({
            contentType: "SANCAI_ENTRY",
            pageNo: 1,
            pageSize: 10,
            syncStatus: "SUCCEEDED"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/sync/page", {
            body: {
                contentType: "SANCAI_ENTRY",
                pageNo: 1,
                pageSize: 10,
                syncStatus: "SUCCEEDED"
            }
        });

        await qaService.getQaSession({ sessionId: "2001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/get", {
            body: {
                sessionId: "2001"
            }
        });

        await qaService.pageQaSessions({
            openedAtEnd: "2026-01-31T23:59:59.999Z",
            openedAtStart: "2026-01-01T00:00:00.000Z",
            pageNo: 1,
            pageSize: 10,
            title: "礼器"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/page", {
            body: {
                openedAtEnd: "2026-01-31T23:59:59.999Z",
                openedAtStart: "2026-01-01T00:00:00.000Z",
                pageNo: 1,
                pageSize: 10,
                title: "礼器"
            }
        });

        await qaService.deleteQaSession({ requesterUserId: "1001", sessionId: "2001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/delete", {
            body: {
                requesterUserId: "1001",
                sessionId: "2001"
            }
        });

        await qaService.createQaSessionExport({
            format: "CSV",
            requesterUserId: "1001",
            sessionId: "2001"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/export", {
            body: {
                format: "CSV",
                requesterUserId: "1001",
                sessionId: "2001"
            }
        });

        const calledUrls = postJson.mock.calls.map(([url]) => String(url));
        expect(calledUrls.join("\n")).not.toMatch(/fastgpt|provider|dataset|collection/iu);
        expect(calledUrls.join("\n")).not.toMatch(/source\/list|trace\/get/iu);
    });

    it("keeps qa console service on Discovery APIs without provider direct urls", () => {
        const serviceSource = readFileSync(
            resolve(process.cwd(), "src/pages/discovery/qa-console/qa-console-service.ts"),
            "utf-8"
        );

        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/health");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/rebuild");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/sync");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/sync/page");
        expect(serviceSource).toContain("/discovery/qa-admin/session/delete");
        expect(serviceSource).toContain("/discovery/qa-admin/session/export");
        expect(serviceSource).not.toMatch(/https?:\/\/|fastgpt|dataset|collection|appId|baseUrl/iu);
    });

    it("preserves qa consumer stream error event message", async () => {
        const onError = vi.fn();
        postEventStream.mockImplementationOnce(async (_url, options) => {
            options.onChunk('event:error\ndata:{"message":"FastGPT appId 未配置"}\n\n');
        });

        await expect(
            qaConsumerService.createQaChatCompletionStream({
                command: {
                    messages: [{ content: "礼器是什么？", role: "user" }],
                    metadata: { sessionId: "7001" },
                    model: "kuzhambu-qa",
                    sessionId: "7001",
                    stream: true
                },
                onError
            })
        ).rejects.toThrow("FastGPT appId 未配置");

        expect(onError).toHaveBeenCalledWith("FastGPT appId 未配置");
    });

    it("maps search admin endpoints and request bodies", async () => {
        await searchService.pageSearchEvents({
            dateFrom: "2026-01-01T00:00:00.000Z",
            dateTo: "2026-01-02T23:59:59.000Z",
            intentTypes: ["REWRITE"],
            operatorId: "admin",
            pageNo: 1,
            pageSize: 20,
            queryText: "礼器",
            searchStatuses: ["SUCCESS"]
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-statistics/events/page", {
            body: {
                dateFrom: "2026-01-01T00:00:00.000Z",
                dateTo: "2026-01-02T23:59:59.000Z",
                intentTypes: ["REWRITE"],
                operatorId: "admin",
                pageNo: 1,
                pageSize: 20,
                queryText: "礼器",
                searchStatuses: ["SUCCESS"]
            }
        });

        await searchService.getSearchEventDetail({ id: "EVT-1001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-statistics/events/get", {
            body: {
                id: "EVT-1001"
            }
        });

        await searchService.rebuildSearchIndex({ confirm: true });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-statistics/index/rebuild", {
            body: {
                confirm: true
            }
        });
    });

    it("maps search consumer endpoints and request bodies", async () => {
        await searchConsumerService.searchDiscovery({
            categoryCodes: ["SANCAI_ENTRY"],
            dateFrom: "2026-01-01T00:00:00.000Z",
            dateTo: "2026-01-02T23:59:59.000Z",
            knowledgeBases: ["SANCAI_ENTRY"],
            pageNo: 1,
            pageSize: 10,
            queryText: "辞官",
            tagNames: ["礼制"]
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search/search", {
            body: {
                categoryCodes: ["SANCAI_ENTRY"],
                dateFrom: "2026-01-01T00:00:00.000Z",
                dateTo: "2026-01-02T23:59:59.000Z",
                knowledgeBases: ["SANCAI_ENTRY"],
                pageNo: 1,
                pageSize: 10,
                queryText: "辞官",
                tagNames: ["礼制"]
            }
        });

        await searchConsumerService.clickSearchResult({
            contentDomain: "classics",
            contentId: "1001",
            contentTitle: "礼器",
            contentType: "SANCAI_ENTRY",
            groupRank: 1,
            resultGroupKey: "SANCAI_ENTRY",
            resultRank: 1,
            searchEventId: "EVT-1001",
            targetPath: "/classics/sancai"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search/click", {
            body: {
                contentDomain: "classics",
                contentId: "1001",
                contentTitle: "礼器",
                contentType: "SANCAI_ENTRY",
                groupRank: 1,
                resultGroupKey: "SANCAI_ENTRY",
                resultRank: 1,
                searchEventId: "EVT-1001",
                targetPath: "/classics/sancai"
            }
        });

        await searchConsumerService.previewSearchResult({
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search/preview", {
            body: {
                contentId: "1001",
                contentType: "SANCAI_ENTRY"
            }
        });
    });
});
