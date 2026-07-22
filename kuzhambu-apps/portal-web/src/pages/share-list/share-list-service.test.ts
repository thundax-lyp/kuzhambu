import { afterEach, describe, expect, it, vi } from "vitest";
import * as http from "@/api/http";
import * as shareListService from "./share-list-service";

describe("share list service", () => {
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("listShares forwards query parameters to postJson", async () => {
        const postJsonSpy = vi.spyOn(http, "postJson").mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            records: [],
            totalCount: 0,
            totalPage: 0
        });

        const query = { pageNo: 1, title: "test" };
        await shareListService.listShares(query);

        expect(postJsonSpy).toHaveBeenCalledWith("/portal/classics/shares/list", query);
    });
});
