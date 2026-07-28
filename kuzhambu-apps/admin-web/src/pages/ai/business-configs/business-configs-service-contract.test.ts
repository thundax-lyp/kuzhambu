import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./business-configs-service";

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";

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
        readFetchBody(init?.body);
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

describe("business configs service ID contracts", () => {
    beforeEach(() => {
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

    it("normalizes numeric config, model and prompt ids from Java AI config endpoints", async () => {
        installFetchRecorder(
            new Map<string, unknown>([
                [
                    "/ai/config/business-config/list",
                    [
                        {
                            id: 700001,
                            capability: "classics_summary",
                            promptTemplateId: 800001,
                            modelId: 900001,
                            enabled: true
                        }
                    ]
                ],
                [
                    "/ai/config/model/list",
                    [
                        {
                            id: 900001,
                            apiSource: "OPENAI",
                            baseUrl: "https://example.test/v1",
                            modelName: "gpt-4o",
                            capabilities: [],
                            enabled: true
                        }
                    ]
                ],
                [
                    "/ai/config/prompt/template/list",
                    [
                        {
                            id: 800001,
                            capability: "classics_summary",
                            name: "古籍摘要提示词",
                            enabled: true
                        }
                    ]
                ]
            ])
        );

        await expect(service.listBusinessConfigs()).resolves.toEqual([
            expect.objectContaining({
                id: "700001",
                promptTemplateId: "800001",
                modelId: "900001"
            })
        ]);
        await expect(service.listBusinessConfigModels()).resolves.toEqual([
            expect.objectContaining({ id: "900001" })
        ]);
        await expect(service.listBusinessConfigPrompts()).resolves.toEqual([
            expect.objectContaining({ id: "800001" })
        ]);
    });
});
