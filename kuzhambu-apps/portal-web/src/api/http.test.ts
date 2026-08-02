import { afterEach, describe, expect, it, vi } from "vitest";
import { buildApiUrl, getJson, postJson } from "./http";

describe("portal web http utilities", () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("builds same-origin URLs as path + query and filters empty query values", () => {
        expect(
            buildApiUrl("/portal/classics", {
                keyword: "三才",
                empty: "",
                nilValue: null,
                undefinedValue: undefined,
                page: 1
            })
        ).toBe("/kuzhambu-api/api/portal/classics?keyword=%E4%B8%89%E6%89%8D&page=1");
    });

    it("getJson uses GET and returns payload data", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(
            async () =>
                new Response(
                    JSON.stringify({
                        code: "COMMON-00000",
                        data: {
                            id: 10001,
                            title: "三才条目"
                        },
                        message: "ok"
                    }),
                    {
                        headers: {
                            "Content-Type": "application/json"
                        },
                        status: 200
                    }
                )
        );

        const data = await getJson<{ id: number; title: string }>("/portal/classics/sancai", {
            pageNo: 1
        });

        expect(data).toEqual({
            id: 10001,
            title: "三才条目"
        });
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-api/api/portal/classics/sancai?pageNo=1",
            {
                headers: {
                    Accept: "application/json"
                },
                method: "GET"
            }
        );
    });

    it("getJson throws when response is not ok", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(
            async () => new Response("error", { status: 500 })
        );

        await expect(getJson("/portal/classics/sancai", {})).rejects.toThrow(
            "Portal API request failed"
        );
    });

    it("postJson uses POST and sends a json body", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(
            async () =>
                new Response(
                    JSON.stringify({
                        code: "COMMON-00000",
                        data: {
                            accepted: true
                        },
                        message: "ok"
                    }),
                    {
                        headers: {
                            "Content-Type": "application/json"
                        },
                        status: 200
                    }
                )
        );

        const data = await postJson<{ accepted: boolean }, { queryText: string }>(
            "/portal/discovery/search/search",
            {
                queryText: "古籍"
            }
        );

        expect(data).toEqual({
            accepted: true
        });
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-api/api/portal/discovery/search/search",
            {
                body: JSON.stringify({
                    queryText: "古籍"
                }),
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json"
                },
                method: "POST"
            }
        );
    });
});
