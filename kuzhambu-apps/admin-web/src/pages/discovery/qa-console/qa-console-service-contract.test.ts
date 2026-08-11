import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./qa-console-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("qa console service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps qa console endpoints and request bodies", async () => {
        await service.getKnowledge();
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/get");

        await service.rebuildKnowledge({ requestId: "REQ-1", traceId: "TRACE-1" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/rebuild", {
            body: {
                requestId: "REQ-1",
                traceId: "TRACE-1"
            }
        });

        await service.updateKnowledge({
            contentId: "1001",
            contentType: "SANCAI_ENTRY",
            currentVersionNo: 2,
            requestId: "REQ-2",
            traceId: "TRACE-2"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/update", {
            body: {
                contentId: "1001",
                contentType: "SANCAI_ENTRY",
                currentVersionNo: 2,
                requestId: "REQ-2",
                traceId: "TRACE-2"
            }
        });

        await service.pageKnowledgeSyncItems({
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

        await service.getQaSession({ sessionId: "2001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/get", {
            body: {
                sessionId: "2001"
            }
        });

        await service.pageQaSessions({
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

        await service.deleteQaSession({ requesterUserId: "1001", sessionId: "2001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/delete", {
            body: {
                requesterUserId: "1001",
                sessionId: "2001"
            }
        });

        await service.downloadQaSession({
            format: "CSV",
            requesterUserId: "1001",
            sessionId: "2001"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/download", {
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

        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/get");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/rebuild");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/update");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/sync/page");
        expect(serviceSource).toContain("/discovery/qa-admin/session/delete");
        expect(serviceSource).toContain("/discovery/qa-admin/session/download");
        expect(serviceSource).not.toMatch(/https?:\/\/|fastgpt|dataset|collection|appId|baseUrl/iu);
    });
});
