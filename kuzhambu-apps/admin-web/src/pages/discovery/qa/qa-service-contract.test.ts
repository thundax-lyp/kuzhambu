import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./qa-service";

const postEventStream = vi.hoisted(() => vi.fn());
const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postEventStream,
    postJson
}));

describe("qa service contracts", () => {
    beforeEach(() => {
        postEventStream.mockReset();
        postJson.mockReset();
    });

    it("maps qa consumer endpoints and request bodies", async () => {
        await service.createQaSession({
            ownerUserId: "1001",
            scope: "PORTAL",
            title: "知识中心问答"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/init", {
            body: {
                ownerUserId: "1001",
                scope: "PORTAL",
                title: "知识中心问答"
            }
        });

        await service.pageQaSessions({
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

        await service.getQaSession({ ownerUserId: "1001", sessionId: "7001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/get", {
            body: {
                ownerUserId: "1001",
                sessionId: "7001"
            }
        });

        await service.deleteQaSession({ ownerUserId: "1001", sessionId: "7001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/delete", {
            body: {
                ownerUserId: "1001",
                sessionId: "7001"
            }
        });

        await service.downloadQaSession({
            format: "CSV",
            ownerUserId: "1001",
            sessionId: "7001"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/session/download", {
            body: {
                format: "CSV",
                ownerUserId: "1001",
                sessionId: "7001"
            }
        });

        await service.createQaChatCompletion({
            messages: [{ content: "礼学是什么？", role: "user" }],
            metadata: {
                contextMode: "GENERAL",
                sessionId: "7001"
            },
            model: "kuzhambu-qa",
            sessionId: "7001",
            stream: false
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa/chat/create", {
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

    it("preserves qa consumer stream error event message", async () => {
        const onError = vi.fn();
        postEventStream.mockImplementationOnce(async (_url, options) => {
            options.onChunk('event:error\ndata:{"message":"FastGPT appId 未配置"}\n\n');
        });

        await expect(
            service.submitChatCompletion({
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
});
