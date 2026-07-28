import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./capability-mappings-service";

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

const installFetchRecorder = (responseByPath: Map<string, unknown>) => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
        const url = readFetchUrl(input);
        const path = url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "");
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

describe("capability mappings service ID contracts", () => {
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

    it("normalizes numeric mapping and model ids from Java AI config endpoints", async () => {
        installFetchRecorder(
            new Map<string, unknown>([
                [
                    "/ai/config/capability/mapping/list",
                    [
                        {
                            mappingId: 3001,
                            scope: "classics",
                            capability: "summary",
                            modelId: 2001,
                            enabled: true
                        }
                    ]
                ],
                [
                    "/ai/config/model/list",
                    [
                        {
                            modelId: 2001,
                            serviceId: 1001,
                            modelName: "gpt-4o",
                            capabilityTags: [],
                            enabled: true
                        }
                    ]
                ],
                ["/ai/config/capability/mapping/save", { id: 3001 }]
            ])
        );

        await expect(service.listCapabilityMappings()).resolves.toEqual([
            expect.objectContaining({ mappingId: "3001", modelId: "2001" })
        ]);
        await expect(service.listEnabledModels()).resolves.toEqual([
            expect.objectContaining({ modelId: "2001", serviceId: "1001" })
        ]);
        await expect(
            service.changeCapabilityMapping({
                mappingId: "3001",
                scope: "classics",
                capability: "summary",
                modelId: "2001",
                enabled: false
            })
        ).resolves.toEqual({ id: "3001" });
    });
});
