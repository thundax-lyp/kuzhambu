import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import type {
    AiRefinementTaskCancelCommand,
    AiRefinementTaskCreateCommand
} from "@/pages/classics/common/ai-refinement-task-service";

interface CapturedCall {
    body: unknown;
    headers?: HeadersInit;
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
            headers: init?.headers,
            method: init?.method,
            path: url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "")
        });

        if (url.includes("/ai/refinement/task/stream")) {
            const encoder = new TextEncoder();
            return new Response(
                new ReadableStream({
                    start(controller) {
                        controller.enqueue(
                            encoder.encode(
                                'event:delta\ndata: {"eventType":"delta","deltaText":"片段"}\n\n'
                            )
                        );
                        controller.close();
                    }
                }),
                {
                    headers: {
                        "Content-Type": "text/event-stream"
                    },
                    status: 200
                }
            );
        }

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    taskId: 7001,
                    status: "PENDING",
                    capability: "summary",
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001,
                    requestedAt: "2026-07-01T12:00:00Z",
                    items: [],
                    total: 0,
                    pageNo: 1,
                    pageSize: 20
                }
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

describe("AI refinement task service request contracts", () => {
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

    it("creates task by invoking refinement task add api", async () => {
        const command: AiRefinementTaskCreateCommand = {
            capability: "summary",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            objectId: null,
            requestedBy: 99,
            serviceRole: "PRIMARY",
            modelId: 11,
            modelName: "gpt-test",
            promptVersionId: 22,
            requestId: "req-1",
            traceId: "trace-1",
            promptMessagesJson: '[{"role":"user","content":"hello"}]',
            promptVariablesJson: '{"title":"三才"}',
            inputPayloadJson: '{"text":"原文"}',
            outputSchemaJson: '{"type":"TEXT"}',
            forceJson: false,
            locale: "zh-CN"
        };

        await aiRefinementTaskService.createTask(command);

        expect(capturedCalls.at(-1)).toEqual({
            body: command,
            headers: {
                "Access-Token": "test-token",
                "Content-Type": "application/json"
            },
            method: "POST",
            path: "/ai/refinement/task/add"
        });
    });

    it("gets task by id", async () => {
        await aiRefinementTaskService.getTask({ taskId: 7001 });

        expect(capturedCalls.at(-1)).toEqual({
            body: {
                taskId: 7001
            },
            headers: {
                "Access-Token": "test-token",
                "Content-Type": "application/json"
            },
            method: "POST",
            path: "/ai/refinement/task/get"
        });
    });

    it("pages tasks with polling filters", async () => {
        await aiRefinementTaskService.pageTasks({
            capability: "summary",
            status: "RUNNING",
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            requestedBy: 99,
            pageNo: 1,
            pageSize: 20
        });

        expect(capturedCalls.at(-1)).toEqual({
            body: {
                capability: "summary",
                status: "RUNNING",
                contentType: "SANCAI_ENTRY",
                contentId: 3001,
                requestedBy: 99,
                pageNo: 1,
                pageSize: 20
            },
            headers: {
                "Access-Token": "test-token",
                "Content-Type": "application/json"
            },
            method: "POST",
            path: "/ai/refinement/task/page"
        });
    });

    it("cancels task with requester id", async () => {
        const command: AiRefinementTaskCancelCommand = {
            taskId: 7001,
            requestedBy: 99
        };

        await aiRefinementTaskService.cancelTask(command);

        expect(capturedCalls.at(-1)).toEqual({
            body: command,
            headers: {
                "Access-Token": "test-token",
                "Content-Type": "application/json"
            },
            method: "POST",
            path: "/ai/refinement/task/cancel"
        });
    });

    it("subscribes task stream with access token header", async () => {
        const events: unknown[] = [];

        await aiRefinementTaskService.requestTaskStream({
            taskId: 7001,
            onEvent: (event) => events.push(event)
        });

        expect(capturedCalls.at(-1)).toEqual({
            body: undefined,
            headers: {
                "Access-Token": "test-token"
            },
            method: "GET",
            path: "/ai/refinement/task/stream?taskId=7001"
        });
        expect(events).toEqual([
            {
                eventType: "delta",
                deltaText: "片段",
                eventId: null
            }
        ]);
    });
});
