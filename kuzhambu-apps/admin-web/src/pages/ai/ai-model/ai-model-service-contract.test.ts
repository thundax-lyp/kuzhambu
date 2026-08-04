import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./ai-model-service";

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

const installFetchRecorder = (responseByPath: Map<string, unknown>) => {
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
                data: responseByPath.get(path) ?? true
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200
            }
        );
    });
};

describe("AI model service ID contracts", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("normalizes numeric model ids from Java AI model endpoints", async () => {
        installFetchRecorder(
            new Map<string, unknown>([
                [
                    "/ai/config/model/list",
                    [
                        {
                            id: 2001,
                            apiSource: "OPENAI",
                            baseUrl: "https://example.test/v1",
                            modelName: "gpt-4o",
                            capabilities: [],
                            enabled: true
                        }
                    ]
                ],
                [
                    "/ai/config/model/update",
                    {
                        id: 2001,
                        apiSource: "OPENAI",
                        baseUrl: "https://example.test/v1",
                        modelName: "gpt-4o",
                        capabilities: [],
                        enabled: false
                    }
                ]
            ])
        );

        await expect(service.listAiModels()).resolves.toEqual([
            expect.objectContaining({ id: "2001" })
        ]);
        await expect(
            service.changeAiModel({
                id: "2001",
                apiSource: "OPENAI",
                baseUrl: "https://example.test/v1",
                modelName: "gpt-4o",
                capabilities: [],
                enabled: false
            })
        ).resolves.toEqual(expect.objectContaining({ id: "2001" }));
    });
});
