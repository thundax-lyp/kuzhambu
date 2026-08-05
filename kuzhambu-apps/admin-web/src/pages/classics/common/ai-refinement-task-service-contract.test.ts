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
                    taskId: "7001",
                    status: "PENDING",
                    capability: "CLASSICS_SUMMARY",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
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
            capability: "CLASSICS_SUMMARY",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            objectId: null,
            serviceRole: "PRIMARY",
            modelId: "11",
            modelName: "gpt-test",
            promptVersionId: "22",
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

    it("labels tags and qa capabilities as retryable task types", () => {
        expect(aiRefinementTaskService.getTaskCapabilityLabel("tags")).toBe("标签");
        expect(aiRefinementTaskService.getTaskCapabilityLabel("qa")).toBe("问答");
        expect(aiRefinementTaskService.getTaskRetryable("FAILED", "tags")).toBe(true);
        expect(aiRefinementTaskService.getTaskRetryable("PARTIAL", "qa")).toBe(true);
    });

    it("prefers text task ids to avoid precision loss for backend long ids", () => {
        expect(
            aiRefinementTaskService.getTaskStableId("869888422381092900", "869888422381092864")
        ).toBe("869888422381092864");
    });

    it("sorts equal-time decimal ids by numeric order", () => {
        const records = [
            { id: "9", requestedAt: "2026-07-28T01:00:00Z" },
            { id: "10", requestedAt: "2026-07-28T01:00:00Z" }
        ];

        expect(
            records.sort((left, right) =>
                aiRefinementTaskService.sortNewestByRequestedAtThenId({ left, right })
            )
        ).toEqual([
            { id: "10", requestedAt: "2026-07-28T01:00:00Z" },
            { id: "9", requestedAt: "2026-07-28T01:00:00Z" }
        ]);
    });

    it("explains task failures with user-facing categories and diagnostic codes", () => {
        expect(
            aiRefinementTaskService.getTaskFailureText(
                "WORKER_REQUEST",
                "MODEL_PROVIDER_UNAVAILABLE",
                "业务处理失败"
            )
        ).toBe(
            "模型服务暂时不可用，请稍后重试或切换模型（阶段：WORKER_REQUEST；类型：MODEL_PROVIDER_UNAVAILABLE）"
        );
        expect(
            aiRefinementTaskService.getTaskFailureText(
                "WORKER_REQUEST",
                "UNSUPPORTED_MODEL_API_SOURCE",
                "api source not supported"
            )
        ).toBe(
            "模型服务来源不支持，请切换模型或修正模型配置（阶段：WORKER_REQUEST；类型：UNSUPPORTED_MODEL_API_SOURCE；详情：api source not supported）"
        );
        expect(
            aiRefinementTaskService.getTaskFailureText(
                "WORKER_REQUEST",
                "WORKER_PROTOCOL_FAILURE",
                "workers 仅支持 OpenAI-compatible 模型接口。"
            )
        ).toBe(
            "模型服务来源不支持，请切换模型或修正模型配置（阶段：WORKER_REQUEST；类型：WORKER_PROTOCOL_FAILURE；详情：workers 仅支持 OpenAI-compatible 模型接口。）"
        );
        expect(
            aiRefinementTaskService.getTaskFailureText(
                "WORKER_RESULT",
                "MODEL_OUTPUT_INVALID_JSON",
                "expected JSON object"
            )
        ).toBe(
            "模型应答格式错误，请检查提示词模板、输出 Schema 或模型能力（阶段：WORKER_RESULT；类型：MODEL_OUTPUT_INVALID_JSON；详情：expected JSON object）"
        );
    });

    it("falls back to stage or raw message for unknown task failures", () => {
        expect(
            aiRefinementTaskService.getTaskFailureText(
                "WORKER_STREAM",
                "UNKNOWN_ERROR",
                "bad stream"
            )
        ).toBe(
            "AI Worker 流式应答异常（阶段：WORKER_STREAM；类型：UNKNOWN_ERROR；详情：bad stream）"
        );
        expect(aiRefinementTaskService.getTaskFailureText(null, null, "业务处理失败")).toBe(
            "业务处理失败"
        );
    });

    it("gets task by id", async () => {
        await aiRefinementTaskService.getTask({ taskId: "7001" });

        expect(capturedCalls.at(-1)).toEqual({
            body: {
                taskId: "7001"
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
            capability: "CLASSICS_SUMMARY",
            status: "RUNNING",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            pageNo: 1,
            pageSize: 20
        });

        expect(capturedCalls.at(-1)).toEqual({
            body: {
                capability: "CLASSICS_SUMMARY",
                status: "RUNNING",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
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

    it("cancels task by id", async () => {
        const command: AiRefinementTaskCancelCommand = {
            taskId: "7001"
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
            taskId: "7001",
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
