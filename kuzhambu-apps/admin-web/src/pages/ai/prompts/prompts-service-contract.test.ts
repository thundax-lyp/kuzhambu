import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./prompts-service";

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

describe("prompts service ID contracts", () => {
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

    it("normalizes numeric template, version and variable ids from Java prompt endpoints", async () => {
        installFetchRecorder(
            new Map<string, unknown>([
                [
                    "/ai/config/prompt/template/list",
                    [
                        {
                            id: 1001,
                            capability: "classics_summary",
                            name: "摘要提示词",
                            enabled: true,
                            currentVersionNo: 2
                        }
                    ]
                ],
                [
                    "/ai/config/prompt/version/current",
                    {
                        id: 2002,
                        templateId: 1001,
                        versionNo: 2,
                        messageTemplatesJson: "[]"
                    }
                ],
                [
                    "/ai/config/prompt/version/list",
                    [
                        {
                            id: 2001,
                            templateId: 1001,
                            versionNo: 1
                        }
                    ]
                ],
                [
                    "/ai/config/prompt/variable/list",
                    [
                        {
                            id: 3001,
                            templateId: 1001,
                            variableName: "title",
                            required: true
                        }
                    ]
                ]
            ])
        );

        await expect(service.listPromptTemplates()).resolves.toEqual([
            expect.objectContaining({ id: "1001" })
        ]);
        await expect(service.getCurrentPromptVersion("1001")).resolves.toEqual(
            expect.objectContaining({ id: "2002", templateId: "1001" })
        );
        await expect(service.listPromptVersions("1001")).resolves.toEqual([
            expect.objectContaining({ id: "2001", templateId: "1001" })
        ]);
        await expect(service.listPromptVariables("1001")).resolves.toEqual([
            expect.objectContaining({ id: "3001", templateId: "1001" })
        ]);
        expect(capturedCalls.map((call) => call.body)).toEqual([
            {},
            { id: "1001" },
            { id: "1001" },
            { id: "1001" }
        ]);
    });
});
