import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./cleanup-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("operations cleanup service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps cleanup execute endpoint", async () => {
        await service.requestCleanup({ cleanupType: "EXPIRED_BACKUP" });
        expect(postJson).toHaveBeenLastCalledWith("/operations/cleanup/execute", {
            body: {
                cleanupType: "EXPIRED_BACKUP"
            }
        });
    });

    it("maps cleanup page and detail endpoints", async () => {
        await service.pageCleanups({
            cleanupType: "EXPIRED_BACKUP",
            cleanupStatus: "SUCCEEDED",
            requesterUserId: 1001,
            pageNo: 1,
            pageSize: 20
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/cleanup/page", {
            body: {
                cleanupType: "EXPIRED_BACKUP",
                cleanupStatus: "SUCCEEDED",
                requesterUserId: 1001,
                pageNo: 1,
                pageSize: 20
            }
        });

        await service.getCleanupDetail({ cleanupId: 901 });
        expect(postJson).toHaveBeenLastCalledWith("/operations/cleanup/detail", {
            body: {
                cleanupId: 901
            }
        });
    });
});
